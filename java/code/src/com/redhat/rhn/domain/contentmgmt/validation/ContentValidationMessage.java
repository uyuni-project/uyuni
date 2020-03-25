package com.redhat.rhn.domain.contentmgmt.validation;

/**
 * A content project validation message of a specific type
 *
 * The type can be one of TYPE_INFO, TYPE_WARN, TYPE_ERROR.
 */
public class ContentValidationMessage {

    public static final String INFO = "info";
    public static final String WARN = "warning";
    public static final String ERROR = "error";

    private String message;
    private String type;

    private ContentValidationMessage(String messageIn, String typeIn) {
        this.message = messageIn;
        this.type = typeIn;
    }

    public static ContentValidationMessage info(String message) {
        return new ContentValidationMessage(message, INFO);
    }

    public static ContentValidationMessage warn(String message) {
        return new ContentValidationMessage(message, WARN);
    }

    public static ContentValidationMessage error(String message) {
        return new ContentValidationMessage(message, ERROR);
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }
}
