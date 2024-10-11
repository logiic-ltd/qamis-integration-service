package rw.gov.mineduc.qamis.integration.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rw.gov.mineduc.qamis.integration.model.DHIS2User;
import rw.gov.mineduc.qamis.integration.repository.DHIS2UserRepository;

import java.util.List;

@Service
public class DHIS2UserService {

    @Autowired
    private DHIS2UserRepository dhis2UserRepository;

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
}
