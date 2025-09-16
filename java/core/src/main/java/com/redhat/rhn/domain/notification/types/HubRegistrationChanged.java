/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.domain.notification.types;

import com.redhat.rhn.common.localization.LocalizationService;

import com.suse.manager.model.hub.IssRole;

public class HubRegistrationChanged implements NotificationData {

    private static final LocalizationService LOCALIZER = LocalizationService.getInstance();

    private final boolean registered;

    private final IssRole role;

    private final String fqdn;

    /**
     * Default constructor
     * @param registeredIn true if this server was registered, false if it was removed
     * @param roleIn the role of the remote server which performed the operation.
     * This value is ignore and always set to HUB if the registeredIn parameter is true.
     * @param fqdnIn the fully qualified domain of the remote server
     */
    public HubRegistrationChanged(boolean registeredIn, IssRole roleIn, String fqdnIn) {
        this.registered = registeredIn;
        this.role = registeredIn ? IssRole.HUB : roleIn;
        this.fqdn = fqdnIn;
    }

    @Override
    public String getSummary() {
        return getLocalizedMessage("summary");
    }

    @Override
    public String getDetails() {
        return getLocalizedMessage("details");
    }

    private String getLocalizedMessage(String messageType) {
        if (registered) {
            // Role will always be hub as registration is not symmetrical
            return LOCALIZER.getMessage("notification.hub.registered." + messageType, fqdn);
        }

        // De-registration notification
        return switch (role) {
            // The hub de-registered this server, which was its peripheral
            case HUB -> LOCALIZER.getMessage("notification.hub.hub_deregistered." + messageType, fqdn);
            // The peripheral de-registered this server, which was its hub
            case PERIPHERAL -> LOCALIZER.getMessage("notification.hub.peripheral_deregistered." + messageType, fqdn);
        };
    }
}
