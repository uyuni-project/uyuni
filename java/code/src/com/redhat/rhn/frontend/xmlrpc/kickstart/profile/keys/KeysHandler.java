/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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

package com.redhat.rhn.frontend.xmlrpc.kickstart.profile.keys;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import com.redhat.rhn.domain.token.Token;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.activationkey.XmlRpcActivationKeysHelper;
import com.redhat.rhn.frontend.xmlrpc.kickstart.XmlRpcKickstartHelper;
import com.redhat.rhn.manager.kickstart.KickstartActivationKeysCommand;

import com.suse.manager.api.ReadOnly;

import java.util.ArrayList;
import java.util.List;

/**
* KeysHandler
* @apidoc.namespace kickstart.profile.keys
* @apidoc.doc Provides methods to access and modify the list of activation keys
* associated with a kickstart profile.
*/
public class KeysHandler extends BaseHandler {

    /**
     * Lookup the activation keys associated with the kickstart profile.
     * @param loggedInUser The current user
     * @param ksLabel The kickstart profile label
     * @return List of map representations of activation keys
     *
     * @apidoc.doc Lookup the activation keys associated with the kickstart
     * profile.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "ksLabel", "the kickstart profile label")
     * @apidoc.returntype
     *   #return_array_begin()
     *     $ActivationKeySerializer
     *   #array_end()
     */
    @ReadOnly
    public List<ActivationKey> getActivationKeys(User loggedInUser, String ksLabel) {

        checkKickstartPerms(loggedInUser);

        // retrieve the data associated with the kickstart profile
        KickstartData data = lookupKsData(ksLabel, loggedInUser.getOrg());

        // The activation keys are stored in the profile as 'tokens'.  While a
        // token is similar to an activation key, it lacks the actual key value
        // (e.g. "1-asdflkajdklajdfk").  As a result, we'll use the token to
        // retrieve this additional info.
        List<ActivationKey> keys = new ArrayList<>();
        for (Token token : data.getDefaultRegTokens()) {
            ActivationKey key = ActivationKeyFactory.lookupByToken(token);
            keys.add(key);
        }
        return keys;
    }

    /**
     * Add an activation key association to the kickstart profile.
     * @param loggedInUser The current user
     * @param ksLabel The kickstart profile label
     * @param key The activation key
     * @return 1 on success, exception thrown otherwise
     *
     * @apidoc.doc Add an activation key association to the kickstart profile
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "ksLabel", "the kickstart profile label")
     * @apidoc.param #param_desc("string", "key", "the activation key")
     * @apidoc.returntype #return_int_success()
     */
    public int addActivationKey(User loggedInUser, String ksLabel, String key) {

        checkKickstartPerms(loggedInUser);

        KickstartData ksdata = lookupKsData(ksLabel, loggedInUser.getOrg());

        KickstartActivationKeysCommand command = new KickstartActivationKeysCommand(
                ksdata.getId(), loggedInUser);

        ActivationKey activationKey = lookupKey(key, loggedInUser);
        ArrayList<Long> ids = new ArrayList<>();
        ids.add(activationKey.getId());

        command.addTokensByIds(ids);
        command.store();

        return 1;
    }

    /**
     * Remove an activation key association from the kickstart profile.
     * @param loggedInUser The current user
     * @param ksLabel The kickstart profile label
     * @param key The activation key
     * @return 1 on success, exception thrown otherwise
     *
     * @apidoc.doc Remove an activation key association from the kickstart profile
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "ksLabel", "the kickstart profile label")
     * @apidoc.param #param_desc("string", "key", "the activation key")
     * @apidoc.returntype #return_int_success()
     */
    public int removeActivationKey(User loggedInUser, String ksLabel, String key) {

        checkKickstartPerms(loggedInUser);

        KickstartData ksdata = lookupKsData(ksLabel, loggedInUser.getOrg());

        KickstartActivationKeysCommand command = new KickstartActivationKeysCommand(
                ksdata.getId(), loggedInUser);

        ActivationKey activationKey = lookupKey(key, loggedInUser);
        ArrayList<Long> ids = new ArrayList<>();
        ids.add(activationKey.getId());

        command.removeTokensByIds(ids);
        command.store();

        return 1;
    }

    private void checkKickstartPerms(User user) {
        if (!user.hasRole(RoleFactory.CONFIG_ADMIN)) {
            throw new PermissionException(LocalizationService.getInstance()
                    .getMessage("permission.configadmin.needed"));
        }
    }

    private KickstartData lookupKsData(String label, Org org) {
        return XmlRpcKickstartHelper.getInstance().lookupKsData(label, org);
    }

    private ActivationKey lookupKey(String key, User user) {
        return XmlRpcActivationKeysHelper.getInstance().lookupKey(user, key);
    }
}
