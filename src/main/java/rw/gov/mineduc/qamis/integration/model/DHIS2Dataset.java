package rw.gov.mineduc.qamis.integration.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "dhis2_datasets")
public class DHIS2Dataset {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String shortName;

    @Column(nullable = true)
    private String periodType;

    @ElementCollection
    @CollectionTable(name = "dhis2_dataset_org_units", joinColumns = @JoinColumn(name = "dataset_id"))
    @Column(name = "org_unit_id")
    private Set<String> organisationUnitIds;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getPeriodType() {
        return periodType;
    }

    public void setPeriodType(String periodType) {
        this.periodType = periodType;
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
}
