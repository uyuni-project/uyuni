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

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * JSON representation of the matcher's output.
 */
public class JsonOutput {

    /** Date and time of the match. */
    private Date timestamp;

    /** The confirmed matches. */
    private List<JsonMatch> confirmedMatches = new LinkedList<>();

    /** The messages. */
    private List<JsonMessage> messages = new LinkedList<>();

    /**
     * Standard constructor.
     *
     * @param timestampIn the timestamp
     * @param confirmedMatchesIn the confirmed matches
     * @param messagesIn the messages
     */
    public JsonOutput(Date timestampIn, List<JsonMatch> confirmedMatchesIn,
            List<JsonMessage> messagesIn) {
        timestamp = timestampIn;
        confirmedMatches = confirmedMatchesIn;
        messages = messagesIn;
    }

    /**
     * Gets the date and time of the match.
     *
     * @return the date and time of the match
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the date and time of the match.
     *
     * @param timestampIn the new date and time of the match
     */
    public void setTimestamp(Date timestampIn) {
        timestamp = timestampIn;
    }

    /**
     * Gets the confirmed matches.
     *
     * @return the confirmed matches
     */
    public List<JsonMatch> getConfirmedMatches() {
        return confirmedMatches;
    }

    /**
     * Sets the confirmed matches.
     *
     * @param confirmedMatchesIn the new confirmed matches
     */
    public void setConfirmedMatches(List<JsonMatch> confirmedMatchesIn) {
        confirmedMatches = confirmedMatchesIn;
    }

    /**
     * Gets the messages.
     *
     * @return the messages
     */
    public List<JsonMessage> getMessages() {
        return messages;
    }

    /**
     * Sets the messages.
     *
     * @param messagesIn the new messages
     */
    public void setMessages(List<JsonMessage> messagesIn) {
        messages = messagesIn;
    }
}
