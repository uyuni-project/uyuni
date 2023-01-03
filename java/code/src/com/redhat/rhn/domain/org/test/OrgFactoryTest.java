/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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

package com.redhat.rhn.domain.org.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.org.CustomDataKey;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Pillar;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.Token;
import com.redhat.rhn.domain.token.TokenFactory;
import com.redhat.rhn.domain.token.test.ActivationKeyTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JUnit test case for the Org class.
 */
public class OrgFactoryTest extends RhnBaseTestCase {

    @Test
    public void testOrgTrust() throws Exception {
        Org org = createTestOrg();
        Org trusted = createTestOrg();
        org.getTrustedOrgs().add(trusted);
        OrgFactory.save(org);
        flushAndEvict(org);
        org = OrgFactory.lookupById(org.getId());
        trusted = OrgFactory.lookupById(trusted.getId());
        assertContains(org.getTrustedOrgs(), trusted);
        assertContains(trusted.getTrustedOrgs(), org);
        org.getTrustedOrgs().remove(trusted);
        OrgFactory.save(org);
        flushAndEvict(org);
        org = OrgFactory.lookupById(org.getId());
        trusted = OrgFactory.lookupById(trusted.getId());
        assertFalse(org.getTrustedOrgs().contains(trusted));
        assertFalse(trusted.getTrustedOrgs().contains(org));
    }

    /**
     * Simple test illustrating how roles work. Note that the channel_admin role
     * is implied for an org admin iff the org has the channel_admin role.
     */
    @Test
    public void testAddRole() {
        User user = UserTestUtils.findNewUser("testuser", "testorg");
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        assertTrue(user.hasRole(RoleFactory.CHANNEL_ADMIN));
    }

    @Test
    public void testLookupById() {
        Org org1 = UserTestUtils.findNewOrg("testOrg" + this.getClass().getSimpleName());
        assertNotNull(org1);
        assertTrue(org1.getId() > 0);
    }

    @Test
    public void testCommitOrg() {
        Org org1 = UserTestUtils.findNewOrg("testOrg" + this.getClass().getSimpleName());
        String changedName = "OrgFactoryTest testCommitOrg " + TestUtils.randomString();
        org1.setName(changedName);
        org1 = OrgFactory.save(org1);
        Long id = org1.getId();
        flushAndEvict(org1);
        Org org2 = OrgFactory.lookupById(id);
        assertEquals(changedName, org2.getName());
    }

    @Test
    public void testStagingContent() throws Exception {
        Org org1 = createTestOrg();
        boolean staging = org1.getOrgConfig().isStagingContentEnabled();
        Long id = org1.getId();
        org1.getOrgConfig().setStagingContentEnabled(!staging);
        OrgFactory.save(org1);
        assertEquals(!staging, org1.getOrgConfig().isStagingContentEnabled());
        flushAndEvict(org1);
        Org org2 = OrgFactory.lookupById(id);
        assertEquals(!staging, org2.getOrgConfig().isStagingContentEnabled());
    }

    @Test
    public void testOrgPillars() throws Exception {
        Org org = createTestOrg();

        Set<Pillar> pillars = new HashSet<>();
        Map<String, Object> pillar1 = new HashMap<>();
        pillar1.put("data1", "foo");
        pillar1.put("data2", 123);
        pillars.add(new Pillar("category1", pillar1, org));
        Map<String, Object> pillar2 = new HashMap<>();
        pillar1.put("bar1", "baz");
        pillar1.put("bar2", 456);
        pillars.add(new Pillar("category2", pillar2, org));
        org.setPillars(pillars);

        TestUtils.saveAndFlush(org);
        Org org2 = OrgFactory.lookupById(org.getId());

        Pillar actual = org2.getPillars().stream()
                .filter(item -> "category1".equals(item.getCategory()))
                .findFirst()
                .get();
        assertNotNull(actual);
        assertEquals(123, actual.getPillar().get("data2"));
        assertFalse(actual.isMinionPillar());
        assertFalse(actual.isGlobalPillar());
        assertFalse(actual.isGroupPillar());
        assertTrue(actual.isOrgPillar());
    }

    private Org createTestOrg() throws Exception {
        Org org1 = OrgFactory.createOrg();
        org1.setName("org created by OrgFactory test: " + TestUtils.randomString());
        org1 = OrgFactory.save(org1);
        // build the channels set
        Channel channel1 = ChannelFactoryTest.createTestChannel(org1);
        flushAndEvict(channel1);
        org1.addOwnedChannel(channel1);
        assertTrue(org1.getId() > 0);
        return org1;
    }

