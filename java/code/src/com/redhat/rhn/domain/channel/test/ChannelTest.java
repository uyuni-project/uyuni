/*
 * Copyright (c) 2009--2013 Red Hat, Inc.
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
package com.redhat.rhn.domain.channel.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelFamilyFactory;
import com.redhat.rhn.domain.channel.ContentSource;
import com.redhat.rhn.domain.channel.ContentSourceType;
import com.redhat.rhn.domain.channel.Modules;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.test.ErrataFactoryTest;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ChannelTest
 */
@SuppressWarnings("deprecation")
public class ChannelTest extends BaseTestCaseWithUser {

    private static Logger log = LogManager.getLogger(ChannelTest.class);


    @Test
    public void testRemovePackage() throws Exception {
        Channel c = ChannelFactoryTest.createTestChannel(user);
        Package p = PackageTest.createTestPackage(user.getOrg());
        c.addPackage(p);
        ChannelFactory.save(c);
        c.removePackage(p, user);
        assertEquals(0, c.getPackageCount());
        assertTrue(c.getPackages().isEmpty());

    }

    @Test
    public void testChannel() throws Exception {
        Channel c = ChannelFactoryTest.createTestChannel(user);
        //add an errata
        Errata e = ErrataFactoryTest.createTestErrata(user.getId());
        c.addErrata(e);
        assertEquals(c.getErratas().size(), 1);
        ChannelFactory.save(c);

        log.debug("Looking up id [{}]", c.getId());
        Channel c2 = ChannelFactory.lookupById(c.getId());
        log.debug("Finished lookup");
        assertEquals(c2.getErratas().size(), 1);

        assertEquals(c.getLabel(), c2.getLabel());
        assertNotNull(c.getChannelArch());

        Channel c3 = ChannelFactoryTest.createTestChannel(user);

        c.setParentChannel(c3);
        assertEquals(c.getParentChannel().getId(), c3.getId());

        //Test isGloballySubscribable
        assertTrue(c.isGloballySubscribable(c.getOrg()));
        c.setGloballySubscribable(false, c.getOrg());
        assertFalse(c.isGloballySubscribable(c.getOrg()));
        c.setGloballySubscribable(true, c.getOrg());
        assertTrue(c.isGloballySubscribable(c.getOrg()));


    }

    @Test
    public void testChannelGpgCheck() throws Exception {
        Channel c = ChannelFactoryTest.createTestChannel(user, true);
        ChannelFactory.save(c);
        assertTrue(c.isGPGCheck());
        TestUtils.flushAndEvict(c);
        Channel c1 = ChannelFactory.lookupById(c.getId());
        assertTrue(c1.isGPGCheck());

        Channel c2 = ChannelFactoryTest.createTestChannel(user, false);
        ChannelFactory.save(c2);
        assertFalse(c2.isGPGCheck());
        TestUtils.flushAndEvict(c2);
        Channel c3 = ChannelFactory.lookupById(c2.getId());
        assertFalse(c3.isGPGCheck());
    }

    @Test
    public void testEquals() throws Exception {
        Channel c1 = ChannelFactoryTest.createTestChannel(user);
        Channel c2 = ChannelFactoryTest.createTestChannel(user);
        assertNotEquals(c1, c2);
        Channel c3 = ChannelFactory.lookupById(c1.getId());
        Set<Channel> testSet = new HashSet<>();
        testSet.add(c1);
        testSet.add(c2);
        testSet.add(c3);
        assertEquals(2, testSet.size());
    }

    @Test
    public void testDistChannelMap() throws Exception {
        Channel c = ChannelFactoryTest.createTestChannel(user);
        ChannelTestUtils.addDistMapToChannel(c);
        c = (Channel) reload(c);
        assertNotNull(c.getDistChannelMaps());
        assertFalse(c.getDistChannelMaps().isEmpty());
    }

