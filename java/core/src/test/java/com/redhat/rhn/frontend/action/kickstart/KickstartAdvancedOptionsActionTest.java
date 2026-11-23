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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.frontend.action.kickstart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartDataTest;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartInstallType;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.testing.RhnPostMockStrutsTestCase;
import com.redhat.rhn.testing.TestUtils;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * KickstartAdvancedOptionsTest
 */
public class KickstartAdvancedOptionsActionTest extends RhnPostMockStrutsTestCase {

    protected KickstartData ksdata;
    protected KickstartData ksdataOptions;

    @BeforeEach
    public void setUp() throws Exception {
        this.ksdata = KickstartDataTest.createKickstartWithChannel(user.getOrg());
        this.ksdataOptions = KickstartDataTest.createKickstartWithOptions(user.getOrg());

        ksdata = TestUtils.saveAndFlush(ksdata);
        ksdataOptions = TestUtils.saveAndFlush(ksdataOptions);

        addRequestParameter(RequestContext.KICKSTART_ID, this.ksdata.getId().toString());
    }

    @Test
    public void testExecute() {
        setRequestPathInfo("/kickstart/KickstartOptionsEdit");
        actionPerform();
        assertNotNull(request.getAttribute(KickstartAdvancedOptionsAction.OPTIONS));
    }

    @Test
    public void testSubmit() {
        setRequestPathInfo("/kickstart/KickstartOptionsEdit");
        addRequestParameter(KickstartAdvancedOptionsAction.SUBMITTED,
                Boolean.TRUE.toString());

        // setup some required fields
        addRequestParameter("keyboard", "keyboard");
        addRequestParameter("keyboard_txt", "US");
        addRequestParameter("lang", "lang");
        addRequestParameter("lang_txt", "en_US");
        addRequestParameter("langsupport", "langsupport");
        addRequestParameter("langsupport_txt", "--default en_US");
        addRequestParameter("mouse", "mouse");
        addRequestParameter("mouse_txt", "none");
        addRequestParameter("bootloader", "bootloader");
        addRequestParameter("bootloader_txt", "--location mbr");
        addRequestParameter("timezone", "timezone");
        addRequestParameter("timezone_txt", "America/New_York");
        addRequestParameter("auth", "auth");
        addRequestParameter("auth_txt", "--enablemd5 --enableshadow");
        addRequestParameter("rootpw", "rootpw");
        addRequestParameter("rootpw_txt", "$1$nCCVpGg");
        addRequestParameter("customOptions", "repo blah");

        // setup a non required field
        addRequestParameter("skipx", "skipx");

        actionPerform();
        assertNotNull(request.getAttribute(RequestContext.KICKSTART));
        assertNotNull(request.getParameter("rootpw"));
        assertNotNull(request.getParameter("keyboard"));
        assertNotNull(request.getParameter("lang"));
        assertNotNull(request.getParameter("langsupport"));
        assertNotNull(request.getParameter("mouse"));
        assertNotNull(request.getParameter("bootloader"));
        assertNotNull(request.getParameter("timezone"));
        assertNotNull(request.getParameter("auth"));
        assertNotNull(request.getParameter("skipx"));

        String[] keys = {"kickstart.options.success"};
        testActionHasMessages(keys);

        // Verify we can submit twice
        actionPerform();
        testActionHasMessages(keys);
    }

    /**
     * auth (authconfig) is deprecated in newer kickstart versions, so a profile
     * must be savable without it.
     */
    @Test
    public void testSubmitWithoutAuth() {
        setRequestPathInfo("/kickstart/KickstartOptionsEdit");
        addRequestParameter(KickstartAdvancedOptionsAction.SUBMITTED,
                Boolean.TRUE.toString());

        // every required field except auth
        addRequestParameter("keyboard", "keyboard");
        addRequestParameter("keyboard_txt", "US");
        addRequestParameter("lang", "lang");
        addRequestParameter("lang_txt", "en_US");
        addRequestParameter("langsupport", "langsupport");
        addRequestParameter("langsupport_txt", "--default en_US");
        addRequestParameter("mouse", "mouse");
        addRequestParameter("mouse_txt", "none");
        addRequestParameter("bootloader", "bootloader");
        addRequestParameter("bootloader_txt", "--location mbr");
        addRequestParameter("timezone", "timezone");
        addRequestParameter("timezone_txt", "America/New_York");
        addRequestParameter("rootpw", "rootpw");
        addRequestParameter("rootpw_txt", "$1$nCCVpGg");

        actionPerform();
        String[] keys = {"kickstart.options.success"};
        testActionHasMessages(keys);
    }

    /**
     * On Enterprise Linux 10 the auth command was removed from the kickstart
     * syntax, so an EL10 autoinstallation profile must be savable without the
     * auth field (issue #12252). The root password (rootpw) stays mandatory.
     */
    @Test
    public void testSubmitEl10WithoutAuth() throws Exception {
        KickstartData el10 = createKickstartWithInstallType(KickstartInstallType.RHEL_10);

        setRequestPathInfo("/kickstart/KickstartOptionsEdit");
        addRequestParameter(KickstartAdvancedOptionsAction.SUBMITTED,
                Boolean.TRUE.toString());
        addRequestParameter(RequestContext.KICKSTART_ID, el10.getId().toString());

        // all mandatory fields except auth (which EL10 does not support)
        addRequiredFieldsExceptAuth();

        actionPerform();

        String[] keys = {"kickstart.options.success"};
        testActionHasMessages(keys);
        // no mandatory-field error must be reported for auth on EL10
        testActionHasNoErrors();
        assertNotNull(request.getAttribute(RequestContext.KICKSTART));

        // the stored profile must not contain an auth command
        KickstartData stored = KickstartFactory.lookupKickstartDataByIdAndOrg(
                user.getOrg(), el10.getId());
        assertTrue(stored.getOptions().stream()
                        .noneMatch(c -> "auth".equals(c.getCommandName().getName())),
                "auth must not be stored for an EL10 profile");
        assertTrue(stored.getOptions().stream()
                        .anyMatch(c -> "rootpw".equals(c.getCommandName().getName())),
                "rootpw must be stored for an EL10 profile");
    }

