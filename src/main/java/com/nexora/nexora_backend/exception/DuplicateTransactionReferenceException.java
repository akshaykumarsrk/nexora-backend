package com.nexora.nexora_backend.exception;

public class DuplicateTransactionReferenceException
        extends RuntimeException {

    public DuplicateTransactionReferenceException(String message) {
        super(message);
    }
}