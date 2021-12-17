/*
 * Copyright (c) 2019 SUSE LLC
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
package com.suse.manager.virtualization;

import org.jdom.Element;

/**
 * Class representing the virtual storage source authentication parameters.
 */
public class PoolSourceAuthentication {
    private String username;
    private String password;
    private String type;
    private String secretType;
    private String secretValue;

    /**
     * Construct authentication
     *
     * @param usernameIn the username
     * @param secretIn the secret
     */
    public PoolSourceAuthentication(String usernameIn, String secretIn) {
        setUsername(usernameIn);
        setPassword(secretIn);
    }

    /**
     * @return Returns the username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param usernameIn The username to set.
     */
    public void setUsername(String usernameIn) {
        username = usernameIn;
    }

    /**
     * @return Returns the secret.
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param passwordIn The secret to set
     */
    public void setPassword(String passwordIn) {
        password = passwordIn;
    }


    /**
     * @return Returns the type.
     */
    public String getType() {
        return type;
    }


    /**
     * @param typeIn The type to set.
     */
    public void setType(String typeIn) {
        type = typeIn;
    }


    /**
     * @return Returns the secretType.
     */
    public String getSecretType() {
        return secretType;
    }


    /**
     * @param secretTypeIn The secretType to set.
     */
    public void setSecretType(String secretTypeIn) {
        secretType = secretTypeIn;
    }


    /**
     * @return Returns the secretValue.
     */
    public String getSecretValue() {
        return secretValue;
    }


    /**
     * @param secretValueIn The secretValue to set.
     */
    public void setSecretValue(String secretValueIn) {
        secretValue = secretValueIn;
    }

    /**
     * Extract the data from the libvirt pool XML source authentication element.
     *
     * @param node the source authentication XML element
     * @return the created source authentication
     * @throws IllegalArgumentException if the node is missing required attributes or children
     */
    public static PoolSourceAuthentication parse(Element node) throws IllegalArgumentException {
        PoolSourceAuthentication result = null;
        if (node != null) {
            String username = node.getAttributeValue("username");
            if (username == null) {
                throw new IllegalArgumentException("Missing required username in pool source authentication");
            }
            result = new PoolSourceAuthentication(username, null);
            result.setType(node.getAttributeValue("type"));
            Element secret = node.getChild("secret");
            if (secret != null) {
                String uuid = secret.getAttributeValue("uuid");
                String usage = secret.getAttributeValue("usage");
                if (uuid != null) {
                    result.setSecretType("uuid");
                    result.setSecretValue(uuid);
                }
                else if (usage != null) {
                    result.setSecretType("usage");
                    result.setSecretValue(usage);
                }
            }
        }
        return result;
    }
}
