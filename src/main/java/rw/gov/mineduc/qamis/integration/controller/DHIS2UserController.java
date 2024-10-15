package rw.gov.mineduc.qamis.integration.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.gov.mineduc.qamis.integration.model.DHIS2User;
import rw.gov.mineduc.qamis.integration.service.DHIS2UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;

@RestController
@RequestMapping("/api/dhis2users")
public class DHIS2UserController {

    @Autowired
    private DHIS2UserService dhis2UserService;

    /**
     * Search for DHIS2 users by name.
     *
     * This endpoint allows searching for DHIS2 users by their display name, first name, or surname.
     *
     * @param name Name to search for (matches display name, first name, or surname)
     * @param pageable Pagination information (page, size, sort)
     * @return A page of DHIS2User objects matching the search criteria
     *
     * Example usage:
     * GET /api/dhis2users/search/name?name=John&page=0&size=20&sort=username,asc
     */
    @GetMapping("/search/name")
    public ResponseEntity<Page<DHIS2User>> searchUsersByName(
            @RequestParam String name,
            Pageable pageable) {
        String[] nameParts = name.split("\\s+");
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? String.join(" ", Arrays.copyOfRange(nameParts, 1, nameParts.length)) : null;
        return ResponseEntity.ok(dhis2UserService.searchUsers(
                null, null, null, name, name, firstName, lastName, 
                null, null, null, pageable
        ));
    }

    /**
     * Search for DHIS2 users based on various criteria.
     *
     * This endpoint allows searching for DHIS2 users with flexible filtering options.
     * All parameters are optional and can be combined for more specific searches.
     *
     * @param userRoleIds List of user role IDs to filter by
     * @param userGroupIds List of user group IDs to filter by
     * @param organisationUnitIds List of organisation unit IDs to filter by
     * @param username Username to search for (partial match)
     * @param displayName Display name to search for (partial match)
     * @param firstName First name to search for (partial match)
     * @param surname Surname to search for (partial match)
     * @param disabled Filter by user disabled status
     * @param lastUpdatedStart Start date for last updated filter
     * @param lastUpdatedEnd End date for last updated filter
     * @param pageable Pagination information (page, size, sort)
     * @return A page of DHIS2User objects matching the search criteria
     *
     * Example usage:
     * GET /api/dhis2users/search?username=john&userGroupIds=group1,group2&page=0&size=20&sort=username,asc
     */
    @GetMapping("/search")
    public ResponseEntity<Page<DHIS2User>> searchUsers(
            @RequestParam(required = false) List<String> userRoleIds,
            @RequestParam(required = false) List<String> userGroupIds,
            @RequestParam(required = false) List<String> organisationUnitIds,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String displayName,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String surname,
            @RequestParam(required = false) Boolean disabled,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastUpdatedStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastUpdatedEnd,
            Pageable pageable) {

        return ResponseEntity.ok(dhis2UserService.searchUsers(
                userRoleIds, userGroupIds, organisationUnitIds, username, displayName,
                firstName, surname, disabled, lastUpdatedStart, lastUpdatedEnd, pageable
        ));
    }

    @PostMapping
    public ResponseEntity<DHIS2User> createUser(@RequestBody DHIS2User user) {
        return ResponseEntity.ok(dhis2UserService.createUser(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DHIS2User> updateUser(@PathVariable String id, @RequestBody DHIS2User user) {
        return ResponseEntity.ok(dhis2UserService.updateUser(id, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        dhis2UserService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sync")
    public ResponseEntity<String> syncUsers(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false, defaultValue = "false") boolean syncAll) {
        int syncedCount = dhis2UserService.synchronizeUsers(fromDate, userId, syncAll);
        return ResponseEntity.ok("Synchronized " + syncedCount + " users");
    }

    /**
     * Manually trigger a full synchronization of DHIS2 users.
     *
     * @return ResponseEntity with a message indicating the number of users synchronized
     */
    @PostMapping("/sync/full")
    public ResponseEntity<String> fullSync() {
        int syncedCount = dhis2UserService.synchronizeUsers(null, null, true);
        return ResponseEntity.ok("Performed full synchronization. Synchronized " + syncedCount + " users");
    }

    /**
     * Manually trigger synchronization for a specific DHIS2 user.
     *
     * @param userId The ID of the DHIS2 user to synchronize
     * @return ResponseEntity with a message indicating the result of the synchronization
     */
    @PostMapping("/sync/user/{userId}")
    public ResponseEntity<String> syncSingleUser(@PathVariable String userId) {
        try {
            int syncedCount = dhis2UserService.synchronizeUsers(null, userId, false);
            if (syncedCount > 0) {
                return ResponseEntity.ok("Successfully synchronized user with ID: " + userId);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error synchronizing user: " + e.getMessage());
        }
    }

    /**
     * Trigger synchronization to fetch all users from DHIS2.
     *
     * @return ResponseEntity with a message indicating the result of the synchronization
     */
    @PostMapping("/sync/all")
    public ResponseEntity<String> syncAllUsers() {
        try {
            int syncedCount = dhis2UserService.synchronizeUsers(null, null, true);
            return ResponseEntity.ok("Successfully synchronized " + syncedCount + " users from DHIS2");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error synchronizing users: " + e.getMessage());
        }
    }
}
