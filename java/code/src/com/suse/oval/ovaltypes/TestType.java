package com.suse.oval.ovaltypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;


/**
 * The optional state_operator attribute provides the logical operator that combines the evaluation results from each referenced state
 * on a per item basis.  Each matching item is compared to each referenced state.
 * <p>
 * The result of comparing each state to a single item is combined based on the specified state_operator value to determine one result
 * for each item. Finally, the results for each item are combined based on the specified check value.  Note that if the test does not contain
 * any references to OVAL States, then the state_operator attribute has no meaning and can be ignored during evaluation.
 * <p>
 * Referencing multiple states in one test allows ranges of possible values to be expressed. For example, one state can check
 * that a value greater than 8 is found and another state can check that a value of less than 16 is found.  In this example
 * the referenced states are combined with a state_operator = 'AND' indicating that the conditions of all referenced states
 * must be satisfied and that the value must be between 8 AND 16.  The valid state_operation values are explained in
 * the description of the OperatorEnumeration simple type.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TestType", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
public class TestType {

    @XmlAttribute(name = "id", required = true)
    protected String id;
    @XmlAttribute(name = "version", required = true)
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger version;
    @XmlAttribute(name = "check_existence")
    protected ExistenceEnum checkExistence;
    @XmlAttribute(name = "state_operator")
    protected LogicOperatorType stateOperator;
    @XmlAttribute(name = "check", required = true)
    protected CheckEnum check;
    @XmlAttribute(name = "comment", required = true)
    protected String comment;
    @XmlAttribute(name = "deprecated")
    protected Boolean deprecated;

    /**
     * These attributes are not specified for the base test type as per the schema; nevertheless, it has been included
     * since both dpkg and rpm test types have it.
     */
    @XmlElement(namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5#linux", required = true)
    protected ObjectRefType object;
    @XmlElement(name = "state", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5#linux")
    protected List<StateRefType> states;

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
     * Gets the value of the checkExistence property.
     */
    public ExistenceEnum getCheckExistence() {
        if (checkExistence == null) {
            return ExistenceEnum.AT_LEAST_ONE_EXISTS;
        } else {
            return checkExistence;
        }
    }

    /**
     * Sets the value of the checkExistence property.
     */
    public void setCheckExistence(ExistenceEnum value) {
        this.checkExistence = value;
    }

    /**
     * Gets the value of the check property.
     */
    public CheckEnum getCheck() {
        return check;
    }

    /**
     * Sets the value of the check property.
     */
    public void setCheck(CheckEnum value) {
        this.check = value;
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

    /**
     * Gets the value of the object property.
     */
    public String getObjectRef() {
        if (object == null) {
            throw new IllegalStateException("Objects cannot be null");
        }
        return object.objectRef;
    }

    /**
     * Sets the value of the object property.
     */
    public void setObjectRef(String value) {
        ObjectRefType refType = new ObjectRefType();
        refType.setObjectRef(value);
        this.object = refType;
    }

    /**
     * Gets the value of the state property.
     * <p>
     * Although the OVAL specs says that an OVAL test could have 0 or more states but for the OVAL files that we're
     * consuming, it's always 0 or 1 state hence an {@code Optional<T>} is used.
     */
    public Optional<String> getStateRef() {
        if (this.states == null) {
            return Optional.empty();
        } else if (this.states.size() == 1) {
            return Optional.ofNullable(states.get(0).getStateRef());
        } else {
            throw new IllegalStateException("Each test is expected to have 0 or 1 state. See the comment above the method");
        }
    }

    public LogicOperatorType getStateOperator() {
        if (stateOperator == null) {
            return LogicOperatorType.AND;
        } else {
            return stateOperator;
        }
    }

    public void setStateOperator(LogicOperatorType stateOperator) {
        this.stateOperator = stateOperator;
    }

    public void setStates(List<StateRefType> states) {
        this.states = states;
    }

    public void setStateRef(String value) {
        states.clear();
        StateRefType stateRef = new StateRefType();
        stateRef.setStateRef(value);
        states.add(stateRef);
    }
}
