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
package com.suse.manager.webui.utils.gson;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Generic JSON response wrapper class
 * @param <T> Type of the data class which will be included in the response
 */
public class ResultJson<T> {

    private final boolean success;
    private final List<String> messages;
    private final Map<String, List<String>> errors;
    private final T data;

    /**
     * Create an error result with the given messages.
     *
     * @param messagesIn a list of messages
     * @param <T> the type of data
     * @return a ResultJson
     */
    public static <T> ResultJson<T> error(String... messagesIn) {
        return error(Arrays.asList(messagesIn));
    }

    /**
     * Create an error result with the given messages.
     *
     * @param messagesIn a list of messages
     * @param <T> the type of data
     * @return a ResultJson
     */
    public static <T> ResultJson<T> error(List<String> messagesIn) {
        return error(messagesIn, null);
    }

    /**
     * Create an error result with the given messages.
     *
     * @param messagesIn a list of messages
     * @param errorsIn a map of field level errors
     * @param <T> the type of data
     * @return a ResultJson
     */
    public static <T> ResultJson<T> error(List<String> messagesIn, Map<String, List<String>> errorsIn) {
        return new ResultJson<>(false, messagesIn, errorsIn, null);
    }

    /**
     * Create an error result with the given messages.
     *
     * @param messagesIn a list of messages
     * @param dataIn the data
     * @param errorsIn a map of field level errors
     * @param <T> the type of data
     * @return a ResultJson
     */
    public static <T> ResultJson<T> error(List<String> messagesIn, Map<String, List<String>> errorsIn, T dataIn) {
        return new ResultJson<>(false, messagesIn, errorsIn, dataIn);
    }

    /**
     * Create a success result without data.
     * @param <T> the type of data
     * @return a ResultJson
     */
    public static <T> ResultJson<T> success() {
        return success(null);
    }

    /**
     * Create a success result with the given data.
     *
     * @param dataIn the data
     * @param <T> the type of data
     * @return a ResultJson
     */
    public static <T> ResultJson<T> success(T dataIn) {
        return new ResultJson<>(true, null, null, dataIn);
    }

    /**
     * Create a success result with the given message.
     *
     * @param messagesIn the messages
     * @param <T> the type of data
     * @return a ResultJson
     */
    public static <T> ResultJson<T> successMessage(String... messagesIn) {
        return new ResultJson<>(true, Arrays.asList(messagesIn), null, null);
    }

    /**
     * Create a success result with the given data.
     *
     * @param dataIn the data
     * @param messagesIn the messages
     * @param <T> the type of data
     * @return a ResultJson
     */
    public static <T> ResultJson<T> success(T dataIn, String... messagesIn) {
        return new ResultJson<>(true, Arrays.asList(messagesIn), null, dataIn);
    }

    /**
     * Instantiates a new Json result.
     *
     * @param successIn  the success
     * @param messagesIn the messages
     * @param errorsIn   the field level errors
     * @param dataIn     the data
     */
    public ResultJson(boolean successIn, List<String> messagesIn, Map<String, List<String>> errorsIn, T dataIn) {
        this.success = successIn;
        this.messages = messagesIn;
        this.errors = errorsIn;
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
     * Gets field level errors.
     *
     * @return the field level errors
     */
    public Map<String, List<String>> getErrors() {
        return errors;
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
