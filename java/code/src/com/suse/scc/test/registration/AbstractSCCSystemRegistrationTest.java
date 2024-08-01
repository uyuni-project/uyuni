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

package com.suse.scc.test.registration;

import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.SCCCredentials;
import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.domain.scc.SCCRegCacheItem;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerInfo;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerTestUtils;

import com.suse.manager.webui.services.SaltStateGeneratorService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class AbstractSCCSystemRegistrationTest extends BaseTestCaseWithUser {

    private final SCCCredentials credentials = CredentialsFactory.createSCCCredentials("username", "password");
    private List<SCCRegCacheItem> testSystems;
    private static final String UPTIME_TEST = "[\"2024-06-26:000000000000000000001111\"," +
                                               "\"2024-06-27:111111111111110000000000\"]";

    /**
     * Sets up systems for testing purposes.
     *
     * @param systemSize     number of systems to create
     * @param paygSystemSize number of aditional payg systems to create
     * @throws Exception if createTestSystem fails
     */
    protected void setupSystems(int systemSize, int paygSystemSize) throws Exception {
        Path tmpSaltRoot = Files.createTempDirectory("salt");
        SaltStateGeneratorService.INSTANCE.setSuseManagerStatesFilesRoot(tmpSaltRoot.toAbsolutePath());
        SaltStateGeneratorService.INSTANCE.setSkipSetOwner(true);

        for (int i = 0; i < systemSize + paygSystemSize; i++) {
            Server testSystem = ServerTestUtils.createTestSystem();
            ServerInfo serverInfo = testSystem.getServerInfo();
            serverInfo.setCheckin(new Date(0)); // 1970-01-01 00:00:00 UTC
            serverInfo.setUptimeData(UPTIME_TEST);
            testSystem.setServerInfo(serverInfo);
            if (i < paygSystemSize) {
                testSystem.setPayg(true);
            }
        }

        SCCCachingFactory.initNewSystemsToForward();
        List<SCCRegCacheItem> allUnregistered = SCCCachingFactory.findSystemsToForwardRegistration();
        this.testSystems = allUnregistered.stream()
                .filter(i -> i.getOptServer().get().getServerInfo().getCheckin().equals(new Date(0)))
                .collect(Collectors.toList());
    }


    public SCCCredentials getCredentials() {
        return credentials;
    }

    public List<SCCRegCacheItem> getTestSystems() {
        return testSystems;
    }
}
