package rw.gov.mineduc.qamis.integration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.gov.mineduc.qamis.integration.model.DHIS2Dataset;

import java.util.List;

public interface DHIS2DatasetRepository extends JpaRepository<DHIS2Dataset, String>, JpaSpecificationExecutor<DHIS2Dataset> {
    
    @Query("SELECT d FROM DHIS2Dataset d JOIN d.organisationUnitIds o WHERE o = :orgUnitId")
    List<DHIS2Dataset> findByOrganisationUnitIdsContaining(@Param("orgUnitId") String orgUnitId);
}
