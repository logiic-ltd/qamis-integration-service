package rw.gov.mineduc.qamis.integration.exception;

public class InspectionSyncException extends RuntimeException {
    public InspectionSyncException(String message) {
        super(message);
    }
    
    public InspectionSyncException(String message, Throwable cause) {
        super(message, cause);
    }
}
