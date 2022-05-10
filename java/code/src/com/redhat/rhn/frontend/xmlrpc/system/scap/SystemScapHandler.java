/*
 * Copyright (c) 2012--2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.xmlrpc.system.scap;

import com.redhat.rhn.domain.action.scap.ScapAction;
import com.redhat.rhn.domain.audit.ScapFactory;
import com.redhat.rhn.domain.audit.XccdfTestResult;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.XccdfRuleResultDto;
import com.redhat.rhn.frontend.dto.XccdfTestResultDto;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.InvalidSystemException;
import com.redhat.rhn.frontend.xmlrpc.TaskomaticApiException;
import com.redhat.rhn.manager.MissingCapabilityException;
import com.redhat.rhn.manager.MissingEntitlementException;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.audit.ScapManager;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.api.ReadOnly;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * SystemScapHandler
 * @apidoc.namespace system.scap
 * @apidoc.doc Provides methods to schedule SCAP scans and access the results.
 */
public class SystemScapHandler extends BaseHandler {

    /**
     * List OpenSCAP XCCDF scans for a given system.
     * @param loggedInUser The current user
     * @param sid The server ID.
     * @return a list of dto holding this info.
     *
     * @apidoc.doc Return a list of finished OpenSCAP scans for a given system.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype
     * #return_array_begin()
     *   $XccdfTestResultDtoSerializer
     * #array_end()
     */
    @ReadOnly
    public List<XccdfTestResultDto> listXccdfScans(User loggedInUser, Integer sid) {
        /* Make sure the system is available to user and throw a nice exception.
         * If it was not done, an empty list would be returned. */
        SystemManager.ensureAvailableToUser(loggedInUser, Long.valueOf(sid));
        return ScapManager.latestTestResultByServerId(loggedInUser, Long.valueOf(sid));
    }

    /**
     * Get Details of given OpenSCAP XCCDF scan.
     * @param loggedInUser The current user
     * @param xid The id of XCCDF scan.
     * @return a details of OpenSCAP XCCDF scan.
     *
     * @apidoc.doc Get details of given OpenSCAP XCCDF scan.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "xid", "ID of XCCDF scan.")
     * @apidoc.returntype $XccdfTestResultSerializer
     */
    @ReadOnly
    public XccdfTestResult getXccdfScanDetails(User loggedInUser, Integer xid) {
        ScapManager.ensureAvailableToUser(loggedInUser, Long.valueOf(xid));
        return ScapFactory.lookupTestResultById(Long.valueOf(xid));
    }

    /**
     * List RuleResults for given XCCDF Scan.
     * @param loggedInUser The current user
     * @param xid The id of XCCDF scan.
     * @return a list of RuleResults for given scan.
     *
     * @apidoc.doc Return a full list of RuleResults for given OpenSCAP XCCDF scan.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "xid", "ID of XCCDF scan.")
     * @apidoc.returntype
     * #return_array_begin()
     *   $XccdfRuleResultDtoSerializer
     * #array_end()
     */
    @ReadOnly
    public List<XccdfRuleResultDto> getXccdfScanRuleResults(User loggedInUser,
            Integer xid) {
        ScapManager.ensureAvailableToUser(loggedInUser, Long.valueOf(xid));
        return ScapManager.ruleResultsPerScan(Long.valueOf(xid));
    }

    /**
     * Delete OpenSCAP XCCDF Scan from the database.
     * @param loggedInUser The current user
     * @param xid The id of XCCDF scan.
     * @return a boolean indicating success of the operation.
     *
     * @apidoc.doc Delete OpenSCAP XCCDF Scan from the #product() database. Note that
     * only those SCAP Scans can be deleted which have passed their retention period.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "xid", "ID of XCCDF scan.")
     * @apidoc.returntype #param_desc("boolean", "status", "indicates success of the operation")
     */
    public Boolean deleteXccdfScan(User loggedInUser, Integer xid) {
        ScapManager.ensureAvailableToUser(loggedInUser, Long.valueOf(xid));
        return ScapManager.deleteScan(Long.valueOf(xid));
    }

    /**
     * Run OpenSCAP XCCDF Evaluation on a given list of servers
     * @param loggedInUser The current user
     * @param sids The list of server ids,
     * @param xccdfPath The path to xccdf document.
     * @param oscapParams The additional params for oscap tool.
     * @return ID of new SCAP action.
     *
     * @apidoc.doc Schedule OpenSCAP scan.
     * @apidoc.param #session_key()
     * @apidoc.param #array_single("int", "sids")
     * @apidoc.param #param_desc("string", "xccdfPath", "path to xccdf content on targeted systems.")
     * @apidoc.param #param_desc("string", "oscapParams", "additional parameters for oscap tool.")
     * @apidoc.returntype #param_desc("int", "id", "ID if SCAP action created")
     */
    public int scheduleXccdfScan(User loggedInUser, List sids,
            String xccdfPath, String oscapParams) {
        return scheduleXccdfScan(loggedInUser, sids, xccdfPath,
                oscapParams, null, new Date());
    }

