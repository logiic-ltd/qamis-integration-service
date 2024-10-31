package rw.gov.mineduc.qamis.integration.model;

import javax.persistence.*;

@Entity
@Table(name = "team_members")
public class TeamMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String role;
    
    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private InspectionTeam team;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public InspectionTeam getTeam() {
        return team;
    }

    public void setTeam(InspectionTeam team) {
        this.team = team;
    }
}
