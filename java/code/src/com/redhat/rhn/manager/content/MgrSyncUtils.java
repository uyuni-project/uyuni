/*
 * Copyright (c) 2014--2021 SUSE LLC
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utility methods to be used in {@link ContentSyncManager} related code.
 */
public class MgrSyncUtils {
    // Logger instance
    private static Logger log = LogManager.getLogger(MgrSyncUtils.class);

    // Source URL handling
    private static final String OFFICIAL_NOVELL_UPDATE_HOST = "nu.novell.com";
    private static final List<String> OFFICIAL_UPDATE_HOSTS =
            Arrays.asList("updates.suse.com", OFFICIAL_NOVELL_UPDATE_HOST);
    private static final List<String> PRODUCT_ARCHS = Arrays.asList("i386", "i486", "i586", "i686", "ia64", "ppc64le",
            "ppc64", "ppc", "s390x", "s390", "x86_64", "aarch64", "amd64");

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
            arch = PRODUCT_ARCHS.stream().filter(channelLabel::contains).findFirst().orElse(arch);
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
        ProductName productName = ChannelFactory.lookupProductNameByLabel(name);
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
     *
     * 1. URL point to localhost, return the normal URL, we have access
     * 2. URL from updates.suse.com, return the path
     * 3. legacy SMT mirror URL /repo/RPMMD/&lt;repo name&gt; if it exists
     * 4. finally, return host + path as path component
     *
     * A mirrorlist URL with query paramater is converted to a path:
     * - key=value => key/value
     * - sort alphabetically
     * - join with a /
     * Example:
     * http://mirrorlist.centos.org/?release=8&arch=x86_64&repo=AppStream&infra=stock
     * file://mirrorlist.centos.org/arch/x86_64/infra/stock/release/8/repo/AppStream
     *
     * @param urlString url
     * @param name repo name
     * @return file URI
     */
    public static URI urlToFSPath(String urlString, String name) {
        String host = "";
        String path = File.separator;
        try {
            URI uri = new URI(urlString);
            host = uri.getHost();
            path = uri.getPath();

            // Case 1
            if ("localhost".equals(host)) {
                return uri;
            }
            String qPath = Arrays.stream(Optional.ofNullable(uri.getQuery()).orElse("").split("&"))
                    .filter(p -> p.contains("=")) // filter out possible auth tokens
                    .map(p ->
                        Arrays.stream(p.split("=", 2))
                            .collect(Collectors.joining(File.separator))
                    )
                    .sorted()
                    .collect(Collectors.joining(File.separator));
            if (!qPath.isBlank()) {
                path = Paths.get(path, qPath).toString();
            }
        }
        catch (URISyntaxException e) {
            log.warn("Unable to parse URL: {}", urlString);
        }
        String sccDataPath = Config.get().getString(ContentSyncManager.RESOURCE_PATH, null);
        if (sccDataPath == null) {
            throw new ContentSyncException("No local mirror path configured");
        }
        File dataPath = new File(sccDataPath);
        // Case 4
        File mirrorPath = new File(dataPath.getAbsolutePath(), host + File.separator + path);

        // Case 2
        if (OFFICIAL_UPDATE_HOSTS.contains(host)) {
            mirrorPath = new File(dataPath.getAbsolutePath(), path);
        }
        else if (name != null) {
            // Case 3
            // everything after the first space are suffixes added to make things unique
            String[] parts  = URLDecoder.decode(name, StandardCharsets.UTF_8).split("[\\s//]");
            if (!(parts[0].isBlank() || parts[0].equals(".."))) {
                File oldMirrorPath = Paths.get(dataPath.getAbsolutePath(), "repo", "RPMMD", parts[0]).toFile();
                if (oldMirrorPath.exists()) {
                    mirrorPath = oldMirrorPath;
                }
                else {
                    // mirror in a common folder (bsc#1201753)
                    File commonMirrorPath = Paths.get(dataPath.getAbsolutePath(), path).toFile();
                    if (commonMirrorPath.exists()) {
                        mirrorPath = commonMirrorPath;
                    }
                }
            }
        }
        Path cleanPath = mirrorPath.toPath().normalize();
        if (!cleanPath.startsWith(sccDataPath)) {
            log.error("Resulting path outside of configured directory {}: {}", dataPath, urlString);
            cleanPath = dataPath.toPath();
        }
        return cleanPath.toUri().normalize();
    }
}
