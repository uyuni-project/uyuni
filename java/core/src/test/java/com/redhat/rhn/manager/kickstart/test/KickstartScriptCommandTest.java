/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.manager.kickstart.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartScript;
import com.redhat.rhn.manager.kickstart.KickstartScriptCreateCommand;
import com.redhat.rhn.manager.kickstart.KickstartScriptDeleteCommand;
import com.redhat.rhn.manager.kickstart.KickstartScriptEditCommand;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * KickstartScriptTest
 */
public class KickstartScriptCommandTest extends BaseKickstartCommandTestCase {


    /**
     * {@inheritDoc}
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

    }

    @Test
    public void testPreCreate() {
        // Lets zero out the scripts
        ksdata.getScripts().clear();
        KickstartFactory.saveKickstartData(ksdata);
        ksdata = (KickstartData) reload(ksdata);
        assertEquals(0, ksdata.getScripts().size());

        // Now make sure we add a new one.
        String language = "/usr/bin/perl";
        String contents = "print \"some string\";\n";
        String chroot = "N";
        KickstartScriptCreateCommand cmd = new
            KickstartScriptCreateCommand(ksdata.getId(), user);
        assertNotNull(cmd.getKickstartData().getScripts());
        KickstartScript kss = cmd.getScript();
        assertNotNull(kss.getScriptType());
        cmd.setScript(language, contents, KickstartScript.TYPE_PRE, chroot, false, null,
                false);
        cmd.store();
        ksdata = (KickstartData) reload(ksdata);
        assertEquals(contents, cmd.getContents());
        assertEquals(language, cmd.getLanguage());
        assertFalse(ksdata.getScripts().isEmpty());
    }

    @Test
    public void testPreEdit() {
        KickstartScript kss = ksdata.getScripts().iterator().next();
        String language = "/usr/bin/perl";
        String contents = "print \"some string\";\n";
        String chroot = "Y";
        KickstartScriptEditCommand cmd =
            new KickstartScriptEditCommand(ksdata.getId(), kss.getId(), user);
        cmd.setScript(language, contents, KickstartScript.TYPE_PRE, chroot, true, null,
                false);
        cmd.store();
        ksdata = (KickstartData) reload(ksdata);
        assertEquals(contents, cmd.getContents());
        assertEquals(language, cmd.getLanguage());
        assertFalse(ksdata.getScripts().isEmpty());
    }

    @Test
    public void testScriptDelete() {

        KickstartScript kss = ksdata.getScripts().iterator().next();
        assertEquals(5, ksdata.getScripts().size());
        KickstartScriptDeleteCommand cmd = new KickstartScriptDeleteCommand(ksdata.getId(),
                kss.getId(), user);
        cmd.store();
        ksdata = (KickstartData) reload(ksdata);
        assertEquals(4, ksdata.getScripts().size());
    }

}
