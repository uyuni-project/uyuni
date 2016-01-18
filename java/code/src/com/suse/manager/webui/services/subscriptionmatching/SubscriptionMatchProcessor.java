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

import com.suse.matcher.json.JsonInput;
import com.suse.matcher.json.JsonMessage;
import com.suse.matcher.json.JsonOutput;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

/**
 * Processes data from the matcher to a form that's displayable by the UI.
 */
public class SubscriptionMatchProcessor {

    /**
     * Gets UI-ready data.
     *
     * @param input matcher input
     * @param output matcher output
     * @return the data
     */
    public Object getData(Optional<JsonInput> input, Optional<JsonOutput> output) {
        if (input.isPresent() && output.isPresent()) {
            return new MatcherUiData(true,
                    output.get().getMessages().stream()
                            .map(m -> adjustMessage(m, input.get()))
                            .collect(Collectors.toList()));
        }
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
            long systemId = Long.parseLong(message.getData().get("id"));
            data.put("name", ofNullable(
                    input.getSystems().stream()
                            .filter(s -> s.getId().equals(systemId))
                            .findFirst()
                            .get().getName())
                    .orElse("System id: " + systemId));
            return new JsonMessage(message.getType(), data);
        }
        else if (message.getType().equals("unsatisfied_pinned_match")) {
            long systemId = Long.parseLong(message.getData().get("system_id"));
            data.put("system_name", ofNullable(
                    input.getSystems().stream()
                            .filter(s -> s.getId().equals(systemId))
                            .findFirst()
                            .get().getName())
                    .orElse("System id: " + systemId));
            long subscriptionId = Long.parseLong(message.getData().get("subscription_id"));
            data.put("subscription_name", ofNullable(
                    input.getSubscriptions().stream()
                            .filter(s -> s.getId().equals(subscriptionId))
                            .findFirst()
                            .get().getName())
                    .orElse("Subscription id: " + subscriptionId));
            return new JsonMessage(message.getType(), data);
        }
        else { // pass it through
            return new JsonMessage(message.getType(), message.getData());
        }
    }
}
