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
package com.redhat.rhn.frontend.xmlrpc.satellite;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.proxy.ProxyHandler;

import org.apache.log4j.Logger;

/**
 * SatelliteHandler
 *
 * @xmlrpc.namespace satellite
 * @xmlrpc.doc Provides methods to obtain details on the Satellite.
 * @deprecated deprecated in favour of proxy and admin.monitoring namespaces.
 */
@Deprecated
public class SatelliteHandler extends BaseHandler {
    private static Logger log = Logger.getLogger(SatelliteHandler.class);

    private ProxyHandler proxyHandler;

    /**
     * @param proxyHandlerIn proxy hander to delegate
     */
    public SatelliteHandler(ProxyHandler proxyHandlerIn) {
        proxyHandler = proxyHandlerIn;
    }

    /**
     * List all proxies on the Satellite for the current org
     * @param loggedInUser The current user
     * @return  list of Maps containing "id", "name", and "last_checkin"
     *
     * @deprecated moved to proxy.listProxies
     * @xmlrpc.doc List the proxies within the user's organization.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.returntype
     * #array_begin()
     *   $SystemOverviewSerializer
     * #array_end()
     */
    @Deprecated
    public Object[] listProxies(User loggedInUser) {
        return proxyHandler.listProxies(loggedInUser);
    }

    /**
     * Indicates if monitoring is enabled on the satellite
     * available since API version 10.13
     * @param loggedInUser The current user
     * @return True if monitoring is enabled
     *
     * @deprecated deprecated unused method. See new namespace admin.monitoring.
     * @xmlrpc.doc Indicates if monitoring is enabled on the satellite
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.returntype #param("boolean", "True if monitoring is enabled")
     */
    @Deprecated
    public boolean isMonitoringEnabled(User loggedInUser) {
        return false;
    }

    /**
     * Use system.getEntitlements() and check the monitoring entitlement.
     *
     * Indicates if monitoring is enabled on the satellite
     * available since API version 10.14
     * @param clientcert client certificate of the system.
     * @return True if monitoring is enabled
     *
     * @deprecated deprecated unused method. See new namespace admin.monitoring.
     * @xmlrpc.doc Indicates if monitoring is enabled on the satellite
     * @xmlrpc.param #param_desc("string", "systemid", "systemid file")
     * @xmlrpc.returntype #param("boolean", "True if monitoring is enabled")
     */
    @Deprecated
    public boolean isMonitoringEnabledBySystemId(String clientcert) {
        return false;
    }

}
