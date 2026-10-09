/*
 * Copyright (c) 2019 SUSE LLC
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
package com.redhat.rhn.manager.channel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.client.InvalidCertificateException;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ContentSource;
import com.redhat.rhn.domain.channel.SslContentSource;
import com.redhat.rhn.domain.kickstart.crypto.SslCryptoKey;
import com.redhat.rhn.domain.kickstart.factory.KickstartFactoryTest;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.manager.channel.repo.EditRepoCommand;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EditRepoCommandTest extends BaseTestCaseWithUser {

    private EditRepoCommand repoCommand;

    private Long contentSourceId;

    @BeforeEach
    public void setUp() throws Exception {

        contentSourceId = createInitialContentSource(user.getOrg());

        repoCommand = new EditRepoCommand(user, contentSourceId);
    }

    @Test
    public void canModifySSLData() throws InvalidCertificateException {
        SslCryptoKey caCert = createTestSslKey();
        SslCryptoKey sslClientCert = createTestSslKey();
        SslCryptoKey sslClientKey = createTestSslKey();

        repoCommand.addSslContentSource(caCert.getId(), sslClientCert.getId(), sslClientKey.getId());
        repoCommand.store();

        TestUtils.flushAndClearSession();

        ContentSource contentSource = ChannelFactory.lookupContentSource(contentSourceId, user.getOrg());
        assertNotNull(contentSource);
        assertNotNull(contentSource.getSslContentSources());
        assertEquals(1, contentSource.getSslContentSources().size(),
            "One SSL set must be associated with the content source");

        SslContentSource sslContentSource = contentSource.getSslContentSources().iterator().next();
        assertEquals(caCert.getId(), sslContentSource.getCaCert().getId(), "CA cert ID should match");
        assertEquals(sslClientCert.getId(), sslContentSource.getClientCert().getId(), "CA cert ID should match");
        assertEquals(sslClientKey.getId(), sslContentSource.getClientKey().getId(), "CA cert ID should match");

        // Ensure we can also remove the ssl settings
        repoCommand.deleteAllSslContentSources();
        repoCommand.store();

        TestUtils.flushAndClearSession();

        contentSource = ChannelFactory.lookupContentSource(contentSourceId, user.getOrg());
        assertNotNull(contentSource);
        assertTrue(CollectionUtils.isEmpty(contentSource.getSslContentSources()),
                "No SSL data should be associated with the content source after deletion");
    }

    /**
     * Test changing the SSL client certificate of an existing repository
     */
    @Test
    public void canChangeSslClientCertOfExistingRepo() throws Exception {
        SslCryptoKey caCert = createTestSslKey();
        SslCryptoKey clientCert = createTestSslKey();
        SslCryptoKey clientKey = createTestSslKey();
        SslCryptoKey newClientCert = createTestSslKey();

        submitSslChange(caCert.getId(), clientCert.getId(), clientKey.getId());
        submitSslChange(caCert.getId(), newClientCert.getId(), clientKey.getId());

        assertSslSet(caCert, newClientCert, clientKey);
    }

    /**
     * Test changing the SSL client key of an existing repository
     */
    @Test
    public void canChangeSslClientKeyOfExistingRepo() throws Exception {
        SslCryptoKey caCert = createTestSslKey();
        SslCryptoKey clientCert = createTestSslKey();
        SslCryptoKey clientKey = createTestSslKey();
        SslCryptoKey newClientKey = createTestSslKey();

        submitSslChange(caCert.getId(), clientCert.getId(), clientKey.getId());
        submitSslChange(caCert.getId(), clientCert.getId(), newClientKey.getId());

        assertSslSet(caCert, clientCert, newClientKey);
    }

    /**
     * Test setting SSL client certificate and key when none are currently set
     */
    @Test
    public void canSetSslClientCertAndKeyFromNone() throws Exception {
        SslCryptoKey caCert = createTestSslKey();
        SslCryptoKey clientCert = createTestSslKey();
        SslCryptoKey clientKey = createTestSslKey();

        submitSslChange(caCert.getId(), null, null);
        submitSslChange(caCert.getId(), clientCert.getId(), clientKey.getId());

        assertSslSet(caCert, clientCert, clientKey);
    }

    /**
     * Test resetting SSL client certificate and key to none
     */
    @Test
    public void canResetSslClientCertAndKeyToNone() throws Exception {
        SslCryptoKey caCert = createTestSslKey();
        SslCryptoKey clientCert = createTestSslKey();
        SslCryptoKey clientKey = createTestSslKey();

        submitSslChange(caCert.getId(), clientCert.getId(), clientKey.getId());
        submitSslChange(caCert.getId(), null, null);

        assertSslSet(caCert, null, null);
    }

    /**
     * Test resubmitting the same SSL set
     */
    @Test
    public void canResubmitUnchangedSslSet() throws Exception {
        SslCryptoKey caCert = createTestSslKey();
        SslCryptoKey clientCert = createTestSslKey();
        SslCryptoKey clientKey = createTestSslKey();

        submitSslChange(caCert.getId(), clientCert.getId(), clientKey.getId());
        submitSslChange(caCert.getId(), clientCert.getId(), clientKey.getId());

        assertSslSet(caCert, clientCert, clientKey);
    }

    /**
     * Test adding more SSL sets than are removed
     */
    @Test
    public void canAddMoreSslSetsThanRemoved() throws Exception {
        SslCryptoKey caCert = createTestSslKey();
        SslCryptoKey otherCaCert = createTestSslKey();
        SslCryptoKey evenOtherCaCert = createTestSslKey();
        SslCryptoKey clientCert = createTestSslKey();
        SslCryptoKey clientKey = createTestSslKey();

        submitSslSets(List.of(sslTuple(caCert, null, null)));
        submitSslSets(List.of(
                sslTuple(caCert, clientCert, clientKey),
                sslTuple(otherCaCert, null, null),
                sslTuple(evenOtherCaCert, null, null)
        ));

        assertSslSets(Set.of(
                sslTuple(caCert, clientCert, clientKey),
                sslTuple(otherCaCert, null, null),
                sslTuple(evenOtherCaCert, null, null)
        ));
    }

    /**
     * Test removing more SSL sets than are added
     */
    @Test
    public void canRemoveMoreSslSetsThanAdded() throws Exception {
        SslCryptoKey caCert = createTestSslKey();
        SslCryptoKey otherCaCert = createTestSslKey();
        SslCryptoKey evenOtherCaCert = createTestSslKey();
        SslCryptoKey clientCert = createTestSslKey();
        SslCryptoKey clientKey = createTestSslKey();

        submitSslSets(List.of(
                sslTuple(caCert, null, null),
                sslTuple(otherCaCert, null, null),
                sslTuple(evenOtherCaCert, null, null))
        );
        submitSslSets(List.of(sslTuple(caCert, clientCert, clientKey)));

        assertSslSets(Set.of(sslTuple(caCert, clientCert, clientKey)));
    }

    /**
     * Mimics a submit of the repository details page (RepoDetailsAction): a fresh command is built for the
     * existing repository, all SSL sets are dropped and the one from the form is added before storing.
     */
    private void submitSslChange(Long caCertId, Long clientCertId, Long clientKeyId) throws Exception {
        submitSslSets(List.of(Arrays.asList(caCertId, clientCertId, clientKeyId)));
    }

    /**
     * Same as {@link #submitSslChange(Long, Long, Long)}, but replacing the SSL sets with all the given
     * (CA cert id, client cert id, client key id) tuples.
     */
    private void submitSslSets(List<List<Long>> sslSets) throws Exception {
        ContentSource current = ChannelFactory.lookupContentSource(contentSourceId, user.getOrg());

        EditRepoCommand cmd = new EditRepoCommand(user, contentSourceId);
        cmd.setLabel(current.getLabel());
        cmd.setUrl(current.getSourceUrl());
        cmd.setType(current.getType().getLabel());
        cmd.setMetadataSigned(current.getMetadataSigned());
        cmd.deleteAllSslContentSources();
        for (List<Long> sslSet : sslSets) {
            cmd.addSslContentSource(sslSet.get(0), sslSet.get(1), sslSet.get(2));
        }
        cmd.store();

        TestUtils.flushAndClearSession();
    }

    private void assertSslSet(SslCryptoKey caCert, SslCryptoKey clientCert, SslCryptoKey clientKey) {
        ContentSource contentSource = ChannelFactory.lookupContentSource(contentSourceId, user.getOrg());
        assertNotNull(contentSource);
        assertEquals(1, contentSource.getSslContentSources().size(),
            "One SSL set must be associated with the content source");

        SslContentSource ssl = contentSource.getSslContentSources().iterator().next();
        assertEquals(caCert.getId(), ssl.getCaCert().getId(), "CA cert ID should match");
        assertEquals(idOf(clientCert), idOf(ssl.getClientCert()), "Client cert ID should match");
        assertEquals(idOf(clientKey), idOf(ssl.getClientKey()), "Client key ID should match");
    }

    private void assertSslSets(Set<List<Long>> expected) {
        ContentSource contentSource = ChannelFactory.lookupContentSource(contentSourceId, user.getOrg());
        assertNotNull(contentSource);

        Set<List<Long>> actual = contentSource.getSslContentSources().stream()
            .map(ssl -> sslTuple(ssl.getCaCert(), ssl.getClientCert(), ssl.getClientKey()))
            .collect(Collectors.toSet());
        assertEquals(expected.size(), contentSource.getSslContentSources().size(),
            "Unexpected number of SSL sets associated with the content source");
        assertEquals(expected, actual, "SSL sets should match");
    }

    private static List<Long> sslTuple(SslCryptoKey caCert, SslCryptoKey clientCert, SslCryptoKey clientKey) {
        return Arrays.asList(idOf(caCert), idOf(clientCert), idOf(clientKey));
    }

    private static Long idOf(SslCryptoKey key) {
        return key == null ? null : key.getId();
    }

    private SslCryptoKey createTestSslKey() {
        return KickstartFactoryTest.createTestSslKey(user.getOrg());
    }

    private static long createInitialContentSource(Org org) {
        String randomLabel = RandomStringUtils.insecure().nextAlphabetic(6);

        ContentSource contentSource = new ContentSource();
        contentSource.setLabel("TestRepo" + randomLabel);
        contentSource.setSourceUrl("https://test.repo." + randomLabel);
        contentSource.setType(ChannelFactory.lookupContentSourceType("yum"));
        contentSource.setOrg(org);
        contentSource.setMetadataSigned(false);
        TestUtils.persist(contentSource);
        TestUtils.flushAndClearSession();

        assertNotNull(contentSource.getId(), "ContentSource id should not be null after persisting");
        return contentSource.getId();
    }
}
