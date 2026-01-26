/*
 * Copyright (c) 2022 SUSE LLC
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

package com.suse.manager.saltboot.test;

import static com.redhat.rhn.domain.org.test.CustomDataKeyTest.createTestCustomDataKey;
import static com.redhat.rhn.domain.server.test.CustomDataValueTest.createTestCustomDataValue;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.Optional.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.org.CustomDataKey;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.server.CustomDataValue;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Pillar;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.saltboot.PXEEvent;
import com.suse.manager.saltboot.PXEEventMessage;
import com.suse.manager.saltboot.PXEEventMessageAction;
import com.suse.manager.saltboot.SaltbootUtils;
import com.suse.salt.netapi.datatypes.Event;

import org.cobbler.Distro;
import org.cobbler.Profile;
import org.cobbler.SystemRecord;
import org.cobbler.test.MockConnection;
import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class PXEEventTest extends JMockBaseTestCaseWithUser {

    private MockConnection cobblerMock;
    private static final String MINION_ID = "minion.local";
    private static final String SALTBOOT_PILLAR = "tuning-saltboot";

    private static final String SALTBOOT_FORMULA = "formula-saltboot";
    private static final String BOOT_IMAGE = "POS_Image_JeOS7-7.0.0-1";
    private static final String SALTBOOT_GROUP = "groupPrefix";
    private static final String MAC_ADDRESS = "00:11:22:33:44:55";


    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        cobblerMock = new MockConnection("http://localhost", "token");

        // create cobbler distro and saltboot profile
        Distro distro = new Distro.Builder<String>()
                .setName(SaltbootUtils.makeCobblerName(user.getOrg(), BOOT_IMAGE))
                .setKernel("kernel_file")
                .setInitrd("initrd_file")
                .setKernelOptions(Optional.of("panic=60 splash=silent"))
                .build(cobblerMock);
        distro.save();

        Profile imageProfile = Profile.create(cobblerMock,
                                              SaltbootUtils.makeCobblerName(user.getOrg(), BOOT_IMAGE),
                                              distro);
        imageProfile.save();
        Profile profile = Profile.create(cobblerMock,
                                         SaltbootUtils.makeCobblerName(user.getOrg(), SALTBOOT_GROUP),
                                         distro);
        profile.save();
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
        MockConnection.clear();
    }

    private Map<String, Object> createTestData(String minionId, Object saltbootGroup, String root, String saltDevice,
                                  String bootImage, String kernelOptions, boolean includeMACs) {
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> innerData = new HashMap<>();
        if (saltbootGroup != null) {
            innerData.put("minion_id_prefix", saltbootGroup);
        }
        if (!root.isEmpty()) {
            innerData.put("root", root);
        }
        if (!saltDevice.isEmpty()) {
            innerData.put("salt_device", saltDevice);
        }
        if (!bootImage.isEmpty()) {
            innerData.put("boot_image", bootImage);
        }
        if (!kernelOptions.isEmpty()) {
            innerData.put("terminal_kernel_parameters", kernelOptions);
        }
        if (includeMACs) {
            Map<String, Object> hwAddrs = new HashMap<>();
            hwAddrs.put("eth1", MAC_ADDRESS);
            hwAddrs.put("lo", "00:00:00:00:00:00");
            innerData.put("hwaddr_interfaces", hwAddrs);
        }

        data.put("id", minionId);
        data.put("data", innerData);

        return data;
    }

    private Map<String, Object> createSaltbootTestData(Boolean repartition, Boolean redeploy) {
        return createSaltbootTestData(repartition, redeploy, null);
    }

    private Map<String, Object> createSaltbootTestData(Boolean repartition, Boolean redeploy, Boolean extraData) {
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> saltbootData = new HashMap<>();
        if (repartition != null) {
            saltbootData.put("force_repartition", repartition);
        }
        if (redeploy != null) {
            saltbootData.put("force_redeploy", redeploy);
        }
        if (extraData != null && extraData) {
            saltbootData.put("extraData", "are here");
        }
        data.put("saltboot", saltbootData);
        return data;
    }

    /**
     * Tests parsing {@link PXEEvent}.
     * "Happy path" scenario.
     */
    @Test
    public void testParse() {
        Event event = mock(Event.class);
        Map<String, Object> data = createTestData(MINION_ID, SALTBOOT_GROUP, "root=/dev/sda1",
                "/dev/sda1", BOOT_IMAGE, "custom=option", true);
        context().checking(new Expectations() {{
            allowing(event).getTag();
            will(returnValue("suse/manager/pxe_update"));
            allowing(event).getData();
            will(returnValue(data));
        }});

        Optional<PXEEvent> parsed = PXEEvent.parse(event);

        assertTrue(parsed.isPresent());
        parsed.ifPresent(e -> {
            assertEquals(MINION_ID, e.getMinionId());
            assertEquals(SALTBOOT_GROUP, e.getSaltbootGroup());
            assertEquals("root=/dev/sda1", e.getRoot());
            assertEquals(Optional.of("/dev/sda1"), e.getSaltDevice());
            assertEquals(Optional.of("custom=option"), e.getKernelParameters());
            assertEquals(BOOT_IMAGE, e.getBootImage());
            // Localhost device should be parsed out
            assertEquals(1, e.getHwAddresses().size());
            assertEquals(MAC_ADDRESS, e.getHwAddresses().get(0));
        });
    }

    /**
     * Tests parsing event with unmatching tag.
     */
    @Test
    public void testParseNotMatchingTag() {
        Event event = mock(Event.class);
        context().checking(new Expectations() {{
            allowing(event).getTag();
            will(returnValue("youDontKnowMe"));
        }});
        assertEquals(empty(), PXEEvent.parse(event));
    }

    /**
     * Tests parsing event with matching tag, but empty data.
     */
    @Test
    public void testParseNoGrains() {
        Event event = mock(Event.class);
        context().checking(new Expectations() {{
            allowing(event).getTag();
            will(returnValue("suse/manager/pxe_update"));
            allowing(event).getData();
            Map<String, Object> data = singletonMap("data", emptyMap());
            will(returnValue(data));
        }});
        assertEquals(empty(), PXEEvent.parse(event));
    }

    /**
     * Tests parsing event when HW address not provided
     */
    @Test
    public void testParseNoHWaddresses() {
        Event event = mock(Event.class);
        Map<String, Object> data = createTestData(MINION_ID, SALTBOOT_GROUP, "root=/dev/sda1",
                "/dev/sda1", BOOT_IMAGE, "custom=option", false);
        context().checking(new Expectations() {{
            allowing(event).getTag();
            will(returnValue("suse/manager/pxe_update"));
            allowing(event).getData();
            will(returnValue(data));
        }});
        assertEquals(empty(), PXEEvent.parse(event));
    }

    /**
     * Tests parsing event when root device not provided
     */
    @Test
    public void testParseNoRoot() {
        Event event = mock(Event.class);
        Map<String, Object> data = createTestData(MINION_ID, SALTBOOT_GROUP, "",
                "/dev/sda1", BOOT_IMAGE, "custom=option", true);
        context().checking(new Expectations() {{
            allowing(event).getTag();
            will(returnValue("suse/manager/pxe_update"));
            allowing(event).getData();
            will(returnValue(data));
        }});
        assertEquals(empty(), PXEEvent.parse(event));
    }

    /**
     * Tests parsing event when boot image is not provided
     */
    @Test
    public void testParseNoBootImage() {
        Event event = mock(Event.class);
        Map<String, Object> data = createTestData(MINION_ID, SALTBOOT_GROUP, "root=/dev/sda1",
                "/dev/sda1", "", "custom=option", true);
        context().checking(new Expectations() {{
            allowing(event).getTag();
            will(returnValue("suse/manager/pxe_update"));
            allowing(event).getData();
            will(returnValue(data));
        }});
        assertEquals(empty(), PXEEvent.parse(event));
    }

    /**
     * Tests parsing event when kernel options not provided
     */
    @Test
    public void testParseNoKernelOptions() {
        Event event = mock(Event.class);
        Map<String, Object> data = createTestData(MINION_ID, SALTBOOT_GROUP, "root=/dev/sda1",
                "/dev/sda1", BOOT_IMAGE, "", true);
        context().checking(new Expectations() {{
            allowing(event).getTag();
            will(returnValue("suse/manager/pxe_update"));
            allowing(event).getData();
            will(returnValue(data));
        }});
        Optional<PXEEvent> parsed = PXEEvent.parse(event);

        assertTrue(parsed.isPresent());
        parsed.ifPresent(e -> {
            assertEquals(MINION_ID, e.getMinionId());
            assertEquals(SALTBOOT_GROUP, e.getSaltbootGroup());
            assertEquals("root=/dev/sda1", e.getRoot());
            assertEquals(Optional.of("/dev/sda1"), e.getSaltDevice());
            assertEquals(Optional.empty(), e.getKernelParameters());
            assertEquals(BOOT_IMAGE, e.getBootImage());
            // Localhost device should be parsed out
            assertEquals(1, e.getHwAddresses().size());
            assertEquals(MAC_ADDRESS, e.getHwAddresses().get(0));
        });
    }

    /**
     * Tests parsing event when salt device not provided
     */
    @Test
    public void testParseNoSaltDevice() {
        Event event = mock(Event.class);
        Map<String, Object> data = createTestData(MINION_ID, SALTBOOT_GROUP, "root=/dev/sda1",
                "", BOOT_IMAGE, "custom=option", true);
        context().checking(new Expectations() {{
            allowing(event).getTag();
            will(returnValue("suse/manager/pxe_update"));
            allowing(event).getData();
            will(returnValue(data));
        }});
        Optional<PXEEvent> parsed = PXEEvent.parse(event);

        assertTrue(parsed.isPresent());
        parsed.ifPresent(e -> {
            assertEquals(MINION_ID, e.getMinionId());
            assertEquals(SALTBOOT_GROUP, e.getSaltbootGroup());
            assertEquals("root=/dev/sda1", e.getRoot());
            assertEquals(Optional.empty(), e.getSaltDevice());
            assertEquals(Optional.of("custom=option"), e.getKernelParameters());
            assertEquals(BOOT_IMAGE, e.getBootImage());
            // Localhost device should be parsed out
            assertEquals(1, e.getHwAddresses().size());
            assertEquals(MAC_ADDRESS, e.getHwAddresses().get(0));
        });
    }

    /**
     * Tests parsing {@link PXEEvent} when branch id is numeric
     */
    @Test
    public void testParseBranchIdIsNumeric() {
        Event event = mock(Event.class);
        Integer branchId = 12345;
        Map<String, Object> data = createTestData(MINION_ID, branchId, "root=/dev/sda1",
                "/dev/sda1", BOOT_IMAGE, "custom=option", true);
        context().checking(new Expectations() {{
            allowing(event).getTag();
            will(returnValue("suse/manager/pxe_update"));
            allowing(event).getData();
            will(returnValue(data));
        }});

        Optional<PXEEvent> parsed = PXEEvent.parse(event);

        assertTrue(parsed.isPresent());
        parsed.ifPresent(e -> {
            assertEquals(MINION_ID, e.getMinionId());
            assertEquals(String.valueOf(branchId), e.getSaltbootGroup());
            assertEquals("root=/dev/sda1", e.getRoot());
            assertEquals(Optional.of("/dev/sda1"), e.getSaltDevice());
            assertEquals(Optional.of("custom=option"), e.getKernelParameters());
            assertEquals(BOOT_IMAGE, e.getBootImage());
            // Localhost device should be parsed out
            assertEquals(1, e.getHwAddresses().size());
            assertEquals(MAC_ADDRESS, e.getHwAddresses().get(0));
        });
    }

    /**
     * Tests default PXE event handling
     *
     * Processing the event, when repart or redeploy flags are not set and when tuning-saltboot is minion pillar
     * @throws Exception
     */
    @Test
    public void testPXEEventAction() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);

        Event event = mock(Event.class);
        Map<String, Object> data = createTestData(minion.getMinionId(), SALTBOOT_GROUP, "root=/dev/sda1",
                "/dev/sda1", BOOT_IMAGE, "custom=option", true);
        context().checking(new Expectations() {{
            allowing(event).getTag();
            will(returnValue("suse/manager/pxe_update"));
            allowing(event).getData();
            will(returnValue(data));
        }});

        Optional<PXEEvent> pxeevent = PXEEvent.parse(event);
        assertTrue(pxeevent.isPresent());

        PXEEventMessage pxemsg = new PXEEventMessage(pxeevent.get());
        PXEEventMessageAction action = new PXEEventMessageAction();

        // Add pillar data without any redeployment flag
        Set<Pillar> pillars = new HashSet<>();
        pillars.add(new Pillar(SALTBOOT_PILLAR, createSaltbootTestData(null, null, true), minion));
        minion.setPillars(pillars);
        minion = TestUtils.saveAndReload(minion);

        assertTrue(minion.getPillarByCategory(SALTBOOT_PILLAR).isPresent());

        // Do the thing
        action.execute(pxemsg);

        // Validate pillar data, there shouldn't be any saltboot redeploy pillars
        minion = TestUtils.reload(minion);
        assertTrue(minion.getPillarByCategory(SALTBOOT_PILLAR).isPresent());
        Pillar returnedPillar = minion.getPillarByCategory(SALTBOOT_PILLAR).get();
        Map<String, Object> saltboot = (Map<String, Object>)returnedPillar.getPillar().get("saltboot");
        assertNull(saltboot.get("force_redeploy"));
        assertNull(saltboot.get("force_repartition"));

        // Validate custom info data, there shouldn't be any
        CustomDataKey saltbootRedeploy = OrgFactory.lookupKeyByLabelAndOrg("saltboot_force_redeploy", minion.getOrg());
        CustomDataValue redeploy = minion.getCustomDataValue(saltbootRedeploy);
        assertNull(redeploy);

        CustomDataKey saltbootRepart = OrgFactory.lookupKeyByLabelAndOrg("saltboot_force_repartition", minion.getOrg());
        CustomDataValue repart = minion.getCustomDataValue(saltbootRepart);
        assertNull(repart);

        // System should be registered in cobbler
        assertNotNull(minion.getCobblerId());
        assertNotNull(SystemRecord.lookupByName(cobblerMock,
                                                SaltbootUtils.makeCobblerName(user.getOrg(), minion.getMinionId())));
    }

    /**
     * Tests default PXE event handling
     *
     * Processing the event, when repart or redeploy flags are not set and when saltboot-formula is group formula
     * Check we do not do any modifications
     * @throws Exception
     */
    @Test
    public void testPXEEventActionNoFlag() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        ServerGroup hwtypeGroup = ServerGroupFactory.create("HWTYPE:test", "testgroup", user.getOrg());
        minion.addGroup(hwtypeGroup);

        Event event = mock(Event.class);
        Map<String, Object> data = createTestData(minion.getMinionId(), SALTBOOT_GROUP, "root=/dev/sda1",
                "/dev/sda1", BOOT_IMAGE, "custom=option", true);
        context().checking(new Expectations() {{
            allowing(event).getTag();
            will(returnValue("suse/manager/pxe_update"));
            allowing(event).getData();
            will(returnValue(data));
        }});

        Optional<PXEEvent> pxeevent = PXEEvent.parse(event);
        assertTrue(pxeevent.isPresent());

        PXEEventMessage pxemsg = new PXEEventMessage(pxeevent.get());
        PXEEventMessageAction action = new PXEEventMessageAction();


        // Create group saltboot pillar
        Set<Pillar> pillars = new HashSet<>();
        pillars.add(new Pillar(SALTBOOT_FORMULA, createSaltbootTestData(null, null), hwtypeGroup));
        hwtypeGroup.setPillars(pillars);
        hwtypeGroup = TestUtils.saveAndReload(hwtypeGroup);
        assertTrue(hwtypeGroup.getPillarByCategory(SALTBOOT_FORMULA).isPresent());

        // Add pillar data without any redeployment flag
        pillars = new HashSet<>();
        pillars.add(new Pillar(SALTBOOT_PILLAR, createSaltbootTestData(false, null), minion));
        minion.setPillars(pillars);
        minion = TestUtils.saveAndReload(minion);
        assertTrue(minion.getPillarByCategory(SALTBOOT_PILLAR).isPresent());

        // Do the thing
        action.execute(pxemsg);

        // Validate pillar data
        minion = TestUtils.reload(minion);
        hwtypeGroup = TestUtils.reload(hwtypeGroup);
        assertFalse(minion.getPillarByCategory(SALTBOOT_PILLAR).isPresent());
        assertTrue(hwtypeGroup.getPillarByCategory(SALTBOOT_FORMULA).isPresent());
        Pillar returnedPillar = hwtypeGroup.getPillarByCategory(SALTBOOT_FORMULA).get();
        Map<String, Object> saltboot = (Map<String, Object>)returnedPillar.getPillar().get("saltboot");
        assertNull(saltboot.get("force_redeploy"));
        assertNull(saltboot.get("force_repartition"));

        // Validate custom info data
        CustomDataKey saltbootRedeploy = OrgFactory.lookupKeyByLabelAndOrg("saltboot_force_redeploy", minion.getOrg());
        CustomDataValue redeploy = minion.getCustomDataValue(saltbootRedeploy);
        assertNull(redeploy);

        CustomDataKey saltbootRepart = OrgFactory.lookupKeyByLabelAndOrg("saltboot_force_repartition", minion.getOrg());
        CustomDataValue repart = minion.getCustomDataValue(saltbootRepart);
        assertNull(repart);

        assertNotNull(minion.getCobblerId());
        assertNotNull(SystemRecord.lookupByName(cobblerMock,
                                                SaltbootUtils.makeCobblerName(user.getOrg(), minion.getMinionId())));
    }

    /**
     * Test we clear redeploy and repartition pillar when they are set for minion
     * @throws Exception
     */
    @Test
    public void testPXEEventActionPillarOnly() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        ServerGroup hwtypeGroup = ServerGroupFactory.create("HWTYPE:test", "testgroup", user.getOrg());
        minion.addGroup(hwtypeGroup);

        Event event = mock(Event.class);
        Map<String, Object> data = createTestData(minion.getMinionId(), SALTBOOT_GROUP, "root=/dev/sda1",
                "/dev/sda1", BOOT_IMAGE, "custom=option", true);
        context().checking(new Expectations() {{
            allowing(event).getTag();
            will(returnValue("suse/manager/pxe_update"));
            allowing(event).getData();
            will(returnValue(data));
        }});

        Optional<PXEEvent> pxeevent = PXEEvent.parse(event);
        assertTrue(pxeevent.isPresent());

        PXEEventMessage pxemsg = new PXEEventMessage(pxeevent.get());
        PXEEventMessageAction action = new PXEEventMessageAction();

        // Generic group pillar
        Set<Pillar> pillars = new HashSet<>();
        pillars.add(new Pillar(SALTBOOT_FORMULA, createSaltbootTestData(null, null), hwtypeGroup));
        hwtypeGroup.setPillars(pillars);
        hwtypeGroup = TestUtils.saveAndReload(hwtypeGroup);

        // Override by system pillar with redeploy flag
        pillars = new HashSet<>();
        pillars.add(new Pillar(SALTBOOT_PILLAR, createSaltbootTestData(true, true, true), minion));
        minion.setPillars(pillars);
        minion = TestUtils.saveAndReload(minion);

        assertTrue(hwtypeGroup.getPillarByCategory(SALTBOOT_FORMULA).isPresent());
        assertTrue(minion.getPillarByCategory(SALTBOOT_PILLAR).isPresent());
        Pillar returnedPillar = hwtypeGroup.getPillarByCategory(SALTBOOT_FORMULA).get();
        Map<String, Object> saltboot = (Map<String, Object>)returnedPillar.getPillar().get("saltboot");
        assertNull(saltboot.get("force_redeploy"));
        assertNull(saltboot.get("force_repartition"));
        returnedPillar = minion.getPillarByCategory(SALTBOOT_PILLAR).get();
        saltboot = (Map<String, Object>)returnedPillar.getPillar().get("saltboot");
        assertNotNull(saltboot.get("force_redeploy"));
        assertNotNull(saltboot.get("force_repartition"));

        // Do the thing
        action.execute(pxemsg);

        // Validate pillar data
        // group data should not be touched, minion data should be reset
        minion = TestUtils.reload(minion);
        hwtypeGroup = TestUtils.reload(hwtypeGroup);
        assertTrue(minion.getPillarByCategory(SALTBOOT_PILLAR).isPresent());
        assertTrue(hwtypeGroup.getPillarByCategory(SALTBOOT_FORMULA).isPresent());
        returnedPillar = hwtypeGroup.getPillarByCategory(SALTBOOT_FORMULA).get();
        saltboot = (Map<String, Object>)returnedPillar.getPillar().get("saltboot");
        assertNull(saltboot.get("force_redeploy"));
        assertNull(saltboot.get("force_repartition"));
        returnedPillar = minion.getPillarByCategory(SALTBOOT_PILLAR).get();
        saltboot = (Map<String, Object>)returnedPillar.getPillar().get("saltboot");
        assertNull(saltboot.get("force_redeploy"));
        assertNull(saltboot.get("force_repartition"));

        // Validate custom info data
        CustomDataKey saltbootRedeploy = OrgFactory.lookupKeyByLabelAndOrg("saltboot_force_redeploy", minion.getOrg());
        CustomDataValue redeploy = minion.getCustomDataValue(saltbootRedeploy);
        assertNull(redeploy);

        CustomDataKey saltbootRepart = OrgFactory.lookupKeyByLabelAndOrg("saltboot_force_repartition", minion.getOrg());
        CustomDataValue repart = minion.getCustomDataValue(saltbootRepart);
        assertNull(repart);

        assertNotNull(minion.getCobblerId());
        assertNotNull(SystemRecord.lookupByName(cobblerMock,
                                                SaltbootUtils.makeCobblerName(user.getOrg(), minion.getMinionId())));
    }

    /**
     * Tests PXE event handling with custom info set to redeployment
     *
     * Processing the event, when repart or redeploy flags are not set and when saltboot-formula is group formula
     * Check that we do not modify group formula
     * @throws Exception
     */
    @Test
    public void testPXEEventActionCustomInfoOnly() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        ServerGroup hwtypeGroup = ServerGroupFactory.create("HWTYPE:test", "testgroup", user.getOrg());

        Event event = mock(Event.class);
        Map<String, Object> data = createTestData(minion.getMinionId(), SALTBOOT_GROUP, "root=/dev/sda1",
                "/dev/sda1", BOOT_IMAGE, "custom=option", true);
        context().checking(new Expectations() {{
            allowing(event).getTag();
            will(returnValue("suse/manager/pxe_update"));
            allowing(event).getData();
            will(returnValue(data));
        }});

        Optional<PXEEvent> pxeevent = PXEEvent.parse(event);
        assertTrue(pxeevent.isPresent());

        PXEEventMessage pxemsg = new PXEEventMessage(pxeevent.get());
        PXEEventMessageAction action = new PXEEventMessageAction();

        Set<Pillar> pillars = new HashSet<>();
        pillars.add(new Pillar(SALTBOOT_FORMULA, createSaltbootTestData(null, null), hwtypeGroup));
        hwtypeGroup.setPillars(pillars);
        hwtypeGroup = TestUtils.saveAndReload(hwtypeGroup);

        assertFalse(minion.getPillarByCategory(SALTBOOT_PILLAR).isPresent());
        assertTrue(hwtypeGroup.getPillarByCategory(SALTBOOT_FORMULA).isPresent());

        CustomDataKey saltbootRedeploy = createTestCustomDataKey(user, "saltboot_force_redeploy");
        CustomDataKey saltbootRepart = createTestCustomDataKey(user, "saltboot_force_repartition");

        user.getOrg().addCustomDataKey(saltbootRedeploy);
        user.getOrg().addCustomDataKey(saltbootRepart);

        createTestCustomDataValue(user, saltbootRedeploy, minion, "True");
        createTestCustomDataValue(user, saltbootRepart, minion, "True");

        // Do the thing
        action.execute(pxemsg);

        // Validate pillar data
        minion = TestUtils.reload(minion);
        hwtypeGroup = TestUtils.reload(hwtypeGroup);
        assertFalse(minion.getPillarByCategory(SALTBOOT_PILLAR).isPresent());
        assertTrue(hwtypeGroup.getPillarByCategory(SALTBOOT_FORMULA).isPresent());
        Pillar returnedPillar = hwtypeGroup.getPillarByCategory(SALTBOOT_FORMULA).get();
        Map<String, Object> saltboot = (Map<String, Object>)returnedPillar.getPillar().get("saltboot");
        assertNull(saltboot.get("force_redeploy"));
        assertNull(saltboot.get("force_repartition"));

        // Validate custom info data
        saltbootRedeploy = OrgFactory.lookupKeyByLabelAndOrg("saltboot_force_redeploy", minion.getOrg());
        CustomDataValue redeploy = minion.getCustomDataValue(saltbootRedeploy);
        assertNull(redeploy);

        saltbootRepart = OrgFactory.lookupKeyByLabelAndOrg("saltboot_force_repartition", minion.getOrg());
        CustomDataValue repart = minion.getCustomDataValue(saltbootRepart);
        assertNull(repart);

        assertNotNull(minion.getCobblerId());
        assertNotNull(SystemRecord.lookupByName(cobblerMock,
                                                SaltbootUtils.makeCobblerName(user.getOrg(), minion.getMinionId())));
    }
}
