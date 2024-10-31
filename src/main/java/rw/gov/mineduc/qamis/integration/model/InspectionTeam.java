package rw.gov.mineduc.qamis.integration.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "inspection_teams")
public class InspectionTeam {
    @Id
    private String name;
    
    @Column(nullable = false)
    private String teamName;
    
    @ManyToOne
    @JoinColumn(name = "inspection_id", nullable = false)
    private Inspection inspection;
    
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TeamMember> members = new HashSet<>();
    
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TeamSchool> schools = new HashSet<>();

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public Inspection getInspection() {
        return inspection;
    }

    public void setInspection(Inspection inspection) {
        this.inspection = inspection;
    }

    public Set<TeamMember> getMembers() {
        return members;
    }

    public void setMembers(Set<TeamMember> members) {
        this.members = members;
    }

    public Set<TeamSchool> getSchools() {
        return schools;
    }

    public void setSchools(Set<TeamSchool> schools) {
        this.schools = schools;
    }
}
