/*
 * Copyright (c) 2023 SUSE LLC
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
package com.redhat.rhn.domain.image.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ImageSyncFactory;
import com.redhat.rhn.domain.image.ImageSyncItem;
import com.redhat.rhn.domain.image.ImageSyncProject;
import com.redhat.rhn.testing.ImageTestUtils;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ImageSyncProjectTest extends JMockBaseTestCaseWithUser {

    private ImageSyncFactory syncFactory;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        syncFactory = new ImageSyncFactory();
    }

    /**
     * Test listing ImageSync Projects
     */
    @Test
    public void testListProjects() {
        ImageStore srcStore = ImageTestUtils.createImageStore("externalRegistry", user);
        ImageStore destStore = ImageTestUtils.createImageStore("localRegistry", user);

        ImageSyncProject prj = new ImageSyncProject();
        prj.setName("test-project");
        prj.setOrg(user.getOrg());
        prj.setScoped(true);
        prj.setSrcStore(srcStore);
        prj.setDestinationImageStore(destStore);
        syncFactory.save(prj);

        ImageSyncItem item = new ImageSyncItem();
        item.setSrcRepository("/opensuse/leap");
        item.setSrcTagsRegexp("^15\\.4.*$");
        item.setOrg(user.getOrg());
        item.setImageSyncProject(prj);
        syncFactory.save(item);

        Long prjId = prj.getId();
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        List<ImageSyncProject> imageSyncProjects = syncFactory.listProjectsByUser(user);
        assertEquals(1, imageSyncProjects.size());

        Optional<ImageSyncProject> optProject = syncFactory.lookupProjectByIdAndUser(prjId, user);

        assertTrue(optProject.isPresent(), "Project not found");

        ImageSyncProject tPrj = imageSyncProjects.get(0);
        assertEquals("test-project", tPrj.getName());
        assertEquals(destStore, tPrj.getDestinationImageStore());
        assertEquals(srcStore, tPrj.getSrcStore());

        List<ImageSyncItem> syncItems = tPrj.getSyncItems();
        assertEquals(1, syncItems.size());

        ImageSyncItem tItems = syncItems.get(0);
        assertEquals("/opensuse/leap", tItems.getSrcRepository());
        assertEquals("^15\\.4.*$", tItems.getSrcTagsRegexp());
    }

    /**
     * Test listing ImageSync Projects
     */
    @Test
    public void testListProjectsMultipleSources() {
        ImageStore srcStore1 = ImageTestUtils.createImageStore("externalRegistry1", user);
        ImageStore srcStore2 = ImageTestUtils.createImageStore("externalRegistry2", user);
        ImageStore destStore = ImageTestUtils.createImageStore("localRegistry", user);

        ImageSyncProject prj1 = new ImageSyncProject("test-project2", user.getOrg(), srcStore1, destStore, true);
        syncFactory.save(prj1);

        ImageSyncProject prj2 = new ImageSyncProject("test-project3", user.getOrg(), srcStore2, destStore, true);
        syncFactory.save(prj2);

        ImageSyncItem item1 = new ImageSyncItem(prj1, user.getOrg(), "/opensuse/leap", "^15\\.4.*$");
        syncFactory.save(item1);

        ImageSyncItem item2 = new ImageSyncItem(prj2, user.getOrg(), "/suse/sles", Arrays.asList("15-SP4"));
        syncFactory.save(item2);

        ImageSyncItem item3 = new ImageSyncItem(prj1, user.getOrg(), "/suse/sles", Arrays.asList("15-SP4"),
                "^15-SP3.*$");
        syncFactory.save(item3);

        ImageSyncItem item4 = new ImageSyncItem(prj2, user.getOrg(), "redhat/rhel/9");
        syncFactory.save(item4);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        List<ImageSyncProject> imageSyncProjects = syncFactory.listProjectsByUser(user);
        assertEquals(2, imageSyncProjects.size());

        ImageSyncProject tPrj1 = imageSyncProjects.get(0);
        assertEquals("test-project2", tPrj1.getName());
        assertEquals(destStore, tPrj1.getDestinationImageStore());
        assertEquals(srcStore1, tPrj1.getSrcStore());

        List<ImageSyncItem> syncItems = tPrj1.getSyncItems();
        assertEquals(2, syncItems.size());

        for (ImageSyncItem tItem : syncItems) {
            if (tItem.equals(item1)) {
                assertEquals("/opensuse/leap", tItem.getSrcRepository());
                assertEquals("^15\\.4.*$", tItem.getSrcTagsRegexp());
                assertEquals(Collections.emptyList(), tItem.getSrcTags());
            }
            else if (tItem.equals(item3)) {
                assertEquals("/suse/sles", tItem.getSrcRepository());
                assertEquals("^15-SP3.*$", tItem.getSrcTagsRegexp());
                assertEquals(Arrays.asList("15-SP4"), tItem.getSrcTags());
            }
            else {
                assertFalse(true, String.format("Unexpected Source found %s", tItem.toString()));
            }
        }

        ImageSyncProject tPrj2 = imageSyncProjects.get(1);
        assertEquals("test-project3", tPrj2.getName());
        assertEquals(destStore, tPrj2.getDestinationImageStore());
        assertEquals(srcStore2, tPrj2.getSrcStore());

        syncItems = tPrj2.getSyncItems();
        assertEquals(2, syncItems.size());

        for (ImageSyncItem tItem : syncItems) {
            if (tItem.equals(item2)) {
                assertEquals("/suse/sles", tItem.getSrcRepository());
                assertNull(tItem.getSrcTagsRegexp());
                assertEquals(Arrays.asList("15-SP4"), tItem.getSrcTags());
            }
            else if (tItem.equals(item4)) {
                assertEquals("redhat/rhel/9", tItem.getSrcRepository());
                assertNull(tItem.getSrcTagsRegexp());
                assertEquals(Collections.emptyList(), tItem.getSrcTags());
            }
            else {
                assertFalse(true, String.format("Unexpected Source found %s", tItem.toString()));
            }
        }
    }
}
