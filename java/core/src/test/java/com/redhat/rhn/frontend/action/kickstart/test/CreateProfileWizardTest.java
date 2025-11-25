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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.frontend.action.kickstart.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartInstallType;
import com.redhat.rhn.domain.kickstart.KickstartVirtualizationType;
import com.redhat.rhn.domain.kickstart.KickstartableTree;
import com.redhat.rhn.domain.kickstart.crypto.CryptoKey;
import com.redhat.rhn.domain.kickstart.crypto.test.CryptoTest;
import com.redhat.rhn.domain.kickstart.test.KickstartDataTest;
import com.redhat.rhn.domain.kickstart.test.KickstartableTreeTest;
import com.redhat.rhn.frontend.action.kickstart.CreateProfileWizardAction;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.RhnMockStrutsTestCase;
import com.redhat.rhn.testing.TestUtils;

import org.apache.struts.action.DynaActionForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CreateProfileWizardTest extends RhnMockStrutsTestCase {

    private static final String URL = "/kickstart/CreateProfileWizard";
    private static final String WIZARD_STEP = "wizardStep";
    private static final String KICKSTART_LABEL = "kickstartLabel";
    private static final String KSTREE_ID = "kstreeId";

    private String label;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        label = TestUtils.randomString();

        // Create some crypto keys that should get associated
        // with the new KickstartData
        CryptoKey sslkey = CryptoTest.createTestKey(user.getOrg());
        sslkey.setCryptoKeyType(KickstartFactory.KEY_TYPE_SSL);
        KickstartFactory.saveCryptoKey(sslkey);
        TestUtils.flushAndEvict(sslkey);

        // Create a GPG key as well, so we can test that just SSL
        // keys are associated.
        CryptoKey gpgkey = CryptoTest.createTestKey(user.getOrg());
        gpgkey.setCryptoKeyType(KickstartFactory.KEY_TYPE_GPG);
        KickstartFactory.saveCryptoKey(gpgkey);
        TestUtils.flushAndEvict(gpgkey);

    }

    @Test
    public void testNoTreesOrChannels() {
        setRequestPathInfo(URL);
        actionPerform();
        verifyNoActionMessages();
        DynaActionForm form = (DynaActionForm) getActionForm();
        if (form.get(CreateProfileWizardAction.CHANNELS) == null) {
            assertNotNull(request.getAttribute(
                CreateProfileWizardAction.NOCHANNELS_PARAM));
        }
        if (form.get(CreateProfileWizardAction.KSTREES_PARAM) == null) {
            assertNotNull(request.getAttribute(
                CreateProfileWizardAction.NOTREES_PARAM));
        }

    }

    @Test
    public void testRhel7() throws Exception {
        Channel treeChannel = ChannelFactoryTest.createTestChannel(user);
        KickstartableTree tree = KickstartableTreeTest.
            createTestKickstartableTree(treeChannel);
        tree.setInstallType(KickstartFactory.lookupKickstartInstallTypeByLabel("rhel_7"));
        KickstartFactory.saveKickstartableTree(tree);
        tree = TestUtils.reload(tree);
        setRequestPathInfo(URL);
        actionPerform();
        verifyNoActionMessages();

        // Step Three
        clearRequestParameters();
        addRequestParameter(WIZARD_STEP, "complete");
        addRequestParameter(KICKSTART_LABEL, label);
        addRequestParameter(KSTREE_ID, tree.getId().toString());
        addRequestParameter("defaultDownload", "true");
        addRequestParameter("rootPassword", "blahh");
        addRequestParameter("rootPasswordConfirm", "blahh");
        actionPerform();
        verifyNoActionMessages();
        KickstartData ksdata = KickstartFactory.lookupKickstartDataByLabelAndOrgId(
                label, user.getOrg().getId());
        assertEquals("--permissive", ksdata.getCommand("selinux").getArguments());
    }

    @Test
    public void testSuccess() throws Exception {

        Channel treeChannel = ChannelFactoryTest.createTestChannel(user);
        KickstartableTree tree = KickstartableTreeTest.
            createTestKickstartableTree(treeChannel);
        tree.setBasePath("rhn/kickstart/ks-rhel-i386-server-7");
        tree.setInstallType(KickstartFactory.
                lookupKickstartInstallTypeByLabel(KickstartInstallType.RHEL_7));
        setRequestPathInfo(URL);
        actionPerform();
        verifyNoActionMessages();


        // Step One
        clearRequestParameters();
        addRequestParameter(WIZARD_STEP, "second");
        addRequestParameter(KICKSTART_LABEL, label);
        addRequestParameter(KSTREE_ID, tree.getId().toString());
        actionPerform();

        verifyNoActionMessages();

        //Step Two
        clearRequestParameters();
        addRequestParameter(WIZARD_STEP, "third");
        addRequestParameter(KICKSTART_LABEL, label);
        addRequestParameter(KSTREE_ID, tree.getId().toString());
        addRequestParameter("defaultDownload", "true");
        actionPerform();
        verifyNoActionMessages();

        // Step Three
        clearRequestParameters();
        addRequestParameter(WIZARD_STEP, "complete");
        addRequestParameter(KICKSTART_LABEL, label);
        addRequestParameter(KSTREE_ID, tree.getId().toString());
        addRequestParameter("defaultDownload", "true");
        addRequestParameter("rootPassword", "blahh");
        addRequestParameter("rootPasswordConfirm", "blahh");
        actionPerform();
        verifyNoActionMessages();
        verifyKSCommandsDefaults(label);
    }

    @Test
    public void testFtpDownload() throws Exception {
        Channel treeChannel = ChannelFactoryTest.createTestChannel(user);
        KickstartableTree tree = KickstartableTreeTest.
            createTestKickstartableTree(treeChannel);

        setRequestPathInfo(URL);
        // Step Three
        clearRequestParameters();
        addRequestParameter(WIZARD_STEP, "third");
        addRequestParameter(KICKSTART_LABEL, label);
        addRequestParameter(KSTREE_ID, tree.getId().toString());
        addRequestParameter("defaultDownload", "false");
        addRequestParameter("userDefinedDownload", "ftp://ftp.redhat.com");
        addRequestParameter("rootPassword", "blahh");
        addRequestParameter("rootPasswordConfirm", "blahh");
        actionPerform();
        verifyNoActionMessages();

    }


    @Test
    public void testLabelValidation() {
        clearRequestParameters();
        setRequestPathInfo(URL);
        addRequestParameter(WIZARD_STEP, "second");
        addRequestParameter(KSTREE_ID, "12997");
        actionPerform();
        verifyForward("first");
    }

    @Test
    public void testKsTreeIdValidation() {
        clearRequestParameters();
        setRequestPathInfo(URL);
        addRequestParameter(WIZARD_STEP, "second");
        addRequestParameter(KICKSTART_LABEL, label);
        actionPerform();
        verifyForward("first");
    }

    @Test
    public void testDownloadValidation() throws Exception {
        clearRequestParameters();
        setRequestPathInfo(URL);
        addRequestParameter(WIZARD_STEP, "third");
        addRequestParameter(KICKSTART_LABEL, label);
        KickstartVirtualizationType type = KickstartFactory.lookupVirtualizationTypes().iterator().next();
        addRequestParameter(CreateProfileWizardAction.VIRTUALIZATION_TYPE_LABEL_PARAM, type.getLabel());
        Channel c = ChannelTestUtils.createBaseChannel(user);
        KickstartableTree tree = KickstartableTreeTest.createTestKickstartableTree(c);
        addRequestParameter(KSTREE_ID, tree.getId().toString());
        actionPerform();
        verifyForward("second");
    }

    @Test
    public void testUserDownloadValidation() throws Exception {
        clearRequestParameters();
        setRequestPathInfo(URL);
        addRequestParameter(WIZARD_STEP, "third");
        addRequestParameter(KICKSTART_LABEL, label);
        KickstartVirtualizationType type = KickstartFactory.lookupVirtualizationTypes().iterator().next();
        addRequestParameter(CreateProfileWizardAction.VIRTUALIZATION_TYPE_LABEL_PARAM,
            type.getLabel());
        Channel c = ChannelTestUtils.createBaseChannel(user);
        KickstartableTree tree = KickstartableTreeTest.createTestKickstartableTree(c);
        addRequestParameter(KSTREE_ID, tree.getId().toString());

        addRequestParameter("defaultDownload", "false");
        actionPerform();
        verifyForward("second");

        clearRequestParameters();
        setRequestPathInfo(URL);
        addRequestParameter(WIZARD_STEP, "third");
        addRequestParameter(KICKSTART_LABEL, label);
        addRequestParameter(KSTREE_ID, tree.getId().toString());
        addRequestParameter(CreateProfileWizardAction.VIRTUALIZATION_TYPE_LABEL_PARAM,
                type.getLabel());
        addRequestParameter("defaultDownload", "false");
        addRequestParameter("userDefinedDownload", "htp://blahblahblbah.com/blahblah");
        actionPerform();
        verifyForward("second");

    }

    @Test
    public void testRootPasswordValidation() {
        clearRequestParameters();
        setRequestPathInfo(URL);
        addRequestParameter(WIZARD_STEP, "complete");
        addRequestParameter(KICKSTART_LABEL, label);
        addRequestParameter(KSTREE_ID, "12997");
        addRequestParameter("defaultDownload", "true");
        addRequestParameter("rootPasswordConfirm", "blah");
        actionPerform();
        verifyForward("third");
    }

    @Test
    public void testRootPasswordConfirmValidation() {
        clearRequestParameters();
        setRequestPathInfo(URL);
        addRequestParameter(WIZARD_STEP, "complete");
        addRequestParameter(KICKSTART_LABEL, label);
        addRequestParameter(KSTREE_ID, "12997");
        addRequestParameter("defaultDownload", "true");
        addRequestParameter("rootPassword", "blah");
        actionPerform();
        verifyForward("third");
    }

    @Test
    public void testLabelAlreadyExists() throws Exception {
        KickstartData k = KickstartDataTest.createTestKickstartData(user.getOrg());
        String[] array = new String[1];
        array[0] = "kickstart.error.labelexists";
        clearRequestParameters();
        setRequestPathInfo(URL);
        addRequestParameter(WIZARD_STEP, "second");
        addRequestParameter(KICKSTART_LABEL, k.getLabel());
        addRequestParameter(KSTREE_ID, "12997");
        actionPerform();
        verifyForward("first");
        verifyActionErrors(array);
    }

    public void verifyKSCommandsDefaults(String labelIn) {
        KickstartData ksdata = KickstartFactory.lookupKickstartDataByLabelAndOrgId(
                                            labelIn, user.getOrg().getId());
        assertNotNull(ksdata);
        //checking to make sure defaults were set correctly
        assertNotNull(ksdata.getCommand("rootpw"));
        assertNotNull(ksdata.getCommand("lang"));
        assertNotNull(ksdata.getCommand("keyboard"));
        assertNotNull(ksdata.getCommand("bootloader"));
        assertNotNull(ksdata.getCommand("timezone"));
        assertNotNull(ksdata.getCommand("auth"));
        assertNotNull(ksdata.getCommand("zerombr"));
        assertNotNull(ksdata.getCommand("reboot"));
        assertNotNull(ksdata.getCommand("skipx"));
        assertNotNull(ksdata.getCommand("clearpart"));
        assertNotNull(ksdata.getCommand("selinux"));
        assertNotNull(ksdata.getCommand("text"));
        assertNotNull(ksdata.getCommand("install"));

        // Special repositories only on RHEL 8
        assertTrue(ksdata.getCommands().stream().noneMatch(cmd -> cmd.getCommandName().getName().equals("repo")));

        //checking to make sure args for the defaults were set correctly
        assertEquals("en_US", ksdata.getCommand("lang").getArguments());
        assertEquals("us", ksdata.getCommand("keyboard").getArguments());
        assertNull(ksdata.getCommand("zerombr").getArguments());
        assertEquals("--all", ksdata.getCommand("clearpart").getArguments());
        assertEquals("--location mbr", ksdata.getCommand("bootloader").getArguments());
        assertEquals("America/New_York", ksdata.getCommand("timezone").getArguments());
        assertEquals("--enableshadow --passalgo=sha256", ksdata.getCommand("auth").getArguments());
        // Test the keys associated with the profile.
        assertNotNull(ksdata.getCryptoKeys());
        assertFalse(ksdata.getCryptoKeys().isEmpty());
        for (CryptoKey key : ksdata.getCryptoKeys()) {
            assertNotEquals(key.getCryptoKeyType(), KickstartFactory.KEY_TYPE_GPG);
        }
    }
}
