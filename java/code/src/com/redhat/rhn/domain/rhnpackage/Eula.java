/**
 * Copyright (c) 2014 SUSE
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
package com.redhat.rhn.domain.rhnpackage;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.BaseDomainHelper;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * POJO for a rhnActionChain row.
 * @author Silvio Moioli <smoioli@suse.de>
 */
public class Eula extends BaseDomainHelper {

    /** The id. */
    private Long id;

    /** The text. */
    private byte[] text;

    /** The checksum. */
    private String checksum;

    /**
     * Default constructor.
     */
    public Eula() {
    }

    /**
     * Gets the id.
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the id.
     * @param idIn the new id
     */
    public void setId(Long idIn) {
        id = idIn;
    }

    /**
     * Gets the text.
     * @return the text
     */
    public byte[] getText() {
        return text;
    }

    /**
     * Gets the text as a string.
     * @return the text
     */
    public String getTextString() {
        return HibernateFactory.getByteArrayContents(getText());
    }

    /**
     * Sets the text.
     * @param textIn the new text
     */
    public void setText(byte[] textIn) {
        text = textIn;
    }

    /**
     * Gets the checksum.
     * @return the checksum
     */
    public String getChecksum() {
        return checksum;
    }

    /**
     * Sets the checksum.
     * @param checksumIn the new checksum
     */
    public void setChecksum(String checksumIn) {
        checksum = checksumIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Eula)) {
            return false;
        }
        Eula otherEula = (Eula) other;
        return new EqualsBuilder()
            .append(getChecksum(), otherEula.getChecksum())
            .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getChecksum())
            .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
        .append("id", getId())
        .append("checksum", getChecksum())
        .toString();
    }
}
