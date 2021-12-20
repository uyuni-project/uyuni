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

import com.suse.manager.webui.utils.gson.BootstrapHostsJson.AuthMethod;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;
import java.util.Optional;

/**
 * Class representation of the data needed for bootstrapping hosts.
 */
public class BootstrapParameters {

    /** Host IP address or DNS name */
    private String host;
    private Optional<Integer> port;
    private String user;
    private Optional<String> password = empty();
    private Optional<String> privateKey = empty();
    private Optional<String> privateKeyPassphrase = empty();
    private Optional<Long> ansibleInventoryId = empty();
    private List<String> activationKeys;
    private Optional<String> reactivationKey;
    private boolean ignoreHostKeys;
    private Optional<Long> proxyId;

    /**
     * Create {@link BootstrapParameters} for password authentication.
     *
     * @param hostIn host
     * @param portIn port
     * @param userIn user
     * @param passwordIn password
     * @param activationKeysIn activation keys
     * @param reactivationKeyIn reactivation key
     * @param ignoreHostKeysIn ignore hostIn keys?
     * @param proxyIdIn proxy id
     */
    public BootstrapParameters(String hostIn, Optional<Integer> portIn, String userIn, Optional<String> passwordIn,
            List<String> activationKeysIn, Optional<String> reactivationKeyIn, boolean ignoreHostKeysIn,
            Optional<Long> proxyIdIn) {
        this.host = hostIn;
        this.port = portIn;
        this.user = userIn;
        this.password = passwordIn;
        this.activationKeys = activationKeysIn;
        this.reactivationKey = reactivationKeyIn;
        this.ignoreHostKeys = ignoreHostKeysIn;
        this.proxyId = proxyIdIn;
    }

    /**
     * Create {@link BootstrapParameters} for ssh-key authentication.
     *
     * @param hostIn host
     * @param portIn port
     * @param userIn user
     * @param privateKeyIn SSH private key as string in PEM format
     * @param privateKeyPwdIn SSH private key passphrase
     * @param activationKeysIn activation keys
     * @param reactivationKeyIn reactivation key
     * @param ignoreHostKeysIn ignore hostIn keys?
     * @param proxyIdIn proxy id
     */
    public BootstrapParameters(String hostIn, Optional<Integer> portIn, String userIn, String privateKeyIn,
            Optional<String> privateKeyPwdIn, List<String> activationKeysIn, Optional<String> reactivationKeyIn,
            boolean ignoreHostKeysIn, Optional<Long> proxyIdIn) {
        this.host = hostIn;
        this.port = portIn;
        this.user = userIn;
        this.privateKey = of(privateKeyIn);
        this.privateKeyPassphrase = privateKeyPwdIn;
        this.activationKeys = activationKeysIn;
        this.reactivationKey = reactivationKeyIn;
        this.ignoreHostKeys = ignoreHostKeysIn;
        this.proxyId = proxyIdIn;
    }

    /**
     * Create {@link BootstrapParameters} for Ansible pre-authentication.
     *
     * @param hostIn host
     * @param portIn port
     * @param userIn user
     * @param ansibleInventoryIdIn the id of the Ansible Inventory Path
     * @param activationKeysIn activation keys
     * @param reactivationKeyIn reactivation key
     * @param ignoreHostKeysIn ignore hostIn keys?
     * @param proxyIdIn proxy id
     */
    public BootstrapParameters(String hostIn, Optional<Integer> portIn, String userIn, Long ansibleInventoryIdIn,
            List<String> activationKeysIn, Optional<String> reactivationKeyIn, boolean ignoreHostKeysIn,
            Optional<Long> proxyIdIn) {
        this.host = hostIn;
        this.port = portIn;
        this.user = userIn;
        this.ansibleInventoryId = of(ansibleInventoryIdIn);
        this.activationKeys = activationKeysIn;
        this.reactivationKey = reactivationKeyIn;
        this.ignoreHostKeys = ignoreHostKeysIn;
        this.proxyId = proxyIdIn;
    }

