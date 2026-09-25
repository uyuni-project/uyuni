/*
 * Copyright (c) 2020--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.redhat.rhn.domain.notification.types;

import com.redhat.rhn.common.localization.LocalizationService;

import java.util.Optional;

/**
 * Notification data for bootstrap repo creation failure.
 */
public class CreateBootstrapRepoFailed implements NotificationData {

    private String identifier;
    private String details;

    /**
     * Constructor
     * @param ident the identifier
     * @param detailsIn the details
     */
    public CreateBootstrapRepoFailed(String ident, String detailsIn) {
        this.identifier = ident;
        this.details = Optional.ofNullable(detailsIn).orElse("");
    }

    /**
     * @return the bootstrap repo identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSummary() {
        return LocalizationService.getInstance().
                getMessage("notification.bootstraprepofailed", getIdentifier());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDetails() {
        if (details != null) {
            return String.format("<pre>%s</pre>", details);
        }
        return "";
    }
}
