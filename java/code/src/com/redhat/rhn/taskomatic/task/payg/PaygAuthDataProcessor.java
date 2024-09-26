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

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ContentSource;
import com.redhat.rhn.domain.channel.SslContentSource;
import com.redhat.rhn.domain.cloudpayg.CloudRmtHost;
import com.redhat.rhn.domain.cloudpayg.CloudRmtHostFactory;
import com.redhat.rhn.domain.cloudpayg.PaygProductFactory;
import com.redhat.rhn.domain.cloudpayg.PaygSshData;
import com.redhat.rhn.domain.cloudpayg.PaygSshDataFactory;
import com.redhat.rhn.domain.credentials.CloudCredentials;
import com.redhat.rhn.domain.credentials.CloudRMTCredentials;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.RHUICredentials;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.crypto.SslCryptoKey;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.product.Tuple2;
import com.redhat.rhn.domain.scc.SCCRepositoryAuth;
import com.redhat.rhn.taskomatic.task.payg.beans.PaygInstanceInfo;
import com.redhat.rhn.taskomatic.task.payg.beans.PaygProductInfo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

        LOG.debug("Process data for {}", paygData.getType());
        if (paygData.getType().equals("CLOUDRMT")) {
            CloudRMTCredentials credentials = processAndGetCloudRMTCredentials(instance, paygData);
            LOG.debug("Number installed Products: {}", paygData.getProducts().size());

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
        else if (paygData.getType().equals("RHUI")) {
            LOG.debug("Number repositories: {}", paygData.getRepositories().size());
            long instanceId = instance.getId();
            Org org = OrgFactory.lookupById(ConfigDefaults.get().getRhuiDefaultOrgId());
            Map<String, SslCryptoKey> cryptoKeyMap = extractCryptoKeys(paygData, instanceId, org);

            // extract repositories from paygData and create or update them
            processRepositories(paygData, cryptoKeyMap, instance, org);
        }
    }

    private void processRepositories(PaygInstanceInfo paygData, Map<String, SslCryptoKey> cryptoKeyMap,
                                     PaygSshData instance, Org org) throws URISyntaxException {
        Set<String> newIdents = new HashSet<>();
        for (Map.Entry<String, Map<String, String>> repo : paygData.getRepositories().entrySet()) {
            boolean needCredentials = paygData.getHeaderAuth().containsKey("X-RHUI-ID");
            String repoIdent = repo.getKey() + "-i" + instance.getId();
            Map<String, String> repodata = repo.getValue();
            URI uri = new URI(repodata.get("url"));
            String queryString = uri.getQuery();

            if (needCredentials) {
                CloudCredentials credentials = processAndGetCredentials(instance, paygData);
                queryString = buildQueryString(uri.getQuery(), "credentials", "mirrcred_" + credentials.getId());
            }
            ContentSource contentSource =
                    Optional.ofNullable(ChannelFactory.lookupContentSourceByOrgAndLabel(org, repoIdent))
                            .orElseGet(() -> {
                                ContentSource cs = new ContentSource();
                                cs.setOrg(org);
                                cs.setLabel(repoIdent);
                                cs.setType(ChannelFactory.lookupContentSourceType("yum"));
                                cs.setMetadataSigned(false);
                                return cs;
                            });
            contentSource.setSourceUrl(new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(),
                    uri.getPath(), queryString, uri.getFragment()).toString());

            if (repodata.containsKey("sslclientcert") &&
                    cryptoKeyMap.containsKey(repodata.get("sslclientcert")) &&
                    repodata.containsKey("sslclientkey") &&
                    cryptoKeyMap.containsKey(repodata.get("sslclientkey")) &&
                    repodata.containsKey("sslcacert") &&
                    cryptoKeyMap.containsKey(repodata.get("sslcacert"))) {
                SslContentSource sslCs = new SslContentSource();
                sslCs.setClientCert(cryptoKeyMap.get(repodata.get("sslclientcert")));
                sslCs.setClientKey(cryptoKeyMap.get(repodata.get("sslclientkey")));
                sslCs.setCaCert(cryptoKeyMap.get(repodata.get("sslcacert")));
                contentSource.setSslSets(Set.of(sslCs));
            }
            else if (repodata.containsKey("sslclientcert") ||
                    repodata.containsKey("sslclientkey")) {
                LOG.error("Repository has incomplete client certificate values: {}", repoIdent);
                continue;
            }
            newIdents.add(repoIdent);
            ChannelFactory.save(contentSource);
        }
        // Cleanup unused repositories
        PaygSshDataFactory.listRhuiRepositoriesCreatedByInstance(instance)
                .stream()
                .filter(c -> !newIdents.contains(c.getLabel()))
                .forEach(ChannelFactory::remove);

    }

    private Map<String, SslCryptoKey> extractCryptoKeys(PaygInstanceInfo paygData, long instanceId, Org org) {
        Map<String, SslCryptoKey> cryptoKeyMap = new HashMap<>();

        for (Map.Entry<String, String> cert : paygData.getCertificates().entrySet()) {
            String filename = cert.getKey().substring(cert.getKey().lastIndexOf("/") + 1);
            String desc = String.format("RHUI %s %s (I%d)", "Client Certificate", filename, instanceId);
            if (cert.getValue().contains("PRIVATE KEY")) {
                // private key
                desc = String.format("RHUI %s %s (I%d)", "Private Key", filename, instanceId);
            }
            else if (filename.equals("Bundle")) {
                desc = String.format("RHUI %s %s (I%d)", "CA Certificate", filename, instanceId);
            }
            else {
                if (cert.getKey().contains("/product/")) {
                    // client certificate
                    desc = String.format("RHUI %s %s (I%d)", "Certificate", filename, instanceId);
                }
                else if (filename.endsWith(".crt")) {
                    // ca cert
                    desc = String.format("RHUI %s %s (I%d)", "CA Certificate", filename, instanceId);
                }
            }
            String cryptoKeyDesc = desc;
            SslCryptoKey cryptoKey =
                    (SslCryptoKey) Optional.ofNullable(KickstartFactory.lookupCryptoKey(cryptoKeyDesc, org))
                            .orElseGet(() -> {
                                SslCryptoKey sslkey = new SslCryptoKey();
                                sslkey.setCryptoKeyType(KickstartFactory.KEY_TYPE_SSL);
                                sslkey.setOrg(org);
                                sslkey.setDescription(cryptoKeyDesc);
                                return sslkey;
                            });
            cryptoKey.setKey(cert.getValue().getBytes(StandardCharsets.UTF_8));
            KickstartFactory.saveCryptoKey(cryptoKey);
            cryptoKeyMap.put(cert.getKey(), cryptoKey);
        }
        // crypto keys might change, but a cleanup will only happen when removing the instance
        return cryptoKeyMap;
    }

    private String buildQueryString(String query, String newKey, String newValue) {
        Map<String, String> queryparams = Arrays.stream(
                        Optional.ofNullable(query)
                                .orElse("")
                                .split("&"))
                .filter(p -> p.contains("=")) // filter out possible auth tokens
                .map(p -> {
                    String[] s = p.split("=", 2);
                    return new Tuple2<String, String>(s[0], s[1]);
                })
                .collect(Collectors.toMap(Tuple2::getA, Tuple2::getB));
        queryparams.put(newKey, newValue);
        return queryparams.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("CloudRmtHost Hostname: {}", rmtHost.getHost());
            LOG.debug("CloudRmtHost IP: {}", rmtHost.getIp());
            LOG.debug("CloudRmtHost SSL Cert: {}", rmtHost.getSslCert());
        }
    }

    private CloudRMTCredentials processAndGetCloudRMTCredentials(PaygSshData instance, PaygInstanceInfo paygData)
        throws URISyntaxException {
        if (!paygData.getType().equals("CLOUDRMT")) {
            throw new PaygDataExtractException("Instance data type is not CloudRMT PAYG");
        }

        final String username = paygData.getBasicAuth().get("username");
        final String password = paygData.getBasicAuth().get("password");
        URI rmtUri = new URI("https", paygData.getRmtHost().get("hostname"), "/repo", null);

        CloudCredentials credentialsIn = instance.getCredentials();
        CloudRMTCredentials credentials = Optional.ofNullable(credentialsIn)
            .flatMap(c -> c.castAs(CloudRMTCredentials.class))
            .map(creds -> {
                creds.setUsername(username);
                creds.setPassword(password);
                creds.setUrl(rmtUri.toString());
                return creds;
            })
            .orElseGet(() -> CredentialsFactory.createCloudRmtCredentials(username, password, rmtUri.toString()));


        if (paygData.getHeaderAuth() != null) {
            if (instance.isSUSEManagerPayg()) {
                // On SUSE Manager 5.0 we need to allow synchronize SUMA older version, like 4.3
                // SUMA 4.3 is a base product, means it also grants access to sles15sp4 base channels
                // To access older suma version we are using the authentication credentials collected from localhost
                // Localhost is sle-micro, and with those headers we cannot access SLES channels needed for 4.3
                // To overcome this we are forcing the header for OS identifier to be the ones used in suma 4.3
                // With the version set to 5.0
                // Then Public Team cloud team can detect that this is a SUMA machine,
                // with the right credentials and grant the needed access to all the channels
                paygData.getHeaderAuth().put("X-Instance-Identifier", "SUSE-Manager-Server");
                paygData.getHeaderAuth().put("X-Instance-Version", "5.0");
            }
            credentials.setExtraAuthData(GSON.toJson(paygData.getHeaderAuth()).getBytes());
        }
        credentials.setPaygSshData(instance);

        if (credentialsIn == null || !credentialsIn.equals(credentials)) {
            // storeCredentials update the modified date which should only be
            // done when the data really change as it would force a full
            // scc product refresh
            CredentialsFactory.storeCredentials(credentials);
        }

        PaygSshDataFactory.savePaygSshData(instance);
        return credentials;
    }

    private RHUICredentials processAndGetCredentials(PaygSshData instance, PaygInstanceInfo paygData) {
        if (!paygData.getType().equals("RHUI")) {
            throw new PaygDataExtractException("Instance data type is not RHUI");
        }

        RHUICredentials credentials = Optional.ofNullable(instance.getCredentials())
            .flatMap(c -> c.castAs(RHUICredentials.class))
            .orElseGet(() -> CredentialsFactory.createRhuiCredentials());
        if (paygData.getHeaderAuth() != null) {
            credentials.setExtraAuthData(GSON.toJson(paygData.getHeaderAuth()).getBytes());
        }
        credentials.setPaygSshData(instance);

        CredentialsFactory.storeCredentials(credentials);
        PaygSshDataFactory.savePaygSshData(instance);
        return credentials;
    }
    /**
     * Invalidate PAYG Instance credentials
     * @param instance the instance
     */
    public void invalidateCredentials(PaygSshData instance) {
        Optional.ofNullable(instance.getCredentials())
            .flatMap(c -> c.castAs(CloudRMTCredentials.class))
            .ifPresent(credentials -> {
                credentials.invalidate();
                CredentialsFactory.storeCredentials(credentials);
            });
    }

}
