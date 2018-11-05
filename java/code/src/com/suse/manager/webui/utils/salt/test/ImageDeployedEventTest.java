/**
 * Copyright (c) 2018 SUSE LLC
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

package com.suse.manager.webui.utils.salt.test;

import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.suse.manager.webui.utils.salt.ImageDeployedEvent;
import com.suse.salt.netapi.datatypes.Event;
import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;

import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * Tests for the {@link com.suse.manager.webui.utils.salt.ImageDeployedEvent}.
 */
public class ImageDeployedEventTest extends JMockBaseTestCaseWithUser {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ClassImposteriser.INSTANCE);
    }

    /**
     * Tests parsing {@link com.suse.manager.webui.utils.salt.ImageDeployedEvent}.
     * "Happy path" scenario.
     */
    public void testParse() {
        Event event = mock(Event.class);
        String machineId = "12345";
        Map<String, Object> grains = singletonMap("machine_id", machineId);
        context().checking(new Expectations() {{
            allowing(event).getTag();
            will(returnValue("suse/manager/image_deployed"));
            allowing(event).getData();
            Map<String, Object> innerData = singletonMap("grains", grains);
            Map<String, Object> data = singletonMap("data", innerData);
            will(returnValue(data));
        }});

        Optional<ImageDeployedEvent> parsed = ImageDeployedEvent.parse(event);

        assertTrue(parsed.isPresent());
        parsed.ifPresent(evt -> {
            assertEquals(of(machineId), evt.getMachineId());
        });
    }

    /**
     * Tests parsing event with unmatching tag.
     */
    public void testParseNotMatchingTag() {
        Event event = mock(Event.class);
        context().checking(new Expectations() {{
            allowing(event).getTag();
            will(returnValue("youDontKnowMe"));
        }});
        assertEquals(empty(), ImageDeployedEvent.parse(event));
    }

    /**
     * Tests parsing event with unmatching tag.
     */
    public void testParseNoGrains() {
        Event event = mock(Event.class);
        context().checking(new Expectations() {{
            allowing(event).getTag();
            will(returnValue("suse/manager/image_deployed"));
            allowing(event).getData();
            Map<String, Object> data = singletonMap("data", emptyMap());
            will(returnValue(data));
        }});
        assertEquals(empty(), ImageDeployedEvent.parse(event));
    }
}
