/*
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
package com.suse.scc.test;

import com.suse.scc.client.SCCClient;
import com.suse.scc.client.SCCClientException;
import com.suse.scc.client.SCCConfig;
import com.suse.scc.client.SCCWebClient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.concurrent.Callable;

/**
 * Convenience requester that is specific to make SCC requests.
 *
 * @param <T> a generic result type
 */
public abstract class SCCRequester<T> implements Callable<T> {

    /** Logger instance. */
    private final Logger log = LogManager.getLogger(SCCRequester.class);

    /** The client instance. */
    private final SCCClient scc;

    /**
     * Default constructor
     * @param uri the server URI
     */
    protected SCCRequester(URI uri) {
        SCCConfig config = new SCCConfig(uri, "user", "password", null, null,
            System.getProperty("java.io.tmpdir"));
        scc = new SCCWebClient(config);
    }

    /**
     * Gets the client instance.
     * @return the client
     */
    public SCCClient getClient() {
        return scc;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T call() {
        T ret = null;
        try {
            ret = request(scc);
        }
        catch (SCCClientException e) {
            // this might or might not be fatal as some tests expect exceptions
            log.warn("Got SCCClientException while processing request, detail below");
            log.warn(e);
        }
        return ret;
    }

    /**
     * Run a request to SCC.
     *
     * @param clientIn the client object to make the request
     * @return a generic request result
     * @throws SCCClientException if there is a problem
     */
    public abstract T request(SCCClient clientIn) throws SCCClientException;
}
