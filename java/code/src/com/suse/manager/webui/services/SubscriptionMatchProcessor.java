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
package com.suse.manager.webui.services;

import com.google.gson.annotations.SerializedName;
import com.suse.matcher.json.JsonInput;
import com.suse.matcher.json.JsonMessage;
import com.suse.matcher.json.JsonOutput;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Processes data from the matcher to a form that's displayable by the UI.
 */
public class SubscriptionMatchProcessor {

    public class MatcherUiData {

        @SerializedName("matcher_data_available")
        private boolean matcherDataAvailable;

        /** The messages */
        private List<JsonMessage> messages = new LinkedList<>();

        public MatcherUiData(boolean matcherDataAvailable, List<JsonMessage> messages) {
            this.matcherDataAvailable = matcherDataAvailable;
            this.messages = messages;
        }

        public List<JsonMessage> getMessages() {
            return messages;
        }

        public void setMessages(List<JsonMessage> messages) {
            this.messages = messages;
        }

        public boolean isMatcherDataAvailable() {
            return matcherDataAvailable;
        }

        public void setMatcherDataAvailable(boolean matcherDataAvailable) {
            this.matcherDataAvailable = matcherDataAvailable;
        }
    }

    /**
     * Gets UI-ready data.
     *
     * @return the data
     */
    public Object getData(Optional<JsonInput> input, Optional<JsonOutput> output) {
        if (input.isPresent() && output.isPresent())
            return new MatcherUiData(true,
                    output.get().getMessages().stream()
                        .map(m -> adjustMessage(m, input.get()))
                        .collect(Collectors.toList()));
        else {
            return new MatcherUiData(false, new LinkedList<>());
        }
    }

    private static JsonMessage adjustMessage(JsonMessage message, JsonInput input) {
        final Set<String> typesWithSystemId = new HashSet<>();
        typesWithSystemId.add("guest_with_unknown_host");
        typesWithSystemId.add("unknown_cpu_count");
        typesWithSystemId.add("physical_guest");

        Map<String, String> data = new HashMap<>();
        if (typesWithSystemId.contains(message.getType())) {
            data.put("name", input.getSystems().stream()
                    .filter(s -> s.getId().equals(Long.parseLong(message.getData().get("id"))))
                    .findFirst()
                    .get().getName());
            return new JsonMessage(message.getType(), data);
        }
        else if (message.getType().equals("unsatisfied_pinned_match")) {
            data.put("system_name", input.getSystems().stream()
                    .filter(s -> s.getId().equals(Long.parseLong(message.getData().get("system_id"))))
                    .findFirst()
                    .get().getName());
            data.put("subscription_name", input.getSubscriptions().stream()
                    .filter(s -> s.getId().equals(Long.parseLong(message.getData().get("subscription_id"))))
                    .findFirst()
                    .get().getName());
            return new JsonMessage(message.getType(), data);
        }
        else { // pass it through
            return new JsonMessage(message.getType(), message.getData());
        }
    }
}
