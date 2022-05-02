/*
 * Copyright (c) 2009--2011 Red Hat, Inc.
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
package com.redhat.rhn.frontend.xmlrpc.system.search;

import com.redhat.rhn.FaultException;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.frontend.action.systems.SystemSearchHelper;
import com.redhat.rhn.frontend.dto.SystemSearchResult;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.SearchServerCommException;
import com.redhat.rhn.frontend.xmlrpc.SearchServerQueryException;

import com.suse.manager.api.ReadOnly;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.List;

import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcFault;

/**
 * SystemSearchHandler
 * Provides access to the internal XMLRPC search-server for system searches
 * @xmlrpc.namespace system.search
 * @xmlrpc.doc Provides methods to perform system search requests using the search server.
 *
 */
public class SystemSearchHandler extends BaseHandler {
    private static Logger log = LogManager.getLogger(SystemSearchHandler.class);

    private List<SystemSearchResult> performSearch(String sessionKey, String searchString,
            String viewMode) throws FaultException {
        Boolean invertResults = false;
        String whereToSearch = ""; // if this is "system_list" it will search SSM only

        DataResult<SystemSearchResult> dr;
        try {
            dr = SystemSearchHelper.systemSearch(sessionKey,
                    searchString,
                    viewMode,
                    invertResults,
                    whereToSearch, true);
        }
        catch (MalformedURLException | XmlRpcException e) {
            log.info("Caught Exception :{}", e);
            e.printStackTrace();
            throw new SearchServerCommException();
            // Connection error to XMLRPC search server
        }
        catch (XmlRpcFault e) {
            log.info("Caught Exception :{}", e);
            log.info("ErrorCode = {}", e.getErrorCode());
            e.printStackTrace();
            if (e.getErrorCode() == 100) {
                log.error("Invalid search query", e);
            }
            throw new SearchServerQueryException();
            // Could not parse query
        }
        // Connection error

        if (dr != null) {
            dr.elaborate(Collections.emptyMap());
            return dr;
        }
        return Collections.emptyList();
    }

    /**
     * List the systems which match this ip.
     * @param sessionKey the session of the user
     * @param searchTerm the search term to match
     * @return list of systems
     * @throws FaultException A FaultException is thrown on error.
     *
     * @xmlrpc.doc List the systems which match this ip.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "searchTerm")
     * @xmlrpc.returntype
     *     #return_array_begin()
     *         $SystemSearchResultSerializer
     *     #array_end()
     */
    @ReadOnly
    public List<SystemSearchResult> ip(String sessionKey, String searchTerm) throws FaultException {
        return performSearch(sessionKey, searchTerm, SystemSearchHelper.IP);
    }

    /**
     * List the systems which match this hostname
     * @param sessionKey the session of the user
     * @param searchTerm the search term to match
     * @return list of systems
     * @throws FaultException A FaultException is thrown on error.
     *
     * @xmlrpc.doc List the systems which match this hostname
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "searchTerm")
     * @xmlrpc.returntype
     *     #return_array_begin()
     *         $SystemSearchResultSerializer
     *     #array_end()
     */
    @ReadOnly
    public List<SystemSearchResult> hostname(String sessionKey, String searchTerm) throws FaultException {
        return performSearch(sessionKey, searchTerm, SystemSearchHelper.HOSTNAME);
    }

    /**
     * List the systems which match this device vendor id
     * @param sessionKey the session of the user
     * @param searchTerm the search term to match
     * @return list of systems
     * @throws FaultException A FaultException is thrown on error.
     *
     * @xmlrpc.doc List the systems which match this device vendor_id
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "searchTerm")
     * @xmlrpc.returntype
     *     #return_array_begin()
     *         $SystemSearchResultSerializer
     *     #array_end()
     */
    @ReadOnly
    public List<SystemSearchResult> deviceVendorId(String sessionKey, String searchTerm) throws FaultException {
        return performSearch(sessionKey, searchTerm, SystemSearchHelper.HW_VENDOR_ID);
    }

    /**
     * List the systems which match this device id
     * @param sessionKey the session of the user
     * @param searchTerm the search term to match
     * @return list of systems
     * @throws FaultException A FaultException is thrown on error.
     *
     * @xmlrpc.doc List the systems which match this device id
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "searchTerm")
     * @xmlrpc.returntype
     *     #return_array_begin()
     *         $SystemSearchResultSerializer
     *     #array_end()
     */
    @ReadOnly
    public List<SystemSearchResult> deviceId(String sessionKey, String searchTerm) throws FaultException {
        return performSearch(sessionKey, searchTerm, SystemSearchHelper.HW_DEVICE_ID);
    }

    /**
     * List the systems which match this device driver
     * @param sessionKey the session of the user
     * @param searchTerm the search term to match
     * @return list of systems
     * @throws FaultException A FaultException is thrown on error.
     *
     * @xmlrpc.doc List the systems which match this device driver.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "searchTerm")
     * @xmlrpc.returntype
     *     #return_array_begin()
     *         $SystemSearchResultSerializer
     *     #array_end()
     */
    @ReadOnly
    public List<SystemSearchResult> deviceDriver(String sessionKey, String searchTerm) throws FaultException {
        return performSearch(sessionKey, searchTerm, SystemSearchHelper.HW_DRIVER);
    }

    /**
     * List the systems which match this device description
     * @param sessionKey the session of the user
     * @param searchTerm the search term to match
     * @return list of systems
     * @throws FaultException A FaultException is thrown on error.
     *
     * @xmlrpc.doc List the systems which match the device description.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "searchTerm")
     * @xmlrpc.returntype
     *     #return_array_begin()
     *         $SystemSearchResultSerializer
     *     #array_end()
     */
    @ReadOnly
    public List<SystemSearchResult> deviceDescription(String sessionKey, String searchTerm) throws FaultException {
        return performSearch(sessionKey, searchTerm, SystemSearchHelper.HW_DESCRIPTION);
    }

    /**
     * List the systems which match this name or description
     * @param sessionKey the session of the user
     * @param searchTerm the search term to match
     * @return list of systems
     * @throws FaultException A FaultException is thrown on error.
     *
     * @xmlrpc.doc List the systems which match this name or description
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "searchTerm")
     * @xmlrpc.returntype
     *     #return_array_begin()
     *         $SystemSearchResultSerializer
     *     #array_end()
     */
    @ReadOnly
    public List<SystemSearchResult> nameAndDescription(String sessionKey, String searchTerm) throws FaultException {
        return performSearch(sessionKey, searchTerm, SystemSearchHelper.NAME_AND_DESCRIPTION);
    }

    /**
     * List the systems which match this UUID
     * @param sessionKey the session of the user
     * @param searchTerm the search term to match
     * @return list of systems
     * @throws FaultException A FaultException is thrown on error.
     *
     * @xmlrpc.doc List the systems which match this UUID
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "searchTerm")
     * @xmlrpc.returntype
     *     #return_array_begin()
     *         $SystemSearchResultSerializer
     *     #array_end()
     */
    @ReadOnly
    public List<SystemSearchResult> uuid(String sessionKey, String searchTerm) throws FaultException {
        return performSearch(sessionKey, searchTerm, SystemSearchHelper.UUID);
    }
}
