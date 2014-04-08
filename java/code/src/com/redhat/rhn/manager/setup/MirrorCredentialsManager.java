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

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.channel.ChannelFamilyFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.BaseManager;
import com.redhat.rhn.manager.satellite.ConfigureSatelliteCommand;

import com.suse.manager.model.ncc.Subscription;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * Domain logic for the Setup Wizard mirror credentials page.
 */
public class MirrorCredentialsManager extends BaseManager {

    /** Logger instance */
    private static Logger log = Logger.getLogger(MirrorCredentialsManager.class);

    /** Configuration key prefix for mirror credential usernames */
    public static final String KEY_MIRRCREDS_USER = "server.susemanager.mirrcred_user";
    /** Configuration key prefix for mirror credential passwords */
    public static final String KEY_MIRRCREDS_PASS = "server.susemanager.mirrcred_pass";
    /** Configuration key prefix for mirror credential email addresses */
    public static final String KEY_MIRRCREDS_EMAIL = "server.susemanager.mirrcred_email";
    /** Configuration key separator for mirror credentials */
    public static final String KEY_MIRRCREDS_SEPARATOR = "_";

    /**
     * Find all valid mirror credentials and return them.
     * @return List of all available mirror credentials
     */
    public static List<MirrorCredentialsDto> findMirrorCredentials() {
        List<MirrorCredentialsDto> credsList = new ArrayList<MirrorCredentialsDto>();

        // Get the main pair of credentials
        String user = Config.get().getString(KEY_MIRRCREDS_USER);
        String password = Config.get().getString(KEY_MIRRCREDS_PASS);
        String email = Config.get().getString(KEY_MIRRCREDS_EMAIL);

        // Add credentials as long as they have user and password
        MirrorCredentialsDto creds;
        int id = 0;
        while (user != null && password != null) {
            if (log.isTraceEnabled()) {
                log.trace("Found credentials (" + id + "): " + user);
            }

            // Create credentials object
            creds = new MirrorCredentialsDto(email, user, password);
            creds.setId(new Long(id));
            credsList.add(creds);

            // Search additional credentials with continuous enumeration
            id++;
            String suffix = KEY_MIRRCREDS_SEPARATOR + id;
            user = Config.get().getString(KEY_MIRRCREDS_USER + suffix);
            password = Config.get().getString(KEY_MIRRCREDS_PASS + suffix);
            email = Config.get().getString(KEY_MIRRCREDS_EMAIL + suffix);
        }
        return credsList;
    }

    /**
     * Find mirror credentials for a given ID.
     * @param id the credentials ID
     * @return pair of credentials for given ID.
     */
    public static MirrorCredentialsDto findMirrorCredentials(long id) {
        // Generate suffix depending on the ID
        String suffix = "";
        if (id > 0) {
            suffix = KEY_MIRRCREDS_SEPARATOR + id;
        }

        // Get the credentials from config
        String user = Config.get().getString(KEY_MIRRCREDS_USER + suffix);
        String password = Config.get().getString(KEY_MIRRCREDS_PASS + suffix);
        String email = Config.get().getString(KEY_MIRRCREDS_EMAIL + suffix);
        MirrorCredentialsDto creds = null;
        if (user != null && password != null) {
            if (log.isDebugEnabled()) {
                log.debug("Found credentials for ID: " + id);
            }
            // Create credentials object
            creds = new MirrorCredentialsDto(email, user, password);
            creds.setId(id);
        }
        return creds;
    }

