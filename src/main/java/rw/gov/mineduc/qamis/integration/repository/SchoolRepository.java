package rw.gov.mineduc.qamis.integration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.gov.mineduc.qamis.integration.model.School;

public interface SchoolRepository extends JpaRepository<School, Integer> {
}
