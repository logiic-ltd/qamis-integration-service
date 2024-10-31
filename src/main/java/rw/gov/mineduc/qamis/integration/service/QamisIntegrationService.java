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
            
            if (response.getBody() == null || !response.getBody().containsKey("data")) {
                log.warn("No data returned from QAMIS API for approved inspections");
                return Collections.emptyList();
            }

            List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");
            return data.stream()
                      .map(this::mapToInspectionDTO)
                      .toList();

        } catch (Exception e) {
            log.error("Error fetching approved inspections from QAMIS: {}", e.getMessage());
            throw new InspectionSyncException("Failed to fetch approved inspections", e);
        }
    }

    public InspectionDTO fetchInspectionDetails(String inspectionId) {
        try {
            String url = qamisConfig.getApiUrl() + "/api/resource/Inspection/" + inspectionId;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getBody() == null || !response.getBody().containsKey("data")) {
                log.warn("No data returned from QAMIS API for inspection: {}", inspectionId);
                return null;
            }

            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            return mapToInspectionDTO(data);

        } catch (Exception e) {
            log.error("Error fetching inspection details from QAMIS for {}: {}", inspectionId, e.getMessage());
            throw new InspectionSyncException("Failed to fetch inspection details: " + inspectionId, e);
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
