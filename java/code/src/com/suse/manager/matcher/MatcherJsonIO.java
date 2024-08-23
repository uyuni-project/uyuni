/*
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

import static java.util.stream.Stream.of;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.credentials.RemoteCredentials;
import com.redhat.rhn.domain.matcher.MatcherRunData;
import com.redhat.rhn.domain.matcher.MatcherRunDataFactory;
import com.redhat.rhn.domain.product.CachingSUSEProductFactory;
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

import com.suse.manager.maintenance.BaseProductManager;
import com.suse.matcher.json.InputJson;
import com.suse.matcher.json.MatchJson;
import com.suse.matcher.json.OutputJson;
import com.suse.matcher.json.ProductJson;
import com.suse.matcher.json.SubscriptionJson;
import com.suse.matcher.json.SystemJson;
import com.suse.matcher.json.VirtualizationGroupJson;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Serializes and deserializes objects from and to JSON.
 */
public class MatcherJsonIO {

    private static final Logger LOGGER = LogManager.getLogger(MatcherJsonIO.class);

    /** Fake ID for the SUSE Manager server system. */
    public static final long SELF_SYSTEM_ID = 2000010000L;

    /** Architecture strings **/
    private static final String AMD64_ARCH_STR = "amd64";
    private static final String S390_ARCH_STR = "s390";
    private static final String PPC64LE_ARCH_STR = "ppc64le";

    /** (De)serializer instance. */
    private Gson gson;

    /** Cached instance of the s390x ServerArch object. */
    private final ServerArch s390arch;

    /** Cached mandatory product ID for an s390x system. */
    private final Optional<Long> productIdForS390xSystem;

    /** Cached mandatory product ID for a regular system. */
    private final Optional<Long> productIdForSystem;

    /** Cached mandatory monitoring product ID for a regular system. */
    private Optional<Long> monitoringProductId;

    /** Cached mandatory monitoring product ID for an s390x system. */
    private Optional<Long> monitoringProductIdS390x;

    /** Translation of unlimited virtual lifecycle products to single variant. **/
    private final Map<Long, Long> lifecycleProductsTranslation;

    /** Fast factory for SUSEProduct objects. */
    private final CachingSUSEProductFactory productFactory;

    /** SUSE Manager server products (by arch) installed on self **/
    private final Map<String, Long> selfProductsByArch;

    /** Monitoring products by arch **/
    private final Map<String, Long> monitoringProductByArch;

    /**
     * Default constructor
     */
    public MatcherJsonIO() {
        this(new BaseProductManager());
    }

    /**
     * Constructor for unit testing
     * @param baseProductManager the product manager to extract the product information
     */
    public MatcherJsonIO(final BaseProductManager baseProductManager) {
        gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting()
            .create();

        s390arch = ServerFactory.lookupServerArchByLabel("s390x");

        productIdForS390xSystem = productIdForEntitlement("SUSE-Manager-Mgmt-Unlimited-Virtual-Z");
        productIdForSystem = productIdForEntitlement("SUSE-Manager-Mgmt-Single");
        lifecycleProductsTranslation = new HashMap<>();
        productIdForEntitlement("SUSE-Manager-Mgmt-Unlimited-Virtual").ifPresent(
                from -> productIdForSystem.ifPresent(to -> lifecycleProductsTranslation.put(from, to)));

        monitoringProductId = productIdForEntitlement("SUSE-Manager-Mon-Single");
        monitoringProductIdS390x = productIdForEntitlement("SUSE-Manager-Mon-Unlimited-Virtual-Z");
        productIdForEntitlement("SUSE-Manager-Mon-Unlimited-Virtual").ifPresent(
                from -> monitoringProductId.ifPresent(to -> lifecycleProductsTranslation.put(from, to)));

        // Try to retrieve the products id from the base product information. Fall back to SUMA 4.3 product ids
        String productName = Optional.ofNullable(baseProductManager.getName()).map(String::toLowerCase).orElse(null);
        String version = baseProductManager.getVersion();

        selfProductsByArch = Map.of(
            AMD64_ARCH_STR, productIdForSelf(productName, version, "x86_64", 2378L),
            S390_ARCH_STR, productIdForSelf(productName, version, "s390x", 2377L),
            PPC64LE_ARCH_STR, productIdForSelf(productName, version, PPC64LE_ARCH_STR, 2376L)
        );

        monitoringProductByArch = Map.of(
            AMD64_ARCH_STR, 1201L,      // SUSE Manager Monitoring Single
            S390_ARCH_STR, 1203L,       // SUSE Manager Monitoring Unlimited Virtual Z
            PPC64LE_ARCH_STR, 1201L     // SUSE Manager Monitoring Single
        );

        productFactory = new CachingSUSEProductFactory();
    }

