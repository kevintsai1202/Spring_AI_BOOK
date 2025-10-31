package com.example.advancedrag.exception;

/**
 * 內容審核異常
 *
 * 內容審核過程中的異常
 */
public class ModerationException extends RuntimeException {

    private String errorCode;
    private Double riskScore;

    public ModerationException(String message) {
        super(message);
    }

    public ModerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModerationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ModerationException(String errorCode, String message, Double riskScore) {
        super(message);
        this.errorCode = errorCode;
        this.riskScore = riskScore;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Double getRiskScore() {
        return riskScore;
    }
}
