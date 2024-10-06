package rw.gov.mineduc.qamis.integration.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import rw.gov.mineduc.qamis.integration.model.School;
import rw.gov.mineduc.qamis.integration.repository.SchoolRepository;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@SpringBootTest
public class FileProcessingServiceTest {

    @Autowired
    private FileProcessingService fileProcessingService;

    @MockBean
    private SchoolRepository schoolRepository;

    @Test
    public void testProcessSchoolFile() throws IOException {
        String filePath = "src/test/resources/sample_schools.xlsx";
        
        when(schoolRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<School> processedSchools = fileProcessingService.processSchoolFileForTesting(filePath);

        assertNotNull(processedSchools);
        assertEquals(20, processedSchools.size()); // The sample file has 20 schools

        // Verify the content of the first school
        School firstSchool = processedSchools.get(0);
        assertNotNull(firstSchool);
        assertNotNull(firstSchool.getSchoolCode());
        assertNotNull(firstSchool.getSchoolName());
        assertNotNull(firstSchool.getProvince());
        assertNotNull(firstSchool.getDistrict());
        assertNotNull(firstSchool.getSector());
        assertNotNull(firstSchool.getCell());
        assertNotNull(firstSchool.getVillage());
        assertNotNull(firstSchool.getSchoolStatus());
        assertNotNull(firstSchool.getSchoolOwner());
        assertNotNull(firstSchool.getLatitude());
        assertNotNull(firstSchool.getLongitude());
        assertNotNull(firstSchool.getDay());
        assertNotNull(firstSchool.getBoarding());

        // Verify that all schools have been processed
        for (School school : processedSchools) {
            assertNotNull(school.getSchoolCode());
            assertNotNull(school.getSchoolName());
            // Add more assertions as needed
        }

        // Verify some specific values
        assertEquals("PRIVATE", processedSchools.get(0).getSchoolStatus());
        assertEquals("PARENTS", processedSchools.get(0).getSchoolOwner());
        assertEquals("DAY", processedSchools.get(0).getDay());
        assertNull(processedSchools.get(0).getBoarding());

        assertEquals("PUBLIC", processedSchools.get(1).getSchoolStatus());
        assertEquals("GoR", processedSchools.get(1).getSchoolOwner());
        assertEquals("DAY", processedSchools.get(1).getDay());
        assertEquals("BOARDING", processedSchools.get(1).getBoarding());

        assertEquals("PRIVATE", processedSchools.get(2).getSchoolStatus());
        assertEquals("OTHERS", processedSchools.get(2).getSchoolOwner());
        assertNull(processedSchools.get(2).getDay());
        assertEquals("BOARDING", processedSchools.get(2).getBoarding());
    }
}
