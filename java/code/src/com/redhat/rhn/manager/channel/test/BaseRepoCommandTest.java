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
package com.redhat.rhn.manager.channel.test;

import static org.junit.jupiter.api.Assertions.fail;

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * BaseRepoCommandTest
 */
public class BaseRepoCommandTest extends RhnBaseTestCase {

    private BaseRepoCommand ccc = null;
    private int label_count = 0;
    private User user = null;

    @BeforeEach
    public void setUp() throws Exception {
        Long oid = UserTestUtils.createOrg("testOrg" + this.getClass().getSimpleName());
        user = UserTestUtils.createUser("testUser", oid);
        Org org = user.getOrg();
        ccc = new CreateRepoCommand(org);
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

    private void validRepoLabelInput(String label) {
        // give it a valid url
        ccc.setUrl("http://localhost/" + label_count++);
        // need to create unique label names.
        ccc.setLabel(label);
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

    private void invalidRepoLabelInput(String label) {
        // give it a valid url
        ccc.setUrl("http://localhost/");
        // need to create unique label names.
        ccc.setLabel(label);
        // need to specify a type
        ccc.setType("yum");
        // need to specify MetadataSigned
        ccc.setMetadataSigned(Boolean.FALSE);

        try {
            ccc.store();
            fail("invalid repository label should have thrown error: " + label);
        }
        catch (InvalidRepoUrlException e) {
            fail("non duplicate url caused error");
        }
        catch (InvalidRepoUrlInputException e) {
            fail("valid repo url input caused error");
        }
        catch (InvalidRepoLabelException e) {
            // expected
        }
        catch (InvalidRepoTypeException e) {
            fail("valid repo type caused error");
        }
    }
}
