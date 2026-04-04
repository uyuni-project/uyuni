/*
 * Copyright (c) 2014--2024 SUSE LLC
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
import com.suse.scc.model.SCCOrganizationSystemsUpdateResponse;
import com.suse.scc.model.SCCProductJson;
import com.suse.scc.model.SCCRegisterSystemItem;
import com.suse.scc.model.SCCRepositoryJson;
import com.suse.scc.model.SCCSubscriptionJson;
import com.suse.scc.model.SCCUpdateSystemItem;
import com.suse.scc.model.SCCVirtualizationHostJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.NoRouteToHostException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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
    private static final Logger LOG = LogManager.getLogger(SCCWebClient.class);
    private final Gson gson = new GsonBuilder()
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
    private static class PaginatedResult<T> {

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
        this(configIn, new HttpClientAdapter(
                Optional.ofNullable(configIn).map(SCCConfig::getAdditionalCerts).orElse(List.of()), false));
    }

    /**
     * Constructor for testing purposes
     * @param configIn the configuration object
     * @param httpClientIn the HttpClientAdapter object
     */
    public SCCWebClient(SCCConfig configIn, HttpClientAdapter httpClientIn) {
        config = configIn;
        httpClient = httpClientIn;
    }

    private <T> T writeCache(T value, String name) {
        Path credentialCache = getCacheDir();
        try {

            UserPrincipal tomcatUser = null;
            UserPrincipal rootUser = null;
            if (!config.isSkipOwner()) {
                FileSystem fileSystem = FileSystems.getDefault();
                UserPrincipalLookupService service = fileSystem.getUserPrincipalLookupService();
                tomcatUser = service.lookupPrincipalByName("tomcat");
                rootUser = service.lookupPrincipalByName("root");
            }

            Files.createDirectories(credentialCache);
            if (!config.isSkipOwner() && Files.getOwner(credentialCache, LinkOption.NOFOLLOW_LINKS).equals(rootUser)) {
                Files.setOwner(credentialCache, tomcatUser);
            }

            Path jsonFilePath = credentialCache.resolve(name + ".json");
            try (BufferedWriter file = Files.newBufferedWriter(jsonFilePath)) {
                gson.toJson(value, file);
                if (!config.isSkipOwner() && Files.getOwner(jsonFilePath, LinkOption.NOFOLLOW_LINKS).equals(rootUser)) {
                    Files.setOwner(jsonFilePath, tomcatUser);
                }
            }
        }
        catch (IOException e) {
            LOG.error(e);
            throw new SCCClientException(e);
        }
        return value;
    }

    public Path getCacheDir() {
        return Paths.get(config.getLoggingDir(),
            config.getUsername().replaceAll("[^a-zA-Z0-9\\._]+", "_"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SCCRepositoryJson> listRepositories() throws SCCClientException {
        List<SCCRepositoryJson> list = getList("/connect/organizations/repositories",
                SCCRepositoryJson.class);
        return writeCache(list, "organizations_repositories");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SCCProductJson> listProducts() throws SCCClientException {
        List<SCCProductJson> list = getList(
                "/connect/organizations/products/unscoped", SCCProductJson.class);
        return writeCache(list, "organizations_products_unscoped");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SCCSubscriptionJson> listSubscriptions() throws SCCClientException {
        List<SCCSubscriptionJson> list = getList("/connect/organizations/subscriptions",
                SCCSubscriptionJson.class);
        return writeCache(list, "organizations_subscriptions");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SCCOrderJson> listOrders() throws SCCClientException {
        List<SCCOrderJson> list = getList("/connect/organizations/orders",
                SCCOrderJson.class);
        return writeCache(list, "organizations_orders");
    }

    @Override
    public List<ProductTreeEntry> productTree() throws SCCClientException {
        List<ProductTreeEntry> list = getList("/suma/product_tree.json",
                ProductTreeEntry.class);
        return writeCache(list, "product_tree");
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
    private <T> List<T> getList(String endpoint, Class<T> resultType)
            throws SCCClientException {

        PaginatedResult<List<T>> firstPage = request(endpoint, SCCClientUtils.toListType(resultType), "GET");
        LOG.info("GET: {}{} Pages: {}", config.getUrl(), endpoint, firstPage.numPages);

        List<CompletableFuture<PaginatedResult<List<T>>>> futures = Stream.iterate(2, i -> i + 1)
                .limit(Math.max(0, firstPage.numPages - 1)).map(pageNum -> {
            String e = endpoint + "?page=" + pageNum;
            return CompletableFuture.supplyAsync(() -> {
                try {
                    LOG.debug("Start Page: {}", pageNum);
                    PaginatedResult<List<T>> page = request(e, SCCClientUtils.toListType(resultType), "GET");
                    LOG.debug("End Page: {}", pageNum);
                    return page;
                }
                catch (SCCClientException e1) {
                    throw new RuntimeException(e1);
                }
            }, executor);
        }).toList();

        CompletableFuture<Void> voidCompletableFuture = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0]));
        voidCompletableFuture.join();
        return Stream.concat(
                Stream.of(firstPage),
                futures.stream().map(CompletableFuture::join)
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

        // overwrite the default
        request.addHeader("User-Agent", ConfigDefaults.get().getProductName() + "/" +
                ConfigDefaults.get().getProductVersion());
        if (LOG.isDebugEnabled()) {
            Arrays.stream(request.getAllHeaders()).forEach(h -> LOG.debug(h.toString()));
        }
    }

    @Override
    public void deleteSystem(long id, String username, String password) throws SCCClientException {
        String url = config.getUrl() + "/connect/organizations/systems/" + id;
        HttpDelete request = new HttpDelete(url);
        addHeaders(request);
        BufferedReader streamReader = null;
        LOG.info("Send DELETE to {}", url);

        try {
            // Connect and parse the response on success
            HttpResponse response = httpClient.executeRequest(request,
                    username, password);

            int responseCode = response.getStatusLine().getStatusCode();

            // DELETE requests do not have content NO_CONTENT is OK
            if (responseCode != HttpStatus.SC_NO_CONTENT) {
                // Request was not successful
                streamReader = SCCClientUtils.getLoggingReader(request.getURI(), response,
                        username, config.getLoggingDir(), !config.isSkipOwner());
                throw new SCCClientException(responseCode, request.getURI().toString(),
                        String.format("Got response code %s connecting to %s: %s", responseCode,
                                request.getURI(), streamReader.lines().collect(Collectors.joining("\n"))));
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

    private String coreCreateUpdateSystems(String requestBody, String username, String password)
            throws SCCClientException {

        HttpPut request = new HttpPut(config.getUrl() + "/connect/organizations/systems");
        // Additional request headers
        addHeaders(request);
        request.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));

        LOG.info("Send PUT to {}{}", config.getUrl(), "/connect/organizations/systems");
        if (LOG.isDebugEnabled()) {
            LOG.debug("Request body: {}", requestBody);
        }

        try {
            // Connect and parse the response on success
            HttpResponse response = httpClient.executeRequest(request, username, password);

            int responseCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Response code: {}", responseCode);
                LOG.debug("Response body: {}", responseBody);
            }
            if (responseCode == HttpStatus.SC_CREATED) {
                return responseBody;
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
        }
    }

    @Override
    public void updateBulkLastSeen(List<SCCUpdateSystemItem> systems, String username, String password)
            throws SCCClientException {
        Map<String, List<SCCUpdateSystemItem>> payload = Map.of("systems", systems);
        String requestBody = gson.toJson(payload);
        coreCreateUpdateSystems(requestBody, username, password);
    }

    @Override
    public SCCOrganizationSystemsUpdateResponse createUpdateSystems(
            List<SCCRegisterSystemItem> systems, String username, String password) throws SCCClientException {
        Map<String, Collection<SCCRegisterSystemItem>> payload = Map.of("systems", systems);
        String requestBody = gson.toJson(payload);
        String responseBody = coreCreateUpdateSystems(requestBody, username, password);

        return gson.fromJson(responseBody, SCCOrganizationSystemsUpdateResponse.class);
    }

    @Override
    public void setVirtualizationHost(List<SCCVirtualizationHostJson> virtHostInfo, String username, String password)
            throws SCCClientException {

        HttpPut request = new HttpPut(config.getUrl() + "/connect/organizations/virtualization_hosts");
        // Additional request headers
        addHeaders(request);
        request.setEntity(new StringEntity(gson.toJson(Map.of("virtualization_hosts", virtHostInfo)),
                ContentType.APPLICATION_JSON));

        LOG.info("Send PUT to {}{}", config.getUrl(), "/connect/organizations/virtualization_hosts");
        if (LOG.isDebugEnabled()) {
            LOG.debug(gson.toJson(Map.of("virtualization_hosts", virtHostInfo)));
        }

        try {
            // Connect and parse the response on success
            HttpResponse response = httpClient.executeRequest(request, username, password);

            int responseCode = response.getStatusLine().getStatusCode();

            //TODO only created is documented by scc we still need to check what they return on update.
            if (responseCode != HttpStatus.SC_OK) {
                // Request was not successful
                if (LOG.isErrorEnabled()) {
                    LOG.error(response.toString());
                }
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
                        config.getUsername(), config.getLoggingDir(), !config.isSkipOwner());

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
                return new PaginatedResult<>(result, nextUrl, numPages);
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
