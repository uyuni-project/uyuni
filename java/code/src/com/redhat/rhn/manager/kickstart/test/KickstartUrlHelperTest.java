/*
 * Copyright (c) 2009--2017 Red Hat, Inc.
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.security.SessionSwap;
import com.redhat.rhn.domain.common.CommonFactory;
import com.redhat.rhn.domain.common.TinyUrl;
import com.redhat.rhn.domain.kickstart.KickstartSession;
import com.redhat.rhn.domain.kickstart.test.KickstartSessionTest;
import com.redhat.rhn.manager.kickstart.KickstartUrlHelper;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;


/**
 * Test for urlhelper
 *
 */
public class KickstartUrlHelperTest extends BaseKickstartCommandTestCase {

    private KickstartUrlHelper helper;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        helper = new KickstartUrlHelper(ksdata, "spacewalk.example.com");
    }

    @Test
    public void testGetKickstartFileUrl() {
        String expected = "http://spacewalk.example.com/" +
            "ks/cfg/org/" + ksdata.getOrg().getId() + "/label/" +
            ksdata.getLabel();
        assertEquals(expected, helper.getKickstartFileUrl());
    }

    @Test
    public void testGetKickstartFileUrlBase() {
        String expected = "http://spacewalk.example.com/" +
                "ks/cfg/org/" + ksdata.getOrg().getId();
        assertEquals(expected, helper.getKickstartFileUrlBase());

    }

    @Test
    public void testGetKickstartFileUrlIpRange() {
        String expected = "http://spacewalk.example.com/" +
            "ks/cfg/org/" + ksdata.getOrg().getId() + "/mode/ip_range";
        assertEquals(expected, helper.getKickstartFileUrlIpRange());

    }

    @Test
    public void testGetKickstartOrgDefaultUrl() {
        String expected = "http://spacewalk.example.com/" +
            "ks/cfg/org/" + ksdata.getOrg().getId() + "/org_default";

        assertEquals(expected, helper.getKickstartOrgDefaultUrl());
    }

    @Test
    public void testGetKickstartMediaPath() {
        String expected = null;
        Long orgId = ksdata.getKickstartDefaults().getKstree().getOrgId();
        if (orgId == null) {
            expected = KickstartUrlHelper.KS_DIST + "/" +
                    ksdata.getKickstartDefaults().getKstree().getLabel();
        }
        else {
            expected = KickstartUrlHelper.KS_DIST + "/org/" + orgId + "/" +
                    ksdata.getKickstartDefaults().getKstree().getLabel();
        }
       assertEquals(expected, helper.getKickstartMediaPath());
    }

    @Test
    public void testGetKickstartMediaUrl() {
        Long orgId = ksdata.getKickstartDefaults().getKstree().getOrgId();
        String expected = "http://spacewalk.example.com" + KickstartUrlHelper.KS_DIST;
        if (orgId == null) {
            expected = expected + "/" +
                    ksdata.getKickstartDefaults().getKstree().getLabel();
        }
        else {
            expected = expected + "/org/" + orgId + "/" +
                    ksdata.getKickstartDefaults().getKstree().getLabel();
        }
        assertEquals(expected, helper.getKickstartMediaUrl());

    }


    @Test
    public void testGetCobblerMediaUrl() {
        helper = new KickstartUrlHelper(ksdata);
        String expected = "http://" +
            KickstartUrlHelper.COBBLER_SERVER_VARIABLE +
            "$" + KickstartUrlHelper.COBBLER_MEDIA_VARIABLE;

        assertEquals(expected, helper.getCobblerMediaUrl());
    }

    @Test
    public void testGetCobblerMediaUrlBase() {
        helper = new KickstartUrlHelper(ksdata);
        String expected = "http://" +
            KickstartUrlHelper.COBBLER_SERVER_VARIABLE;

        assertEquals(expected, helper.getCobblerMediaUrlBase());
    }

    @Test
    public void testGetKickstartMediaSessionUrl() throws Exception {
        // /ks/dist/session/35x45fed383beaeb31a184166b4c1040633/ks-f9-x86_64
        KickstartSession session =
            KickstartSessionTest.createKickstartSession(ksdata, user);
        TestUtils.saveAndFlush(session);
        session = (KickstartSession) reload(session);
        String encodedId = SessionSwap.encodeData(session.getId().toString());
        String expected = "http://spacewalk.example.com/" +
            "ty/" + "";
        String url = helper.getKickstartMediaSessionUrl(session);
        // "http://spacewalk.example.com/ty/weOyQenH";
        String token = url.substring(url.lastIndexOf("/"));
        token = token.split("/")[1];
        TinyUrl ty = CommonFactory.lookupTinyUrl(token);
        assertNotNull(ty);
        assertTrue(url.startsWith(expected));
        // /ks/dist/session/143x8fb9d782967b2736618b2b4a9169c975/
        //   ks-ChannelLabelGS5CmSOIuu9Vu2dOkc
        String expectedRealPath = KickstartUrlHelper.KS_DIST + "/session/" + encodedId +
            "/" + ksdata.getTree().getLabel();
        assertEquals(expectedRealPath, ty.getUrl());

    }


    @Test
    public void testGetKickstartMediaSessionPath() throws Exception {
        // /ks/dist/session/35x45fed383beaeb31a184166b4c1040633/ks-f9-x86_64
        KickstartSession session =
            KickstartSessionTest.createKickstartSession(ksdata, user);
        TestUtils.saveAndFlush(session);
        session = (KickstartSession) reload(session);
        String encodedId = SessionSwap.encodeData(session.getId().toString());
        String expected = "/ty/" + "";
        String url = helper.getKickstartMediaPath(session, new Date());
        // "/ty/weOyQenH";
        String token = url.substring(url.lastIndexOf("/"));
        token = token.split("/")[1];
        TinyUrl ty = CommonFactory.lookupTinyUrl(token);
        assertNotNull(ty);
        System.out.println("TY: " + url + " expect: " + expected);
        assertTrue(url.startsWith(expected));
        // /ks/dist/session/143x8fb9d782967b2736618b2b4a9169c975/
        //   ks-ChannelLabelGS5CmSOIuu9Vu2dOkc
        String expectedRealPath = KickstartUrlHelper.KS_DIST + "/session/" + encodedId +
            "/" + ksdata.getTree().getLabel();
        assertEquals(expectedRealPath, ty.getUrl());

    }

}
