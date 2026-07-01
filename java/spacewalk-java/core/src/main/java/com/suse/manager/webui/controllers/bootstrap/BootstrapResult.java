/*
 * Copyright (c) 2022 SUSE LLC
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
package com.suse.manager.webui.controllers.bootstrap;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Representation of the status of bootstrap and possibly error messages.
 */
public class BootstrapResult {

    private final boolean success;
    private final List<BootstrapError> errors;
    private final String contactMethod;

    /**
     * Build a result from the outcome and the error message
     * @param successIn the operation outcome
     * @param errorMessage the error message
     */
    public BootstrapResult(boolean successIn, String errorMessage) {
        this(successIn, null, List.of(new BootstrapError(errorMessage)));
    }

    /**
     * Build a result from the outcome and the errors
     * @param successIn the operation outcome
     * @param errorsIn the list of errors
     */
    public BootstrapResult(boolean successIn, List<BootstrapError> errorsIn) {
        this(successIn, null, errorsIn);
    }

    /**
     * Build a result from the outcome and the errors
     *
     * @param successIn the operation outcome
     * @param contactMethodIn the contact method
     * @param errorMessage the error message
     */
    public BootstrapResult(boolean successIn, String contactMethodIn, String errorMessage) {
        this(successIn, contactMethodIn, List.of(new BootstrapError(errorMessage)));
    }

    /**
     * Build a result from the outcome and the errors
     * @param successIn the operation outcome
     * @param contactMethodIn the contact method
     * @param errorsIn the list of errors
     */
    public BootstrapResult(boolean successIn, String contactMethodIn, List<BootstrapError> errorsIn) {
        this.success = successIn;
        this.contactMethod = contactMethodIn;
        this.errors = errorsIn;
    }



    /**
     * @return success
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * @return messages
     */
    public List<String> getMessages() {
        return errors.stream().map(BootstrapError::getMessage).collect(Collectors.toList());
    }

    /**
     * @return contactMethod
     */
    public Optional<String> getContactMethod() {
        return Optional.ofNullable(contactMethod);
    }

    /**
     * @return bootstrap result converted to a map
     */
    public JsonObject asJson() {
        JsonObject object = new JsonObject();
        JsonArray arr = new JsonArray();
        errors.stream().map(e -> e.asJson()).forEach(arr::add);
        object.add("success", new JsonPrimitive(success));
        object.add("errors", arr);
        return object;
    }
}
