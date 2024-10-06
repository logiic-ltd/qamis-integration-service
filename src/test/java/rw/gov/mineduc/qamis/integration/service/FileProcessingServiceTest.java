package rw.gov.mineduc.qamis.integration.service;

import com.opencsv.exceptions.CsvException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import rw.gov.mineduc.qamis.integration.model.School;
import rw.gov.mineduc.qamis.integration.repository.SchoolRepository;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

class FileProcessingServiceTest {

    @InjectMocks
    private FileProcessingService fileProcessingService;

    @Mock
    private SchoolRepository schoolRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProcessSchoolFile() throws IOException, CsvException {
        String testFilePath = "src/test/resources/sample_schools.csv";
        when(schoolRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<School> processedSchools = fileProcessingService.processSchoolFileForTesting(testFilePath);

        assertNotNull(processedSchools);
        assertFalse(processedSchools.isEmpty());
        assertEquals(20, processedSchools.size());

        // Verify the content of the first school
        School firstSchool = processedSchools.get(0);
        assertAllPropertiesNotNull(firstSchool);
        assertEquals(110101, firstSchool.getSchoolCode());
        assertEquals("CYAPEPE PRIMARY SCHOOL", firstSchool.getSchoolName());
        assertEquals("Kigali City", firstSchool.getProvince());
        assertEquals("Nyarugenge", firstSchool.getDistrict());
        assertEquals("Gitega", firstSchool.getSector());
        assertEquals("Akabahizi", firstSchool.getCell());
        assertEquals("Iterambere", firstSchool.getVillage());
        assertEquals("PRIVATE", firstSchool.getSchoolStatus());
        assertEquals("PARENTS", firstSchool.getSchoolOwner());
        assertEquals(-1.94486, firstSchool.getLatitude());
        assertEquals(30.05216, firstSchool.getLongitude());
        assertEquals("DAY", firstSchool.getDay());
        assertNull(firstSchool.getBoarding());
    }

    private void assertAllPropertiesNotNull(School school) {
        assertNotNull(school.getSchoolCode());
        assertNotNull(school.getSchoolName());
        assertNotNull(school.getProvince());
        assertNotNull(school.getDistrict());
        assertNotNull(school.getSector());
        assertNotNull(school.getCell());
        assertNotNull(school.getVillage());
        assertNotNull(school.getSchoolStatus());
        assertNotNull(school.getSchoolOwner());
        assertNotNull(school.getLatitude());
        assertNotNull(school.getLongitude());
        // Day and Boarding can be null, so we don't assert them
    }
}
