package rw.gov.mineduc.qamis.integration.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rw.gov.mineduc.qamis.integration.model.School;
import rw.gov.mineduc.qamis.integration.repository.SchoolRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.File;
import java.io.FileInputStream;

@Service
public class FileProcessingService {

    @Autowired
    private SchoolRepository schoolRepository;

    public List<School> processSchoolFile(MultipartFile file) throws IOException {
        return processExcelFile(file.getInputStream());
    }

    public List<School> processSchoolFileForTesting(String filePath) throws IOException {
        File file = new File(filePath);
        try (FileInputStream fis = new FileInputStream(file)) {
            return processExcelFile(fis);
        }
    }

    private List<School> processExcelFile(InputStream is) throws IOException {
        List<School> schools = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            // Skip the header row
            if (rows.hasNext()) {
                rows.next();
            }

            while (rows.hasNext()) {
                Row currentRow = rows.next();
                School school = new School();

                school.setSchoolCode((int) currentRow.getCell(0).getNumericCellValue());
                school.setSchoolName(currentRow.getCell(1).getStringCellValue());
                school.setProvince(currentRow.getCell(2).getStringCellValue());
                school.setDistrict(currentRow.getCell(3).getStringCellValue());
                school.setSector(currentRow.getCell(4).getStringCellValue());
                school.setCell(currentRow.getCell(5).getStringCellValue());
                school.setVillage(currentRow.getCell(6).getStringCellValue());
                school.setSchoolStatus(School.SchoolStatus.valueOf(currentRow.getCell(7).getStringCellValue().toUpperCase()));
                school.setSchoolOwner(School.SchoolOwner.valueOf(currentRow.getCell(8).getStringCellValue().toUpperCase()));
                school.setLatitude(currentRow.getCell(9).getNumericCellValue());
                school.setLongitude(currentRow.getCell(10).getNumericCellValue());
                school.setSchoolType(School.SchoolType.valueOf(currentRow.getCell(11).getStringCellValue().toUpperCase()));

                schools.add(school);
            }
        }

        return schoolRepository.saveAll(schools);
    }
}
