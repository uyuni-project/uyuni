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
import com.redhat.rhn.domain.image.ImageSyncProject;
import com.redhat.rhn.domain.image.ImageSyncSource;
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
        prj.setDestinationImageStore(destStore);
        syncFactory.save(prj);

        ImageSyncSource src = new ImageSyncSource();
        src.setSrcRepository("/opensuse/leap");
        src.setSrcTagsRegexp("^15\\.4.*$");
        src.setOrg(user.getOrg());
        src.setSrcStore(srcStore);
        src.setImageSyncProject(prj);
        syncFactory.save(src);

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

        List<ImageSyncSource> syncSources = tPrj.getSyncSources();
        assertEquals(1, syncSources.size());

        ImageSyncSource tSrc = syncSources.get(0);
        assertEquals("/opensuse/leap", tSrc.getSrcRepository());
        assertEquals("^15\\.4.*$", tSrc.getSrcTagsRegexp());
        assertEquals(srcStore, tSrc.getSrcStore());
    }

    /**
     * Test listing ImageSync Projects
     */
    @Test
    public void testListProjectsMultipleSources() {
        ImageStore srcStore1 = ImageTestUtils.createImageStore("externalRegistry1", user);
        ImageStore srcStore2 = ImageTestUtils.createImageStore("externalRegistry2", user);
        ImageStore destStore = ImageTestUtils.createImageStore("localRegistry", user);

        ImageSyncProject prj = new ImageSyncProject("test-project2", user.getOrg(), destStore, true);
        syncFactory.save(prj);

        ImageSyncSource src1 = new ImageSyncSource(prj, user.getOrg(), srcStore1, "/opensuse/leap",
                "^15\\.4.*$");
        syncFactory.save(src1);

        ImageSyncSource src2 = new ImageSyncSource(prj, user.getOrg(), srcStore2, "/suse/sles",
                Arrays.asList("15-SP4"));
        syncFactory.save(src2);

        ImageSyncSource src3 = new ImageSyncSource(prj, user.getOrg(), srcStore1, "/suse/sles",
                Arrays.asList("15-SP4"), "^15-SP3.*$");
        syncFactory.save(src3);

        ImageSyncSource src4 = new ImageSyncSource(prj, user.getOrg(), srcStore2, "redhat/rhel/9");
        syncFactory.save(src4);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        List<ImageSyncProject> imageSyncProjects = syncFactory.listProjectsByUser(user);
        assertEquals(1, imageSyncProjects.size());

        ImageSyncProject tPrj = imageSyncProjects.get(0);
        assertEquals("test-project2", tPrj.getName());
        assertEquals(destStore, tPrj.getDestinationImageStore());

        List<ImageSyncSource> syncSources = tPrj.getSyncSources();
        assertEquals(4, syncSources.size());

        for (ImageSyncSource tSrc : syncSources) {
            if (tSrc.equals(src1)) {
                assertEquals("/opensuse/leap", tSrc.getSrcRepository());
                assertEquals("^15\\.4.*$", tSrc.getSrcTagsRegexp());
                assertEquals(Collections.emptyList(), tSrc.getSrcTags());
                assertEquals(srcStore1, tSrc.getSrcStore());
            }
            else if (tSrc.equals(src2)) {
                assertEquals("/suse/sles", tSrc.getSrcRepository());
                assertNull(tSrc.getSrcTagsRegexp());
                assertEquals(Arrays.asList("15-SP4"), tSrc.getSrcTags());
                assertEquals(srcStore2, tSrc.getSrcStore());
            }
            else if (tSrc.equals(src3)) {
                assertEquals("/suse/sles", tSrc.getSrcRepository());
                assertEquals("^15-SP3.*$", tSrc.getSrcTagsRegexp());
                assertEquals(Arrays.asList("15-SP4"), tSrc.getSrcTags());
                assertEquals(srcStore1, tSrc.getSrcStore());
            }
            else if (tSrc.equals(src4)) {
                assertEquals("redhat/rhel/9", tSrc.getSrcRepository());
                assertNull(tSrc.getSrcTagsRegexp());
                assertEquals(Collections.emptyList(), tSrc.getSrcTags());
                assertEquals(srcStore2, tSrc.getSrcStore());
            }
            else {
                assertFalse(true, String.format("Unexpected Source found %s", tSrc.toString()));
            }
        }
    }
}
