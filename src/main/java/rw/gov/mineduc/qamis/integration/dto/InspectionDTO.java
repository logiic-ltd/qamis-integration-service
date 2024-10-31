package rw.gov.mineduc.qamis.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InspectionDTO {
    private String id;
    private String name;
    private Map<String, Object> additionalProperties;
    
    @JsonProperty("inspection_name")
    private String inspectionName;
    
    @JsonProperty("workflow_state")
    private String workflowState;
    
    // Flexible field to capture any additional fields
    @JsonProperty("custom_fields")
    private Map<String, Object> customFields;
    
    @JsonProperty("teams")
    private List<Map<String, Object>> teams;
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getInspectionName() { return inspectionName; }
    public void setInspectionName(String inspectionName) { this.inspectionName = inspectionName; }
    
    public String getWorkflowState() { return workflowState; }
    public void setWorkflowState(String workflowState) { this.workflowState = workflowState; }
    
    public Map<String, Object> getCustomFields() { return customFields; }
    public void setCustomFields(Map<String, Object> customFields) { this.customFields = customFields; }
    
    public List<Map<String, Object>> getTeams() { return teams; }
    public void setTeams(List<Map<String, Object>> teams) { this.teams = teams; }
    
    public Map<String, Object> getAdditionalProperties() { return additionalProperties; }
    public void setAdditionalProperties(Map<String, Object> additionalProperties) { 
        this.additionalProperties = additionalProperties;
    }
}
