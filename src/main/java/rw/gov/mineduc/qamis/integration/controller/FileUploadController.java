package rw.gov.mineduc.qamis.integration.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rw.gov.mineduc.qamis.integration.model.School;
import rw.gov.mineduc.qamis.integration.service.FileProcessingService;

import com.opencsv.exceptions.CsvException;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Autowired
    private FileProcessingService fileProcessingService;

    @PostMapping("/schools")
    public ResponseEntity<?> uploadSchoolFile(@RequestPart("file") MultipartFile file) {
        try {
            List<School> processedSchools = fileProcessingService.processSchoolFile(file);
            return ResponseEntity.ok().body("Successfully processed and saved " + processedSchools.size() + " schools to the database.");
        } catch (IOException | CsvException e) {
            return ResponseEntity.badRequest().body("Error processing file: " + e.getMessage());
        }
    }

}
