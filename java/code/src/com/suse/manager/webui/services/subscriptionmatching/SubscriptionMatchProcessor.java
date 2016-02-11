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

import com.redhat.rhn.domain.server.PinnedSubscription;
import com.redhat.rhn.domain.server.PinnedSubscriptionFactory;
import com.redhat.rhn.taskomatic.TaskoFactory;
import com.redhat.rhn.taskomatic.TaskoRun;
import com.suse.matcher.json.JsonInput;
import com.suse.matcher.json.JsonMessage;
import com.suse.matcher.json.JsonOutput;
import com.suse.matcher.json.JsonProduct;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

/**
 * Processes data from the matcher to a form that's displayable by the UI.
 * todo consider caching immediate lookup values to maps to improve performance
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
        TaskoRun latestRun = TaskoFactory.getLatestRun("gatherer-matcher-bunch");
        Date latestStart = latestRun == null ? null : latestRun.getStartTime();
        Date latestEnd = latestRun == null ? null : latestRun.getEndTime();
        if (input.isPresent() && output.isPresent()) {
            MatcherUiData matcherUiData = new MatcherUiData(true,
                    latestStart,
                    latestEnd,
                    messages(input.get(), output.get()),
                    subscriptions(input.get(), output.get()),
                    unmatchedSystems(input.get(), output.get()),
                    pinnedMatches(input.get(), output.get()),
                    systems(input.get(), output.get()));
            return matcherUiData;
        }
        else {
            return new MatcherUiData(false, latestStart, latestEnd, new LinkedList<>(),
                    new HashMap<>(), new LinkedList<>(), new LinkedList<>(),
                    new HashMap<>());
        }
    }

    private Map<String, System> systems(JsonInput input, JsonOutput output) {
        return input.getSystems().stream()
                .map(s -> new System(
                    s.getId(),
                    s.getName(),
                    s.getCpus(),
                    null,
                    output.getMatches().stream()
                        .map(m -> m.getSubscriptionId())
                        .distinct()
                        .collect(toList())
                ))
                .collect(toMap(
                    s -> "" + s.getId(),
                    s -> s
                 ));
    }

    private List<PinnedMatch> pinnedMatches(JsonInput input, JsonOutput output) {
        return PinnedSubscriptionFactory.getInstance().listPinnedSubscriptions().stream()
                .map(ps -> new PinnedMatch(
                    ps.getId(),
                    ps.getSubscriptionId(),
                    ps.getSystemId(),
                    deriveMatchStatus(ps, input, output)))
                .collect(toList());
    }

    private static String deriveMatchStatus(PinnedSubscription ps, JsonInput input,
            JsonOutput output) {
        boolean satisfied = output.getConfirmedMatches().stream()
                .filter(m -> m.getSystemId().equals(ps.getSystemId()) &&
                    m.getSubscriptionId().equals(ps.getSubscriptionId()))
                .findAny()
                .isPresent();

        if (satisfied) {
            return "satisfied";
        }

        boolean unsatisfied = input.getPinnedMatches().stream()
                .filter(p -> p.getSystemId().equals(ps.getSystemId()) &&
                    p.getSubscriptionId().equals(ps.getSubscriptionId()))
                .findAny()
                .isPresent();

        if (unsatisfied) {
            return "unsatisfied";
        }

        return "pending";
    }

    private List<JsonMessage> messages(JsonInput input, JsonOutput output) {
        return output.getMessages().stream()
                .filter(m -> !m.getType().equals("unsatisfied_pinned_match"))
                .map(m -> translateMessage(m, input)) .collect(toList());
    }

    private Map<String, Subscription> subscriptions(JsonInput input, JsonOutput output) {
        Map<Long, Integer> matchedQuantity = matchedQuantity(output);
        return input.getSubscriptions().stream()
                .filter(s -> s.getQuantity() != null)
                .map(js -> new Subscription(js.getId(),
                        js.getPartNumber(),
                        js.getName(),
                        output.getSubscriptionPolicies().get(js.getId()),
                        js.getQuantity(),
                        matchedQuantity.getOrDefault(js.getId(), 0),
                        js.getStartDate(), js.getEndDate()))
                .filter(s -> s.getTotalQuantity() != null && s.getTotalQuantity() > 0)
                .filter(s -> s.getPolicy() != null)
                .filter(s -> s.getStartDate() != null && s.getEndDate() != null)
                .collect(toMap(
                    s -> "" + s.getId(),
                    s -> s
                 ));
    }

    private Map<Long, Integer> matchedQuantity(JsonOutput output) {
        // check what about ids which are in input, but not in output (currently we set them
        // to 0)
        // compute cents by subscription id
        Map<Long, Integer> matchedCents = new HashMap<>();
        Map<Long, Integer> matchedQuantity = new HashMap<>();
        output.getMatches().stream()
                .filter(m -> m.getConfirmed())
                .forEach(m -> matchedCents.merge(m.getSubscriptionId(), m.getCents(),
                        Math::addExact));

        matchedCents.forEach((sid, cents)
                -> matchedQuantity.put(sid, (cents + 100 - 1) / 100));

        return matchedQuantity;
    }

    private static JsonMessage translateMessage(JsonMessage message, JsonInput input) {
        if (message.getType().equals("unknown_part_number")) {
            return new JsonMessage("unknownPartNumber", new HashMap<String, String>() { {
                put("partNumber", message.getData().get("part_number"));
            } });
        }
        if (message.getType().equals("physical_guest")) {
            return new JsonMessage("physicalGuest", message.getData());
        }
        if (message.getType().equals("guest_with_unknown_host")) {
            return new JsonMessage("guestWithUnknownHost", message.getData());
        }
        if (message.getType().equals("unknown_cpu_count")) {
            return new JsonMessage("unknownCpuCount", message.getData());
        }

        // pass it through
        return new JsonMessage(message.getType(), message.getData());
    }

    private List<System> unmatchedSystems(JsonInput input, JsonOutput output) {
        Set<Long> freeProducts = input.getProducts().stream()
                .filter(p -> p.getFree())
                .map(p -> p.getId())
                .collect(Collectors.toSet());

        Map<Long, Set<Long>> systemMatchedProducts = output.getMatches().stream()
                .filter(m -> m.getConfirmed())
                .collect(Collectors.toMap(
                        m -> m.getSystemId(),
                        m -> Collections.singleton(m.getProductId()),
                        (old, newV) -> concat(old.stream(), newV.stream())
                                .collect(Collectors.toSet())));

        // for each system, subtract the matched products from its products
        // ignore free products
        return input.getSystems().stream()
                .map(s -> {
                    List<String> unmatchedProductNames = s.getProductIds().stream()
                            .filter(e -> !systemMatchedProducts.getOrDefault(s.getId(),
                                Collections.emptySet()).contains(e))
                            .filter(id -> !freeProducts.contains(id))
                            .map(id -> productNameById(id, input))
                            .sorted()
                            .collect(toList());

                    return new System(
                            s.getId(),
                            s.getName(),
                            s.getCpus(),
                            unmatchedProductNames,
                            null);
                })
                .filter(s -> !s.getProducts().isEmpty())
                .collect(toList());
    }

    private String productNameById(Long id, JsonInput input) {
        return input.getProducts().stream()
                .filter(p -> p.getId().equals(id))
                .map(JsonProduct::getName)
                .findFirst()
                .orElse("Unknown product (" + id + ")");
    }
}
