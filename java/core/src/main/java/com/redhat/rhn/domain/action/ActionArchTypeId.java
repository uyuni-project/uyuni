/*
 * Copyright (c) 2025 SUSE LLC
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
package com.redhat.rhn.domain.action;

import org.apache.commons.lang3.builder.EqualsBuilder;

import java.io.Serializable;

public class ActionArchTypeId implements Serializable {
    private Long archTypeId;
    private String actionStyle;

    /**
     * Constructor
     */
    public ActionArchTypeId() { }

    /**
     * Constructor
     * @param archTypeIdIn the arch type id
     * @param actionStyleIn the action style
     */
    public ActionArchTypeId(Long archTypeIdIn, String actionStyleIn) {
        archTypeId = archTypeIdIn;
        actionStyle = actionStyleIn;
    }

    @Override
    public int hashCode() {
        return archTypeId.hashCode() + archTypeId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ActionArchTypeId other)) {
            return false;
        }
        return new EqualsBuilder()
                .append(archTypeId, other.archTypeId)
                .append(actionStyle, other.actionStyle).isEquals();
    }
}
