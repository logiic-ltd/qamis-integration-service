package rw.gov.mineduc.qamis.integration.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.gov.mineduc.qamis.integration.model.School;
import rw.gov.mineduc.qamis.integration.service.SchoolService;
import rw.gov.mineduc.qamis.integration.model.School;

@RestController
@RequestMapping("/api/schools")
public class SchoolController {

  @Autowired private SchoolService schoolService;

  @GetMapping("/{schoolCode}")
  public ResponseEntity<Map<String, Object>> getSchoolDetails(
      @PathVariable Integer schoolCode, @RequestParam(required = false) List<String> properties) {

    Map<String, Object> schoolDetails = schoolService.getSchoolDetails(schoolCode, properties);

    if (schoolDetails == null) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(schoolDetails);
  }

  /**
   * Search for schools by name or school code.
   *
   * <p>This endpoint allows searching for schools by their name or school code. The search is
   * case-insensitive and matches any part of the name or code.
   *
   * @param name Name or code to search for
   * @param pageable Pagination information (page, size, sort)
   * @return A page of school summaries matching the search criteria
   *     <p>Example usage: GET /api/schools/search?name=Primary&page=0&size=20&sort=schoolName,asc
   */
  @GetMapping("/search")
  public ResponseEntity<Page<Map<String, Object>>> searchSchools(
      @RequestParam String name, Pageable pageable) {
    Page<School> schools = schoolService.searchSchools(name, pageable);
    Page<Map<String, Object>> schoolSummaries = schools.map(schoolService::getSchoolSummary);
    return ResponseEntity.ok(schoolSummaries);
  }
  @PostMapping("/")
  public ResponseEntity<String> createSchool(@RequestBody School school) {
    if (school.getSchoolName() == null || school.getSchoolName().isEmpty() ||
        school.getProvince() == null || school.getProvince().isEmpty() ||
        school.getDistrict() == null || school.getDistrict().isEmpty() ||
        school.getSector() == null || school.getSector().isEmpty()) {
      return ResponseEntity.badRequest().body("School name and address fields are mandatory.");
    }

    schoolService.saveSchool(school);
    return ResponseEntity.ok("School created successfully.");
  }
}
