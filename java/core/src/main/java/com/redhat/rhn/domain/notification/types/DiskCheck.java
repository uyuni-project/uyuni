/*
 * Copyright (c) 2022--2025 SUSE LLC
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

import com.suse.manager.utils.DiskCheckSeverity;

public class DiskCheck implements NotificationData {

    private static final LocalizationService LOCALIZATION_SERVICE = LocalizationService.getInstance();

    private final DiskCheckSeverity severity;
    private final String component;

     /**
     * Convenience constructor for legacy callers that only provide a severity.
     * Uses a generic component name of "system".
     * @param severityIn The severity of the disk check.
     */
    public DiskCheck(DiskCheckSeverity severityIn) {
        this("system", severityIn);
    }

    /**
     * Primary constructor.
     * @param componentIn The component being checked.
     * @param severityIn The severity of the disk check.
     */
    public DiskCheck(String componentIn, DiskCheckSeverity severityIn) {
        this.component = componentIn;
        this.severity = severityIn;
    }

    @Override
    public NotificationSeverity getSeverity() {
        if (this.severity.equals(DiskCheckSeverity.CRITICAL)) {
            return NotificationSeverity.ERROR;
        }

        if (this.severity.equals(DiskCheckSeverity.ALERT)) {
          return NotificationSeverity.WARNING;
        }
        // anything else is informational
        return NotificationSeverity.INFO;
    }

    @Override
    public String getSummary() {
        return LOCALIZATION_SERVICE.getMessage("notification.diskcheck.summary", this.component, this.severity.name());
    }

    @Override
    public String getDetails() {
        return LOCALIZATION_SERVICE.getMessage("notification.diskcheck.details", this.component, this.severity.name());
    }
}
