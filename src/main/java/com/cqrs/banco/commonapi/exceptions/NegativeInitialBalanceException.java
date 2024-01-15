package com.cqrs.banco.commonapi.exceptions;

public class NegativeInitialBalanceException extends RuntimeException {
    public NegativeInitialBalanceException(String message) {
        super(message);
    }
}
