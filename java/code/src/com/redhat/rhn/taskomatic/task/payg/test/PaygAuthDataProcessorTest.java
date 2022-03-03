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
package com.redhat.rhn.taskomatic.task.payg.test;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.cloudpayg.CloudRmtHost;
import com.redhat.rhn.domain.cloudpayg.CloudRmtHostFactory;
import com.redhat.rhn.domain.cloudpayg.PaygSshData;
import com.redhat.rhn.domain.cloudpayg.PaygSshDataFactory;
import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.product.SUSEProductSCCRepository;
import com.redhat.rhn.domain.product.test.SUSEProductTestUtils;
import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.domain.scc.SCCRepository;
import com.redhat.rhn.domain.scc.SCCRepositoryAuth;
import com.redhat.rhn.domain.scc.SCCRepositoryCloudRmtAuth;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.taskomatic.task.payg.PaygAuthDataProcessor;
import com.redhat.rhn.taskomatic.task.payg.beans.PaygInstanceInfo;
import com.redhat.rhn.taskomatic.task.payg.beans.PaygProductInfo;
import com.redhat.rhn.testing.TestUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PaygAuthDataProcessorTest extends BaseHandlerTestCase {

    private final PaygAuthDataProcessor paygDataProcessor = new PaygAuthDataProcessor();
    private PaygSshData paygData;
    private PaygInstanceInfo paygInstanceInfo;

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .create();

    private static final Map<String, Boolean> CHANNEL_SUFFIX = new HashMap<>();
    static {
        // key -> suffix; value -> is installer updates
        CHANNEL_SUFFIX.put("-Pool", false);
        CHANNEL_SUFFIX.put("-Updates", false);
        CHANNEL_SUFFIX.put("-debuginfo", false);
        CHANNEL_SUFFIX.put("Installer", true);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        paygData = createPaygSshData();
        PaygSshDataFactory.savePaygSshData(paygData);
        paygInstanceInfo = createPaygInstanceInfo();

        populateProducts();
    }

    public void testFirstExecution() throws URISyntaxException {
        paygDataProcessor.processPaygInstanceData(paygData, paygInstanceInfo);
        assertExpectedData();

    }

    public void testUpdateData() throws URISyntaxException {
        Credentials cred = CredentialsFactory.createCredentials("u", "p", Credentials.TYPE_CLOUD_RMT, null);
        cred.setUrl("//my_url");
        cred.setPaygSshData(paygData);
        paygData.setCredentials(cred);
        CredentialsFactory.storeCredentials(cred);

        CloudRmtHost cloudRmt = CloudRmtHostFactory.createCloudRmtHost();
        cloudRmt.setHost("name");
        cloudRmt.setIp("ip");
        cloudRmt.setSslCert("empty");
        cloudRmt.setPaygSshData(paygData);
        CloudRmtHostFactory.saveCloudRmtHost(cloudRmt);
        paygData.setRmtHosts(cloudRmt);

        PaygSshDataFactory.savePaygSshData(paygData);

        paygDataProcessor.processPaygInstanceData(paygData, paygInstanceInfo);
        assertExpectedData();
    }

    public void testUpdateRepos() throws URISyntaxException {
        Credentials cred = CredentialsFactory.createCredentials("u", "p", Credentials.TYPE_CLOUD_RMT, null);
        cred.setUrl("//my_url");
        cred.setPaygSshData(paygData);
        paygData.setCredentials(cred);
        CredentialsFactory.storeCredentials(cred);
        PaygSshDataFactory.savePaygSshData(paygData);

        SUSEProduct productSleHa = SUSEProductFactory.lookupByProductId(1324);
        CHANNEL_SUFFIX.keySet().forEach(suffix -> SCCCachingFactory
                .lookupRepositoryByName(productSleHa.getName() + "-" + productSleHa.getVersion() + suffix)
                .ifPresent(repo-> {
                    SCCRepositoryCloudRmtAuth newAuth = new SCCRepositoryCloudRmtAuth();
                    newAuth.setRepo(repo);
                    newAuth.setCredentials(cred);
                    SCCCachingFactory.saveRepositoryAuth(newAuth);
                }));
        assertEquals(4, SCCCachingFactory.lookupRepositoryAuthByCredential(cred).size());

        paygDataProcessor.processPaygInstanceData(paygData, paygInstanceInfo);
        assertExpectedData();
    }

    private void assertExpectedData() {
        paygData = HibernateFactory.reload(paygData);

        assertEquals(9, SCCCachingFactory.lookupRepositoryAuth().size());
        assertEquals(1, HibernateFactory.getSession()
                .createQuery("SELECT a FROM Credentials a", Credentials.class).getResultList().size());
        assertEquals(1, CloudRmtHostFactory.lookupCloudRmtHosts().size());

        assertNotNull(paygData.getCredentials());
        assertNotNull(paygData.getCredentials().getExtraAuthData());
        Map<String, String> extraAuthData = GSON.fromJson(new String(paygData.getCredentials().getExtraAuthData()),
                Map.class);
        assertEquals(extraAuthData.size(), 1);
        assertEquals(extraAuthData.get("X-Instance-Data"), "PGRvY3VtZW50PnsKICAiYWNjb3VudElkIiA6ICI2NDEwODAwN");
        assertEquals(paygData.getCredentials().getUsername(), paygInstanceInfo.getBasicAuth().get("username"));
        assertEquals(paygData.getCredentials().getPassword(), paygInstanceInfo.getBasicAuth().get("password"));
        assertEquals(paygData.getCredentials().getUrl(),
                "https://" + paygInstanceInfo.getRmtHost().get("hostname") + "/repo");

        List<SCCRepositoryAuth> repoAuths = SCCCachingFactory.
                lookupRepositoryAuthByCredential(paygData.getCredentials());
        assertEquals(repoAuths.size(), 9);
        assertTrue(repoAuths.stream().anyMatch(r -> r.getRepo().getName().equals("sles-12.1-Pool")));
        assertTrue(repoAuths.stream().anyMatch(r -> r.getRepo().getName().equals("sles-12.1-Updates")));
        assertTrue(repoAuths.stream().anyMatch(r -> r.getRepo().getName().equals("sles-12.1-debuginfo")));
        assertTrue(repoAuths.stream().anyMatch(r -> r.getRepo().getName().equals("sles-15.1-Pool")));
        assertTrue(repoAuths.stream().anyMatch(r -> r.getRepo().getName().equals("sles-15.1-Updates")));
        assertTrue(repoAuths.stream().anyMatch(r -> r.getRepo().getName().equals("sles-15.1-debuginfo")));
        assertTrue(repoAuths.stream().anyMatch(r -> r.getRepo().getName().equals("sle-module-basesystem-15.1-Pool")));
        assertTrue(repoAuths.stream().anyMatch(r -> r.getRepo().getName()
                .equals("sle-module-basesystem-15.1-Updates")));
        assertTrue(repoAuths.stream().anyMatch(r -> r.getRepo().getName()
                .equals("sle-module-basesystem-15.1-debuginfo")));

        assertNotNull(paygData.getRmtHosts());
        assertEquals(paygInstanceInfo.getRmtHost().get("ip"), paygData.getRmtHosts().getIp());
        assertEquals(paygInstanceInfo.getRmtHost().get("hostname"), paygData.getRmtHosts().getHost());
        assertEquals(paygInstanceInfo.getRmtHost().get("server_ca"), paygData.getRmtHosts().getSslCert());
    }

    private void populateProducts() {
        SUSEProductTestUtils.createVendorSUSEProducts();
        SUSEProductFactory.findAllSUSEProducts()
                .forEach(suseProduct -> CHANNEL_SUFFIX.forEach((suffix, installer) -> createProductRepo(suseProduct,
                            createTestRepo(suseProduct.getName() + "-" +
                                    suseProduct.getVersion() + suffix, installer))));
    }

    private SUSEProductSCCRepository createProductRepo(SUSEProduct suseProduct, SCCRepository sccRepository) {
        SUSEProductSCCRepository productRepo = new SUSEProductSCCRepository();
        productRepo.setProduct(suseProduct);
        productRepo.setRootProduct(suseProduct);
        productRepo.setRepository(sccRepository);
        productRepo.setChannelLabel(sccRepository.getName());
        productRepo.setParentChannelLabel(sccRepository.getName());
        productRepo.setChannelName(sccRepository.getDescription());
        productRepo.setMandatory(true);
        return TestUtils.saveAndReload(productRepo);
    }

    private SCCRepository createTestRepo(String name, boolean installerUpdates) {
        SCCRepository repo = new SCCRepository();
        repo.setSccId(new Random().nextLong());
        repo.setDescription(name);
        repo.setDistroTarget(TestUtils.randomString());
        repo.setName(name);
        repo.setUrl("https://dummy.domain.top/" + name);
        repo.setAutorefresh(true);
        repo.setInstallerUpdates(installerUpdates);
        return TestUtils.saveAndReload(repo);
    }

    private PaygSshData createPaygSshData() {
        PaygSshData paygSshData = PaygSshDataFactory.createPaygSshData();
        paygSshData.setDescription("My special instance");
        paygSshData.setHost("my-instance");
        paygSshData.setPort(21);
        paygSshData.setUsername("username");
        paygSshData.setPassword("password");
        paygSshData.setKey("key");
        paygSshData.setKeyPassword("keyPassword");
        paygSshData.setErrorMessage("My status");
        return paygSshData;
    }

    private PaygInstanceInfo createPaygInstanceInfo() {
        List<PaygProductInfo> products = new LinkedList<>();
        products.add(new PaygProductInfo("SLES", "15.2", "x86_64"));
        products.add(new PaygProductInfo("sle-module-basesystem", "15.2", "x86_64"));

        Map<String, String> basicAuth = new HashMap<>();
        basicAuth.put("username", "SCC_05c394f");
        basicAuth.put("password", "0e248802");

        String headerAuth = "X-Instance-Data:PGRvY3VtZW50PnsKICAiYWNjb3VudElkIiA6ICI2NDEwODAwN";

        Map<String, String> rmtHost = new HashMap<>();
        rmtHost.put("hostname", "smt-ec2.susecloud.net");
        rmtHost.put("ip", "18.156.40.199");
        rmtHost.put("server_ca", "-----BEGIN CERTIFICATE-----");

        PaygInstanceInfo info = new PaygInstanceInfo(products, basicAuth, headerAuth, rmtHost);
        return info;
    }
}
