package com.example.etl.exception;

/**
 * ETL Pipeline 異常
 */
public class EtlPipelineException extends RuntimeException {

    public EtlPipelineException(String message) {
        super(message);
    }

    public EtlPipelineException(String message, Throwable cause) {
        super(message, cause);
    }
}