    /**
     * Run OpenSCAP XCCDF Evaluation on a given list of servers
     * @param loggedInUser The current user
     * @param sids The list of server ids,
     * @param xccdfPath The path to xccdf document.
     * @param oscapParams The additional params for oscap tool.
     * @param date The date of earliest occurence.
     * @return ID of new SCAP action.
     *
     * @apidoc.doc Schedule OpenSCAP scan.
     * @apidoc.param #session_key()
     * @apidoc.param #array_single("int", "sids")
     * @apidoc.param #param_desc("string", "xccdfPath", "path to xccdf content on targeted systems.")
     * @apidoc.param #param_desc("string", "oscapParams", "additional parameters for oscap tool.")
     * @apidoc.param #param_desc("$date","date",
     *                       "The date to schedule the action")
     * @apidoc.returntype #param_desc("int", "id", "ID if SCAP action created")
     */
    public int scheduleXccdfScan(User loggedInUser, List sids,
            String xccdfPath, String oscapParams, Date date) {
        return scheduleXccdfScan(loggedInUser, sids, xccdfPath,
                oscapParams, null, date);
    }

    /**
     * Run OpenSCAP XCCDF Evaluation on a given list of servers
     * @param loggedInUser The current user
     * @param sids The list of server ids,
     * @param xccdfPath The path to xccdf document.
     * @param oscapParams The additional params for oscap tool.
     * @param ovalFiles Optional OVAL files for oscap tool.
     * @param date The date of earliest occurence.
     * @return ID of new SCAP action.
     *
     * @apidoc.doc Schedule OpenSCAP scan.
     * @apidoc.param #session_key()
     * @apidoc.param #array_single("int", "sids")
     * @apidoc.param #param_desc("string", "xccdfPath", "Path to xccdf content on targeted systems.")
     * @apidoc.param #param_desc("string", "oscapPrams", "Additional parameters for oscap tool.")
     * @apidoc.param #param_desc("string", "ovalFiles", "Additional OVAL files for oscap tool.")
     * @apidoc.param #param_desc("$date","date",
     *                       "The date to schedule the action")
     * @apidoc.returntype #param_desc("int", "id", "ID if SCAP action created")
     */
    public int scheduleXccdfScan(User loggedInUser, List sids,
             String xccdfPath, String oscapParams, String ovalFiles, Date date) {
        if (sids.isEmpty()) {
            throw new InvalidSystemException();
        }

        HashSet<Long> longServerIds = new HashSet<>();
        for (Object serverIdIn : sids) {
            longServerIds.add(Long.valueOf((Integer) serverIdIn));
        }

        try {
            ScapAction action = ActionManager.scheduleXccdfEval(loggedInUser,
                    longServerIds, xccdfPath, oscapParams, ovalFiles, date);
            return action.getId().intValue();
        }
        catch (MissingEntitlementException e) {
           throw new com.redhat.rhn.frontend.xmlrpc.MissingEntitlementException(
                   e.getMessage());
        }
        catch (MissingCapabilityException e) {
           throw new com.redhat.rhn.frontend.xmlrpc.MissingCapabilityException(
                   e.getCapability(), e.getServer());
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
    }

    /**
     * Run Open Scap XCCDF Evaluation on a given server
     * @param loggedInUser The current user
     * @param sid The server id.
     * @param xccdfPath The path to xccdf path.
     * @param oscapParams The additional params for oscap tool.
     * @return ID of the new scap action.
     *
     * @apidoc.doc Schedule Scap XCCDF scan.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param_desc("string", "xccdfPath", "Path to xccdf content on targeted systems.")
     * @apidoc.param #param_desc("string", "oscapPrams", "Additional parameters for oscap tool.")
     * @apidoc.returntype #param_desc("int", "id", "ID of the scap action created")
     */
    public int scheduleXccdfScan(User loggedInUser, Integer sid,
        String xccdfPath, String oscapParams) {
        return scheduleXccdfScan(loggedInUser, sid, xccdfPath, oscapParams, new Date());
    }

    /**
     * Run Open Scap XCCDF Evaluation on a given server at a given time.
     * @param loggedInUser The current user
     * @param sid The server id.
     * @param xccdfPath The path to xccdf path.
     * @param oscapParams The additional params for oscap tool.
     * @param date The date of earliest occurence
     * @return ID of the new scap action.
     *
     * @apidoc.doc Schedule Scap XCCDF scan.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param_desc("string", "xccdfPath", "Path to xccdf content on targeted systems.")
     * @apidoc.param #param_desc("string", "oscapPrams", "Additional parameters for oscap tool.")
     * @apidoc.param #param_desc("$date","date",
     *                       "The date to schedule the action")
     * @apidoc.returntype #param_desc("int", "id", "ID of the scap action created")
     */
    public int scheduleXccdfScan(User loggedInUser, Integer sid,
            String xccdfPath, String oscapParams, Date date) {
        List serverIds = new ArrayList();
        serverIds.add(sid);
        return scheduleXccdfScan(loggedInUser, serverIds, xccdfPath, oscapParams, null, date);
    }
}
