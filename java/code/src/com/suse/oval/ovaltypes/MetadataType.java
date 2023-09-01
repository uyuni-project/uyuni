/*
 * Copyright (c) 2023 SUSE LLC
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

package com.suse.oval.ovaltypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * Additional metadata is also allowed, although it is not part of the official OVAL Schema.
 * Individual organizations can place metadata items that they feel are important and these will be skipped during
 * the validation.
 * <p>
 * All OVAL really cares about is that the stated metadata items are there.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MetadataType", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
public class MetadataType {

    @XmlElement(namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5", required = true)
    protected String title;
    @XmlElement(namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
    protected List<ReferenceType> reference;
    @XmlElement(namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5", required = true)
    protected String description;
    @XmlElement(name = "advisory", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
    protected Advisory advisory;

    /**
     * Gets the value of the title property.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     *
     * @param valueIn the title to set
     */
    public void setTitle(String valueIn) {
        this.title = valueIn;
    }

    /**
     * Gets the value of the reference property.
     *
     * @return the list of associated references
     */
    public List<ReferenceType> getReferences() {
        if (reference == null) {
            reference = new ArrayList<>();
        }
        return this.reference;
    }

    /**
     * Gets the value of the description property.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     *
     * @param valueIn the description to set
     */
    public void setDescription(String valueIn) {
        this.description = valueIn;
    }

    public Optional<Advisory> getAdvisory() {
        return Optional.ofNullable(advisory);
    }

    public void setAdvisory(Advisory advisoryIn) {
        this.advisory = advisoryIn;
    }
}
