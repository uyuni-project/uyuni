/*
 * Copyright (c) 2020 SUSE LLC
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
package com.suse.manager.virtualization.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.suse.manager.virtualization.PoolDefinition;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class PoolDefinitionTest  {

    @Test
    public void testParseRbd() {
        String xml =
                "<pool type='rbd'>\n" +
                "  <name>test-ses</name>\n" +
                "  <uuid>ede33e0a-9df0-479f-8afd-55085a01b244</uuid>\n" +
                "  <capacity unit='bytes'>0</capacity>\n" +
                "  <allocation unit='bytes'>0</allocation>\n" +
                "  <available unit='bytes'>0</available>\n" +
                "  <source>\n" +
                "    <host name='ses2.tf.local' port='1234'/>\n" +
                "    <host name='ses3.tf.local'/>\n" +
                "    <name>libvirt-pool</name>\n" +
                "    <auth type='ceph' username='libvirt'>\n" +
                "      <secret usage='pool_test-ses'/>\n" +
                "    </auth>\n" +
                "  </source>\n" +
                "</pool>";
        PoolDefinition actual = PoolDefinition.parse(xml);
        assertEquals("test-ses", actual.getName());
        assertEquals("ede33e0a-9df0-479f-8afd-55085a01b244", actual.getUuid());
        assertEquals("rbd", actual.getType());
        assertNull(actual.getTarget());
        assertEquals("libvirt-pool", actual.getSource().getName());
        assertEquals("ceph", actual.getSource().getAuth().getType());
        assertEquals("libvirt", actual.getSource().getAuth().getUsername());
        assertEquals("usage", actual.getSource().getAuth().getSecretType());
        assertEquals("pool_test-ses", actual.getSource().getAuth().getSecretValue());
        assertTrue(Arrays.asList("ses2.tf.local:1234", "ses3.tf.local").equals(actual.getSource().getHosts()));
    }

    @Test
    public void testParseDir() {
        String xml =
                "<pool type='dir'>\n" +
                "  <name>default</name>\n" +
                "  <uuid>9f2d114f-4dc7-4f76-8831-062931c04a9a</uuid>\n" +
                "  <capacity unit='bytes'>210303426560</capacity>\n" +
                "  <allocation unit='bytes'>6631251968</allocation>\n" +
                "  <available unit='bytes'>203672174592</available>\n" +
                "  <source>\n" +
                "  </source>\n" +
                "  <target>\n" +
                "    <path>/var/lib/libvirt/images</path>\n" +
                "    <permissions>\n" +
                "      <mode>0755</mode>\n" +
                "      <owner>0</owner>\n" +
                "      <group>123</group>\n" +
                "      <label>virt_image_t</label>\n" +
                "    </permissions>\n" +
                "  </target>\n" +
                "</pool>";
        PoolDefinition actual = PoolDefinition.parse(xml);
        assertEquals("default", actual.getName());
        assertEquals("9f2d114f-4dc7-4f76-8831-062931c04a9a", actual.getUuid());
        assertEquals("dir", actual.getType());
        assertEquals("/var/lib/libvirt/images", actual.getTarget().getPath());
        assertEquals("0755", actual.getTarget().getMode());
        assertEquals("0", actual.getTarget().getOwner());
        assertEquals("123", actual.getTarget().getGroup());
        assertEquals("virt_image_t", actual.getTarget().getSeclabel());
    }

    @Test
    public void testParseFs() {
        String xml =
                "<pool type=\"fs\">\n" +
                "  <name>virtimages</name>\n" +
                "  <source>\n" +
                "    <device path=\"/dev/VolGroup00/VirtImages\"/>\n" +
                "    <format type=\"ocfs2\"/> \n" +
                "  </source>\n" +
                "  <target>\n" +
                "    <path>/var/lib/virt/images</path>\n" +
                "  </target>\n" +
                "</pool>";
        PoolDefinition actual = PoolDefinition.parse(xml);
        assertEquals("virtimages", actual.getName());
        assertNull(actual.getUuid());
        assertEquals("fs", actual.getType());
        assertEquals("/var/lib/virt/images", actual.getTarget().getPath());
        assertEquals(1, actual.getSource().getDevices().size());
        assertEquals("/dev/VolGroup00/VirtImages", actual.getSource().getDevices().get(0).getPath());
        assertEquals("ocfs2", actual.getSource().getFormat());
    }

    @Test
    public void testParseNetfs() {
        String xml =
                "<pool type='netfs'>\n" +
                "  <name>virtimages</name>\n" +
                "  <source>\n" +
                "    <host name='nfs.example.com'/>\n" +
                "    <dir path='/var/lib/virt/images'/>\n" +
                "    <format type='nfs'/>\n" +
                "  </source>\n" +
                "  <target>\n" +
                "    <path>/var/lib/virt/images</path>\n" +
                "  </target>\n" +
                "</pool>";
        PoolDefinition actual = PoolDefinition.parse(xml);
        assertEquals("virtimages", actual.getName());
        assertNull(actual.getUuid());
        assertEquals("netfs", actual.getType());
        assertEquals("/var/lib/virt/images", actual.getTarget().getPath());
        assertTrue(Arrays.asList("nfs.example.com").equals(actual.getSource().getHosts()));
        assertEquals("/var/lib/virt/images", actual.getSource().getDir());
        assertEquals("nfs", actual.getSource().getFormat());
    }

    @Test
    public void testParseLogical() {
        String xml =
                "<pool type='logical'>\n" +
                "  <name>HostVG</name>\n" +
                "  <source>\n" +
                "    <device path='/dev/sda1' part_separator='yes'/>\n" +
                "    <device path='/dev/sdb1'/>\n" +
                "  </source>\n" +
                "  <target>\n" +
                "    <path>/dev/HostVG</path>\n" +
                "  </target>\n" +
                "</pool>";
        PoolDefinition actual = PoolDefinition.parse(xml);
        assertEquals("logical", actual.getType());
        assertEquals("/dev/HostVG", actual.getTarget().getPath());
        assertEquals(2, actual.getSource().getDevices().size());
        assertEquals("/dev/sda1", actual.getSource().getDevices().get(0).getPath());
        assertTrue(actual.getSource().getDevices().get(0).isSeparator().get());
        assertEquals("/dev/sdb1", actual.getSource().getDevices().get(1).getPath());
        assertTrue(actual.getSource().getDevices().get(1).isSeparator().isEmpty());
    }

    @Test
    public void testParseScsi() {
        String xml =
                "<pool type='scsi'>\n" +
                "  <name>virtimages</name>\n" +
                "  <source>\n" +
                "    <adapter name='host0'/>\n" +
                "  </source>\n" +
                "  <target>\n" +
                "    <path>/dev/disk/by-path</path>\n" +
                "  </target>\n" +
                "</pool>";
        PoolDefinition actual = PoolDefinition.parse(xml);
        assertEquals("host0", actual.getSource().getAdapter().getName());
    }

    @Test
    public void testParseIscsiDirect() {
        String xml =
                "<pool type='iscsi-direct'>\n" +
                "  <name>virtimages</name>\n" +
                "  <source>\n" +
                "    <host name='iscsi.example.com'/>\n" +
                "    <device path='iqn.2013-06.com.example:iscsi-pool'/>\n" +
                "    <initiator>\n" +
                "      <iqn name='iqn.2013-06.com.example:iscsi-initiator'/>\n" +
                "    </initiator>\n" +
                "    <auth type='chap' username='myname'>\n" +
                "      <secret uuid='2ec115d7-3a88-3ceb-bc12-0ac909a6fd87'/>\n" +
                "    </auth>\n" +
                "  </source>\n" +
                "</pool>";
        PoolDefinition actual = PoolDefinition.parse(xml);
        assertEquals("iqn.2013-06.com.example:iscsi-initiator", actual.getSource().getInitiator());
        assertEquals("chap", actual.getSource().getAuth().getType());
        assertEquals("myname", actual.getSource().getAuth().getUsername());
        assertEquals("uuid", actual.getSource().getAuth().getSecretType());
        assertEquals("2ec115d7-3a88-3ceb-bc12-0ac909a6fd87", actual.getSource().getAuth().getSecretValue());
    }

    @Test
    public void testParseIscsiAddress() {
        String xml =
                "<pool type=\"iscsi\">\n" +
                "  <name>virtimages</name>\n" +
                "  <source>\n" +
                "    <host name=\"iscsi.example.com\"/>\n" +
                "    <device path=\"iqn.2013-06.com.example:iscsi-pool\"/>\n" +
                "    <adapter type='scsi_host'>\n" +
                "      <parentaddr unique_id='1'>\n" +
                "        <address domain='0x0000' bus='0x00' slot='0x1f' function='0x2'/>\n" +
                "      </parentaddr>\n" +
                "    </adapter>\n" +
                "  </source>\n" +
                "  <target>\n" +
                "    <path>/dev/disk/by-path</path>\n" +
                "  </target>\n" +
                "</pool>";
        PoolDefinition actual = PoolDefinition.parse(xml);
        assertEquals("scsi_host", actual.getSource().getAdapter().getType());
        assertEquals("1", actual.getSource().getAdapter().getParentAddressUid());
        assertEquals("0000:00:1f.2", actual.getSource().getAdapter().getParentAddress());
    }

    @Test
    public void testParseIscsiWwnn() {
        String xml =
                "<pool type=\"iscsi\">\n" +
                "  <name>virtimages</name>\n" +
                "  <source>\n" +
                "    <host name=\"iscsi.example.com\"/>\n" +
                "    <device path=\"iqn.2013-06.com.example:iscsi-pool\"/>\n" +
                "    <adapter type='fc_host' parent='scsi_host5' wwnn='20000000c9831b4b' wwpn='10000000c9831b4b'/>\n" +
                "  </source>\n" +
                "  <target>\n" +
                "    <path>/dev/disk/by-path</path>\n" +
                "  </target>\n" +
                "</pool>";
        PoolDefinition actual = PoolDefinition.parse(xml);
        assertEquals("fc_host", actual.getSource().getAdapter().getType());
        assertEquals("scsi_host5", actual.getSource().getAdapter().getParent());
        assertEquals("20000000c9831b4b", actual.getSource().getAdapter().getWwnn());
        assertEquals("10000000c9831b4b", actual.getSource().getAdapter().getWwpn());
    }
}
