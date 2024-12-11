package rw.gov.mineduc.qamis.integration.service;


import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.gov.mineduc.qamis.integration.dto.SchoolIdentificationDTO;
import rw.gov.mineduc.qamis.integration.model.School;
import rw.gov.mineduc.qamis.integration.model.SchoolIdentification;

import rw.gov.mineduc.qamis.integration.repository.SchoolIdentificationRepository;
import rw.gov.mineduc.qamis.integration.repository.SchoolRepository;



import java.lang.reflect.Field;

import java.time.LocalDateTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;



@Service
public class SchoolService {

    private static final Logger log = LoggerFactory.getLogger(SchoolService.class);

    @Autowired
    private SchoolRepository schoolRepository;



    @Autowired
    private SchoolIdentificationRepository schoolIdentificationRepository;

    @Autowired
    private QamisIntegrationService qamisIntegrationService;

    @Autowired
    private ObjectMapper objectMapper;



    public Map<String, Object> getSchoolDetails(Integer schoolCode, List<String> properties) {
        Optional<School> schoolOptional = schoolRepository.findById(schoolCode);
        if (schoolOptional.isEmpty()) {
            return null;
        }

        School school = schoolOptional.get();
        Map<String, Object> result = new HashMap<>();

        if (properties == null || properties.isEmpty()) {
            // Return all properties if no specific properties are requested
            for (Field field : School.class.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    result.put(field.getName(), field.get(school));
                } catch (IllegalAccessException e) {
                    // Log the error and continue
                    e.printStackTrace();
                }
            }
        } else {
            for (String property : properties) {
                try {
                    Field field = School.class.getDeclaredField(property);
                    field.setAccessible(true);
                    result.put(property, field.get(school));
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    // Log the error and continue
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

    public Page<School> searchSchools(String searchTerm, Pageable pageable) {
        Specification<School> spec = (root, query, cb) -> {
            String lowercaseSearchTerm = "%" + searchTerm.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("schoolName")), lowercaseSearchTerm),
                cb.like(cb.lower(root.get("schoolCode").as(String.class)), lowercaseSearchTerm)
            );
        };

        return schoolRepository.findAll(spec, pageable);
    }

