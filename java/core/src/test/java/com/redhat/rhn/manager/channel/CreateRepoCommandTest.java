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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.redhat.rhn.common.client.InvalidCertificateException;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ContentSource;
import com.redhat.rhn.domain.channel.SslContentSource;
import com.redhat.rhn.domain.kickstart.crypto.SslCryptoKey;
import com.redhat.rhn.domain.kickstart.factory.KickstartFactoryTest;
import com.redhat.rhn.frontend.xmlrpc.channel.repo.InvalidRepoLabelException;
import com.redhat.rhn.frontend.xmlrpc.channel.repo.InvalidRepoUrlInputException;
import com.redhat.rhn.manager.channel.repo.CreateRepoCommand;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CreateRepoCommandTest extends BaseTestCaseWithUser {

    private CreateRepoCommand repoCommand;
    private int labelCount = 0;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        repoCommand = new CreateRepoCommand(user.getOrg());
    }

    @Test
    public void testVerifyUrlInput() {

        // Url must begin with a valid protocol (http, https, ftp, ...),
        //  end with a valid TLD and contain only valid characters

        // YUM - I N V A L I D
        invalidUrlInput("", "yum");
        invalidUrlInput("example.com", "yum");
        invalidUrlInput("htp://some_test_url.com", "yum");
        invalidUrlInput("www.example.com", "yum");

        // YUM - V A L I D
        validUrlInput("file:///srv/mirror", "yum");
        validUrlInput("http://localhost", "yum");
        validUrlInput("http://localhost:8080", "yum");
        validUrlInput("http://localhost/pub", "yum");
        validUrlInput("http://localhost.com.x/pub", "yum");
        validUrlInput("http://www.example.com", "yum");
        validUrlInput("http://www.example.co.uk", "yum");
        validUrlInput("https://www3.example.com", "yum");
        validUrlInput("https://test123.com", "yum");
        validUrlInput("http://example.com", "yum");
        validUrlInput("http://another-one.de/example", "yum");
        validUrlInput("https://another-one.de/example%20", "yum");
        validUrlInput("http://another-one.de/example?v=10", "yum");
        validUrlInput("ftp://exampleABC.com", "yum");

        // ULN - V A L I D
        validUrlInput("uln://foo/example_label", "uln");
        validUrlInput("uln:///example_label", "uln");

        // ULN - I N V A L I D
        invalidUrlInput("", "uln");
        invalidUrlInput("example.com", "uln");
        invalidUrlInput("htp://some_test_url.com", "uln");
        invalidUrlInput("www.example.com", "uln");
    }

    private void invalidUrlInput(String url, String type) {
        // give it an invalid url
        repoCommand.setUrl(url);
        // give it a valid label
        repoCommand.setLabel("valid-label-name");
        // need to specify a type
        repoCommand.setType(type);
        // need to specify MetadataSigned
        repoCommand.setMetadataSigned(Boolean.FALSE);

        assertThrows(InvalidRepoUrlInputException.class, () -> {
            repoCommand.store();
            TestUtils.flushAndClearSession();
        });
    }

    private void validUrlInput(String url, String type) {
        // give it a valid url
        repoCommand.setUrl(url);
        // need to create unique label names.
        repoCommand.setLabel("valid-label-name-" + labelCount++);
        // need to specify a type
        repoCommand.setType(type);
        // need to specify MetadataSigned
        repoCommand.setMetadataSigned(Boolean.FALSE);

        assertDoesNotThrow(() -> {
            repoCommand.store();
            TestUtils.flushAndClearSession();
        });
    }

    @Test
    public void testRepoLabelInput() {

        // Repository label must contain only letters, hyphens, periods, underscores and numeral

        // I N V A L I D
        invalidRepoLabelInput("");
        invalidRepoLabelInput("example;repo");
        invalidRepoLabelInput("example_repo$");
        invalidRepoLabelInput("my*examplerepo");
        invalidRepoLabelInput("example^repo");

        // V A L I D
        validRepoLabelInput("my example repo");
        validRepoLabelInput("my-example-repo");
        validRepoLabelInput("my-example_repo");
        validRepoLabelInput("my_repo_22");
        validRepoLabelInput("My-Example-Repo");
        validRepoLabelInput("My/Example/Repo/15");
        validRepoLabelInput("My_Example_Repo_15.5");
        validRepoLabelInput("My_Example_Repo_(15.5)");
        validRepoLabelInput("My_Example_Repo_'15.5'");
    }

    @Test
    public void canCreateRepoWithSSLData() throws InvalidCertificateException {
        SslCryptoKey caCert = KickstartFactoryTest.createTestSslKey(user.getOrg());
        SslCryptoKey sslClientCert = KickstartFactoryTest.createTestSslKey(user.getOrg());
        SslCryptoKey sslClientKey = KickstartFactoryTest.createTestSslKey(user.getOrg());

        repoCommand.setLabel("TestWitSSLData");
        repoCommand.setType("yum");
        repoCommand.setUrl("http://localhost");
        repoCommand.setMetadataSigned(false);
        repoCommand.addSslSet(caCert.getId(), sslClientCert.getId(), sslClientKey.getId());
        repoCommand.store();

        TestUtils.flushAndClearSession();

        ContentSource contentSource = ChannelFactory.lookupContentSource(repoCommand.getRepo().getId(), user.getOrg());
        assertNotNull(contentSource);
        assertNotNull(contentSource.getSslSets());
        assertEquals(1, contentSource.getSslSets().size(), "One SSL set must be associated with the content source");

        SslContentSource sslContentSource = contentSource.getSslSets().iterator().next();
        assertEquals(caCert.getId(), sslContentSource.getCaCert().getId(), "CA cert ID should match");
        assertEquals(sslClientCert.getId(), sslContentSource.getClientCert().getId(), "CA cert ID should match");
        assertEquals(sslClientKey.getId(), sslContentSource.getClientKey().getId(), "CA cert ID should match");
    }

    private void validRepoLabelInput(String label) {
        // give it a valid url
        repoCommand.setUrl("http://localhost/" + labelCount++);
        // need to create unique label names.
        repoCommand.setLabel(label);
        // need to specify a type
        repoCommand.setType("yum");
        // need to specify MetadataSigned
        repoCommand.setMetadataSigned(Boolean.FALSE);

        assertDoesNotThrow(() -> {
            repoCommand.store();
            TestUtils.flushAndClearSession();
        });
    }

    private void invalidRepoLabelInput(String label) {
        // give it a valid url
        repoCommand.setUrl("http://localhost/");
        // need to create unique label names.
        repoCommand.setLabel(label);
        // need to specify a type
        repoCommand.setType("yum");
        // need to specify MetadataSigned
        repoCommand.setMetadataSigned(Boolean.FALSE);

        assertThrows(InvalidRepoLabelException.class, () -> {
            repoCommand.store();
            TestUtils.flushAndClearSession();
        });
    }
}
