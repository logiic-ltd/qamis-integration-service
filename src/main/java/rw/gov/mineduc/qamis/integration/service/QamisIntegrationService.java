package rw.gov.mineduc.qamis.integration.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.client.RestTemplate;
import rw.gov.mineduc.qamis.integration.dto.SchoolIdentificationDTO;
import rw.gov.mineduc.qamis.integration.exception.QamisApiException;
import rw.gov.mineduc.qamis.integration.config.QamisConfig;
import rw.gov.mineduc.qamis.integration.dto.InspectionDTO;


import java.util.stream.Collectors;

@Service
public class QamisIntegrationService {
    private static final Logger log = LoggerFactory.getLogger(QamisIntegrationService.class);

    @Autowired
    private QamisConfig qamisConfig;

    @Autowired
    private RestTemplate restTemplate;

    private static final DateTimeFormatter CUSTOM_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

    @Transactional(readOnly = true)
    public List<InspectionDTO> fetchApprovedInspections() {
        try {
            String url = qamisConfig.getApiUrl() + "/api/resource/Inspection?filters=[[\"workflow_state\",\"=\",\"Approved by DG\"]]&fields=[\"name\",\"inspection_name\",\"workflow_state\",\"modified\"]";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "token " + qamisConfig.getApiToken());
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
            );
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new QamisApiException("QAMIS API returned error status: " + response.getStatusCode(), 
                    response.getStatusCode().value());
            }
            
            if (response.getBody() == null) {
                throw new QamisApiException("QAMIS API returned null response body", 500);
            }
            
            if (!response.getBody().containsKey("data")) {
                log.warn("No data field in QAMIS API response");
                return Collections.emptyList();
            }

            List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");
            if (data == null) {
                log.warn("Data field is null in QAMIS API response");
                return Collections.emptyList();
            }

            return data.stream()
                      .filter(item -> item != null && item.containsKey("name"))
                      .map(item -> {
                          InspectionDTO dto = new InspectionDTO();
                          dto.setId((String) item.get("name"));
                          dto.setName((String) item.get("name"));
                          return dto;
                      })
                      .collect(Collectors.toList());

        } catch (QamisApiException e) {
            log.error("QAMIS API error: {} (Status: {})", e.getMessage(), e.getStatusCode());
            throw e;
        } catch (Exception e) {
            log.error("Error fetching approved inspections from QAMIS: {}", e.getMessage(), e);
            throw new QamisApiException("Failed to fetch approved inspections", e);
        }
    }

    @Transactional(readOnly = true)
    public InspectionDTO fetchInspectionDetails(String inspectionId) throws QamisApiException {
        if (inspectionId == null || inspectionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Inspection ID cannot be null or empty");
        }

        try {
            // Fetch basic inspection details
            String url = qamisConfig.getApiUrl() + "/api/resource/Inspection/" + inspectionId;
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "token " + qamisConfig.getApiToken());
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
            );
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new QamisApiException("QAMIS API returned error status: " + response.getStatusCode(), 
                    response.getStatusCode().value());
            }
            
            if (response.getBody() == null) {
                throw new QamisApiException("QAMIS API returned null response body for inspection: " + inspectionId, 500);
            }
            
            if (!response.getBody().containsKey("data")) {
                throw new QamisApiException("No data field in QAMIS API response for inspection: " + inspectionId, 500);
            }

            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            if (data == null) {
                throw new QamisApiException("Data field is null in QAMIS API response for inspection: " + inspectionId, 500);
            }

            InspectionDTO dto = new InspectionDTO();
            dto.setId((String) data.get("name"));
            dto.setName((String) data.get("name"));
            dto.setInspectionName((String) data.get("inspection_name"));
            dto.setWorkflowState((String) data.get("workflow_state"));
            dto.setCustomFields(data);

            validateInspectionDTO(dto, inspectionId);
            // Fetch teams for this inspection
            dto.setTeams(fetchInspectionTeams(inspectionId));
            
            return dto;

        } catch (QamisApiException e) {
            log.error("QAMIS API error for inspection {}: {} (Status: {})", 
                     inspectionId, e.getMessage(), e.getStatusCode());
            throw e;
        } catch (Exception e) {
            log.error("Error fetching inspection details from QAMIS for {}: {}", inspectionId, e.getMessage(), e);
            throw new QamisApiException("Failed to fetch inspection details: " + inspectionId, e);
        }
    }

    public List<Map<String, Object>> fetchInspectionTeams(String inspectionId) {
        try {
            // First fetch the inspection details to get team links
            String url = qamisConfig.getApiUrl() + "/api/resource/Inspection/" + inspectionId;
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "token " + qamisConfig.getApiToken());
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
            );
            
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null || !response.getBody().containsKey("data")) {
                log.warn("No inspection data found for ID {}", inspectionId);
                return Collections.emptyList();
            }

            Map<String, Object> inspectionData = (Map<String, Object>) response.getBody().get("data");
            List<Map<String, Object>> teamLinks = (List<Map<String, Object>>) inspectionData.get("inspection_teams");
            
            if (teamLinks == null || teamLinks.isEmpty()) {
                log.warn("No team links found for inspection {}", inspectionId);
                return Collections.emptyList();
            }

            List<Map<String, Object>> teams = new ArrayList<>();
            for (Map<String, Object> teamLink : teamLinks) {
                String teamName = (String) teamLink.get("team_name");
                if (teamName != null) {
                    try {
                        Map<String, Object> teamDetails = fetchTeamDetails(teamName);
                        if (teamDetails != null) {
                            // Add metadata from the team link
                            teamDetails.put("link_name", teamLink.get("name"));
                            teamDetails.put("members_count", teamLink.get("members_count")); 
                            teamDetails.put("schools_count", teamLink.get("schools_count"));
                            teams.add(teamDetails);
                        }
                    } catch (QamisApiException e) {
                        log.error("Error fetching team details for team {} in inspection {}: {}", 
                                teamName, inspectionId, e.getMessage());
                    }
                }
            }
            
            return teams;
        } catch (Exception e) {
            log.error("Error fetching teams for inspection {}: {}", inspectionId, e.getMessage());
            throw new QamisApiException("Failed to fetch teams for inspection: " + inspectionId, e);
        }
    }

    public Map<String, Object> fetchTeamDetails(String teamId) throws QamisApiException {
        if (teamId == null || teamId.trim().isEmpty()) {
            throw new IllegalArgumentException("Team ID cannot be null or empty");
        }

        try {
            String url = qamisConfig.getApiUrl() + "/api/resource/Inspection Team/" + teamId;
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "token " + qamisConfig.getApiToken());
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
            );
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new QamisApiException("QAMIS API returned error status when fetching team details: " + response.getStatusCode(), 
                    response.getStatusCode().value());
            }
            
            if (response.getBody() == null || !response.getBody().containsKey("data")) {
                throw new QamisApiException("No team details found for ID " + teamId, 404);
            }

            return (Map<String, Object>) response.getBody().get("data");
        } catch (Exception e) {
            log.error("Error fetching team details for {}: {}", teamId, e.getMessage());
            throw new QamisApiException("Failed to fetch team details: " + teamId, e);
        }
    }

    private void validateInspectionDTO(InspectionDTO dto, String inspectionId) {
        if (dto.getId() == null || dto.getId().trim().isEmpty()) {
            throw new QamisApiException("Invalid inspection data: missing ID for inspection: " + inspectionId, 500);
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new QamisApiException("Invalid inspection data: missing name for inspection: " + inspectionId, 500);
        }
        if (dto.getWorkflowState() == null || dto.getWorkflowState().trim().isEmpty()) {
            throw new QamisApiException("Invalid inspection data: missing workflow state for inspection: " + inspectionId, 500);
        }
    }

    private InspectionDTO mapToInspectionDTO(Map<String, Object> data) {
        InspectionDTO dto = new InspectionDTO();
        dto.setId((String) data.get("name")); // In QAMIS API, 'name' is used as ID
        dto.setName((String) data.get("name"));
        dto.setInspectionName((String) data.get("inspection_name"));
        dto.setWorkflowState((String) data.get("workflow_state"));
        dto.setCustomFields(data);
        return dto;
    }



    /**
     * Fetch data from Frappe API.
     *
     * @param lastSyncTimeStamp - Timestamp of the last successful synchronization.
     * @return List of raw data maps fetched from the API.
     */

    public List<SchoolIdentificationDTO> fetchSchoolIdentification(LocalDateTime lastSyncTimeStamp){
        try{
            LocalDateTime defaultTimestamp = LocalDateTime.of(2024,11,1,0,0);
            LocalDateTime timestamp = lastSyncTimeStamp != null ? lastSyncTimeStamp : defaultTimestamp;

            String apiURL = qamisConfig.getApiUrl() + "/api/resource/School Identification?filters=[[\"modified\", \">\", \"" + timestamp + "\"]]&fields=[\"name\",\"modified\"]";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization","token"+ qamisConfig.getApiToken());

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiURL,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );
            log.info("Fetching basic school identifications from Frappe API: {}", apiURL);
            log.info("response from the api:{}",response);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null){
                Map<String,Object> body = response.getBody();
                if (body.containsKey("data")){
                    List<Map<String,Object>> rawData = (List<Map<String, Object>>) body.get("data");
                    return rawData.stream().map(this::mapToSchoolIdentificationDTO).collect(Collectors.toList());
                }
            }
            log.warn("Unexpected or empty response from Frappe API.");
            return Collections.emptyList();

        }catch (Exception e){
            log.error("Error fetching basic school identifications: {}", e.getMessage(), e);
            throw new RuntimeException("failed to fetch school identification",e);
        }
    }

    public SchoolIdentificationDTO fetchSchoolIdentificationDetails(String schoolName){

        try{
            String apiURL = qamisConfig.getApiUrl() + "/api/resource/School Identification/" + schoolName;
            log.debug("school names:{}",schoolName);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization","token" + qamisConfig.getApiToken());

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiURL,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );
            log.info("Fetching detailed data for school: {}", schoolName);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null){
                Map<String,Object> data = (Map<String, Object>) response.getBody().get("data");
                if (data != null){
                    SchoolIdentificationDTO dto = mapToSchoolIdentificationDTO(data);
                    validateSchoolIdentificationDTO(dto,schoolName);
                    return dto;
                }
            }
            log.warn("No details found for school: {}", schoolName);
            throw new RuntimeException("No details found for school:"+schoolName);
        }catch (Exception e){
            log.error("Error fetching detailed data for school {}: {}", schoolName, e.getMessage(), e);
            throw new RuntimeException("failed to fetch school identification data for school name:"+schoolName,e);
        }

    }

    private SchoolIdentificationDTO mapToSchoolIdentificationDTO(Map<String, Object> data) {
        return SchoolIdentificationDTO.builder()
                .schoolName((String) data.get("name"))
                .schoolCode((Integer) data.get("school_code"))
                .schoolStatus((String) data.get("status"))
                .schoolOwner((String) data.get("school_owner"))
                .schoolOwnerContact((String) data.get("contact"))
                .accommodationStatus((String) data.get("accommodation_status"))
                .yearOfEstablishment((Integer) data.get("year_of_establishment"))
                .village((String) data.get("village"))
                .cell((String) data.get("cell"))
                .sector((String) data.get("sector"))
                .district((String) data.get("district"))
                .province((String) data.get("province"))
                .headteacherName((String) data.get("ht_name"))
                .headteacherQualification((String) data.get("qualification_of_headteacher"))
                .headteacherTelephone((String) data.get("telephone"))
                .numberOfBoys((Integer) data.get("number_of_boys"))
                .numberOfGirls((Integer) data.get("number_of_girls"))
                .totalStudents((Integer) data.get("total_nr_students"))
                .studentsWithSen((Integer) data.get("students_with_sen"))
                .numberOfMaleTeachers((Integer) data.get("number_of_male_teachers"))
                .numberOfFemaleTeachers((Integer) data.get("number_of_female_teachers"))
                .totalTeachers((Integer) data.get("number_of_teachers"))
                .numberOfMaleAssistantTeachers((Integer) data.get("number_of_male_assistant_teachers"))
                .numberOfFemaleAssistantTeachers((Integer) data.get("number_of_female_assistant_teachers"))
                .totalAssistantTeachers((Integer) data.get("number_of_assistant_teachers"))
                .totalAdministrativeStaff((Integer) data.get("total_number_of_administrative_staff"))
                .headteacher((Integer) data.get("headteacher"))
                .deputyHeadteacher((Integer) data.get("deputy_headteacher"))
                .secretary((Integer) data.get("secretary"))
                .librarian((Integer) data.get("librarian"))
                .accountant((Integer) data.get("accountant"))
                .otherAdministrativeStaff((Integer) data.get("other_staff"))
                .totalSupportingStaff((Integer) data.get("total_number_of_supporting_staff"))
                .cleaners((Integer) data.get("cleaners"))
                .watchmen((Integer) data.get("watchmen"))
                .schoolCooks((Integer) data.get("school_cooks"))
                .storekeeper((Integer) data.get("storekeeper"))
                .drivers((Integer) data.get("drivers"))
                .otherSupportingStaff((Integer) data.get("other_supporting_staff"))
                .numberOfClassrooms((Integer) data.get("nbr_of_classrooms"))
                .numberOfLatrines((Integer) data.get("nbr_of_latrines"))
                .numberOfKitchens((Integer) data.get("number_of_kitchen"))
                .numberOfDiningHalls((Integer) data.get("number_of_dining_hall"))
                .numberOfLibraries((Integer) data.get("number_of_library"))
                .numberOfSmartClassrooms((Integer) data.get("number_of_smart_classrooms"))
                .numberOfComputerLabs((Integer) data.get("number_of_computer_lab"))
                .numberOfAdministrativeOffices((Integer) data.get("number_of_admin_offices"))
                .numberOfMultipurposeHalls((Integer) data.get("number_of_multipurpose_halls"))
                .numberOfAcademicStaffRooms((Integer) data.get("number_of_academic_staff_rooms"))
                .lastModified(parseLocalDateTime((String) data.get("modified")))
                .build();
    }

    private void validateSchoolIdentificationDTO(SchoolIdentificationDTO dto,String schoolName){
        if (dto.getSchoolName() == null || dto.getSchoolName().isEmpty()){
            throw new RuntimeException("invalid data: school name is missing for school:"+schoolName);
        }
    }

    private LocalDateTime parseLocalDateTime(String dateTimeString) {
        try {
            return LocalDateTime.parse(dateTimeString, CUSTOM_DATE_FORMATTER);
        } catch (Exception e) {
            log.error("Error parsing date string: {}", dateTimeString, e);
            throw new RuntimeException("Failed to parse date string: " + dateTimeString, e);
        }
    }






}
