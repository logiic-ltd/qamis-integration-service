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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import rw.gov.mineduc.qamis.integration.dto.SchoolDTO;
import rw.gov.mineduc.qamis.integration.service.SchoolService;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
public class SchoolControllerTest {

  private MockMvc mockMvc;

  @Mock private SchoolService schoolService;

  @InjectMocks private SchoolController schoolController;

  private ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(schoolController).build();
  }

  @Test
  void testCreateSchool() throws Exception {
    SchoolDTO schoolDTO = new SchoolDTO();
    schoolDTO.setSchoolName("Test School");
    schoolDTO.setProvince("Test Province");
    schoolDTO.setDistrict("Test District");
    schoolDTO.setSector("Test Sector");
    schoolDTO.setCell("Test Cell");
    schoolDTO.setVillage("Test Village");

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
