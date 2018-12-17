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

package com.redhat.rhn.frontend.xmlrpc.sync.content;

import com.redhat.rhn.common.util.SCCRefreshLock;
import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.product.MgrSyncChannelDto;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.manager.content.ContentSyncException;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.manager.content.MgrSyncProductDto;
import com.redhat.rhn.manager.setup.MirrorCredentialsDto;
import com.redhat.rhn.manager.setup.MirrorCredentialsManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * API handler offering content synchronization methods.
 *
 * @xmlrpc.namespace sync.content
 * @xmlrpc.doc Provides the namespace for the content synchronization methods.
 */
public class ContentSyncHandler extends BaseHandler {

    /**
     * List all accessible products.
     *
     * @param loggedInUser the currently logged in user
     * @return List of products with their extensions (add-ons).
     * @throws ContentSyncException in case of an error
     *
     * @xmlrpc.doc List all accessible products.
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.returntype #array()
     *                       $MgrSyncProductDtoSerializer
     *                    #array_end()
     */
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
     * @xmlrpc.doc List all accessible channels.
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.returntype #array()
     *                       $MgrSyncChannelDtoSerializer
     *                    #array_end()
     */
    public List<MgrSyncChannelDto> listChannels(User loggedInUser) {
        ensureSatAdmin(loggedInUser);
        ContentSyncManager csm = new ContentSyncManager();
        return csm.listChannels();
    }

    /**
     * @Deprecated
     * Synchronize channels between the Customer Center and the SUSE Manager database.
     * This method is one step of the whole refresh cycle.
     *
     * @param loggedInUser the currently logged in user
     * @param mirrorUrl optional mirror URL
     * @return Integer
     * @throws ContentSyncException in case of an error
     *
     * @xmlrpc.doc (Deprecated) Synchronize channels between the Customer Center
     *             and the SUSE Manager database.
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.param #param_desc("string", "mirrorUrl", "Sync from mirror temporarily")
     * @xmlrpc.returntype #return_int_success()
     */
    public Integer synchronizeChannels(User loggedInUser, String mirrorUrl)
            throws ContentSyncException {
        return 1;
    }

    /**
     * Synchronize channel families between the Customer Center
     * and the SUSE Manager database.
     * This method is one step of the whole refresh cycle.
     *
     * @param loggedInUser the currently logged in user
     * @return Integer
     * @throws ContentSyncException in case of an error
     *
     * @xmlrpc.doc Synchronize channel families between the Customer Center
     *             and the SUSE Manager database.
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.returntype #return_int_success()
     */
    public Integer synchronizeChannelFamilies(User loggedInUser)
            throws ContentSyncException {
        ensureSatAdmin(loggedInUser);
        SCCRefreshLock.tryGetLock();
        try {
            ContentSyncManager csm = new ContentSyncManager();
            csm.updateChannelFamilies(csm.readChannelFamilies());
        }
        finally {
            SCCRefreshLock.unlockFile();
        }
        return BaseHandler.VALID;
    }

    /**
     * Synchronize SUSE products between the Customer Center and the SUSE Manager database.
     * This method is one step of the whole refresh cycle.
     *
     * @param loggedInUser the currently logged in user
     * @return Integer
     * @throws ContentSyncException in case of an error
     *
     * @xmlrpc.doc Synchronize SUSE products between the Customer Center
     *             and the SUSE Manager database.
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.returntype #return_int_success()
     */
    public Integer synchronizeProducts(User loggedInUser) throws ContentSyncException {
        ensureSatAdmin(loggedInUser);
        SCCRefreshLock.tryGetLock();
        try {
            ContentSyncManager csm = new ContentSyncManager();
            csm.updateSUSEProducts(csm.getProducts());
        }
        finally {
            SCCRefreshLock.unlockFile();
        }
        return BaseHandler.VALID;
    }

    /**
     * @deprecated
     * Synchronize SUSE product channels between the Customer Center
     * and the SUSE Manager database.
     * This method is one step of the whole refresh cycle.
     *
     * @param loggedInUser the currently logged in user
     * @return Integer
     *
     * @xmlrpc.doc (Deprecated) Synchronize SUSE product channels between the Customer Center
     *             and the SUSE Manager database.
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.returntype #return_int_success()
     */
    @Deprecated
    public Integer synchronizeProductChannels(User loggedInUser) {
        ensureSatAdmin(loggedInUser);
        return BaseHandler.VALID;
    }

    /**
     * Synchronize subscriptions between the Customer Center
     * and the SUSE Manager database.
     * This method is one step of the whole refresh cycle.
     *
     * @param loggedInUser the currently logged in user
     * @return Integer
     * @throws ContentSyncException in case of an error
     *
     * @xmlrpc.doc Synchronize subscriptions between the Customer Center
     *             and the SUSE Manager database.
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.returntype #return_int_success()
     */
    public Integer synchronizeSubscriptions(User loggedInUser) throws ContentSyncException {
        ensureSatAdmin(loggedInUser);
        SCCRefreshLock.tryGetLock();
        try {
            ContentSyncManager csm = new ContentSyncManager();
            csm.updateSubscriptions();
        }
        finally {
            SCCRefreshLock.unlockFile();
        }
        return BaseHandler.VALID;
    }

