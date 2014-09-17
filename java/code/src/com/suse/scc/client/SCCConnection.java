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

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Class representation of a connection to SCC for issuing API requests.
 */
public class SCCConnection {

    /** The endpoint. */
    private final String endpoint;

    /** The gzip encoding string. */
    private final String GZIP_ENCODING = "gzip";

    /** The config object. */
    private final SCCConfig config;

    /**
     * Init a connection to a given SCC endpoint.
     *
     * @param endpointIn the endpoint
     * @param configIn the config
     */
    public SCCConnection(String endpointIn, SCCConfig configIn) {
        endpoint = endpointIn;
        config = configIn;
    }

    /**
     * Perform a GET request and parse the result into list of given {@link Class}.
     *
     * @param <T> the generic type
     * @param resultType the type of the result
     * @return object of type given by resultType
     * @throws SCCClientException if the request was not successful
     */
    public <T> T get(Type resultType) throws SCCClientException {
        return request(resultType, "GET");
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
    private <T> T request(Type resultType, String method) throws SCCClientException {
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
                return gson.fromJson(streamReader, toListType(resultType));
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
}
