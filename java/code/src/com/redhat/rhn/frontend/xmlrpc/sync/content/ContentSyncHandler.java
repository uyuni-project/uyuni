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

package com.redhat.rhn.frontend.xmlrpc.sync.content;

import com.redhat.rhn.common.util.FileLocks;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.SCCCredentials;
import com.redhat.rhn.domain.product.MgrSyncChannelDto;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.product.SUSEProductSCCRepository;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.manager.content.ContentSyncException;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.manager.content.MgrSyncProductDto;
import com.redhat.rhn.manager.setup.MirrorCredentialsDto;
import com.redhat.rhn.manager.setup.MirrorCredentialsManager;

import com.suse.manager.api.ReadOnly;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * API handler offering content synchronization methods.
 *
 * @apidoc.namespace sync.content
 * @apidoc.doc Provides the namespace for the content synchronization methods.
 */
public class ContentSyncHandler extends BaseHandler {

    /**
     * List all accessible products.
     *
     * @param loggedInUser the currently logged in user
     * @return List of products with their extensions (add-ons).
     * @throws ContentSyncException in case of an error
     *
     * @apidoc.doc List all accessible products.
     * @apidoc.param #session_key()
     * @apidoc.returntype #return_array_begin()
     *                       $MgrSyncProductDtoSerializer
     *                    #array_end()
     */
    @ReadOnly
    public Collection<MgrSyncProductDto> listProducts(User loggedInUser)
            throws ContentSyncException {
        ensureSatAdmin(loggedInUser);
        ContentSyncManager csm = new ContentSyncManager();
        return csm.listProducts();
    }

    /**
     * List all accessible channels.
     *
     * @param loggedInUser the currently logged in user
     * @return List of channels.
     * @throws ContentSyncException in case of an error
     *
     * @apidoc.doc List all accessible channels.
     * @apidoc.param #session_key()
     * @apidoc.returntype #return_array_begin()
     *                       $MgrSyncChannelDtoSerializer
     *                    #array_end()
     */
    @ReadOnly
    public List<MgrSyncChannelDto> listChannels(User loggedInUser) {
        ensureSatAdmin(loggedInUser);
        ContentSyncManager csm = new ContentSyncManager();
        return csm.listChannels();
    }

    /**
     * @Deprecated
     * Synchronize channels between the Customer Center and the #product() database.
     * This method is one step of the whole refresh cycle.
     *
     * @param loggedInUser the currently logged in user
     * @param mirrorUrl optional mirror URL
     * @return Integer
     * @throws ContentSyncException in case of an error
     *
     * @apidoc.doc (Deprecated) Synchronize channels between the Customer Center
     *             and the #product() database.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "mirrorUrl", "Sync from mirror temporarily")
     * @apidoc.returntype #return_int_success()
     */
    public Integer synchronizeChannels(User loggedInUser, String mirrorUrl)
            throws ContentSyncException {
        return 1;
    }

    /**
     * Synchronize channel families between the Customer Center
     * and the #product() database.
     * This method is one step of the whole refresh cycle.
     *
     * @param loggedInUser the currently logged in user
     * @return Integer
     * @throws ContentSyncException in case of an error
     *
     * @apidoc.doc Synchronize channel families between the Customer Center
     *             and the #product() database.
     * @apidoc.param #session_key()
     * @apidoc.returntype #return_int_success()
     */
    public Integer synchronizeChannelFamilies(User loggedInUser)
            throws ContentSyncException {
        ensureSatAdmin(loggedInUser);
        FileLocks.SCC_REFRESH_LOCK.withFileLock(() -> {
                ContentSyncManager csm = new ContentSyncManager();
                csm.updateChannelFamilies(csm.readChannelFamilies());
        });
        return BaseHandler.VALID;
    }

