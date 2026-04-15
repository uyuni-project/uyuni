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

public class EditRepoCommandTest extends BaseTestCaseWithUser {

    private EditRepoCommand repoCommand;

    private Long contentSourceId;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        contentSourceId = createInitialContentSource(user.getOrg());

        repoCommand = new EditRepoCommand(user, contentSourceId);
    }

    @Test
    public void canModifySSLData() throws InvalidCertificateException {
        SslCryptoKey caCert = KickstartFactoryTest.createTestSslKey(user.getOrg());
        SslCryptoKey sslClientCert = KickstartFactoryTest.createTestSslKey(user.getOrg());
        SslCryptoKey sslClientKey = KickstartFactoryTest.createTestSslKey(user.getOrg());

        repoCommand.addSslSet(caCert.getId(), sslClientCert.getId(), sslClientKey.getId());
        repoCommand.store();

        TestUtils.flushAndClearSession();

        ContentSource contentSource = ChannelFactory.lookupContentSource(contentSourceId, user.getOrg());
        assertNotNull(contentSource);
        assertNotNull(contentSource.getSslSets());
        assertEquals(1, contentSource.getSslSets().size(), "One SSL set must be associated with the content source");

        SslContentSource sslContentSource = contentSource.getSslSets().iterator().next();
        assertEquals(caCert.getId(), sslContentSource.getCaCert().getId(), "CA cert ID should match");
        assertEquals(sslClientCert.getId(), sslContentSource.getClientCert().getId(), "CA cert ID should match");
        assertEquals(sslClientKey.getId(), sslContentSource.getClientKey().getId(), "CA cert ID should match");

        // Ensure we can also remove the ssl settings
        repoCommand.deleteAllSslSets();
        repoCommand.store();

        TestUtils.flushAndClearSession();

        contentSource = ChannelFactory.lookupContentSource(contentSourceId, user.getOrg());
        assertNotNull(contentSource);
        assertTrue(CollectionUtils.isEmpty(contentSource.getSslSets()),
                "No SSL data should be associated with the content source after deletion");
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
