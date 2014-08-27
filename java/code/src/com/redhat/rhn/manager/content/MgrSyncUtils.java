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
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ChannelProduct;
import com.redhat.rhn.domain.channel.ProductName;

import com.suse.mgrsync.MgrSyncChannel;
import com.suse.scc.client.SCCProxySettings;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;

/**
 * Utility methods to be used in {@link ContentSyncManager} related code.
 */
public class MgrSyncUtils {

    // Logger instance
    private static final Logger log = Logger.getLogger(MgrSyncUtils.class);

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
        HttpURLConnection connection = null;
        int responseCode = -1;
        try {
            // Setup proxy support
            ConfigDefaults configDefaults = ConfigDefaults.get();
            String proxyHost = configDefaults.getProxyHost();
            if (!StringUtils.isBlank(proxyHost)) {
                int proxyPort = configDefaults.getProxyPort();
                Proxy proxy = new Proxy(Proxy.Type.HTTP,
                        new InetSocketAddress(proxyHost, proxyPort));
                connection = (HttpURLConnection) new URL(url).openConnection(proxy);
                if (log.isDebugEnabled()) {
                    log.debug("Sending HEAD request via proxy: " + proxyHost);
                }
                String proxyUsername = configDefaults.getProxyUsername();
                String proxyPassword = configDefaults.getProxyPassword();
                if (!StringUtils.isBlank(proxyUsername) &&
                        !StringUtils.isBlank(proxyPassword)) {
                    try {
                        String creds = proxyUsername + ':' + proxyPassword;
                        byte[] encoded = Base64.encodeBase64(creds.getBytes("iso-8859-1"));
                        final String proxyAuth = new String(encoded, "iso-8859-1");
                        connection.addRequestProperty("Proxy-Authorization", proxyAuth);
                    }
                    catch (UnsupportedEncodingException e) {
                        // Can't happen
                    }
                }
            }
            else {
                // No proxy is used
                if (log.isDebugEnabled()) {
                    log.debug("Sending HEAD request without proxy: " + proxyHost);
                }
                connection = (HttpURLConnection) new URL(url).openConnection();
            }

            // Configure the request
            connection.setRequestMethod("HEAD");

            // Basic authentication
            byte[] credsBytes = Base64.encodeBase64((username + ':' + password).getBytes());
            String credsString = new String(credsBytes);
            if (credsString != null) {
                connection.setRequestProperty("Authorization", "Basic " + credsString);
            }

            // Get the response code
            responseCode = connection.getResponseCode();
        }
        catch (MalformedURLException e) {
            throw new ContentSyncException(e);
        }
        catch (IOException e) {
            throw new ContentSyncException(e);
        }
        finally {
            if (connection != null) {
                connection.disconnect();
            }
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
}
