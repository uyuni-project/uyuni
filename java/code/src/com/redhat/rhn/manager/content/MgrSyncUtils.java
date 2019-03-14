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

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.util.http.HttpClientAdapter;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ChannelProduct;
import com.redhat.rhn.domain.channel.ProductName;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.rhnpackage.PackageArch;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpHead;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * Utility methods to be used in {@link ContentSyncManager} related code.
 */
public class MgrSyncUtils {
    // Logger instance
    private static Logger log = Logger.getLogger(MgrSyncUtils.class);

    // Source URL handling
    private static final String OFFICIAL_NOVELL_UPDATE_HOST = "nu.novell.com";
    private static final List<String> OFFICIAL_UPDATE_HOSTS =
            Arrays.asList("updates.suse.com", OFFICIAL_NOVELL_UPDATE_HOST);
    private static final List<String> PRODUCT_ARCHS = Arrays.asList("i586", "ia64", "ppc64le", "ppc64", "ppc",
            "s390x", "s390", "x86_64", "aarch64", "amd64");

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
     * @param packageArch we want to get the arch from
     * @param channelLabel alternative try to find the arch in the channelLabel
     * @return channel arch object
     */
    public static ChannelArch getChannelArch(PackageArch packageArch, String channelLabel) {
        String arch = "x86_64";
        if (packageArch != null) {
            arch = packageArch.getLabel();
        }
        else {
            arch = PRODUCT_ARCHS.stream().filter(a -> channelLabel.contains(a)).findFirst().orElse(arch);
        }
        if (arch.equals("i686") || arch.equals("i586") ||
                arch.equals("i486") || arch.equals("i386")) {
            arch = "ia32";
        }
        else if (arch.equals("ppc64")) {
            arch = "ppc";
        }
        else if (arch.equals("amd64")) {
            arch = "amd64-deb";
        }
        return ChannelFactory.findArchByLabel("channel-" + arch);
    }

    /**
     * Get the channel for a given channel label.
     * If label is null it returns null. If the channel label is not found
     * it throws an exception.
     *
     * @param label the label
     * @return the channel
     * @throws ContentSyncException if the parent channel is not installed
     */
    public static Channel getChannel(String label) throws ContentSyncException {
        Channel channel = null;
        if (label != null) {
            channel = ChannelFactory.lookupByLabel(label);
            if (channel == null) {
                throw new ContentSyncException("The parent channel is not installed: " + label);
            }
        }
        return channel;
    }

    /**
     * Find a {@link ChannelProduct} or create it if necessary and return it.
     * @param product product to find or create
     * @return channel product
     */
    public static ChannelProduct findOrCreateChannelProduct(SUSEProduct product) {
        ChannelProduct p = ChannelFactory.findChannelProduct(
                product.getName(), product.getVersion());
        if (p == null) {
            p = new ChannelProduct();
            p.setProduct(product.getName());
            p.setVersion(product.getVersion());
            p.setBeta(false);
            ChannelFactory.save(p);
        }
        return p;
    }

    /**
     * Find a {@link ProductName} or create it if necessary and return it.
     * @param name channel
     * @return product name
     */
    public static ProductName findOrCreateProductName(String name) {
        ProductName productName = ChannelFactory.lookupProductNameByLabel(
                name);
        if (productName == null) {
            productName = new ProductName();
            productName.setLabel(name);
            productName.setName(name);
            ChannelFactory.save(productName);
        }
        return productName;
    }

    /**
     * Convert network URL to file system URL.
     * @param urlString url
     * @param name repo name
     * @return file URI
     */
    public static URI urlToFSPath(String urlString, String name) {
        String host = "";
        String path = "/";
        try {
            URL url = new URL(urlString);
            host = url.getHost();
            path = url.getPath();
        }
        catch (MalformedURLException e) {
            log.warn("Unable to parse URL: " + urlString);
        }
        String sccDataPath = Config.get().getString(ContentSyncManager.RESOURCE_PATH, null);
        File dataPath = new File(sccDataPath);

        if (!OFFICIAL_UPDATE_HOSTS.contains(host) && name != null) {
            // everything after the first space are suffixes added to make things unique
            String[] parts  = name.split("\\s");
            return new File(dataPath.getAbsolutePath() + "/repo/RPMMD/" + parts[0]).toURI();
        }

        return new File(dataPath.getAbsolutePath() + path).toURI();
    }
}
