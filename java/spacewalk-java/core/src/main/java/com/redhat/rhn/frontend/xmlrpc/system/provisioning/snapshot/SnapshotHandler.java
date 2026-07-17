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
package com.redhat.rhn.frontend.xmlrpc.system.provisioning.snapshot;

import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.redhat.rhn.domain.rhnpackage.PackageNevra;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerSnapshot;
import com.redhat.rhn.domain.server.SnapshotTag;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.InvalidArgsException;
import com.redhat.rhn.frontend.xmlrpc.InvalidSystemException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchSnapshotException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchSystemException;
import com.redhat.rhn.frontend.xmlrpc.SnapshotLookupException;
import com.redhat.rhn.frontend.xmlrpc.SnapshotTagAlreadyExistsException;
import com.redhat.rhn.frontend.xmlrpc.TaskomaticApiException;
import com.redhat.rhn.frontend.xmlrpc.system.XmlRpcSystemHelper;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.api.ApiIgnore;
import com.suse.manager.api.ApiType;
import com.suse.manager.api.ReadOnly;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * SnapshotHandler
 * @apidoc.namespace system.provisioning.snapshot
 * @apidoc.doc Provides methods to access and delete system snapshots.
 */
public class SnapshotHandler extends BaseHandler {

    private final XmlRpcSystemHelper xmlRpcSystemHelper;

    /**
     * @param xmlRpcSystemHelperIn XmlRpcSystemHelper
     */
    public SnapshotHandler(XmlRpcSystemHelper xmlRpcSystemHelperIn) {
        xmlRpcSystemHelper = xmlRpcSystemHelperIn;
    }

    /**
     * List the snapshots for a given system that were created on or between
     * the dates specified.
     *
     * This version of the method is intended for HTTP only, because null {@link Date}s are not allowed in XMLRPC
     * @param loggedInUser The current user
     * @param sid system id
     * @param startDate start date
     * @param endDate end date
     * @return list of server snapshots
     * @since 10.1
     *
     * @apidoc.doc List snapshots for a given system.
     * A user may optionally provide a start and end date to narrow the snapshots that
     * will be listed.  For example,
     * <ul>
     * <li>If the user provides startDate only, all snapshots created either on or after
     * the date provided will be returned.</li>
     * <li>If user provides startDate and endDate, all snapshots created on or between the
     * dates provided will be returned.</li>
     * <li>If the user doesn't provide a startDate and endDate, all snapshots associated
     * with the server will be returned.</li>
     * </ul>
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param($date, "startDate")
     * @apidoc.param #param($date, "endDate")
     * @apidoc.returntype
     *  #return_array_begin()
     *      $ServerSnapshotSerializer
     *  #array_end()
     */
    @ReadOnly
    @ApiIgnore(ApiType.XMLRPC)
    public List<ServerSnapshot> listSnapshots(User loggedInUser, Integer sid, Date startDate, Date endDate) {
        Server server = lookupServer(loggedInUser, sid);
        return ServerFactory.listSnapshots(server.getOrg(), server, startDate, endDate);
    }

    /**
     * List the snapshots for a given system that were created on or between
     * the dates specified.
     *
     * This version of the method is intended for XMLRPC only, because {@link Map}s are not allowed in HTTP query params
     * @param loggedInUser The current user
     * @param sid system id
     * @param dateDetails map containing optional start/end date
     * @return list of server snapshots
     * @since 10.1
     *
     * @apidoc.doc List snapshots for a given system.
     * A user may optionally provide a start and end date to narrow the snapshots that
     * will be listed.  For example,
     * <ul>
     * <li>If the user provides startDate only, all snapshots created either on or after
     * the date provided will be returned.</li>
     * <li>If user provides startDate and endDate, all snapshots created on or between the
     * dates provided will be returned.</li>
     * <li>If the user doesn't provide a startDate and endDate, all snapshots associated
     * with the server will be returned.</li>
     * </ul>
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param
     *     #struct_begin("dateDetails")
     *         #prop_desc($date, "startDate", "Optional, unless endDate
     *         is provided.")
     *         #prop_desc($date, "endDate", "Optional.")
     *     #struct_end()
     * @apidoc.returntype
     *  #return_array_begin()
     *      $ServerSnapshotSerializer
     *  #array_end()
     */
    @ReadOnly
    @ApiIgnore(ApiType.HTTP)
    public List<ServerSnapshot> listSnapshots(User loggedInUser, Integer sid, Map<String, Date> dateDetails) {
        validateDateKeys(dateDetails);
        Date startDate = null;
        Date endDate = null;

        if (dateDetails.containsKey("startDate")) {
            startDate = dateDetails.get("startDate");
        }
        if (dateDetails.containsKey("endDate")) {
            endDate = dateDetails.get("endDate");
        }
        return listSnapshots(loggedInUser, sid, startDate, endDate);
    }

