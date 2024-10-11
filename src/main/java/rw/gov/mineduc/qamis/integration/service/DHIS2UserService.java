package rw.gov.mineduc.qamis.integration.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import rw.gov.mineduc.qamis.integration.config.DHIS2Config;
import rw.gov.mineduc.qamis.integration.model.DHIS2User;
import rw.gov.mineduc.qamis.integration.model.SyncInfo;
import rw.gov.mineduc.qamis.integration.repository.DHIS2UserRepository;
import rw.gov.mineduc.qamis.integration.repository.SyncInfoRepository;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DHIS2UserService {

    private static final String SYNC_INFO_ID = "DHIS2_SYNC";

    // This service acts as a caching layer for DHIS2 users.
    // It periodically synchronizes with DHIS2 and stores user data locally,
    // allowing external applications to query user data without directly accessing DHIS2.

    @Autowired
    private DHIS2UserRepository dhis2UserRepository;

    @Autowired
    private SyncInfoRepository syncInfoRepository;

    @Autowired
    private DHIS2Config dhis2Config;

    @Autowired
    private RestTemplate restTemplate;

    public Page<DHIS2User> searchUsers(
            List<String> userRoleIds,
            List<String> userGroupIds,
            List<String> organisationUnitIds,
            String username,
            String displayName,
            String firstName,
            String surname,
            Boolean disabled,
            LocalDateTime lastUpdatedStart,
            LocalDateTime lastUpdatedEnd,
            Pageable pageable) {

        Specification<DHIS2User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userRoleIds != null && !userRoleIds.isEmpty()) {
                predicates.add(root.join("userRoleIds").in(userRoleIds));
            }
            if (userGroupIds != null && !userGroupIds.isEmpty()) {
                predicates.add(root.join("userGroupIds").in(userGroupIds));
            }
            if (organisationUnitIds != null && !organisationUnitIds.isEmpty()) {
                predicates.add(root.join("organisationUnitIds").in(organisationUnitIds));
            }
            if (username != null && !username.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("username")), "%" + username.toLowerCase() + "%"));
            }
            if (displayName != null && !displayName.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("displayName")), "%" + displayName.toLowerCase() + "%"));
            }
            if (firstName != null && !firstName.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%"));
            }
            if (surname != null && !surname.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("surname")), "%" + surname.toLowerCase() + "%"));
            }
            if (disabled != null) {
                predicates.add(cb.equal(root.get("disabled"), disabled));
            }
            if (lastUpdatedStart != null && lastUpdatedEnd != null) {
                predicates.add(cb.between(root.get("lastUpdated"), lastUpdatedStart, lastUpdatedEnd));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return dhis2UserRepository.findAll(spec, pageable);
    }

    public DHIS2User createUser(DHIS2User user) {
        // TODO: Implement DHIS2 API call to create user
        return dhis2UserRepository.save(user);
    }

    public DHIS2User updateUser(String id, DHIS2User user) {
        // TODO: Implement DHIS2 API call to update user
        user.setId(id);
        return dhis2UserRepository.save(user);
    }

    public void deleteUser(String id) {
        // TODO: Implement DHIS2 API call to delete user
        dhis2UserRepository.deleteById(id);
    }

    @Autowired
    private DHIS2DatasetService dhis2DatasetService;

    @Scheduled(cron = "${dhis2.syncCron}")
    public void synchronizeData() {
        synchronizeUsers(null, null, false);
        dhis2DatasetService.synchronizeDatasets();
    }

    public int synchronizeUsers(LocalDateTime fromDate, String userId, boolean syncAll) {
        String url = dhis2Config.getApiUrl() + "/api/users.json?fields=id,username,displayName,firstName,surname,userCredentials[userRoles[id]],userGroups[id],organisationUnits[id],lastUpdated,disabled&paging=false";
        
        if (fromDate != null && !syncAll) {
            url += "&filter=lastUpdated:gte:" + fromDate.format(DateTimeFormatter.ISO_DATE_TIME);
        } else if (!syncAll) {
            LocalDateTime lastSyncTime = getLastSyncTime();
            if (lastSyncTime != null) {
                url += "&filter=lastUpdated:gte:" + lastSyncTime.format(DateTimeFormatter.ISO_DATE_TIME);
            }
        }

        if (userId != null && !userId.isEmpty()) {
            url += "&filter=id:eq:" + userId;
        }

        HttpHeaders headers = createAuthHeaders();
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        List<Map<String, Object>> users = (List<Map<String, Object>>) response.getBody().get("users");

        int syncedCount = 0;
        for (Map<String, Object> userData : users) {
            String id = (String) userData.get("id");
            DHIS2User user = dhis2UserRepository.findById(id).orElse(new DHIS2User());
            updateUserFromData(user, userData);
            dhis2UserRepository.save(user);
            syncedCount++;
        }

        if (syncAll || (fromDate == null && userId == null)) {
            updateLastSyncTime(LocalDateTime.now());
        }

        return syncedCount;
    }

    @Scheduled(cron = "${dhis2.syncCron}")
    public void scheduledSynchronizeUsers() {
        synchronizeUsers(null, null, false);
    }

    private void updateUserFromData(DHIS2User user, Map<String, Object> userData) {
        user.setId((String) userData.get("id"));
        user.setUsername((String) userData.get("username"));
        user.setDisplayName((String) userData.get("displayName"));
        user.setFirstName((String) userData.get("firstName"));
        user.setSurname((String) userData.get("surname"));
        user.setLastUpdated(LocalDateTime.parse((String) userData.get("lastUpdated"), DateTimeFormatter.ISO_DATE_TIME));
        user.setDisabled((Boolean) userData.get("disabled"));

        Map<String, Object> userCredentials = (Map<String, Object>) userData.get("userCredentials");
        if (userCredentials != null) {
            List<Map<String, String>> userRoles = (List<Map<String, String>>) userCredentials.get("userRoles");
            if (userRoles != null) {
                user.setUserRoleIds(new HashSet<>(userRoles.stream().map(role -> role.get("id")).collect(Collectors.toList())));
            }
        }

        List<Map<String, String>> userGroups = (List<Map<String, String>>) userData.get("userGroups");
        if (userGroups != null) {
            user.setUserGroupIds(new HashSet<>(userGroups.stream().map(group -> group.get("id")).collect(Collectors.toList())));
        }

        List<Map<String, String>> orgUnits = (List<Map<String, String>>) userData.get("organisationUnits");
        if (orgUnits != null) {
            user.setOrganisationUnitIds(new HashSet<>(orgUnits.stream().map(ou -> ou.get("id")).collect(Collectors.toList())));
        }
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String auth = dhis2Config.getUsername() + ":" + dhis2Config.getPassword();
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
        headers.set("Authorization", "Basic " + new String(encodedAuth));
        return headers;
    }

    private LocalDateTime getLastSyncTime() {
        return syncInfoRepository.findById(SYNC_INFO_ID)
                .map(SyncInfo::getLastSyncTime)
                .orElse(null);
    }

    private void updateLastSyncTime(LocalDateTime time) {
        SyncInfo syncInfo = syncInfoRepository.findById(SYNC_INFO_ID)
                .orElse(new SyncInfo(SYNC_INFO_ID, time));
        syncInfo.setLastSyncTime(time);
        syncInfoRepository.save(syncInfo);
    }

    private DHIS2User mapUserData(Map<String, Object> userData) {
        DHIS2User user = new DHIS2User();
        user.setId((String) userData.get("id"));
        user.setUsername((String) userData.get("username"));
        user.setDisplayName((String) userData.get("displayName"));
        user.setFirstName((String) userData.get("firstName"));
        user.setSurname((String) userData.get("surname"));
        user.setLastUpdated(LocalDateTime.parse((String) userData.get("lastUpdated"), DateTimeFormatter.ISO_DATE_TIME));
        user.setDisabled((Boolean) userData.get("disabled"));

        Map<String, Object> userCredentials = (Map<String, Object>) userData.get("userCredentials");
        if (userCredentials != null) {
            List<Map<String, String>> userRoles = (List<Map<String, String>>) userCredentials.get("userRoles");
            if (userRoles != null) {
                user.setUserRoleIds(new HashSet<>(userRoles.stream().map(role -> role.get("id")).collect(Collectors.toList())));
            }
        }

        List<Map<String, String>> userGroups = (List<Map<String, String>>) userData.get("userGroups");
        if (userGroups != null) {
            user.setUserGroupIds(new HashSet<>(userGroups.stream().map(group -> group.get("id")).collect(Collectors.toList())));
        }

        List<Map<String, String>> orgUnits = (List<Map<String, String>>) userData.get("organisationUnits");
        if (orgUnits != null) {
            user.setOrganisationUnitIds(new HashSet<>(orgUnits.stream().map(ou -> ou.get("id")).collect(Collectors.toList())));
        }

        return user;
    }
}
