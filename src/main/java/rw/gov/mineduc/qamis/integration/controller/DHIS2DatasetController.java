package rw.gov.mineduc.qamis.integration.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.gov.mineduc.qamis.integration.model.DHIS2Dataset;
import rw.gov.mineduc.qamis.integration.service.DHIS2DatasetService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/dhis2datasets")
public class DHIS2DatasetController {

    @Autowired
    private DHIS2DatasetService dhis2DatasetService;

    @GetMapping("/search")
    public ResponseEntity<Page<DHIS2Dataset>> searchDatasets(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String shortName,
            @RequestParam(required = false) String periodType,
            @RequestParam(required = false) List<String> organisationUnitIds,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastUpdatedStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastUpdatedEnd,
            Pageable pageable) {

        return ResponseEntity.ok(dhis2DatasetService.searchDatasets(
                name, shortName, periodType, organisationUnitIds, lastUpdatedStart, lastUpdatedEnd, pageable
        ));
    }

    /**
     * Search for DHIS2 datasets by name.
     *
     * This endpoint allows searching for DHIS2 datasets by their name or short name.
     *
     * @param name Name or short name to search for (partial match, case-insensitive)
     * @param pageable Pagination information (page, size, sort)
     * @return A page of DHIS2Dataset objects matching the search criteria
     *
     * Example usage:
     * GET /api/dhis2datasets/search/name?name=monthly&page=0&size=20&sort=name,asc
     */
    @GetMapping("/search/name")
    public ResponseEntity<Page<DHIS2Dataset>> searchDatasetsByName(
            @RequestParam String name,
            Pageable pageable) {
        return ResponseEntity.ok(dhis2DatasetService.searchDatasetsFlexible(name, pageable));
    }

    /**
     * Search for DHIS2 datasets by name (flexible search).
     *
     * This endpoint allows searching for DHIS2 datasets by their name, short name, or any partial match.
     *
     * @param name Name to search for (partial match, case-insensitive)
     * @param pageable Pagination information (page, size, sort)
     * @return A page of DHIS2Dataset objects matching the search criteria
     *
     * Example usage:
     * GET /api/dhis2datasets/search/flexible?name=school&page=0&size=20&sort=name,asc
     */
    @GetMapping("/search/flexible")
    public ResponseEntity<Page<DHIS2Dataset>> searchDatasetsFlexible(
            @RequestParam String name,
            Pageable pageable) {
        return ResponseEntity.ok(dhis2DatasetService.searchDatasetsFlexible(name, pageable));
    }

    @GetMapping
    public ResponseEntity<Page<DHIS2Dataset>> getAllDatasets(Pageable pageable) {
        return ResponseEntity.ok(dhis2DatasetService.getAllDatasets(pageable));
    }

    @GetMapping("/search/by-org-unit/{orgUnitId}")
    public ResponseEntity<List<DHIS2Dataset>> searchDatasetsByOrgUnit(@PathVariable String orgUnitId) {
        return ResponseEntity.ok(dhis2DatasetService.searchDatasetsByOrganisationUnit(orgUnitId));
    }

    @PostMapping("/sync")
    public ResponseEntity<String> syncDatasets(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) String datasetId,
            @RequestParam(required = false, defaultValue = "false") boolean syncAll) {
        int syncedCount = dhis2DatasetService.synchronizeDatasets(fromDate, datasetId, syncAll);
        return ResponseEntity.ok("Synchronized " + syncedCount + " datasets");
    }

    @PostMapping("/sync/{datasetId}")
    public ResponseEntity<String> syncSingleDataset(@PathVariable String datasetId) {
        int syncedCount = dhis2DatasetService.synchronizeDatasets(null, datasetId, false);
        return ResponseEntity.ok("Synchronized " + syncedCount + " dataset");
    }

    @PostMapping("/sync/all")
    public ResponseEntity<String> syncAllDatasets() {
        int syncedCount = dhis2DatasetService.synchronizeDatasets(null, null, true);
        return ResponseEntity.ok("Synchronized " + syncedCount + " datasets");
    }
}
