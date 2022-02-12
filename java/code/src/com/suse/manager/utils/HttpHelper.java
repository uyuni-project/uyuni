/*
 * Copyright (c) 2020 SUSE LLC
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
package com.suse.manager.utils;

import com.redhat.rhn.common.util.http.HttpClientAdapter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * HttpHelper for easy doing HTTP requests
 */
public class HttpHelper {
    // Logger instance
    private static Logger log = Logger.getLogger(HttpHelper.class);

    // Adapter object for handling HTTP requests
    private final HttpClientAdapter httpClient;

    /**
     * Constructor
     */
    public HttpHelper() {
        httpClient = new HttpClientAdapter();
    }

    /**
     * Constructor
     * @param client a HttpClientAdapter
     */
    public HttpHelper(HttpClientAdapter client) {
        httpClient = client;
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
    public HttpResponse sendHeadRequest(String url, String username, String password)
            throws IOException {
        return sendHeadRequest(url, username, password, false);
    }

    /**
     * Send a GET request to a given URL.
     *
     * @param url the URL to verify
     * @return the response code of the request
     * @throws IOException in case of an error
     */
    public HttpResponse sendGetRequest(String url) throws IOException {
        return sendGetRequest(url, null, null);
    }

    /**
     * Send a GET request to a given URL.
     * When HttpResponse object is not needed anymore, cleanup(HttpResponse) should be called
     * to release the connection.
     *
     * @param url the URL to verify
     * @param username username for authentication (pass null for unauthenticated requests)
     * @param password password for authentication (pass null for unauthenticated requests)
     * @return the response code of the request
     * @throws IOException in case of an error
     */
    public HttpResponse sendGetRequest(String url, String username, String password)
            throws IOException {
        return sendGetRequest(url, username, password, false);
    }

    /**
     * Return body as String
     * @param response the response
     * @param defaultCharset character set to be applied if none found in the entity (NULL default to "ISO-8859-1")
     * @return body as String
     * @throws IOException
     */
    public String getBodyAsString(HttpResponse response, String defaultCharset) throws IOException {
        return EntityUtils.toString(response.getEntity(), defaultCharset);
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
    private HttpResponse sendHeadRequest(String url, String username, String password, boolean ignoreNoProxy)
            throws IOException {
        log.debug("HEAD: " + url);
        HttpHead headRequest = new HttpHead(url);
        try {
            return httpClient.executeRequest(headRequest, username, password, ignoreNoProxy);
        }
        finally {
            headRequest.releaseConnection();
        }
    }

    /**
     * Send a GET request to a given URL.
     * When HttpResponse object is not needed anymore, cleanup(HttpResponse) should be called
     * to release the connection.
     *
     * @param url the URL to verify
     * @param username username for authentication (pass null for unauthenticated requests)
     * @param password password for authentication (pass null for unauthenticated requests)
     * @param ignoreNoProxy set true to ignore the "no_proxy" setting
     * @return the HTTP response Object of the request
     * @throws IOException in case of an error
     */
    private HttpResponse sendGetRequest(String url, String username, String password, boolean ignoreNoProxy)
            throws IOException {
        log.debug("GET: " + url);
        HttpGet getRequest = new HttpGet(url);
        return httpClient.executeRequest(getRequest, username, password, ignoreNoProxy);
    }

    /**
     * Release all resources held by the
     * @param response
     */
    public void cleanup(HttpResponse response) {
        log.debug("Cleanup");
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            EntityUtils.consumeQuietly(entity);
        }
    }
}
