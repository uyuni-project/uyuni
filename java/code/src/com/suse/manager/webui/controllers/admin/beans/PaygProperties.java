/**
 * Copyright (c) 2021 SUSE LLC
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

package com.suse.manager.webui.controllers.admin.beans;

import com.google.gson.annotations.SerializedName;

public class PaygProperties {
    private String description;
    private String host;
    private String port;
    private String username;
    private String password;
    private String key;
    @SerializedName("key_password")
    private String keyPassword;
    @SerializedName("bastion_host")
    private String bastionHost;
    @SerializedName("bastion_port")
    private String bastionPort;
    @SerializedName("bastion_username")
    private String bastionUsername;
    @SerializedName("bastion_password")
    private String bastionPassword;
    @SerializedName("bastion_key")
    private String bastionKey;
    @SerializedName("bastion_key_password")
    private String bastionKeyPassword;
    @SerializedName("instance_edit")
    private Boolean instanceEdit;
    @SerializedName("bastion_edit")
    private Boolean bastionEdit;

    /**
     * default constructor
     */
    public PaygProperties() {
    }

    /**
     * Constructor with all the existing properties
     * @param descriptionIn
     * @param hostIn
     * @param portIn
     * @param usernameIn
     * @param passwordIn
     * @param keyIn
     * @param keyPasswordIn
     * @param bastionHostIn
     * @param bastionPortIn
     * @param bastionUsernameIn
     * @param bastionPasswordIn
     * @param bastionKeyIn
     * @param bastionKeyPasswordIn
     * @param instanceEditIn
     * @param bastionEditIn
     */
    public PaygProperties(String descriptionIn,
                          String hostIn, String portIn,
                          String usernameIn, String passwordIn,
                          String keyIn, String keyPasswordIn,
                          String bastionHostIn, String bastionPortIn,
                          String bastionUsernameIn, String bastionPasswordIn,
                          String bastionKeyIn, String bastionKeyPasswordIn,
                          Boolean instanceEditIn, Boolean bastionEditIn) {
        this.description = descriptionIn;
        this.host = hostIn;
        this.port = portIn;
        this.username = usernameIn;
        this.password = passwordIn;
        this.key = keyIn;
        this.keyPassword = keyPasswordIn;
        this.bastionHost = bastionHostIn;
        this.bastionPort = bastionPortIn;
        this.bastionUsername = bastionUsernameIn;
        this.bastionPassword = bastionPasswordIn;
        this.bastionKey = bastionKeyIn;
        this.bastionKeyPassword = bastionKeyPasswordIn;
        this.instanceEdit = instanceEditIn;
        this.bastionEdit = bastionEditIn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String descriptionIn) {
        this.description = descriptionIn;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String hostIn) {
        this.host = hostIn;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String portIn) {
        this.port = portIn;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String usernameIn) {
        this.username = usernameIn;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String passwordIn) {
        this.password = passwordIn;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String keyIn) {
        this.key = keyIn;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public void setKeyPassword(String keyPasswordIn) {
        this.keyPassword = keyPasswordIn;
    }

    public String getBastionHost() {
        return bastionHost;
    }

    public void setBastionHost(String bastionHostIn) {
        this.bastionHost = bastionHostIn;
    }

    public String getBastionPort() {
        return bastionPort;
    }

    public void setBastionPort(String bastionPortIn) {
        this.bastionPort = bastionPortIn;
    }

    public String getBastionUsername() {
        return bastionUsername;
    }

    public void setBastionUsername(String bastionUsernameIn) {
        this.bastionUsername = bastionUsernameIn;
    }

    public String getBastionPassword() {
        return bastionPassword;
    }

    public void setBastionPassword(String bastionPasswordIn) {
        this.bastionPassword = bastionPasswordIn;
    }

    public String getBastionKey() {
        return bastionKey;
    }

    public void setBastionKey(String bastionKeyIn) {
        this.bastionKey = bastionKeyIn;
    }

    public String getBastionKeyPassword() {
        return bastionKeyPassword;
    }

    public void setBastionKeyPassword(String bastionKeyPasswordIn) {
        this.bastionKeyPassword = bastionKeyPasswordIn;
    }

    public Boolean isInstanceEdit() {
        return instanceEdit;
    }

    public void setInstanceEdit(Boolean instanceEditIn) {
        instanceEdit = instanceEditIn;
    }

    public Boolean isBastionEdit() {
        return bastionEdit;
    }

    public void setBastionEdit(Boolean bastionEditIn) {
        bastionEdit = bastionEditIn;
    }
}
