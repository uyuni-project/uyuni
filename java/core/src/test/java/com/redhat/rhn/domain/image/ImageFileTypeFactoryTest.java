/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class ImageFileTypeFactoryTest extends BaseTestCaseWithUser {

    private static final String BASENAME = "POS_Image.x86_64-1.0.0";

    @Test
    public void testDefaultExtensionsAreEnabled() {
        Map<String, ImageFileType> types = ImageFileTypeFactory.lookupEnabledFileTypes(user.getOrg());
        assertEquals(21, types.size());
        assertTrue(types.keySet().containsAll(List.of(".xz", ".kernel", ".initrd", ".vagrant.libvirt.box",
                ".spdx.json")));
        assertFalse(types.containsKey(".raw"));
    }

    @Test
    public void testDefaultFiletypes() {
        assertEquals("kernel", lookupDefaultType(".kernel").getFiletype());
        assertEquals("initrd", lookupDefaultType(".initrd").getFiletype());
        assertEquals("sbom", lookupDefaultType(".spdx.json").getFiletype());
        assertFalse(lookupDefaultType(".raw").isEnabled());

        ImageFileType xz = lookupDefaultType(".xz");
        assertEquals("image", xz.getFiletype());
        assertTrue(xz.isEnabled());
    }

    private static ImageFileType lookupDefaultType(String extension) {
        return HibernateFactory.getSession()
                .createQuery("FROM ImageFileType WHERE extension = :ext AND org IS NULL", ImageFileType.class)
                .setParameter("ext", extension)
                .uniqueResult();
    }

    @Test
    public void testFilterAllowedFiles() {
        List<String> files = List.of(
                "/var/lib/Kiwi/build1/images.build/" + BASENAME,
                "/var/lib/Kiwi/build1/images.build/" + BASENAME + ".xz",
                "/var/lib/Kiwi/build1/images.build/" + BASENAME + ".initrd.xz",
                "/var/lib/Kiwi/build1/images.build/" + BASENAME + "-5.14.21-default.kernel",
                "/var/lib/Kiwi/build1/images.build/" + BASENAME + ".md5",
                "/var/lib/Kiwi/build1/images.build/" + BASENAME + ".sha256",
                "/var/lib/Kiwi/build1/images.build/" + BASENAME + ".packages",
                "/var/lib/Kiwi/build1/images.build/" + BASENAME + ".raw",
                "/var/lib/Kiwi/build1/images.build/" + BASENAME + ".vagrant.libvirt.box",
                "/var/lib/Kiwi/build1/images.build/" + BASENAME + ".spdx.json");

        Map<String, String> allowed = ImageFileTypeFactory.filterAllowedFiles(files, BASENAME, user.getOrg());

        // the bare basename is the uncompressed image, metadata and disabled extensions are not collected
        assertEquals(List.of(files.get(0), files.get(1), files.get(2), files.get(3), files.get(8), files.get(9)),
                List.copyOf(allowed.keySet()));
        // the filetype of the matched extension is kept, the inspection refines it
        assertEquals("image", allowed.get(files.get(0)));
        assertEquals("image", allowed.get(files.get(1)));
        assertEquals("image", allowed.get(files.get(2)));
        assertEquals("kernel", allowed.get(files.get(3)));
        assertEquals("image", allowed.get(files.get(8)));
        assertEquals("sbom", allowed.get(files.get(9)));
    }

    @Test
    public void testFilterAcceptsFileNamesAndExtensions() {
        assertEquals(Map.of(BASENAME + ".qcow2", "image", ".iso", "image"),
                ImageFileTypeFactory.filterAllowedFiles(
                        List.of(BASENAME + ".qcow2", BASENAME + ".txt", ".iso"), BASENAME, user.getOrg()));
    }

    @Test
    public void testDisabledExtensionIsFilteredOut() {
        HibernateFactory.getSession()
                .createQuery("FROM ImageFileType WHERE extension = :ext", ImageFileType.class)
                .setParameter("ext", ".xz")
                .uniqueResult()
                .setEnabled(false);
        TestUtils.flushSession();

        assertEquals(List.of(BASENAME + ".gz"),
                List.copyOf(ImageFileTypeFactory.filterAllowedFiles(
                        List.of(BASENAME + ".xz", BASENAME + ".gz"), BASENAME, user.getOrg()).keySet()));
    }

    @Test
    public void testDisabledLongerExtensionExcludesShorterMatch() {
        for (String extension : List.of(".tar.xz", ".install.iso")) {
            ImageFileType disabled = new ImageFileType();
            disabled.setOrg(user.getOrg());
            disabled.setExtension(extension);
            disabled.setEnabled(false);
            HibernateFactory.getSession().persist(disabled);
        }
        TestUtils.flushSession();

        List<String> files = List.of(BASENAME + ".xz", BASENAME + ".tar.xz",
                BASENAME + ".iso", BASENAME + ".install.iso");
        Map<String, String> allowed = ImageFileTypeFactory.filterAllowedFiles(files, BASENAME, user.getOrg());

        // the disabled .tar.xz and .install.iso keep their files excluded
        // despite the enabled shorter extensions .xz and .iso
        assertEquals(List.of(files.get(0), files.get(2)), List.copyOf(allowed.keySet()));
        assertEquals("image", allowed.get(files.get(0)));
        assertEquals("image", allowed.get(files.get(2)));

        // the enabled lookup hides the disabled entries
        Map<String, ImageFileType> enabled = ImageFileTypeFactory.lookupEnabledFileTypes(user.getOrg());
        assertTrue(enabled.containsKey(".xz") && enabled.containsKey(".iso"));
        assertFalse(enabled.containsKey(".tar.xz") || enabled.containsKey(".install.iso"));
    }

    @Test
    public void testOrgOverridesDefaultDisabled() {
        ImageFileType raw = new ImageFileType();
        raw.setOrg(user.getOrg());
        raw.setExtension(".raw");
        raw.setEnabled(true);
        HibernateFactory.getSession().persist(raw);
        TestUtils.flushSession();

        Map<String, ImageFileType> enabled = ImageFileTypeFactory.lookupEnabledFileTypes(user.getOrg());
        assertTrue(enabled.containsKey(".raw"));
        assertTrue(enabled.get(".raw").isEnabled());
        assertFalse(lookupDefaultType(".raw").isEnabled());
        assertEquals("image", enabled.get(".raw").getFiletype());
        assertEquals(Map.of(BASENAME + ".raw", "image"),
                ImageFileTypeFactory.filterAllowedFiles(List.of(BASENAME + ".raw"), BASENAME, user.getOrg()));
    }
}
