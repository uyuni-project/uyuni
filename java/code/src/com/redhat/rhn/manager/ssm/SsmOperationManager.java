/**
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
package com.redhat.rhn.manager.ssm;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.OperationDetailsDto;
import com.redhat.rhn.manager.BaseManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles the tracking of SSM asynchronous operations, providing functionality for
 * the creation, update, and retrieval of the data.
 *
 * @version $Revision$
 */
public class SsmOperationManager extends BaseManager {

    /**
     * Private constructor to enforce static nature of the class.
     */
    private SsmOperationManager() {
    }

    /**
     * Returns a list of all operations for the given user, regardless of their status.
     *
     * @param user operations returned only for this user; cannot be <code>null</code>
     * @return list of maps containing the data describing each operation
     */
    public static DataResult<OperationDetailsDto> allOperations(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user cannot be null");
        }

        SelectMode m = ModeFactory.getMode("ssm_operation_queries", "find_all_operations");

        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("user_id", user.getId());

        DataResult result = m.execute(params);
        return result;
    }

    /**
     * Returns a list of all operations for the given user that are currently executing.
     *
     * @param user operations returned only for this user; cannot be <code>null</code>
     * @return list of maps containing the data describing each matching operation
     */
    public static DataResult<OperationDetailsDto> inProgressOperations(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user cannot be null");
        }

        SelectMode m =
            ModeFactory.getMode("ssm_operation_queries", "find_operations_with_status");

        Map<String, Object> params = new HashMap<String, Object>(2);
        params.put("user_id", user.getId());
        params.put("status", SsmOperationStatus.IN_PROGRESS.getText());

        DataResult result = m.execute(params);
        return result;
    }

    /**
     * Returns a list of all operations for the given user that have completed.
     *
     * @param user operations returned only for this user; cannot be <code>null</code>
     * @return list of maps containing the data describing each matching operation
     */
    public static DataResult<OperationDetailsDto> completedOperations(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user cannot be null");
        }

        SelectMode m =
            ModeFactory.getMode("ssm_operation_queries", "find_operations_with_status");

        Map<String, Object> params = new HashMap<String, Object>(2);
        params.put("user_id", user.getId());
        params.put("status", SsmOperationStatus.COMPLETED.getText());

        DataResult result = m.execute(params);
        return result;
    }

    /**
     * Returns the details of the given operation.
     *
     * @param user        verifies that the user isn't trying to load someone else's
     *                    operation; cannot be <code>null</code>
     * @param operationId database ID of the operation to load
     * @return OperationsDto given an operation id or null.
     */
    public static OperationDetailsDto findOperationById(User user, long operationId) {
        if (user == null) {
            throw new IllegalArgumentException("user cannot be null");
        }

        SelectMode m = ModeFactory.getMode("ssm_operation_queries", "find_operation_by_id");

        Map<String, Object> params = new HashMap<String, Object>(2);
        params.put("user_id", user.getId());
        params.put("op_id", operationId);

        DataResult<OperationDetailsDto> result = m.execute(params);
        if (!result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    /**
     * Creates a new operation, defaulting the status to "in progress".
     * <p/>
     * For efficiency, this call assumes the following:
     * <ul>
     * <li>The set of servers that are taking place in the operation are already in the
     * database as an RhnSet (the name of set is passed into this call).</li>
     * <li>The server ID is stored in the first element (i.e. "element" in the set table).
     * </ul>
     * <p/>
     * This should be a safe assumption since, at very least, if all servers are taking
     * place in the operation they are already in the SSM RhnSet. If only a subset
     * is needed, a nested select can be used to drop them into a new set, preventing
     * the need to have another insert per server for this call.
     *
     * @param user        user under which to associate the operation; cannot be
     *                    <code>null</code>
     * @param messageId message id of operation description; cannot be <code>null</code>
     * @param rhnSetLabel references a RhnSet with the server IDs to associate with the
     *                    new operation; if this is <code>null</code> no mappings will
     *                    be created at this time
     * @return the id of the created operation
     */
    public static long createOperation(User user, String messageId,
                                       String rhnSetLabel) {
        if (user == null) {
            throw new IllegalArgumentException("user cannot be null");
        }

        if (messageId == null) {
            throw new IllegalArgumentException("description cannot be null");
        }

        SelectMode selectMode;
        WriteMode writeMode;
        Map<String, Object> params = new HashMap<String, Object>();

        // Select the operation ID manually from the sequence so we can add the mappings
        // from the operation to the servers
        selectMode = ModeFactory.getMode("ssm_operation_queries", "get_seq_nextval");
        DataResult nextValResult = selectMode.execute(params);
        Map<String, Object> nextValMap = (Map<String, Object>) nextValResult.get(0);
        long operationId = (Long) nextValMap.get("nextval");

        // Add the operation data
        writeMode = ModeFactory.getWriteMode("ssm_operation_queries", "create_operation");

        params.clear();
        params.put("op_id", operationId);
        params.put("user_id", user.getId());
        params.put("description", LocalizationService.getInstance().getMessage(messageId));
        params.put("status", SsmOperationStatus.IN_PROGRESS.getText());

        writeMode.executeUpdate(params);

        // Add the server/operation mappings
        if (rhnSetLabel != null) {
            associateServersWithOperation(operationId, user.getId(), rhnSetLabel);
        }

        return operationId;
    }

    /**
     * Indicates the operation has completed, updating its status to indicate this.
     *
     * @param user        verifies that the user isn't trying to load someone else's
     *                    operation; cannot be <code>null</code>
     * @param operationId database ID of the operation to update
     */
    public static void completeOperation(User user, long operationId) {
        if (user == null) {
            throw new IllegalArgumentException("user cannot be null");
        }

        WriteMode m =
            ModeFactory.getWriteMode("ssm_operation_queries", "update_status");

        Map<String, Object> params = new HashMap<String, Object>(3);
        params.put("user_id", user.getId());
        params.put("op_id", operationId);
        params.put("status", SsmOperationStatus.COMPLETED.getText());

        m.executeUpdate(params);
    }

    /**
     * Returns a list of servers that took part in the given SSM operation.
     *
     * @param operationId operation for which to return the server IDs
     * @return list of maps, one per server ID, where each map contains a single
     *         entry (key: server_id) containing the server ID
     */
    public static DataResult findServerDataForOperation(long operationId) {
        SelectMode m = ModeFactory.getMode("ssm_operation_queries",
            "find_server_data_for_operation_id");

        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("op_id", operationId);

        // list of maps of server_id -> <id>
        DataResult result = m.execute(params);
        return result;
    }

    /**
     * Associates an operation with a group of servers against which it was run, where
     * the servers are found in an RhnSet. The IDs for these servers must be stored in
     * the "element" field of the RhnSet.
     *
     * @param operationId identifies an existing operation to associate with servers
     * @param userId      identifies the user performing the operation
     * @param setLabel    identifies the set in which to find server IDs
     */
    public static void associateServersWithOperation(long operationId, long userId,
                                                     String setLabel) {
        WriteMode writeMode =
            ModeFactory.getWriteMode("ssm_operation_queries", "map_servers_to_operation");

        Map<String, Object> params = new HashMap<String, Object>(3);
        params.put("op_id", operationId);
        params.put("user_id", userId);
        params.put("set_label", setLabel);

        writeMode.executeUpdate(params);
    }

    /**
     * Associates an operation with a group of servers against which it was run, where
     * a list of server ids are passed in
     * @param operationId identifies an existing operation to associate with servers
     * @param userId      identifies the user performing the operation
     * @param sidsIn the list server ids
     */
    public static void associateServersWithOperation(long operationId, long userId,
                                                         List<Long> sidsIn) {
        WriteMode writeMode =
            ModeFactory.getWriteMode("ssm_operation_queries",
                                        "map_sids_to_operation");
        Map<String, Object> params = new HashMap<String, Object>(2);
        params.put("op_id", operationId);
        params.put("user_id", userId);
        writeMode.executeUpdate(params, sidsIn);
    }

    /**
     * Updates an association between an operation and a server adding a note.
     * @param operationId identifies an existing operation
     * @param serverId identifies the server on which the operation failed
     * @param note note to be added
     */
    public static void addNoteToOperationOnServer(long operationId, long serverId,
        String note) {
        WriteMode writeMode = ModeFactory.getWriteMode("ssm_operation_queries",
            "add_note_to_operation_on_server");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("op_id", operationId);
        params.put("server_id", serverId);
        params.put("note", note);
        writeMode.executeUpdate(params);
    }
}