    private long productIdForSelf(String name, String version, String arch, long defaultId) {
        SUSEProduct ent = SUSEProductFactory.findSUSEProduct(name, version, null, arch, false);
        return Optional.ofNullable(ent).map(SUSEProduct::getProductId).orElse(defaultId);
    }

    /**
     * @param arch - cpu architecture of this SUMa
     * @param includeSelf - true if we want to add SUMa products and host
     * @param selfMonitoringEnabled whether the monitoring of SUMA server itself is enabled
     * @param needsEntitlements true if the server needs entitlements for the system is managing
     * @return an object representation of the JSON input for the matcher
     * about systems on this Server
     */
    public List<SystemJson> getJsonSystems(String arch, boolean includeSelf, boolean selfMonitoringEnabled,
                                           boolean needsEntitlements) {
        Set<String> vCoreCountedChannelFamilies = of("MICROOS-ARM64", "MICROOS-X86", "MICROOS-Z", "MICROOS-PPC")
                .flatMap(cf -> of("", "-ALPHA", "-BETA").map(s -> cf + s))
                .collect(Collectors.toSet());

        Stream<SystemJson> systems = ServerFactory.list(true, true).stream()
            .map(system -> {
                Long cpus = system.getCpu() == null ? null : system.getCpu().getNrsocket();
                Set<String> entitlements = system.getEntitlementLabels();
                boolean virtualHost = entitlements.contains(EntitlementManager.VIRTUALIZATION_ENTITLED) ||
                        !system.getGuests().isEmpty();
                Set<Long> productIds = productIdsForServer(system, needsEntitlements, entitlements)
                    .collect(Collectors.toSet());
                boolean countVCores = !virtualHost && system.getInstalledProductSet()
                        .map(productSet -> productSet.getBaseProduct())
                        .map(baseProduct -> baseProduct.getChannelFamily())
                        .stream()
                        .anyMatch(cf -> vCoreCountedChannelFamilies.contains(cf.getLabel()));
                if (countVCores) {
                    // HACK: better would be to introduce a field in SystemJson and adapt subscription-matcher
                    // For now it is not worth the effort
                    cpus = system.getCpu() == null ? null : system.getCpu().getNrCPU();
                }
                return new SystemJson(
                    system.getId(),
                    system.getName(),
                    cpus == null ? null : cpus.intValue(),
                    !system.isVirtualGuest(),
                    virtualHost,
                    getVirtualGuests(system),
                    productIds
                );
            });

        return Stream.concat(systems, jsonSystemForSelf(arch, includeSelf, selfMonitoringEnabled))
            .collect(Collectors.toList());
    }

    private static Set<Long> getVirtualGuests(Server system) {
        return system.getGuests().stream()
            .filter(vi -> vi.getGuestSystem() != null)
            .map(vi -> vi.getGuestSystem().getId())
            .collect(Collectors.toSet());
    }

    /**
     * @return an object representation of the JSON input for the matcher
     * about SUSE products on this Server
     */
    public List<ProductJson> getJsonProducts() {
        return SUSEProductFactory.findAllSUSEProducts().stream()
                .map(p -> new ProductJson(
                        p.getProductId(),
                        p.getFriendlyName(),
                        p.getChannelFamily() != null ? p.getChannelFamily().getLabel() : "",
                        p.isBase(),
                        p.getFree()))
                .collect(Collectors.toList());
    }

