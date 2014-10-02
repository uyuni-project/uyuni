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
import com.redhat.rhn.manager.content.ContentSyncException;
import com.redhat.rhn.manager.satellite.ConfigureSatelliteCommand;

import com.suse.manager.model.ncc.Subscription;

import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * Domain logic for the Setup Wizard mirror credentials page, the NCC version.
 */
public class NCCMirrorCredentialsManager extends MirrorCredentialsManager {

    /** Logger instance */
    private static Logger log = Logger.getLogger(NCCMirrorCredentialsManager.class);

    /** Configuration key prefix for mirror credential usernames */
    public static final String KEY_MIRRCREDS_USER = "server.susemanager.mirrcred_user";
    /** Configuration key prefix for mirror credential passwords */
    public static final String KEY_MIRRCREDS_PASS = "server.susemanager.mirrcred_pass";
    /** Configuration key prefix for mirror credential email addresses */
    public static final String KEY_MIRRCREDS_EMAIL = "server.susemanager.mirrcred_email";
    /** Configuration key separator for mirror credentials */
    public static final String KEY_MIRRCREDS_SEPARATOR = "_";

    // The command class to use
    private Class<? extends ConfigureSatelliteCommand> commandClass;

    /**
     * Default constructor.
     */
    protected NCCMirrorCredentialsManager() {
        this(ConfigureSatelliteCommand.class);
    }

    /**
     * Constructor accepting a command class, use directly for tests.
     * @param commandClassIn the config command class to use
     */
    public NCCMirrorCredentialsManager(
            Class<? extends ConfigureSatelliteCommand> commandClassIn) {
        commandClass = commandClassIn;
    }

