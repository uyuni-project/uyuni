/*
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

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelFamilyFactory;
import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.SCCCredentials;
import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.domain.scc.SCCRegCacheItem;
import com.redhat.rhn.frontend.xmlrpc.sync.content.SCCContentSyncSource;
import com.redhat.rhn.manager.content.ContentSyncException;
import com.redhat.rhn.manager.content.ContentSyncManager;

import com.suse.cloud.CloudPaygManager;
import com.suse.scc.SCCSystemRegistrationManager;
import com.suse.scc.client.SCCClient;
import com.suse.scc.client.SCCConfig;
import com.suse.scc.client.SCCWebClient;
import com.suse.scc.model.SCCSubscriptionJson;

import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

/**
 * Manager class for working with mirror credentials.
 */
public class MirrorCredentialsManager {

    /** Logger instance */
    private static Logger log = LogManager.getLogger(MirrorCredentialsManager.class);

    private final CloudPaygManager cloudPaygManager;

    private final ContentSyncManager contentSyncManager;

    /**
     * Default Constructor
     */
    public MirrorCredentialsManager() {
        this(GlobalInstanceHolder.PAYG_MANAGER, new ContentSyncManager());
    }

    /**
     * Constructore
     * @param cloudPaygManagerIn the cloud manager
     * @param contentSyncManagerIn the content sync manager
     */
    public MirrorCredentialsManager(CloudPaygManager cloudPaygManagerIn, ContentSyncManager contentSyncManagerIn) {
        cloudPaygManager = cloudPaygManagerIn;
        contentSyncManager = contentSyncManagerIn;
    }

