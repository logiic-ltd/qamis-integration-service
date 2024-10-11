package rw.gov.mineduc.qamis.integration.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import rw.gov.mineduc.qamis.integration.model.DHIS2User;

import java.time.LocalDateTime;
import java.util.Optional;

public interface DHIS2UserRepository extends JpaRepository<DHIS2User, String>, JpaSpecificationExecutor<DHIS2User> {
    Optional<DHIS2User> findTopByOrderByLastUpdatedDesc();
    Page<DHIS2User> findByLastUpdatedBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
}
