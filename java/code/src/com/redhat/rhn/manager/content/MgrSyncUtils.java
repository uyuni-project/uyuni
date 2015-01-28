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
package com.redhat.rhn.manager.content;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.util.HttpUtils;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ChannelProduct;
import com.redhat.rhn.domain.channel.ProductName;

import com.suse.mgrsync.MgrSyncChannel;
import com.suse.scc.client.SCCProxySettings;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Utility methods to be used in {@link ContentSyncManager} related code.
 */
public class MgrSyncUtils {

    // Logger instance
    private static final Logger log = Logger.getLogger(MgrSyncUtils.class);

    // This file is touched once the server has been migrated to SCC
    public static final String SCC_MIGRATED = "/var/lib/spacewalk/scc/migrated";
    public static final String SCC_DEFAULT = "/var/lib/spacewalk/scc/default_scc";

    /**
     * Send a HEAD request to a given URL to verify accessibility with given credentials.
     *
     * @param url the URL to verify
     * @param username username for authentication
     * @param password password for authentication
     * @return the response code of the request
     */
    public static int sendHeadRequest(String url, String username, String password)
            throws ContentSyncException {
        if (log.isDebugEnabled()) {
            log.debug("Sending HEAD request to: " + url);
        }
        int responseCode = -1;
        HttpClient httpClient = HttpUtils.initHttpClient(username, password);
        HeadMethod headMethod = new HeadMethod(url);

        try {
            responseCode = httpClient.executeMethod(headMethod);
        }
        catch (IOException e) {
            throw new ContentSyncException(e);
        }
        finally {
            headMethod.releaseConnection();
        }
        return responseCode;
    }

    /**
     * Return the current proxy settings as {@link SCCProxySettings}.
     *
     * @return the proxy settings configured in /etc/rhn/rhn.conf
     */
    public static SCCProxySettings getRhnProxySettings() {
        ConfigDefaults configDefaults = ConfigDefaults.get();
        SCCProxySettings settings = new SCCProxySettings(
                configDefaults.getProxyHost(), configDefaults.getProxyPort());
        settings.setUsername(configDefaults.getProxyUsername());
        settings.setPassword(configDefaults.getProxyPassword());
        return settings;
    }

    /**
     * Handle special cases where SUSE arch names differ from the RedHat ones.
     *
     * @param channel channel that we want to get the arch from
     * @return channel arch object
     */
    public static ChannelArch getChannelArch(MgrSyncChannel channel) {
        String arch = channel.getArch();
        if (arch.equals("i686") || arch.equals("i586")
                || arch.equals("i486") || arch.equals("i386")) {
            arch = "ia32";
        }
        else if (arch.equals("ppc64")) {
            arch = "ppc";
        }
        return ChannelFactory.findArchByLabel("channel-" + arch);
    }

    /**
     * Get the parent channel of a given {@link MgrSyncChannel} by looking it up
     * via the given label. Return null if the given channel is a base channel.
     *
     * @param channel to look up its parent
     * @return the parent channel
     * @throws ContentSyncException if the parent channel is not installed
     */
    public static Channel getParentChannel(MgrSyncChannel channel)
            throws ContentSyncException {
        String parent = channel.getParent();
        if (ContentSyncManager.BASE_CHANNEL.equals(parent)) {
            return null;
        }
        else {
            Channel parentChannel = ChannelFactory.lookupByLabel(parent);
            if (parentChannel == null) {
                throw new ContentSyncException("The parent channel of " +
                        channel.getLabel() + " is not currently installed.");
            }
            return parentChannel;
        }
    }

    /**
     * Find a {@link ChannelProduct} or create it if necessary and return it.
     * @param channel
     * @return channel product
     */
    public static ChannelProduct findOrCreateChannelProduct(MgrSyncChannel channel) {
        ChannelProduct product = ChannelFactory.findChannelProduct(
                channel.getProductName(), channel.getProductVersion());
        if (product == null) {
            product = new ChannelProduct();
            product.setProduct(channel.getProductName());
            product.setVersion(channel.getProductVersion());
            product.setBeta("N");
            ChannelFactory.save(product);
        }
        return product;
    }

    /**
     * Find a {@link ProductName} or create it if necessary and return it.
     * @param channel
     * @return product name
     */
    public static ProductName findOrCreateProductName(MgrSyncChannel channel) {
        ProductName productName = ChannelFactory.lookupProductNameByLabel(
                channel.getProductName());
        if (productName == null) {
            productName = new ProductName();
            productName.setLabel(channel.getProductName());
            productName.setName(channel.getProductName());
            ChannelFactory.save(productName);
        }
        return productName;
    }

    /**
     * Check if this server has been migrated to an SCC backend yet.
     * @return true if provider is migrated from NCC to SCC.
     */
    public static boolean isMigratedToSCC() {
        return new File(MgrSyncUtils.SCC_MIGRATED).exists();
    }

    /**
     * @return true if SCC is the default backend to be used, no matter
     * if the customer has migrated yet or not.
     */
    public static boolean isSCCTheDefault() {
        return new File(MgrSyncUtils.SCC_DEFAULT).exists();
    }
}