    /**
     * Negative control for the EL10 test above: a genuinely mandatory field
     * (timezone) that is left out must still be reported as missing, and the
     * error must name timezone, not auth.
     */
    @Test
    public void testSubmitEl10MissingTimezoneReportsError() throws Exception {
        KickstartData el10 = createKickstartWithInstallType(KickstartInstallType.RHEL_10);

        setRequestPathInfo("/kickstart/KickstartOptionsEdit");
        addRequestParameter(KickstartAdvancedOptionsAction.SUBMITTED,
                Boolean.TRUE.toString());
        addRequestParameter(RequestContext.KICKSTART_ID, el10.getId().toString());

        addRequiredFieldsExceptAuth("timezone");

        actionPerform();

        String[] errorKeys = {"errors.required"};
        testActionHasErrors(errorKeys);
        // the reported field must be timezone, not the unsupported auth field
        ActionMessages errors = (ActionMessages) request.getAttribute(Globals.ERROR_KEY);
        Iterator<?> it = errors.get();
        assertTrue(it.hasNext());
        ActionMessage msg = (ActionMessage) it.next();
        Object[] values = msg.getValues();
        assertNotNull(values);
        assertEquals(1, values.length);
        assertEquals("timezone", values[0],
                "the mandatory-field error must name the actually missing field");
    }

    /**
     * Regression test for older Enterprise Linux versions (e.g. EL9): the auth
     * field is still supported there and submitting it works as before.
     */
    @Test
    public void testSubmitEl9WithAuth() throws Exception {
        KickstartData el9 = createKickstartWithInstallType(KickstartInstallType.RHEL_9);

        setRequestPathInfo("/kickstart/KickstartOptionsEdit");
        addRequestParameter(KickstartAdvancedOptionsAction.SUBMITTED,
                Boolean.TRUE.toString());
        addRequestParameter(RequestContext.KICKSTART_ID, el9.getId().toString());

        addRequiredFieldsExceptAuth();
        addRequestParameter("auth", "auth");
        addRequestParameter("auth_txt", "--enableshadow --passalgo=sha256");

        actionPerform();

        String[] keys = {"kickstart.options.success"};
        testActionHasMessages(keys);

        KickstartData stored = KickstartFactory.lookupKickstartDataByIdAndOrg(
                user.getOrg(), el9.getId());
        assertTrue(stored.getOptions().stream()
                        .anyMatch(c -> "auth".equals(c.getCommandName().getName())),
                "auth must still be stored for an EL9 profile");
    }

    private void addRequiredFieldsExceptAuth(String... alsoSkip) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("keyboard", "US");
        fields.put("lang", "en_US");
        fields.put("langsupport", "--default en_US");
        fields.put("mouse", "none");
        fields.put("bootloader", "--location mbr");
        fields.put("timezone", "America/New_York");
        fields.put("rootpw", "$1$nCCVpGg");
        for (Map.Entry<String, String> e : fields.entrySet()) {
            if (Arrays.asList(alsoSkip).contains(e.getKey())) {
                continue;
            }
            addRequestParameter(e.getKey(), e.getKey());
            addRequestParameter(e.getKey() + "_txt", e.getValue());
        }
    }

    private KickstartData createKickstartWithInstallType(String installTypeLabel)
            throws Exception {
        KickstartData k = KickstartDataTest.createKickstartWithChannel(user.getOrg());
        k.getTree().setInstallType(
                KickstartFactory.lookupKickstartInstallTypeByLabel(installTypeLabel));
        return TestUtils.saveAndFlush(k);
    }

    /*
     * need to test how hibernate handles cascading orphans from the
     * parent set. Page is loaded that has a ksdata with options already
     * set. The reqeste params will replace the existing option set
     */
    @Test
    public void testReplaceSubmit() {
        setRequestPathInfo("/kickstart/KickstartOptionsEdit");
        addRequestParameter(KickstartAdvancedOptionsAction.SUBMITTED,
                Boolean.TRUE.toString());

        // setup some required fields
        addRequestParameter("keyboard", "keyboard");
        addRequestParameter("keyboard_txt", "US");
        addRequestParameter("lang", "lang");
        addRequestParameter("lang_txt", "en_US");
        addRequestParameter("langsupport", "langsupport");
        addRequestParameter("langsupport_txt", "--default en_US");
        addRequestParameter("mouse", "mouse");
        addRequestParameter("mouse_txt", "");
        addRequestParameter("bootloader", "bootloader");
        addRequestParameter("bootloader_txt", "--location mbr");
        addRequestParameter("timezone", "timezone");
        addRequestParameter("timezone_txt", "America/New_York");
        addRequestParameter("auth", "auth");
        addRequestParameter("auth_txt", "--enablemd5 --enableshadow");
        addRequestParameter("rootpw", "rootpw");
        addRequestParameter("rootpw_txt", "badpassword");
        addRequestParameter("customOptions", "repo blah");

        // setup a non required field
        addRequestParameter("skipx", "skipx");
        addRequestParameter(RequestContext.KICKSTART_ID,
                this.ksdataOptions.getId().toString());
        actionPerform();
        assertNotNull(request.getAttribute(RequestContext.KICKSTART));

        String[] keys = {"kickstart.options.success"};
        testActionHasMessages(keys);
    }


}
