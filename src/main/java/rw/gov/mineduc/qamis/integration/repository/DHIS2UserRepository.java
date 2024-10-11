package rw.gov.mineduc.qamis.integration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.gov.mineduc.qamis.integration.model.DHIS2User;

import java.util.List;
import java.util.Optional;

public interface DHIS2UserRepository extends JpaRepository<DHIS2User, String> {

    List<DHIS2User> findByDisplayNameContainingIgnoreCase(String name);

    List<DHIS2User> findByUsernameContainingIgnoreCase(String username);

    @Query("SELECT u FROM DHIS2User u JOIN u.userRoleIds r WHERE r = :roleId")
    List<DHIS2User> findByUserRoleId(@Param("roleId") String roleId);

    @Query("SELECT u FROM DHIS2User u JOIN u.userGroupIds g WHERE g = :groupId")
    List<DHIS2User> findByUserGroupId(@Param("groupId") String groupId);

    @Query("SELECT u FROM DHIS2User u JOIN u.organisationUnitIds o WHERE o = :orgUnitId")
    List<DHIS2User> findByOrganisationUnitId(@Param("orgUnitId") String orgUnitId);

    Optional<DHIS2User> findTopByOrderByLastUpdatedDesc();
}
