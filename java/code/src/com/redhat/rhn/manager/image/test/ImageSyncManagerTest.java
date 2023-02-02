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
import com.redhat.rhn.domain.image.ImageSyncItem;
import com.redhat.rhn.domain.image.ImageSyncProject;
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
        ImageStore srcStore = ImageTestUtils.createImageStore("externalRegistry", user);
        ImageStore destStore = ImageTestUtils.createImageStore("localRegistry", user);
        ImageSyncProject project = syncManager.createProject("test-project", srcStore.getId(), destStore.getId(),
                true, user);
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
        ImageStore srcStore = ImageTestUtils.createImageStore("externalRegistry", user);
        ImageStore destStore = ImageTestUtils.createImageStore("localRegistry", user);

        user.removePermanentRole(RoleFactory.IMAGE_ADMIN);
        assertThrows(PermissionException.class,
                () -> syncManager.createProject("test-project", srcStore.getId(), destStore.getId(), true, user));

        user.addPermanentRole(RoleFactory.IMAGE_ADMIN);
        ImageSyncProject project = syncManager.createProject("test-project", srcStore.getId(), destStore.getId(),
                true, user);
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
    public void testCreateProjectWithSyncItems() {
        ImageStore destStore = ImageTestUtils.createImageStore("localRegistry", user);
        ImageStore srcStore = ImageTestUtils.createImageStore("externalRegistry", user);
        ImageSyncProject project = syncManager.createProject("test-project", srcStore.getId(), destStore.getId(),
                true, user);
        assertNotNull(project.getId());

        ImageSyncItem item = syncManager.createSyncItem(project.getId(), "/suse/sles", List.of("15-SP4"),
                "^12-SP.*$", user);
        assertNotNull(item.getId());

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        Optional<ImageSyncProject> optProject = syncManager.lookupProject(project.getId(), user);
        assertEquals(project, optProject.get());

        List<ImageSyncItem> syncItems = optProject.get().getSyncItems();
        assertEquals(1, syncItems.size());
        assertEquals(item, syncItems.get(0));
    }

    @Test
    public void testUpdateProjectAndSyncItems() {
        ImageStore destStore = ImageTestUtils.createImageStore("localRegistry", user);
        ImageStore newDestStore = ImageTestUtils.createImageStore("newLocalRegistry", user);
        ImageStore srcStore = ImageTestUtils.createImageStore("externalRegistry", user);
        ImageStore newSrcStore = ImageTestUtils.createImageStore("newExternalRegistry", user);

        ImageSyncProject project = syncManager.createProject("test-project", srcStore.getId(), destStore.getId(),
                true, user);
        assertNotNull(project.getId());

        ImageSyncItem item = syncManager.createSyncItem(project.getId(), "/suse/sles", List.of("15-SP4"),
                "^12-SP.*$", user);
        assertNotNull(item.getId());

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        ImageSyncProject dbProject = syncManager.lookupProject(project.getId(), user).orElseThrow();
        assertEquals(project, dbProject);

        syncManager.updateProject(dbProject.getId(), user, newSrcStore.getId(), null, null);
        syncManager.updateProject(dbProject.getId(), user, null, newDestStore.getId(), null);
        syncManager.updateProject(dbProject.getId(), user, null, null, false);

        List<ImageSyncItem> syncItems = dbProject.getSyncItems();
        assertEquals(1, syncItems.size());
        ImageSyncItem dbItem = syncItems.get(0);

        syncManager.updateSyncItem(dbItem.getId(), user, "/opensuse/leap", null, null);
        syncManager.updateSyncItem(dbItem.getId(), user, null, Arrays.asList("15.3", "15.4"), null);
        syncManager.updateSyncItem(dbItem.getId(), user, null, null, "^42\\..*$");

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        dbProject = syncManager.lookupProject(project.getId(), user).orElseThrow();
        assertEquals(newSrcStore, dbProject.getSrcStore());
        assertEquals(newDestStore, dbProject.getDestinationImageStore());
        assertEquals(false, dbProject.isScoped());

        dbItem = dbProject.getSyncItems().get(0);
        assertEquals("/opensuse/leap", dbItem.getSrcRepository());
        assertEquals(Arrays.asList("15.3", "15.4"), dbItem.getSrcTags());
        assertEquals("^42\\..*$", dbItem.getSrcTagsRegexp());
    }

    @Test
    public void testDeleteProject() {
        ImageStore destStore = ImageTestUtils.createImageStore("localRegistry", user);
        ImageStore srcStore = ImageTestUtils.createImageStore("externalRegistry", user);
        ImageSyncProject project = syncManager.createProject("test-project", srcStore.getId(), destStore.getId(),
                true, user);
        assertNotNull(project.getId());

        ImageSyncItem item = syncManager.createSyncItem(project.getId(), "/suse/sles", List.of("15-SP4"),
                "^12-SP.*$", user);
        assertNotNull(item.getId());

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        Optional<ImageSyncProject> optProject = syncManager.lookupProject(project.getId(), user);
        assertEquals(project, optProject.get());

        List<ImageSyncItem> syncItems = optProject.get().getSyncItems();
        //Long oldItemId = syncItems.get(0).getId();

        syncManager.deleteProject("test-project", user);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        assertEquals(Optional.empty(), syncManager.lookupProject("test-project", user));
        assertEquals(0, syncManager.listSyncItems(user).size());
    }

    @Test
    public void testDeleteItem() {
        ImageStore destStore = ImageTestUtils.createImageStore("localRegistry", user);
        ImageStore srcStore = ImageTestUtils.createImageStore("externalRegistry", user);
        ImageSyncProject project = syncManager.createProject("test-project", srcStore.getId(), destStore.getId(),
                true, user);
        assertNotNull(project.getId());

        ImageSyncItem item1 = syncManager.createSyncItem(project.getId(), "/suse/sles", List.of("15-SP4"),
                "^12-SP.*$", user);
        assertNotNull(item1.getId());

        ImageSyncItem item2 = syncManager.createSyncItem(project.getId(), "/opensuse/leap", null,
                "^15\\..*$", user);
        assertNotNull(item2.getId());

        Long itemId1 = item1.getId();

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        syncManager.deleteSyncItem(itemId1, user);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        Optional<ImageSyncProject> optProject = syncManager.lookupProject(project.getId(), user);
        assertTrue(optProject.isPresent());

        List<ImageSyncItem> syncItems = optProject.get().getSyncItems();
        assertEquals(1, syncItems.size());
        assertEquals(1, syncManager.listSyncItems(user).size());

        assertEquals(item2, syncItems.get(0));
    }
}