    /**
     * list the packages for a given snapshot
     * @param loggedInUser The current user
     * @param snapId snapshot id
     * @return Set of packageNevra objects
     * @since 10.1
     *
     * @apidoc.doc List the packages associated with a snapshot.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "snapId")
     * @apidoc.returntype
     *      #return_array_begin()
     *         $PackageNevraSerializer
     *     #array_end()
     */
    @ReadOnly
    public Set<PackageNevra> listSnapshotPackages(User loggedInUser, Integer snapId) {
        ServerSnapshot snap = lookupSnapshot(loggedInUser, snapId);
        return snap.getPackages();

    }

    /**
     * list the config files for a given snapshot
     * @param loggedInUser The current user
     * @param snapId snapshot id
     * @return Set of ConfigRevision objects
     * @since 10.2
     *
     * @apidoc.doc List the config files associated with a snapshot.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "snapId")
     * @apidoc.returntype
     *      #return_array_begin()
     *         $ConfigRevisionSerializer
     *     #array_end()
     */
    @ReadOnly
    public Set<ConfigRevision> listSnapshotConfigFiles(User loggedInUser, Integer snapId) {
        ServerSnapshot snap = lookupSnapshot(loggedInUser, snapId);
        return snap.getConfigRevisions();
    }

    /**
     * Deletes all snapshots across multiple systems.
     * @param loggedInUser The current user
     * @param startDate start date
     * @param endDate end date
     * @return 1 on success
     * @since 10.1
     *
     * @apidoc.doc  Deletes all snapshots across multiple systems based on the given date
     * criteria.  For example,
     * <ul>
     * <li>If the user provides startDate only, all snapshots created either on or after
     * the date provided will be removed.</li>
     * <li>If user provides startDate and endDate, all snapshots created on or between the
     * dates provided will be removed.</li>
     * <li>If the user doesn't provide a startDate and endDate, all snapshots will be
     * removed.</li>
     * </ul>
     *
     * @apidoc.param #session_key()
     * @apidoc.param #param($date, "startDate")
     * @apidoc.param #param($date, "endDate")
     * @apidoc.returntype #return_int_success()
     */
    @ApiIgnore(ApiType.XMLRPC)
    public int deleteSnapshots(User loggedInUser, Date startDate, Date endDate) {
        ServerFactory.deleteSnapshots(loggedInUser.getOrg(), startDate, endDate);
        return 1;
    }

    /**
     * Deletes all snapshots for a given system based on the given date criteria.
     * @param loggedInUser The current user
     * @param sid system id
     * @param startDate start date
     * @param endDate end date
     * @return 1 on success
     * @since 10.1
     *
     * @apidoc.doc  Deletes all snapshots for a given system based on the date
     * criteria.  For example,
     * <ul>
     * <li>If the user provides startDate only, all snapshots created either on or after
     * the date provided will be removed.</li>
     * <li>If user provides startDate and endDate, all snapshots created on or between the
     * dates provided will be removed.</li>
     * <li>If the user doesn't provide a startDate and endDate, all snapshots associated
     * with the server will be removed.</li>
     * </ul>
     *
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "sid", "ID of system to delete snapshots for")
     * @apidoc.param #param($date, "startDate")
     * @apidoc.param #param($date, "endDate")
     * @apidoc.returntype #return_int_success()
     */
    @ApiIgnore(ApiType.XMLRPC)
    public int deleteSnapshots(User loggedInUser, Integer sid, Date startDate, Date endDate) {
        Server server = lookupServer(loggedInUser, sid);
        ServerFactory.deleteSnapshots(loggedInUser.getOrg(), server, startDate, endDate);
        return 1;
    }

