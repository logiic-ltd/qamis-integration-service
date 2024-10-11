package rw.gov.mineduc.qamis.integration.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import rw.gov.mineduc.qamis.integration.model.DHIS2Dataset;
import rw.gov.mineduc.qamis.integration.service.DHIS2DatasetService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DHIS2DatasetControllerTest {

    @Mock
    private DHIS2DatasetService dhis2DatasetService;

    @InjectMocks
    private DHIS2DatasetController dhis2DatasetController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSearchDatasets() {
        DHIS2Dataset dataset1 = new DHIS2Dataset();
        dataset1.setId("1");
        dataset1.setName("Dataset 1");

        DHIS2Dataset dataset2 = new DHIS2Dataset();
        dataset2.setId("2");
        dataset2.setName("Dataset 2");

        List<DHIS2Dataset> datasets = Arrays.asList(dataset1, dataset2);
        Page<DHIS2Dataset> page = new PageImpl<>(datasets);

        when(dhis2DatasetService.searchDatasets(
                anyString(), anyString(), anyString(), anyList(),
                any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(page);

        ResponseEntity<Page<DHIS2Dataset>> response = dhis2DatasetController.searchDatasets(
                "Dataset",
                "DS",
                "Monthly",
                Collections.singletonList("OU1"),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(),
                Pageable.unpaged()
        );

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().getTotalElements());
    }

    @Test
    void testSearchDatasetsByOrgUnit() {
        DHIS2Dataset dataset = new DHIS2Dataset();
        dataset.setId("1");
        dataset.setName("Dataset 1");

        List<DHIS2Dataset> datasets = Collections.singletonList(dataset);

        when(dhis2DatasetService.searchDatasetsByOrganisationUnit(anyString()))
                .thenReturn(datasets);

        ResponseEntity<List<DHIS2Dataset>> response = dhis2DatasetController.searchDatasetsByOrgUnit("OU1");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals("Dataset 1", response.getBody().get(0).getName());
    }

    @Test
    void testSyncDatasets() {
        ResponseEntity<Void> response = dhis2DatasetController.syncDatasets();

        assertEquals(204, response.getStatusCodeValue());
        verify(dhis2DatasetService, times(1)).synchronizeDatasets();
    }
}