/*
 * Copyright (c) 2017--2020 SUSE LLC
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
package com.redhat.rhn.domain.notification.types;

/**
 * Available notification types
 */
public enum NotificationType {
    ONBOARDING_FAILED,
    CHANNEL_SYNC_FAILED,
    CHANNEL_SYNC_FINISHED,
    CREATE_BOOTSTRAP_REPO_FAILED,
    STATE_APPLY_FAILED,
    PAYG_AUTHENTICATION_UPDATE_FAILED,
    END_OF_LIFE_PERIOD,
    SUBSCRIPTION_WARNING,
    UPDATE_AVAILABLE,
    PAYG_NOT_COMPLIANT_WARNING,
    SCC_OPT_OUT_WARNING
}