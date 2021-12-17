/*
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
package com.suse.manager.webui.controllers.utils;

import com.redhat.rhn.domain.server.ContactMethod;
import com.redhat.rhn.domain.token.ActivationKeyFactory;

import java.util.Optional;

/**
 * Minion contact method utilies.
 */
public class ContactMethodUtil {

    public static final String DEFAULT = "default";
    public static final String SSH_PUSH = "ssh-push";
    public static final String SSH_PUSH_TUNNEL = "ssh-push-tunnel";

    private ContactMethodUtil() { }

    /**
     * Get the contact method based on the actiovation key (if any)
     * and the default contact method.
     * @param activationKey activation key
     * @param defaultContactMethod fallback contact method
     * @return the label of the contact method (default, ssh-push, ssh-push-tunnel)
     */
    public static String getContactMethod(Optional<String> activationKey,
                                          String defaultContactMethod) {
        return activationKey
                .map(ActivationKeyFactory::lookupByKey)
                .map(key -> key.getContactMethod())
                .map(method -> method.getLabel())
                .orElse(defaultContactMethod);
    }

    /**
     * Returns true if contact method is ssh-push or ssh-push-default
     * @param  cm contact method
     * @return true if contact method is ssh-push or ssh-push-default
     */
    public static boolean isSSHPushContactMethod(ContactMethod cm) {
        return isSSHPushContactMethod(cm.getLabel());
    }

    /**
     * Returns true if contact method is ssh-push or ssh-push-default
     * @param  cm contact method given as string
     * @return true if contact method is ssh-push or ssh-push-default
     */
    public static boolean isSSHPushContactMethod(String cm) {
        return cm.equals(SSH_PUSH) || cm.equals(SSH_PUSH_TUNNEL);
    }

    /**
     * @return default contact method for SSH minions.
     */
    public static String getSSHMinionDefault() {
        return SSH_PUSH;
    }

    /**
     * @return default contact method for regular minions.
     */
    public static String getRegularMinionDefault() {
        return DEFAULT;
    }
}
