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
import com.redhat.rhn.domain.server.ServerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;
import java.util.stream.Stream;

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
     * @return an object representation of the JSON input for the matcher
     * about systems on this Server
     */
    public List<JsonSystem> getJsonSystems() {
        return ServerFactory.list().stream()
            .map(s -> new JsonSystem(s))
            .collect(toList());
    }

    /**
     * @return an object representation of the JSON input for the matcher
     * about SUSE products on this Server
     */
    public List<JsonProduct> getJsonProducts() {
        return SUSEProductFactory.findAllSUSEProducts().stream()
            .map(p -> new JsonProduct(p.getProductId(), p.getFriendlyName()))
            .collect(toList());
    }

    /**
     * @return an object representation of the JSON input for the matcher
     * about subscriptions on this Server
     */
    public List<JsonSubscription> getJsonSubscriptions() {
        return SCCCachingFactory.lookupOrderItems().stream()
            .map(s -> new JsonSubscription(s))
            .collect(toList());
    }

    /**
     * @return an object representation of the JSON input for the matcher
     * about pinned matches
     */
    public List<JsonPinnedMatch> getJsonPinnedMatches() {
        return ServerFactory.list().stream()
            .map(s -> s.getPinnedSubscriptions().stream()
               .map(p -> new JsonPinnedMatch(s.getId(), p.getOrderitemId()))
            )
            .reduce(Stream::concat).get()
            .collect(toList());
    }
}
