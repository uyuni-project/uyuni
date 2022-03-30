/*
 * Copyright (c) 2017 SUSE LLC
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
package com.suse.manager.api;

/**
 * HTTP API JSON response wrapper class
 */
public class HttpApiResponse {

    private final boolean success;
    private final String message;
    private final Object result;

    /**
     * Creates an error result with the given message
     *
     * @param messageIn the response message
     * @return the response instance
     */
    public static HttpApiResponse error(String messageIn) {
        return new HttpApiResponse(false, messageIn, null);
    }

    /**
     * Creates a success result with the given data
     *
     * @param dataIn the data
     * @return a ResultJson
     */
    public static HttpApiResponse success(Object dataIn) {
        return new HttpApiResponse(true, null, dataIn);
    }

    /**
     * Constructs a new json result
     *
     * @param successIn true if the operation was successful
     * @param messageIn the response message
     * @param resultIn the response data data
     */
    public HttpApiResponse(boolean successIn, String messageIn, Object resultIn) {
        this.success = successIn;
        this.message = messageIn;
        this.result = resultIn;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return success;
    }

    public Object getResult() {
        return result;
    }
}
