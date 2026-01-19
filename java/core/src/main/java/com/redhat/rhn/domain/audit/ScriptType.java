package com.redhat.rhn.domain.audit;

/**
 * Enumeration of supported custom remediation script types.
 */
public enum ScriptType {
    BASH("bash"),
    SALT("salt");

    private final String value;

    ScriptType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Parse a string into a ScriptType.
     * @param value rule string value
     * @return the matching ScriptType or null if not found
     */
    public static ScriptType fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (ScriptType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }
    
    @Override
    public String toString() {
        return value;
    }
}