    /**
     * Synchronize SUSE products between the Customer Center and the #product() database.
     * This method is one step of the whole refresh cycle.
     *
     * @param loggedInUser the currently logged in user
     * @return Integer
     * @throws ContentSyncException in case of an error
     *
     * @apidoc.doc Synchronize SUSE products between the Customer Center
     *             and the #product() database.
     * @apidoc.param #session_key()
     * @apidoc.returntype #return_int_success()
     */
    public Integer synchronizeProducts(User loggedInUser) throws ContentSyncException {
        ensureSatAdmin(loggedInUser);
        FileLocks.SCC_REFRESH_LOCK.withFileLock(() -> {
            ContentSyncManager csm = new ContentSyncManager();
            csm.updateSUSEProducts(csm.getProducts());
        });
        return BaseHandler.VALID;
    }

    /**
     * Synchronize subscriptions between the Customer Center
     * and the #product() database.
     * This method is one step of the whole refresh cycle.
     *
     * @param loggedInUser the currently logged in user
     * @return Integer
     * @throws ContentSyncException in case of an error
     *
     * @apidoc.doc Synchronize subscriptions between the Customer Center
     *             and the #product() database.
     * @apidoc.param #session_key()
     * @apidoc.returntype #return_int_success()
     */
    public Integer synchronizeSubscriptions(User loggedInUser) throws ContentSyncException {
        ensureSatAdmin(loggedInUser);
        FileLocks.SCC_REFRESH_LOCK.withFileLock(() -> {
            ContentSyncManager csm = new ContentSyncManager();
            csm.updateSubscriptions();
        });
        return BaseHandler.VALID;
    }

    /**
     * Synchronize Repositories between the Customer Center
     * and the #product() database.
     * This method is one step of the whole refresh cycle.
     *
     * @param loggedInUser the currently logged in user
     * @param mirrorUrl optional mirror url
     * @return Integer
     * @throws ContentSyncException in case of an error
     *
     * @apidoc.doc Synchronize repositories between the Customer Center
     *             and the #product() database.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "mirrorUrl", "Optional mirror url or null")
     * @apidoc.returntype #return_int_success()
     */
    public Integer synchronizeRepositories(User loggedInUser, String mirrorUrl) throws ContentSyncException {
        ensureSatAdmin(loggedInUser);
        FileLocks.SCC_REFRESH_LOCK.withFileLock(() -> {
            ContentSyncManager csm = new ContentSyncManager();
            csm.updateRepositories(mirrorUrl);
        });
        return BaseHandler.VALID;
    }

    /**
     * Add a new channel to the #product() database.
     *
     * @param loggedInUser the currently logged in user
     * @param channelLabel label of the channel to add
     * @param mirrorUrl optional mirror URL
     * @return Integer
     * @throws ContentSyncException in case of an error
     *
     * @apidoc.doc Add a new channel to the #product() database
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelLabel", "Label of the channel to add")
     * @apidoc.param #param_desc("string", "mirrorUrl", "Sync from mirror temporarily")
     * @apidoc.returntype #return_int_success()
     */
    public Integer addChannel(User loggedInUser, String channelLabel, String mirrorUrl)
            throws ContentSyncException {
        ensureSatAdmin(loggedInUser);
        ContentSyncManager csm = new ContentSyncManager();
        if (csm.isRefreshNeeded(mirrorUrl)) {
            throw new ContentSyncException("Product Data refresh needed. Please call mgr-sync refresh.");
        }
        csm.addChannel(channelLabel, mirrorUrl);
        return BaseHandler.VALID;
    }

