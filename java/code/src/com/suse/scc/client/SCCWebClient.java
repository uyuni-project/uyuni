/**
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
package com.suse.scc.client;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.util.http.HttpClientAdapter;
import com.redhat.rhn.manager.content.ProductTreeEntry;

import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.scc.model.SCCOrderJson;
import com.suse.scc.model.SCCProductJson;
import com.suse.scc.model.SCCRegisterSystemJson;
import com.suse.scc.model.SCCRepositoryJson;
import com.suse.scc.model.SCCSubscriptionJson;
import com.suse.scc.model.SCCSystemCredentialsJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.NoRouteToHostException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class representation of a connection to SCC for issuing API requests.
 */
public class SCCWebClient implements SCCClient {

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private static Logger log = Logger.getLogger(SCCWebClient.class);
    private Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .create();

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

        private final int numPages;

        /**
         * Instantiates a new paginated result.
         *  @param resultIn the result in
         * @param nextUrlIn the next url in
         * @param numPagesIn number of pages
         */
        PaginatedResult(T resultIn, String nextUrlIn, int numPagesIn) {
            result = resultIn;
            nextUrl = nextUrlIn;
            this.numPages = numPagesIn;
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
    public List<SCCRepositoryJson> listRepositories() throws SCCClientException {
        return getList("/connect/organizations/repositories",
                SCCRepositoryJson.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SCCProductJson> listProducts() throws SCCClientException {
        return getList(
                "/connect/organizations/products/unscoped", SCCProductJson.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SCCSubscriptionJson> listSubscriptions() throws SCCClientException {
        return getList("/connect/organizations/subscriptions",
                SCCSubscriptionJson.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SCCOrderJson> listOrders() throws SCCClientException {
        return getList("/connect/organizations/orders",
                SCCOrderJson.class);
    }

    @Override
    public List<ProductTreeEntry> productTree() throws SCCClientException {
        return getList("/suma/product_tree.json",
                ProductTreeEntry.class);
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

        PaginatedResult<List<T>> firstPage = request(endpoint, SCCClientUtils.toListType(resultType), "GET");
        log.info("Pages: " + firstPage.numPages);

        List<CompletableFuture<PaginatedResult<List<T>>>> futures = Stream.iterate(2, i -> i + 1)
                .limit(Math.max(0, firstPage.numPages - 1)).map(pageNum -> {
            String e = endpoint + "?page=" + pageNum;
            CompletableFuture<PaginatedResult<List<T>>> get = CompletableFuture.supplyAsync(() -> {
                try {
                    log.info("Start Page: " + pageNum);
                    PaginatedResult<List<T>> page = request(e, SCCClientUtils.toListType(resultType), "GET");
                    log.info("End Page: " + pageNum);
                    return page;
                }
                catch (SCCClientException e1) {
                    throw new RuntimeException(e1);
                }
            }, executor);
            return get;
        }).collect(Collectors.toList());

        CompletableFuture<Void> voidCompletableFuture = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[futures.size()]));
        voidCompletableFuture.join();
        return Stream.concat(
                Stream.of(firstPage),
                futures.stream().map(f -> f.join())
                )
                .flatMap(p -> p.result.stream())
                .collect(Collectors.toList());
    }

    private void addHeaders(AbstractHttpMessage request) {
        request.addHeader("Accept", "application/vnd.scc.suse.com.v4+json");
        request.addHeader("Accept-Encoding", "gzip, deflate");

        // Send the UUID for debugging if available
        String uuid = config.getUUID();
        request.addHeader("SMS", uuid != null ? uuid : "undefined");
    }

    @Override
    public void deleteSystem(long id) throws SCCClientException {
        HttpDelete request = new HttpDelete(config.getUrl() + "/connect/organizations/systems/" + id);
        addHeaders(request);
        Reader streamReader = null;
        try {
            // Connect and parse the response on success
            HttpResponse response = httpClient.executeRequest(request,
                    config.getUsername(), config.getPassword());

            int responseCode = response.getStatusLine().getStatusCode();

            if (responseCode == HttpStatus.SC_NO_CONTENT) {
                streamReader = SCCClientUtils.getLoggingReader(request.getURI(), response,
                        config.getUsername(), config.getLoggingDir());

            }
            else {
                // Request was not successful
                throw new SCCClientException(responseCode, request.getURI().toString(),
                        "Got response code " + responseCode + " connecting to " + request.getURI());
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

    @Override
    public SCCSystemCredentialsJson createSystem(SCCRegisterSystemJson system) throws SCCClientException {
        HttpPost request = new HttpPost(config.getUrl() + "/connect/organizations/systems");
        // Additional request headers
        addHeaders(request);
        request.setEntity(new StringEntity(gson.toJson(system), ContentType.APPLICATION_JSON));

        Reader streamReader = null;
        try {
            // Connect and parse the response on success
            HttpResponse response = httpClient.executeRequest(request,
                    config.getUsername(), config.getPassword());

            int responseCode = response.getStatusLine().getStatusCode();

            //TODO only created is documented by scc we still need to check what they return on update.
            if (responseCode == HttpStatus.SC_CREATED) {
                streamReader = SCCClientUtils.getLoggingReader(request.getURI(), response,
                        config.getUsername(), config.getLoggingDir());

                return gson.fromJson(streamReader, SCCSystemCredentialsJson.class);
            }
            else {
                // Request was not successful
                throw new SCCClientException(responseCode, request.getURI().toString(),
                        "Got response code " + responseCode + " connecting to " + request.getURI());
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
        HttpRequestBase request = SCCRequestFactory.getInstance().initRequest(
                method, endpoint, config);
        try {
            // Connect and parse the response on success
            HttpResponse response = httpClient.executeRequest(request,
                    config.getUsername(), config.getPassword());

            int responseCode = response.getStatusLine().getStatusCode();

            if (responseCode == HttpStatus.SC_OK) {
                streamReader = SCCClientUtils.getLoggingReader(request.getURI(), response,
                        config.getUsername(), config.getLoggingDir());

                // Parse result type from JSON
                T result = gson.fromJson(streamReader, resultType);

                Optional<Integer> perPageOpt = Optional.ofNullable(response.getFirstHeader("Per-Page"))
                        .map(h -> Integer.parseInt(h.getValue()));
                Optional<Integer> totalOpt = Optional.ofNullable(response.getFirstHeader("Total"))
                        .map(h -> Integer.parseInt(h.getValue()));
                Optional<Integer> numPagesOpt = perPageOpt.flatMap(perPage -> totalOpt
                        .map(total -> (int)Math.ceil(total / perPage.floatValue())));

                int numPages = numPagesOpt.orElse(1);

                String nextUrl = null;
                Header linkHeader = response.getFirstHeader("Link");
                if (linkHeader != null) {
                    String linkHeaderValue = linkHeader.getValue();
                    Matcher m = Pattern
                            .compile(".*<" + config.getUrl() + "(.*?)>; rel=\"next\".*")
                            .matcher(linkHeaderValue);
                    if (m.matches()) {
                        nextUrl = m.group(1);
                    }
                }
                return new PaginatedResult<T>(result, nextUrl, numPages);
            }
            else {
                // Request was not successful
                throw new SCCClientException(responseCode, request.getURI().toString(),
                        "Got response code " + responseCode + " connecting to " + request.getURI());
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
