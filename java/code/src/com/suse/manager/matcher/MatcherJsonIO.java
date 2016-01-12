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
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Serializes and deserializes objects from and to JSON.
 */
public class MatcherJsonIO {

    /** (De)serializer instance. */
    private Gson gson;

    private final boolean includeSelf;

    private final String arch;

    /**
     * Logger for this class
     */
    private static Logger logger = Logger.getLogger(MatcherJsonIO.class);

    /**
     * Constructor
     *
     * @param includeSelfIn - true if we want to add the products of the SUMA instance
     *                      running Matcher to the JSON output. Since SUMA Server is not
     *                      typically a SUMA Client at the same time, its system (with
     *                      products) wouldn't reported in the matcher input.
     *
     *                      Typically this flag is true if this SUMA instance is an ISS
     *                      Master.
     *
     * @param archIn - cpu architecture of this SUMA instance. This is important for correct
     *               product ID computation in case includeSelf == true.
     */
    public MatcherJsonIO(boolean includeSelfIn, String archIn) {
        gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting()
            .create();
        includeSelf = includeSelfIn;
        arch = archIn;
    }

    /**
     * @return an object representation of the JSON input for the matcher
     * about systems on this Server
     */
    public List<JsonSystem> getJsonSystems() {
        List<JsonSystem> systems = ServerFactory.list().stream()
                .map(s -> new JsonSystem(s))
                .collect(toList());

        if (includeSelf) {
            JsonSystem sumaServer = new JsonSystem();
            sumaServer.setId(Long.MAX_VALUE);
            sumaServer.setCpus(1L);
            sumaServer.setName("SUSE Manager Server system");
            sumaServer.setPhysical(true);
            sumaServer.setProductIds(computeSelfProductIds());

            systems.add(sumaServer);
        }

        return systems;
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
            .reduce(Stream::concat).orElse(Stream.empty())
            .collect(toList());
    }

    /**
     * @return an object representation of the JSON input for the matcher
     */
    public String getMatcherInput() {
        JsonInput result = new JsonInput();
        result.setSystems(getJsonSystems());
        result.setProducts(getJsonProducts());
        result.setSubscriptions(getJsonSubscriptions());
        result.setPinnedMatches(getJsonPinnedMatches());
        return gson.toJson(result);
    }

    /**
     * Computes the product ids of the the SUSE Manager Server product and the SUSE Linux
     * Enterprise product running on this machine.
     *
     * @return list of product ids with product installed on self
     */
    private List<Long> computeSelfProductIds() {
        List<Long> result = new ArrayList<>();
        if (arch.contains("amd64")) {
            result.add(1349L); // SUSE Manager Server 3.0 x86_64
            result.add(1322L); // SUSE Linux Enterprise Server 12 SP1 x86_64
        }
        else if (arch.contains("s390")) {
            result.add(1348L); // SUSE Manager Server 3.0 s390
            result.add(1335L); // SUSE Linux Enterprise Server 12 SP1 s390
        }
        else {
            logger.warn(String.format("Couldn't determine products for SUMA server itself" +
                    " for architecture %s. Master SUSE Manager Server system products" +
                    " won't be reported to the subscription matcher.", arch));
        }

        return result;
    }

}
