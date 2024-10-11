package rw.gov.mineduc.qamis.integration.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import rw.gov.mineduc.qamis.integration.config.DHIS2Config;
import rw.gov.mineduc.qamis.integration.model.DHIS2Dataset;
import rw.gov.mineduc.qamis.integration.model.SyncInfo;
import rw.gov.mineduc.qamis.integration.repository.DHIS2DatasetRepository;
import rw.gov.mineduc.qamis.integration.repository.SyncInfoRepository;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DHIS2DatasetService {

    private static final String SYNC_INFO_ID = "DHIS2_DATASET_SYNC";

    @Autowired
    private DHIS2DatasetRepository dhis2DatasetRepository;

    @Autowired
    private SyncInfoRepository syncInfoRepository;

    @Autowired
    private DHIS2Config dhis2Config;

    @Autowired
    private RestTemplate restTemplate;

    public Page<DHIS2Dataset> searchDatasets(
            String name,
            String shortName,
            String periodType,
            List<String> organisationUnitIds,
            LocalDateTime lastUpdatedStart,
            LocalDateTime lastUpdatedEnd,
            Pageable pageable) {

        Specification<DHIS2Dataset> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (shortName != null && !shortName.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("shortName")), "%" + shortName.toLowerCase() + "%"));
            }
            if (periodType != null && !periodType.isEmpty()) {
                predicates.add(cb.equal(root.get("periodType"), periodType));
            }
            if (organisationUnitIds != null && !organisationUnitIds.isEmpty()) {
                predicates.add(root.join("organisationUnitIds").in(organisationUnitIds));
            }
            if (lastUpdatedStart != null && lastUpdatedEnd != null) {
                predicates.add(cb.between(root.get("lastUpdated"), lastUpdatedStart, lastUpdatedEnd));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return dhis2DatasetRepository.findAll(spec, pageable);
    }

    public void synchronizeDatasets() {
        LocalDateTime lastSyncTime = getLastSyncTime();
        String url = dhis2Config.getApiUrl() + "/api/dataSets.json?fields=id,name,shortName,periodType,organisationUnits[id],lastUpdated&paging=false";
        
        if (lastSyncTime != null) {
            url += "&filter=lastUpdated:gte:" + lastSyncTime.format(DateTimeFormatter.ISO_DATE_TIME);
        }

        HttpHeaders headers = createAuthHeaders();
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        List<Map<String, Object>> datasets = (List<Map<String, Object>>) response.getBody().get("dataSets");

        for (Map<String, Object> datasetData : datasets) {
            DHIS2Dataset dataset = mapDatasetData(datasetData);
            dhis2DatasetRepository.save(dataset);
        }

        updateLastSyncTime(LocalDateTime.now());
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String auth = dhis2Config.getUsername() + ":" + dhis2Config.getPassword();
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
        headers.set("Authorization", "Basic " + new String(encodedAuth));
        return headers;
    }

    private LocalDateTime getLastSyncTime() {
        return syncInfoRepository.findById(SYNC_INFO_ID)
                .map(SyncInfo::getLastSyncTime)
                .orElse(null);
    }

    private void updateLastSyncTime(LocalDateTime time) {
        SyncInfo syncInfo = syncInfoRepository.findById(SYNC_INFO_ID)
                .orElse(new SyncInfo(SYNC_INFO_ID, time));
        syncInfo.setLastSyncTime(time);
        syncInfoRepository.save(syncInfo);
    }

    private DHIS2Dataset mapDatasetData(Map<String, Object> datasetData) {
        DHIS2Dataset dataset = new DHIS2Dataset();
        dataset.setId((String) datasetData.get("id"));
        dataset.setName((String) datasetData.get("name"));
        dataset.setShortName((String) datasetData.get("shortName"));
        dataset.setPeriodType((String) datasetData.get("periodType"));
        dataset.setLastUpdated(LocalDateTime.parse((String) datasetData.get("lastUpdated"), DateTimeFormatter.ISO_DATE_TIME));

        List<Map<String, String>> orgUnits = (List<Map<String, String>>) datasetData.get("organisationUnits");
        if (orgUnits != null) {
            dataset.setOrganisationUnitIds(new HashSet<>(orgUnits.stream().map(ou -> ou.get("id")).collect(Collectors.toSet())));
        }

        return dataset;
    }
}
