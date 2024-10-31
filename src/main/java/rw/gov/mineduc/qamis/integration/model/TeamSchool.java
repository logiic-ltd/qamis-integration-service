package rw.gov.mineduc.qamis.integration.model;

import javax.persistence.*;

@Entity
@Table(name = "team_schools")
public class TeamSchool {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String schoolCode;
    
    @Column(nullable = false)
    private String schoolName;
    
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

    public String getSchoolCode() {
        return schoolCode;
    }

    public void setSchoolCode(String schoolCode) {
        this.schoolCode = schoolCode;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public InspectionTeam getTeam() {
        return team;
    }

    public void setTeam(InspectionTeam team) {
        this.team = team;
    }
}
