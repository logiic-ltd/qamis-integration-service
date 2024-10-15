package rw.gov.mineduc.qamis.integration.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import rw.gov.mineduc.qamis.integration.model.DHIS2User;
import rw.gov.mineduc.qamis.integration.service.DHIS2UserService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class DHIS2UserControllerTest {

    @Mock
    private DHIS2UserService dhis2UserService;

    @InjectMocks
    private DHIS2UserController dhis2UserController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSearchUsers() {
        DHIS2User user1 = new DHIS2User();
        user1.setId("1");
        user1.setUsername("user1");
        user1.setDisplayName("User One");

        DHIS2User user2 = new DHIS2User();
        user2.setId("2");
        user2.setUsername("user2");
        user2.setDisplayName("User Two");

        List<DHIS2User> users = Arrays.asList(user1, user2);
        Page<DHIS2User> page = new PageImpl<>(users);

        when(dhis2UserService.searchUsers(
                anyList(), anyList(), anyList(), anyString(), anyString(),
                anyString(), anyString(), anyBoolean(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        ResponseEntity<Page<DHIS2User>> response = dhis2UserController.searchUsers(
                Collections.singletonList("role1"),
                Collections.singletonList("group1"),
                Collections.singletonList("ou1"),
                "user",
                "User",
                "First",
                "Last",
                false,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(),
                Pageable.unpaged()
        );

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().getTotalElements());
    }

    @Test
    void testSearchUsersByUsername() {
        DHIS2User user = new DHIS2User();
        user.setId("1");
        user.setUsername("testuser");
        user.setDisplayName("Test User");

        Page<DHIS2User> page = new PageImpl<>(Collections.singletonList(user));

        when(dhis2UserService.searchUsers(
                eq(null), eq(null), eq(null), eq("testuser"), eq(null),
                eq(null), eq(null), eq(null), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(page);

        ResponseEntity<Page<DHIS2User>> response = dhis2UserController.searchUsers(
                null, null, null, "testuser", null, null, null, null, null, null, Pageable.unpaged()
        );

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals("testuser", response.getBody().getContent().get(0).getUsername());
    }

    @Test
    void testSearchUsersByUserGroup() {
        DHIS2User user = new DHIS2User();
        user.setId("1");
        user.setUsername("groupuser");
        user.setDisplayName("Group User");

        Page<DHIS2User> page = new PageImpl<>(Collections.singletonList(user));

        when(dhis2UserService.searchUsers(
                eq(null), eq(Collections.singletonList("group1")), eq(null), eq(null), eq(null),
                eq(null), eq(null), eq(null), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(page);

        ResponseEntity<Page<DHIS2User>> response = dhis2UserController.searchUsers(
                null, Collections.singletonList("group1"), null, null, null, null, null, null, null, null, Pageable.unpaged()
        );

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals("groupuser", response.getBody().getContent().get(0).getUsername());
    }

    @Test
    void testSearchUsersByName() {
        DHIS2User user = new DHIS2User();
        user.setId("1");
        user.setUsername("johnsmith");
        user.setDisplayName("John Smith");
        user.setFirstName("John");
        user.setSurname("Smith");

        Page<DHIS2User> page = new PageImpl<>(Collections.singletonList(user));

        when(dhis2UserService.searchUsers(
                eq(null), eq(null), eq(null), eq("John Smith"), eq("John Smith"),
                eq("John"), eq("Smith"), eq(null), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(page);

        ResponseEntity<Page<DHIS2User>> response = dhis2UserController.searchUsersByName("John Smith", Pageable.unpaged());

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals("johnsmith", response.getBody().getContent().get(0).getUsername());
        assertEquals("John Smith", response.getBody().getContent().get(0).getDisplayName());
    }

    @Test
    void testSyncUsers() {
        when(dhis2UserService.synchronizeUsers(any(), any(), anyBoolean())).thenReturn(5);

        ResponseEntity<String> response = dhis2UserController.syncUsers(null, null, false);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Synchronized 5 users", response.getBody());
    }

    @Test
    void testSyncAllUsers() {
        when(dhis2UserService.synchronizeUsers(null, null, true)).thenReturn(10);

        ResponseEntity<String> response = dhis2UserController.syncAllUsers();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Successfully synchronized 10 users from DHIS2", response.getBody());
    }
}
