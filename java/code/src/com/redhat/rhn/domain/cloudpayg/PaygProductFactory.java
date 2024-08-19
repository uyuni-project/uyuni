/*
 * Copyright (c) 2023 SUSE LLC
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

package com.redhat.rhn.domain.cloudpayg;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.util.RpmVersionComparator;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelFamilyFactory;
import com.redhat.rhn.domain.common.ArchType;
import com.redhat.rhn.domain.credentials.CloudCredentials;
import com.redhat.rhn.domain.credentials.CloudRMTCredentials;
import com.redhat.rhn.domain.credentials.RemoteCredentials;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.domain.scc.SCCRepository;
import com.redhat.rhn.domain.scc.SCCRepositoryAuth;
import com.redhat.rhn.domain.scc.SCCRepositoryCloudRmtAuth;
import com.redhat.rhn.taskomatic.task.payg.beans.PaygProductInfo;

import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PaygProductFactory extends HibernateFactory {

    private static final Logger LOGGER = LogManager.getLogger(PaygProductFactory.class);

    private static final RpmVersionComparator RPM_VERSION_COMPARATOR = new RpmVersionComparator();

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    /**
     * Associates the given credentials to the specified products. All previous associated products will be removed.
     * @param credentials the credentials
     * @param productInfos a collection of products associated with the credentials
     */
    public static void updateProducts(CloudCredentials credentials, Collection<PaygProductInfo> productInfos) {
        Session session = getSession();

        // First delete all the existing products
        session.createNamedQuery("PaygCredentialsProduct.deleteByCredentialsId")
            .setParameter("credsId", credentials.getId())
            .executeUpdate();

        if (CollectionUtils.isEmpty(productInfos)) {
            return;
        }

        // Store the products
        productInfos.stream()
            .map(product -> new PaygCredentialsProduct(credentials.getId(), product))
            .forEach(session::save);
    }

    /**
     * Retrieves all the products associated with the given credentials
     * @param credentials the credentials
     * @return the list of products associated with the credentials or an empty list if none are set.
     */
    public static List<PaygProductInfo> getProductsForCredentials(CloudRMTCredentials credentials) {
        return getSession().createNamedQuery("PaygCredentialsProduct.listByCredentialsId", PaygCredentialsProduct.class)
            .setParameter("credsId", credentials.getId())
            .stream()
            .map(p -> new PaygProductInfo(p.getName(), p.getVersion(), p.getArch()))
            .collect(Collectors.toList());
    }

    /**
     * Updates the repository authorization for the products associated with the specified credentials
     * @param credentials the credentials
     * @param products    the products
     * @return the list of authorization that have been processed
     */
    public static List<SCCRepositoryAuth> refreshRepositoriesAuths(RemoteCredentials credentials,
                                                                   Collection<PaygProductInfo> products) {
        List<SCCRepositoryAuth> existingAuths = SCCCachingFactory.lookupRepositoryAuthByCredential(credentials);

        List<SCCRepositoryAuth> processedAuths = new ArrayList<>();
        PaygProductFactory.getRepositoryForProducts(products).forEach(repository -> {
            SCCRepositoryAuth authRepo = getOrCreateRepositoryAuth(existingAuths, repository);

            //TODO: this needs some class restructuring
            authRepo.setCredentials(credentials);
            // Update content source URL, since it should be pointing to a Credentials record, and it may have changed
            if (authRepo.getContentSource() != null) {
                authRepo.getContentSource().setSourceUrl(authRepo.getUrl());
            }

            SCCCachingFactory.saveRepositoryAuth(authRepo);
            processedAuths.add(authRepo);
        });

        existingAuths.stream()
            .filter(er -> processedAuths.stream().noneMatch(pr -> er.getId().equals(pr.getId())))
            .forEach(SCCCachingFactory::deleteRepositoryAuth);

        return processedAuths;
    }

    private static SCCRepositoryAuth getOrCreateRepositoryAuth(List<SCCRepositoryAuth> auths,
                                                               SCCRepository repository) {
        return auths.stream()
            .filter(r -> r.getRepo().getId().equals(repository.getId()))
            .findFirst()
            .orElseGet(() -> {
                SCCRepositoryCloudRmtAuth newAuth = new SCCRepositoryCloudRmtAuth();
                newAuth.setRepo(repository);
                return newAuth;
            });
    }

    /**
     * Retrieves the repositories associated with the specified products
     * @param products the list of products
     * @return the set of repositories
     */
    private static Stream<SCCRepository> getRepositoryForProducts(Collection<PaygProductInfo> products) {
        return products.stream().flatMap(product -> {
            if (product.getName().equalsIgnoreCase("suse-manager-proxy")) {
                return SCCCachingFactory.lookupRepositoriesByRootProductNameVersionArchForPayg(
                    product.getName(), product.getVersion(), product.getArch());
            }

            return SCCCachingFactory.lookupRepositoriesByProductNameAndArchForPayg(
                    product.getName(), product.getArch())
                // We add Tools Channels directly to SLE12 products, but they are not accessible
                // via the SLES credentials. We need to remove them from all except the sle-manager-tools
                // product
                .filter(r -> !(!product.getName().equalsIgnoreCase("sle-manager-tools") &&
                    r.getName().toLowerCase(Locale.ROOT).startsWith("sle-manager-tools12")));
        });
    }

    /**
     *  Lists the additional products that are accessible when SUSE Manager is PAYG. These are:
     *   1) SUSE Manager Tools products with RPM architecture (DEB are not yet supported)
     *   2) SUSE Manager proxy products with version >= 4.2
     * @return the list of additional products that are accessible when SUSE Manager is PAYG
     */
    public static List<PaygProductInfo> listAdditionalProductsForSUMAPayg() {
        return SUSEProductFactory.findAllSUSEProducts().stream()
            .filter(p -> Objects.nonNull(p.getChannelFamily()) && Objects.nonNull(p.getArch()))
            .filter(p -> isSupportedToolsProduct(p) || isSupportedProxyProduct(p) || isSupportedOpenSUSEProduct(p))
            .map(p -> new PaygProductInfo(p.getName(), p.getVersion(), p.getArch().getLabel()))
            .collect(Collectors.toList());
    }

    private static boolean isSupportedToolsProduct(SUSEProduct product) {
        ChannelFamily family = product.getChannelFamily();
        ArchType architecture = product.getArch().getArchType();

        return ChannelFamilyFactory.TOOLS_CHANNEL_FAMILY_LABEL.equals(family.getLabel()) &&
            // TODO: deb not yet available on RMT
            PackageFactory.ARCH_TYPE_RPM.equals(architecture.getLabel());
    }

    private static boolean isSupportedProxyProduct(SUSEProduct product) {
        ChannelFamily family = product.getChannelFamily();
        String version = product.getVersion();

        // This exclude ALPHA and BETA versions
        return List.of(ChannelFamilyFactory.PROXY_CHANNEL_FAMILY_LABEL,
                        ChannelFamilyFactory.PROXY_ARM_CHANNEL_FAMILY_LABEL).contains(family.getLabel()) &&
            RPM_VERSION_COMPARATOR.compare(version, "4.3") >= 0;
    }

    private static boolean isSupportedOpenSUSEProduct(SUSEProduct product) {
        ChannelFamily family = product.getChannelFamily();

        return ChannelFamilyFactory.OPENSUSE_CHANNEL_FAMILY_LABEL.equals(family.getLabel());
    }
}
