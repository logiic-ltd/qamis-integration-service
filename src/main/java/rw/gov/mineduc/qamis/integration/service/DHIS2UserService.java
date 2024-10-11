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
import rw.gov.mineduc.qamis.integration.repository.DHIS2UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class DHIS2UserService {

    @Autowired
    private DHIS2UserRepository dhis2UserRepository;

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

    @Scheduled(fixedRateString = "${dhis2.syncIntervalMinutes}", timeUnit = java.util.concurrent.TimeUnit.MINUTES)
    public void synchronizeUsers() {
        LocalDateTime lastSyncTime = getLastSyncTime();
        String url = dhis2Config.getApiUrl() + "/api/users.json?fields=*&paging=false";
        
        if (lastSyncTime != null) {
            url += "&filter=lastUpdated:gte:" + lastSyncTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString((dhis2Config.getUsername() + ":" + dhis2Config.getPassword()).getBytes()));

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        List<Map<String, Object>> users = (List<Map<String, Object>>) response.getBody().get("users");

        for (Map<String, Object> userData : users) {
            DHIS2User user = mapUserData(userData);
            dhis2UserRepository.save(user);
        }

        updateLastSyncTime(LocalDateTime.now());
    }

    private LocalDateTime getLastSyncTime() {
        // Implement logic to get the last sync time from a persistent store
        // For simplicity, you can store it in a file or database
        return null; // Return null for initial sync
    }

    private void updateLastSyncTime(LocalDateTime time) {
        // Implement logic to update the last sync time in a persistent store
    }

    private DHIS2User mapUserData(Map<String, Object> userData) {
        DHIS2User user = new DHIS2User();
        // Map the userData to DHIS2User object
        // You'll need to implement this mapping based on the DHIS2 API response structure
        return user;
    }
}