    /**
     * Store a given pair of credentials in the filesystem after editing or using the next
     * available free index for new credentials.
     * @param creds mirror credentials to store
     * @param userIn the current user
     * @param request the current HTTP request object, used for session caching
     * @return list of validation errors or null in case of success
     */
    public static ValidatorError[] storeMirrorCredentials(MirrorCredentialsDto creds,
            User userIn, HttpServletRequest request) {
        if (creds.getUser() == null || creds.getPassword() == null) {
            return null;
        }

        // Find the first free ID if necessary
        Long id = creds.getId();
        if (creds.getId() == null) {
            List<MirrorCredentialsDto> credentials =
                    MirrorCredentialsManager.findMirrorCredentials();
            id = new Long(credentials.size());
        }

        // Check if there is changes by looking at previous object
        MirrorCredentialsDto oldCreds = MirrorCredentialsManager.findMirrorCredentials(id);
        if (!creds.equals(oldCreds)) {
            // Generate suffix depending on the ID
            String suffix = "";
            if (id > 0) {
                suffix = KEY_MIRRCREDS_SEPARATOR + id;
            }
            ConfigureSatelliteCommand configCommand = new ConfigureSatelliteCommand(userIn);
            configCommand.updateString(KEY_MIRRCREDS_USER + suffix, creds.getUser());
            configCommand.updateString(KEY_MIRRCREDS_PASS + suffix, creds.getPassword());
            if (creds.getEmail() != null) {
                configCommand.updateString(KEY_MIRRCREDS_EMAIL + suffix, creds.getEmail());
            }
            // Remove old credentials data from cache
            if (oldCreds != null) {
                SetupWizardSessionCache.clearSubscriptions(oldCreds, request);
            }
            return configCommand.storeConfiguration();
        }
        else {
            // Nothing to do
            return null;
        }
    }

    /**
     * Delete a pair of credentials given by their ID. Includes some sophisticated logic
     * to shift IDs in case you delete a pair of credentials from the middle.
     * @param id the id of credentials being deleted
     * @param userIn the user currently logged in
     * @param request the current HTTP request object, used for session caching
     * @return list of validation errors or null in case of success
     */
    public static ValidatorError[] deleteMirrorCredentials(Long id, User userIn,
            HttpServletRequest request) {
        ValidatorError[] errors = null;

        // Store credentials to empty cache later
        MirrorCredentialsDto credentials = MirrorCredentialsManager.findMirrorCredentials(id);

        // Find all credentials and see what needs to be done
        List<MirrorCredentialsDto> creds = MirrorCredentialsManager.findMirrorCredentials();
        ConfigureSatelliteCommand configCommand = new ConfigureSatelliteCommand(userIn);

        for (MirrorCredentialsDto c : creds) {
            int index = creds.indexOf(c);
            // First skip all credentials to the left of the one that should be deleted
            if (index > id) {
                // Then shift to the left each
                String targetSuffix = "";
                if (index > 1) {
                    targetSuffix = KEY_MIRRCREDS_SEPARATOR + (index - 1);
                }
                configCommand.updateString(KEY_MIRRCREDS_USER + targetSuffix,
                        c.getUser());
                configCommand.updateString(KEY_MIRRCREDS_PASS + targetSuffix,
                        c.getPassword());
                if (c.getEmail() != null) {
                    configCommand.updateString(KEY_MIRRCREDS_EMAIL + targetSuffix,
                            c.getEmail());
                }
            }
        }

        // Delete the last credentials
        String suffix = "";
        if (creds.size() > 1) {
            suffix = KEY_MIRRCREDS_SEPARATOR + (creds.size() - 1);
        }
        configCommand.remove(KEY_MIRRCREDS_USER + suffix);
        configCommand.remove(KEY_MIRRCREDS_PASS + suffix);
        configCommand.remove(KEY_MIRRCREDS_EMAIL + suffix);

        // Store configuration and clean cache for deleted credentials
        errors = configCommand.storeConfiguration();
        SetupWizardSessionCache.clearSubscriptions(credentials, request);
        return errors;
    }

    /**
     * Make primary credentials for a given credentials ID.
     * Cache is not affected by reordering, because username is used as the key.
     * @param id the id of credentials being made primary
     * @param userIn the current user
     * @param request the current HTTP request
     * @return list of validation errors or null in case of success
     */
    public static ValidatorError[] makePrimaryCredentials(Long id, User userIn,
            HttpServletRequest request) {
        ValidatorError[] errors = null;
        List<MirrorCredentialsDto> allCreds = MirrorCredentialsManager.findMirrorCredentials();
        if (allCreds.size() > 1) {
            // Find the future primary creds before reordering
            MirrorCredentialsDto primaryCreds =
                    MirrorCredentialsManager.findMirrorCredentials(id);
            ConfigureSatelliteCommand configCommand = new ConfigureSatelliteCommand(userIn);

            // Shift all indices starting from 1
            int i = 1;
            for (MirrorCredentialsDto c : allCreds) {
                if (allCreds.indexOf(c) != id) {
                    String targetSuffix = KEY_MIRRCREDS_SEPARATOR + i;
                    configCommand.updateString(KEY_MIRRCREDS_USER + targetSuffix,
                            c.getUser());
                    configCommand.updateString(KEY_MIRRCREDS_PASS + targetSuffix,
                            c.getPassword());
                    if (c.getEmail() != null) {
                        configCommand.updateString(KEY_MIRRCREDS_EMAIL + targetSuffix,
                                c.getEmail());
                    }
                    i++;
                }
            }

            // Set the primary credentials and store
            primaryCreds.setId(0L);
            configCommand.updateString(KEY_MIRRCREDS_USER, primaryCreds.getUser());
            configCommand.updateString(KEY_MIRRCREDS_PASS, primaryCreds.getPassword());
            if (primaryCreds.getEmail() != null) {
                configCommand.updateString(KEY_MIRRCREDS_EMAIL, primaryCreds.getEmail());
            }
            errors = configCommand.storeConfiguration();
        }
        return errors;
    }

