/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.xmlrpc.system.provisioning.snapshot.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageNevra;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerSnapshot;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.frontend.xmlrpc.system.XmlRpcSystemHelper;
import com.redhat.rhn.frontend.xmlrpc.system.provisioning.snapshot.SnapshotHandler;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.testing.ServerGroupTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.suse.cloud.CloudPaygManager;
import com.suse.manager.attestation.AttestationManager;
import com.suse.manager.webui.controllers.bootstrap.RegularMinionBootstrapper;
import com.suse.manager.webui.controllers.bootstrap.SSHMinionBootstrapper;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.services.test.TestSaltApi;
import com.suse.manager.webui.services.test.TestSystemQuery;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * SnapshotHandlerTest
 */
public class SnapshotHandlerTest extends BaseHandlerTestCase {

    private SaltApi saltApi = new TestSaltApi();
    private SystemQuery systemQuery = new TestSystemQuery();
    private CloudPaygManager paygManager = new CloudPaygManager();
    private AttestationManager attestationManager = new AttestationManager();
    private RegularMinionBootstrapper regularMinionBootstrapper =
            new RegularMinionBootstrapper(systemQuery, saltApi, paygManager, attestationManager);
    private SSHMinionBootstrapper sshMinionBootstrapper =
            new SSHMinionBootstrapper(systemQuery, saltApi, paygManager, attestationManager);
    private XmlRpcSystemHelper xmlRpcSystemHelper = new XmlRpcSystemHelper(
            regularMinionBootstrapper,
            sshMinionBootstrapper
    );
    private SnapshotHandler handler = new SnapshotHandler(xmlRpcSystemHelper);

    private ServerSnapshot generateSnapshot(Server server) {
        ServerSnapshot snap = new ServerSnapshot();
        snap.setServer(server);
        snap.setOrg(server.getOrg());
        snap.setReason("blah");
        return snap;
    }

    @Test
    public void testListSnapshots() {
        Server server = ServerFactoryTest.createTestServer(admin, true);
        ServerSnapshot snap = generateSnapshot(server);
        ServerGroup grp = ServerGroupTestUtils.createEntitled(server.getOrg(),
         ServerConstants.getServerGroupTypeEnterpriseEntitled());
        snap.addGroup(grp);

        TestUtils.saveAndFlush(snap);
        List<ServerSnapshot> list = handler.listSnapshots(admin, server.getId().intValue(), new HashMap<>());
        assertContains(list, snap);
        assertContains(snap.getGroups(), grp);

    }

    @Test
    public  void testListSnapshotPackages() {
        Server server = ServerFactoryTest.createTestServer(admin, true);
        ServerSnapshot snap = generateSnapshot(server);
        Package pack = PackageTest.createTestPackage(admin.getOrg());
        PackageNevra packN = new PackageNevra();
        packN.setArch(pack.getPackageArch());
        packN.setEvr(pack.getPackageEvr());
        packN.setName(pack.getPackageName());
        snap.getPackages().add(packN);
        TestUtils.saveAndFlush(packN);
        TestUtils.saveAndFlush(snap);
        Set<PackageNevra> list = handler.listSnapshotPackages(admin,
                snap.getId().intValue());
         assertContains(list, packN);
    }

    @Test
    public void testDeleteSnapshot() {
        Server server = ServerFactoryTest.createTestServer(admin, true);
        ServerSnapshot snap = generateSnapshot(server);
        TestUtils.saveAndFlush(snap);

        handler.deleteSnapshot(admin, snap.getId().intValue());
        List<ServerSnapshot> list = handler.listSnapshots(admin, server.getId().intValue(), new HashMap<>());
        assertTrue(list.isEmpty());

    }

    @Test
    public void testDeleteSnapshots() {
        Server server = ServerFactoryTest.createTestServer(admin, true);
        ServerSnapshot snap = generateSnapshot(server);
        generateSnapshot(server);
        generateSnapshot(server);
        generateSnapshot(server);
        generateSnapshot(server);
        TestUtils.saveAndFlush(snap);

        handler.deleteSnapshots(admin, server.getId().intValue(), new HashMap<>());
        List<ServerSnapshot> list = handler.listSnapshots(admin, server.getId().intValue(), new HashMap<>());
        assertTrue(list.isEmpty());
    }
}
