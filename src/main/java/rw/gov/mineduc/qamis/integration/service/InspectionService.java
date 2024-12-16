package rw.gov.mineduc.qamis.integration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;


import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;

import org.springframework.http.HttpHeaders;


import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;
import java.util.*;

import org.springframework.scheduling.support.CronTrigger;
import org.springframework.web.client.RestTemplate;
import rw.gov.mineduc.qamis.integration.dto.InspectionDTO;
import rw.gov.mineduc.qamis.integration.model.TeamMember;
import rw.gov.mineduc.qamis.integration.model.TeamSchool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.gov.mineduc.qamis.integration.exception.InspectionSyncException;
import rw.gov.mineduc.qamis.integration.model.Inspection;
import rw.gov.mineduc.qamis.integration.model.InspectionChecklist;

import rw.gov.mineduc.qamis.integration.model.InspectionTeam;
import rw.gov.mineduc.qamis.integration.repository.InspectionChecklistRepository;
import rw.gov.mineduc.qamis.integration.repository.InspectionRepository;
import rw.gov.mineduc.qamis.integration.repository.InspectionTeamRepository;


import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class InspectionService {
    private static final Logger log = LoggerFactory.getLogger(InspectionService.class);

    @Autowired
    private InspectionRepository inspectionRepository;

    @Autowired
    private InspectionTeamRepository inspectionTeamRepository;

    @Autowired
    private InspectionChecklistRepository inspectionChecklistRepository;

    @Value("${dhis2.apiUrl}")
    private String dhis2ApiUrl;

    @Value("${dhis2.username}")
    private String dhis2Username;

    @Value("${dhis2.password}")
    private String dhis2Password;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TaskScheduler taskScheduler;
    @Autowired
    private QamisIntegrationService qamisIntegrationService;

    private final Map<String, String> dataElementMap = new HashMap<>();

    /**
     * Constructor to initialize InspectionService. Loads all configuration properties prefixed with "dataElement." into the dataElementMap.
     * This allows dynamic configuration of data element mappings via the application properties.
     */
    public InspectionService(ConfigurableEnvironment environment) {

        String prefix = "dataElement.";

        // Iterate through all property sources in the environment
        environment.getPropertySources().forEach(source -> {
            if (source instanceof EnumerablePropertySource<?> enumerableSource) {
                // Get all property names and filter those with the given prefix
                String[] propertyNames = enumerableSource.getPropertyNames();
                Map<String, String> filteredProperties = Stream.of(propertyNames)
                        .filter(name -> name.startsWith(prefix))
                        .collect(Collectors.toMap(
                                name -> name.substring(prefix.length()), // Remove prefix from key
                                name -> (String) enumerableSource.getProperty(name) // Get value
                        ));
                dataElementMap.putAll(filteredProperties);
            }
        });

        log.debug("Loaded dataElementMap: {}", dataElementMap);
    }

    /**
     * Schedules a task to update inspections every minute using a cron expression.
     */
    @PostConstruct
    public void ScheduleWithCronTrigger() {
        taskScheduler.schedule(this::ScheduleInspectionQamisUpdate, new CronTrigger("0 0 1 * * *"));
    }

    /**
     * Fetches a paginated list of all inspections from the database.
     * @param pageable Pagination information
     * @return A page of inspections
     */
    @Transactional(readOnly = true)
    public Page<Inspection> findAllInspections(Pageable pageable) {
        log.debug("Fetching page {} of inspections", pageable.getPageNumber());
        return inspectionRepository.findAll(pageable);
    }

    /**
     * Fetches an inspection by its name.
     * @param name Name of the inspection
     * @return An Optional containing the inspection if found
     */
    @Transactional(readOnly = true)
    public Optional<Inspection> findInspectionByName(String name) {
        log.debug("Fetching inspection with name: {}", name);
        return inspectionRepository.findById(name);
    }

    /**
     * Saves a new or updated inspection to the database. Ensures the inspection is validated before saving.
     * @param inspection The inspection object to save
     * @return The saved inspection
     */
    @Transactional
    public Inspection saveInspection(Inspection inspection) {
        log.info("Saving inspection: {}", inspection.getName());
        try {
            validateInspection(inspection);
            return inspectionRepository.save(inspection);
        } catch (Exception e) {
            log.error("Error saving inspection {}: {}", inspection.getName(), e.getMessage());
            throw new InspectionSyncException("Failed to save inspection: " + inspection.getName(), e);
        }
    }

    /**
     * Synchronizes an inspection with the database. Handles both creating new inspections and updating existing ones.
     * Ensures related entities (teams, checklists) are synchronized properly.
     * @param inspection The inspection object to synchronize
     */
    @Transactional
    public void syncInspection(Inspection inspection) {
        log.info("Starting sync for inspection: {}", inspection.getName());
        try {
            validateInspection(inspection);

            Optional<Inspection> existingInspectionOpt = inspectionRepository.findById(inspection.getName());
            boolean isUpdate = existingInspectionOpt.isPresent();

            if (isUpdate) {
                Inspection existingInspection = existingInspectionOpt.get();
                if (!shouldSyncInspection(existingInspection, inspection)) {
                    return;
                }
                log.info("Updating existing inspection: {} (Last modified: {})",
                        inspection.getName(), existingInspection.getLastModified());

                // Clear existing relationships to prevent orphaned records
                existingInspection.getTeams().clear();
                existingInspection.getChecklists().clear();
                inspectionRepository.save(existingInspection);
            } else {
                log.info("Creating new inspection: {}", inspection.getName());
            }

            log.debug("Syncing inspection data for: {}", inspection.getName());

            // Save the inspection to ensure it exists in the database
            inspection = inspectionRepository.save(inspection);

            // Sync related entities (teams and checklists)
            syncTeams(inspection);
            syncChecklists(inspection);

            // Final save to update all relationships
            inspection = inspectionRepository.save(inspection);

            log.info("Successfully {} inspection: {} (Last modified: {})",
                    (isUpdate ? "updated" : "created"),
                    inspection.getName(),
                    inspection.getLastModified());
        } catch (Exception e) {
            log.error("Error during inspection sync {}: {}", inspection.getName(), e.getMessage());
            throw new InspectionSyncException("Failed to sync inspection: " + inspection.getName(), e);
        }
    }

    /**
     * Determines if an inspection needs to be synchronized by comparing last modified timestamps.
     * @param existing The existing inspection record
     * @param incoming The incoming inspection data
     * @return True if the inspection should be synchronized, false otherwise
     */
    private boolean shouldSyncInspection(Inspection existing, Inspection incoming) {
        if (existing == null) {
            log.debug("New inspection detected: {}", incoming.getName());
            return true;
        }

        if (incoming.getLastModified().isAfter(existing.getLastModified())) {
            log.debug("Updated inspection detected: {} (Local: {}, Remote: {})",
                    incoming.getName(),
                    existing.getLastModified(),
                    incoming.getLastModified());
            return true;
        }

        log.debug("No updates needed for inspection: {} (Last modified: {})",
                existing.getName(), existing.getLastModified());
        return false;
    }

    /**
     * Synchronizes all teams associated with an inspection. Ensures team members and schools are validated and saved.
     * @param inspection The inspection object whose teams need to be synchronized
     */
    private void syncTeams(Inspection inspection) {
        log.debug("Syncing {} teams for inspection {}",
                inspection.getTeams().size(), inspection.getName());

        for (InspectionTeam team : inspection.getTeams()) {
            try {
                validateTeam(team);
                team.setInspection(inspection);

                // Process team members
                for (TeamMember member : team.getMembers()) {
                    validateTeamMember(member);
                    member.setTeam(team);
                }

                // Process team schools
                for (TeamSchool school : team.getSchools()) {
                    validateTeamSchool(school);
                    school.setTeam(team);
                }

                inspectionTeamRepository.save(team);
                log.debug("Successfully saved team {} with {} members and {} schools",
                        team.getName(), team.getMembers().size(), team.getSchools().size());

            } catch (Exception e) {
                log.error("Error saving team {} for inspection {}: {}",
                        team.getName(), inspection.getName(), e.getMessage());
                throw new InspectionSyncException(
                        "Failed to sync team: " + team.getName() +
                                " for inspection: " + inspection.getName(), e);
            }
        }
    }

    /**
     * Validates a team object to ensure required fields are present and non-empty.
     * @param team The team object to validate
     */
    private void validateTeam(InspectionTeam team) {
        if (team.getName() == null || team.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Team name cannot be null or empty");
        }
        if (team.getTeamName() == null || team.getTeamName().trim().isEmpty()) {
            throw new IllegalArgumentException("Team display name cannot be null or empty");
        }
    }

    /**
     * Validates a team member object to ensure required fields are present and non-empty.
     * @param member The team member object to validate
     */
    private void validateTeamMember(TeamMember member) {
        if (member.getName() == null || member.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Team member name cannot be null or empty");
        }
    }

    /**
     * Validates a team school object to ensure required fields are present and non-empty.
     * @param school The team school object to validate
     */
    private void validateTeamSchool(TeamSchool school) {
        if (school.getSchoolCode() == null || school.getSchoolCode().trim().isEmpty()) {
            throw new IllegalArgumentException("School code cannot be null or empty");
        }
        if (school.getSchoolName() == null || school.getSchoolName().trim().isEmpty()) {
            throw new IllegalArgumentException("School name cannot be null or empty");
        }
    }

    /**
     * Synchronizes all checklists associated with an inspection.
     * @param inspection The inspection object whose checklists need to be synchronized
     */
    private void syncChecklists(Inspection inspection) {
        log.debug("Syncing {} checklists for inspection {}",
                inspection.getChecklists().size(), inspection.getName());

        for (InspectionChecklist checklist : inspection.getChecklists()) {
            try {
                checklist.setInspection(inspection);
                inspectionChecklistRepository.save(checklist);
            } catch (Exception e) {
                log.error("Error saving checklist {} for inspection {}: {}",
                        checklist.getName(), inspection.getName(), e.getMessage());
                throw new InspectionSyncException(
                        "Failed to sync checklist: " + checklist.getName() +
                                " for inspection: " + inspection.getName(), e);
            }
        }
    }


    @Scheduled(cron = "${inspection.syncCron}")
    @Transactional
    public void scheduledSyncApprovedInspections() {
        log.info("Starting scheduled sync of approved inspections from QAMIS");
        try {
            List<InspectionDTO> approvedInspections = qamisIntegrationService.fetchApprovedInspections();
            
            log.debug("Found {} approved inspections to sync from QAMIS", approvedInspections.size());
            
            for (InspectionDTO inspectionDTO : approvedInspections) {
                try {
                    InspectionDTO details = qamisIntegrationService.fetchInspectionDetails(inspectionDTO.getId());
                    if (details != null) {
                        Inspection inspection = convertDTOToInspection(details);
                        syncInspection(inspection);
                    }
                } catch (Exception e) {
                    log.error("Failed to sync approved inspection {}: {}", 
                             inspectionDTO.getName(), e.getMessage());
                }
            }
            log.info("Completed scheduled sync of approved inspections from QAMIS");
        } catch (Exception e) {
            log.error("Error during scheduled inspection sync: {}", e.getMessage());
            throw new InspectionSyncException("Scheduled inspection sync failed", e);
        }
    }

    private Inspection convertDTOToInspection(InspectionDTO dto) {
        Inspection inspection = new Inspection();
        Map<String, Object> data = dto.getCustomFields();
        
        inspection.setName(dto.getName());
        inspection.setInspectionName((String) data.get("inspection_name"));
        inspection.setWorkflowState((String) data.get("workflow_state"));
        inspection.setStartDate(parseDate((String) data.get("start_date")));
        inspection.setEndDate(parseDate((String) data.get("end_date")));
        
        // Strip HTML tags from rich text fields
        inspection.setIntroduction(stripHtml((String) data.get("introduction")));
        inspection.setObjectives(stripHtml((String) data.get("objectives")));
        inspection.setMethodology(stripHtml((String) data.get("methodology")));
        inspection.setExecutiveSummary(stripHtml((String) data.get("executive_summary")));
        
        // Handle teams from separate API call
        List<Map<String, Object>> teams = dto.getTeams();
        if (teams != null && !teams.isEmpty()) {
            for (Map<String, Object> teamData : teams) {
                InspectionTeam team = new InspectionTeam();
                team.setName((String) teamData.get("name"));
                team.setTeamName((String) teamData.get("team_name"));
                team.setInspection(inspection);
                
                // Handle team members
                List<Map<String, Object>> members = (List<Map<String, Object>>) teamData.get("members");
                if (members != null) {
                    for (Map<String, Object> memberData : members) {
                        TeamMember member = new TeamMember();
                        member.setName((String) memberData.get("name"));
                        member.setTeam(team);
                        team.getMembers().add(member);
                    }
                }
                
                // Handle team schools
                List<Map<String, Object>> schools = (List<Map<String, Object>>) teamData.get("schools");
                if (schools != null) {
                    for (Map<String, Object> schoolData : schools) {
                        TeamSchool school = new TeamSchool();
                        school.setSchoolCode((String) schoolData.get("school_code"));
                        school.setSchoolName((String) schoolData.get("school_name"));
                        school.setTeam(team);
                        team.getSchools().add(school);
                    }
                }
                
                inspection.getTeams().add(team);
            }
        }
        
        // Handle checklists
        List<Map<String, Object>> checklists = (List<Map<String, Object>>) data.get("checklists");
        if (checklists != null) {
            for (Map<String, Object> checklistData : checklists) {
                InspectionChecklist checklist = new InspectionChecklist();
                checklist.setName((String) checklistData.get("name"));
                checklist.setId((String) checklistData.get("id"));
                checklist.setShortName((String) checklistData.get("short_name"));
                checklist.setPeriodType((String) checklistData.get("period_type"));
                checklist.setLastUpdated(LocalDateTime.parse(
                    (String) checklistData.get("last_updated"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
                ));
                checklist.setInspection(inspection);
                inspection.getChecklists().add(checklist);
            }
        }
        
        inspection.setLastModified(LocalDateTime.parse(
            (String) data.get("modified"), 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
        ));
        
        return inspection;
    }

    private String stripHtml(String html) {
        if (html == null) return null;
        return html.replaceAll("<[^>]*>", "").trim();
    }
    
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null) return null;
        try {
            return LocalDate.parse(dateStr.toString());
        } catch (Exception e) {
            log.warn("Failed to parse date: {}", dateStr);
            return null;
        }
    }

    private void validateInspection(Inspection inspection) {
        if (inspection.getName() == null || inspection.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Inspection name cannot be null or empty");
        }
        if (inspection.getInspectionName() == null || inspection.getInspectionName().trim().isEmpty()) {
            throw new IllegalArgumentException("Inspection display name cannot be null or empty");
        }
        if (inspection.getStartDate() == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (inspection.getEndDate() == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }
        if (inspection.getEndDate().isBefore(inspection.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
        if (inspection.getWorkflowState() == null || inspection.getWorkflowState().trim().isEmpty()) {
            throw new IllegalArgumentException("Workflow state cannot be null or empty");
        }
    }


    /**
     * Synchronization that updates QAMIS of each checklist used in inspection.
     * The process start bellow
     */

    @Transactional
    public void ScheduleInspectionQamisUpdate(){
        List<Inspection> inspections = inspectionRepository.findByWorkflowStateAndIsSynced("Approved by DG",false);

        for (Inspection inspection : inspections){
            processInspection(inspection);
            log.info("Scheduled inspection '{}' for processing at {}", inspection.getName(), LocalDateTime.now());
           // LocalDateTime oneAmSchedule = inspection.getStartDate().atStartOfDay().plusHours(1);

           /* if (oneAmSchedule.isAfter(LocalDateTime.now())){
                taskScheduler.schedule(()-> processInspection(inspection), Timestamp.valueOf(oneAmSchedule));
                log.info("Scheduled inspection '{}' for processing at {}", inspection.getName(), oneAmSchedule);
            }*/
        }
    }

    @Transactional
    private void processInspection(Inspection inspection){
        try {
            log.info("Processing inspection '{}'", inspection.getName());
            batchUpdateChecklistForInspection(inspection);
        }catch (Exception e){
            log.error("Error processing inspection '{}': {}", inspection.getName(), e.getMessage());
        }
    }

    @Transactional
    public void batchUpdateChecklistForInspection(Inspection inspection) {
        log.info("Starting batch update for inspection: {}", inspection.getName());
        try {
            log.info("Processing inspection: {}", inspection.getName());

            // Prepare the payload
            String payLoad = preparePayloadForChecklist(inspection);
            log.debug("Payload prepared for inspection {}: {}", inspection.getName(), payLoad);

            // Create HTTP headers and make the API call
            HttpHeaders headers = createBasicAuthHeader();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> requestEntity = new HttpEntity<>(payLoad, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    dhis2ApiUrl + "/api/dataValueSets?orgUnitIdScheme=name",
                    requestEntity,
                    String.class
            );
            log.debug("POST request URL: {}", dhis2ApiUrl + "/api/dataValueSets?orgUnitIdScheme=name");
            log.debug("Payload: {}", payLoad);
            log.debug("Response status: {}, body: {}", response.getStatusCode(), response.getBody());

            // Handle the response
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully synced checklist for inspection: {}", inspection.getName());
                inspection.setSynced(true);
                inspectionRepository.save(inspection);
            } else {
                log.error("Failed to sync checklist for inspection {}: {}", inspection.getName(), response.getBody());
                throw new RuntimeException("Failed to sync checklist: " + response.getBody());
            }
        } catch (Exception e) {
            log.error("Error syncing checklist for inspection: {}", inspection.getName(), e);
        }
        log.info("Batch update process completed for inspection: {}", inspection.getName());
    }


    @Scheduled(cron = "0 0 1 * * ?") // Runs daily at 1 AM
    public void rescheduleMissedInspections() {
        List<Inspection> missedInspections = inspectionRepository.findByWorkflowStateAndIsSyncedAndStartDateLessThanEqual(
                "Approved by DG", false, LocalDate.now());

        for (Inspection inspection : missedInspections) {
            LocalDateTime midnightStartDate = inspection.getStartDate().atStartOfDay().plusHours(1);
            if (midnightStartDate.isAfter(LocalDateTime.now())) {
                taskScheduler.schedule(() -> batchUpdateChecklistForInspection(inspection),
                        Timestamp.valueOf(midnightStartDate));
                log.info("Rescheduled inspection '{}' for processing at {}", inspection.getName(), midnightStartDate);
            }
        }
    }








    private String preparePayloadForChecklist(Inspection inspection) {
        log.info("Preparing payload for inspection: {}", inspection.getName());

        String period = inspection.getEndDate().format(DateTimeFormatter.ofPattern("yyyyMM"));
        log.debug("Formatted period for inspection {}: {}", inspection.getName(), period);

        // Build the payload
        List<Map<String, String>> dataValues = new ArrayList<>();

        for (InspectionChecklist checklist : inspection.getChecklists()) {
            String checklistName = checklist.getId();

            log.debug("Processing checklist: {} for inspection: {}", checklistName, inspection.getName());

            // Retrieve dataElement IDs from application.properties
            String introductionId = getDataElementId("introduction", checklistName);
            String objectiveId = getDataElementId("objective", checklistName);
            String missionId = getDataElementId("mission", checklistName);

            log.debug("Retrieved dataElement IDs for checklist {}: [Introduction: {}, Objective: {}, Mission: {}]",
                    checklistName, introductionId, objectiveId, missionId);

            // Iterate through all teams and schools associated with the inspection
            for (InspectionTeam team : inspection.getTeams()) {
                for (TeamSchool school : team.getSchools()) {
                    String schoolName = school.getSchoolName(); // Extract school name
                    log.debug("Processing school: {} for inspection: {}", schoolName, inspection.getName());

                    // Add introduction, objective, and mission to dataValues for this school
                    if (introductionId != null) {
                        dataValues.add(createDataValue(introductionId, period, schoolName, inspection.getIntroduction()));
                        log.debug("Added introduction dataValue for school: {}", schoolName);
                    }
                    if (objectiveId != null) {
                        dataValues.add(createDataValue(objectiveId, period, schoolName, inspection.getObjectives()));
                        log.debug("Added objective dataValue for school: {}", schoolName);
                    }
                    if (missionId != null) {
                        dataValues.add(createDataValue(missionId, period, schoolName, inspection.getMethodology()));
                        log.debug("Added mission dataValue for school: {}", schoolName);
                    }
                }
            }
        }

        // Create the final JSON structure
        Map<String, Object> payload = new HashMap<>();
        payload.put("dataValues", dataValues);
        log.debug("Constructed payload for inspection {}: {}", inspection.getName(), payload);

        try {
            String jsonPayload = new ObjectMapper().writeValueAsString(payload);
            log.info("Successfully prepared JSON payload for inspection: {}", inspection.getName());
            return jsonPayload;
        } catch (JsonProcessingException e) {
            log.error("Error creating JSON payload for inspection: {}", inspection.getName(), e);
            throw new RuntimeException("Error creating JSON payload", e);
        }
    }



    private Map<String, String> createDataValue(String dataElement,String period,String orgUnit,String value){
        Map<String,String> dataValue = new HashMap<>();
        dataValue.put("dataElement",dataElement);
        dataValue.put("period",period);
        dataValue.put("orgUnit",orgUnit);
        dataValue.put("value",value);

        return dataValue;
    }

    public String getDataElementId(String type, String checklistId) {
        String key = String.format("%s.%s", type, checklistId);
        String value = dataElementMap.get(key);

        if (value == null) {
            log.warn("No dataElement ID found for key: {}", key);
        }
        return value;
    }

    private HttpHeaders createBasicAuthHeader(){
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(dhis2Username,dhis2Password);
        return headers;
    }


}
