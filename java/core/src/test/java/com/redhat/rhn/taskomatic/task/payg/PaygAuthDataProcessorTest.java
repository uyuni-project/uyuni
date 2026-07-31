/*
 * Copyright (c) 2021--2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.taskomatic.task.payg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.cloudpayg.CloudRmtHost;
import com.redhat.rhn.domain.cloudpayg.CloudRmtHostFactory;
import com.redhat.rhn.domain.cloudpayg.PaygSshData;
import com.redhat.rhn.domain.cloudpayg.PaygSshDataFactory;
import com.redhat.rhn.domain.credentials.BaseCredentials;
import com.redhat.rhn.domain.credentials.CloudCredentials;
import com.redhat.rhn.domain.credentials.CloudRMTCredentials;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.product.ChannelTemplate;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.product.SUSEProductTestUtils;
import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.domain.scc.SCCRepository;
import com.redhat.rhn.domain.scc.SCCRepositoryCloudRmtAuth;
import com.redhat.rhn.frontend.xmlrpc.BaseHandlerTestCase;
import com.redhat.rhn.taskomatic.task.payg.beans.PaygInstanceInfo;
import com.redhat.rhn.taskomatic.task.payg.beans.PaygProductInfo;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class PaygAuthDataProcessorTest extends BaseHandlerTestCase {

    private final PaygAuthDataProcessor paygDataProcessor = new PaygAuthDataProcessor();
    private PaygSshData paygData;
    private PaygInstanceInfo paygInstanceInfo;

    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
        .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
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

    @BeforeEach
    public void setUp() throws Exception {
        populateProducts();
    }

    @Test
    public void testFirstExecution() throws URISyntaxException {

        paygData = PaygSshDataFactory.savePaygSshData(createPaygSshData("My special instance"));
        paygInstanceInfo = createPaygInstanceInfo();

        paygDataProcessor.processPaygInstanceData(paygData, paygInstanceInfo);
        paygDataProcessor.processPaygInstanceData(paygData, paygInstanceInfo);

        assertExpectedData();
    }

    @Test
    public void testUpdateData() throws URISyntaxException {
        paygData = PaygSshDataFactory.savePaygSshData(createPaygSshData("My special instance"));
        paygInstanceInfo = createPaygInstanceInfo();

        CloudRMTCredentials cred = CredentialsFactory.createCloudRmtCredentials("user", "password", "//my_url");
        cred.setPaygSshData(paygData);
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

    @Test
    public void testUpdateRepos() throws URISyntaxException {
        paygData = PaygSshDataFactory.savePaygSshData(createPaygSshData("My special instance"));
        paygInstanceInfo = createPaygInstanceInfo();

        CloudRMTCredentials cred = CredentialsFactory.createCloudRmtCredentials("user", "password", "//my_url");
        cred.setPaygSshData(paygData);
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

    @Test
    public void canExtractFromMultiplePaygInstances() throws Exception {
        // First iteration creates the credential, second one reuses and updates them
        for (int i = 0; i < 2; i++) {
            Map.of("MLM PAYG", "server.json", "SLES 15 SP6", "sles.json", "RHEL 10", "rhui.json")
                .entrySet()
                .stream()
                .map(entry -> {
                    var sshData = PaygSshDataFactory.lookupByHostname(asHostName(entry.getKey())).orElse(null);
                    if (sshData == null) {
                        sshData = PaygSshDataFactory.savePaygSshData(createPaygSshData(entry.getKey()));
                    }

                    var serverInstanceInfo = parseResource(entry.getValue());

                    return Pair.of(sshData, serverInstanceInfo);
                })
                .forEach(entry -> {
                    try {
                        paygDataProcessor.processPaygInstanceData(entry.getKey(), entry.getValue());
                    }
                    catch (Exception ex) {
                        fail("Unable to process and save payg data", ex);
                    }
                });

            TestUtils.flushAndClearSession();
        }
    }

    private void assertExpectedData() {
        PaygSshData data = TestUtils.reload(paygData);
        assertEquals(12, SCCCachingFactory.lookupRepositoryAuth().size());
        assertEquals(1, HibernateFactory.getSession()
                .createQuery("SELECT a FROM BaseCredentials a", BaseCredentials.class).getResultList().size());
        assertEquals(1, CloudRmtHostFactory.lookupCloudRmtHosts().size());

        CloudCredentials cloudCredentials = data.getCredentials();
        assertNotNull(cloudCredentials);
        assertNotNull(cloudCredentials.getExtraAuthData());

        Map<String, String> extraAuthData = GSON.fromJson(new String(cloudCredentials.getExtraAuthData()),
                Map.class);
        assertEquals(extraAuthData.size(), 1);
        assertEquals(extraAuthData.get("X-Instance-Data"), "PGRvY3VtZW50PnsKICAiYWNjb3VudElkIiA6ICI2NDEwODAwN");
        Optional<CloudRMTCredentials> cloudRMTCredentials = cloudCredentials.castAs(CloudRMTCredentials.class);
        assertTrue(cloudRMTCredentials.isPresent());
        assertEquals(cloudRMTCredentials.get().getUsername(), paygInstanceInfo.getBasicAuth().get("username"));
        assertEquals(cloudRMTCredentials.get().getPassword(), paygInstanceInfo.getBasicAuth().get("password"));
        assertEquals(cloudRMTCredentials.get().getUrl(),
                "https://" + paygInstanceInfo.getRmtHost().get("hostname") + "/repo");

        Set<String> repoNamesSet = SCCCachingFactory.lookupRepositoryAuthByCredential(cloudCredentials)
            .stream()
            .map(r -> r.getRepo().getName())
            .collect(Collectors.toSet());

        assertEquals(repoNamesSet.size(), 12);
        TestUtils.assertContains(repoNamesSet, "sles-12.1-Pool");
        TestUtils.assertContains(repoNamesSet, "sles-12.1-Updates");
        TestUtils.assertContains(repoNamesSet, "sles-12.1-debuginfo");
        TestUtils.assertContains(repoNamesSet, "sles-15.1-Pool");
        TestUtils.assertContains(repoNamesSet, "sles-15.1-Updates");
        TestUtils.assertContains(repoNamesSet, "sles-15.1-debuginfo");
        TestUtils.assertContains(repoNamesSet, "sle-module-basesystem-15.1-Pool");
        TestUtils.assertContains(repoNamesSet, "sle-module-basesystem-15.1-Updates");
        TestUtils.assertContains(repoNamesSet, "sle-module-basesystem-15.1-debuginfo");
        TestUtils.assertContains(repoNamesSet, "sle-module-basesystem-15.5-Pool");
        TestUtils.assertContains(repoNamesSet, "sle-module-basesystem-15.5-Updates");
        TestUtils.assertContains(repoNamesSet, "sle-module-basesystem-15.5-debuginfo");

        assertNotNull(data.getRmtHosts());
        assertEquals(paygInstanceInfo.getRmtHost().get("ip"), data.getRmtHosts().getIp());
        assertEquals(paygInstanceInfo.getRmtHost().get("hostname"), data.getRmtHosts().getHost());
        assertEquals(paygInstanceInfo.getRmtHost().get("server_ca"), data.getRmtHosts().getSslCert());
    }

    private void populateProducts() {
        SUSEProductTestUtils.createVendorSUSEProducts();
        SUSEProductFactory.findAllSUSEProducts()
                .forEach(suseProduct -> CHANNEL_SUFFIX.forEach((suffix, installer) -> createChannelTemplate(suseProduct,
                            createTestRepo(suseProduct.getName() + "-" +
                                    suseProduct.getVersion() + suffix, installer))));
    }

    private ChannelTemplate createChannelTemplate(SUSEProduct suseProduct, SCCRepository sccRepository) {
        ChannelTemplate productRepo = new ChannelTemplate();
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

    private PaygSshData createPaygSshData(String name) {
        PaygSshData paygSshData = PaygSshDataFactory.createPaygSshData();
        paygSshData.setDescription(name);
        paygSshData.setHost(asHostName(name));
        paygSshData.setPort(22);
        paygSshData.setUsername("username");
        paygSshData.setPassword("password");
        paygSshData.setKey("key");
        paygSshData.setKeyPassword("keyPassword");
        paygSshData.setErrorMessage("My status");
        return paygSshData;
    }

    private String asHostName(String name) {
        return name.toLowerCase().replace(" ", "-");
    }

    private PaygInstanceInfo createPaygInstanceInfo() {
        List<PaygProductInfo> products = new LinkedList<>();
        products.add(new PaygProductInfo("SLES", "15.2", "x86_64"));
        products.add(new PaygProductInfo("sle-module-basesystem", "15.2", "x86_64"));

        Map<String, String> basicAuth = new HashMap<>();
        basicAuth.put("username", "SCC_05c394f");
        basicAuth.put("password", "0e248802");

        Map<String, String> headerAuth = new HashMap<>();
        headerAuth.put("X-Instance-Data", "PGRvY3VtZW50PnsKICAiYWNjb3VudElkIiA6ICI2NDEwODAwN");

        Map<String, String> rmtHost = new HashMap<>();
        rmtHost.put("hostname", "smt-ec2.susecloud.net");
        rmtHost.put("ip", "18.156.40.199");
        rmtHost.put("server_ca", "-----BEGIN CERTIFICATE-----");

        return new PaygInstanceInfo(products, basicAuth, headerAuth, rmtHost);
    }

    private static PaygInstanceInfo parseResource(String resource) {
        try (var stream = PaygAuthDataProcessorTest.class.getResourceAsStream(resource);
             var reader = new InputStreamReader(Objects.requireNonNull(stream), StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, PaygInstanceInfo.class);
        }
        catch (Exception ex) {
            return fail("Unable to load PaygInstanceInfo instance from resource " + resource, ex);
        }
    }
}
