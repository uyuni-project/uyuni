/**
 * Copyright (c) 2014 SUSE
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SUSE trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate SUSE trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.frontend.xmlrpc.sync.content;

import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.manager.content.ContentSyncException;
import com.redhat.rhn.manager.content.ContentSyncManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @xmlrpc.namespace sync.content
 * @xmlrpc.doc Provides the namespace for the Content synchronization methods.
 */
public class ContentSyncHandler extends BaseHandler {
    /**
     * Add product by name.
     *
     * @param sessionKey Session token.
     * @param name Name of the product.
     * @return State of the action result.
     *
     * @xmlrpc.doc Add product by name.
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.param #param_desc("string", "name", "Name of the product")
     * @xmlrpc.returntype #return_int_success()
     */
    public Integer addProduct(String sessionKey, String name) {
        BaseHandler.getLoggedInUser(sessionKey);
        System.err.println("Added product: " + name);

        return BaseHandler.VALID;
    }


    /**
     * Add channel by label.
     *
     * @param sessionKey Session token.
     * @param label Name of the product.
     * @return State of the action result.
     *
     * @xmlrpc.doc Add product by name.
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.param #param_desc("string", "label", "Label of the channel")
     * @xmlrpc.returntype #return_int_success()
     */
    public Integer addChannel(String sessionKey, String label) {
        BaseHandler.getLoggedInUser(sessionKey);
        System.err.println("Added channel: " + label);

        return BaseHandler.VALID;
    }


    /**
     * List all products that are accessible to the organization.
     *
     * @param sessionKey Session token.
     * @return List of products with their extensions (add-ons).
     *
     * @xmlrpc.doc List all products that are accessible to the organization.
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.returntype #array()
     *                       $SCCProductSerializer
     *                    #array_end()
     */
    public Object[] listProducts(String sessionKey) {
        BaseHandler.getLoggedInUser(sessionKey);
        return new ContentSyncManager().getProducts().toArray();
    }


    /**
     * List all channels that are accessible to the organization.
     *
     * @param sessionKey Session token.
     * @return List of channels.
     *
     * @xmlrpc.doc List all channels that are accessible to the organization.
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.returntype #array()
     *                       $MgrSyncChannelSerializer
     *                    #array_end()
     */
    public Object[] listChannels(String sessionKey) throws ContentSyncException {
        BaseHandler.getLoggedInUser(sessionKey);
        return new ContentSyncManager().listChannels().toArray();
    }


    /**
     * List channel extensions (add-ons).
     *
     * @param sessionKey Session token.
     * @param label The label of the channel.
     * @return List of channels.
     *
     * @xmlrpc.doc List all channels that are accessible to the organization.
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.param #param_desc("string", "label", "Channel label")
     * @xmlrpc.returntype #array()
     *                      #struct("addon")
     *                        #prop_desc("string", "name", "Name of the product")
     *                        #prop_desc("string", "label", "Label of the product (identifier)")
     *                        #prop_desc("string", "version", "Version")
     *                        #prop_desc("string", "release", "Release type")
     *                        #prop_desc("string", "arch", "Architecture")
     *                        #prop_desc("string", "title", "Title (friendly name)")
     *                        #prop_desc("string", "description", "Description")
     *                      #struct_end()
     *                    #array_end()
     */
    public List<Map<String, Object>> listChannelExtensions(String sessionKey, String label) {
        BaseHandler.getLoggedInUser(sessionKey);
        List<Map<String, Object>> extensions = new ArrayList<Map<String, Object>>();

        // Some bogus data that looks like real.
        for (int i = 0; i < 10; i++) {
            Map<String, Object> ext = new HashMap<String, Object>();
            ext.put("name", "SUSE Linux Enterprise Server");
            ext.put("label", "sle-sdk");
            ext.put("version", "12");
            ext.put("release", "GA");
            ext.put("arch", "ppc64le");
            ext.put("title", "SUSE Linux Enterprise Software Development Cat 12 ppc64le");
            ext.put("description", "Something to entertain you for long.");
            ext.put("status", "available");
            extensions.add(ext);
        }

        return extensions;
    }


