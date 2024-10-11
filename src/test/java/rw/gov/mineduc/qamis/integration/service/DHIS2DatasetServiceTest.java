package rw.gov.mineduc.qamis.integration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import rw.gov.mineduc.qamis.integration.config.DHIS2Config;
import rw.gov.mineduc.qamis.integration.model.DHIS2Dataset;
import rw.gov.mineduc.qamis.integration.model.SyncInfo;
import rw.gov.mineduc.qamis.integration.repository.DHIS2DatasetRepository;
import rw.gov.mineduc.qamis.integration.repository.SyncInfoRepository;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DHIS2DatasetServiceTest {

    @Mock
    private DHIS2DatasetRepository dhis2DatasetRepository;

    @Mock
    private SyncInfoRepository syncInfoRepository;

    @Mock
    private DHIS2Config dhis2Config;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private DHIS2DatasetService dhis2DatasetService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSearchDatasets() {
        // Prepare test data
        DHIS2Dataset dataset1 = createDataset("1", "Dataset 1", "DS1", "Monthly", new HashSet<>(Arrays.asList("OU1", "OU2")));
        DHIS2Dataset dataset2 = createDataset("2", "Dataset 2", "DS2", "Yearly", new HashSet<>(Arrays.asList("OU2", "OU3")));
        List<DHIS2Dataset> datasets = Arrays.asList(dataset1, dataset2);
        Page<DHIS2Dataset> datasetPage = new PageImpl<>(datasets);

        // Mock repository behavior
        when(dhis2DatasetRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(datasetPage);

        // Perform search
        Page<DHIS2Dataset> result = dhis2DatasetService.searchDatasets(
                "Dataset",
                "DS",
                "Monthly",
                Arrays.asList("OU1", "OU2"),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(),
                PageRequest.of(0, 10)
        );

        // Verify results
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(dhis2DatasetRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void testSearchDatasetsByOrganisationUnit() {
        // Prepare test data
        DHIS2Dataset dataset1 = createDataset("1", "Dataset 1", "DS1", "Monthly", new HashSet<>(Arrays.asList("OU1", "OU2")));
        DHIS2Dataset dataset2 = createDataset("2", "Dataset 2", "DS2", "Yearly", new HashSet<>(Arrays.asList("OU2", "OU3")));
        List<DHIS2Dataset> datasets = Arrays.asList(dataset1, dataset2);

        // Mock repository behavior
        when(dhis2DatasetRepository.findByOrganisationUnitIdsContaining("OU2")).thenReturn(datasets);

        // Perform search
        List<DHIS2Dataset> result = dhis2DatasetService.searchDatasetsByOrganisationUnit("OU2");

        // Verify results
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(ds -> ds.getOrganisationUnitIds().contains("OU2")));
        verify(dhis2DatasetRepository, times(1)).findByOrganisationUnitIdsContaining("OU2");
    }

    @Test
    void testSynchronizeDatasets() {
        // Mock DHIS2Config
        when(dhis2Config.getApiUrl()).thenReturn("https://play.dhis2.org/2.39.0");
        when(dhis2Config.getUsername()).thenReturn("admin");
        when(dhis2Config.getPassword()).thenReturn("district");

        // Mock RestTemplate response
        Map<String, Object> datasetData = new HashMap<>();
        datasetData.put("id", "dataset1");
        datasetData.put("name", "Dataset 1");
        datasetData.put("shortName", "DS1");
        datasetData.put("periodType", "Monthly");
        datasetData.put("lastUpdated", "2023-05-01T12:00:00.000");
        List<Map<String, String>> orgUnits = new ArrayList<>();
        orgUnits.add(Collections.singletonMap("id", "OU1"));
        datasetData.put("organisationUnits", orgUnits);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("dataSets", Collections.singletonList(datasetData));

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(responseBody, org.springframework.http.HttpStatus.OK));

        // Mock SyncInfoRepository
        when(syncInfoRepository.findById("DHIS2_DATASET_SYNC")).thenReturn(Optional.empty());

        // Perform synchronization
        int syncedCount = dhis2DatasetService.synchronizeDatasets(null, null, false);

        // Verify results
        assertEquals(1, syncedCount);
        verify(dhis2DatasetRepository, times(1)).save(any(DHIS2Dataset.class));
        verify(syncInfoRepository, times(1)).save(any(SyncInfo.class));
    }

    @Test
    void testSynchronizeDatasetsSyncAll() {
        // Similar setup as testSynchronizeDatasets()
        // ...

        int syncedCount = dhis2DatasetService.synchronizeDatasets(null, null, true);

        // Verify results
        // ...
    }

    @Test
    void testSynchronizeDatasetsSingleDataset() {
        // Setup for syncing a single dataset
        // ...

        int syncedCount = dhis2DatasetService.synchronizeDatasets(null, "dataset1", false);

        // Verify results
        // ...
    }

    private DHIS2Dataset createDataset(String id, String name, String shortName, String periodType, HashSet<String> orgUnits) {
        DHIS2Dataset dataset = new DHIS2Dataset();
        dataset.setId(id);
        dataset.setName(name);
        dataset.setShortName(shortName);
        dataset.setPeriodType(periodType);
        dataset.setOrganisationUnitIds(orgUnits);
        dataset.setLastUpdated(LocalDateTime.now());
        return dataset;
    }
}
