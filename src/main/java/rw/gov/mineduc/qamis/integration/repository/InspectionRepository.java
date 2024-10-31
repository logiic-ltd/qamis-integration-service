package rw.gov.mineduc.qamis.integration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import rw.gov.mineduc.qamis.integration.model.Inspection;

public interface InspectionRepository extends JpaRepository<Inspection, String>, JpaSpecificationExecutor<Inspection> {
}
