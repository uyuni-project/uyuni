/*
 * Copyright (c) 2024 SUSE LLC
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

package com.redhat.rhn.domain.channel;

import org.apache.commons.lang3.builder.EqualsBuilder;

import java.io.Serializable;

public class AppStreamApiKey implements Serializable {
    private Long id;
    private String rpm;

    @Override
    public int hashCode() {
        return rpm.hashCode() + id.intValue();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof AppStreamApiKey key)) {
            return false;
        }
        return new EqualsBuilder().append(id, key.id).append(rpm, key.rpm).isEquals();
    }
}
