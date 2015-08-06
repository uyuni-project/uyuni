/**
 * Copyright (c) 2015 SUSE LLC
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
package com.suse.manager.webui.models;

import com.redhat.rhn.domain.user.User;
import com.suse.saltstack.netapi.AuthModule;
import com.suse.saltstack.netapi.client.SaltStackClient;
import com.suse.saltstack.netapi.datatypes.Keys;
import com.suse.saltstack.netapi.exception.SaltStackException;

import java.net.URI;

/**
 * Class containing functions for accessing data needed by MinionsController.
 */
public class MinionsModel {

    // The salt URI as string
    private static final URI saltMasterURI = URI.create("http://localhost:9080");

    private MinionsModel() { }

    /**
     * Get the minion keys from salt-api with their respective status.
     *
     * @param user the user to use for connecting to salt-api
     * @return the keys with their respective status as returned from salt-api
     */
    public static Keys getKeys(User user) {
        SaltStackClient client;
        try {
            client = new SaltStackClient(saltMasterURI);
            // FIXME: Pass on actual user credentials as soon as it is supported
            client.login("admin", "", AuthModule.AUTO);
            Keys keys = client.keys();
            client.logoutAsync();
            return keys;
        }
        catch (SaltStackException e) {
            throw new RuntimeException(e);
        }
    }
}
