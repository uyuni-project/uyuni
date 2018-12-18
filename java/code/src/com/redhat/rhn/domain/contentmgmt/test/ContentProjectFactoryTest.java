/**
 * Copyright (c) 2018 SUSE LLC
 * <p>
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 * <p>
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.domain.contentmgmt.test;

import com.redhat.rhn.domain.contentmgmt.ContentEnvironment;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import java.util.Set;

public class ContentProjectFactoryTest extends BaseTestCaseWithUser {

    //todo
    public void testFoo() {
        ContentProject cp = new ContentProject();
        cp.setLabel("cplabel");
        cp.setDescription("cpdesc");

        ContentProjectFactory.getInstance().save(cp);

        System.out.println(ContentProjectFactory.getInstance());
    }

    public void testEnvironment() throws Exception {
        ContentProjectFactory contentProdjectFactory = ContentProjectFactory.getInstance();
        ContentProject cp = new ContentProject();
        cp.setLabel("project1");
        cp.setDescription("This is project 1");
        contentProdjectFactory.save(cp);

        ContentEnvironment envdev = new ContentEnvironment();
        envdev.setLabel("dev");
        envdev.setName("Development");
        envdev.setContentProject(cp);
        contentProdjectFactory.save(envdev);

        ContentEnvironment envtest = new ContentEnvironment();
        envtest.setLabel("test");
        envtest.setName("Test");
        envtest.setContentProject(cp);
        contentProdjectFactory.save(envtest);

        envdev.setNextEnvironment(envtest);
        contentProdjectFactory.save(envdev);

        ContentProject p1 = contentProdjectFactory.lookupContentProjectByLabel("project1");
        assertEquals("project1", p1.getLabel());
        assertEquals("This is project 1", p1.getDescription());

        Set<ContentEnvironment> envs = p1.getEnvironments();
        ContentEnvironment first = envs.iterator().next();
        if (first.getLabel().equals("dev")) {
            assertEquals("Development", first.getName());
            ContentEnvironment next = first.getNextEnvironment();
            assertEquals("test", next.getLabel());
            assertEquals("Test", next.getName());
            assertTrue(next.getPrevEnvironment().equals(first));
        }
        else if (first.getLabel().equals("test")) {
            assertEquals("Test", first.getName());
            ContentEnvironment prev = first.getPrevEnvironment();
            assertEquals("dev", prev.getLabel());
            assertEquals("Development", prev.getName());
            assertTrue(prev.getNextEnvironment().equals(first));
        }
    }
}
