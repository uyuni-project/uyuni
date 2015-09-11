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

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.util.http.HttpClientAdapter;
import com.redhat.rhn.domain.scc.SCCRepository;

import com.google.gson.Gson;
import com.suse.scc.model.SCCProduct;
import com.suse.scc.model.SCCSubscription;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.NoRouteToHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class representation of a connection to SCC for issuing API requests.
 */
public class SCCWebClient implements SCCClient {

    /** The config object. */
    private final SCCConfig config;

    /** Adapter object for handling HTTP requests. */
    private final HttpClientAdapter httpClient;

    /**
     *  Represents a partial result with a pointer to the next one.
     *
     * @param <T> the generic type
     */
    private class PaginatedResult<T> {

        /** The result. */
        private final T result;

        /** The next url. */
        private final String nextUrl;

        /**
         * Instantiates a new paginated result.
         *
         * @param resultIn the result in
         * @param nextUrlIn the next url in
         */
        PaginatedResult(T resultIn, String nextUrlIn) {
            result = resultIn;
            nextUrl = nextUrlIn;
        }
    }

    /**
     * Constructor for connecting to SUSE Customer Center.
     * @param configIn the configuration object
     */
    public SCCWebClient(SCCConfig configIn) {
        config = configIn;
        httpClient = new HttpClientAdapter();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SCCRepository> listRepositories() throws SCCClientException {
        return getList("/connect/organizations/repositories",
                SCCRepository.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SCCProduct> listProducts() throws SCCClientException {
        return getList(
                "/connect/organizations/products/unscoped", SCCProduct.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SCCSubscription> listSubscriptions() throws SCCClientException {
        return getList("/connect/organizations/subscriptions",
                SCCSubscription.class);
    }

    /**
     * Perform a GET request and parse the result into list of given {@link Class}.
     *
     * @param <T> the generic type
     * @param endpoint the GET request endpoint
     * @param resultType the type of the result
     * @return object of type given by resultType
     * @throws SCCClientException if the request was not successful
     */
    private <T> List<T> getList(String endpoint, Type resultType)
            throws SCCClientException {
        List<T> result = new LinkedList<T>();
        PaginatedResult<List<T>> partialResult;
        do {
            partialResult = request(endpoint, SCCClientUtils.toListType(resultType), "GET");
            result.addAll(partialResult.result);
            endpoint = partialResult.nextUrl;
        } while (partialResult.nextUrl != null);
        return result;
    }

    /**
     * Perform HTTP request and parse the result into a given result type.
     *
     * @param <T> the generic type
     * @param endpoint the endpoint
     * @param resultType the type of the result
     * @param method the HTTP method to use
     * @return object of type given by resultType
     * @throws SCCClientException in case of a problem
     */
    private <T> PaginatedResult<T> request(String endpoint, Type resultType, String method)
            throws SCCClientException {
        Reader streamReader = null;
        HttpMethod request = SCCRequestFactory.getInstance().initRequest(
                method, endpoint, config);
        try {
            // Connect and parse the response on success
            int responseCode = httpClient.executeRequest(request,
                    config.getUsername(), config.getPassword());
            if (responseCode == HttpStatus.SC_OK) {
                streamReader = SCCClientUtils.getLoggingReader(request,
                        config.getUsername(), config.getLoggingDir());

                // Parse result type from JSON
                Gson gson = new Gson();
                T result = gson.fromJson(streamReader, resultType);

                String nextUrl = null;
                Header linkHeader = request.getResponseHeader("Link");
                if (linkHeader != null) {
                    String linkHeaderValue = linkHeader.getValue();
                    Matcher m = Pattern
                            .compile(".*<" + config.getUrl() + "(.*?)>; rel=\"next\".*")
                            .matcher(linkHeaderValue);
                    if (m.matches()) {
                        nextUrl = m.group(1);
                    }
                }
                return new PaginatedResult<T>(result, nextUrl);
            }
            else {
                // Request was not successful
                throw new SCCClientException("Got response code " + responseCode +
                        " connecting to " + request.getURI());
            }
        }
        catch (NoRouteToHostException e) {
            String proxy = ConfigDefaults.get().getProxyHost();
            throw new SCCClientException("No route to SCC" +
                    (proxy != null ? " or the Proxy: " + proxy : ""));
        }
        catch (IOException e) {
            throw new SCCClientException(e);
        }
        finally {
            request.releaseConnection();
            SCCClientUtils.closeQuietly(streamReader);
        }
    }
}
