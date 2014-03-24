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

import com.suse.manager.model.ncc.ListSubscriptions;
import com.suse.manager.model.ncc.Subscription;
import com.suse.manager.model.ncc.SubscriptionList;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

/**
 * Simple client for NCC subscription data
 */
public class NCCClient {

    // Logger for this class
    private static final Logger logger = Logger.getLogger(NCCClient.class);
    private final static String NCC_URL = "https://secure-www.novell.com/center/regsvc/?command=listsubscriptions";
    private String nccUrl;
    // Maximum number of redirects that will be followed
    private final static int MAX_REDIRECTS = 10;

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
     * @throws NCCException
     */
    public List<Subscription> downloadSubscriptions(MirrorCredentials creds) throws NCCException {
        // Setup XML to send it with the request
        ListSubscriptions listsubs = new ListSubscriptions();
        listsubs.setUser(creds.getUser());
        listsubs.setPassword(creds.getPassword());
        PostMethod post = new PostMethod(this.nccUrl);
        List<Subscription> subscriptions = null;
        try {
            // Serialize into XML
            Serializer serializer = new Persister();
            StringWriter xmlString = new StringWriter();
            serializer.write(listsubs, xmlString);
            RequestEntity entity = new StringRequestEntity(
                    xmlString.toString(), "text/xml", "UTF-8");

            // Manually follow redirects as long as we get 302
            HttpClient httpclient = new HttpClient();
            int result = 0;
            int redirects = 0;
            do {
                if (result == 302) {
                    // Prepare the redirect
                    Header locationHeader = post.getResponseHeader("Location");
                    String location = locationHeader.getValue();
                    logger.info("Got 302, following redirect to: " + location);
                    post = new PostMethod(location);
                    redirects++;
                }

                // Execute the request
                post.setRequestEntity(entity);
                result = httpclient.executeMethod(post);
                if (logger.isDebugEnabled()) {
                    logger.debug("Response status code: " + result);
                }
            } while (result == 302 && redirects < MAX_REDIRECTS);

            // Parse the response body in case of success
            if (result == 200) {
                InputStream stream = post.getResponseBodyAsStream();
                SubscriptionList subsList = serializer.read(SubscriptionList.class, stream);
                subscriptions = subsList.getSubscriptions();
                logger.info("Found " + subscriptions.size() + " subscriptions");
            }
        } catch (Exception e) {
            throw new NCCException(e);
        } finally {
            logger.debug("Releasing connection");
            post.releaseConnection();
        }
        return subscriptions;
    }
}
