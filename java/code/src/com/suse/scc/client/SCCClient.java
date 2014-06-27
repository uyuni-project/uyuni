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

import com.google.gson.reflect.TypeToken;
import com.suse.scc.model.SCCProduct;
import com.suse.scc.model.SCCRepository;

import org.apache.commons.codec.binary.Base64;

import java.lang.reflect.Type;
import java.util.List;

/**
 * SCC API Client.
 * TODO: Support proxied connections.
 */
public class SCCClient {

    /**
     * Constructor expecting a hostname and credentials.
     */
    public SCCClient(String hostname, String username, String password) {
        // Put a given hostname to config
        if (hostname != null) {
            SCCConfig.getInstance().put(SCCConfig.HOSTNAME, hostname);
        }

        // Encode the given credentials
        byte[] credsBytes = Base64.encodeBase64((username + ':' + password).getBytes());
        String credsString = new String(credsBytes);
        SCCConfig.getInstance().put(SCCConfig.ENCODED_CREDS, credsString);
    }

    /**
     * Constructor for connecting to scc.suse.com.
     */
    public SCCClient(String username, String password) {
        this(null, username, password);
    }

    /**
     * Gets and returns the list of products available to an organization.
     *
     * GET /connect/organizations/products
     *
     * @return list of products available to organization
     */
    public List<SCCProduct> listProducts() throws SCCClientException {
        SCCConnection scc = new SCCConnection("/connect/organizations/products");
        Type returnType = new TypeToken<List<SCCProduct>>(){}.getType();
        return scc.get(returnType);
    }

    /**
     * Gets and returns the list of available repositories to an organization.
     *
     * GET /connect/organizations/repositories
     *
     * @return list of repositories available to organization
     */
    public List<SCCRepository> listRepositories() throws SCCClientException {
        SCCConnection scc = new SCCConnection("/connect/organizations/repositories");
        Type returnType = new TypeToken<List<SCCRepository>>(){}.getType();
        return scc.get(returnType);
    }
}
