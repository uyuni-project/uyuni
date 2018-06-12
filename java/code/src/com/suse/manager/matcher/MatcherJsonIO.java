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

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.empty;
import static java.util.stream.Stream.of;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.matcher.MatcherRunData;
import com.redhat.rhn.domain.matcher.MatcherRunDataFactory;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.product.SUSEProductSet;
import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.domain.scc.SCCSubscription;
import com.redhat.rhn.domain.server.PinnedSubscription;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerArch;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerFactory;
import com.redhat.rhn.manager.entitlement.EntitlementManager;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.suse.matcher.json.JsonInput;
import com.suse.matcher.json.JsonMatch;
import com.suse.matcher.json.JsonOutput;
import com.suse.matcher.json.JsonProduct;
import com.suse.matcher.json.JsonSubscription;
import com.suse.matcher.json.JsonSystem;
import com.suse.matcher.json.JsonVirtualizationGroup;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Serializes and deserializes objects from and to JSON.
 */
public class MatcherJsonIO {

    /** Fake ID for the SUSE Manager server system. */
    public static final long SELF_SYSTEM_ID = 2000010000L;

    /** (De)serializer instance. */
    private Gson gson;

    /** Cached instance of the s390x ServerArch object. */
    private final ServerArch s390arch;

    /**
     * Logger for this class
     */
    private static Logger logger = Logger.getLogger(MatcherJsonIO.class);

    /**
     * Constructor
     */
    public MatcherJsonIO() {
        gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting()
            .create();
        s390arch = ServerFactory.lookupServerArchByLabel("s390x");
    }

    /**
     * @param includeSelf - true if we want to add SUMa products and host
     * @param arch - cpu architecture of this SUMa
     * @return an object representation of the JSON input for the matcher
     * about systems on this Server
     */
    public List<JsonSystem> getJsonSystems(boolean includeSelf, String arch) {
        Stream<JsonSystem> systems = ServerFactory.list(true, true).stream()
            .map(system -> {
                Long cpus = system.getCpu() == null ? null : system.getCpu().getNrsocket();
                Set<Long> productIds = productIdsForServer(system).collect(toSet());
                return new JsonSystem(
                    system.getId(),
                    system.getName(),
                    cpus == null ? null : cpus.intValue(),
                    !system.isVirtualGuest(),
                    system.isVirtualHost(),
                    getVirtualGuests(system),
                    productIds
                );
            });

        return concat(systems, jsonSystemForSelf(includeSelf, arch)).collect(toList());
    }

    private static Set<Long> getVirtualGuests(Server system) {
        return system.getGuests().stream()
            .filter(vi -> vi.getGuestSystem() != null)
            .map(vi -> vi.getGuestSystem().getId())
            .collect(toSet());
    }

    /**
     * @return an object representation of the JSON input for the matcher
     * about SUSE products on this Server
     */
    public List<JsonProduct> getJsonProducts() {
        return SUSEProductFactory.findAllSUSEProducts().stream()
                .map(p -> new JsonProduct(
                        p.getProductId(),
                        p.getFriendlyName(),
                        p.getChannelFamily() != null ? p.getChannelFamily().getLabel() : "",
                        p.isBase(),
                        p.getFree()))
                .collect(toList());
    }

    /**
     * @return an object representation of the JSON input for the matcher
     * about subscriptions on this Server
     */
    public List<JsonSubscription> getJsonSubscriptions() {
        return SCCCachingFactory.lookupOrderItems().stream()
            .map(order -> {
                SCCSubscription subscription = order.getSubscription();
                Credentials credentials = order.getCredentials();
                return new JsonSubscription(
                    order.getSccId(),
                    order.getSku(),
                    subscription == null ? null : subscription.getName(),
                    order.getQuantity() == null ? null : order.getQuantity().intValue(),
                    order.getStartDate(),
                    order.getEndDate(),
                    credentials == null ? "extFile" : credentials.getUsername(),
                    subscription == null ? new HashSet<>() :
                        subscription.getProducts().stream()
                            .map(i -> i.getProductId())
                            .collect(toSet())
                );
            })
            .collect(toList());
    }

    /**
     * @return an object representation of the JSON input for the matcher
     * about pinned matches
     */
    @SuppressWarnings("unchecked")
    public List<JsonMatch> getJsonMatches() {
        return ((List<PinnedSubscription>) HibernateFactory.getSession()
                .createCriteria(PinnedSubscription.class).list()).stream()
                .map(p -> new JsonMatch(
                    p.getSystemId(), p.getSubscriptionId(), null, null, null))
                .collect(toList());
    }

    /**
     * Returns input data for subscription-matcher as a string.
     *
     * @param includeSelf - true if we want to add the products of the SUMA instance
     *                      running Matcher to the JSON output. Since SUMA Server is not
     *                      typically a SUMA Client at the same time, its system (with
     *                      products) wouldn't reported in the matcher input.
     *
     *                      Typically this flag is true if this SUMA instance is an ISS
     *                      Master.
     *
     * @param arch - cpu architecture of this SUMA instance. This is important for correct
     *               product ID computation in case includeSelf == true.
     * @return an object representation of the JSON input for the matcher
     */
    public String generateMatcherInput(boolean includeSelf, String arch) {
        return gson.toJson(new JsonInput(
            new Date(),
            getJsonSystems(includeSelf, arch),
            getJsonVirtualizationGroups(),
            getJsonProducts(),
            getJsonSubscriptions(),
            getJsonMatches())
        );
    }

