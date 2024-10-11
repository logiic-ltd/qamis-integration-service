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
import rw.gov.mineduc.qamis.integration.model.DHIS2Dataset;
import rw.gov.mineduc.qamis.integration.repository.DHIS2DatasetRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DHIS2DatasetServiceTest {

    @Mock
    private DHIS2DatasetRepository dhis2DatasetRepository;

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
