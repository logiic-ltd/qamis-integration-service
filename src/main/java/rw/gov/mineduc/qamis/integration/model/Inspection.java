package rw.gov.mineduc.qamis.integration.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "inspections")
public class Inspection {
    @Id
    private String name;
    
    @Column(nullable = false)
    private String inspectionName;
    
    @Column(nullable = false)
    private LocalDate startDate;
    
    @Column(nullable = false)
    private LocalDate endDate;
    
    @Column(columnDefinition = "TEXT")
    private String introduction;
    
    @Column(columnDefinition = "TEXT")
    private String objectives;
    
    @Column(columnDefinition = "TEXT")
    private String methodology;
    
    @Column(columnDefinition = "TEXT")
    private String executiveSummary;
    
    @Column(nullable = false)
    private String workflowState;
    
    @Column(nullable = false)
    private LocalDateTime lastModified;
    
    @OneToMany(mappedBy = "inspection", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<InspectionTeam> teams = new HashSet<>();
    
    @OneToMany(mappedBy = "inspection", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<InspectionChecklist> checklists = new HashSet<>();

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInspectionName() {
        return inspectionName;
    }

    public void setInspectionName(String inspectionName) {
        this.inspectionName = inspectionName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getObjectives() {
        return objectives;
    }

    public void setObjectives(String objectives) {
        this.objectives = objectives;
    }

    public String getMethodology() {
        return methodology;
    }

    public void setMethodology(String methodology) {
        this.methodology = methodology;
    }

    public String getExecutiveSummary() {
        return executiveSummary;
    }

    public void setExecutiveSummary(String executiveSummary) {
        this.executiveSummary = executiveSummary;
    }

    public String getWorkflowState() {
        return workflowState;
    }

    public void setWorkflowState(String workflowState) {
        this.workflowState = workflowState;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public Set<InspectionTeam> getTeams() {
        return teams;
    }

    public void setTeams(Set<InspectionTeam> teams) {
        this.teams = teams;
    }

    public Set<InspectionChecklist> getChecklists() {
        return checklists;
    }

    public void setChecklists(Set<InspectionChecklist> checklists) {
        this.checklists = checklists;
    }
}
