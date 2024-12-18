package rw.gov.mineduc.qamis.integration.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rw.gov.mineduc.qamis.integration.model.School;
import rw.gov.mineduc.qamis.integration.model.SchoolIdentification;
import rw.gov.mineduc.qamis.integration.repository.SchoolIdentificationRepository;
import rw.gov.mineduc.qamis.integration.repository.SchoolRepository;

import java.io.*;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@Service
public class FileProcessingService {

    @Autowired
    private SchoolRepository schoolRepository;

    @Autowired
    private SchoolIdentificationRepository schoolIdentificationRepository;

    public List<School> processSchoolFile(MultipartFile file) throws IOException, CsvException {
        return processCsvFile(file.getInputStream());
    }

    public List<School> processSchoolFileForTesting(String filePath) throws IOException, CsvException {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            return processCsvFile(fis);
        }
    }

    private List<School> processCsvFile(InputStream is) throws IOException, CsvException {
        List<School> schools = new ArrayList<>();
        List<SchoolIdentification> schoolIdentifications = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new InputStreamReader(is))) {
            List<String[]> rows = reader.readAll();

            // Skip the header row
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                Integer schoolCode = parseIntOrNull(row[0]);


                if (schoolCode == null) {
                    continue; // Skip rows with invalid school codes
                }

                try {
                    School school = schoolRepository.findById(schoolCode).orElse(new School());

                    school.setSchoolCode(schoolCode);
                    school.setSchoolName(parseStringOrNull(row[1]));
                    school.setProvince(parseStringOrNull(row[2]));
                    school.setDistrict(parseStringOrNull(row[3]));
                    school.setSector(parseStringOrNull(row[4]));
                    school.setCell(parseStringOrNull(row[5]));
                    school.setVillage(parseStringOrNull(row[6]));
                    school.setSchoolStatus(parseStringOrNull(row[7]));
                    school.setSchoolOwner(parseStringOrNull(row[8]));
                    school.setLatitude(parseDoubleOrNull(row[9]));
                    school.setLongitude(parseDoubleOrNull(row[10]));
                    school.setDay(parseStringOrNull(row[11]));
                    school.setBoarding(parseStringOrNull(row[12]));
                    String email = parseStringOrNull(row[20]);
                    if (isValidEmail(email)) {
                        school.setSchoolEmail(email);
                    } else {
                        System.err.println("Invalid email format for school code " + schoolCode + ": " + email);
                    }

                    schools.add(school);

                    synchronizeSchoolIdentification(school,schoolIdentifications);
                } catch (Exception e) {
                    // Log the error and continue with the next row
                    System.err.println("Error processing row " + i + ": " + e.getMessage());
                    log.error("Error in processing schools csv file",e.getMessage());
                }
            }
        }

        schoolRepository.saveAll(schools);

        schoolIdentificationRepository.saveAll(schoolIdentifications);

        return schools;
    }

    private Integer parseIntOrNull(String value) {
        try {
            return value != null && !value.isEmpty() ? Integer.parseInt(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double parseDoubleOrNull(String value) {
        try {
            return value != null && !value.isEmpty() ? Double.parseDouble(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String parseStringOrNull(String value) {
        return (value != null && !value.isEmpty() && !value.equalsIgnoreCase("NULL")) ? value : "";
    }

    private void synchronizeSchoolIdentification(School school, List<SchoolIdentification> schoolIdentifications){
        Integer schoolCode = school.getSchoolCode();

        Optional<SchoolIdentification> existingSchoolIdentification = schoolIdentificationRepository.findBySchoolCode(schoolCode);

        SchoolIdentification schoolIdentification = existingSchoolIdentification.orElse(new SchoolIdentification());

        schoolIdentification.setSchoolCode(schoolCode);
        schoolIdentification.setSchoolName(school.getSchoolName());
        schoolIdentification.setSchoolStatus(school.getSchoolStatus());
        schoolIdentification.setSchoolOwner(school.getSchoolOwner());
        schoolIdentification.setProvince(school.getProvince());
        schoolIdentification.setDistrict(school.getDistrict());
        schoolIdentification.setSector(school.getSector());
        schoolIdentification.setCell(school.getCell());
        schoolIdentification.setVillage(school.getVillage());
        schoolIdentification.setLatitude(school.getLatitude());
        schoolIdentification.setLongitude(school.getLongitude());
        schoolIdentification.setSchoolEmail(school.getSchoolEmail());

        schoolIdentifications.add(schoolIdentification);
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        Pattern pattern = Pattern.compile(emailRegex);
        return email != null && pattern.matcher(email).matches();
    }
}
