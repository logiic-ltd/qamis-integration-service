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

@RestController
@RequestMapping("/api/dhis2users")
public class DHIS2UserController {

    @Autowired
    private DHIS2UserService dhis2UserService;

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
    public ResponseEntity<Void> syncUsers() {
        dhis2UserService.synchronizeUsers();
        return ResponseEntity.noContent().build();
    }
}
