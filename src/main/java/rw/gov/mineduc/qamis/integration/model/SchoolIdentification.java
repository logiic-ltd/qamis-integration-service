package rw.gov.mineduc.qamis.integration.model;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "school_identification",
        indexes = {@Index(name = "unique_non_null_code", columnList = "schoolCode", unique = true)})
public class SchoolIdentification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Basic School Information
    @Column(nullable = false)
    private String schoolName;

    @Column(unique = true, nullable = true) // Nullable to accommodate schools without codes
    private Integer schoolCode;

    @Column(nullable = false)
    private String schoolStatus;

    @Column(nullable = false)
    private String schoolOwner;

    @Column(length = 20, nullable = true)
    private String schoolOwnerContact;

    @Column(nullable = false)
    private String accommodationStatus;

    @Column(name = "year_of_establishment", nullable = false)
    private Integer yearOfEstablishment;

    @Column(nullable = false)
    private String province;

    @Column(nullable = false)
    private String district;

    @Column(nullable = false)
    private String sector;

    @Column(nullable = true)
    private String cell;

    @Column(nullable = true)
    private String village;

    private String schoolEmail;

    // Headteacher Information
    @Column(name = "headteacher_name", nullable = false)
    private String headteacherName;

    @Column(name = "headteacher_qualification", length = 255, nullable = true)
    private String headteacherQualification;

    @Column(name = "headteacher_telephone", length = 20, nullable = true)
    private String headteacherTelephone;

    // Student Information
    @Column(name = "total_students", columnDefinition = "INTEGER DEFAULT 0", nullable = false)
    private Integer totalStudents;

    @Column(name = "number_of_girls", columnDefinition = "INTEGER DEFAULT 0", nullable = false)
    private Integer numberOfGirls;

    @Column(name = "number_of_boys", columnDefinition = "INTEGER DEFAULT 0", nullable = false)
    private Integer numberOfBoys;

    @Column(name = "students_with_sen", columnDefinition = "INTEGER DEFAULT 0", nullable = true)
    private Integer studentsWithSen;

    // Teacher Information
    @Column(name = "number_of_male_teachers", columnDefinition = "INTEGER DEFAULT 0", nullable = false)
    private Integer numberOfMaleTeachers;

    @Column(name = "number_of_female_teachers", columnDefinition = "INTEGER DEFAULT 0", nullable = false)
    private Integer numberOfFemaleTeachers;

    @Column(name = "total_teachers", columnDefinition = "INTEGER DEFAULT 0", nullable = false)
    private Integer totalTeachers;

    @Column(name = "number_of_male_assistant_teachers", columnDefinition = "INTEGER DEFAULT 0", nullable = true)
    private Integer numberOfMaleAssistantTeachers;

    @Column(name = "number_of_female_assistant_teachers", columnDefinition = "INTEGER DEFAULT 0", nullable = true)
    private Integer numberOfFemaleAssistantTeachers;

    @Column(name = "total_assistant_teachers", columnDefinition = "INTEGER DEFAULT 0", nullable = true)
    private Integer totalAssistantTeachers;

    // Administrative Staff
    @Column(name = "total_administrative_staff", columnDefinition = "INTEGER DEFAULT 0", nullable = true)
    private Integer totalAdministrativeStaff;

    @Column(name = "deputy_headteacher", nullable = true)
    private Integer deputyHeadteacher;

    @Column(nullable = true)
    private Integer secretary;

    @Column(nullable = true)
    private Integer librarian;

    @Column(nullable = true)
    private Integer accountant;

    // Supporting Staff
    @Column(name = "total_supporting_staff", columnDefinition = "INTEGER DEFAULT 0", nullable = true)
    private Integer totalSupportingStaff;

    @Column(columnDefinition = "INTEGER DEFAULT 0", nullable = true)
    private Integer cleaners;

    @Column(columnDefinition = "INTEGER DEFAULT 0", nullable = true)
    private Integer watchmen;

    @Column(name = "school_cooks", columnDefinition = "INTEGER DEFAULT 0", nullable = true)
    private Integer schoolCooks;

    @Column(name = "store_keeper", columnDefinition = "INTEGER DEFAULT 0", nullable = true)
    private Integer storeKeeper;

    @Column(columnDefinition = "INTEGER DEFAULT 0", nullable = true)
    private Integer drivers;

    // Infrastructure
    @Column(name = "nbr_of_classrooms", columnDefinition = "INTEGER DEFAULT 0", nullable = false)
    private Integer nbrOfClassrooms;

    @Column(name = "nbr_of_latrines", columnDefinition = "INTEGER DEFAULT 0", nullable = false)
    private Integer nbrOfLatrines;

    @Column(name = "number_of_kitchen", columnDefinition = "INTEGER DEFAULT 0", nullable = false)
    private Integer numberOfKitchen;

    @Column(name = "number_of_dining_hall", columnDefinition = "INTEGER DEFAULT 0", nullable = true)
    private Integer numberOfDiningHall;

    @Column(name = "number_of_library", columnDefinition = "INTEGER DEFAULT 0", nullable = false)
    private Integer numberOfLibrary;

    @Column(name = "number_of_smart_classrooms", columnDefinition = "INTEGER DEFAULT 0", nullable = false)
    private Integer numberOfSmartClassrooms;

    @Column(name = "number_of_computer_lab", columnDefinition = "INTEGER DEFAULT 0", nullable = false)
    private Integer numberOfComputerLab;

    @Column(name = "number_of_admin_offices", columnDefinition = "INTEGER DEFAULT 0", nullable = true)
    private Integer numberOfAdminOffices;

    @Column(name = "number_of_multipurpose_halls", columnDefinition = "INTEGER DEFAULT 0", nullable = true)
    private Integer numberOfMultipurposeHalls;

    @Column(name = "number_of_academic_staff_rooms", columnDefinition = "INTEGER DEFAULT 0", nullable = true)
    private Integer numberOfAcademicStaffRooms;

    // Metadata
    @Column(name = "latitude", nullable = true)
    private Double latitude;

    @Column(name = "longitude", nullable = true)
    private Double longitude;

    @Column(name = "last_modified", nullable = false)
    private LocalDateTime lastModified;

}