    public Map<String, Object> getSchoolSummary(School school) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("schoolCode", school.getSchoolCode());
        summary.put("schoolName", school.getSchoolName());
        summary.put("province", school.getProvince());
        summary.put("district", school.getDistrict());
        summary.put("sector", school.getSector());
        summary.put("cell", school.getCell());
        summary.put("village", school.getVillage());
        return summary;
    }




    /* the process of synchronising school identification*/
    @Scheduled(cron = "${inspection.syncCron}")
    public void scheduleSchoolIdentificationSync(){
        log.info("Starting scheduled synchronization for school identification data.");
        LocalDateTime lastSyncTimestamp = schoolIdentificationRepository.findTopByOrderByLastModifiedDesc().map(SchoolIdentification::getLastModified).orElse(LocalDateTime.of(2024,11,01,0,0));
        log.info("Last synchronization timestamp: {}", lastSyncTimestamp);
        try {
            List<SchoolIdentificationDTO> schoolIdentificationData = qamisIntegrationService.fetchSchoolIdentification(lastSyncTimestamp);
            for (SchoolIdentificationDTO dto : schoolIdentificationData){
                try {
                    SchoolIdentificationDTO detailData = qamisIntegrationService.fetchSchoolIdentificationDetails(dto.getSchoolName());
                    syncSchoolIdentification(detailData);
                }catch (Exception e){
                    log.error("Failed to synchronize school: {}", dto.getSchoolName(), e);
                }
            }
            log.info("Completed scheduled synchronization for school identification data.");
        }catch (Exception e){
            log.error("Error during scheduled synchronization: {}", e.getMessage());
        }
    }



    @Transactional
    public void syncSchoolIdentification(SchoolIdentificationDTO dto){
        log.info("Starting sync for school: {}", dto.getSchoolName());
        try{
            validateSchoolIdentificationDTO(dto);

            SchoolIdentification newSchoolIdentification = convertDTOToSchoolIdentification(dto);

            Optional<SchoolIdentification> existingSchoolIdentification;
            if (dto.getSchoolCode() != null){
                existingSchoolIdentification = schoolIdentificationRepository.findBySchoolCode(dto.getSchoolCode());
            }else {
                existingSchoolIdentification = schoolIdentificationRepository.findBySchoolNameAndProvinceAndVillage(dto.getSchoolName(),dto.getProvince(),dto.getVillage());

            }

            if (existingSchoolIdentification.isPresent()){
                SchoolIdentification existingSchool = existingSchoolIdentification.get();
                if (!shouldSyncSchoolIdentification(existingSchool,newSchoolIdentification)){
                    log.info("No update needed for school: {}", dto.getSchoolName());
                    return;
                }
                newSchoolIdentification.setSchoolCode(existingSchool.getSchoolCode());
                log.info("Updating existing school: {}", dto.getSchoolName());
            }else {
                log.info("Creating new school: {}", dto.getSchoolName());

            }

            schoolIdentificationRepository.save(newSchoolIdentification);
            log.info("Successfully synchronized school: {}", dto.getSchoolName());
        }catch (Exception e){
            log.error("Error during synchronization for school: {}", dto.getSchoolName(), e);
            throw new RuntimeException("failed to synchronise school:"+dto.getSchoolName(),e);
        }
    }

    private boolean shouldSyncSchoolIdentification(SchoolIdentification existingSchool,SchoolIdentification newSchool){
        if (newSchool.getLastModified().isAfter(existingSchool.getLastModified())){
            log.info("Detected updated data for school: {}", newSchool.getSchoolName());
            return true;
        }
        log.info("No updates needed for school: {}", existingSchool.getSchoolName());
        return false;
    }

    private void validateSchoolIdentificationDTO(SchoolIdentificationDTO dto) {
        if (dto.getSchoolName() == null || dto.getSchoolName().isEmpty()) {
            throw new IllegalArgumentException("School name cannot be null or empty");
        }
        if (dto.getDistrict() == null || dto.getDistrict().isEmpty()) {
            throw new IllegalArgumentException("District cannot be null or empty for school: " + dto.getSchoolName());
        }

    }

    private SchoolIdentification convertDTOToSchoolIdentification(SchoolIdentificationDTO dto){
        return SchoolIdentification.builder()
                .schoolName(dto.getSchoolName())
                .schoolCode(dto.getSchoolCode())
                .schoolStatus(dto.getSchoolStatus())
                .schoolOwner(dto.getSchoolOwner())
                .schoolOwnerContact(dto.getSchoolOwnerContact())
                .accommodationStatus(dto.getAccommodationStatus())
                .yearOfEstablishment(dto.getYearOfEstablishment())
                .province(dto.getProvince())
                .district(dto.getDistrict())
                .sector(dto.getSector())
                .cell(dto.getCell())
                .village(dto.getVillage())
                .headteacherName(dto.getHeadteacherName())
                .headteacherQualification(dto.getHeadteacherQualification())
                .headteacherTelephone(dto.getHeadteacherTelephone())
                .totalStudents(dto.getTotalStudents())
                .numberOfGirls(dto.getNumberOfGirls())
                .numberOfBoys(dto.getNumberOfBoys())
                .studentsWithSen(dto.getStudentsWithSen())
                .numberOfMaleTeachers(dto.getNumberOfMaleTeachers())
                .numberOfFemaleTeachers(dto.getNumberOfFemaleTeachers())
                .totalTeachers(dto.getTotalTeachers())
                .numberOfMaleAssistantTeachers(dto.getNumberOfMaleAssistantTeachers())
                .numberOfFemaleAssistantTeachers(dto.getNumberOfFemaleAssistantTeachers())
                .totalAssistantTeachers(dto.getTotalAssistantTeachers())
                .totalAdministrativeStaff(dto.getTotalAdministrativeStaff())
                .deputyHeadteacher(dto.getDeputyHeadteacher())
                .secretary(dto.getSecretary())
                .librarian(dto.getLibrarian())
                .accountant(dto.getAccountant())
                .totalSupportingStaff(dto.getTotalSupportingStaff())
                .cleaners(dto.getCleaners())
                .watchmen(dto.getWatchmen())
                .schoolCooks(dto.getSchoolCooks())
                .storeKeeper(dto.getStorekeeper())
                .drivers(dto.getDrivers())
                .nbrOfClassrooms(dto.getNumberOfClassrooms())
                .nbrOfLatrines(dto.getNumberOfLatrines())
                .numberOfKitchen(dto.getNumberOfKitchens())
                .numberOfDiningHall(dto.getNumberOfDiningHalls())
                .numberOfLibrary(dto.getNumberOfLibraries())
                .numberOfSmartClassrooms(dto.getNumberOfSmartClassrooms())
                .numberOfComputerLab(dto.getNumberOfComputerLabs())
                .numberOfAdminOffices(dto.getNumberOfAdministrativeOffices())
                .numberOfMultipurposeHalls(dto.getNumberOfMultipurposeHalls())
                .numberOfAcademicStaffRooms(dto.getNumberOfAcademicStaffRooms())
                .latitude(dto.getLatitudes())
                .longitude(dto.getLongitude())
                .lastModified(dto.getLastModified())
                .build();
    }







}
