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
import com.redhat.rhn.domain.cloudpayg.PaygSshData;
import com.redhat.rhn.domain.cloudpayg.PaygSshDataFactory;
import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.crypto.SslCryptoKey;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.product.Tuple2;
import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.domain.scc.SCCRepository;
import com.redhat.rhn.domain.scc.SCCRepositoryAuth;
import com.redhat.rhn.domain.scc.SCCRepositoryCloudRmtAuth;
import com.redhat.rhn.taskomatic.task.payg.beans.PaygInstanceInfo;
import com.redhat.rhn.taskomatic.task.payg.beans.PaygProductInfo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
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
        Credentials credentials = processAndGetCredentials(instance, paygData);
        if (paygData.getType().equals("CLOUDRMT")) {
            LOG.debug("Number installed Products: {}", paygData.getProducts().size());
            List<SCCRepositoryAuth> existingRepos = SCCCachingFactory.lookupRepositoryAuthByCredential(credentials);

            List<SCCRepository> repositories = getReposToInsert(paygData.getProducts());

            List<SCCRepositoryAuth> processedRepoAuth = new ArrayList<>();
            repositories.forEach(sccRepo -> {
                SCCRepositoryAuth authRepo = existingRepos.stream()
                        .filter(r -> r.getRepo().getId().equals(sccRepo.getId()))
                        .findFirst().orElseGet(() -> {
                            SCCRepositoryCloudRmtAuth newAuth = new SCCRepositoryCloudRmtAuth();
                            newAuth.setRepo(sccRepo);
                            return newAuth;
                        });

                authRepo.setCredentials(credentials);
                // Update content source URL, since it should be pointing to a Credentials record,
                // and it may have changed
                if (authRepo.getContentSource() != null) {
                    authRepo.getContentSource().setSourceUrl(authRepo.getUrl());
                }

                SCCCachingFactory.saveRepositoryAuth(authRepo);
                processedRepoAuth.add(authRepo);
            });

            LOG.debug("Total repository authentication inserted: {}", processedRepoAuth.size());
            existingRepos.stream()
                    .filter(er -> processedRepoAuth.stream().noneMatch(pr -> er.getId().equals(pr.getId())))
                    .forEach(SCCCachingFactory::deleteRepositoryAuth);

            processCloudRmtHost(instance, paygData);
        }
        else if (paygData.getType().equals("RHUI")) {
            LOG.debug("Number repositories: {}", paygData.getRepositories().size());
            long credentialsId = credentials.getId();
            Org org = OrgFactory.lookupById(ConfigDefaults.get().getRhuiDefaultOrgId());

            String dCert = String.format("Red Hat Update Infrastructure %s (C%d)", "Client Certificate", credentialsId);
            SslCryptoKey clientCert = (SslCryptoKey) Optional.ofNullable(KickstartFactory.lookupCryptoKey(dCert, org))
                    .orElseGet(() -> {
                        SslCryptoKey sslkey = new SslCryptoKey();
                        sslkey.setCryptoKeyType(KickstartFactory.KEY_TYPE_SSL);
                        sslkey.setOrg(org);
                        sslkey.setDescription(dCert);
                        return sslkey;
                    });
            clientCert.setKey(paygData.getClientCertificate().getBytes(StandardCharsets.UTF_8));
            KickstartFactory.saveCryptoKey(clientCert);

            String dKey = String.format("Red Hat Update Infrastructure %s (C%d)", "Client Key", credentialsId);
            SslCryptoKey clientKey = (SslCryptoKey) Optional.ofNullable(KickstartFactory.lookupCryptoKey(dKey, org))
                    .orElseGet(() -> {
                        SslCryptoKey sslkey = new SslCryptoKey();
                        sslkey.setCryptoKeyType(KickstartFactory.KEY_TYPE_SSL);
                        sslkey.setOrg(org);
                        sslkey.setDescription(dKey);
                        return sslkey;
                    });
            clientKey.setKey(paygData.getClientKey().getBytes(StandardCharsets.UTF_8));
            KickstartFactory.saveCryptoKey(clientKey);

            String dCa = String.format("Red Hat Update Infrastructure %s (C%d)", "CA Certificate", credentialsId);
            SslCryptoKey caCert = (SslCryptoKey) Optional.ofNullable(KickstartFactory.lookupCryptoKey(dCa, org))
                    .orElseGet(() -> {
                        SslCryptoKey sslkey = new SslCryptoKey();
                        sslkey.setCryptoKeyType(KickstartFactory.KEY_TYPE_SSL);
                        sslkey.setOrg(org);
                        sslkey.setDescription(dCa);
                        return sslkey;
                    });
            caCert.setKey(paygData.getCaCertificate().getBytes(StandardCharsets.UTF_8));
            KickstartFactory.saveCryptoKey(caCert);

            SslContentSource sslCs = new SslContentSource();
            sslCs.setClientCert(clientCert);
            sslCs.setClientKey(clientKey);
            sslCs.setCaCert(caCert);

            for (Map.Entry<String, String> repo : paygData.getRepositories().entrySet()) {
                String repoIdent = repo.getKey() + "-c" + credentialsId;
                String repoUrl = repo.getValue();
                URI uri = new URI(repoUrl);
                String q = buildQueryString(uri.getQuery(), "credentials", "mirrcred_" + credentialsId);

                ContentSource contentSource = Optional.ofNullable(
                        ChannelFactory.lookupContentSourceByOrgAndLabel(org, repoIdent)).orElseGet(() -> {
                    ContentSource cs = new ContentSource();
                    cs.setOrg(org);
                    cs.setLabel(repoIdent);
                    cs.setType(ChannelFactory.lookupContentSourceType("yum"));
                    cs.setMetadataSigned(false);
                    return cs;
                });
                contentSource.setSourceUrl(new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(),
                        uri.getPath(), q, uri.getFragment()).toString());
                contentSource.setSslSets(Set.of(sslCs));
                ChannelFactory.save(contentSource);
            }
        }
    }

    private String buildQueryString(String query, String newKey, String newValue) {
            Map<String, String> queryparams = Arrays.stream(Optional.ofNullable(query).orElse("").split("&"))
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
    }

    private Credentials processAndGetCredentials(PaygSshData instance, PaygInstanceInfo paygData)
            throws URISyntaxException {

        if (paygData.getType().equals("CLOUDRMT")) {
            final String username = paygData.getBasicAuth().get("username");
            final String password = paygData.getBasicAuth().get("password");
            Credentials credentials = Optional.ofNullable(instance.getCredentials()).orElseGet(() ->
                    CredentialsFactory.createCredentials(username, password, Credentials.TYPE_CLOUD_RMT, null));

            credentials.setUsername(username);
            credentials.setPassword(password);

            URI credentialsURI = new URI("https", paygData.getRmtHost().get("hostname"), "/repo", null);
            credentials.setUrl(credentialsURI.toString());

            if (paygData.getHeaderAuth() != null) {
                credentials.setExtraAuthData(GSON.toJson(paygData.getHeaderAuth()).getBytes());
            }
            credentials.setPaygSshData(instance);

            CredentialsFactory.storeCredentials(credentials);

            instance.setCredentials(credentials);
            PaygSshDataFactory.savePaygSshData(instance);
            return credentials;
        }
        else if (paygData.getType().equals("RHUI")) {
            Credentials credentials = Optional.ofNullable(instance.getCredentials()).orElseGet(() ->
                    CredentialsFactory.createCredentials("RHUI", " ", Credentials.TYPE_RHUI, null));
            if (paygData.getHeaderAuth() != null) {
                credentials.setExtraAuthData(GSON.toJson(paygData.getHeaderAuth()).getBytes());
            }
            credentials.setPaygSshData(instance);

            CredentialsFactory.storeCredentials(credentials);

            instance.setCredentials(credentials);
            PaygSshDataFactory.savePaygSshData(instance);
            return credentials;
        }
        throw new PaygDataExtractException("Unknown data type: " + paygData.getType());
    }

    private List<SCCRepository> getReposToInsert(List<PaygProductInfo> products) {
        return products.stream().map(product ->
            SCCCachingFactory.lookupRepositoriesByProductNameAndArchForPayg(product.getName(), product.getArch())
        ).flatMap(List::stream).collect(Collectors.toList());
    }
}
