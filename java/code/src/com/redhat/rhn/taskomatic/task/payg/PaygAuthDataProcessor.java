/*
 * Copyright (c) 2021 SUSE LLC
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

package com.redhat.rhn.taskomatic.task.payg;

import com.redhat.rhn.domain.cloudpayg.CloudRmtHost;
import com.redhat.rhn.domain.cloudpayg.CloudRmtHostFactory;
import com.redhat.rhn.domain.cloudpayg.PaygProductFactory;
import com.redhat.rhn.domain.cloudpayg.PaygSshData;
import com.redhat.rhn.domain.cloudpayg.PaygSshDataFactory;
import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.scc.SCCRepositoryAuth;
import com.redhat.rhn.taskomatic.task.payg.beans.PaygInstanceInfo;
import com.redhat.rhn.taskomatic.task.payg.beans.PaygProductInfo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PaygAuthDataProcessor {

    private static final Logger LOG = LogManager.getLogger(PaygAuthDataProcessor.class);


    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .create();

    /**
     * Will process the authentication data and cryptographic material and save it on the database.
     * It also processes the list of instance installed products,
     * and will find to which channels the authentication data have access to.
     * @param instance payg ssh data connection object
     * @param paygData Authentication data and cryptographic material to connect to cloud rmt host
     * @throws URISyntaxException
     */
    public void processPaygInstanceData(PaygSshData instance, PaygInstanceInfo paygData) throws URISyntaxException {

        LOG.debug("Number installed Products: {}", paygData.getProducts().size());
        Credentials credentials = processAndGetCredentials(instance, paygData);

        // Update the PAYG products associated with the credentials
        LOG.debug("Associating the installed products with the credentials # {}", credentials.getId());
        PaygProductFactory.updateProducts(credentials, paygData.getProducts());

        // Add the Tools and Proxy products that are accessible when in SUMA Payg environment
        List<PaygProductInfo> products = new LinkedList<>(paygData.getProducts());
        if (instance.isSUSEManagerPayg()) {
            products.addAll(PaygProductFactory.listAdditionalProductsForSUMAPayg());
        }

        // Update the authorizations for accessing the product repositories
        List<SCCRepositoryAuth> repoAuths = PaygProductFactory.refreshRepositoriesAuths(credentials, products);
        LOG.debug("Total repository authentication processed: {}", repoAuths.size());

        // Store the information about the Cloud RMT server
        processCloudRmtHost(instance, paygData);
    }

    private void processCloudRmtHost(PaygSshData instance, PaygInstanceInfo paygData) {
        CloudRmtHost rmtHost = Optional.ofNullable(instance.getRmtHosts())
                .orElseGet(CloudRmtHostFactory::createCloudRmtHost);
        rmtHost.setHost(paygData.getRmtHost().get("hostname"));
        rmtHost.setIp(paygData.getRmtHost().get("ip"));
        rmtHost.setSslCert(paygData.getRmtHost().get("server_ca"));
        rmtHost.setPaygSshData(instance);
        CloudRmtHostFactory.saveCloudRmtHost(rmtHost);

        instance.setRmtHosts(rmtHost);
        PaygSshDataFactory.savePaygSshData(instance);
    }

    private Credentials processAndGetCredentials(PaygSshData instance, PaygInstanceInfo paygData)
            throws URISyntaxException {

        final String username = paygData.getBasicAuth().get("username");
        final String password = paygData.getBasicAuth().get("password");
        Credentials credentialsIn = instance.getCredentials();
        Credentials credentials = Optional.ofNullable(instance.getCredentials()).orElseGet(() ->
                CredentialsFactory.createCredentials(username, password, Credentials.TYPE_CLOUD_RMT));

        credentials.setUsername(username);
        credentials.setPassword(password);

        URI credentialsURI = new URI("https", paygData.getRmtHost().get("hostname"), "/repo", null);
        credentials.setUrl(credentialsURI.toString());

        List<String> paygDataHeaders = paygData.getHeaders();
        if (CollectionUtils.isNotEmpty(paygDataHeaders)) {
            Map<String, String> headers = new HashMap<>();

            paygDataHeaders.stream()
                           .filter(header -> header.contains(":"))
                           .map(header -> header.split(":", 2))
                           .forEach(split -> headers.put(split[0], split[1]));

            credentials.setExtraAuthData(GSON.toJson(headers).getBytes());
        }
        credentials.setPaygSshData(instance);

        if (credentialsIn == null || !credentialsIn.equals(credentials)) {
            // storeCredentials update the modified date which should only be
            // done when the data really change as it would force a full
            // scc product refresh
            CredentialsFactory.storeCredentials(credentials);
        }

        instance.setCredentials(credentials);
        PaygSshDataFactory.savePaygSshData(instance);

        return credentials;
    }
    /**
     * Invalidate PAYG Instance credentials
     * @param instance the instance
     */
    public void invalidateCredentials(PaygSshData instance) {
        Optional.ofNullable(instance.getCredentials())
                .ifPresent(c -> {
                    Map<String, String> headers = new HashMap<>();
                    c.setExtraAuthData(GSON.toJson(headers).getBytes());
                    c.setPassword("invalidated");
                    CredentialsFactory.storeCredentials(c);
                });
    }

}
