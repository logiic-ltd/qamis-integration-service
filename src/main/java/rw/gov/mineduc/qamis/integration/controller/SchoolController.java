package rw.gov.mineduc.qamis.integration.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.gov.mineduc.qamis.integration.service.SchoolService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schools")
public class SchoolController {

    @Autowired
    private SchoolService schoolService;

    @GetMapping("/{schoolCode}")
    public ResponseEntity<Map<String, Object>> getSchoolDetails(
            @PathVariable Integer schoolCode,
            @RequestParam(required = false) List<String> properties) {
        
        Map<String, Object> schoolDetails = schoolService.getSchoolDetails(schoolCode, properties);
        
        if (schoolDetails == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(schoolDetails);
    }
}
