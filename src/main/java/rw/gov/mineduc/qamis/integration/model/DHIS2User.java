package rw.gov.mineduc.qamis.integration.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "dhis2_users")
public class DHIS2User {

    @Id
    private String id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String displayName;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String surname;

    @ElementCollection
    @CollectionTable(name = "dhis2_user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role_id")
    private Set<String> userRoleIds;

    @ElementCollection
    @CollectionTable(name = "dhis2_user_groups", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "group_id")
    private Set<String> userGroupIds;

    @ElementCollection
    @CollectionTable(name = "dhis2_user_org_units", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "org_unit_id")
    private Set<String> organisationUnitIds;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    @Column(nullable = false)
    private boolean disabled;

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Set<String> getUserRoleIds() {
        return userRoleIds;
    }

    public void setUserRoleIds(Set<String> userRoleIds) {
        this.userRoleIds = userRoleIds;
    }

    public Set<String> getUserGroupIds() {
        return userGroupIds;
    }

    public void setUserGroupIds(Set<String> userGroupIds) {
        this.userGroupIds = userGroupIds;
    }

    public Set<String> getOrganisationUnitIds() {
        return organisationUnitIds;
    }

    public void setOrganisationUnitIds(Set<String> organisationUnitIds) {
        this.organisationUnitIds = organisationUnitIds;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}
