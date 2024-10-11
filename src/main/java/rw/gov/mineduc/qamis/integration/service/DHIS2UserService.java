package rw.gov.mineduc.qamis.integration.service;

import org.springframework.beans.factory.annotation.Autowired;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DHIS2UserService {

    private static final String SYNC_INFO_ID = "DHIS2_SYNC";

    @Autowired
    private DHIS2UserRepository dhis2UserRepository;

    @Autowired
    private SyncInfoRepository syncInfoRepository;

    @Autowired
    private DHIS2Config dhis2Config;

    @Autowired
    private RestTemplate restTemplate;

    public DHIS2User saveUser(DHIS2User user) {
        return dhis2UserRepository.save(user);
    }

    public List<DHIS2User> searchUsersByName(String name) {
        return dhis2UserRepository.findByDisplayNameContainingIgnoreCase(name);
    }

    public List<DHIS2User> searchUsersByUsername(String username) {
        return dhis2UserRepository.findByUsernameContainingIgnoreCase(username);
    }

    public List<DHIS2User> searchUsersByRole(String roleId) {
        return dhis2UserRepository.findByUserRoleId(roleId);
    }

    public List<DHIS2User> searchUsersByGroup(String groupId) {
        return dhis2UserRepository.findByUserGroupId(groupId);
    }

    public List<DHIS2User> searchUsersByOrganisationUnit(String orgUnitId) {
        return dhis2UserRepository.findByOrganisationUnitId(orgUnitId);
    }

    @Scheduled(cron = "${dhis2.syncCron}")
    public void synchronizeUsers() {
        LocalDateTime lastSyncTime = getLastSyncTime();
        String url = dhis2Config.getApiUrl() + "/api/users.json?fields=id,username,displayName,firstName,surname,userCredentials[userRoles[id]],userGroups[id],organisationUnits[id],lastUpdated,disabled&paging=false";
        
        if (lastSyncTime != null) {
            url += "&filter=lastUpdated:gte:" + lastSyncTime.format(DateTimeFormatter.ISO_DATE_TIME);
        }

        HttpHeaders headers = createAuthHeaders();
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        List<Map<String, Object>> users = (List<Map<String, Object>>) response.getBody().get("users");

        for (Map<String, Object> userData : users) {
            DHIS2User user = mapUserData(userData);
            dhis2UserRepository.save(user);
        }

        updateLastSyncTime(LocalDateTime.now());
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
                user.setUserRoleIds(new HashSet<>(userRoles.stream().map(role -> role.get("id")).toList()));
            }
        }

        List<Map<String, String>> userGroups = (List<Map<String, String>>) userData.get("userGroups");
        if (userGroups != null) {
            user.setUserGroupIds(new HashSet<>(userGroups.stream().map(group -> group.get("id")).toList()));
        }

        List<Map<String, String>> orgUnits = (List<Map<String, String>>) userData.get("organisationUnits");
        if (orgUnits != null) {
            user.setOrganisationUnitIds(new HashSet<>(orgUnits.stream().map(ou -> ou.get("id")).toList()));
        }

        return user;
    }
}
