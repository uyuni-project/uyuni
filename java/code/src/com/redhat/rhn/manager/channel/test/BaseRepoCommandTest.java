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

    public void testRepoLabelInput() {

        // Repository label must contain only letters, hyphens, periods, underscores and numeral

        // I N V A L I D
        invalidRepoLabelInput("");
        invalidRepoLabelInput("example/repo");
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
        catch (InvalidRepoLabelException e) {
            fail("valid repo label caused error");
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
        catch (InvalidRepoLabelException e) {
            // expected
        }
    }
}
