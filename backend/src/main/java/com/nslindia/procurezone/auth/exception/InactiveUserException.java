package com.nslindia.procurezone.auth.exception;

public class InactiveUserException extends RuntimeException {

    public InactiveUserException() {
        super("User account is inactive");
    }
}
