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

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.IOException;
import java.io.InputStream;
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
    private HttpClient httpClient;

    /**
     * Creates a client for the NCC registration service
     * using the default known URL
     */
    public NCCClient() {
        this(NCC_URL);
    }

    /**
     * Creates a client for the NCC registration service
     * @param url Custom URL
     */
    public NCCClient(String url) {
        this.nccUrl = url;
        this.httpClient = initHttpClient(null, null);
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
        PostMethod postMethod = new PostMethod();

        try {
            // Follow up to MAX_REDIRECTS redirects manually. HttpClient is unable to
            // automatically handle redirects of entity enclosing methods such as POST.
            String location = this.nccUrl + NCC_LIST_SUBSCRIPTIONS_COMMAND;
            int result = HttpStatus.SC_MOVED_TEMPORARILY;
            int redirects = 0;

            while (result == HttpStatus.SC_MOVED_TEMPORARILY && redirects < MAX_REDIRECTS) {
                postMethod = new PostMethod(location);

                // Set the XML request entity
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                serializer.write(listsubs, stream);
                postMethod.setRequestEntity(
                        new ByteArrayRequestEntity(stream.toByteArray()));

                // Execute the request and prepare redirect if necessary
                result = httpClient.executeMethod(postMethod);

                if (result == HttpStatus.SC_MOVED_TEMPORARILY) {
                    location = postMethod.getResponseHeader("Location").getValue();
                    if (log.isDebugEnabled()) {
                        log.debug("Got 302, following redirect to: " + location);
                    }
                    postMethod.releaseConnection();
                    redirects++;
                }
            }

            // Parse the response body in case of success
            if (result == HttpStatus.SC_OK) {
                InputStream stream = postMethod.getResponseBodyAsStream();
                SubscriptionList subsList = serializer.read(SubscriptionList.class, stream);
                subscriptions = subsList.getSubscriptions();
                log.info("Found " + subscriptions.size() + " subscriptions");
            }
        }
        // Generic exception is thrown from serializer.write() and read()
        catch (Exception e) {
            throw new NCCException(e);
        }
        finally {
            postMethod.releaseConnection();
        }
        return subscriptions;
    }

    /**
     * Pings NCC.
     * @return true if NCC is reachable, false if it is not
     */
    public boolean ping() {
        GetMethod getPing = new GetMethod(nccUrl + NCC_PING_COMMAND);
        try {
            int returnCode = httpClient.executeMethod(getPing);
            if (log.isDebugEnabled()) {
                log.debug("NCC ping return code: " + returnCode);
            }
            return returnCode == HttpStatus.SC_OK;
        }
        catch (IOException e) {
            return false;
        }
        finally {
            getPing.releaseConnection();
        }
    }

    /**
     * TODO: Make this a public method to be reused by other classes.
     * Initialize and return a {@link HttpClient} for general purpose.
     * @param username username for basic authentication (optional)
     * @param password password for basic authentication (optional)
     * @return {@link HttpClient} object
     */
    private HttpClient initHttpClient(String username, String password) {
        HttpClient httpClient = new HttpClient();

        // Apply proxy settings
        String proxyHost = ConfigDefaults.get().getProxyHost();
        if (!StringUtils.isBlank(proxyHost)) {
            int proxyPort = ConfigDefaults.get().getProxyPort();
            httpClient.getHostConfiguration().setProxy(proxyHost, proxyPort);
            if (log.isDebugEnabled()) {
                log.debug("Using proxy: " + proxyHost + ":" + proxyPort);
            }

            String proxyUsername = ConfigDefaults.get().getProxyUsername();
            String proxyPassword = ConfigDefaults.get().getProxyPassword();
            if (!StringUtils.isBlank(proxyUsername) &&
                    !StringUtils.isBlank(proxyPassword)) {
                Credentials proxyCredentials = new UsernamePasswordCredentials(
                        proxyUsername, proxyPassword);
                httpClient.getState().setProxyCredentials(AuthScope.ANY, proxyCredentials);
            }
        }

        // Basic authentication
        if (!StringUtils.isBlank(username) && !StringUtils.isBlank(password)) {
            Credentials creds = new UsernamePasswordCredentials(username, password);
            httpClient.getState().setCredentials(AuthScope.ANY, creds);
        }

        return httpClient;
    }
}