    /**
     * Deletes all snapshots across multiple systems.
     * @param loggedInUser The current user
     * @param dateDetails map containing optional start/end Date objects.
     * @return 1 on success
     * @since 10.1
     *
     * @apidoc.doc  Deletes all snapshots across multiple systems based on the given date
     * criteria.  For example,
     * <ul>
     * <li>If the user provides startDate only, all snapshots created either on or after
     * the date provided will be removed.</li>
     * <li>If user provides startDate and endDate, all snapshots created on or between the
     * dates provided will be removed.</li>
     * <li>If the user doesn't provide a startDate and endDate, all snapshots will be
     * removed.</li>
     * </ul>
     *
     * @apidoc.param #session_key()
     * @apidoc.param
     *     #struct_begin("dateDetails")
     *         #prop_desc($date, "startDate", "Optional, unless endDate
     *         is provided.")
     *         #prop_desc($date, "endDate", "Optional.")
     *     #struct_end()
     * @apidoc.returntype #return_int_success()
     */
    @ApiIgnore(ApiType.HTTP)
    public int deleteSnapshots(User loggedInUser, Map dateDetails) {
        validateDateKeys(dateDetails);
        Date startDate = null;
        Date endDate = null;

        if (dateDetails.containsKey("startDate")) {
            startDate = parseInputValue(dateDetails.get("startDate"), Date.class);
        }
        if (dateDetails.containsKey("endDate")) {
            endDate = parseInputValue(dateDetails.get("endDate"), Date.class);
        }
        return deleteSnapshots(loggedInUser, startDate, endDate);
    }

    /**
     * Deletes all snapshots for a given system based on the given date criteria.
     * @param loggedInUser The current user
     * @param sid system id
     * @param dateDetails map containing optional start/end Date objects.
     * @return 1 on success
     * @since 10.1
     *
     * @apidoc.doc  Deletes all snapshots for a given system based on the date
     * criteria.  For example,
     * <ul>
     * <li>If the user provides startDate only, all snapshots created either on or after
     * the date provided will be removed.</li>
     * <li>If user provides startDate and endDate, all snapshots created on or between the
     * dates provided will be removed.</li>
     * <li>If the user doesn't provide a startDate and endDate, all snapshots associated
     * with the server will be removed.</li>
     * </ul>
     *
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "sid", "ID of system to delete
     *          snapshots for")
     * @apidoc.param
     *     #struct_begin("dateDetails")
     *         #prop_desc($date, "startDate", "Optional, unless endDate
     *         is provided.")
     *         #prop_desc($date, "endDate", "Optional.")
     *     #struct_end()
     * @apidoc.returntype #return_int_success()
     */
    @ApiIgnore(ApiType.HTTP)
    public int deleteSnapshots(User loggedInUser, Integer sid, Map<String, Date> dateDetails) {
        validateDateKeys(dateDetails);
        Date startDate = null;
        Date endDate = null;

        if (dateDetails.containsKey("startDate")) {
            startDate = dateDetails.get("startDate");
        }
        if (dateDetails.containsKey("endDate")) {
            endDate = dateDetails.get("endDate");
        }
        return deleteSnapshots(loggedInUser, sid, startDate, endDate);
    }

    /**
     * Deletes a snapshot
     * @param loggedInUser The current user
     * @param snapId id of snapshot
     * @return 1 on success
     * @since 10.1
     *
     * @apidoc.doc  Deletes a snapshot with the given snapshot id
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "snapId", "ID of snapshot to delete")
     * @apidoc.returntype #return_int_success()
     */
    public int deleteSnapshot(User loggedInUser, Integer snapId) {
        ServerSnapshot snap = lookupSnapshot(loggedInUser, snapId);
        ServerFactory.deleteSnapshot(snap);
        return 1;
    }

    /**
     * Adds tag to snapshot
     * @param loggedInUser The current user
     * @param snapId shapshot id
     * @param tagName name iof the snapshot tag
     * @return 1 on success
     *
     * @apidoc.doc Adds tag to snapshot
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "snapId", "ID of the snapshot")
     * @apidoc.param #param_desc("string", "tagName", "Name of the snapshot tag")
     * @apidoc.returntype #return_int_success()
     */
    public int addTagToSnapshot(User loggedInUser, Integer snapId, String tagName) {
        ServerSnapshot snap = lookupSnapshot(loggedInUser, snapId);
        if (!snap.addTag(tagName)) {
            throw new SnapshotTagAlreadyExistsException(tagName);
        }
        return 1;
    }

