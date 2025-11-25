/*
 * Copyright (c) 2024--2025 SUSE LLC
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

import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * A notification for a failed report database update.
 */
public class ReportDatabaseUpdateFailed implements NotificationData {

    private static final LocalizationService L10N_SERVICE = LocalizationService.getInstance();

    private static final String BODY_TEMPLATE = "<p>%s</p><br/><pre>%s</pre>";

    private final String error;

    private final String peripheralServer;

    /**
     * Constructs a ReportDatabaseUpdateFailed notification with the specified exception.
     *
     * @param exceptionIn the exception that caused the database update to fail
     */
    public ReportDatabaseUpdateFailed(Exception exceptionIn) {
        this(exceptionIn, null);
    }

    /**
     * Constructs a ReportDatabaseUpdateFailed notification with the specified exception and peripheral server.
     *
     * @param exceptionIn the exception that caused the database update to fail
     * @param peripheralServerIn the name of the peripheral server involved
     */
    public ReportDatabaseUpdateFailed(Exception exceptionIn, String peripheralServerIn) {
        this.error = ExceptionUtils.getStackTrace(exceptionIn);
        this.peripheralServer = peripheralServerIn;
    }

    @Override
    public String getSummary() {
        return L10N_SERVICE.getMessage("notification.reportdbupdatefailed");
    }

    @Override
    public String getDetails() {
        String message;
        if (peripheralServer != null) {
            message = L10N_SERVICE.getMessage("notification.reportdbupdatefailed.details.hub", peripheralServer);
        }
        else {
            message = L10N_SERVICE.getMessage("notification.reportdbupdatefailed.details.local");
        }

        return String.format(BODY_TEMPLATE, message, error);
    }
}
