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
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

import java.util.ArrayList;
import java.util.List;


/**
 * The required operator attribute provides the logical operator that binds the different statements inside a criteria
 * together. The optional negate attribute signifies that the result of the criteria as a whole should be negated
 * during analysis.
 * <p>
 * For example, consider a criteria that evaluates to TRUE if certain software is installed.
 * <p>
 * By negating this test, it now evaluates to TRUE if the software is NOT installed. The optional comment attribute
 * provides a short description of the criteria.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CriteriaType", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
public class CriteriaType implements BaseCriteria {

    @XmlElements({
        @XmlElement(name = "criteria",
                namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5", type = CriteriaType.class),
        @XmlElement(name = "criterion",
                namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5", type = CriterionType.class)
    })
    protected List<BaseCriteria> children;
    @XmlAttribute(name = "operator")
    protected LogicOperatorType operator;
    @XmlAttribute(name = "negate")
    protected Boolean negate;
    @XmlAttribute(name = "comment")
    protected String comment;

    /**
     * Gets the value of the contained criteria or criterion objects.
     *
     * @return the list of criteria or criterion children under this criteria
     */
    public List<BaseCriteria> getChildren() {
        if (children == null) {
            children = new ArrayList<>();
        }
        return this.children;
    }

    /**
     * Gets the value of the operator property.
     *
     * @return the operator property or {@link LogicOperatorType.AND} if none is specified
     */
    public LogicOperatorType getOperator() {
        if (operator == null) {
            return LogicOperatorType.AND;
        }
        else {
            return operator;
        }
    }

    /**
     * Sets the value of the operator property.
     *
     * @param value the operator property to set
     */
    public void setOperator(LogicOperatorType value) {
        this.operator = value;
    }

    /**
     * Gets the value of the negate property.
     *
     * @return a boolean that indicates whether to negate result after evaluation or {@code false} if none is specified
     */
    public boolean isNegate() {
        if (negate == null) {
            return false;
        }
        else {
            return negate;
        }
    }

    /**
     * Sets the value of the negate property.
     *
     * @param value a boolean that indicates whether to negate result after evaluation or not
     */
    public void setNegate(Boolean value) {
        this.negate = value;
    }

    /**
     * Gets the value of the comment property.
     *
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the value of the comment property.
     *
     * @param value the comment to set
     */
    public void setComment(String value) {
        this.comment = value;
    }
}
