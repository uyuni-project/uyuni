/**
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.domain;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * AbstractLabelNameHelper - We have a set of tables in our schema
 * that are basic name/label pairs.  This class can be used by these tables
 * to represent them.
 *
 * @version $Rev$
 */
public class AbstractLabelNameHelper extends BaseDomainHelper {

    private Long id;
    private String label;
    private String name;


    /**
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }

    /**
     * @param i The id to set.
     */
    public void setId(Long i) {
        this.id = i;
    }

    /**
     * @return Returns the label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param l The label to set.
     */
    public void setLabel(String l) {
        this.label = l;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param n The name to set.
     */
    public void setName(String n) {
        this.name = n;
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return new HashCodeBuilder().append(this.getId())
                                    .append(this.getName())
                                    .append(this.getLabel())
                                    .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object oth) {
        if (!(oth instanceof AbstractLabelNameHelper)) {
            return false;
        }
        AbstractLabelNameHelper other = (AbstractLabelNameHelper) oth;
        return new EqualsBuilder().append(this.getId(), other.getId())
                                  .append(this.getName(), other.getName())
                                  .append(this.getLabel(), other.getLabel())
                                  .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
       StringBuilder sb = new StringBuilder();
       sb.append(getClass().getName());
       sb.append(" : id: ");
       sb.append(getId());
       return sb.toString();
    }

 }
