package rw.gov.mineduc.qamis.integration.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import rw.gov.mineduc.qamis.integration.dto.SchoolDTO;
import rw.gov.mineduc.qamis.integration.service.SchoolService;

@WebMvcTest(SchoolController.class)
public class SchoolControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private SchoolService schoolService;

  @InjectMocks private SchoolController schoolController;

  private ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testCreateSchool() throws Exception {
    SchoolDTO schoolDTO = new SchoolDTO();
    schoolDTO.setSchoolName("Test School");
    schoolDTO.setProvince("Test Province");
    schoolDTO.setDistrict("Test District");
    schoolDTO.setSector("Test Sector");

    doNothing().when(schoolService).saveSchool(any());

    mockMvc
        .perform(
            post("/api/schools/create")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(schoolDTO)))
        .andExpect(status().isOk())
        .andExpect(content().string("School created successfully."));
  }
}
