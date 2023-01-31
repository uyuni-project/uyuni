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
package com.redhat.rhn.manager.image.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ImageSyncProject;
import com.redhat.rhn.domain.image.ImageSyncSource;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.manager.image.ImageSyncManager;
import com.redhat.rhn.testing.ImageTestUtils;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ImageSyncManagerTest extends JMockBaseTestCaseWithUser {
    private ImageSyncManager syncManager;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        syncManager = new ImageSyncManager();
        user.addPermanentRole(RoleFactory.IMAGE_ADMIN);
    }

    @Test
    public void testCreateProjectAndLookup() {
        ImageStore destStore = ImageTestUtils.createImageStore("localRegistry", user);
        ImageSyncProject project = syncManager.createProject("test-project", destStore.getId(), true, user);
        assertNotNull(project.getId());

        Optional<ImageSyncProject> optProject = syncManager.lookupProject(project.getId(), user);
        assertEquals(project, optProject.get());

        Optional<ImageSyncProject> optProject2 = syncManager.lookupProject("test-project", user);
        assertEquals(project, optProject2.get());

        List<ImageSyncProject> prjs = syncManager.listProjects(user);
        assertEquals(1, prjs.size());
    }

    @Test
    public void testPermissions() {
        ImageStore destStore = ImageTestUtils.createImageStore("localRegistry", user);

        user.removePermanentRole(RoleFactory.IMAGE_ADMIN);
        assertThrows(PermissionException.class,
                () -> syncManager.createProject("test-project", destStore.getId(), true, user));

        user.addPermanentRole(RoleFactory.IMAGE_ADMIN);
        ImageSyncProject project = syncManager.createProject("test-project", destStore.getId(), true, user);
        assertNotNull(project.getId());

        user.removePermanentRole(RoleFactory.IMAGE_ADMIN);
        assertThrows(PermissionException.class,
                () -> syncManager.lookupProject(project.getId(), user));
        assertThrows(PermissionException.class,
                () -> syncManager.lookupProject("test-project", user));
        assertThrows(PermissionException.class,
                () -> syncManager.listProjects(user));
    }

    @Test
    public void testCreateProjectWithSource() {
        ImageStore destStore = ImageTestUtils.createImageStore("localRegistry", user);
        ImageStore srcStore = ImageTestUtils.createImageStore("externalRegistry", user);
        ImageSyncProject project = syncManager.createProject("test-project", destStore.getId(), true, user);
        assertNotNull(project.getId());

        ImageSyncSource source = syncManager.createSource(project.getId(), srcStore.getId(), "/suse/sles",
                Arrays.asList("15-SP4"), "^12-SP.*$", user);
        assertNotNull(source.getId());

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        Optional<ImageSyncProject> optProject = syncManager.lookupProject(project.getId(), user);
        assertEquals(project, optProject.get());

        List<ImageSyncSource> syncSources = optProject.get().getSyncSources();
        assertEquals(1, syncSources.size());
        assertEquals(source, syncSources.get(0));
    }

    @Test
    public void testUpdateProjectAndSource() {
        ImageStore destStore = ImageTestUtils.createImageStore("localRegistry", user);
        ImageStore newDestStore = ImageTestUtils.createImageStore("newLocalRegistry", user);
        ImageStore srcStore = ImageTestUtils.createImageStore("externalRegistry", user);
        ImageStore newSrcStore = ImageTestUtils.createImageStore("newExternalRegistry", user);

        ImageSyncProject project = syncManager.createProject("test-project", destStore.getId(), true, user);
        assertNotNull(project.getId());

        ImageSyncSource source = syncManager.createSource(project.getId(), srcStore.getId(), "/suse/sles",
                Arrays.asList("15-SP4"), "^12-SP.*$", user);
        assertNotNull(source.getId());

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        ImageSyncProject dbProject = syncManager.lookupProject(project.getId(), user).orElseThrow();
        assertEquals(project, dbProject);

        syncManager.updateProject(dbProject.getId(), user, newDestStore.getId(), null);
        syncManager.updateProject(dbProject.getId(), user, null, false);

        List<ImageSyncSource> syncSources = dbProject.getSyncSources();
        assertEquals(1, syncSources.size());
        ImageSyncSource dbSource = syncSources.get(0);

        syncManager.updateSource(dbSource.getId(), user, newSrcStore.getId(), null, null, null);
        syncManager.updateSource(dbSource.getId(), user, null, "/opensuse/leap", null, null);
        syncManager.updateSource(dbSource.getId(), user, null, null, Arrays.asList("15.3", "15.4"), null);
        syncManager.updateSource(dbSource.getId(), user, null, null, null, "^42\\..*$");

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        dbProject = syncManager.lookupProject(project.getId(), user).orElseThrow();
        assertEquals(newDestStore, dbProject.getDestinationImageStore());
        assertEquals(false, dbProject.isScoped());

        dbSource = dbProject.getSyncSources().get(0);
        assertEquals(newSrcStore, dbSource.getSrcStore());
        assertEquals("/opensuse/leap", dbSource.getSrcRepository());
        assertEquals(Arrays.asList("15.3", "15.4"), dbSource.getSrcTags());
        assertEquals("^42\\..*$", dbSource.getSrcTagsRegexp());
    }

    @Test
    public void testDeleteProject() {
        ImageStore destStore = ImageTestUtils.createImageStore("localRegistry", user);
        ImageStore srcStore = ImageTestUtils.createImageStore("externalRegistry", user);
        ImageSyncProject project = syncManager.createProject("test-project", destStore.getId(), true, user);
        assertNotNull(project.getId());

        ImageSyncSource source = syncManager.createSource(project.getId(), srcStore.getId(), "/suse/sles",
                Arrays.asList("15-SP4"), "^12-SP.*$", user);
        assertNotNull(source.getId());

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        Optional<ImageSyncProject> optProject = syncManager.lookupProject(project.getId(), user);
        assertEquals(project, optProject.get());

        List<ImageSyncSource> syncSources = optProject.get().getSyncSources();
        Long oldSourceId = syncSources.get(0).getId();

        syncManager.deleteProject("test-project", user);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        assertEquals(Optional.empty(), syncManager.lookupProject("test-project", user));
        assertEquals(0, syncManager.listSources(user).size());
    }

    @Test
    public void testDeleteSource() {
        ImageStore destStore = ImageTestUtils.createImageStore("localRegistry", user);
        ImageStore srcStore1 = ImageTestUtils.createImageStore("externalRegistry1", user);
        ImageStore srcStore2 = ImageTestUtils.createImageStore("externalRegistry2", user);
        ImageSyncProject project = syncManager.createProject("test-project", destStore.getId(), true, user);
        assertNotNull(project.getId());

        ImageSyncSource source1 = syncManager.createSource(project.getId(), srcStore1.getId(), "/suse/sles",
                Arrays.asList("15-SP4"), "^12-SP.*$", user);
        assertNotNull(source1.getId());

        ImageSyncSource source2 = syncManager.createSource(project.getId(), srcStore2.getId(), "/opensuse/leap",
                null, "^15\\..*$", user);
        assertNotNull(source2.getId());

        Long sourceId1 = source1.getId();

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        syncManager.deleteSource(sourceId1, user);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        Optional<ImageSyncProject> optProject = syncManager.lookupProject(project.getId(), user);
        assertTrue(optProject.isPresent());

        List<ImageSyncSource> syncSources = optProject.get().getSyncSources();
        assertEquals(1, syncSources.size());
        assertEquals(1, syncManager.listSources(user).size());

        assertEquals(source2, syncSources.get(0));
    }
}
