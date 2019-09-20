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

        // I N V A L I D
        invalidUrlInput("");
        invalidUrlInput("example.com");
        invalidUrlInput("htp://some_test_url.com");
        invalidUrlInput("www.example.com");
        invalidUrlInput("http://test123/example.net");
        invalidUrlInput("ftp://example@test123.com");
        invalidUrlInput("http://example_1.com");
        invalidUrlInput("http://example#2.com");
        invalidUrlInput("http://example;3.com");
        invalidUrlInput("http://example:4.com");
        invalidUrlInput("http://example=5.com");
        invalidUrlInput("http://example,6.com");
        invalidUrlInput("http://example$7.com");
        invalidUrlInput("http://example$8.com");
        invalidUrlInput("http://example(9).com");
        invalidUrlInput("http://example[0].com");
        invalidUrlInput("http://example.123");


        // V A L I D
        validUrlInput("http://www.example.com");
        validUrlInput("http://www.example.co.uk");
        validUrlInput("https://www3.example.com");
        validUrlInput("https://test123.com");
        validUrlInput("http://example.com");
        validUrlInput("http://another-one.de/example");
        validUrlInput("https://another-one.de/example%20");
        validUrlInput("http://another-one.de/example?v=10");
        validUrlInput("ftp://exampleABC.com");
    }

    private void invalidUrlInput(String url) {
        // give it an invalid url
        ccc.setUrl(url);
        // give it a valid label
        ccc.setLabel("valid-label-name");
        // need to specify a type
        ccc.setType("yum");
        // need to specify MetadataSigned
        ccc.setMetadataSigned(Boolean.FALSE);

        try {
            ccc.store();
            fail("invalid url should have thrown error");
        }
        catch (InvalidRepoUrlException e) {
            fail("non duplicate url caused error");
        }
        catch (InvalidRepoUrlInputException expected) {
            // expected
        }
        catch (InvalidRepoLabelException e) {
            fail("valid repo label caused error");
        }
        catch (InvalidRepoTypeException e) {
            fail("valid repo type caused error");
        }
    }

    private void validUrlInput(String url) {
        // give it a valid url
        ccc.setUrl(url);
        // need to create unique label names.
        ccc.setLabel("valid-label-name-" + label_count++);
        // need to specify a type
        ccc.setType("yum");
        // need to specify MetadataSigned
        ccc.setMetadataSigned(Boolean.FALSE);

        try {
            ccc.store();
        }
        catch (InvalidRepoUrlException e) {
            fail("non duplicate url caused error");
        }
        catch (InvalidRepoUrlInputException e) {
            fail("valid repo url input caused error");
        }
        catch (InvalidRepoLabelException e) {
            fail("valid repo label caused error");
        }
        catch (InvalidRepoTypeException e) {
            fail("valid repo type caused error");
        }
    }
}
