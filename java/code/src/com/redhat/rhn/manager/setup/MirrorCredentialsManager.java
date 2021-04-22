/**
 * Copyright (c) 2014 SUSE LLC
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
import com.redhat.rhn.domain.channel.ChannelFamilyFactory;
import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.domain.scc.SCCRegCacheItem;
import com.redhat.rhn.manager.content.ContentSyncException;
import com.redhat.rhn.manager.content.ContentSyncManager;

import com.redhat.rhn.taskomatic.SCCSystemRegistry;
import com.suse.scc.client.SCCClientException;
import com.suse.scc.model.SCCSubscriptionJson;

import org.apache.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

/**
 * Manager class for working with mirror credentials.
 */
public class MirrorCredentialsManager {

    /** Logger instance */
    private static Logger log = Logger.getLogger(MirrorCredentialsManager.class);

    /**
     * Find all currently available mirror credentials and return them.
     *
     * @return list of all available mirror credentials
     */
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
     * Find mirror credentials for a given ID.
     *
     * @param id of the credentials to find
     * @return credentials for given ID
     */
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
     * Store mirror credentials given as {@link MirrorCredentialsDto}.
     *
     * @param creds the mirror credentials to store
     * @param request the current HTTP request object (used for session caching)
     * @return id of the stored mirror credentials
     * @throws ContentSyncException in case of errors
     */
    public long storeMirrorCredentials(MirrorCredentialsDto creds,
            HttpServletRequest request) throws ContentSyncException {
        if (creds.getUser() == null || creds.getPassword() == null) {
            throw new ContentSyncException("User or password is empty");
        }
        // Check if the supplied user name already exists in stored credentials
        for (Credentials existingCred : CredentialsFactory.lookupSCCCredentials()) {
            if (existingCred.getUsername().equals(creds.getUser()) &&
                    (creds.getId() != existingCred.getId())) {
                throw new MirrorCredentialsNotUniqueException("Username already exists");
            }
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
            makePrimaryCredentials(c.getId());
        }
        return c.getId();
    }

    /**
     * Delete credentials given by ID.
     *
     * @param id the id of credentials to be deleted
     * @param request the current HTTP request object (used for session caching)
     * @throws ContentSyncException in case of problems making new primary creds
     */
    public void deleteMirrorCredentials(Long id, HttpServletRequest request)
            throws ContentSyncException {
        // Store credentials to empty cache later
        MirrorCredentialsDto credentials = findMirrorCredentials(id);

        Credentials dbCreds = CredentialsFactory.lookupCredentialsById(id);

        // Make new primary credentials if necessary
        if (credentials.isPrimary()) {
            List<Credentials> credsList = CredentialsFactory.lookupSCCCredentials();
            if (credsList != null && !credsList.isEmpty()) {
                credsList.stream().filter(c -> !c.equals(dbCreds)).findFirst()
                    .ifPresent(c -> {
                        try {
                            makePrimaryCredentials(c.getId());
                        }
                        catch (ContentSyncException e) {
                            log.error("Invalid Credential");
                        }
                    });
            }
        }

        // Check for systems registered under this credentials and start delete requests
        List<SCCRegCacheItem> itemList = SCCCachingFactory.listRegItemsByCredentials(dbCreds);
        log.debug(itemList.size() + " RegCacheItems found to force delete");
        try {
            URI url = new URI(Config.get().getString(ConfigDefaults.SCC_URL));
            String uuid = ContentSyncManager.getUUID();
            SCCSystemRegistry sccSystemRegistry = new SCCSystemRegistry(url, uuid);
            sccSystemRegistry.deregister(itemList, true);
        }
        catch (URISyntaxException e) {
            log.error("Invalid SCC URL configured.", e);
        }

        // Clear Repository Authentications
        SCCCachingFactory.lookupRepositoryAuthByCredential(dbCreds).stream().forEach(a -> {
            SCCCachingFactory.deleteRepositoryAuth(a);
        });

        // Clear the cache for deleted credentials
        if (request != null) {
            SetupWizardSessionCache.clearSubscriptions(credentials, request);
        }

        // Delete from database
        CredentialsFactory.removeCredentials(dbCreds);

        // Link orphan content sources
        ContentSyncManager csm = new ContentSyncManager();
        csm.linkAndRefreshContentSource(null);
    }

    /**
     * Make primary credentials for a given credentials ID.
     *
     * @param id the id of credentials to make the primary creds
     * @throws ContentSyncException in case the credentials cannot be found
     */
    public void makePrimaryCredentials(Long id) throws ContentSyncException {
        if (CredentialsFactory.lookupCredentialsById(id) == null) {
            throw new ContentSyncException("Credentials not found: " + id);
        }

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

    /**
     * Return cached list of subscriptions or "null" for signaling "verification failed".
     *
     * @param creds the credentials
     * @param request the current HTTP request
     * @param forceRefresh set true to refresh the cached subscriptions
     * @return list of subscriptions or null for "verification failed"
     */
    public List<SubscriptionDto> getSubscriptions(MirrorCredentialsDto creds,
            HttpServletRequest request, boolean forceRefresh) {
        // Implicitly download subscriptions if requested
        if (forceRefresh ||
                SetupWizardSessionCache.credentialsStatusUnknown(creds, request)) {
            if (log.isDebugEnabled()) {
                log.debug("Downloading subscriptions for " + creds.getUser());
            }
            try {
                Credentials credentials =
                        CredentialsFactory.lookupCredentialsById(creds.getId());
                List<SCCSubscriptionJson> subscriptions = new ContentSyncManager().
                        updateSubscriptions(credentials);
                SetupWizardSessionCache.storeSubscriptions(
                        makeDtos(subscriptions), creds, request);
            }
            catch (SCCClientException e) {
                log.error("Error getting subscriptions for " +
                        creds.getUser() + ": " + e.getMessage());
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
    private List<SubscriptionDto> makeDtos(List<SCCSubscriptionJson> subscriptions) {
        if (subscriptions == null) {
            return null;
        }
        Map<String, String> familyNameByLabel = ChannelFamilyFactory.getAllChannelFamilies()
                .stream().collect(Collectors.toMap(cf -> cf.getLabel(), cf -> cf.getName()));
        // Go through all of the given subscriptions
        List<SubscriptionDto> dtos = new ArrayList<SubscriptionDto>();
        for (SCCSubscriptionJson s : subscriptions) {
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
                String name = Optional.ofNullable(familyNameByLabel.get(productClass)).orElse(productClass);

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
