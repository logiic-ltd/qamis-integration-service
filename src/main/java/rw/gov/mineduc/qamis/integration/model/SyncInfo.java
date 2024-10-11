package rw.gov.mineduc.qamis.integration.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class SyncInfo {

    @Id
    private String id;

    private LocalDateTime lastSyncTime;

    public SyncInfo() {
    }

    public SyncInfo(String id, LocalDateTime lastSyncTime) {
        this.id = id;
        this.lastSyncTime = lastSyncTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getLastSyncTime() {
        return lastSyncTime;
    }

    public void setLastSyncTime(LocalDateTime lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }
}
