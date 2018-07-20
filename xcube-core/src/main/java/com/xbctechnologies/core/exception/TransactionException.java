package com.xbctechnologies.core.exception;

public class TransactionException extends Exception {
    public TransactionException(Throwable e) {
        super(e);
    }

    public TransactionException(String msg) {
        super(msg);
    }
}