    /**
     * Returns the JSON representation of virtualization groups.
     *
     * @return virtualization groups
     */
    public List<JsonVirtualizationGroup> getJsonVirtualizationGroups() {
        // only group we currently support is by virtual host manager
        return VirtualHostManagerFactory.getInstance().listVirtualHostManagers().stream()
                .map(vhm -> new JsonVirtualizationGroup(
                        vhm.getId(),
                        vhm.getLabel(),
                        "virtual_host_manager_" + vhm.getGathererModule().toLowerCase(),
                        vhm.getServers().stream()
                                .flatMap(s -> getVirtualGuests(s).stream())
                                .collect(Collectors.toSet())))
                .collect(Collectors.toList());
    }

    /**
     * Returns the latest matcher input (that was used for the latest matcher run)
     * @return the input data or empty in case the corresponding data is missing
     */
    public Optional<JsonInput> getLastMatcherInput() {
        MatcherRunData data = MatcherRunDataFactory.getSingle();
        return ofNullable(gson.fromJson(
                data == null ? null : data.getInput(),
                JsonInput.class));
    }

    /**
     * Gets the latest matcher output data
     * @return the output or empty in case the matcher did not run yet
     */
    public Optional<JsonOutput> getLastMatcherOutput() {
        MatcherRunData data = MatcherRunDataFactory.getSingle();
        return ofNullable(gson.fromJson(
                data == null ? null : data.getOutput(),
                JsonOutput.class));
    }

    /**
     * Computes the product ids of the the SUSE Manager Server product and the SUSE Linux
     * Enterprise product running on this machine.
     */
    private Set<Long> computeSelfProductIds(String arch) {
        Set<Long> result = new LinkedHashSet<>();
        if (arch.contains("amd64")) {
            result.add(1518L); // SUSE Manager Server 3.1 x86_64
            result.add(1357L); // SUSE Linux Enterprise Server 12 SP2 x86_64
        }
        else if (arch.contains("s390")) {
            result.add(1519L); // SUSE Manager Server 3.1 s390x
            result.add(1356L); // SUSE Linux Enterprise Server 12 SP2 s390x
        }
        else {
            logger.warn(String.format("Couldn't determine products for SUMA server itself" +
                    " for architecture %s. Master SUSE Manager Server system products" +
                    " won't be reported to the subscription matcher.", arch));
        }

        return result;
    }

    /**
     * Returns SUSE product ids for a server, including ids for SUSE Manager entitlements.
     * (For systems without a SUSE base product, empty stream is returned as we don't
     * require SUSE Manager entitlements for such systems).
     */
    private Stream<Long> productIdsForServer(Server server) {
        SUSEProductSet productSet = server.getInstalledProductSet();
        if (productSet == null || productSet.getBaseProduct() == null) {
            return empty();
        }

        // concatenate base product, addon products and the SUSE Manager entitlements
        return of(
            of(productSet.getBaseProduct().getProductId()),
            productSet.getAddonProducts().stream().map(p -> p.getProductId()),
            entitlementIdsForServer(server)
        ).flatMap(Function.identity());
    }

    /**
     * Returns SUSE Manager entitlement product ids for a server.
     */
    private Stream<Long> entitlementIdsForServer(Server server) {
        if (server.hasEntitlement(EntitlementManager.MANAGEMENT) ||
                server.hasEntitlement(EntitlementManager.SALT)) {
            if (server.getServerArch().equals(s390arch)) {
                return productIdsForS390xSystem();
            }
            else if (server.hasVirtualizationEntitlement()) {
                return productIdsForVirtualHost();
            }
            else {
                return productIdsForSystem();
            }
        }
        return empty();
    }

    /**
     * Retruns product ids for an s390x system.
     */
    private Stream<Long> productIdsForS390xSystem() {
        return of(
            productIdForEntitlement("SUSE-Manager-Mgmt-Unlimited-Virtual-Z"),
            productIdForEntitlement("SUSE-Manager-Prov-Unlimited-Virtual-Z")
        ).filter(id -> id.isPresent()).map(id -> id.get());
    }

    /**
     * Retruns product ids for a virtual host.
     */
    private Stream<Long> productIdsForVirtualHost() {
        return of(
            productIdForEntitlement("SUSE-Manager-Mgmt-Unlimited-Virtual"),
            productIdForEntitlement("SUSE-Manager-Prov-Unlimited-Virtual")
        ).filter(id -> id.isPresent()).map(id -> id.get());
    }

    /**
     * Returns product ids for a normal system.
     */
    private Stream<Long> productIdsForSystem() {
        return of(
            productIdForEntitlement("SUSE-Manager-Mgmt-Single"),
            productIdForEntitlement("SUSE-Manager-Prov-Single")
        ).filter(id -> id.isPresent()).map(id -> id.get());
    }

    /**
     * Returns a single SUSE Manager entitlement product ids given the name.
     * @return the product id or Optional.empty if the correspoding product has not been
     * added to SUSE Manager. Currently this happens only before the first mgr-sync,
     * which is scheduled at setup time
     */
    private Optional<Long> productIdForEntitlement(String productName) {
        SUSEProduct ent = SUSEProductFactory.findSUSEProduct(productName, "1.2", null,
                null, true);
        if (ent != null) {
            return Optional.of(ent.getProductId());
        }
        else {
            return Optional.empty();
        }
    }

    /**
     * Returns an optional JsonSystem for the SUSE Manager server.
     */
    private Stream<JsonSystem> jsonSystemForSelf(boolean includeSelf, String arch) {
        if (includeSelf) {
            return of(new JsonSystem(
                SELF_SYSTEM_ID,
                "SUSE Manager Server system",
                1,
                true,
                false,
                new HashSet<>(),
                computeSelfProductIds(arch)
            ));
        }
        else {
            return empty();
        }
    }
}
