package rw.gov.mineduc.qamis.integration.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import rw.gov.mineduc.qamis.integration.config.QamisConfig;
import rw.gov.mineduc.qamis.integration.dto.InspectionDTO;
import rw.gov.mineduc.qamis.integration.exception.InspectionSyncException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class QamisIntegrationService {
    private static final Logger log = LoggerFactory.getLogger(QamisIntegrationService.class);

    @Autowired
    private QamisConfig qamisConfig;

    @Autowired
    private RestTemplate restTemplate;

    public List<InspectionDTO> fetchApprovedInspections() {
        try {
            String url = qamisConfig.getApiUrl() + "/api/resource/Inspection?filters=[[\"workflow_state\", \"=\", \"Approved by DG\"]]";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new QamisApiException("QAMIS API returned error status: " + response.getStatusCode(), 
                    response.getStatusCode().value());
            }
            
            if (response.getBody() == null) {
                throw new QamisApiException("QAMIS API returned null response body", 500);
            }
            
            if (!response.getBody().containsKey("data")) {
                log.warn("No data field in QAMIS API response");
                return Collections.emptyList();
            }

            List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");
            if (data == null) {
                log.warn("Data field is null in QAMIS API response");
                return Collections.emptyList();
            }

            return data.stream()
                      .filter(item -> item != null && item.containsKey("name"))
                      .map(item -> {
                          InspectionDTO dto = new InspectionDTO();
                          dto.setId((String) item.get("name"));
                          dto.setName((String) item.get("name"));
                          return dto;
                      })
                      .toList();

        } catch (QamisApiException e) {
            log.error("QAMIS API error: {} (Status: {})", e.getMessage(), e.getStatusCode());
            throw e;
        } catch (Exception e) {
            log.error("Error fetching approved inspections from QAMIS: {}", e.getMessage(), e);
            throw new QamisApiException("Failed to fetch approved inspections", e);
        }
    }

    public InspectionDTO fetchInspectionDetails(String inspectionId) {
        if (inspectionId == null || inspectionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Inspection ID cannot be null or empty");
        }

        try {
            String url = qamisConfig.getApiUrl() + "/api/resource/Inspection/" + inspectionId;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new QamisApiException("QAMIS API returned error status: " + response.getStatusCode(), 
                    response.getStatusCode().value());
            }
            
            if (response.getBody() == null) {
                throw new QamisApiException("QAMIS API returned null response body for inspection: " + inspectionId, 500);
            }
            
            if (!response.getBody().containsKey("data")) {
                throw new QamisApiException("No data field in QAMIS API response for inspection: " + inspectionId, 500);
            }

            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            if (data == null) {
                throw new QamisApiException("Data field is null in QAMIS API response for inspection: " + inspectionId, 500);
            }

            InspectionDTO dto = new InspectionDTO();
            dto.setId((String) data.get("name"));
            dto.setName((String) data.get("name"));
            dto.setInspectionName((String) data.get("inspection_name"));
            dto.setWorkflowState((String) data.get("workflow_state"));
            dto.setCustomFields(data);

            validateInspectionDTO(dto, inspectionId);
            return dto;

        } catch (QamisApiException e) {
            log.error("QAMIS API error for inspection {}: {} (Status: {})", 
                     inspectionId, e.getMessage(), e.getStatusCode());
            throw e;
        } catch (Exception e) {
            log.error("Error fetching inspection details from QAMIS for {}: {}", inspectionId, e.getMessage(), e);
            throw new QamisApiException("Failed to fetch inspection details: " + inspectionId, e);
        }
    }

    private void validateInspectionDTO(InspectionDTO dto, String inspectionId) {
        if (dto.getId() == null || dto.getId().trim().isEmpty()) {
            throw new QamisApiException("Invalid inspection data: missing ID for inspection: " + inspectionId, 500);
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new QamisApiException("Invalid inspection data: missing name for inspection: " + inspectionId, 500);
        }
        if (dto.getWorkflowState() == null || dto.getWorkflowState().trim().isEmpty()) {
            throw new QamisApiException("Invalid inspection data: missing workflow state for inspection: " + inspectionId, 500);
        }
    }

    private InspectionDTO mapToInspectionDTO(Map<String, Object> data) {
        InspectionDTO dto = new InspectionDTO();
        dto.setId((String) data.get("name")); // In QAMIS API, 'name' is used as ID
        dto.setName((String) data.get("name"));
        dto.setInspectionName((String) data.get("inspection_name"));
        dto.setWorkflowState((String) data.get("workflow_state"));
        dto.setCustomFields(data);
        return dto;
    }
}
