package com.suse.oval.ovaltypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * The required test_ref attribute is the actual id of the test being referenced. The optional negate attribute signifies
 * that the result of an individual test should be negated during analysis. For example, consider a test that evaluates to TRUE
 * if a specific patch is installed. By negating this test, it now evaluates to TRUE if the patch is NOT installed.
 * <p>
 * The optional comment attribute provides a short description of the specified test and should mirror the comment
 * attribute of the actual test.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CriterionType", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
public class CriterionType implements BaseCriteria {

    @XmlAttribute(name = "test_ref", required = true)
    protected String testRef;
    @XmlAttribute(name = "negate")
    protected Boolean negate;
    @XmlAttribute(name = "comment")
    protected String comment;

    /**
     * Gets the value of the testRef property.
     */
    public String getTestRef() {
        return testRef;
    }

    /**
     * Sets the value of the testRef property.
     */
    public void setTestRef(String value) {
        this.testRef = value;
    }

    /**
     * Gets the value of the negate property.
     */
    public boolean isNegate() {
        if (negate == null) {
            return false;
        } else {
            return negate;
        }
    }

    /**
     * Sets the value of the negate property.
     */
    public void setNegate(Boolean value) {
        this.negate = value;
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
}
