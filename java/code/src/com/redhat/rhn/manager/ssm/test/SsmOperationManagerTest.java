/**
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
package com.redhat.rhn.manager.ssm.test;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.rhnset.RhnSetElement;
import com.redhat.rhn.domain.rhnset.SetCleanup;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.OperationDetailsDto;
import com.redhat.rhn.frontend.dto.ServerOperationDataDto;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.rhnset.RhnSetManager;
import com.redhat.rhn.manager.ssm.SsmOperationManager;
import com.redhat.rhn.manager.ssm.SsmOperationStatus;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.UserTestUtils;

import java.util.ArrayList;
import java.util.Set;

/**
 * @version $Revision$
 */
public class SsmOperationManagerTest extends RhnBaseTestCase {

    private static final String EXPECTED_NOTE = "Test note";

    private User ssmUser;

    private RhnSet serverSet;
    private String serverSetLabel;

    protected void setUp() throws Exception {
        super.setUp();
        ssmUser = UserTestUtils.findNewUser("ssmuser", "ssmorg");
        serverSetLabel = populateRhnSet();
    }

    public void testCreateAndAllOperations() throws Exception {
        // Test
        SsmOperationManager.createOperation(ssmUser, "Test operation", serverSetLabel);

        DataResult result = SsmOperationManager.allOperations(ssmUser);

        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
    }


