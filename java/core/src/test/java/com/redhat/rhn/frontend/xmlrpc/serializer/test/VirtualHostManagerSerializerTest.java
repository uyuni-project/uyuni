/*
 * Copyright (c) 2015 SUSE LLC
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

package com.redhat.rhn.frontend.xmlrpc.serializer.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.VHMCredentials;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManager;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerConfig;
import com.redhat.rhn.frontend.xmlrpc.serializer.VirtualHostManagerSerializer;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import redstone.xmlrpc.XmlRpcSerializer;

/**
 * VirtualHostManagerSerializer test
 */
public class VirtualHostManagerSerializerTest extends BaseTestCaseWithUser {

    private VirtualHostManager manager;

    /**
     * {@inheritDoc}
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        manager = new VirtualHostManager();
        manager.setLabel("myLabel");
        manager.setGathererModule("myModule");
        manager.setOrg(user.getOrg());
    }

    /**
     * Minimal test
     */
    @Test
    public void testSimple() {
        VirtualHostManagerSerializer serializer = new VirtualHostManagerSerializer();
        Writer output = new StringWriter();
        serializer.serialize(manager, output, new XmlRpcSerializer());

        String actual = output.toString();
        assertTrue(actual.contains("<name>label</name>"));
        assertTrue(actual.contains("<string>myLabel</string>"));
        assertTrue(actual.contains("<name>gatherer_module</name>"));
        assertTrue(actual.contains("<string>myModule</string>"));
        assertTrue(actual.contains("<name>org_id</name>"));
        assertTrue(actual.contains("<i4>" + user.getOrg().getId() + "</i4>"));
    }

    /**
     * Test serializing VirtualHostManager with credentials
     */
    @Test
    public void testWithCreds() {
        VHMCredentials creds = CredentialsFactory.createVHMCredentials("Somebody", "strongpass");
        manager.setCredentials(creds);

        VirtualHostManagerSerializer serializer = new VirtualHostManagerSerializer();
        Writer output = new StringWriter();
        serializer.serialize(manager, output, new XmlRpcSerializer());

        String actual = output.toString();
        assertTrue(actual.contains("<name>username</name>"));
        assertTrue(actual.contains("<string>Somebody</string>"));
        // we don't want the password in the output!
        assertFalse(actual.contains("password"));
        assertFalse(actual.contains("strongpass"));
    }

    /**
     * Test serializing VirtualHostManager with configs
     */
    @Test
    public void testWithConfigs() {
        Set<VirtualHostManagerConfig> configs = new HashSet<>();
        VirtualHostManagerConfig config = new VirtualHostManagerConfig();
        config.setVirtualHostManager(manager);
        config.setParameter("foopar");
        config.setValue("barval");
        configs.add(config);
        manager.setConfigs(configs);

        VirtualHostManagerSerializer serializer = new VirtualHostManagerSerializer();
        Writer output = new StringWriter();
        serializer.serialize(manager, output, new XmlRpcSerializer());

        String actual = output.toString();
        System.out.println(actual);
        assertTrue(actual.contains("<name>foopar</name>"));
        assertTrue(actual.contains("<string>barval</string>"));
    }

}
