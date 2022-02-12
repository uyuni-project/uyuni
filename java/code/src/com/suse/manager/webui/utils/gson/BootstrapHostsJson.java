/*
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

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

import org.apache.commons.lang3.StringUtils;

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
    private AuthMethod authMethod;
    private String password;
    private String privKey;
    private String privKeyPwd;
    private Long ansibleInventoryId;
    private List<String> activationKeys;
    private String reactivationKey;
    private boolean ignoreHostKeys;
    private Long proxy;

    /**
     * Authentication method for bootstrapping:
     *
     * PASSWORD: authentication with a password
     * SSH_KEY: authentication with an ssh key
     * ANSIBLE_PREAUTH: authenticate using ansible before bootstrapping
     */
    public enum AuthMethod {
        PASSWORD,
        SSH_KEY,
        ANSIBLE_PREAUTH;

        /**
         * Create an {@link AuthMethod} from string
         *
         * @param val the string
         * @return the {@link AuthMethod}
         */
        public static AuthMethod parse(String val) {
            switch (val.toLowerCase()) {
                case "password":
                    return PASSWORD;
                case "ssh-key":
                    return SSH_KEY;
                case "ansible-preauth":
                    return ANSIBLE_PREAUTH;
                default:
                    throw new IllegalArgumentException(String.format("Can't convert '%s' to auth method enum.", val));
            }
        }
    }

    /**
     * Default constructor.
     */
    public BootstrapHostsJson() { }

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
     * Gets the authMethod.
     *
     * @return authMethod
     */
    public AuthMethod getAuthMethod() {
        return authMethod;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the privKey.
     *
     * @return privKey
     */
    public String getPrivKey() {
        return privKey;
    }

    /**
     * Gets the privKeyPwd.
     *
     * @return privKeyPwd
     */
    public String getPrivKeyPwd() {
        return privKeyPwd;
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
     * @return value of reactivationKey
     */
    public String getReactivationKey() {
        return reactivationKey;
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
     * Helper method to return the port as an Optional&lt;Integer&gt;.
     *
     * @return port as an Optional&lt;Integer&gt;
     */
    public Optional<Integer> getPortInteger() {
        Optional<Integer> ret = empty();
        if (StringUtils.isNotEmpty(port)) {
            ret = Optional.of(Integer.valueOf(port));
        }
        return ret;
    }

    /**
     * Helper method to return the auth method as an Optional&lt;String&gt;.
     *
     * @return auth method wrapped in Optional, or empty Optional if auth method is empty.
     */
    public Optional<AuthMethod> maybeGetAuthMethod() {
        return ofNullable(authMethod);
    }

    /**
     * Helper method to return the password as an Optional&lt;String&gt;.
     *
     * @return password wrapped in Optional, or empty Optional if password is empty.
     */
    public Optional<String> maybeGetPassword() {
        return maybeGetString(getPassword());
    }

    /**
     * Helper method to return the ssh private key as an Optional&lt;String&gt;.
     *
     * @return optional of ssh private key
     */
    public Optional<String> maybeGetPrivKey() {
        return maybeGetString(getPrivKey());
    }

    /**
     * Helper method to return the ssh private key passphrase as an Optional&lt;String&gt;.
     *
     * @return optional of ssh private key passphrase
     */
    public Optional<String> maybeGetPrivKeyPwd() {
        return maybeGetString(getPrivKeyPwd());
    }

    /**
     * Helper method to return the reactivation key as as an Optional&lt;String&gt;.
     *
     * @return reactivation key wrapped as Optional, or empty if no reactivation key is provided
     */
    public Optional<String> maybeGetReactivationKey() {
        return maybeGetString(getReactivationKey());
    }

    /**
     * Gets the ansibleInventoryId.
     *
     * @return ansibleInventoryId
     */
    public Long getAnsibleInventoryId() {
        return ansibleInventoryId;
    }

    private static Optional<String> maybeGetString(String str) {
        if (StringUtils.isEmpty(str)) {
            return empty();
        }
        return of(str);
    }
}
