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

import com.redhat.rhn.common.localization.LocalizationService;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Objects;

/**
 * Simple error message happened during bootstrapping.
 */
public class BootstrapError {

    private static final String DEFAULT_ERROR_MSG = LocalizationService.getInstance()
                                                                       .getMessage("bootstrap.minion.error");

    private final String message;

    /**
     * Build a simple error message
     *
     * @param messageIn the error message
     */
    public BootstrapError(String messageIn) {
        this.message = Objects.requireNonNullElse(messageIn, DEFAULT_ERROR_MSG);
    }

    /**
     * Returns the error message describing this bootstrapping error.
     *
     * @return the error message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Converts this error to a map
     * @return a map describing the error
     */
    public JsonObject asJson() {
        JsonObject object = new JsonObject();
        object.add("message", new JsonPrimitive(getMessage()));
        return object;
    }

    @Override
    public String toString() {
        return message;
    }
}
