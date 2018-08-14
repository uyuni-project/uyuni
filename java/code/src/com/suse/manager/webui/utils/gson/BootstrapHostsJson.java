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

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Class representation of JSON data as sent by the minion bootstrapping UI.
 */
public class BootstrapHostsJson {

    /** Host IP address or DNS name */
    private String host;
    private String port = "22";
    private String user = "root";
    private String password;
    private List<String> activationKeys;
    private boolean ignoreHostKeys;
    private Long proxy;

    /**
     * Default constructor.
     */
    public BootstrapHostsJson() {
    }

    /**
     * Constructor to be used for bootstrapping systems via API.
     *
     * @param hostIn target host
     * @param portIn SSH port
     * @param userIn SSH user
     * @param passwordIn SSH password
     * @param activationKey activation key
     * @param proxyIn system ID of proxy server to use
     */
    public BootstrapHostsJson(String hostIn, Integer portIn, String userIn,
                              String passwordIn, String activationKey, Long proxyIn) {
        host = hostIn;
        port = String.valueOf(portIn);
        user = userIn;
        password = passwordIn;
        activationKeys = StringUtils.isEmpty(activationKey) ?
                new ArrayList<>() : Arrays.asList(activationKey);
        proxy = proxyIn;
        ignoreHostKeys = true;
    }

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
     * @return the password
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
     * @return value of activationKeys
     */
    public List<String> getActivationKeys() {
        return activationKeys;
    }

    /**
     * @return the id of the proxy
     */
    public Long getProxy() {
        return proxy;
    }

    /**
     * Convenience method for getting first selected activation key or empty.
     * @return first selected activation key label or empty if none selected
     */
    public Optional<String> getFirstActivationKey() {
        return Optional.ofNullable(getActivationKeys())
                .flatMap(list -> list.stream().findFirst());
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

    /**
     * Helper method to return the password as an Optional<String>.
     *
     * @return password wrapped in Optional, or empty Optional if password is empty.
     */
    public Optional<String> maybeGetPassword() {
        if (StringUtils.isEmpty(password)) {
            return Optional.empty();
        }
        return Optional.of(password);
    }
}
