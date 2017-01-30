/**
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
package com.suse.manager.webui.utils.gson;

import java.util.Collections;
import java.util.List;

/**
 * Generic JSON response wrapper class
 * @param <T> Type of the data class which will be included in the response
 */
public class JsonResult<T> {

    private final boolean success;
    private final List<String> messages;
    private final T data;

    /**
     * Instantiates a new Json result.
     *
     * @param successIn the success
     */
    public JsonResult(boolean successIn) {
        this(successIn, (T) null);
    }

    /**
     * Instantiates a new Json result.
     *
     * @param successIn the success
     * @param message   the message
     */
    public JsonResult(boolean successIn, String message) {
        this(successIn, message, null);
    }

    /**
     * Instantiates a new Json result.
     *
     * @param successIn  the success
     * @param messagesIn the messages
     */
    public JsonResult(boolean successIn, List<String> messagesIn) {
        this(successIn, messagesIn, null);
    }

    /**
     * Instantiates a new Json result.
     *
     * @param successIn the success
     * @param dataIn    the data
     */
    public JsonResult(boolean successIn, T dataIn) {
        this(successIn, Collections.emptyList(), dataIn);
    }

    /**
     * Instantiates a new Json result.
     *
     * @param successIn the success
     * @param message   the message
     * @param dataIn    the data
     */
    public JsonResult(boolean successIn, String message, T dataIn) {
        this(successIn, Collections.singletonList(message), dataIn);
    }

    /**
     * Instantiates a new Json result.
     *
     * @param successIn  the success
     * @param messagesIn the messages
     * @param dataIn     the data
     */
    public JsonResult(boolean successIn, List<String> messagesIn, T dataIn) {
        this.success = successIn;
        this.messages = messagesIn;
        this.data = dataIn;
    }

    /**
     * Gets messages.
     *
     * @return the messages
     */
    public List<String> getMessages() {
        return messages;
    }

    /**
     * Is the result success.
     *
     * @return true if success, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Gets the data.
     *
     * @return the data
     */
    public T getData() {
        return data;
    }
}
