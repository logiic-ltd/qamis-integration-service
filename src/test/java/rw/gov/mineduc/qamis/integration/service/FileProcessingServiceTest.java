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
        assertEquals(5, processedSchools.size()); // Assuming the sample file has 5 schools

        // Add more assertions to verify the content of processed schools
    }
}
