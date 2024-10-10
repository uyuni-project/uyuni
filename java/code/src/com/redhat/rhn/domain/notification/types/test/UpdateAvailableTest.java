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
package com.redhat.rhn.domain.notification.types.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.notification.NotificationMessage;
import com.redhat.rhn.domain.notification.types.NotificationType;
import com.redhat.rhn.domain.notification.types.UpdateAvailable;
import com.redhat.rhn.testing.MockObjectTestCase;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UpdateAvailableTest extends MockObjectTestCase {

    private Runtime runtimeMock;
    private Process processMock;

    @BeforeEach
    public void setUp() {
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        runtimeMock = mock(Runtime.class);
        processMock = mock(Process.class);
    }

    @Test
    public void testPropertiesAndStrings() {
        UpdateAvailable notification = new UpdateAvailable(runtimeMock);
        assertEquals(NotificationType.UpdateAvailable, notification.getType());
        assertEquals(NotificationMessage.NotificationMessageSeverity.WARNING, notification.getSeverity());
        assertEquals("Updates are available.", notification.getSummary());
        if (ConfigDefaults.get().isUyuni()) {
            assertEquals("A new update for Uyuni is now available. For further details, please refer to the " +
                         "<a href=\"https://www.uyuni-project.org/pages/stable-version.html\">release notes<a>.",
                         notification.getDetails());
        }
        else {
            assertEquals("A new update for SUSE Manager is now available. For further details, please refer to the " +
                         "<a href=\"https://www.suse.com/releasenotes/x86_64/SUSE-MANAGER/4.3/index.html\">release " +
                         "notes<a>.", notification.getDetails());
        }
    }

    @Test
    public void testUpdatesAvailable() throws Exception {
        // Return 0 on all invocations of exec() -> an update is available
        checking(new Expectations() {{
             allowing(runtimeMock).exec(with(any(String[].class)));
             will(returnValue(processMock));
             allowing(processMock).waitFor();
             allowing(processMock).exitValue();
             will(returnValue(0));
        }});

        UpdateAvailable notification = new UpdateAvailable(runtimeMock);
        assertTrue(notification.updateAvailable());
    }

    @Test
    public void testNoUpdatesAvailable() throws Exception {
        // Return 1 on all invocations of exec() -> no update is available
        checking(new Expectations() {{
             allowing(runtimeMock).exec(with(any(String[].class)));
             will(returnValue(processMock));
             allowing(processMock).waitFor();
             allowing(processMock).exitValue();
             will(returnValue(1));
        }});

        UpdateAvailable notification = new UpdateAvailable(runtimeMock);
        assertFalse(notification.updateAvailable());
    }
}
