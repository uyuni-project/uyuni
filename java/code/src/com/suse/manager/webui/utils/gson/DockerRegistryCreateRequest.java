/**
 * Copyright (c) 2017 SUSE LLC
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

/**
 * JSON request wrapper for Docker registry image stores.
 */
public class DockerRegistryCreateRequest {

    /**
     * Nested class for credentials.
     */
    public static class CredentialsJson {

        private String username;
        private String password;
        private String email;

        /**
         * @return the username
         */
        public String getUsername() {
            return username;
        }

        /**
         * @return the password
         */
        public String getPassword() {
            return password;
        }

        /**
         * @return the email
         */
        public String getEmail() {
            return email;
        }
    }

    private String label;
    private String uri;
    private CredentialsJson credentials;

    /**
     * @return the credentials
     */
    public CredentialsJson getCredentials() {
        return credentials;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }
}
