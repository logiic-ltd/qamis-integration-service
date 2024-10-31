package rw.gov.mineduc.qamis.integration.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class InspectionService {
    private static final Logger log = LoggerFactory.getLogger(InspectionService.class);

    @Autowired
    private InspectionRepository inspectionRepository;

    @Autowired
    private InspectionTeamRepository inspectionTeamRepository;

    @Autowired
    private InspectionChecklistRepository inspectionChecklistRepository;

    @Transactional(readOnly = true)
    public Page<Inspection> findAllInspections(Pageable pageable) {
        log.debug("Fetching page {} of inspections", pageable.getPageNumber());
        return inspectionRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Inspection> findInspectionByName(String name) {
        log.debug("Fetching inspection with name: {}", name);
        return inspectionRepository.findById(name);
    }

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

    @Transactional
    public void syncInspection(Inspection inspection) {
        log.info("Starting sync for inspection: {}", inspection.getName());
        try {
            Inspection existingInspection = inspectionRepository.findById(inspection.getName())
                    .orElse(null);

            if (shouldSyncInspection(existingInspection, inspection)) {
                log.debug("Syncing inspection data for: {}", inspection.getName());
                inspection = inspectionRepository.save(inspection);

                syncTeams(inspection);
                syncChecklists(inspection);

                log.info("Successfully synced inspection: {}", inspection.getName());
            } else {
                log.debug("Skipping sync for inspection {}, no updates needed", inspection.getName());
            }
        } catch (Exception e) {
            log.error("Error during inspection sync {}: {}", inspection.getName(), e.getMessage());
            throw new InspectionSyncException("Failed to sync inspection: " + inspection.getName(), e);
        }
    }

    private boolean shouldSyncInspection(Inspection existing, Inspection incoming) {
        return existing == null || 
               incoming.getLastModified().isAfter(existing.getLastModified());
    }

    private void syncTeams(Inspection inspection) {
        log.debug("Syncing {} teams for inspection {}", 
                 inspection.getTeams().size(), inspection.getName());
        
        for (InspectionTeam team : inspection.getTeams()) {
            try {
                team.setInspection(inspection);
                inspectionTeamRepository.save(team);
            } catch (Exception e) {
                log.error("Error saving team {} for inspection {}: {}", 
                         team.getName(), inspection.getName(), e.getMessage());
                throw new InspectionSyncException(
                    "Failed to sync team: " + team.getName() + 
                    " for inspection: " + inspection.getName(), e);
            }
        }
    }

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

    @Autowired
    private QamisIntegrationService qamisIntegrationService;

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
                inspection.getTeams().add(team);
            }
        }
        
        // Handle checklists
        List<Map<String, Object>> checklists = (List<Map<String, Object>>) data.get("checklists");
        if (checklists != null) {
            for (Map<String, Object> checklistData : checklists) {
                InspectionChecklist checklist = new InspectionChecklist();
                checklist.setName((String) checklistData.get("name"));
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
}
