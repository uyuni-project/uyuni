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
package com.suse.matcher.json;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * JSON representation of the matcher's output.
 */
public class JsonOutput {

    /** Date and time of the match. */
    private Date timestamp;

    /** The confirmed matches. */
    private List<JsonMatch> confirmedMatches = new LinkedList<>();

    /** Mapping from subscription id to its policy */
    private Map<Long, String> subscriptionPolicies = new HashMap<>();

    /** The messages. */
    private List<JsonMessage> messages = new LinkedList<>();

    /**
     * Standard constructor.
     *
     * @param timestampIn the timestamp
     * @param confirmedMatchesIn the confirmed matches
     * @param messagesIn the messages
     * @param subscriptionPoliciesIn mapping from subscription id to its policy
     */
    public JsonOutput(Date timestampIn, List<JsonMatch> confirmedMatchesIn,
            List<JsonMessage> messagesIn, Map<Long, String> subscriptionPoliciesIn) {
        timestamp = timestampIn;
        confirmedMatches = confirmedMatchesIn;
        messages = messagesIn;
        subscriptionPolicies = subscriptionPoliciesIn;
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
     * Gets the subscription to policy mapping.
     * @return the subscription to policy mapping.
     */
    public Map<Long, String> getSubscriptionPolicies() {
        return subscriptionPolicies;
    }

    /**
     * Sets the subscription to policy mapping.
     * @param subscriptionPoliciesIn subscription to policy map
     */
    public void setSubscriptionPolicies(Map<Long, String> subscriptionPoliciesIn) {
        this.subscriptionPolicies = subscriptionPoliciesIn;
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