    /**
     * @return an object representation of the JSON input for the matcher
     * about subscriptions on this Server
     */
    public List<SubscriptionJson> getJsonSubscriptions() {
        return SCCCachingFactory.lookupOrderItems().stream()
            .map(order -> {
                SCCSubscription subscription = SCCCachingFactory.lookupSubscriptionBySccId(order.getSubscriptionId());
                RemoteCredentials credentials = Optional.ofNullable(order.getCredentials())
                    .flatMap(cr -> cr.castAs(RemoteCredentials.class))
                    .orElse(null);
                return new SubscriptionJson(
                    order.getSccId(),
                    order.getSku(),
                    subscription == null ? null : subscription.getName(),
                    order.getQuantity() == null ? null : order.getQuantity().intValue(),
                    order.getStartDate(),
                    order.getEndDate(),
                    credentials == null ? "extFile" : credentials.getUsername(),
                    subscription == null ? new HashSet<>() :
                        subscription.getProducts().stream()
                                // we want to merge the unlimited virtual products with the single ones
                                .map(i -> lifecycleProductsTranslation.getOrDefault(i.getProductId(), i.getProductId()))
                                .collect(Collectors.toSet())
                );
            })
            .collect(Collectors.toList());
    }

    /**
     * @return an object representation of the JSON input for the matcher
     * about pinned matches
     */
    public List<MatchJson> getJsonMatches() {
        return HibernateFactory.getSession()
            .createQuery("SELECT ps FROM PinnedSubscription ps", PinnedSubscription.class)
            .stream()
            .map(p -> new MatchJson(p.getSystemId(), p.getSubscriptionId(), null, null))
            .collect(Collectors.toList());
    }

