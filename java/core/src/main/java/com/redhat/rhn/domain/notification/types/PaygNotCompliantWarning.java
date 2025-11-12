/*
 * Copyright (c) 2023--2025 SUSE LLC
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

public class PaygNotCompliantWarning implements NotificationData {
    private static final LocalizationService LOCALIZATION_SERVICE = LocalizationService.getInstance();

    @Override
    public String getSummary() {
        return LOCALIZATION_SERVICE.getMessage("notification.paygnotcompliantwarning.summary");
    }

    @Override
    public String getDetails() {
        return LOCALIZATION_SERVICE.getMessage("notification.paygnotcompliantwarning.detail");
    }
}
