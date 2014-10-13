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
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.channel.ChannelFamilyFactory;
import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.content.ContentSyncException;
import com.redhat.rhn.manager.content.ContentSyncManager;

import com.suse.scc.client.SCCClientException;
import com.suse.scc.model.SCCSubscription;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * Domain logic for the Setup Wizard mirror credentials page, the SCC version.
 */
public class SCCMirrorCredentialsManager extends MirrorCredentialsManager {

    /** Logger instance */
    private static Logger log = Logger.getLogger(SCCMirrorCredentialsManager.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MirrorCredentialsDto> findMirrorCredentials() {
        List<MirrorCredentialsDto> credsList = new ArrayList<MirrorCredentialsDto>();
        for (Credentials c : CredentialsFactory.lookupSCCCredentials()) {
            MirrorCredentialsDto creds = new MirrorCredentialsDto(
                    c.getUsername(), c.getPassword());
            creds.setId(c.getId());
            if (c.getUrl() != null) {
                creds.setPrimary(true);
            }
            credsList.add(creds);
        }
        if (log.isDebugEnabled()) {
            log.debug("Found " + credsList.size() + " mirror credentials");
        }
        return credsList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MirrorCredentialsDto findMirrorCredentials(long id) {
        Credentials c = CredentialsFactory.lookupCredentialsById(id);
        MirrorCredentialsDto creds = 
                new MirrorCredentialsDto(c.getUsername(), c.getPassword());
        creds.setId(c.getId());
        // We use the URL to identify primary
        if (c.getUrl() != null) {
            creds.setPrimary(true);
        }
        if (log.isDebugEnabled()) {
            log.debug("Found credentials (" + creds.getId() + "): " + creds.getUser());
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

        // Try to lookup the credentials first
        Credentials c = null;
        if (creds.getId() != null) {
            c = CredentialsFactory.lookupCredentialsById(creds.getId());
        }
        if (c == null) {
            c = CredentialsFactory.createSCCCredentials();
        }
        else {
            // We are editing existing credentials, clear the cache
            MirrorCredentialsDto oldCreds = findMirrorCredentials(creds.getId());
            SetupWizardSessionCache.clearSubscriptions(oldCreds, request);
        }
        c.setUsername(creds.getUser());
        c.setPassword(creds.getPassword());
        CredentialsFactory.storeCredentials(c);
        if (log.isDebugEnabled()) {
            log.debug("Stored credentials (" + c.getId() + "): " + c.getUsername());
        }

        // Make this the primary pair of credentials if it's the only one
        if (CredentialsFactory.lookupSCCCredentials().size() == 1) {
            makePrimaryCredentials(c.getId(), userIn, request);
        }
        return c.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValidatorError[] deleteMirrorCredentials(Long id, User user,
            HttpServletRequest request) {
        // Store credentials to empty cache later
        MirrorCredentialsDto credentials = findMirrorCredentials(id);

        // Delete from database
        Credentials dbCreds = CredentialsFactory.lookupCredentialsById(id);
        CredentialsFactory.removeCredentials(dbCreds);

        // Make new primary credentials if necessary
        if (credentials.isPrimary()) {
            List<Credentials> credsList = CredentialsFactory.lookupSCCCredentials();
            if (credsList != null && !credsList.isEmpty()) {
                makePrimaryCredentials(credsList.get(0).getId(), user, request);
            }
        }

        // Clear the cache for deleted credentials
        if (request != null) {
            SetupWizardSessionCache.clearSubscriptions(credentials, request);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValidatorError[] makePrimaryCredentials(Long id, User userIn,
            HttpServletRequest request) {
        ValidatorError[] errors = null;

        // Check if future primary credentials exist
        if (CredentialsFactory.lookupCredentialsById(id) != null) {
            for (MirrorCredentialsDto c : findMirrorCredentials()) {
                Credentials dbCreds = CredentialsFactory.lookupCredentialsById(c.getId());
                if (dbCreds.getId().equals(id)) {
                    dbCreds.setUrl(Config.get().getString(ConfigDefaults.SCC_URL));
                    CredentialsFactory.storeCredentials(dbCreds);
                }
                else if (dbCreds.getUrl() != null) {
                    dbCreds.setUrl(null);
                    CredentialsFactory.storeCredentials(dbCreds);
                }
            }
        }
        else {
            // FIXME: We should really return something else from here
            errors = new ValidatorError[1];
            errors[0] = new ValidatorError("config.storeconfig.error",
                    Integer.toString(-1));
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
            try {
                List<SCCSubscription> subscriptions = new ContentSyncManager().
                        getSubscriptions(creds.getUser(), creds.getPassword());
                SetupWizardSessionCache.storeSubscriptions(
                        makeDtos(subscriptions), creds, request);
            } catch (SCCClientException e) {
                log.error("Error getting subscriptions for " +
                        creds.getUser() + ", " + e.getMessage());
            }
        }

        // Return from cache
        return SetupWizardSessionCache.getSubscriptions(creds, request);
    }

    /**
     * Create a list of {@link SubscriptionDto} objects from a given list of subscriptions
     * as parsed from SCC.
     *
     * @param subscriptions SCC subscriptions
     * @return list of subscription DTOs
     */
    private List<SubscriptionDto> makeDtos(List<SCCSubscription> subscriptions) {
        if (subscriptions == null) {
            return null;
        }
        // Go through all of the given subscriptions
        List<SubscriptionDto> dtos = new ArrayList<SubscriptionDto>();
        for (SCCSubscription s : subscriptions) {
            // Skip all non-active
            if (!s.getStatus().equals("ACTIVE")) {
                continue;
            }

            // Determine subscription name from given product class
            List<String> productClasses = s.getProductClasses();
            if (productClasses.isEmpty()) {
                log.warn("No product class for subscription: " +
                        s.getName() + ", skipping...");
                continue;
            }
            String subscriptionName = null;
            for (String productClass : productClasses) {
                String name = ChannelFamilyFactory.getNameByLabel(productClass);
                if (StringUtils.isBlank(name)) {
                    log.warn("Empty name for: " + productClass);
                    name = productClass;
                }

                // It is an OR relationship: append with OR
                if (subscriptionName == null) {
                    subscriptionName = name;
                }
                else {
                    subscriptionName = subscriptionName + " OR " + name;
                }
            }

            // We have a valid subscription, add it as DTO
            SubscriptionDto dto = new SubscriptionDto();
            dto.setName(subscriptionName);
            dto.setStartDate(s.getStartsAt());
            dto.setEndDate(s.getExpiresAt());
            dtos.add(dto);
        }
        return dtos;
    }
}
