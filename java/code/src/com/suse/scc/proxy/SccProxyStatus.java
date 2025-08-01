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

package com.suse.scc.proxy;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.Labeled;

public enum SccProxyStatus implements Labeled {
    SCC_CREATION_PENDING,
    SCC_CREATED,
    SCC_REMOVAL_PENDING,
    SCC_VIRTHOST_PENDING;

    @Override
    public String getLabel() {
        return this.name().toLowerCase();
    }

    public String getDescription() {
        return LocalizationService.getInstance().getMessage("proxy.status." + this.name().toLowerCase());
    }
}
