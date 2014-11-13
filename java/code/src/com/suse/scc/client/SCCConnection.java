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

import com.redhat.rhn.domain.scc.SCCRepository;

import com.google.gson.Gson;
import com.suse.scc.model.SCCProduct;
import com.suse.scc.model.SCCSubscription;

import org.apache.commons.codec.binary.Base64;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Class representation of a connection to SCC for issuing API requests.
 */
public class SCCConnection {

    /** The gzip encoding string. */
    private final String GZIP_ENCODING = "gzip";

    /** The config object. */
    private final SCCConfig config;

    /** Represents a partial result with a pointer to the next one. */
    private class PaginatedResult<T> {
        private final T result;
        private final String nextUrl;

        PaginatedResult(T resultIn, String nextUrlIn) {
            result = resultIn;
            nextUrl = nextUrlIn;
        }
    }

    /**
     * Constructor for connecting to scc.suse.com.
     *
     * @param url the URL of scc
     * @param username the username
     * @param password the password
     */
    public SCCConnection(String url, String username, String password) {
        config = new SCCConfig();

        config.put(SCCConfig.URL, url);

        byte[] credsBytes = Base64.encodeBase64((username + ':' + password).getBytes());
        String credsString = new String(credsBytes);
        config.put(SCCConfig.ENCODED_CREDS, credsString);
    }

    /**
     * Gets the configuration object.
     * @return the config
     */
    public SCCConfig getConfig() {
        return config;
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
    public <T> List<T> getList(String endpoint, Type resultType) throws SCCClientException {
        List<T> result = new LinkedList<T>();
        PaginatedResult<List<T>> partialResult;
        do {
            if (SCCConnection.this.config.getLocalResourcePath() == null) {
                partialResult = request(endpoint, toListType(resultType), "GET");
            }
            else {
                try {
                    partialResult = localFSRequest(endpoint, toListType(resultType));
                }
                catch (IOException ex) {
                    throw new SCCClientException(ex);
                }
            }
            result.addAll(partialResult.result);
            endpoint = partialResult.nextUrl;
        } while (partialResult.nextUrl != null);
        return result;
    }

    /**
     * Read data from the file in the local directory.
     * @param <T> the generic type
     * @return object of type given by resultType
     */
    @SuppressWarnings("unchecked")
    private <T> PaginatedResult<T> localFSRequest(String endpoint, Type resultType)
            throws FileNotFoundException,
                   IOException {
        String filename = endpoint
                .replaceAll("connect/", "/")     // Remove SCC part for APIs, no leading "/"
                .replaceAll("/+", "/")           // Replace double slashes to single
                .replaceAll("^/", "")            // Remove leading slash, if any
                .replaceAll("/", "_") + ".json"; // Replace slashes with underscore, add ext

        return new PaginatedResult<T>((T) new Gson().fromJson(
                new BufferedReader(new InputStreamReader(new FileInputStream(
                        new File(this.config.getLocalResourcePath() + "/" + filename)))),
                resultType), null);
    }

    /**
     * Perform HTTP request and parse the result into a given result type.
     *
     * @param <T> the generic type
     * @param resultType the type of the result
     * @param method the HTTP method to use
     * @return object of type given by resultType
     * @throws SCCClientException in case of a problem
     */
    private <T> PaginatedResult<T> request(String endpoint, Type resultType, String method)
        throws SCCClientException {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        GZIPInputStream gzipStream = null;
        try {
            // Setup the connection
            connection = SCCRequestFactory.getInstance().initConnection(
                    method, endpoint, config);

            // Connect and parse the response on success
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = connection.getInputStream();

                // Unzip stream if in gzip format
                Reader inputStreamReader;
                if (GZIP_ENCODING.equals(connection.getContentEncoding())) {
                    gzipStream = new GZIPInputStream(inputStream);
                    inputStreamReader = new InputStreamReader(gzipStream);
                }
                else {
                    inputStreamReader = new InputStreamReader(inputStream);
                }
                Reader streamReader = new BufferedReader(inputStreamReader);

                // Parse result type from JSON
                Gson gson = new Gson();
                T result = gson.fromJson(streamReader, resultType);

                String nextUrl = null;
                String linkHeader = connection.getHeaderField("Link");
                if (linkHeader != null) {
                    Matcher m = Pattern
                            .compile(".*<" + config.getUrl() + "(.*?)>; rel=\"next\".*")
                            .matcher(linkHeader);
                    if (m.matches()) {
                        nextUrl = m.group(1);
                    }
                }
                return new PaginatedResult<T>(result, nextUrl);
            }
            else {
                // Request was not successful
                throw new SCCClientException("Response code: " + responseCode);
            }
        }
        catch (IOException e) {
            throw new SCCClientException(e);
        }
        finally {
            // Clean up connection and streams
            if (connection != null) {
                connection.disconnect();
            }
            SCCClientUtils.closeQuietly(inputStream);
            SCCClientUtils.closeQuietly(gzipStream);
        }
    }

    /**
     * Returns a type which is a list of the specified type.
     * @param elementType the element type
     * @return the List type
     */
    public Type toListType(final Type elementType) {
        Type resultListType = new ParameterizedType() {

            @Override
            public Type[] getActualTypeArguments() {
                return new Type[] {elementType};
            }

            @Override
            public Type getRawType() {
                return List.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
        return resultListType;
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
        return getList("/connect/organizations/repositories",
                SCCRepository.class);
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
        return getList(
                "/connect/organizations/products/unscoped", SCCProduct.class);
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
        return getList("/connect/organizations/subscriptions",
                SCCSubscription.class);
    }
}
