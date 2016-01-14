/**
 * Copyright (c) 2016 SUSE LLC
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *    * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *    * Neither the name of SUSE LLC nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.suse.matcher.json;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JSON representation of a user message generated during the match (error, warning, etc.).
 */
public class JsonMessage {

    /** A label identifying the message type. */
    private String type;

    /** Arbitrary data connected to this message. */
    private Map<String, String> data = new LinkedHashMap<>();

    /**
     * Standard constructor.
     *
     * @param typeIn the type
     * @param dataIn the data
     */
    public JsonMessage(String typeIn, Map<String, String> dataIn) {
        type = typeIn;
        data = dataIn;
    }

    /**
     * Gets the a label identifying the message type.
     *
     * @return the a label identifying the message type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the a label identifying the message type.
     *
     * @param typeIn the new a label identifying the message type
     */
    public void setType(String typeIn) {
        type = typeIn;
    }

    /**
     * Gets the data connected to this message.
     *
     * @return the data connected to this message
     */
    public Map<String, String> getData() {
        return data;
    }

    /**
     * Sets the data connected to this message.
     *
     * @param dataIn the new data connected to this message
     */
    public void setData(Map<String, String> dataIn) {
        data = dataIn;
    }
}