    @Test
    public void testIsProxy() throws Exception {
        Channel c = ChannelFactoryTest.createTestChannel(user);
        ChannelFamily cfam =
                ChannelFamilyFactoryTest.createTestChannelFamily(user, false,
                        ChannelFamilyFactory.PROXY_CHANNEL_FAMILY_LABEL);

        c.setChannelFamily(cfam);

        TestUtils.saveAndFlush(c);

        Channel c2 = ChannelFactory.lookupById(c.getId());
        assertTrue(c2.isProxy());
    }

    @Test
    public void testIsSub() throws Exception {
        Channel c = ChannelFactoryTest.createTestChannel(user);
        Server s = ServerTestUtils.createTestSystem(user);
        assertTrue(c.isSubscribable(c.getOrg(), s));
    }

    @Test
    public void testDeleteChannel() throws Exception {
        Channel c = ChannelFactoryTest.createTestChannel(user);
        Long id = c.getId();
        assertNotNull(c);
        ChannelFactory.save(c);
        assertNotNull(ChannelFactory.lookupById(id));
        ChannelFactory.remove(c);
        TestUtils.flushAndEvict(c);
        assertNull(ChannelFactory.lookupById(id));
    }

    @Test
    public void testIsBaseChannel() {
        Channel c = new Channel();
        Channel p = new Channel();
        c.setParentChannel(p);
        assertFalse(c.isBaseChannel());
        c.setParentChannel(null);
        assertTrue(c.isBaseChannel());
    }

    @Test
    public void testAddPackage() throws Exception {
        Channel c = ChannelFactoryTest.createTestChannel(user);
        Package p = PackageTest.createTestPackage(user.getOrg());
        assertNotNull(c);
        assertEquals("channel-x86_64", c.getChannelArch().getLabel());
        assertNotNull(p);
        assertEquals("noarch", p.getPackageArch().getLabel());

        try {
            c.addPackage(p);
        }
        catch (Exception e) {
            fail("noarch should be acceptible in an x86_64 channel");
        }


        try {
            PackageArch pa = PackageFactory.lookupPackageArchByLabel("aarch64");
            assertNotNull(pa);
            p.setPackageArch(pa);
            c.addPackage(p);
            fail("aarch64 is not acceptible in an x86_64 channel");
        }
        catch (Exception e) {
            // expected.
        }

    }

    @Test
    public void testContentSource() throws Exception {
        Channel c = ChannelFactoryTest.createTestChannel(user);
        ContentSource cs = new ContentSource();
        cs.setLabel("repo_label-" + c.getLabel());
        cs.setSourceUrl("fake url");
        List<ContentSourceType> cst = ChannelFactory.listContentSourceTypes();
        cs.setType(cst.get(0));
        cs.setOrg(user.getOrg());
        cs = (ContentSource) TestUtils.saveAndReload(cs);
        c.getSources().add(cs);
        c = (Channel) TestUtils.saveAndReload(c);
        assertNotEmpty(c.getSources());
    }

    @Test
    public void testIsTypeRpm() throws Exception {
        Channel c = ChannelFactoryTest.createTestChannel(user);
        ChannelArch arch = ChannelFactory.lookupArchByLabel("channel-ia64");
        c.setChannelArch(arch);

        assertTrue(c.isTypeRpm());
    }

    @Test
    public void testIsTypeDeb() throws Exception {
        Channel c = ChannelFactoryTest.createTestChannel(user);
        ChannelArch arch = ChannelFactory.lookupArchByLabel("channel-ia64-deb");
        c.setChannelArch(arch);

        assertTrue(c.isTypeDeb());
    }

    @Test
    public void testIsModular() throws Exception {
        Channel c = ChannelFactoryTest.createTestChannel(user);
        assertNull(c.getModules());
        assertFalse(c.isModular());

        c.setModules(new Modules());
        assertNotNull(c.getModules());
        assertTrue(c.isModular());
    }

}
