/*
 * Copyright (c) 2013 SUSE LLC
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
package com.redhat.rhn.manager.audit;

/**
 * Class encapsulating an errata's id and advisory.
 *
 */
public class ErrataIdAdvisoryPair {

    private long id;
    private String advisory;

    /**
     * Constructor.
     *
     * @param idIn errata id
     * @param advisoryIn errata advisory
     */
    public ErrataIdAdvisoryPair(long idIn, String advisoryIn) {
        super();
        this.id = idIn;
        this.advisory = advisoryIn;
    }

    /**
     * Return the errata Id.
     *
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * Set the errata Id.
     *
     * @param idIn the id to set
     */
    public void setId(long idIn) {
        this.id = idIn;
    }

    /**
     * Return the errata advisory.
     *
     * @return the advisory
     */
    public String getAdvisory() {
        return advisory;
    }

    /**
     * Set the errata advisory.
     *
     * @param advisoryIn the advisory to set
     */
    public void setAdvisory(String advisoryIn) {
        this.advisory = advisoryIn;
    }

    /**
     * @return  a hash code value for this object.
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    /**
     * @param   obj   the reference object with which to compare.
     * @return  {@code true} if this object is the same as the obj
     *          argument; {@code false} otherwise.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ErrataIdAdvisoryPair other = (ErrataIdAdvisoryPair) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }
}
