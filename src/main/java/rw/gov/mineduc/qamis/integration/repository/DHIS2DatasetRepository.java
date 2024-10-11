package rw.gov.mineduc.qamis.integration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import rw.gov.mineduc.qamis.integration.model.DHIS2Dataset;

public interface DHIS2DatasetRepository extends JpaRepository<DHIS2Dataset, String>, JpaSpecificationExecutor<DHIS2Dataset> {
}
