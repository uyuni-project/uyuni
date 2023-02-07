/*
 * Copyright (c) 2014 Red Hat, Inc.
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
package com.redhat.rhn.domain.org.usergroup;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.org.Org;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


/**
 * ExtGroup
 */
public abstract class ExtGroup extends BaseDomainHelper implements Comparable<ExtGroup> {

    private Long id;
    private String label;
    private Org org;

    /**
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }

    /**
     * @param idIn The id to set.
     */
    public void setId(Long idIn) {
        id = idIn;
    }

    /**
     * @return Returns the label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param labelIn The label to set.
     */
    public void setLabel(String labelIn) {
        label = labelIn;
    }

    /**
     * @return Returns the org.
     */
    public Org getOrg() {
        return org;
    }

    /**
     * @param orgIn The org to set.
     */
    public void setOrg(Org orgIn) {
        org = orgIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(ExtGroup objectIn) {
        if (objectIn instanceof UserExtGroup) {
            return 0;
        }
        return id.compareTo(objectIn.getId());
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).append(label).append(org).toHashCode();
    }

    @Override
    public boolean equals(Object oIn) {
        if (this == oIn) {
            return true;
        }

        if (!(oIn instanceof ExtGroup)) {
            return false;
        }

        ExtGroup extGroup = (ExtGroup) oIn;

        return new EqualsBuilder().append(id, extGroup.id)
                .append(label, extGroup.label).append(org, extGroup.org).isEquals();
    }
}
