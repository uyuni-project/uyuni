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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.frontend.xmlrpc.sync.content;

import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
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
     *                      #struct("entry")
     *                        #prop_desc("string", "name", "Name of the product")
     *                        #prop_desc("string", "label", "Label of the product (identifier)")
     *                        #prop_desc("string", "version", "Version")
     *                        #prop_desc("string", "release", "Release type")
     *                        #prop_desc("string", "arch", "Architecture")
     *                        #prop_desc("string", "title", "Title (friendly name)")
     *                        #prop_desc("string", "description", "Description")
     *                        #prop_desc("string", "status", "Available, unavailable or installed")
     *                      #struct_end()
     *                      #array()
     *                        #struct("extensions")
     *                          #prop_desc("string", "name", "Extension name")
     *                          #prop_desc("string", "label", "Extension label")
     *                          #prop_desc("string", "version", "Version")
     *                          #prop_desc("string", "release", "Type of the release.")
     *                          #prop_desc("string", "arch", "Architecture")
     *                          #prop_desc("string", "title", "Title (friendly name)")
     *                          #prop_desc("string", "description", "Description")
     *                          #prop_desc("string", "status", "Available, unavailable or installed")
     *                        #struct_end()
     *                      #array_end()
     *                    #array_end()
     */
    public List<Map<String, Object>> listProducts(String sessionKey) {
        List<Map<String, Object>> products = new ArrayList<Map<String, Object>>();

        // Some bogus data that looks like real.
        Map<String, Object> product = new HashMap<String, Object>();
        product.put("name", "SUSE Linux Enterprise Server");
        product.put("label", "SUSE_SLES");
        product.put("version", "12");
        product.put("release", "GA");
        product.put("arch", "x86_64");
        product.put("title", "SUSE Linux Enterprise Server 11 x86_64");
        product.put("description", "An ultimate service pack for your Windows OS!");
        product.put("status", "available");

        List<Map<String, Object>> extensions = new ArrayList<Map<String, Object>>();
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

        product.put("extensions", extensions);
        products.add(product);

        return products;
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
     *                      #struct("entry")
     *                        #prop_desc("string", "name", "The name of the channel")
     *                        #prop_desc("string", "target", "Distribution target")
     *                        #prop_desc("string", "description", "Description of the channel")
     *                        #prop_desc("string", "url", "URL of the repository")
     *                      #struct_end()
     *                    #array_end()
     */
    public List<Map<String, Object>> listChannels(String sessionKey) {
        List<Map<String, Object>> channels = new ArrayList<Map<String, Object>>();

        // Some bogus data that looks like real.
        Map<String, Object> channel = new HashMap<String, Object>();
        channel.put("name", "SLE10-SDK-SP4-Online");
        channel.put("target", "sles-10-i586");
        channel.put("description", "SLE10-SDK-SP4-Online for sles-10-i586");
        channel.put("url", "https://nu.novell.com/repo/$RCE/SLE10-SDK-SP4-Online/sles-10-i586");
        channels.add(channel);

        return channels;
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
        List<Map<String, Object>> extensions = new ArrayList<Map<String, Object>>();

        // Some bogus data that looks like real.
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

        return extensions;
    }
}
