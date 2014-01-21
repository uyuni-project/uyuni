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

package com.redhat.rhn.manager.setup;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.manager.BaseManager;

public class SetupWizardManager extends BaseManager {

    // Logger for this class
    private static Logger logger = Logger.getLogger(SetupWizardManager.class);

    // Config keys
    public final static String KEY_MIRROR_CREDENTIALS_USER = "server.susemanager.mirrcred_user";
    public final static String KEY_MIRROR_CREDENTIALS_PASS = "server.susemanager.mirrcred_pass";
    public final static String KEY_MIRROR_CREDENTIALS_EMAIL = "server.susemanager.mirrcred_email";

    /**
     * Find all valid mirror credentials and return them.
     * @return List of all available mirror credentials
     */
    public static List<MirrorCredentials> getMirrorCredentials() {
        List<MirrorCredentials> credsList = new ArrayList<MirrorCredentials>();

        // Get the main pair of credentials
        String user = Config.get().getString(KEY_MIRROR_CREDENTIALS_USER);
        String password = Config.get().getString(KEY_MIRROR_CREDENTIALS_PASS);
        String email = Config.get().getString(KEY_MIRROR_CREDENTIALS_EMAIL);

        // Add credentials as long as they have user and password
        MirrorCredentials creds;
        int index = 1;
        while (user != null && password != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Mirror Credentials: " + user + ":" + password + ", " + email);
            }

            // Create credentials object
            creds = new MirrorCredentials(user, password, email);
            credsList.add(creds);

            // Search additional credentials with continuous enumeration
            user = Config.get().getString(KEY_MIRROR_CREDENTIALS_USER + "." + index);
            password = Config.get().getString(KEY_MIRROR_CREDENTIALS_PASS + "." + index);
            email = Config.get().getString(KEY_MIRROR_CREDENTIALS_EMAIL + "." + index);
            index++;
        }

        return credsList;
    }
}