    public void testCreateAndAllOperations2() throws Exception {
        long operationId = SsmOperationManager.createOperation(ssmUser,
                                            "Test testCreateAndAllOperations2 ", null);
        SsmOperationManager.associateServersWithOperation(operationId, ssmUser.getId(),
                                                new ArrayList<Long>(serverSet.
                                                                    getElementValues()));
        DataResult result = SsmOperationManager.allOperations(ssmUser);

        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    public void testCreateCompleteAndInProgressOperations() throws Exception {
        // Test
        long completeMeId =
            SsmOperationManager.createOperation(ssmUser,
                "Test operation 1", serverSetLabel);
        SsmOperationManager.createOperation(ssmUser, "Test operation 2", serverSetLabel);

        SsmOperationManager.completeOperation(ssmUser, completeMeId);

        // Verify

        //   Verify counts for all and in progress operations
        DataResult<OperationDetailsDto> all = SsmOperationManager.allOperations(ssmUser);
        DataResult<OperationDetailsDto> inProgress = SsmOperationManager.
                                                    inProgressOperations(ssmUser);

        assertEquals(2, all.size());
        assertEquals(1, inProgress.size());

        //   Verify the completed operation has its progress set to 100
        for (OperationDetailsDto operation : all) {
            if (operation.getId() == completeMeId) {
                assertEquals(SsmOperationStatus.COMPLETED.getText(),
                    operation.getStatus());
            }
        }
    }

    public void testCreateAndFindOperation() throws Exception {
        // Test
        long operationId =
            SsmOperationManager.createOperation(ssmUser,
                "Test operation 1", serverSetLabel);

        OperationDetailsDto operation = SsmOperationManager.
                                    findOperationById(ssmUser, operationId);

        // Verify
        assertNotNull(operation);

        assertTrue(operation.getDescription().contains("Test operation 1"));
        assertEquals(SsmOperationStatus.IN_PROGRESS.getText(), operation.getStatus());
        assertNotNull(operation.getStarted());
        assertNotNull(operation.getModified());
    }

    public void testFindNonExistentOperation() {
        // Test
        OperationDetailsDto result = SsmOperationManager.
                                        findOperationById(ssmUser, 100000L);

        // Verify
        assertNull(result);
    }

    public void testFindServerDataForOperation() throws Exception {
        // Setup
        long operationId =
            SsmOperationManager.createOperation(ssmUser, "Test operation", serverSetLabel);

        // Test
        DataResult result = SsmOperationManager.findServerDataForOperation(operationId);

        // Verify
        assertNotNull(result);
        assertEquals(2, result.size());

        ServerOperationDataDto serverData = (ServerOperationDataDto) result.get(0);
        assertNotNull(serverData.getId());
        assertNotNull(serverData.getName());
        assertNull(serverData.getNote());
    }

    public void testAssociateServersWithOperation() throws Exception {
        // Setup

        //   Pass null label so no servers are associated
        long operationId =
            SsmOperationManager.createOperation(ssmUser, "Test operation", null);

        //   Sanity check
        DataResult result = SsmOperationManager.findServerDataForOperation(operationId);
        assertNotNull(result);
        assertEquals(0, result.size());

        // Test
        SsmOperationManager.associateServersWithOperation(operationId, ssmUser.getId(),
            serverSetLabel);

        // Verify
        result = SsmOperationManager.findServerDataForOperation(operationId);
        assertNotNull(result);
        assertEquals(2, result.size());

        ServerOperationDataDto serverData = (ServerOperationDataDto) result.get(0);
        assertNotNull(serverData.getId());
        assertNotNull(serverData.getName());
        assertNull(serverData.getNote());
    }

    /**
     * This test should ensure that if the associate method is called with two sets
     * that may both contain one or more of the same server, only one entry is made
     * for the server.
     * <p/>
     * The driving use case behind this is the scenario where a server is subscribed to
     * one channel and unsubscribed from another in the same SSM batch task.
     *
     * @throws Exception if there is an error running the test
     */
    public void testAssociateServersWithOperationMultipleSets() throws Exception {
        // Setup

        //   Pass null label so no servers are associated
        long operationId =
            SsmOperationManager.createOperation(ssmUser, "Test operation", null);

        //   Sanity check
        DataResult result = SsmOperationManager.findServerDataForOperation(operationId);
        assertNotNull(result);
        assertEquals(0, result.size());

        //   Populate second set with one of the servers from the first
        RhnSetDecl setDecl =
            RhnSetDecl.findOrCreate("SsmOperationManagerTestSet2", SetCleanup.NOOP);
        RhnSet secondSet = setDecl.create(ssmUser);
        secondSet.addElement(serverSet.getElements().iterator().next().getElement());
        RhnSetManager.store(secondSet);
        String secondSetLabel = secondSet.getLabel();

        // Test
        SsmOperationManager.associateServersWithOperation(operationId, ssmUser.getId(),
            serverSetLabel);
        SsmOperationManager.associateServersWithOperation(operationId, ssmUser.getId(),
            secondSetLabel);

        // Verify
        result = SsmOperationManager.findServerDataForOperation(operationId);
        assertNotNull(result);
        assertEquals(2, result.size());

        ServerOperationDataDto serverData = (ServerOperationDataDto) result.get(0);
        assertNotNull(serverData.getId());
        assertNotNull(serverData.getName());
        assertNull(serverData.getNote());
    }

    /**
     * This test should ensure that only one association is created a single set references
     * the same server more than once (this could occur if the second element in the set
     * differs).
     * <p/>
     * The driving use case behind this is the scenario where a server is subscribed to
     * two different channels in the same SSM batch task.
     *
     * @throws Exception if there is an error running the test
     */
    public void testAssociateServersWithOperationDuplicateServer() throws Exception {
        // Setup

        //   Pass null label so no servers are associated
        long operationId =
            SsmOperationManager.createOperation(ssmUser, "Test operation", null);

        //   Sanity check
        DataResult result = SsmOperationManager.findServerDataForOperation(operationId);
        assertNotNull(result);
        assertEquals(0, result.size());

        //   Populate second set so we don't mangle the common one to all tests
        Server testServer = ServerFactoryTest.createTestServer(ssmUser, true);

        RhnSetDecl setDecl =
            RhnSetDecl.findOrCreate("SsmOperationManagerTestSet3", SetCleanup.NOOP);
        RhnSet secondSet = setDecl.create(ssmUser);

        secondSet.addElement(testServer.getId(), 1L);
        secondSet.addElement(testServer.getId(), 2L);

        RhnSetManager.store(secondSet);
        String secondSetLabel = secondSet.getLabel();

        // Test
        SsmOperationManager.associateServersWithOperation(operationId, ssmUser.getId(),
            secondSetLabel);

        // Verify
        result = SsmOperationManager.findServerDataForOperation(operationId);
        assertNotNull(result);
        assertEquals(1, result.size());

        ServerOperationDataDto serverData = (ServerOperationDataDto) result.get(0);
        assertNotNull(serverData.getId());
        assertNotNull(serverData.getName());
        assertNull(serverData.getNote());
    }

    /**
     * Tests a failed server-operation association.
     * @throws Exception when things go wrong
     */
    @SuppressWarnings("unchecked")
    public void testAssociateServersWithFailedOperation() throws Exception {
        //   Pass null label so no servers are associated
        long operationId =
            SsmOperationManager.createOperation(ssmUser, "Test operation", null);

        // Test
        SsmOperationManager.associateServersWithOperation(operationId, ssmUser.getId(),
            serverSetLabel);
        for (RhnSetElement server : (Set<RhnSetElement>)serverSet) {
            SsmOperationManager.addNoteToOperationOnServer(operationId,
                server.getElement(), EXPECTED_NOTE);
        }

        // Verify
        DataResult<Object> result = SsmOperationManager
            .findServerDataForOperation(operationId);
        assertNotNull(result);
        assertEquals(2, result.size());

        ServerOperationDataDto serverData = (ServerOperationDataDto) result.get(0);
        assertNotNull(serverData.getId());
        assertNotNull(serverData.getName());
        assertEquals(EXPECTED_NOTE, serverData.getNote());
    }

    /**
     * Populates an RhnSet with server IDs.
     *
     * @return label referencing the set that was populated
     * @throws Exception if there is an error creating a server
     */
    private String populateRhnSet() throws Exception {
        RhnSetDecl setDecl =
            RhnSetDecl.findOrCreate("SsmOperationManagerTestSet", SetCleanup.NOOP);
        serverSet = setDecl.create(ssmUser);

        for (int ii = 0; ii < 2; ii++) {
            Server testServer = ServerFactoryTest.createTestServer(ssmUser, true);
            serverSet.addElement(testServer.getId());
        }

        RhnSetManager.store(serverSet);

        return serverSet.getLabel();
    }
}