    /**
     * Returns input data for subscription-matcher as a string.
     * @param arch cpu architecture of this SUMA instance. This is important for correct
     *             product ID computation in case includeSelf == true.
     * @param includeSelf true if we want to add the products of the SUMA instance
     *                    running Matcher to the JSON output. Since SUMA Server is not
     *                    typically a SUMA Client at the same time, its system (with
     *                    products) wouldn't be reported in the matcher input.
     *
     *                    Typically, this flag is true if this SUMA instance is an ISS
     *                    Master.
     * @param selfMonitoringEnabled whether the monitoring of SUMA server itself is enabled
     * @param needsEntitlements true if the server needs entitlements for the system is managing
     * @return an object representation of the JSON input for the matcher
     */
    public String generateMatcherInput(String arch, boolean includeSelf, boolean selfMonitoringEnabled,
                                       boolean needsEntitlements) {
        return gson.toJson(new InputJson(
            new Date(),
            getJsonSystems(arch, includeSelf, selfMonitoringEnabled, needsEntitlements),
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
    public List<VirtualizationGroupJson> getJsonVirtualizationGroups() {
        // only group we currently support is by virtual host manager
        return VirtualHostManagerFactory.getInstance().listVirtualHostManagers().stream()
                .map(vhm -> new VirtualizationGroupJson(
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
    public Optional<InputJson> getLastMatcherInput() {
        return Optional.ofNullable(MatcherRunDataFactory.getSingle())
            .map(MatcherRunData::getInput)
            .map(input -> gson.fromJson(input, InputJson.class));
    }

    /**
     * Gets the latest matcher output data
     * @return the output or empty in case the matcher did not run yet
     */
    public Optional<OutputJson> getLastMatcherOutput() {
        return Optional.ofNullable(MatcherRunDataFactory.getSingle())
            .map(MatcherRunData::getOutput)
            .map(output -> gson.fromJson(output, OutputJson.class));
    }

    /**
     * Computes the product ids of the the SUSE Manager Server product and the SUSE Linux
     * Enterprise product running on this machine.
     */
    private Set<Long> computeSelfProductIds(boolean includeSelf, boolean selfMonitoringEnabled, String arch) {
        if (!includeSelf && !selfMonitoringEnabled) {
            return Collections.emptySet();
        }


        if (!Arrays.asList(AMD64_ARCH_STR, S390_ARCH_STR, PPC64LE_ARCH_STR).contains(arch)) {
            LOGGER.warn("Couldn't determine products for SUMA server itself" +
                    " for architecture {}. Master SUSE Manager Server system products" +
                    " won't be reported to the subscription matcher.", arch);
            return Collections.emptySet();
        }

        Set<Long> result = new LinkedHashSet<>();
        if (includeSelf) {
            result.add(selfProductsByArch.get(arch));
        }

        if (selfMonitoringEnabled) {
            result.add(monitoringProductByArch.get(arch));
        }

        return result;
    }

    /**
     * Returns SUSE product ids for a server, including ids for SUSE Manager entitlements.
     * (For systems without a SUSE base product, empty stream is returned as we don't
     * require SUSE Manager entitlements for such systems).
     * Filters out the products with "SLE-M-T" product class as they are not considered in
     * subscription matching.
     * Also filters out the products for PAYG (Pay-As-You-Go) instances.
     * The product ids for entitlements are only added if SUSE Manager is BYOS
     */
    private Stream<Long> productIdsForServer(Server server, boolean needsEntitlements, Set<String> entitlements) {
        List<SUSEProduct> products = productFactory.map(server.getInstalledProducts())
                .filter(product -> !"SLE-M-T".equals(product.getChannelFamily().getLabel()))
                .collect(Collectors.toList());

        if (products.stream().noneMatch(SUSEProduct::isBase)) {
            return Stream.empty();
        }

        // add SUSE Manager entitlements
        return Stream.concat(
                server.isPayg() ? Stream.empty() : products.stream().map(SUSEProduct::getProductId),
                needsEntitlements ? entitlementIdsForServer(server, entitlements) : Stream.empty()
        );
    }

    /**
     * Returns SUSE Manager entitlement product ids for a server.
     */
    private Stream<Long> entitlementIdsForServer(Server server, Set<String> entitlements) {
        Optional<Long> lifecycleProduct = Optional.empty();
        boolean managementIncluded = server.isPayg() && server.getInstalledProductSet()
            .map(SUSEProductSet::getBaseProduct)
            .map(SUSEProduct::getName)
            .filter(baseProductName -> "sles_sap".equals(baseProductName))
            .isPresent();

        if (!managementIncluded && (entitlements.contains(EntitlementManager.SALT_ENTITLED) ||
                entitlements.contains(EntitlementManager.ENTERPRISE_ENTITLED))) {
            if (server.getServerArch().equals(s390arch)) {
                lifecycleProduct = productIdForS390xSystem;
            }
            else {
                lifecycleProduct = productIdForSystem;
            }
        }
        Optional<Long> monitoringProduct = Optional.empty();
        if (entitlements.contains(EntitlementManager.MONITORING_ENTITLED)) {
            if (server.getServerArch().equals(s390arch)) {
                monitoringProduct = monitoringProductIdS390x;
            }
            else {
                monitoringProduct = monitoringProductId;
            }
        }

        return Stream.concat(lifecycleProduct.stream(), monitoringProduct.stream());
    }

    /**
     * Returns a single SUSE Manager entitlement product ids given the name.
     * @return the product id or Optional.empty if the correspoding product has not been
     * added to SUSE Manager. Currently this happens only before the first mgr-sync,
     * which is scheduled at setup time
     */
    private Optional<Long> productIdForEntitlement(String productName) {
        SUSEProduct ent = SUSEProductFactory.findSUSEProduct(productName, "1.2", null, null, true);
        return Optional.ofNullable(ent).map(SUSEProduct::getProductId);
    }

    /**
     * Returns an optional SystemJson for the SUSE Manager server.
     */
    private Stream<SystemJson> jsonSystemForSelf(String arch, boolean includeSelf, boolean selfMonitoringEnabled) {
        if (includeSelf || selfMonitoringEnabled) {
            return Stream.of(new SystemJson(
                SELF_SYSTEM_ID,
                "SUSE Manager Server system",
                1,
                true,
                false,
                new HashSet<>(),
                computeSelfProductIds(includeSelf, selfMonitoringEnabled, arch)
            ));
        }

        return Stream.empty();
    }
}
