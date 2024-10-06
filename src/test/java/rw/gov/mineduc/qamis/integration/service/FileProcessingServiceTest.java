package rw.gov.mineduc.qamis.integration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import rw.gov.mineduc.qamis.integration.model.School;
import rw.gov.mineduc.qamis.integration.repository.SchoolRepository;

import com.opencsv.exceptions.CsvException;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@SpringBootTest
class FileProcessingServiceTest {

    @Autowired
    private FileProcessingService fileProcessingService;

    @MockBean
    private SchoolRepository schoolRepository;

    private List<School> processedSchools;

    @BeforeEach
    void setUp() throws IOException, CsvException {
        String filePath = "src/test/resources/sample_schools.csv";
        when(schoolRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        processedSchools = fileProcessingService.processSchoolFileForTesting(filePath);
    }

    @Test
    void testProcessSchoolFile() {
        assertNotNull(processedSchools);
        assertEquals(20, processedSchools.size());
    }

    @Test
    void testSchoolProperties() {
        School firstSchool = processedSchools.get(0);
        assertAllPropertiesNotNull(firstSchool);
    }

    @Test
    void testAllSchoolsProcessed() {
        processedSchools.forEach(this::assertAllPropertiesNotNull);
    }

    @Test
    void testSpecificSchoolValues() {
        School school0 = processedSchools.get(0);
        assertEquals("PRIVATE", school0.getSchoolStatus());
        assertEquals("PARENTS", school0.getSchoolOwner());
        assertEquals("DAY", school0.getDay());
        assertNull(school0.getBoarding());

        School school1 = processedSchools.get(1);
        assertEquals("PUBLIC", school1.getSchoolStatus());
        assertEquals("GoR", school1.getSchoolOwner());
        assertEquals("DAY", school1.getDay());
        assertEquals("BOARDING", school1.getBoarding());

        School school2 = processedSchools.get(2);
        assertEquals("PRIVATE", school2.getSchoolStatus());
        assertEquals("OTHERS", school2.getSchoolOwner());
        assertNull(school2.getDay());
        assertEquals("BOARDING", school2.getBoarding());
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
        // Day and Boarding can be null, so we don't assert them here
    }
}
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
        assertEquals(19, processedSchools.size());

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
