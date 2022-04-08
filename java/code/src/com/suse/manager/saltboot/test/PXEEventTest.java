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

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.Optional.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;

import com.suse.manager.saltboot.PXEEvent;
import com.suse.salt.netapi.datatypes.Event;

import com.google.gson.JsonObject;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PXEEventTest extends JMockBaseTestCaseWithUser {

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }

    private Map<String, Object> createTestData(String minionId, String saltbootGroup, String root, String saltDevice,
                                  String bootImage, String kernelOptions, boolean includeMACs) {
        Map<String, Object> data = new HashMap();
        JsonObject jsonData = new JsonObject();
        if (!saltbootGroup.isEmpty()) {
            jsonData.addProperty("minion_id_prefix", saltbootGroup);
        }
        if (!root.isEmpty()) {
            jsonData.addProperty("root", root);
        }
        if (!saltDevice.isEmpty()) {
            jsonData.addProperty("salt_device", saltDevice);
        }
        if (!bootImage.isEmpty()) {
            jsonData.addProperty("boot_image", bootImage);
        }
        if (!kernelOptions.isEmpty()) {
            jsonData.addProperty("terminal_kernel_parameters", kernelOptions);
        }
        if (includeMACs) {
            JsonObject hwAddrs = new JsonObject();
            hwAddrs.addProperty("eth1", "00:11:22:33:44:55");
            hwAddrs.addProperty("lo", "00:00:00:00:00:00");
            jsonData.add("hwaddr_interfaces", hwAddrs);
        }

        data.put("id", minionId);
        data.put("data", jsonData);

        return data;
    }
    /**
     * Tests parsing {@link PXEEvent}.
     * "Happy path" scenario.
     */
    @Test
    public void testParse() {
        Event event = mock(Event.class);
        Map<String, Object> data = createTestData("minion.local", "groupPrefix", "root=/dev/sda1",
                "/dev/sda1", "POS_Image_JeOS7-7.0.0-1", "custom=option", true);
        context().checking(new Expectations() {{
            allowing(event).getTag();
            will(returnValue("suse/manager/pxe_update"));
            allowing(event).getData();
            will(returnValue(data));
        }});

        Optional<PXEEvent> parsed = PXEEvent.parse(event);

        assertTrue(parsed.isPresent());
        parsed.ifPresent(e -> {
            assertEquals("minion.local", e.getMinionId());
            assertEquals("groupPrefix", e.getSaltbootGroup());
            assertEquals("root=/dev/sda1", e.getRoot());
            assertEquals(Optional.of("/dev/sda1"), e.getSaltDevice());
            assertEquals(Optional.of("custom=option"), e.getKernelParameters());
            assertEquals("POS_Image_JeOS7-7.0.0-1", e.getBootImage());
            // Localhost device should be parsed out
            assertTrue(e.getHwAddresses().size() == 1);
            assertEquals(e.getHwAddresses().get(0), "00:11:22:33:44:55");
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
        Map<String, Object> data = createTestData("minion.local", "groupPrefix", "root=/dev/sda1",
                "/dev/sda1", "POS_Image_JeOS7-7.0.0-1", "custom=option", false);
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
        Map<String, Object> data = createTestData("minion.local", "groupPrefix", "",
                "/dev/sda1", "POS_Image_JeOS7-7.0.0-1", "custom=option", true);
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
        Map<String, Object> data = createTestData("minion.local", "groupPrefix", "root=/dev/sda1",
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
        Map<String, Object> data = createTestData("minion.local", "groupPrefix", "root=/dev/sda1",
                "/dev/sda1", "POS_Image_JeOS7-7.0.0-1", "", true);
        context().checking(new Expectations() {{
            allowing(event).getTag();
            will(returnValue("suse/manager/pxe_update"));
            allowing(event).getData();
            will(returnValue(data));
        }});
        Optional<PXEEvent> parsed = PXEEvent.parse(event);

        assertTrue(parsed.isPresent());
        parsed.ifPresent(e -> {
            assertEquals("minion.local", e.getMinionId());
            assertEquals("groupPrefix", e.getSaltbootGroup());
            assertEquals("root=/dev/sda1", e.getRoot());
            assertEquals(Optional.of("/dev/sda1"), e.getSaltDevice());
            assertEquals(Optional.empty(), e.getKernelParameters());
            assertEquals("POS_Image_JeOS7-7.0.0-1", e.getBootImage());
            // Localhost device should be parsed out
            assertTrue(e.getHwAddresses().size() == 1);
            assertEquals(e.getHwAddresses().get(0), "00:11:22:33:44:55");
        });
    }

    /**
     * Tests parsing event when salt device not provided
     */
    @Test
    public void testParseNoSaltDevice() {
        Event event = mock(Event.class);
        Map<String, Object> data = createTestData("minion.local", "groupPrefix", "root=/dev/sda1",
                "", "POS_Image_JeOS7-7.0.0-1", "custom=option", true);
        context().checking(new Expectations() {{
            allowing(event).getTag();
            will(returnValue("suse/manager/pxe_update"));
            allowing(event).getData();
            will(returnValue(data));
        }});
        Optional<PXEEvent> parsed = PXEEvent.parse(event);

        assertTrue(parsed.isPresent());
        parsed.ifPresent(e -> {
            assertEquals("minion.local", e.getMinionId());
            assertEquals("groupPrefix", e.getSaltbootGroup());
            assertEquals("root=/dev/sda1", e.getRoot());
            assertEquals(Optional.empty(), e.getSaltDevice());
            assertEquals(Optional.of("custom=option"), e.getKernelParameters());
            assertEquals("POS_Image_JeOS7-7.0.0-1", e.getBootImage());
            // Localhost device should be parsed out
            assertTrue(e.getHwAddresses().size() == 1);
            assertEquals(e.getHwAddresses().get(0), "00:11:22:33:44:55");
        });
    }
}
