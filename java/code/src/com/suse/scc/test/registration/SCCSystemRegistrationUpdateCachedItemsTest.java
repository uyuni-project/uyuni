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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.domain.scc.SCCRegCacheItem;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerInfo;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerTestUtils;

import com.suse.manager.webui.services.SaltStateGeneratorService;
import com.suse.scc.model.SCCSystemCredentialsJson;
import com.suse.scc.registration.SCCSystemRegistrationContext;
import com.suse.scc.registration.SCCSystemRegistrationUpdateCachedItems;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tests {@link com.suse.scc.registration.SCCSystemRegistration}.
 */
public class SCCSystemRegistrationUpdateCachedItemsTest extends BaseTestCaseWithUser {

    private List<SCCRegCacheItem> testSystems;
    private static final String UPTIME_TEST = "[\"2024-06-26:000000000000000000001111\"," +
                                               "\"2024-06-27:111111111111110000000000\"]";

    private void setupSystems(int systemSize) throws Exception {
        Path tmpSaltRoot = Files.createTempDirectory("salt");
        SaltStateGeneratorService.INSTANCE.setSuseManagerStatesFilesRoot(tmpSaltRoot.toAbsolutePath());
        SaltStateGeneratorService.INSTANCE.setSkipSetOwner(true);

        for (int i = 0; i < systemSize; i++) {
            Server testSystem = ServerTestUtils.createTestSystem();
            ServerInfo serverInfo = testSystem.getServerInfo();
            serverInfo.setCheckin(new Date(0)); // 1970-01-01 00:00:00 UTC
            serverInfo.setUptimeData(UPTIME_TEST);
            testSystem.setServerInfo(serverInfo);
        }

        SCCCachingFactory.initNewSystemsToForward();
        List<SCCRegCacheItem> allUnregistered = SCCCachingFactory.findSystemsToForwardRegistration();
        this.testSystems = allUnregistered.stream()
                .filter(i -> i.getOptServer().get().getServerInfo().getCheckin().equals(new Date(0)))
                .collect(Collectors.toList());
    }


    /**
     * Tests when no systems are provided.
     * Asserts no changes occur in relevant context collections nor exceptions are thrown.
     */
    @Test
    public void testSuccessSCCSystemRegistrationUpdateCachedItemsWhenNoSystemsProvided() throws Exception {
        // setup
        this.setupSystems(0);

        final SCCSystemRegistrationContext context = new SCCSystemRegistrationContext(null, testSystems, null);

        // pre-conditions
        assertEquals(0, context.getItems().size());
        assertEquals(0, context.getRegisteredSystems().size());
        assertEquals(0, context.getPendingRegistrationSystemsByLogin().size());
        assertEquals(0, context.getPaygSystems().size());

        // execution
        new SCCSystemRegistrationUpdateCachedItems().handle(context);

        // assertions
        assertEquals(0, context.getItems().size());
        assertEquals(0, context.getRegisteredSystems().size());
        assertEquals(0, context.getPendingRegistrationSystemsByLogin().size());
        assertEquals(0, context.getPaygSystems().size());
    }

    /**
     * Test success when 21 systems are provided:
     * - 7 were registered successfully;
     *      - before this 7 systems will be in context.getRegisteredSystems() & there will be no items having a SccId;
     *      - after, the 7 systems will still be in context.getRegisteredSystems() but there will be 7 items having a
     *      SccId;
     * - 7 failed to register;
     *      - before there are 14 systems in context.getPendingRegistrationSystemsByLogin() & there will be no items
     *      having a registration error time;
     *      - after, 7 systems will remain in context.getPendingRegistrationSystemsByLogin() but there will be 7 items
     *      having a registration error time;
     * - 7 are PAYG systems;
     *      - before the 7 systems will be in context.getPaygSystems() & all 21 systems are marked as requiring
     *      registration;
     *      - after, the 7 systems will still be in context.getPaygSystems() but only 7 will still be marked as
     *      requiring registration (the ones that failed registration);
     */
    @Test
    public void testSuccessSCCSystemRegistrationUpdateCachedItems() throws Exception {
        // setup
        this.setupSystems(21);
        final SCCSystemRegistrationContext context = new SCCSystemRegistrationContext(null, testSystems, null);
        for (int i = 0; i < 14; i++) {
            String login = "login" + i;
            SCCSystemCredentialsJson sccSystemCredentialsJson = new SCCSystemCredentialsJson(login, login +
                    "_password", Long.valueOf(i));
            context.getItemsByLogin().put(login, testSystems.get(i));
            context.getPendingRegistrationSystemsByLogin().put(login, null);
            if (i < 7) {
                context.getRegisteredSystems().add(sccSystemCredentialsJson);
            }
        }
        context.getPaygSystems().addAll(testSystems.subList(14, 21));


        // pre-conditions
        assertEquals(21, context.getItems().size());

        assertEquals(7, context.getRegisteredSystems().size());
        assertEquals(0, context.getItems().stream().filter(p -> p.getOptSccId().isPresent()).count());

        assertEquals(14, context.getPendingRegistrationSystemsByLogin().size());
        assertEquals(0, context.getItems().stream().filter(p -> p.getOptRegistrationErrorTime().isPresent()).count());


        assertEquals(7, context.getPaygSystems().size());
        assertEquals(21, context.getItems().stream().filter(SCCRegCacheItem::isSccRegistrationRequired).count());

        // execution
        new SCCSystemRegistrationUpdateCachedItems().handle(context);

        // assertions
        assertEquals(21, context.getItems().size());

        assertEquals(7, context.getRegisteredSystems().size());
        assertEquals(7, context.getItems().stream().filter(p -> p.getOptSccId().isPresent()).count());

        assertEquals(7, context.getPendingRegistrationSystemsByLogin().size());
        assertEquals(7, context.getItems().stream().filter(p -> p.getOptRegistrationErrorTime().isPresent()).count());

        assertEquals(7, context.getPaygSystems().size());
        assertEquals(7, context.getItems().stream().filter(SCCRegCacheItem::isSccRegistrationRequired).count());
    }

}
