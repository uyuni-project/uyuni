/**
 * Copyright (c) 2019 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.manager.channel.test;

import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.channel.repo.InvalidRepoLabelException;
import com.redhat.rhn.frontend.xmlrpc.channel.repo.InvalidRepoTypeException;
import com.redhat.rhn.frontend.xmlrpc.channel.repo.InvalidRepoUrlException;
import com.redhat.rhn.frontend.xmlrpc.channel.repo.InvalidRepoUrlInputException;
import com.redhat.rhn.manager.channel.repo.BaseRepoCommand;
import com.redhat.rhn.manager.channel.repo.CreateRepoCommand;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.UserTestUtils;

/**
 * BaseRepoCommandTest
 */
public class BaseRepoCommandTest extends RhnBaseTestCase {

    private BaseRepoCommand ccc = null;
    private int label_count = 0;
    private User user = null;

    public void setUp() throws Exception {
        super.setUp();
        Long oid = UserTestUtils.createOrg("testOrg" + this.getClass().getSimpleName());
        user = UserTestUtils.createUser("testUser", oid);
        Org org = user.getOrg();
        ccc = new CreateRepoCommand(org);
    }

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
        ccc.setUrl(url);
        // give it a valid label
        ccc.setLabel("valid-label-name");
        // need to specify a type
        ccc.setType(type);
        // need to specify MetadataSigned
        ccc.setMetadataSigned(Boolean.FALSE);

        try {
            ccc.store();
            fail("invalid url should have thrown error: " + url);
        }
        catch (InvalidRepoUrlException e) {
            fail("non duplicate url caused error: " + url);
        }
        catch (InvalidRepoUrlInputException expected) {
            // expected
        }
        catch (InvalidRepoLabelException e) {
            fail("valid repo label caused error: " + url);
        }
        catch (InvalidRepoTypeException e) {
            fail("valid repo type caused error: " + url);
        }
    }

    private void validUrlInput(String url, String type) {
        // give it a valid url
        ccc.setUrl(url);
        // need to create unique label names.
        ccc.setLabel("valid-label-name-" + label_count++);
        // need to specify a type
        ccc.setType(type);
        // need to specify MetadataSigned
        ccc.setMetadataSigned(Boolean.FALSE);

        try {
            ccc.store();
        }
        catch (InvalidRepoUrlException e) {
            fail("non duplicate url caused error: " + url);
        }
        catch (InvalidRepoUrlInputException e) {
            fail("valid repo url input caused error: " + url);
        }
        catch (InvalidRepoLabelException e) {
            fail("valid repo label caused error: " + url);
        }
        catch (InvalidRepoTypeException e) {
            fail("valid repo type caused error: " + url);
        }
    }
}
