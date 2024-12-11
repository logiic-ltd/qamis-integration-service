package rw.gov.mineduc.qamis.integration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolIdentificationDTO {

    @JsonProperty("school_name")
    private String schoolName;

    @JsonProperty("school_code")
    private Integer schoolCode;

    @JsonProperty("status")
    private String schoolStatus;

    @JsonProperty("school_owner")
    private String schoolOwner;

    @JsonProperty("contact")
    private String schoolOwnerContact;

    @JsonProperty("accommodation_status")
    private String accommodationStatus;

    @JsonProperty("year_of_establishment")
    private Integer yearOfEstablishment;

    @JsonProperty("village")
    private String village;

    @JsonProperty("cell")
    private String cell;

    @JsonProperty("sector")
    private String sector;

    @JsonProperty("district")
    private String district;

    @JsonProperty("province")
    private String province;

    @JsonProperty("school_email")
    private String schoolEmail;

    @JsonProperty("latitudes")
    private Double latitudes;

    @JsonProperty("longitude")
    private Double longitude;

    @JsonProperty("ht_name")
    private String headteacherName;

    @JsonProperty("qualification_of_headteacher")
    private String headteacherQualification;

    @JsonProperty("telephone")
    private String headteacherTelephone;

    @JsonProperty("number_of_boys")
    private Integer numberOfBoys;

    @JsonProperty("number_of_girls")
    private Integer numberOfGirls;

    @JsonProperty("total_nr_students")
    private Integer totalStudents;

    @JsonProperty("students_with_sen")
    private Integer studentsWithSen;

    @JsonProperty("number_of_male_teachers")
    private Integer numberOfMaleTeachers;

    @JsonProperty("number_of_female_teachers")
    private Integer numberOfFemaleTeachers;

    @JsonProperty("number_of_teachers")
    private Integer totalTeachers;

    @JsonProperty("number_of_male_assistant_teachers")
    private Integer numberOfMaleAssistantTeachers;

    @JsonProperty("number_of_female_assistant_teachers")
    private Integer numberOfFemaleAssistantTeachers;

    @JsonProperty("number_of_assistant_teachers")
    private Integer totalAssistantTeachers;

    @JsonProperty("total_number_of_administrative_staff")
    private Integer totalAdministrativeStaff;

    @JsonProperty("headteacher")
    private Integer headteacher;

    @JsonProperty("deputy_headteacher")
    private Integer deputyHeadteacher;

    @JsonProperty("secretary")
    private Integer secretary;

    @JsonProperty("librarian")
    private Integer librarian;

    @JsonProperty("accountant")
    private Integer accountant;

    @JsonProperty("other_staff")
    private Integer otherAdministrativeStaff;

    @JsonProperty("total_number_of_supporting_staff")
    private Integer totalSupportingStaff;

    @JsonProperty("cleaners")
    private Integer cleaners;

    @JsonProperty("watchmen")
    private Integer watchmen;

    @JsonProperty("school_cooks")
    private Integer schoolCooks;

    @JsonProperty("storekeeper")
    private Integer storekeeper;

    @JsonProperty("drivers")
    private Integer drivers;

    @JsonProperty("other_supporting_staff")
    private Integer otherSupportingStaff;

    @JsonProperty("nbr_of_classrooms")
    private Integer numberOfClassrooms;

    @JsonProperty("nbr_of_latrines")
    private Integer numberOfLatrines;

    @JsonProperty("number_of_kitchen")
    private Integer numberOfKitchens;

    @JsonProperty("number_of_dining_hall")
    private Integer numberOfDiningHalls;

    @JsonProperty("number_of_library")
    private Integer numberOfLibraries;

    @JsonProperty("number_of_smart_classrooms")
    private Integer numberOfSmartClassrooms;

    @JsonProperty("number_of_computer_lab")
    private Integer numberOfComputerLabs;

    @JsonProperty("number_of_admin_offices")
    private Integer numberOfAdministrativeOffices;

    @JsonProperty("number_of_multipurpose_halls")
    private Integer numberOfMultipurposeHalls;

    @JsonProperty("number_of_academic_staff_rooms")
    private Integer numberOfAcademicStaffRooms;
    @JsonProperty("last_modified")
    private LocalDateTime lastModified;


}
