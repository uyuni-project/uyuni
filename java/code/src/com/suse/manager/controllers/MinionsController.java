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
package com.suse.manager.controllers;

import com.suse.saltstack.netapi.AuthModule;
import com.suse.saltstack.netapi.client.SaltStackClient;
import com.suse.saltstack.netapi.datatypes.Keys;
import com.suse.saltstack.netapi.exception.SaltStackException;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * MinionsController class defining backend methods for the minions page.
 */
public class MinionsController {

    // The salt URI as string
    private String saltMasterURI = "http://localhost:9080";

    /**
     * Get salt keys with their status.
     *
     * @return informaton about the keys from salt master
     */
    public Keys getKeys() {
        SaltStackClient client;
        try {
            client = new SaltStackClient(new URI(saltMasterURI));
            client.login("admin", "admin", AuthModule.AUTO);
            Keys keys = client.keys();
            client.logout();
            return keys;
        } catch (URISyntaxException | SaltStackException e) {
            throw new RuntimeException(e);
        }
    }
}
