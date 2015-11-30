/**
 * Copyright (c) 2014 SUSE LLC
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

import com.redhat.rhn.common.util.http.HttpClientAdapter;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ChannelProduct;
import com.redhat.rhn.domain.channel.ProductName;

import com.suse.mgrsync.XMLChannel;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpHead;

import java.io.IOException;

/**
 * Utility methods to be used in {@link ContentSyncManager} related code.
 */
public class MgrSyncUtils {

    // No instances should be created
    private MgrSyncUtils() {
    }

    /**
     * Send a HEAD request to a given URL to verify accessibility with given credentials.
     *
     * @param url the URL to verify
     * @param username username for authentication (pass null for unauthenticated requests)
     * @param password password for authentication (pass null for unauthenticated requests)
     * @return the response code of the request
     * @throws IOException in case of an error
     */
    public static HttpResponse sendHeadRequest(String url, String username, String password)
            throws IOException {
        return sendHeadRequest(url, username, password, false);
    }

    /**
     * Send a HEAD request to verify a proxy server (ignoring the "no_proxy" setting).
     *
     * @param url the URL to use for verification
     * @return true if return code of HTTP request is 200, otherwise false
     * @throws IOException in case of an error
     */
    public static boolean verifyProxy(String url) throws IOException {
        return sendHeadRequest(url, null, null, true).getStatusLine()
                .getStatusCode() == HttpStatus.SC_OK;
    }

    /**
     * Send a HEAD request to a given URL to verify accessibility with given credentials.
     *
     * @param url the URL to verify
     * @param username username for authentication (pass null for unauthenticated requests)
     * @param password password for authentication (pass null for unauthenticated requests)
     * @param ignoreNoProxy set true to ignore the "no_proxy" setting
     * @return the response code of the request
     * @throws IOException in case of an error
     */
    private static HttpResponse sendHeadRequest(String url, String username,
            String password, boolean ignoreNoProxy) throws IOException {
        HttpClientAdapter httpClient = new HttpClientAdapter();
        HttpHead headRequest = new HttpHead(url);
        try {
            return httpClient.executeRequest(
                    headRequest, username, password, ignoreNoProxy);
        }
        finally {
            headRequest.releaseConnection();
        }
    }

    /**
     * Handle special cases where SUSE arch names differ from the RedHat ones.
     *
     * @param channel channel that we want to get the arch from
     * @return channel arch object
     */
    public static ChannelArch getChannelArch(XMLChannel channel) {
        String arch = channel.getArch();
        if (arch.equals("i686") || arch.equals("i586") ||
                arch.equals("i486") || arch.equals("i386")) {
            arch = "ia32";
        }
        else if (arch.equals("ppc64")) {
            arch = "ppc";
        }
        return ChannelFactory.findArchByLabel("channel-" + arch);
    }

    /**
     * Get the parent channel of a given {@link XMLChannel} by looking it up
     * via the given label. Return null if the given channel is a base channel.
     *
     * @param channel to look up its parent
     * @return the parent channel
     * @throws ContentSyncException if the parent channel is not installed
     */
    public static Channel getParentChannel(XMLChannel channel)
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
     * @param channel channel
     * @return channel product
     */
    public static ChannelProduct findOrCreateChannelProduct(XMLChannel channel) {
        ChannelProduct product = ChannelFactory.findChannelProduct(
                channel.getProductName(), channel.getProductVersion());
        if (product == null) {
            product = new ChannelProduct();
            product.setProduct(channel.getProductName());
            product.setVersion(channel.getProductVersion());
            product.setBeta(false);
            ChannelFactory.save(product);
        }
        return product;
    }

    /**
     * Find a {@link ProductName} or create it if necessary and return it.
     * @param channel channel
     * @return product name
     */
    public static ProductName findOrCreateProductName(XMLChannel channel) {
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
