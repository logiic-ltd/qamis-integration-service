package rw.gov.mineduc.qamis.integration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import rw.gov.mineduc.qamis.integration.model.InspectionTeam;

public interface InspectionTeamRepository extends JpaRepository<InspectionTeam, String>, JpaSpecificationExecutor<InspectionTeam> {
}