    /**
     * Find all currently available mirror credentials and return them.
     *
     * @return list of all available mirror credentials
     */
    public List<MirrorCredentialsDto> findMirrorCredentials() {
        List<MirrorCredentialsDto> credsList =
                CredentialsFactory.listSCCCredentials().stream()
                        .map(MirrorCredentialsDto::fromSCCCredentials)
                        .sorted(
                            // Put primary first then use the sort by id
                            Comparator.comparing(MirrorCredentialsDto::isPrimary).reversed()
                                .thenComparing(MirrorCredentialsDto::getId)
                        )
                        .collect(Collectors.toList());
        if (log.isDebugEnabled()) {
            log.debug("Found {} mirror credentials", credsList.size());
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
        return CredentialsFactory.lookupSCCCredentialsById(id).map(c -> {
            MirrorCredentialsDto creds =
                    new MirrorCredentialsDto(c.getUsername(), c.getPassword());
            creds.setId(c.getId());
            // We use the URL to identify primary
            if (c.getUrl() != null) {
                creds.setPrimary(true);
            }
            if (log.isDebugEnabled()) {
                log.debug("Found credentials ({}): {}", creds.getId(), creds.getUser());
            }
            return creds;
        }).orElseThrow(() -> new ContentSyncException("Credentials not found: " + id));
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
        for (SCCCredentials existingCred : CredentialsFactory.listSCCCredentials()) {
            if (existingCred.getUsername().equals(creds.getUser()) &&
                    (!Objects.equals(existingCred.getId(), creds.getId()))) {
                throw new MirrorCredentialsNotUniqueException("Username already exists");
            }
        }

        // Try to lookup the credentials first
        SCCCredentials c = Optional.ofNullable(creds.getId())
                .flatMap(id -> CredentialsFactory.lookupSCCCredentialsById(creds.getId()))
                .map(existing -> {
                    // We are editing existing credentials, clear the cache
                    MirrorCredentialsDto oldCreds = findMirrorCredentials(creds.getId());
                    SetupWizardSessionCache.clearSubscriptions(oldCreds, request);

                    existing.setUsername(creds.getUser());
                    existing.setPassword(creds.getPassword());

                    return existing;
                })
                .orElseGet(() -> CredentialsFactory.createSCCCredentials(creds.getUser(), creds.getPassword()));

        CredentialsFactory.storeCredentials(c);
        if (log.isDebugEnabled()) {
            log.debug("Stored credentials ({}): {}", c.getId(), c.getUsername());
        }

        // Make this the primary pair of credentials if it's the only one
        if (CredentialsFactory.listSCCCredentials().size() == 1) {
            makePrimaryCredentials(c.getId());
        }
        // update info about hasSCCCredentials
        cloudPaygManager.checkRefreshCache(true);
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
            List<SCCCredentials> credsList = CredentialsFactory.listSCCCredentials();
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
        log.debug("{} RegCacheItems found to force delete", itemList.size());
        try {
            URI url = new URI(Config.get().getString(ConfigDefaults.SCC_URL));
            String uuid = ContentSyncManager.getUUID();
            SCCConfig sccConfig = new SCCConfig(url, "", "", uuid);
            SCCClient sccClient = new SCCWebClient(sccConfig);
            SCCSystemRegistrationManager sccRegManager = new SCCSystemRegistrationManager(sccClient);
            sccRegManager.deregister(itemList, true);
        }
        catch (URISyntaxException e) {
            log.error("Invalid SCC URL configured.", e);
        }

        // Clear Repository Authentications
        SCCCachingFactory.lookupRepositoryAuthByCredential(dbCreds)
                .forEach(SCCCachingFactory::deleteRepositoryAuth);

        // Clear the cache for deleted credentials
        if (request != null) {
            SetupWizardSessionCache.clearSubscriptions(credentials, request);
        }

        // Delete from database
        CredentialsFactory.removeCredentials(dbCreds);

        // Link orphan content sources
        contentSyncManager.linkAndRefreshContentSource(null);

        // update info about hasSCCCredentials
        cloudPaygManager.checkRefreshCache(true);
    }

    /**
     * Make primary credentials for a given credentials ID.
     *
     * @param id the id of credentials to make the primary creds
     * @throws ContentSyncException in case the credentials cannot be found
     */
    public void makePrimaryCredentials(Long id) throws ContentSyncException {
        if (CredentialsFactory.lookupSCCCredentialsById(id).isEmpty()) {
            throw new ContentSyncException("Credentials not found: " + id);
        }

        for (MirrorCredentialsDto c : findMirrorCredentials()) {
            CredentialsFactory.lookupSCCCredentialsById(c.getId()).ifPresent(dbCreds -> {
                if (dbCreds.getId().equals(id)) {
                    dbCreds.setUrl(Config.get().getString(ConfigDefaults.SCC_URL));
                    CredentialsFactory.storeCredentials(dbCreds);
                }
                else if (dbCreds.getUrl() != null) {
                    dbCreds.setUrl(null);
                    CredentialsFactory.storeCredentials(dbCreds);
                }
            });
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
        if (forceRefresh || SetupWizardSessionCache.credentialsStatusUnknown(creds, request)) {
            if (log.isDebugEnabled()) {
                log.debug("Downloading subscriptions for {}", creds.getUser());
            }
            try {
                CredentialsFactory.lookupSCCCredentialsById(creds.getId())
                    .ifPresent(credentials -> {
                        SCCContentSyncSource source = new SCCContentSyncSource(credentials);
                        List<SCCSubscriptionJson> subscriptions = contentSyncManager.updateSubscriptions(source);
                        SetupWizardSessionCache.storeSubscriptions(makeDtos(subscriptions), creds, request);
                    });
            }
            catch (ContentSyncException e) {
                log.error("Error getting subscriptions for {}: {}", creds.getUser(), e.getMessage());
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
        List<SubscriptionDto> dtos = new ArrayList<>();
        if (CollectionUtils.isEmpty(subscriptions)) {
            return dtos;
        }

        Map<String, String> familyNameByLabel = ChannelFamilyFactory.getAllChannelFamilies()
            .stream().collect(Collectors.toMap(ChannelFamily::getLabel, ChannelFamily::getName));

        subscriptions.stream()
            // Only active subscriptions
            .filter(s -> "ACTIVE".equals(s.getStatus()))
            // Only subscription with at least one product class
            .filter(s -> {
                if (CollectionUtils.isEmpty(s.getProductClasses())) {
                    log.warn("No product class for subscription: {}, skipping...", s.getName());
                    return false;
                }

                return true;
            })
            // Convert the SCCSubscriptionJSON to a SubscriptionDTO
            .map(s -> {
                StringBuilder subscriptionNameBuilder = new StringBuilder();
                for (String productClass : s.getProductClasses()) {
                    String name = familyNameByLabel.getOrDefault(productClass, productClass);

                    // It is an OR relationship: append with OR
                    if (subscriptionNameBuilder.length() > 0) {
                        subscriptionNameBuilder.append(" OR ");
                    }

                    subscriptionNameBuilder.append(name);
                }

                return new SubscriptionDto(subscriptionNameBuilder.toString(), s.getStartsAt(), s.getExpiresAt());
            })
            .forEach(dtos::add);

        return dtos;
    }
}
