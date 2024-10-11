package rw.gov.mineduc.qamis.integration.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.gov.mineduc.qamis.integration.model.DHIS2User;
import rw.gov.mineduc.qamis.integration.service.DHIS2UserService;

import java.util.List;

@RestController
@RequestMapping("/api/dhis2users")
public class DHIS2UserController {

    @Autowired
    private DHIS2UserService dhis2UserService;

    @GetMapping("/search")
    public ResponseEntity<List<DHIS2User>> searchUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String roleId,
            @RequestParam(required = false) String groupId,
            @RequestParam(required = false) String orgUnitId) {

        List<DHIS2User> users;

        if (name != null) {
            users = dhis2UserService.searchUsersByName(name);
        } else if (username != null) {
            users = dhis2UserService.searchUsersByUsername(username);
        } else if (roleId != null) {
            users = dhis2UserService.searchUsersByRole(roleId);
        } else if (groupId != null) {
            users = dhis2UserService.searchUsersByGroup(groupId);
        } else if (orgUnitId != null) {
            users = dhis2UserService.searchUsersByOrganisationUnit(orgUnitId);
        } else {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(users);
    }
}