    /**
     * Add a new channel to the #product() database and its required bases.
     *
     * @param loggedInUser the currently logged in user
     * @param channelLabel label of the channel to add
     * @param mirrorUrl optional mirror URL
     * @return Array of enabled channel labels
     * @throws ContentSyncException in case of an error
     *
     * @apidoc.doc Add a new channel to the #product() database
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelLabel", "Label of the channel to add")
     * @apidoc.param #param_desc("string", "mirrorUrl", "Sync from mirror temporarily")
     * @apidoc.returntype #array_single("string", "enabled channel labels")
     */
    public Object[] addChannels(User loggedInUser, String channelLabel, String mirrorUrl)
            throws ContentSyncException {
        ensureSatAdmin(loggedInUser);
        ContentSyncManager csm = new ContentSyncManager();
        if (csm.isRefreshNeeded(mirrorUrl)) {
            throw new ContentSyncException("Product Data refresh needed. Please call mgr-sync refresh.");
        }

        List<String> mandatoryChannelLabels =
                SUSEProductFactory.findNotSyncedMandatoryChannels(channelLabel)
                .map(SUSEProductSCCRepository::getChannelLabel)
                .collect(Collectors.toList());

        LinkedHashSet<String> channelLabelsToAdd = new LinkedHashSet<>(mandatoryChannelLabels);
        channelLabelsToAdd.add(channelLabel);

        for (String channel : channelLabelsToAdd) {
            csm.addChannel(channel, mirrorUrl);
        }

        List<String> returnList = new ArrayList<>();
        returnList.add(channelLabel);
        returnList.addAll(mandatoryChannelLabels);
        return returnList.toArray();
    }

    /**
     * Add organization credentials (mirror credentials) to #product().
     *
     * @param loggedInUser the currently logged in user
     * @param username organization credentials (mirror credentials) username
     * @param password organization credentials (mirror credentials) password
     * @param primary make this the primary credentials
     * @return Integer
     * @throws ContentSyncException in case of an error
     *
     * @apidoc.doc Add organization credentials (mirror credentials) to #product().
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "username", "Organization credentials
     *                                                  (Mirror credentials) username")
     * @apidoc.param #param_desc("string", "password", "Organization credentials
     *                                                  (Mirror credentials) password")
     * @apidoc.param #param_desc("boolean", "primary", "Make this the primary credentials")
     * @apidoc.returntype #return_int_success()
     */
    public Integer addCredentials(User loggedInUser, String username, String password,
            boolean primary) throws ContentSyncException {
        ensureSatAdmin(loggedInUser);
        MirrorCredentialsDto creds = new MirrorCredentialsDto(username, password);
        MirrorCredentialsManager credsManager = new MirrorCredentialsManager();
        long id = credsManager.storeMirrorCredentials(creds, null);
        if (primary) {
            credsManager.makePrimaryCredentials(id);
        }
        return BaseHandler.VALID;
    }

    /**
     * Delete organization credentials (mirror credentials) from #product().
     *
     * @param loggedInUser the currently logged in user
     * @param username username of the credentials to be deleted
     * @return Integer
     * @throws ContentSyncException in case of an error
     *
     * @apidoc.doc Delete organization credentials (mirror credentials) from #product().
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "username", "Username of credentials to delete")
     * @apidoc.returntype #return_int_success()
     */
    public Integer deleteCredentials(User loggedInUser, String username)
            throws ContentSyncException {
        ensureSatAdmin(loggedInUser);
        for (SCCCredentials c : CredentialsFactory.listSCCCredentials()) {
            if (c.getUsername().equals(username)) {
                new MirrorCredentialsManager().deleteMirrorCredentials(c.getId(), null);
                break;
            }
        }
        return BaseHandler.VALID;
    }

    /**
     * List organization credentials (mirror credentials) available in #product().
     *
     * @param loggedInUser the currently logged in user
     * @return List of organization credentials (mirror credentials)
     * @throws ContentSyncException in case of an error
     *
     * @apidoc.doc List organization credentials (mirror credentials) available in
     *             #product().
     * @apidoc.param #session_key()
     * @apidoc.returntype #return_array_begin()
     *                       $MirrorCredentialsDtoSerializer
     *                    #array_end()
     */
    @ReadOnly
    public List<MirrorCredentialsDto> listCredentials(User loggedInUser)
            throws ContentSyncException {
        ensureSatAdmin(loggedInUser);
        return new MirrorCredentialsManager().findMirrorCredentials();
    }
}
