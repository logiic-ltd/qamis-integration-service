package rw.gov.mineduc.qamis.integration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import rw.gov.mineduc.qamis.integration.model.InspectionChecklist;

public interface InspectionChecklistRepository extends JpaRepository<InspectionChecklist, String>, JpaSpecificationExecutor<InspectionChecklist> {
}