    /**
     * Create bootstrap parameters based on the JSON input.
     *
     * @param json JSON input
     * @return the bootstrap parameters
     */
    public static BootstrapParameters createFromJson(BootstrapHostsJson json) {
        final BootstrapHostsJson.AuthMethod authMethod = json.maybeGetAuthMethod().orElse(AuthMethod.PASSWORD);
        switch (authMethod) {
            case PASSWORD:
                return new BootstrapParameters(json.getHost(), json.getPortInteger(), json.getUser(),
                        json.maybeGetPassword(), json.getActivationKeys(), json.maybeGetReactivationKey(),
                        json.getIgnoreHostKeys(), Optional.ofNullable(json.getProxy()));
            case SSH_KEY:
                return new BootstrapParameters(json.getHost(), json.getPortInteger(), json.getUser(),
                        json.getPrivKey(), json.maybeGetPrivKeyPwd(), json.getActivationKeys(),
                        json.maybeGetReactivationKey(), json.getIgnoreHostKeys(),
                        Optional.ofNullable(json.getProxy()));
            case ANSIBLE_PREAUTH:
                return new BootstrapParameters(json.getHost(), json.getPortInteger(), json.getUser(),
                        json.getAnsibleInventoryId(), json.getActivationKeys(), json.maybeGetReactivationKey(),
                        json.getIgnoreHostKeys(), Optional.ofNullable(json.getProxy()));
            default:
                throw new UnsupportedOperationException("Unsupported auth method " + authMethod);
        }
    }

    /**
     * Gets the host.
     *
     * @return host
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the host.
     *
     * @param hostIn - the host
     */
    public void setHost(String hostIn) {
        host = hostIn;
    }

    /**
     * Gets the port.
     *
     * @return port
     */
    public Optional<Integer> getPort() {
        return port;
    }

    /**
     * Sets the port.
     *
     * @param portIn - the port
     */
    public void setPort(Optional<Integer> portIn) {
        port = portIn;
    }

    /**
     * Gets the user.
     *
     * @return user
     */
    public String getUser() {
        return user;
    }

    /**
     * Sets the user.
     *
     * @param userIn - the user
     */
    public void setUser(String userIn) {
        user = userIn;
    }

    /**
     * Gets the password.
     *
     * @return password
     */
    public Optional<String> getPassword() {
        return password;
    }

    /**
     * Sets the password.
     *
     * @param passwordIn - the password
     */
    public void setPassword(Optional<String> passwordIn) {
        password = passwordIn;
    }

    /**
     * Gets the privateKey.
     *
     * @return privateKey
     */
    public Optional<String> getPrivateKey() {
        return privateKey;
    }

    /**
     * Sets the privateKey.
     *
     * @param privateKeyIn the privateKey
     */
    public void setPrivateKey(Optional<String> privateKeyIn) {
        privateKey = privateKeyIn;
    }

    /**
     * Gets the privateKeyPassphrase.
     *
     * @return privateKeyPassphrase
     */
    public Optional<String> getPrivateKeyPassphrase() {
        return privateKeyPassphrase;
    }

    /**
     * Sets the privateKeyPassphrase.
     *
     * @param privateKeyPassphraseIn the privateKeyPassphrase
     */
    public void setPrivateKeyPassphrase(Optional<String> privateKeyPassphraseIn) {
        privateKeyPassphrase = privateKeyPassphraseIn;
    }

    /**
     * Gets the activationKeys.
     *
     * @return activationKeys
     */
    public List<String> getActivationKeys() {
        return activationKeys;
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
     * Sets the activationKeys.
     *
     * @param activationKeysIn - the activationKeys
     */
    public void setActivationKeys(List<String> activationKeysIn) {
        activationKeys = activationKeysIn;
    }

    /**
     * @return Returns the reactivationKey.
     */
    public Optional<String> getReactivationKey() {
        return reactivationKey;
    }

    /**
     * @param reactivationKeyIn The reactivationKey to set.
     */
    public void setReactivationKey(Optional<String> reactivationKeyIn) {
        reactivationKey = reactivationKeyIn;
    }

    /**
     * Gets the ignoreHostKeys.
     *
     * @return ignoreHostKeys
     */
    public boolean isIgnoreHostKeys() {
        return ignoreHostKeys;
    }

    /**
     * Sets "ignore host keys" flag
     * @param ignoreHostKeysIn the "ignore host keys" flag
     */
    public void setIgnoreHostKeys(boolean ignoreHostKeysIn) {
        this.ignoreHostKeys = ignoreHostKeysIn;
    }

    /**
     * Gets the proxy id.
     *
     * @return proxyId
     */
    public Optional<Long> getProxyId() {
        return proxyId;
    }

    /**
     * Gets the ansibleInventoryId.
     *
     * @return ansibleInventoryId
     */
    public Optional<Long> getAnsibleInventoryId() {
        return ansibleInventoryId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("host", host)
                .append("port", port)
                .append("activationKeys", activationKeys)
                .append("proxyId", proxyId)
                .append("reactivationKey", reactivationKey)
                .toString();
    }
}
