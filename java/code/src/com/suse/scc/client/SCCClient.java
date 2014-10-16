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
package com.suse.scc.client;

import com.suse.scc.model.SCCProduct;
import com.suse.scc.model.SCCRepository;
import com.suse.scc.model.SCCSubscription;

import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

/**
 * SCC API Client.
 */
public class SCCClient {

    private final SCCConfig config = new SCCConfig();

    /**
     * Constructor for connecting to scc.suse.com.
     *
     * @param username the username
     * @param password the password
     * @throws URISyntaxException
     */
    public SCCClient(String username, String password)
            throws URISyntaxException {
        this(SCCConfig.DEFAULT_URL, username, password);
    }

    /**
     * Constructor for connecting to scc.suse.com.
     *
     * @param url the URL of scc
     * @param username the username
     * @param password the password
     * @throws URISyntaxException
     */
    public SCCClient(String url, String username, String password)
            throws URISyntaxException {
        // Put the schema int the config
        config.setUrl(url);

        // Encode the given credentials
        byte[] credsBytes = Base64.encodeBase64((username + ':' + password).getBytes());
        String credsString = new String(credsBytes);
        config.put(SCCConfig.ENCODED_CREDS, credsString);
    }

    /**
     * Directly access the configuration
     * @return the configuration
     */
    public SCCConfig getConfig() {
        return config;
    }

    /**
     * Set the proxy for this client
     * @param settings Proxy settings
     */
    public void setProxySettings(SCCProxySettings settings) {
        if (settings.getHostname() != null) {
            config.put(SCCConfig.PROXY_HOSTNAME, settings.getHostname());
            config.put(SCCConfig.PROXY_PORT, String.valueOf(settings.getPort()));
        }
        if (settings.getUsername() != null) {
            config.put(SCCConfig.PROXY_USERNAME, settings.getUsername());
            if (settings.getPassword() != null) {
                config.put(SCCConfig.PROXY_PASSWORD, settings.getPassword());
            }
        }
    }

    /**
     * Configure this client's UUID that will be sent to SCC for debugging
     * purposes.
     * @param uuid the UUID to send for debugging
     */
    public void setUUID(String uuid) {
        if (uuid != null) {
            config.put(SCCConfig.UUID, uuid);
        }
    }

    /**
     * Set local resource path from where JSON data will be read instead of accessing
     * the network. If path is set to null, network will be used instead.
     * @param path
     * @throws SCCClientException
     */
    public void setResourceLocalPath(File path) throws SCCClientException {
        if (path == null) {
            this.config.remove(SCCConfig.RESOURCE_PATH);
            return;
        }

        if (!path.canRead()) {
            throw new SCCClientException(
                    String.format("Unable to access resource at \"%s\" location.",
                                  path.getAbsolutePath()));
        }
        else if (!path.isDirectory()) {
            throw new SCCClientException(
                    String.format("Path \"%s\" must be a directory.",
                                  path.getAbsolutePath()));
        }

        this.config.put(SCCConfig.RESOURCE_PATH, path.getAbsolutePath());
    }

    /**
     * Gets and returns the list of all products.
     *
     * GET /connect/organizations/products/unscoped
     *
     * @return list of all available products
     * @throws SCCClientException if anything goes wrong SCC side
     */
    public List<SCCProduct> listProducts() throws SCCClientException {
        return new SCCConnection("/connect/organizations/products/unscoped", config)
                .getList(SCCProduct.class);
    }

    /**
     * Gets and returns the list of repositories available to an organization.
     *
     * GET /connect/organizations/repositories
     *
     * @return list of repositories available to organization
     * @throws SCCClientException if anything goes wrong SCC side
     */
    public List<SCCRepository> listRepositories() throws SCCClientException {
        return new SCCConnection("/connect/organizations/repositories", config)
                .getList(SCCRepository.class);
    }

    /**
     * Gets and returns the list of subscriptions available to an organization.
     *
     * GET /connect/organizations/subscriptions
     *
     * @return list of subscriptions available to organization
     * @throws SCCClientException if anything goes wrong SCC side
     */
    public List<SCCSubscription> listSubscriptions() throws SCCClientException {
        return new SCCConnection("/connect/organizations/subscriptions", config)
                .getList(SCCSubscription.class);
    }
}
