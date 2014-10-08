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
 * SUSE trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate SUSE trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.frontend.xmlrpc.sync.content;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.manager.content.ContentSyncException;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.manager.content.MgrSyncUtils;
import com.redhat.rhn.manager.setup.MirrorCredentialsDto;
import com.redhat.rhn.manager.setup.MirrorCredentialsManager;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
     * @param sessionKey Session token.
     * @return List of products with their extensions (add-ons).
     * @throws ContentSyncException in case of an error
     *
     * @xmlrpc.doc List all accessible products.
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.returntype #array()
     *                       $ListedProductSerializer
     *                    #array_end()
     */
    public Object[] listProducts(String sessionKey) throws ContentSyncException {
        BaseHandler.getLoggedInUser(sessionKey);
        ContentSyncManager csm = new ContentSyncManager();
        return csm.listProducts(csm.listChannels(csm.getRepositories())).toArray();
    }

    /**
     * List all accessible channels.
     *
     * @param sessionKey Session Key
     * @return List of channels.
     * @throws ContentSyncException in case of an error
     *
     * @xmlrpc.doc List all accessible channels.
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.returntype #array()
     *                       $MgrSyncChannelSerializer
     *                    #array_end()
     */
    public Object[] listChannels(String sessionKey) throws ContentSyncException {
        BaseHandler.getLoggedInUser(sessionKey);
        ContentSyncManager csm = new ContentSyncManager();
        return csm.listChannels(csm.getRepositories()).toArray();
    }

    /**
     * Synchronize channels between the Customer Center and the SUSE Manager database.
     * This method is one step of the whole refresh cycle.
     *
     * @param sessionKey User session token.
     * @param mirrorUrl optional mirror URL
     * @return Integer
     * @throws ContentSyncException in case of an error
     *
     * @xmlrpc.doc Synchronize channels between the Customer Center
     *             and the SUSE Manager database.
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.param #param_desc("string", "mirrorUrl", "Sync from mirror temporarily")
     * @xmlrpc.returntype #return_int_success()
     */
    public Integer synchronizeChannels(String sessionKey, String mirrorUrl)
            throws ContentSyncException {
        BaseHandler.getLoggedInUser(sessionKey);
        ContentSyncManager csm = new ContentSyncManager();
        csm.updateChannels(csm.getRepositories(), mirrorUrl);
        return BaseHandler.VALID;
    }

    /**
     * Synchronize channel families between the Customer Center
     * and the SUSE Manager database.
     * This method is one step of the whole refresh cycle.
     *
     * @param sessionKey User session token.
     * @return Integer
     * @throws ContentSyncException in case of an error
     *
     * @xmlrpc.doc Synchronize channel families between the Customer Center
     *             and the SUSE Manager database.
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.returntype #return_int_success()
     */
    public Integer synchronizeChannelFamilies(String sessionKey)
            throws ContentSyncException {
        BaseHandler.getLoggedInUser(sessionKey);
        ContentSyncManager csm = new ContentSyncManager();
        csm.updateChannelFamilies(csm.readChannelFamilies());
        return BaseHandler.VALID;
    }

    /**
     * Synchronize SUSE products between the Customer Center and the SUSE Manager database.
     * This method is one step of the whole refresh cycle.
     *
     * @param sessionKey User session token.
     * @return Integer
     * @throws ContentSyncException in case of an error
     *
     * @xmlrpc.doc Synchronize SUSE products between the Customer Center
     *             and the SUSE Manager database.
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.returntype #return_int_success()
     */
    public Integer synchronizeProducts(String sessionKey) throws ContentSyncException {
        BaseHandler.getLoggedInUser(sessionKey);
        ContentSyncManager csm = new ContentSyncManager();
        csm.updateSUSEProducts(csm.getProducts());
        return BaseHandler.VALID;
    }

    /**
     * Synchronize SUSE product channels between the Customer Center
     * and the SUSE Manager database.
     * This method is one step of the whole refresh cycle.
     *
     * @param sessionKey User session token.
     * @return Integer
     * @throws ContentSyncException in case of an error
     *
     * @xmlrpc.doc Synchronize SUSE product channels between the Customer Center
     *             and the SUSE Manager database.
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.returntype #return_int_success()
     */
    public Integer synchronizeProductChannels(String sessionKey)
            throws ContentSyncException {
        BaseHandler.getLoggedInUser(sessionKey);
        ContentSyncManager csm = new ContentSyncManager();
        csm.updateSUSEProductChannels(csm.getAvailableChannels(csm.readChannels()));
        return BaseHandler.VALID;
    }

    /**
     * Synchronize upgrade paths between the Customer Center
     * and the SUSE Manager database.
     * This method is one step of the whole refresh cycle.
     *
     * @param sessionKey User session token.
     * @return Integer
     * @throws ContentSyncException in case of an error
     *
     * @xmlrpc.doc Synchronize upgrade paths between the Customer Center
     *             and the SUSE Manager database.
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.returntype #return_int_success()
     */
    public Integer synchronizeUpgradePaths(String sessionKey) throws ContentSyncException {
        BaseHandler.getLoggedInUser(sessionKey);
        new ContentSyncManager().updateUpgradePaths();
        return BaseHandler.VALID;
    }

    /**
     * Synchronize subscriptions between the Customer Center
     * and the SUSE Manager database.
     * This method is one step of the whole refresh cycle.
     *
     * @param sessionKey User session token.
     * @return Integer
     * @throws ContentSyncException in case of an error
     *
     * @xmlrpc.doc Synchronize subscriptions between the Customer Center
     *             and the SUSE Manager database.
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.returntype #return_int_success()
     */
    public Integer synchronizeSubscriptions(String sessionKey) throws ContentSyncException {
        BaseHandler.getLoggedInUser(sessionKey);
        ContentSyncManager csm = new ContentSyncManager();
        csm.updateSubscriptions(csm.getSubscriptions());
        return BaseHandler.VALID;
    }

    /**
     * Add a new channel to the SUSE Manager database.
     *
     * @param sessionKey user session token
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
    public Integer addChannel(String sessionKey, String channelLabel, String mirrorUrl)
            throws ContentSyncException {
        BaseHandler.getLoggedInUser(sessionKey);
        ContentSyncManager csm = new ContentSyncManager();
        csm.addChannel(channelLabel, csm.getRepositories(), mirrorUrl);
        return BaseHandler.VALID;
    }

    /**
     * Migrate this SUSE Manager server to work with SCC.
     *
     * @param sessionKey user session token
     * @return Integer
     * @throws ContentSyncException in case of an error
     *
     * @xmlrpc.doc Migrate this SUSE Manager server to work with SCC.
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.returntype #return_int_success()
     */
    public Integer performMigration(String sessionKey) throws ContentSyncException {
        User user = BaseHandler.getLoggedInUser(sessionKey);

        // Clear relevant database tables
        SUSEProductFactory.clearAllProducts();

        // Migrate mirror credentials into the DB
        MirrorCredentialsManager credsManager = MirrorCredentialsManager.createInstance();
        int id = 0;
        for (MirrorCredentialsDto dto : credsManager.findMirrorCredentials()) {
            Credentials c = CredentialsFactory.createSCCCredentials();
            c.setUsername(dto.getUser());
            c.setPassword(dto.getPassword());
            // We identify the primary credentials by setting a URL
            if (id == 0) {
                c.setUrl(Config.get().getString("scc_url"));
            }
            CredentialsFactory.storeCredentials(c);
            id++;
        }
        for (long i = --id; id >= 0; id--) {
            credsManager.deleteMirrorCredentials(i, user, null);
        }

        // Touch file to indicate that server has been migrated
        try {
            FileUtils.touch(new File(MgrSyncUtils.SCC_MIGRATED));
        }
        catch (IOException e) {
            throw new ContentSyncException(e);
        }
        return BaseHandler.VALID;
    }

    /**
     * Add mirror credentials to SUSE Manager.
     *
     * @param sessionKey user session token
     * @param username mirror credentials username
     * @param password mirror credentials password
     * @param primary make this the primary credentials
     * @return Integer
     * @throws ContentSyncException
     *
     * @xmlrpc.doc Add mirror credentials to SUSE Manager.
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.param #param_desc("string", "username", "Mirror credentials username")
     * @xmlrpc.param #param_desc("string", "password", "Mirror credentials password")
     * @xmlrpc.param #param_desc("boolean", "primary", "Make this the primary credentials")
     * @xmlrpc.returntype #return_int_success()
     */
    public Integer addCredentials(String sessionKey, String username, String password,
            boolean primary) throws ContentSyncException {
        User user = BaseHandler.getLoggedInUser(sessionKey);
        MirrorCredentialsDto creds = new MirrorCredentialsDto(username, password);
        MirrorCredentialsManager credsManager = MirrorCredentialsManager.createInstance();
        long id = credsManager.storeMirrorCredentials(creds, user, null);
        if (primary) {
            credsManager.makePrimaryCredentials(id, user, null);
        }
        return BaseHandler.VALID;
    }

    /**
     * Delete mirror credentials from SUSE Manager.
     *
     * @param sessionKey user session token
     * @param username username of the credentials to be deleted
     * @return Integer
     * @throws ContentSyncException
     *
     * @xmlrpc.doc Delete mirror credentials from SUSE Manager.
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.param #param_desc("string", "username", "Username of the credentials to be deleted")
     * @xmlrpc.returntype #return_int_success()
     */
    public Integer deleteCredentials(String sessionKey, String username)
            throws ContentSyncException {
        User user = BaseHandler.getLoggedInUser(sessionKey);
        for (Credentials c : CredentialsFactory.lookupSCCCredentials()) {
            if (c.getUsername().equals(username)) {
                MirrorCredentialsManager credsManager =
                        MirrorCredentialsManager.createInstance();
                credsManager.deleteMirrorCredentials(c.getId(), user, null);
                break;
            }
        }
        return BaseHandler.VALID;
    }

    /**
     * List mirror credentials available in SUSE Manager.
     *
     * @param sessionKey user session token
     * @return List of mirror credentials
     * @throws ContentSyncException
     *
     * @xmlrpc.doc List mirror credentials available in SUSE Manager.
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.returntype #array()
     *                       $MirrorCredentialsDtoSerializer
     *                    #array_end()
     */
    public List<MirrorCredentialsDto> listCredentials(String sessionKey)
            throws ContentSyncException {
        MirrorCredentialsManager credsManager =
                MirrorCredentialsManager.createInstance();
        return credsManager.findMirrorCredentials();
    }
}
