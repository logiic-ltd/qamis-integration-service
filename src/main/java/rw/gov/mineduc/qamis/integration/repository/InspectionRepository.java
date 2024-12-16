package rw.gov.mineduc.qamis.integration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import rw.gov.mineduc.qamis.integration.model.Inspection;

import java.time.LocalDate;
import java.util.List;

public interface InspectionRepository extends JpaRepository<Inspection, String>, JpaSpecificationExecutor<Inspection> {

	List<Inspection> findByWorkflowStateAndIsSynced(String workflowState, boolean isSynced);
	List<Inspection> findByWorkflowStateAndIsSyncedAndStartDateLessThanEqual(
			String workflowState, boolean isSynced, LocalDate date);
}
