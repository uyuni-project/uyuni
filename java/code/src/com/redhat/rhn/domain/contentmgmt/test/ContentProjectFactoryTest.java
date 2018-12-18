/**
 * Copyright (c) 2018 SUSE LLC
 *
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

package com.redhat.rhn.domain.contentmgmt.test;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.contentmgmt.ContentEnvironment;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import java.util.List;
import java.util.Set;

/**
 * Tests for {@link com.redhat.rhn.domain.contentmgmt.ContentProjectFactory}
 */
public class ContentProjectFactoryTest extends BaseTestCaseWithUser {

    public void testEnvironment() {
        ContentProjectFactory contentProjectFactory = ContentProjectFactory.getInstance();
        ContentProject cp = new ContentProject();
        cp.setLabel("project1");
        cp.setName("Project 1");
        cp.setDescription("This is project 1");
        cp.setOrg(user.getOrg());
        contentProjectFactory.save(cp);

        ContentEnvironment envdev = new ContentEnvironment();
        envdev.setLabel("dev");
        envdev.setName("Development");
        envdev.setContentProject(cp);
        contentProjectFactory.save(envdev);

        ContentEnvironment envtest = new ContentEnvironment();
        envtest.setLabel("test");
        envtest.setName("Test");
        envtest.setContentProject(cp);
        contentProjectFactory.save(envtest);

        envdev.setNextEnvironment(envtest);
        contentProjectFactory.save(envdev);
        HibernateFactory.getSession().flush();

        ContentProject p1 = contentProjectFactory.lookupContentProjectByLabel("project1");
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

    /**
     * Tests saving a ContentProject and listing it from the DB.
     */
    public void testSaveAndList() {
        ContentProject cp = new ContentProject();
        cp.setLabel("cplabel");
        cp.setDescription("cpdesc");
        cp.setName("cpname");
        cp.setOrg(user.getOrg());
        ContentProjectFactory.getInstance().save(cp);

        Org org2 = OrgFactory.createOrg();
        org2.setName("test org for content project");
        HibernateFactory.getSession().save(org2);
        ContentProject cp2 = new ContentProject();
        cp2.setLabel("cplabel2");
        cp2.setName("cpname2");
        cp2.setOrg(org2);
        ContentProjectFactory.getInstance().save(cp2);

        List<ContentProject> contentProjects = ContentProjectFactory.getInstance().listContentProjects(user.getOrg());
        assertEquals(1, contentProjects.size());
        ContentProject fromDb = contentProjects.get(0);
        assertNotNull(fromDb.getId());
        assertEquals(cp.getLabel(), fromDb.getLabel());
        assertEquals(cp.getDescription(), fromDb.getDescription());
        assertEquals(cp.getName(), fromDb.getName());
        assertEquals(cp.getOrg(), fromDb.getOrg());
    }

}
