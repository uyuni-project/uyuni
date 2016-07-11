/**
 * Copyright (c) 2016 SUSE LLC
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
package com.suse.manager.webui.utils.gson;

import org.apache.commons.lang.StringUtils;

import java.util.Optional;

/**
 * Class representation of JSON data as sent by the minion bootstrapping UI.
 */
public class JSONBootstrapHosts {

    /** Host IP address or DNS name */
    private String host;
    private String port;
    private String user;
    private String password;
    private boolean ignoreHostKeys;

    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @return the port
     */
    public String getPort() {
        return port;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @return the host
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return value of ignoreHostKeys
     */
    public boolean getIgnoreHostKeys() {
        return ignoreHostKeys;
    }

    /**
     * Helper method to return the port as an Optional<Integer>.
     *
     * @return port as an Optional<Integer>
     */
    public Optional<Integer> getPortInteger() {
        Optional<Integer> ret = Optional.empty();
        if (StringUtils.isNotEmpty(port)) {
            ret = Optional.of(Integer.valueOf(port));
        }
        return ret;
    }
}