    /**
     * Synchronize channels between the Customer Center and the SUSE Manager database.
     * This method is one step of the whole refresh cycle.
     *
     * @param sessionKey User session token.
     * @throws ContentSyncException
     *
     * @xmlrpc.doc Synchronize channels between the Customer Center
     *             and the SUSE Manager database.
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.returntype #return_int_success()
     */
    public Integer synchronizeChannels(String sessionKey) throws ContentSyncException {
        BaseHandler.getLoggedInUser(sessionKey);
        new ContentSyncManager().updateChannels();

        return BaseHandler.VALID;
    }


    /**
     * Synchronize channel families between the Customer Center
     * and the SUSE Manager database.
     * This method is one step of the whole refresh cycle.
     *
     * @param sessionKey User session token.
     * @throws ContentSyncException
     *
     * @xmlrpc.doc Synchronize channel families between the Customer Center
     *             and the SUSE Manager database.
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.returntype #return_int_success()
     */
    public Integer synchronizeChannelFamilies(String sessionKey)
            throws ContentSyncException {
        BaseHandler.getLoggedInUser(sessionKey);
        ContentSyncManager csm = new ContentSyncManager();
        csm.updateChannelFamilies(csm.readChannelFamilies());

        return BaseHandler.VALID;
    }


    /**
     * Synchronize SUSE products between the Customer Center and the SUSE Manager database.
     * This method is one step of the whole refresh cycle.
     *
     * @param sessionKey User session token.
     * @throws ContentSyncException
     *
     * @xmlrpc.doc Synchronize SUSE products between the Customer Center
     *             and the SUSE Manager database.
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.returntype #return_int_success()
     */
    public Integer synchronizeProducts(String sessionKey) throws ContentSyncException {
        BaseHandler.getLoggedInUser(sessionKey);
        ContentSyncManager csm = new ContentSyncManager();
        csm.updateSUSEProducts(csm.getProducts());

        return BaseHandler.VALID;
    }


    /**
     * Synchronize SUSE product channels between the Customer Center
     * and the SUSE Manager database.
     * This method is one step of the whole refresh cycle.
     *
     * @param sessionKey User session token.
     * @throws ContentSyncException
     *
     * @xmlrpc.doc Synchronize SUSE product channels between the Customer Center
     *             and the SUSE Manager database.
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.returntype #return_int_success()
     */
    public Integer synchronizeProductChannels(String sessionKey)
            throws ContentSyncException {
        BaseHandler.getLoggedInUser(sessionKey);
        ContentSyncManager csm = new ContentSyncManager();
        csm.updateSUSEProductChannels(csm.getAvailableChannels(csm.readChannels()));

        return BaseHandler.VALID;
    }


    /**
     * Synchronize upgrade paths between the Customer Center
     * and the SUSE Manager database.
     * This method is one step of the whole refresh cycle.
     *
     * @param sessionKey User session token.
     * @throws ContentSyncException
     *
     * @xmlrpc.doc Synchronize upgrade paths between the Customer Center
     *             and the SUSE Manager database.
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.returntype #return_int_success()
     */
    public Integer synchronizeUpgradePaths(String sessionKey) throws ContentSyncException {
        BaseHandler.getLoggedInUser(sessionKey);
        new ContentSyncManager().updateUpgradePaths();

        return BaseHandler.VALID;
    }


    /**
     * Synchronize subscriptions between the Customer Center
     * and the SUSE Manager database.
     * This method is one step of the whole refresh cycle.
     *
     * @param sessionKey User session token.
     * @throws ContentSyncException
     *
     * @xmlrpc.doc Synchronize subscriptions between the Customer Center
     *             and the SUSE Manager database.
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.returntype #return_int_success()
     */
    public Integer synchronizeSubscriptions(String sessionKey) throws ContentSyncException {
        BaseHandler.getLoggedInUser(sessionKey);
        ContentSyncManager csm = new ContentSyncManager();
        csm.updateSubscriptions(csm.getSubscriptions());

        return BaseHandler.VALID;
    }
}
