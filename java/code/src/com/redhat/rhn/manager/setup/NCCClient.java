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
package com.redhat.rhn.manager.setup;

import com.redhat.rhn.common.conf.ConfigDefaults;

import com.suse.manager.model.ncc.ListSubscriptions;
import com.suse.manager.model.ncc.Subscription;
import com.suse.manager.model.ncc.SubscriptionList;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.List;

/**
 * Simple client for NCC subscription data
 */
public class NCCClient {

    /** Logger instance. */
    private static Logger log = Logger.getLogger(NCCClient.class);

    private static final String NCC_URL = "https://secure-www.novell.com/center/regsvc/";
    private static final String NCC_PING_COMMAND = "?command=ping";
    private static final String NCC_LIST_SUBSCRIPTIONS_COMMAND =
            "?command=listsubscriptions";
    private static final int MAX_REDIRECTS = 10;

    private String nccUrl;

    /**
     * Creates a client for the NCC registration service
     * using the default known URL
     */
    public NCCClient() {
        this.nccUrl = NCC_URL;
    }

    /**
     * Creates a client for the NCC registration service
     * @param url Custom URL
     */
    public NCCClient(String url) {
        this.nccUrl = url;
    }

    /**
     * Sets the url for the NCC service.
     * This method is useful if you want to do testing against
     * a development or mock server.
     * @param url NCC url
     */
    public void setNCCUrl(String url) {
        this.nccUrl = url;
    }

    /**
     * Connect to NCC and return subscriptions for a given pair of credentials.
     * @param creds the mirror credentials to use
     * @return list of subscriptions available via the given credentials
     * @throws NCCException in case something bad happens with NCC
     */
    public List<Subscription> downloadSubscriptions(MirrorCredentialsDto creds)
        throws NCCException {
        // Setup XML to send it with the request
        ListSubscriptions listsubs = new ListSubscriptions();
        listsubs.setUser(creds.getUser());
        listsubs.setPassword(creds.getPassword());
        List<Subscription> subscriptions = null;
        Serializer serializer = new Persister();
        HttpURLConnection connection = null;

        try {
            // Perform request(s)
            String location = this.nccUrl + NCC_LIST_SUBSCRIPTIONS_COMMAND;
            int result = 302;
            int redirects = 0;

            while (result == 302 && redirects < MAX_REDIRECTS) {
                // Initialize connection
                connection = getConnection("POST", location);
                connection.setDoOutput(true);
                connection.setInstanceFollowRedirects(false);

                // Serialize into XML
                serializer.write(listsubs, connection.getOutputStream());

                // Execute the request
                connection.connect();
                result = connection.getResponseCode();
                if (log.isDebugEnabled()) {
                    log.debug("Response status code: " + result);
                }

                if (result == 302) {
                    // Prepare the redirect
                    location = connection.getHeaderField("Location");
                    log.info("Got 302, following redirect to: " + location);
                    connection.disconnect();
                    redirects++;
                }
            }

            // Parse the response body in case of success
            if (result == 200) {
                InputStream stream = connection.getInputStream();
                SubscriptionList subsList = serializer.read(SubscriptionList.class, stream);
                subscriptions = subsList.getSubscriptions();
                log.info("Found " + subscriptions.size() + " subscriptions");
            }
        }
        catch (Exception e) {
            throw new NCCException(e);
        }
        finally {
            if (connection != null) {
                log.debug("Releasing connection");
                connection.disconnect();
            }
        }
        return subscriptions;
    }

    /**
     * Returns an HTTP connection object to query NCC. Returned client has proxy
     * configured.
     * @param method HTTP method to use
     * @param location URL to make requests to
     * @return the http connection
     * @throws IOException if network errors happen
     * @throws MalformedURLException if location is not valid
     */
    public HttpURLConnection getConnection(String method, String location)
        throws MalformedURLException, IOException {
        ConfigDefaults configDefaults = ConfigDefaults.get();
        String proxyHost = configDefaults.getProxyHost();
        URL url = new URL(location);

        HttpURLConnection result = null;
        if (!StringUtils.isEmpty(proxyHost)) {
            int proxyPort = configDefaults.getProxyPort();
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost,
                proxyPort));

            result = (HttpURLConnection) url.openConnection(proxy);

            String proxyUsername = configDefaults.getProxyUsername();
            String proxyPassword = configDefaults.getProxyPassword();
            if (!StringUtils.isEmpty(proxyUsername) &&
                !StringUtils.isEmpty(proxyPassword)) {
                try {
                    byte[] encodedBytes = Base64
                        .encodeBase64((proxyUsername + ':' + proxyPassword)
                            .getBytes("iso-8859-1"));
                    final String encoded = new String(encodedBytes, "iso-8859-1");
                    result.addRequestProperty("Proxy-Authorization", encoded);
                }
                catch (UnsupportedEncodingException e) {
                    // can't happen
                }
            }
        }
        else {
            result = (HttpURLConnection) url.openConnection();
        }

        result.setRequestMethod(method);

        return result;
    }

    /**
     * Pings NCC.
     * @return true if NCC is reachable, false if it is not
     */
    public boolean ping() {
        try {
            HttpURLConnection connection = getConnection("GET", nccUrl + NCC_PING_COMMAND);
            connection.connect();
            return connection.getResponseCode() == 200;
        }
        catch (IOException e) {
            return false;
        }
    }
}