    /**
     * Private helper method to lookup a server from an sid, and throws a FaultException
     * if the server cannot be found.
     * @param user The user looking up the server
     * @param sid The id of the server we're looking for
     * @return Returns the server corresponding to sid
     * @throws NoSuchSystemException A NoSuchSystemException is thrown if the server
     * corresponding to sid cannot be found.
     */
    private Server lookupServer(User user, Integer sid) throws NoSuchSystemException {
        return xmlRpcSystemHelper.lookupServer(user, sid);
    }

    private ServerSnapshot lookupSnapshot(User user, Integer snapId)
        throws NoSuchSnapshotException {
        ServerSnapshot snap = ServerFactory.lookupSnapshotById(snapId);
        if (snap == null) {
            throw new NoSuchSnapshotException(snapId);
        }
        lookupServer(user, snap.getServer().getId().intValue());
        return snap;
    }

    private void validateDateKeys(Map map) throws InvalidArgsException {
        // confirm that map contains only valid keys
        Set<String> validKeys = new HashSet<>();
        validKeys.add("startDate");
        validKeys.add("endDate");
        validateMap(validKeys, map);
    }

    /**
     * @param loggedInUser The current user
     * @param sid server ID
     * @param snapId snapshot ID
     * @return 1 in case of success, exception thrown otherwise
     * @apidoc.doc Rollbacks server to snapshot
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param_desc("int", "snapId", "ID of the snapshot")
     * @apidoc.returntype #return_int_success()
     */
    public int rollbackToSnapshot(User loggedInUser, Integer sid,
            Integer snapId) {
        try {
            Server server = lookupServer(loggedInUser, sid);

            if (server == null) {
                throw new InvalidSystemException();
            }

            ServerSnapshot snapshot =
                    ServerFactory.lookupSnapshotById(snapId);
            if (snapshot == null) {
                throw new SnapshotLookupException(snapId);
            }
            doRollback(loggedInUser, snapshot);
            return BaseHandler.VALID;
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
    }

    /**
     * @param loggedInUser The current user
     * @param sid server ID
     * @param tagName Snapshot tag name
     * @return 1 in case of success, exception thrown otherwise
     * @apidoc.doc Rollbacks server to snapshot
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param_desc("string", "tagName", "Name of the snapshot tag")
     * @apidoc.returntype #return_int_success()
     */
    public int rollbackToTag(User loggedInUser, Integer sid, String tagName) {
        try {
            SnapshotTag tag = ServerFactory.lookupSnapshotTagbyName(tagName);
            for (ServerSnapshot snapshot : tag.getSnapshots()) {
                if (snapshot.getServer().getId() == sid.longValue()) {
                    doRollback(loggedInUser, snapshot);
                }
            }
            return BaseHandler.VALID;
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
    }

    /**
     * @param loggedInUser The current user
     * @param tagName Snapshot tag name
     * @return 1 in case of success, exception thrown otherwise
     * @apidoc.doc Rollbacks server to snapshot
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "tagName", "Name of the snapshot tag")
     * @apidoc.returntype #return_int_success()
     */
    public int rollbackToTag(User loggedInUser, String tagName) {
        try {
            SnapshotTag tag = ServerFactory.lookupSnapshotTagbyName(tagName);
            for (ServerSnapshot snapshot : tag.getSnapshots()) {
                doRollback(loggedInUser, snapshot);
            }
            return BaseHandler.VALID;
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
    }

    private void doRollback(User loggedInUser, ServerSnapshot snapshot)
        throws com.redhat.rhn.taskomatic.TaskomaticApiException {
        SystemManager.ensureAvailableToUser(loggedInUser, snapshot.getServer().getId());
        ActionManager.checkConfigActionOnServer(ActionFactory.TYPE_CONFIGFILES_DEPLOY,
                snapshot.getServer());
        snapshot.cancelPendingActions();
        snapshot.rollbackChannels();
        snapshot.rollbackGroups();
        snapshot.rollbackPackages(loggedInUser);
        snapshot.rollbackConfigFiles(loggedInUser);
    }
}