    /**
     * Synchronize Repositories between the Customer Center
     * and the SUSE Manager database.
     * This method is one step of the whole refresh cycle.
     *
     * @param loggedInUser the currently logged in user
     * @param mirrorUrl optional mirror url
     * @return Integer
     * @throws ContentSyncException in case of an error
     *
     * @xmlrpc.doc Synchronize repositories between the Customer Center
     *             and the SUSE Manager database.
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.param #param_desc("string", "mirrorUrl", "Optional mirror url or null")
     * @xmlrpc.returntype #return_int_success()
     */
    public Integer synchronizeRepositories(User loggedInUser, String mirrorUrl) throws ContentSyncException {
        ensureSatAdmin(loggedInUser);
        SCCRefreshLock.tryGetLock();
        try {
            ContentSyncManager csm = new ContentSyncManager();
            csm.updateRepositories(mirrorUrl);
        }
        finally {
            SCCRefreshLock.unlockFile();
        }
        return BaseHandler.VALID;
    }

    /**
     * Add a new channel to the SUSE Manager database.
     *
     * @param loggedInUser the currently logged in user
     * @param channelLabel label of the channel to add
     * @param mirrorUrl optional mirror URL
     * @return Integer
     * @throws ContentSyncException in case of an error
     *
     * @xmlrpc.doc Add a new channel to the SUSE Manager database
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.param #param_desc("string", "channelLabel", "Label of the channel to add")
     * @xmlrpc.param #param_desc("string", "mirrorUrl", "Sync from mirror temporarily")
     * @xmlrpc.returntype #return_int_success()
     */
    public Integer addChannel(User loggedInUser, String channelLabel, String mirrorUrl)
            throws ContentSyncException {
        ensureSatAdmin(loggedInUser);
        ContentSyncManager csm = new ContentSyncManager();
        csm.addChannel(channelLabel, mirrorUrl);
        return BaseHandler.VALID;
    }

    /**
     * Add a new channel to the SUSE Manager database and its required bases.
     *
     * @param loggedInUser the currently logged in user
     * @param channelLabel label of the channel to add
     * @param mirrorUrl optional mirror URL
     * @return Array of enabled channel labels
     * @throws ContentSyncException in case of an error
     *
     * @xmlrpc.doc Add a new channel to the SUSE Manager database
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.param #param_desc("string", "channelLabel", "Label of the channel to add")
     * @xmlrpc.param #param_desc("string", "mirrorUrl", "Sync from mirror temporarily")
     * @xmlrpc.returntype #array_single("string", "enabled channel labels")
     */
    public Object[] addChannels(User loggedInUser, String channelLabel, String mirrorUrl)
            throws ContentSyncException {
        ensureSatAdmin(loggedInUser);
        ContentSyncManager csm = new ContentSyncManager();

        List<String> mandatoryChannelLabels =
                SUSEProductFactory.findNotSyncedMandatoryChannels(channelLabel)
                .map(c -> c.getChannelLabel())
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
     * Add organization credentials (mirror credentials) to SUSE Manager.
     *
     * @param loggedInUser the currently logged in user
     * @param username organization credentials (mirror credentials) username
     * @param password organization credentials (mirror credentials) password
     * @param primary make this the primary credentials
     * @return Integer
     * @throws ContentSyncException in case of an error
     *
     * @xmlrpc.doc Add organization credentials (mirror credentials) to SUSE Manager.
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.param #param_desc("string", "username", "Organization credentials
     *                                                  (Mirror credentials) username")
     * @xmlrpc.param #param_desc("string", "password", "Organization credentials
     *                                                  (Mirror credentials) password")
     * @xmlrpc.param #param_desc("boolean", "primary", "Make this the primary credentials")
     * @xmlrpc.returntype #return_int_success()
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
     * Delete organization credentials (mirror credentials) from SUSE Manager.
     *
     * @param loggedInUser the currently logged in user
     * @param username username of the credentials to be deleted
     * @return Integer
     * @throws ContentSyncException in case of an error
     *
     * @xmlrpc.doc Delete organization credentials (mirror credentials) from SUSE Manager.
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.param #param_desc("string", "username", "Username of credentials to delete")
     * @xmlrpc.returntype #return_int_success()
     */
    public Integer deleteCredentials(User loggedInUser, String username)
            throws ContentSyncException {
        ensureSatAdmin(loggedInUser);
        for (Credentials c : CredentialsFactory.lookupSCCCredentials()) {
            if (c.getUsername().equals(username)) {
                new MirrorCredentialsManager().deleteMirrorCredentials(c.getId(), null);
                break;
            }
        }
        return BaseHandler.VALID;
    }

    /**
     * List organization credentials (mirror credentials) available in SUSE Manager.
     *
     * @param loggedInUser the currently logged in user
     * @return List of organization credentials (mirror credentials)
     * @throws ContentSyncException in case of an error
     *
     * @xmlrpc.doc List organization credentials (mirror credentials) available in
     *             SUSE Manager.
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.returntype #array()
     *                       $MirrorCredentialsDtoSerializer
     *                    #array_end()
     */
    public List<MirrorCredentialsDto> listCredentials(User loggedInUser)
            throws ContentSyncException {
        ensureSatAdmin(loggedInUser);
        return new MirrorCredentialsManager().findMirrorCredentials();
    }
}
