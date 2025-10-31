package com.example.rag.basic.exception;

/**
 * RAG 系統異常
 */
public class RAGException extends RuntimeException {

    public RAGException(String message) {
        super(message);
    }

    public RAGException(String message, Throwable cause) {
        super(message, cause);
    }

}
