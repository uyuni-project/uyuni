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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The optional state_operator attribute provides the logical operator that combines the evaluation results from
 * each referenced state on a per item basis.  Each matching item is compared to each referenced state.
 * <p>
 * The result of comparing each state to a single item is combined based on the specified state_operator value to
 * determine one result for each item. Finally, the results for each item are combined based on the specified
 * check value.
 * <p>
 * Note that if the test does not contain any references to OVAL States, then the state_operator attribute has no
 * meaning and can be ignored during evaluation.
 * <p>
 * Referencing multiple states in one test allows ranges of possible values to be expressed. For example, one state
 * can check that a value greater than 8 is found and another state can check that a value of less than 16 is found.
 * In this example the referenced states are combined with a state_operator = 'AND' indicating that the conditions
 * of all referenced states must be satisfied and that the value must be between 8 AND 16.  The valid state_operation
 * values are explained in the description of the OperatorEnumeration simple type.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TestType", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
public class TestType {

    @XmlAttribute(name = "id", required = true)
    protected String id;
    @XmlAttribute(name = "comment", required = true)
    protected String comment;

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
     *
     * @return the test's id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param valueIn the test id to set
     */
    public void setId(String valueIn) {
        this.id = valueIn;
    }

    /**
     * Gets the value of the comment property.
     *
     * @return the comment value
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the value of the comment property.
     *
     * @param valueIn the comment value to set
     */
    public void setComment(String valueIn) {
        this.comment = valueIn;
    }

    /**
     * Gets the value of the object property.
     *
     * @return the id of the object associated with this test
     */
    public String getObjectRef() {
        if (object == null) {
            throw new IllegalStateException("Objects cannot be null");
        }
        return object.objectRef;
    }

    /**
     * Sets the value of the object property.
     * @param valueIn the object id to set
     */
    public void setObjectRef(String valueIn) {
        ObjectRefType refType = new ObjectRefType();
        refType.setObjectRef(valueIn);
        this.object = refType;
    }

    /**
     * Gets the value of the state property.
     * <p><br>
     * NOTE: Although the OVAL specs says that an OVAL test could have 0 or more states but for the OVAL files that
     * we are consuming, is always 0 or 1 state hence an {@code Optional} is used.
     *
     * @return an {@link Optional} that may or may not contain a state reference
     */
    public Optional<String> getStateRef() {
        if (this.states == null) {
            return Optional.empty();
        }
        else if (this.states.size() == 1) {
            return Optional.ofNullable(states.get(0).getStateRef());
        }
        else {
            throw new IllegalStateException("Each test is expected to have 0 or 1 state");
        }
    }

    public void setStates(List<StateRefType> statesIn) {
        this.states = statesIn;
    }

    /**
     * Sets the associated state id
     *
     * @param valueIn the state id to set
     * */
    public void setStateRef(String valueIn) {
        if (states == null) {
            states = new ArrayList<>();
        }
        states.clear();
        StateRefType stateRef = new StateRefType();
        stateRef.setStateRef(valueIn);
        states.add(stateRef);
    }
}
