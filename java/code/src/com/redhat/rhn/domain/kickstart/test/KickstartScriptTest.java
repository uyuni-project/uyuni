/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.domain.kickstart.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartScript;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;

/**
 *
 * KickstartScriptTest
 */
public class KickstartScriptTest extends BaseTestCaseWithUser {

    public static final byte[] DATA = "echo \"hello world\"".getBytes();

    /*
    @Test
    public void testRevision() throws Exception {
        KickstartData ksdata = KickstartDataTest.createKickstartWithOptions(user.getOrg());
        KickstartScript script = KickstartScriptTest.createPost(ksdata);
        script.setRevision(1L);
        System.out.println("\n\n\n\n\nSSSSSSSSSSSS\n\n\n\n");
        ksdata = (KickstartData) TestUtils.saveAndReload(ksdata);
        System.out.println("\n\n\n\n\nZZZZZZZZZZZZ\n\n\n\n");
        script = ksdata.getScripts().iterator().next();
        assertNotNull(script.getRevision());
        assertNotNull(script.getId());
        KickstartScript lookedUp = (KickstartScript)  HibernateFactory.getSession()
            .getNamedQuery("KickstartScript.findLatestScriptRevisionByID")
            .setLong("id", script.getId())
            .uniqueResult();
        assertNotNull(lookedUp);
    }*/

    @Test
    public void testScript() throws Exception {
        KickstartData ksdata = KickstartDataTest.createKickstartWithOptions(user.getOrg());
        KickstartFactory.saveKickstartData(ksdata);
        ksdata = (KickstartData) reload(ksdata);
        assertNotNull(ksdata.getScripts());
        assertEquals(5, ksdata.getScripts().size());
        KickstartScript ks2 = ksdata.getScripts().iterator().next();

        assertNotNull(ks2.getDataContents());

        // Test delete
        ksdata.removeScript(ks2);
        KickstartFactory.saveKickstartData(ksdata);
        ksdata = (KickstartData) reload(ksdata);
        assertEquals(4, ksdata.getScripts().size());
    }

    @Test
    public void testMultiplePreScripts() throws Exception {
        KickstartData ksdata = KickstartDataTest.createKickstartWithOptions(user.getOrg());
        KickstartScript kss1 = createPre(ksdata);
        KickstartScript kss2 = createPre(ksdata);
        ksdata.addScript(kss1);
        ksdata.addScript(kss2);
        assertTrue(kss1.getPosition() < kss2.getPosition());
        KickstartFactory.saveKickstartData(ksdata);
        ksdata = (KickstartData) reload(ksdata);
        assertTrue(kss1.getPosition() < kss2.getPosition());
    }

    @Test
    public void testLargeScript() throws Exception {
        String largeString = RandomStringUtils.randomAscii(4000);
        KickstartData ksdata = KickstartDataTest.createKickstartWithOptions(user.getOrg());
        ksdata.getScripts().clear();
        KickstartFactory.saveKickstartData(ksdata);
        ksdata = (KickstartData) reload(ksdata);

        // Create 2 scripts, one with data, one without.
        KickstartScript script = createPost(ksdata);
        script.setPosition(1L);
        KickstartScript scriptEmpty = createPost(ksdata);
        script.setData(largeString.getBytes(StandardCharsets.UTF_8));

        // Make sure we are setting the blob to be an empty byte
        // array.  The bug happens when one script is empty.
        scriptEmpty.setData(new byte[0]);
        scriptEmpty.setPosition(2L);
        ksdata.addScript(script);
        ksdata.addScript(scriptEmpty);
        TestUtils.saveAndFlush(script);
        TestUtils.saveAndFlush(scriptEmpty);

        KickstartFactory.saveKickstartData(ksdata);
        ksdata = (KickstartData) reload(ksdata);
        Iterator i = ksdata.getScripts().iterator();
        boolean found = false;
        assertEquals(2, ksdata.getScripts().size());
        while (i.hasNext()) {
            KickstartScript loaded = (KickstartScript) i.next();
            if (loaded.getDataContents().equals(largeString)) {
                found = true;
            }
        }
        assertTrue(found);
    }



    public static KickstartScript createPreInterpreter(KickstartData k) {
        KickstartScript ks = new KickstartScript();
        ks.setInterpreter("/usr/bin/perl");
        ks.setChroot("Y");
        ks.setData(DATA);
        ks.setPosition(1L);
        ks.setScriptType(KickstartScript.TYPE_PRE);
        ks.setKsdata(k);
        ks.setRaw(true);
        return ks;
    }

    public static KickstartScript createPostInterpreter(KickstartData k) {
        KickstartScript ks = new KickstartScript();
        ks.setInterpreter("/usr/bin/python");
        ks.setChroot("Y");
        ks.setPosition(2L);
        ks.setData(DATA);
        ks.setScriptType(KickstartScript.TYPE_POST);
        ks.setKsdata(k);
        ks.setRaw(true);
        return ks;
    }

    public static KickstartScript createPostChrootInt(KickstartData k) {
        KickstartScript ks = new KickstartScript();
        ks.setInterpreter("/usr/bin/python");
        ks.setData(DATA);
        ks.setChroot("N");
        ks.setPosition(3L);
        ks.setScriptType(KickstartScript.TYPE_POST);
        ks.setKsdata(k);
        ks.setRaw(true);
        return ks;
    }

    public static KickstartScript createPre(KickstartData k) {
        KickstartScript ks = new KickstartScript();
        ks.setChroot("Y");
        ks.setData(DATA);
        ks.setPosition(4L);
        ks.setScriptType(KickstartScript.TYPE_PRE);
        ks.setKsdata(k);
        ks.setRaw(true);
        return ks;
    }

    public static KickstartScript createPost(KickstartData k) {
        KickstartScript ks = new KickstartScript();
        ks.setChroot("Y");
        ks.setData(DATA);
        ks.setPosition(5L);
        ks.setScriptType(KickstartScript.TYPE_POST);
        ks.setKsdata(k);
        ks.setRaw(true);
        return ks;
    }

}