    /**
     * Return the config command instance.
     * @return the config command instance
     */
    private ConfigureSatelliteCommand getConfigCommand(User user) {
        ConfigureSatelliteCommand cmd = null;
        try {
            Constructor<? extends ConfigureSatelliteCommand> constructor =
                    commandClass.getConstructor(User.class);
            cmd = (ConfigureSatelliteCommand) constructor.newInstance(user);
        }
        catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        catch (SecurityException e) {
            e.printStackTrace();
        }
        catch (InstantiationException e) {
            e.printStackTrace();
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return cmd;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MirrorCredentialsDto> findMirrorCredentials() {
        List<MirrorCredentialsDto> credsList = new ArrayList<MirrorCredentialsDto>();

        // Get the main pair of credentials
        String user = Config.get().getString(KEY_MIRRCREDS_USER);
        String password = Config.get().getString(KEY_MIRRCREDS_PASS);
        String email = Config.get().getString(KEY_MIRRCREDS_EMAIL);

        // Add credentials as long as they have user and password
        MirrorCredentialsDto creds;
        int id = 0;
        while (user != null && password != null) {
            if (log.isDebugEnabled()) {
                log.debug("Found credentials (" + id + "): " + user);
            }

            // Create credentials object
            creds = new MirrorCredentialsDto(email, user, password);
            creds.setId(new Long(id));
            if (id == 0) {
                creds.setPrimary(true);
            }
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
     * {@inheritDoc}
     */
    @Override
    public MirrorCredentialsDto findMirrorCredentials(long id) {
        // Return null in case there is no primary creds
        MirrorCredentialsDto creds = null;

        // Generate suffix depending on the ID
        String suffix = "";
        if (id > 0) {
            suffix = KEY_MIRRCREDS_SEPARATOR + id;
        }

        // Get the credentials from config
        String user = Config.get().getString(KEY_MIRRCREDS_USER + suffix);
        String password = Config.get().getString(KEY_MIRRCREDS_PASS + suffix);
        String email = Config.get().getString(KEY_MIRRCREDS_EMAIL + suffix);
        if (user != null && password != null) {
            if (log.isDebugEnabled()) {
                log.debug("Found credentials for ID: " + id);
            }
            // Create credentials object
            creds = new MirrorCredentialsDto(email, user, password);
            creds.setId(id);
            if (id == 0) {
                creds.setPrimary(true);
            }
        }
        return creds;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long storeMirrorCredentials(MirrorCredentialsDto creds, User userIn,
            HttpServletRequest request) throws ContentSyncException {
        if (creds.getUser() == null || creds.getPassword() == null) {
            throw new ContentSyncException("User or password is empty");
        }

        // Find the first free ID if necessary
        Long id = creds.getId();
        if (creds.getId() == null) {
            List<MirrorCredentialsDto> credentials = findMirrorCredentials();
            id = new Long(credentials.size());
        }

        // Check if there is changes by looking at previous object
        MirrorCredentialsDto oldCreds = findMirrorCredentials(id);
        if (!creds.equals(oldCreds)) {
            // Generate suffix depending on the ID
            String suffix = "";
            if (id > 0) {
                suffix = KEY_MIRRCREDS_SEPARATOR + id;
            }
            ConfigureSatelliteCommand configCommand = getConfigCommand(userIn);
            configCommand.updateString(KEY_MIRRCREDS_USER + suffix, creds.getUser());
            configCommand.updateString(KEY_MIRRCREDS_PASS + suffix, creds.getPassword());
            if (creds.getEmail() != null) {
                configCommand.updateString(KEY_MIRRCREDS_EMAIL + suffix, creds.getEmail());
            }
            // Remove old credentials data from cache
            if (oldCreds != null) {
                SetupWizardSessionCache.clearSubscriptions(oldCreds, request);
            }
            ValidatorError[] errors = configCommand.storeConfiguration();
            if (errors != null) {
                throw new ContentSyncException(errors[0].getLocalizedMessage());
            }
        }
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValidatorError[] deleteMirrorCredentials(Long id, User user,
            HttpServletRequest request) {
        // Store credentials to empty cache later
        MirrorCredentialsDto credentials = findMirrorCredentials(id);

        // Find all credentials and see what needs to be done
        List<MirrorCredentialsDto> creds = findMirrorCredentials();
        ConfigureSatelliteCommand configCommand = getConfigCommand(user);
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

        // Store configuration
        ValidatorError[] errors = configCommand.storeConfiguration();

        // Clear the cache for deleted credentials before returning
        if (request != null) {
            SetupWizardSessionCache.clearSubscriptions(credentials, request);
        }
        return errors;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValidatorError[] makePrimaryCredentials(Long id, User userIn,
            HttpServletRequest request) {
        ValidatorError[] errors = null;
        List<MirrorCredentialsDto> allCreds = findMirrorCredentials();
        if (allCreds.size() > 1) {
            // Find the future primary creds before reordering
            MirrorCredentialsDto primaryCreds = findMirrorCredentials(id);
            ConfigureSatelliteCommand configCommand = getConfigCommand(userIn);

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
     * {@inheritDoc}
     */
    @Override
    public List<SubscriptionDto> getSubscriptions(MirrorCredentialsDto creds,
            HttpServletRequest request, boolean forceRefresh) {
        // Implicitly download subscriptions if requested
        if (forceRefresh ||
                SetupWizardSessionCache.credentialsStatusUnknown(creds, request)) {
            if (log.isDebugEnabled()) {
                log.debug("Downloading subscriptions for " + creds.getUser());
            }
            List<Subscription> subscriptions = downloadSubscriptions(creds);
            SetupWizardSessionCache.storeSubscriptions(
                    makeDtos(subscriptions), creds, request);
        }

        // Return from cache
        return SetupWizardSessionCache.getSubscriptions(creds, request);
    }

    /**
     * Connect to NCC and return subscriptions for a given pair of credentials.
     *
     * @param creds the mirror credentials to use
     * @return list of subscriptions available via the given credentials
     */
    private List<Subscription> downloadSubscriptions(MirrorCredentialsDto creds) {
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
     * Make DTOs from a given list of {@link Subscription} objects read from NCC.
     * While doing that, filter out only active subscriptions and get human
     * readable names from DB.
     *
     * @param subscriptions list of subscriptions
     * @return list of subscription DTOs
     */
    private List<SubscriptionDto> makeDtos(List<Subscription> subscriptions) {
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
}
