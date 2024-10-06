package rw.gov.mineduc.qamis.integration.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rw.gov.mineduc.qamis.integration.model.School;
import rw.gov.mineduc.qamis.integration.repository.SchoolRepository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileProcessingService {

    @Autowired
    private SchoolRepository schoolRepository;

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

                    schools.add(school);
                } catch (Exception e) {
                    // Log the error and continue with the next row
                    System.err.println("Error processing row " + i + ": " + e.getMessage());
                }
            }
        }

        return schoolRepository.saveAll(schools);
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
}
