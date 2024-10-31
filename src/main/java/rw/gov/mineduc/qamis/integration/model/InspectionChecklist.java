package rw.gov.mineduc.qamis.integration.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inspection_checklists")
public class InspectionChecklist {
    @Id
    private String name;
    
    @Column(nullable = false)
    private String id;
    
    @Column(nullable = false)
    private String shortName;
    
    @Column(nullable = false)
    private String periodType;
    
    @Column(nullable = false)
    private LocalDateTime lastUpdated;
    
    @ManyToOne
    @JoinColumn(name = "inspection_id", nullable = false)
    private Inspection inspection;

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Inspection getInspection() {
        return inspection;
    }

    public void setInspection(Inspection inspection) {
        this.inspection = inspection;
    }
}
