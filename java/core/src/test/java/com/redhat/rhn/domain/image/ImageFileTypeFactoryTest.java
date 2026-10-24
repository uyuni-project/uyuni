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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.Test;

import java.util.List;

public class ImageFileTypeFactoryTest extends BaseTestCaseWithUser {

    private static final String BASENAME = "POS_Image.x86_64-1.0.0";

    @Test
    public void testDefaultExtensionsAreEnabled() {
        List<String> extensions = ImageFileTypeFactory.lookupEnabledExtensions();
        assertEquals(21, extensions.size());
        assertTrue(extensions.containsAll(List.of(".xz", ".kernel", ".initrd", ".vagrant.libvirt.box")));
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
                "/var/lib/Kiwi/build1/images.build/" + BASENAME + ".vagrant.libvirt.box");

        // the bare basename is the uncompressed image, metadata files are not collected
        assertEquals(List.of(files.get(0), files.get(1), files.get(2), files.get(3), files.get(7)),
                ImageFileTypeFactory.filterAllowedFiles(files, BASENAME));
    }

    @Test
    public void testFilterAcceptsFileNamesAndExtensions() {
        assertEquals(List.of(BASENAME + ".qcow2", ".iso"),
                ImageFileTypeFactory.filterAllowedFiles(
                        List.of(BASENAME + ".qcow2", BASENAME + ".txt", ".iso"), BASENAME));
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
                ImageFileTypeFactory.filterAllowedFiles(List.of(BASENAME + ".xz", BASENAME + ".gz"), BASENAME));
    }
}
