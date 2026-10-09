/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.autoinstallation.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.testing.MockObjectTestCase;

import com.suse.manager.autoinstallation.KernelOptionsList;

import org.cobbler.SystemRecord;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the shared kernel options helpers.
 */
public class KernelOptionsHelperTest extends MockObjectTestCase {

    private static final String FQDN = "server.example.com";

    /**
     * Enables class mocking.
     */
    @BeforeEach
    public void setUp() {
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }

    /**
     * An explicit textmode wins: text is dropped, textmode is kept.
     */
    @Test
    public void testSuseTextmodeKeepsTextmode() {
        KernelOptionsList options = new KernelOptionsList("text textmode=1 console=ttyS0");
        KernelOptionsHelper.applySuseTextmode(options);
        assertEquals("textmode=1 console=ttyS0", options.toString());
    }

    /**
     * A plain text option is translated to textmode=1.
     */
    @Test
    public void testSuseTextmodeTranslatesText() {
        KernelOptionsList options = new KernelOptionsList("text console=ttyS0");
        KernelOptionsHelper.applySuseTextmode(options);
        assertTrue(options.hasOption("textmode", "1"));
        assertFalse(options.hasOption("text"));
        assertEquals("console=ttyS0 textmode=1", options.toString());
    }

    /**
     * Without a text option nothing changes.
     */
    @Test
    public void testSuseTextmodeWithoutText() {
        KernelOptionsList options = new KernelOptionsList("console=ttyS0");
        KernelOptionsHelper.applySuseTextmode(options);
        assertEquals("console=ttyS0", options.toString());
    }

    /**
     * The nopxe URL points at the Cobbler system nopxe service.
     */
    @Test
    public void testCobblerSystemNopxeUrl() {
        SystemRecord system = mock(SystemRecord.class, "system");
        checking(new org.jmock.Expectations() {{
            allowing(system).getName(); will(returnValue("system1"));
        }});
        assertEquals("http://" + FQDN + "/cblr/svc/op/nopxe/system/system1",
                KernelOptionsHelper.cobblerSystemNopxeUrl(FQDN, system));
    }

    /**
     * A null system record is rejected.
     */
    @Test
    public void testCobblerSystemNopxeUrlNullSystem() {
        assertThrows(KernelOptionsBuilderException.class,
                () -> KernelOptionsHelper.cobblerSystemNopxeUrl(FQDN, null));
    }
}
