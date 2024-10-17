package rw.gov.mineduc.qamis.integration.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.gov.mineduc.qamis.integration.model.School;
import rw.gov.mineduc.qamis.integration.service.SchoolService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    /**
     * Search for schools by name or school code.
     *
     * This endpoint allows searching for schools by their name or school code.
     * The search is case-insensitive and matches any part of the name or code.
     *
     * @param name Name or code to search for
     * @param pageable Pagination information (page, size, sort)
     * @return A page of school summaries matching the search criteria
     *
     * Example usage:
     * GET /api/schools/search?name=Primary&page=0&size=20&sort=schoolName,asc
     */
    @GetMapping("/search")
    public ResponseEntity<Page<Map<String, Object>>> searchSchools(
            @RequestParam String name,
            Pageable pageable) {
        Page<School> schools = schoolService.searchSchools(name, pageable);
        Page<Map<String, Object>> schoolSummaries = schools.map(schoolService::getSchoolSummary);
        return ResponseEntity.ok(schoolSummaries);
    }
}
