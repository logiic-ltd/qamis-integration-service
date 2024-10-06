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
                School school = new School();

                school.setSchoolCode(Integer.parseInt(row[0]));
                school.setSchoolName(row[1]);
                school.setProvince(row[2]);
                school.setDistrict(row[3]);
                school.setSector(row[4]);
                school.setCell(row[5]);
                school.setVillage(row[6]);
                school.setSchoolStatus(row[7]);
                school.setSchoolOwner(row[8]);
                school.setLatitude(Double.parseDouble(row[9]));
                school.setLongitude(Double.parseDouble(row[10]));
                school.setDay(row[11].isEmpty() ? null : row[11]);
                school.setBoarding(row[12].isEmpty() ? null : row[12]);

                schools.add(school);
            }
        }

        return schoolRepository.saveAll(schools);
    }
}
