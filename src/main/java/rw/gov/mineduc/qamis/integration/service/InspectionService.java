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

    @Scheduled(cron = "${inspection.syncCron}")
    @Transactional
    public void scheduledSyncApprovedInspections() {
        log.info("Starting scheduled sync of approved inspections");
        try {
            Page<Inspection> approvedInspections = inspectionRepository.findAll(
                (root, query, cb) -> cb.equal(root.get("workflowState"), "APPROVED"),
                Pageable.unpaged()
            );
            
            log.debug("Found {} approved inspections to sync", approvedInspections.getContent().size());
            
            for (Inspection inspection : approvedInspections.getContent()) {
                try {
                    syncInspection(inspection);
                } catch (Exception e) {
                    log.error("Failed to sync approved inspection {}: {}", 
                             inspection.getName(), e.getMessage());
                }
            }
            log.info("Completed scheduled sync of approved inspections");
        } catch (Exception e) {
            log.error("Error during scheduled inspection sync: {}", e.getMessage());
            throw new InspectionSyncException("Scheduled inspection sync failed", e);
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
