/**
 * Copyright (c) 2014--2015 SUSE LLC
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

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 * Helper class for setting up HTTP Requests as {@link HttpMethod} objects.
 */
public class SCCRequestFactory {

    /** Singleton instance. */
    private static SCCRequestFactory instance = new SCCRequestFactory();

    /**
     * Instantiates a new SCC request factory.
     */
    private SCCRequestFactory() {
    }

    /**
     * Gets the single instance of SCCRequestFactory.
     * @return single instance of SCCRequestFactory
     */
    public static SCCRequestFactory getInstance() {
        return instance;
    }

    /**
     * Init an HTTP request to an SCC endpoint for a given config.
     *
     * @param method the HTTP method to use
     * @param endpoint the SCC API endpoint
     * @param config SCC client configuration
     * @return {@link HttpMethod} object representing the request
     * @throws SCCClientException in case of an error
     */
    public HttpMethod initRequest(String method, String endpoint, SCCConfig config)
            throws SCCClientException {
        HttpMethod request = null;
        if (method.equals("GET")) {
            request = new GetMethod(config.getUrl() + endpoint);
        }
        else {
            throw new SCCClientException("HTTP method not supported: " + method);
        }

        // Additional request headers
        request.setRequestHeader("Accept", "application/vnd.scc.suse.com.v4+json");
        request.setRequestHeader("Accept-Encoding", "gzip, deflate");

        // Send the UUID for debugging if available
        String uuid = config.getUUID();
        request.setRequestHeader("SMS", uuid != null ? uuid : "undefined");

        return request;
    }
}
