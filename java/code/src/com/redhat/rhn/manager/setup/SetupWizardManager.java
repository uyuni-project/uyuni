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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.BaseManager;
import com.redhat.rhn.manager.satellite.ConfigureSatelliteCommand;
import com.suse.manager.model.ncc.ListSubscriptions;
import com.suse.manager.model.ncc.Subscription;
import com.suse.manager.model.ncc.SubscriptionList;

public class SetupWizardManager extends BaseManager {

    // Logger for this class
    private static Logger logger = Logger.getLogger(SetupWizardManager.class);

    // Config keys
    public final static String KEY_MIRRCREDS_USER = "server.susemanager.mirrcred_user";
    public final static String KEY_MIRRCREDS_PASS = "server.susemanager.mirrcred_pass";
    public final static String KEY_MIRRCREDS_EMAIL = "server.susemanager.mirrcred_email";

    // NCC URL for listing subscriptions
    private final static String NCC_URL = "https://secure-www.novell.com/center/regsvc/?command=listsubscriptions";

    // Session attribute keys
    private final static String SUBSCRIPTIONS_KEY = "SETUP_WIZARD_SUBSCRIPTIONS";

    // Maximum number of redirects that will be followed
    private final static int MAX_REDIRECTS = 10;

    /**
     * Find all valid mirror credentials and return them.
     * @return List of all available mirror credentials
     */
    public static List<MirrorCredentials> findMirrorCredentials() {
        List<MirrorCredentials> credsList = new ArrayList<MirrorCredentials>();

        // Get the main pair of credentials
        String user = Config.get().getString(KEY_MIRRCREDS_USER);
        String password = Config.get().getString(KEY_MIRRCREDS_PASS);
        String email = Config.get().getString(KEY_MIRRCREDS_EMAIL);

        // Add credentials as long as they have user and password
        MirrorCredentials creds;
        int id = 0;
        while (user != null && password != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Found credentials (" + id + "): " + user);
            }

            // Create credentials object
            creds = new MirrorCredentials(email, user, password);
            creds.setId(new Long(id));
            credsList.add(creds);

            // Search additional credentials with continuous enumeration
            id++;
            user = Config.get().getString(KEY_MIRRCREDS_USER + "." + id);
            password = Config.get().getString(KEY_MIRRCREDS_PASS + "." + id);
            email = Config.get().getString(KEY_MIRRCREDS_EMAIL + "." + id);
        }
        return credsList;
    }

    /**
     * Find mirror credentials for a given ID.
     * @return pair of credentials for given ID.
     */
    public static MirrorCredentials findMirrorCredentials(long id) {
        // Generate suffix depending on the ID
        String suffix = "";
        if (id > 0) {
            suffix = "." + id;
        }

        // Get the credentials from config
        String user = Config.get().getString(KEY_MIRRCREDS_USER + suffix);
        String password = Config.get().getString(KEY_MIRRCREDS_PASS + suffix);
        String email = Config.get().getString(KEY_MIRRCREDS_EMAIL + suffix);
        MirrorCredentials creds = null;
        if (user != null && password != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Found credentials (" + id + "): " + user);
            }

            // Create credentials object
            creds = new MirrorCredentials(email, user, password);
            creds.setId(id);
        }
        return creds;
    }

    /**
     * Store a given pair of credentials in the filesystem using the next available index.
     * @param creds mirror credentials to store
     * @param user the current user
     * @return list of validation errors or null in case of success
     */
    public static ValidatorError[] storeMirrorCredentials(MirrorCredentials creds, User userIn) {
        if (creds.getUser() == null || creds.getPassword() == null) {
            return null;
        }

        // Find the first free ID if necessary
        Long id = creds.getId();
        if (creds.getId() == null) {
            List<MirrorCredentials> credentials = SetupWizardManager.findMirrorCredentials();
            id = new Long(credentials.size());
        }

        // Generate suffix depending on the ID
        String suffix = "";
        if (id > 0) {
            suffix = "." + id;
        }
        ConfigureSatelliteCommand configCommand = new ConfigureSatelliteCommand(userIn);
        configCommand.updateString(KEY_MIRRCREDS_USER + suffix, creds.getUser());
        configCommand.updateString(KEY_MIRRCREDS_PASS + suffix, creds.getPassword());
        if (creds.getEmail() != null) {
            configCommand.updateString(KEY_MIRRCREDS_EMAIL + suffix, creds.getEmail());
        }
        return configCommand.storeConfiguration();
    }

    /**
     * Delete a pair of credentials given by their ID. Includes some sophisticated logic
     * to shift IDs in case you delete a pair of credentials from the middle.
     * @param id the id of credentials being deleted
     * @param userIn the user currently logged in
     * @return list of validation errors or null in case of success
     */
    public static ValidatorError[] deleteMirrorCredentials(Long id, User userIn) {
        // Find all credentials and see what needs to be done
        List<MirrorCredentials> creds = SetupWizardManager.findMirrorCredentials();

        // TODO: Delete subscriptions from session cache
        if (creds.size() == id + 1) {
            // Just store empty credentials
            MirrorCredentials delCreds = new MirrorCredentials("", "", "");
            delCreds.setId(id);
            return SetupWizardManager.storeMirrorCredentials(delCreds, userIn);
        }
        else if (creds.size() > id + 1) {
            // We need to shift indices
            ConfigureSatelliteCommand configCommand = new ConfigureSatelliteCommand(userIn);
            for (MirrorCredentials c : creds) {
                int index = creds.indexOf(c);
                if (index > id) {
                    String targetSuffix = "";
                    if (index > 1) {
                        targetSuffix = "." + (index - 1);
                    }
                    configCommand.updateString(KEY_MIRRCREDS_USER + targetSuffix, c.getUser());
                    configCommand.updateString(KEY_MIRRCREDS_PASS + targetSuffix, c.getPassword());
                    if (c.getEmail() != null) {
                        configCommand.updateString(KEY_MIRRCREDS_EMAIL + targetSuffix, c.getEmail());
                    }
                    // Empty the last pair of credentials
                    if (index == creds.size() - 1) {
                        targetSuffix = "." + index;
                        configCommand.updateString(KEY_MIRRCREDS_USER + targetSuffix, "");
                        configCommand.updateString(KEY_MIRRCREDS_PASS + targetSuffix, "");
                        configCommand.updateString(KEY_MIRRCREDS_EMAIL + targetSuffix, "");
                    }
                }
            }
            return configCommand.storeConfiguration();
        }
        else {
            return null;
        }
    }

    /**
     * Connect to NCC and return subscriptions for a given pair of credentials.
     * @param creds the mirror credentials to use
     * @return list of subscriptions available via the given credentials
     */
    public static List<Subscription> downloadSubscriptions(MirrorCredentials creds) {
        // Setup XML to send it with the request
        ListSubscriptions listsubs = new ListSubscriptions();
        listsubs.setUser(creds.getUser());
        listsubs.setPassword(creds.getPassword());
        PostMethod post = new PostMethod(NCC_URL);
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
        } catch (HttpException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            logger.debug("Releasing connection");
            post.releaseConnection();
        }
        return subscriptions;
    }

    /**
     * Put a list of subscriptions in the session cache, while 'null' is stored whenever the
     * verification status is "failed" for a given pair of credentials.
     * @param subscriptions subscriptions
     * @param request request
     */
    @SuppressWarnings("unchecked")
    public static void storeSubsInSession(List<Subscription> subscriptions,
            MirrorCredentials creds, HttpServletRequest request) {
        HttpSession session = request.getSession();
        Map<String, List<Subscription>> subsMap =
                (Map<String, List<Subscription>>) session.getAttribute(SUBSCRIPTIONS_KEY);

        // Create the map for caching if it doesn't exist
        if (subsMap == null) {
            subsMap = new HashMap<String, List<Subscription>>();
            session.setAttribute(SUBSCRIPTIONS_KEY, subsMap);
        }

        // Store or update the subscriptions
        logger.debug("Storing subscriptions for " + creds.getUser());
        subsMap.put(creds.getUser(), subscriptions);
    }

    /**
     * Return cached list of subscriptions or null for signaling "verification failed".
     * @param creds credentials
     * @param request request
     * @return list of subscriptions or null signaling "verification failed"
     */
    @SuppressWarnings("unchecked")
    public static List<Subscription> getSubsFromSession(MirrorCredentials creds,
            HttpServletRequest request) {
        List<Subscription> ret = null;
        HttpSession session = request.getSession();
        Map<String, List<Subscription>> subsMap =
                (Map<String, List<Subscription>>) session.getAttribute(SUBSCRIPTIONS_KEY);
        if (subsMap != null) {
            ret = subsMap.get(creds.getUser());
        }
        return ret;
    }

    /**
     * Check if the verification status of any given credentials is unknown.
     * @param creds credentials
     * @param request request
     * @return true if verification status is unknown for the given creds, otherwise false.
     */
    @SuppressWarnings("unchecked")
    public static boolean verificationStatusUnknown(MirrorCredentials creds,
            HttpServletRequest request) {
        boolean ret = true;
        HttpSession session = request.getSession();
        Map<String, List<Subscription>> subsMap =
                (Map<String, List<Subscription>>) session.getAttribute(SUBSCRIPTIONS_KEY);
        if (subsMap != null && subsMap.containsKey(creds.getUser())) {
            ret = false;
        }
        return ret;
    }
}