    @Test
    public void testCreateOrg() throws Exception {
        Org org1 = createTestOrg();
        Org org2 = OrgFactory.lookupById(org1.getId());
        assertEquals(org2.getName(), org1.getName());
        assertNotNull(org2.getOwnedChannels());
    }

    @Test
    public void testOrgDefautRegistrationToken() throws Exception {
        User user = UserTestUtils.findNewUser("testUser", "testOrg", true);
        Org orig = user.getOrg();
        orig.setName("org created by OrgFactory test: " + TestUtils.randomString());
        // build the channels set
        Channel channel1 = ChannelFactoryTest.createTestChannel(orig);
        flushAndEvict(channel1);
        orig.addOwnedChannel(channel1);
        orig = OrgFactory.save(orig);
        assertTrue(orig.getId() > 0);

        assertNull(orig.getToken());
        ActivationKey key = ActivationKeyTest.createTestActivationKey(user);
        // Token is hidden behind activation key so we have to look it up
        // manually:
        Token token = TokenFactory.lookupById(key.getId());
        orig.setToken(token);
        orig = OrgFactory.save(orig);
        Long origId = orig.getId();
        flushAndEvict(orig);

        Org lookup = OrgFactory.lookupById(origId);
        assertEquals(token.getId(), lookup.getToken().getId());
        lookup.setToken(null);
        flushAndEvict(lookup);

        lookup = OrgFactory.lookupById(origId);
        assertNull(lookup.getToken());
    }

    /**
     * Test to see if the Org returns list of UserGroup IDs
     */
    @Test
    public void testGetRoles() {
        Org org1 = UserTestUtils.findNewOrg("testOrg" + this.getClass().getSimpleName());
        assertNotNull(org1.getRoles());
        assertTrue(org1.hasRole(RoleFactory.ORG_ADMIN));
    }

    @Test
    public void testLookupSatOrg() {
        assertNotNull(OrgFactory.getSatelliteOrg());
    }

    @Test
    public void testCustomDataKeys() {
        User user = UserTestUtils.findNewUser("testuser", "testorg");
        Org org = user.getOrg();

        Set keys = org.getCustomDataKeys();
        int sizeBefore = keys.size();

        CustomDataKey key = CustomDataKeyTest.createTestCustomDataKey(user);
        assertFalse(keys.contains(key));
        assertFalse(org.hasCustomDataKey(key.getLabel()));
        assertFalse(org.hasCustomDataKey("foo" + System.currentTimeMillis()));
        assertFalse(org.hasCustomDataKey(null));

        org.addCustomDataKey(key);

        keys = org.getCustomDataKeys();
        int sizeAfter = keys.size();

        assertTrue(keys.contains(key));
        assertTrue(sizeBefore < sizeAfter);
        assertTrue(org.hasCustomDataKey(key.getLabel()));

        CustomDataKey key2 = OrgFactory.lookupKeyByLabelAndOrg(key.getLabel(),
                org);
        assertNotNull(key2);

        key2 = OrgFactory.lookupKeyByLabelAndOrg(null, org);
        assertNull(key2);
    }

    @Test
    public void testLookupOrgsWithServersInFamily() throws Exception {
        Server s = ServerTestUtils.createTestSystem();
        Channel chan = s.getChannels().iterator().next();
        ChannelFamily family = chan.getChannelFamily();

        List<Org> orgs = OrgFactory.lookupOrgsUsingChannelFamily(family);
        assertEquals(1, orgs.size());
    }

    @Test
    public void testGetOrgCount() throws Exception {
        ServerTestUtils.createTestSystem();
        long totalOrgs = OrgFactory.getTotalOrgCount();
        assertTrue(totalOrgs > 0);
    }

    @Test
    public void testLookupAllOrgs() throws Exception {
        ServerTestUtils.createTestSystem();
        List<Org> totalOrgs = OrgFactory.lookupAllOrgs();
        assertFalse(totalOrgs.isEmpty());
    }

    @Test
    public void testClmSyncPatchesConfig() throws Exception {
        Org org = createTestOrg();

        org.getOrgConfig().setClmSyncPatches(false);
        org = HibernateFactory.reload(org);
        assertFalse(org.getOrgConfig().isClmSyncPatches());

        org.getOrgConfig().setClmSyncPatches(true);
        org = HibernateFactory.reload(org);
        assertTrue(org.getOrgConfig().isClmSyncPatches());
    }

}
