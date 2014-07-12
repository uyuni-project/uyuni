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

import com.redhat.rhn.common.client.ClientCertificate;
import com.redhat.rhn.common.client.ClientCertificateDigester;
import com.redhat.rhn.common.client.InvalidCertificateException;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelFamilyFactory;
import com.redhat.rhn.domain.entitlement.Entitlement;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.satellite.CertificateFactory;
import com.redhat.rhn.domain.server.EntitlementServerGroup;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ChannelOverview;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.MethodInvalidParamException;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;
import com.redhat.rhn.frontend.xmlrpc.system.XmlRpcSystemHelper;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.SystemManager;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * SatelliteHandler
 * @version $Rev$
 *
 * @xmlrpc.namespace satellite
 * @xmlrpc.doc Provides methods to obtain details on the Satellite.
 */
public class SatelliteHandler extends BaseHandler {
    private static Logger log = Logger.getLogger(SatelliteHandler.class);

    /**
     * List all proxies on the Satellite for the current org
     * @param loggedInUser The current user
     * @return  list of Maps containing "id", "name", and "last_checkin"
     *
     * @xmlrpc.doc List the proxies within the user's organization.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.returntype
     * #array()
     *   $SystemOverviewSerializer
     * #array_end()
     */
    public Object[] listProxies(User loggedInUser) {
        List <Server> proxies = ServerFactory.lookupProxiesByOrg(loggedInUser);
        List toReturn = new ArrayList();
        XmlRpcSystemHelper helper = XmlRpcSystemHelper.getInstance();
        for (Server server : proxies) {
            toReturn.add(helper.format(server));
        }
        return toReturn.toArray();
    }


    /**
     * Lists all the channel and system entitlements for the org associated
     * with the user executing the request.
     * @param loggedInUser The current user
     * @return A map containing two items.  "system" which is an array of
     *          EntitlementServerGroup objects, and 'channel' which is an
     *          array of "ChannelOverview" objects
     *
     * @xmlrpc.doc Lists all channel and system entitlements for the organization
     * associated with the user executing the request.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.returntype
     * #struct("channel/system entitlements")
     *   #prop_array_begin("system")
     *     $EntitlementServerGroupSerializer
     *   #prop_array_end()
     *   #prop_array_begin("channel")
     *     $ChannelOverviewSerializer
     *   #prop_array_end()
     * #struct_end()
     */
    public Map listEntitlements(User loggedInUser) {

        List<EntitlementServerGroup> systemEnts = new
            LinkedList<EntitlementServerGroup>();

        for (Entitlement ent : EntitlementManager.getBaseEntitlements()) {
            EntitlementServerGroup group = ServerGroupFactory.lookupEntitled(
                    ent, loggedInUser.getOrg());
            if (group != null) {
                systemEnts.add(group);
            }
        }

        for (Entitlement ent : EntitlementManager.getAddonEntitlements()) {
            EntitlementServerGroup group = ServerGroupFactory.lookupEntitled(
                    ent, loggedInUser.getOrg());
            if (group != null) {
                systemEnts.add(group);
            }
        }

        List<ChannelOverview> channels = ChannelManager.entitlements(
                loggedInUser.getOrg().getId(), null);

        /* Once bz 445260 is resolved, the loop below may be removed and the channels
         * list may be used 'as is'.
         */
        if (channels != null) {
            for (ChannelOverview item : channels) {
                ChannelFamily fam = ChannelFamilyFactory.lookupById(item.getId());
                if (fam.getOrg() != null) {
                    item.setOrgId(fam.getOrg().getId());
                }
            }
        }

        Map toReturn = new HashMap();
        toReturn.put("system", systemEnts.toArray());
        toReturn.put("channel", channels.toArray());

        return toReturn;
    }

    /**
     * Get the Satellite certificate expiration date
     * @param loggedInUser The current user
     * @return A Date object of the expiration of the certificate
     *
     * @xmlrpc.doc Retrieves the certificate expiration date of the activated
     *      certificate.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.returntype
     *    $date
     */
    public Date getCertificateExpirationDate(User loggedInUser) {
        if (!loggedInUser.hasRole(RoleFactory.SAT_ADMIN)) {
            throw new PermissionCheckFailureException(RoleFactory.SAT_ADMIN);
        }

        return CertificateFactory.lookupNewestCertificate().getExpires();
    }

    /**
     * Indicates if monitoring is enabled on the satellite
     * available since API version 10.13
     * @param loggedInUser The current user
     * @return True if monitoring is enabled
     *
     * @xmlrpc.doc Indicates if monitoring is enabled on the satellite
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.returntype #param("boolean", "True if monitoring is enabled")
     */
    public boolean isMonitoringEnabled(User loggedInUser) {
        return ConfigDefaults.get().isMonitoringBackend();
    }

    /**
     * Indicates if monitoring is enabled on the satellite
     * available since API version 10.14
     * @param clientcert client certificate of the system.
     * @return True if monitoring is enabled
     * @throws MethodInvalidParamException thrown if certificate is invalid.
     *
     * @xmlrpc.doc Indicates if monitoring is enabled on the satellite
     * @xmlrpc.param #param_desc("string", "systemid", "systemid file")
     * @xmlrpc.returntype #param("boolean", "True if monitoring is enabled")
     */

    public boolean isMonitoringEnabledBySystemId(String clientcert)
        throws MethodInvalidParamException {

        StringReader rdr = new StringReader(clientcert);
        Server server = null;

        ClientCertificate cert;
        try {
            cert = ClientCertificateDigester.buildCertificate(rdr);
            server = SystemManager.lookupByCert(cert);
        }
        catch (IOException ioe) {
            log.error("IOException - Trying to access a system with an " +
                    "invalid certificate", ioe);
            throw new MethodInvalidParamException();
        }
        catch (SAXException se) {
            log.error("SAXException - Trying to access a " +
                    "system with an invalid certificate", se);
            throw new MethodInvalidParamException();
        }
        catch (InvalidCertificateException e) {
            log.error("InvalidCertificateException - Trying to access a " +
                    "system with an invalid certificate", e);
            throw new MethodInvalidParamException();
        }
        return ConfigDefaults.get().isMonitoringBackend();
    }
}
