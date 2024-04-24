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
package com.redhat.rhn.domain.action.appstream;

import com.redhat.rhn.domain.action.Action;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class AppStreamAction extends Action {

    private static final long serialVersionUID = 1L;
    private Set<AppStreamActionDetails> details = new HashSet<>();

    public Set<AppStreamActionDetails> getDetails() {
        return details;
    }

    /**
     * Sets the details for this AppStreamAction.
     *
     * @param detailsIn the Set of AppStreamActionDetails to be set
     */
    public void setDetails(Set<AppStreamActionDetails> detailsIn) {
        if (detailsIn != null) {
            details = new HashSet<>(detailsIn);
            details.forEach(d -> d.setParentAction(this));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object oIn) {
        if (this == oIn) return true;

        if (oIn == null || getClass() != oIn.getClass()) return false;

        AppStreamAction that = (AppStreamAction) oIn;

        return new EqualsBuilder().appendSuper(super.equals(oIn)).append(details, that.details).isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).appendSuper(super.hashCode()).append(details).toHashCode();
    }
}
