/**
 * Copyright (c) 2014 SUSE
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
package com.redhat.rhn.manager.content;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.suse.scc.client.SCCClientException;
import com.suse.scc.client.SCCProxySettings;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.codec.binary.Base64;

/**
 * Utility methods to be used in {@link ContentSyncManager} related code.
 */
public class MgrSyncUtils {

    /**
     * Send a HEAD request to a given URL to verify accessibility with given credentials.
     * TODO: Support proxied requests with authentication.
     *
     * @param url the URL to verify
     * @param username username for authentication
     * @param password password for authentication
     * @return the response code of the request
     */
    public static int sendHeadRequest(String url, String username, String password)
            throws SCCClientException {
        HttpURLConnection connection = null;
        int responseCode = -1;
        try {
            // Encode the given credentials
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");

            // Basic authentication
            byte[] credsBytes = Base64.encodeBase64((username + ':' + password).getBytes());
            String credsString = new String(credsBytes);
            if (credsString != null) {
                connection.setRequestProperty("Authorization", "BASIC " + credsString);
            }

            // Get the response code
            responseCode = connection.getResponseCode();
        }
        catch (MalformedURLException e) {
            throw new SCCClientException(e);
        }
        catch (IOException e) {
            throw new SCCClientException(e);
        }
        finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return responseCode;
    }

    /**
     * @return the proxy settings configured in /etc/rhn/rhn.conf
     */
    public static SCCProxySettings getRhnProxySettings() {
        ConfigDefaults configDefaults = ConfigDefaults.get();
        SCCProxySettings settings = new SCCProxySettings(
                configDefaults.getProxyHost(), configDefaults.getProxyPort());
        settings.setUsername(configDefaults.getProxyUsername());
        settings.setPassword(configDefaults.getProxyPassword());
        return settings;
    }
}
