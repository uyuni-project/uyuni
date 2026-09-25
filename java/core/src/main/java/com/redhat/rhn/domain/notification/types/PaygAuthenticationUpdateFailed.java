/*
 * Copyright (c) 2021--2025 SUSE LLC
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

public class PaygAuthenticationUpdateFailed implements NotificationData {

    private String host;
    private Long paygDataId;

    /**
     * COnstructor with all the parameters
     * @param hostIn
     * @param paygDataIdIn
     */
    public PaygAuthenticationUpdateFailed(String hostIn, Long paygDataIdIn) {
        this.host = hostIn;
        this.paygDataId = paygDataIdIn;
    }

    public String getHost() {
        return host;
    }

    public Long getPaygDataId() {
        return paygDataId;
    }

    @Override
    public String getSummary() {
        return LocalizationService.getInstance().getMessage("notification.paygauthenticationupdatefailed",
                getPaygDataId().toString(), getHost());
    }

    @Override
    public String getDetails() {
        return "";
    }
}
