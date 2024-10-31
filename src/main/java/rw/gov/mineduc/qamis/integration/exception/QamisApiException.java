package rw.gov.mineduc.qamis.integration.exception;

public class QamisApiException extends RuntimeException {
    private final int statusCode;

    public QamisApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public QamisApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
