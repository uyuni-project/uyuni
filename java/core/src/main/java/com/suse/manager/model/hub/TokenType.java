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

package com.suse.manager.model.hub;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.Labeled;

public enum TokenType implements Labeled {
    ISSUED,
    CONSUMED;

    @Override
    public String getLabel() {
        return this.name().toLowerCase();
    }

    public String getDescription() {
        return LocalizationService.getInstance().getMessage("hub.tokenType." + this.name().toLowerCase());
    }
}
