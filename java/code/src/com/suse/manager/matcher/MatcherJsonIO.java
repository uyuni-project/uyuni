/**
 * Copyright (c) 2015 SUSE LLC
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

package com.suse.manager.matcher;

import static java.util.stream.Collectors.toList;

import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.domain.scc.SCCOrderItem;
import com.redhat.rhn.domain.server.PinnedSubscription;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Serializes and deserializes objects from and to JSON.
 */
public class MatcherJsonIO {

    /** (De)serializer instance. */
    private Gson gson;

    /**
     * Constructor
     */
    public MatcherJsonIO() {
        gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting()
            .create();
    }

    /**
     * @return a json string with all systems on this Server
     */
    public String getJsonSystems() {
        List<JsonSystem> systems = new LinkedList<JsonSystem>();
        for (Server s : ServerFactory.list()) {
            JsonSystem sys = new JsonSystem(s);
            systems.add(sys);
        }
        return gson.toJson(systems);
    }

    /**
     * @return a json string with all SUSE products on this Server
     */
    public String getJsonProducts() {
        return gson.toJson(
            SUSEProductFactory.findAllSUSEProducts().stream()
                .map(p -> new JsonProduct(p.getProductId(), p.getFriendlyName()))
                .collect(toList())
        );
    }

    /**
     * @return a json string with all subscriptions
     */
    public String getJsonSubscriptions() {
        List<JsonSubscription> subscriptions = new LinkedList<>();
        for (SCCOrderItem item : SCCCachingFactory.lookupOrderItems()) {
            JsonSubscription sub = new JsonSubscription(item);
            subscriptions.add(sub);
        }
        return gson.toJson(subscriptions);
    }

    /**
     * @return a json string with all pinned matches
     */
    public String getJsonPinnedMatches() {
        List<JsonPinnedMatch> pins = new LinkedList<>();
        for (Server s : ServerFactory.list()) {
            Set<PinnedSubscription> sysPins = s.getPinnedSubscriptions();
            if (!sysPins.isEmpty()) {
                for (PinnedSubscription pin : sysPins) {
                    pins.add(new JsonPinnedMatch(s.getId(), pin.getOrderitemId()));
                }
            }
        }
        return gson.toJson(pins);
    }
}
