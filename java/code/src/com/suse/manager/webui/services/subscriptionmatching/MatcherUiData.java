/**
 * Copyright (c) 2016 SUSE LLC
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
package com.suse.manager.webui.services.subscriptionmatching;

import com.google.gson.annotations.SerializedName;
import com.suse.matcher.json.JsonMessage;

import java.util.LinkedList;
import java.util.List;

/**
 * Backing data for the Subscription Matching UI
 */
public class MatcherUiData {

    @SerializedName("matcher_data_available")
    private boolean matcherDataAvailable;

    private List<JsonMessage> messages = new LinkedList<>();

    /**
     * Standard constructor.
     * @param matcherDataAvailableIn - true if the matcher data is available
     * @param messagesIn - list of messages
     */
    public MatcherUiData(boolean matcherDataAvailableIn, List<JsonMessage> messagesIn) {
        matcherDataAvailable = matcherDataAvailableIn;
        messages = messagesIn;
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
     * @param messagesIn the messages
     */
    public void setMessages(List<JsonMessage> messagesIn) {
        messages = messagesIn;
    }

    /**
     * True if the data from the matcher is available.
     *
     * @return true true if the matcher data is available
     */
    public boolean isMatcherDataAvailable() {
        return matcherDataAvailable;
    }

    /**
     * Sets the flag for the matcher data availability
     * @param matcherDataAvailableIn the flag
     */
    public void setMatcherDataAvailable(boolean matcherDataAvailableIn) {
        matcherDataAvailable = matcherDataAvailableIn;
    }
}
