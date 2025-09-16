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

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Bootstrapping error happened during the execution of a salt command.
 */
public class SaltBootstrapError extends BootstrapError {

    private final String standardOutput;

    private final String standardError;

    private final String result;

    /**
     * @param messageIn the error message
     * @param standardOutputIn the messages on the standard output
     * @param standardErrorIn the messages on the standard error
     * @param resultIn the result as returned by the process
     */
    public SaltBootstrapError(String messageIn, String standardOutputIn, String standardErrorIn, String resultIn) {
        super(messageIn);
        this.standardOutput = standardOutputIn;
        this.standardError = standardErrorIn;
        this.result = resultIn;
    }

    public String getStandardOutput() {
        return standardOutput;
    }

    public String getStandardError() {
        return standardError;
    }

    public String getResult() {
        return result;
    }

    @Override
    public JsonObject asJson() {
        JsonObject object = new JsonObject();

        object.add("message", new JsonPrimitive(getMessage()));
        if (standardOutput != null) {
            object.add("standardOutput", new JsonPrimitive(standardOutput));
        }
        if (standardError != null) {
            object.add("standardError", new JsonPrimitive(standardError));
        }
        if (result != null) {
            object.add("result", new JsonPrimitive(result));
        }
        return object;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(getMessage());

        if (standardOutput != null) {
            sb.append("\n\nStandard output:\n").append(standardOutput);
        }

        if (standardError != null) {
            sb.append("\n\nStandard error:\n").append(standardError);
        }

        if (result != null) {
            sb.append("\n\nResult:\n").append(result);
        }

        return sb.toString();
    }
}
