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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import java.math.BigInteger;
import java.util.Optional;


/**
 * When evaluating a particular state against an object, one should evaluate each individual entity separately.
 * The individual results are then combined by the operator to produce an overall result.
 * <p>
 * This process holds true even when there are multiple instances of the same entity. Evaluate each instance separately,
 * taking the entity check attribute into account, and then combine everything using the operator.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StateType", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
public class StateType {

    @XmlAttribute(name = "id", required = true)
    protected String id;
    @XmlAttribute(name = "version", required = true)
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger version;
    @XmlAttribute(name = "operator")
    protected LogicOperatorType operator;
    @XmlAttribute(name = "comment")
    protected String comment;
    @XmlAttribute(name = "deprecated")
    protected Boolean deprecated;
    @XmlElement(name = "evr", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5#linux")
    protected EVRType packageEVR;
    @XmlElement(name = "arch", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5#linux")
    protected ArchType packageArch;
    @XmlElement(name = "version", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5#linux")
    protected VersionType packageVersion;


    /**
     * Gets the value of the id property.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the version property.
     */
    public BigInteger getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     */
    public void setVersion(BigInteger value) {
        this.version = value;
    }

    /**
     * Gets the value of the operator property.
     */
    public LogicOperatorType getOperator() {
        if (operator == null) {
            return LogicOperatorType.AND;
        } else {
            return operator;
        }
    }

    /**
     * Sets the value of the operator property.
     */
    public void setOperator(LogicOperatorType value) {
        this.operator = value;
    }

    /**
     * Gets the value of the comment property.
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the value of the comment property.
     */
    public void setComment(String value) {
        this.comment = value;
    }

    /**
     * Gets the value of the deprecated property.
     */
    public boolean isDeprecated() {
        if (deprecated == null) {
            return false;
        } else {
            return deprecated;
        }
    }

    /**
     * Sets the value of the deprecated property.
     */
    public void setDeprecated(Boolean value) {
        this.deprecated = value;
    }

    public Optional<EVRType> getPackageEVR() {
        return Optional.ofNullable(packageEVR);
    }

    public void setPackageEVR(EVRType packageEVR) {
        this.packageEVR = packageEVR;
    }

    public Optional<ArchType> getPackageArch() {
        return Optional.ofNullable(packageArch);
    }

    public void setPackageArch(ArchType packageArch) {
        this.packageArch = packageArch;
    }

    public Optional<VersionType> getPackageVersion() {
        return Optional.ofNullable(packageVersion);
    }

    public void setPackageVersion(VersionType packageVersion) {
        this.packageVersion = packageVersion;
    }
}
