package com.suse.oval.exceptions;

public class OvalParserException extends RuntimeException {
    public OvalParserException() {
        super();
    }

    public OvalParserException(String message) {
        super(message);
    }

    public OvalParserException(Throwable cause) {
        super(cause);
    }

    public OvalParserException(String message, Throwable cause) {
        super(message, cause);
    }
}
