package com.suse.manager.saltboot;

public class SaltbootException extends RuntimeException {

    /**
     * @param messageIn exception message
     */
    public SaltbootException(String messageIn) {
        super(messageIn);
    }

    /**
     * @param causeIn cause
     */
    public SaltbootException(Throwable causeIn) {
        super(causeIn);
    }

    /**
     * @param messageIn exception message
     * @param causeIn cause
     */
    public SaltbootException(String messageIn, Throwable causeIn) {
        super(messageIn, causeIn);
    }
}
