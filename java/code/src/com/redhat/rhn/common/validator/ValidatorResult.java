/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.common.validator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  The <code>ValidatorResult</code> class is a container intended to be used
 *  by validation methods that return both errors and warnings.
 * </p>
 */
public class ValidatorResult {
    private List<ValidatorError> validationErrors = new LinkedList<>();
    private List<ValidatorWarning> validationWarnings = new LinkedList<>();
    private Map<String, List<ValidatorError>> fieldValidationErrors = new HashMap<>();

    /**
     * Add a ValidatorError to the list of errors.
     *
     * @param error ValidatorError to be added.
     */
    public void addError(ValidatorError error) {
        validationErrors.add(error);
    }

    /**
     * Add a ValidatorError to the list of errors.
     *
     * @param key the message key of the error
     * @param args the args that go with the error message.
     *
     */
    public void addError(String key, Object... args) {
        addError(new ValidatorError(key, args));
    }

    /**
     * Add a ValidatorError to the list of field errors.
     *
     * @param field Field where the ValidatorError should be added.
     * @param error ValidatorError to be added.
     */
    public void addFieldError(String field, ValidatorError error) {
        fieldValidationErrors.putIfAbsent(field, new LinkedList<>());
        fieldValidationErrors.get(field).add(error);
    }

    /**
     * Add a ValidatorError to the list of field errors.
     *
     * @param field Field associated with the ValidatorError.
     * @param key the message key of the error
     * @param args the args that go with the error message.
     *
     */
    public void addFieldError(String field, String key, Object... args) {
        addFieldError(field, new ValidatorError(key, args));
    }

    /**
     * Add a ValidatorWarning to the list of warnings.
     *
     * @param warning ValidatorWarning to be added.
     */
    public void addWarning(ValidatorWarning warning) {
        validationWarnings.add(warning);
    }

    /**
     * Add a ValidatorWarning to the list of warnings.
     * @param key the message key of the warning
     * @param args the args that go with the warning.
     */
    public void addWarning(String key, Object... args) {
        addWarning(new ValidatorWarning(key, args));
    }


    /**
     * Retrieve the list of ValidatorErrors.
     *
     * @return List of ValidatorError objects.
     */
    public List<ValidatorError> getErrors() {
        return validationErrors;
    }

    /**
     * Retrieve the list of ValidatorErrors.
     *
     * @return List of ValidatorError objects.
     */
    public Map<String, List<ValidatorError>> getFieldErrors() {
        return fieldValidationErrors;
    }

    /**
     * Retrieve the list of ValidatorWarnings.
     *
     * @return List of ValidatorWarning objects.
     */
    public List<ValidatorWarning> getWarnings() {
        return validationWarnings;
    }

    /**
     * Appends the results of the passed in ValidatorResult
     * to this result
     * @param result the results to append.
     */
    public void append(ValidatorResult result) {
        getErrors().addAll(result.getErrors());
        getWarnings().addAll(result.getWarnings());
        result.getFieldErrors().forEach((field, fieldErrors) -> {
            fieldValidationErrors.putIfAbsent(field, new LinkedList<>());
            fieldValidationErrors.get(field).addAll(fieldErrors);
        });
    }

    /**
     * @return the error message
     */
    public String getMessage() {
        StringBuilder str = new StringBuilder();
        if (!validationErrors.isEmpty()) {
            str.append("ERRORS:\n");
            for (ValidatorError error : validationErrors) {
                str.append(error.getMessage()).append("\n");
            }

        }
        if (!fieldValidationErrors.isEmpty()) {
            str.append("FIELD_ERRORS:\n");
            fieldValidationErrors.forEach((field, fieldErrors) -> fieldErrors.forEach(fieldError ->
                    str.append(field).append(" - ").append(fieldError.getMessage()).append("\n")
            ));
        }
        if (!validationWarnings.isEmpty()) {
            str.append("WARNINGS:\n");
            for (ValidatorWarning warning : validationWarnings) {
                str.append(warning.getMessage()).append("\n");
            }
        }
        return str.toString();
    }

    /**
     *
     * @return true if there are no errors or warnings..
     */
    public boolean isEmpty() {
        return getWarnings().isEmpty() && !hasErrors();
    }

    /**
     * @return true if errors exist in this result.
     */
    public boolean hasErrors() {
        return !getErrors().isEmpty() || !getFieldErrors().isEmpty();
    }
}