    /**
     * Connect to NCC and return subscriptions for a given pair of credentials.
     * @param creds the mirror credentials to use
     * @return list of subscriptions available via the given credentials
     */
    public static List<Subscription> downloadSubscriptions(MirrorCredentialsDto creds) {
        List<Subscription> subscriptions = null;
        NCCClient nccClient = new NCCClient();
        try {
            return nccClient.downloadSubscriptions(creds);
        }
        catch (NCCException e) {
            log.error(e.getMessage());
        }
        return subscriptions;
    }

    /**
     * Make DTOs from a given list of {@link Subscription} objects read from
     * NCC. While doing that, filter out only active subscriptions and get human
     * readable names from DB.
     * @param subscriptions
     * @return list of subscription DTOs
     */
    private static List<SubscriptionDto> makeDtos(List<Subscription> subscriptions) {
        if (subscriptions == null) {
            return null;
        }
        // Go through all of the given subscriptions
        List<SubscriptionDto> dtos = new ArrayList<SubscriptionDto>();
        for (Subscription sub : subscriptions) {
            if (sub.getSubstatus().equals("EXPIRED")) {
                continue;
            }

            // Determine subscription name from given product class
            String subName = null;
            String productClass = sub.getProductClass();

            // Check if there is a comma separated list of product classes
            if (productClass.indexOf(',') == -1) {
                subName = ChannelFamilyFactory.getNameByLabel(productClass);
                if (subName == null || subName.isEmpty()) {
                    log.warn("Empty name for: " + productClass);
                    subName = productClass;
                }
            }
            else {
                if (log.isDebugEnabled()) {
                    log.debug("List of product classes: " + productClass);
                }
                List<String> productClasses = Arrays.asList(productClass.split(","));
                for (String s : productClasses) {
                    String name = ChannelFamilyFactory.getNameByLabel(s);
                    if (name == null || name.isEmpty()) {
                        log.warn("Empty name for: " + s);
                        name = s;
                    }

                    // It is an OR relationship: append with OR
                    if (subName == null) {
                        subName = name;
                    }
                    else {
                        subName = subName + " OR " + name;
                    }
                }
            }

            // We have a valid subscription, add it as DTO
            SubscriptionDto dto = new SubscriptionDto();
            dto.setName(subName);
            dto.setStartDate(sub.getStartDate());
            dto.setEndDate(sub.getEndDate());
            dtos.add(dto);
        }
        return dtos;
    }

    /**
     * Return cached list of subscriptions or "null" for signaling "verification failed".
     * @param creds credentials
     * @param request request
     * @param forceRefresh set true to refresh the cached subscriptions
     * @return list of subscriptions or null signaling "verification failed"
     */
    public static List<SubscriptionDto> getSubscriptions(MirrorCredentialsDto creds,
            HttpServletRequest request, boolean forceRefresh) {
        // Implicitly download subscriptions if requested
        if (forceRefresh ||
                SetupWizardSessionCache.credentialsStatusUnknown(creds, request)) {
            if (log.isDebugEnabled()) {
                log.debug("Downloading subscriptions for " + creds.getUser());
            }
            List<Subscription> subscriptions =
                    MirrorCredentialsManager.downloadSubscriptions(creds);
            SetupWizardSessionCache.storeSubscriptions(
                    makeDtos(subscriptions), creds, request);
        }

        // Return from cache
        return SetupWizardSessionCache.getSubscriptions(creds, request);
    }
}
