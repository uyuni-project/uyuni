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
package com.suse.scc;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.suse.scc.model.Product;

import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.zip.GZIPInputStream;

/**
 * Methods for talking to SCC.
 * TODO: Support proxies.
 */
public class SCCClient {

    private String hostname;
    private String username;
    private String password;

    /**
     * Constructor
     */
    public SCCClient(String hostname, String username, String password) {
        this.hostname = hostname;
        this.username = username;
        this.password = password;
    }

    /**
     * Gets and returns the list of products available for an organization.
     *
     * GET /connect/organizations/products
     *
     * @return list of products available for organization
     */
    public Collection<Product> listProducts() throws SCCClientException {
        Collection<Product> products = null;
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        GZIPInputStream gzipStream = null;

        try {
            // Setup connection
            String location = "https://" + hostname + "/connect/organizations/products";
            connection = SCCClientUtils.getConnection("GET", location);

            // Request content to be compressed
            connection.setRequestProperty("Accept-Encoding", "gzip, deflate");

            // Basic authentication
            byte[] encodedBytes = Base64.encodeBase64(
                    (username + ':' + password).getBytes("iso-8859-1"));
            final String encodedCreds = new String(encodedBytes, "iso-8859-1");
            connection.setRequestProperty("Authorization", "Basic " + encodedCreds);

            // Execute the request
            connection.connect();
            int responseCode = connection.getResponseCode();

            // Parse the response body in case of success
            if (responseCode == 200) {
                // Decompress the gzip stream
                inputStream = connection.getInputStream();
                gzipStream = new GZIPInputStream(inputStream);
                String productsJSON = SCCClientUtils.streamToString(gzipStream);

                // Parse from JSON
                Gson gson = new Gson();
                Type collectionType = new TypeToken<Collection<Product>>(){}.getType();
                products = gson.fromJson(productsJSON, collectionType);
            }
        }
        catch (MalformedURLException e) {
            throw new SCCClientException(e);
        }
        catch (IOException e) {
            throw new SCCClientException(e);
        }
        finally {
            // Disconnect
            if (connection != null) {
                connection.disconnect();
            }
            // Close streams
            SCCClientUtils.closeQuietly(inputStream);
            SCCClientUtils.closeQuietly(gzipStream);
        }
        return products;
    }
}
