package rw.gov.mineduc.qamis.integration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import rw.gov.mineduc.qamis.integration.model.SchoolIdentification;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SchoolIdentificationRepository  extends JpaRepository<SchoolIdentification,Integer> , JpaSpecificationExecutor<SchoolIdentification> {
    Optional<SchoolIdentification> findTopByOrderByLastModifiedDesc();
   // boolean existsBySchoolCode(Integer schoolCode);
    //boolean existsBySchoolNameAndProvinceAndVillage(String schoolName,String province,String village);
    Optional<SchoolIdentification> findBySchoolCode(Integer schoolCode);
   Optional<SchoolIdentification> findBySchoolNameAndProvinceAndVillage(String schoolName,String province,String village);
}
