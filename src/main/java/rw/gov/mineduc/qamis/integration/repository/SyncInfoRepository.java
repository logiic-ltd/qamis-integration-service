package rw.gov.mineduc.qamis.integration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.gov.mineduc.qamis.integration.model.SyncInfo;

public interface SyncInfoRepository extends JpaRepository<SyncInfo, String> {
}
