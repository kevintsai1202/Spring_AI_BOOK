package com.example.advancedrag.exception;

/**
 * 評估測試異常
 *
 * 評估測試過程中的異常
 */
public class EvaluationException extends RuntimeException {

    private String errorCode;
    private String testCaseId;

    public EvaluationException(String message) {
        super(message);
    }

    public EvaluationException(String message, Throwable cause) {
        super(message, cause);
    }

    public EvaluationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public EvaluationException(String errorCode, String message, String testCaseId) {
        super(message);
        this.errorCode = errorCode;
        this.testCaseId = testCaseId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getTestCaseId() {
        return testCaseId;
    }
}
