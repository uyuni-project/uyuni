/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.testing;

import static org.junit.jupiter.api.Assertions.fail;

import com.mockobjects.Expectation;

import org.apache.struts.action.DynaActionForm;
import org.apache.struts.action.DynaActionFormClass;
import org.apache.struts.config.FormBeanConfig;
import org.apache.struts.config.FormPropertyConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * RhnMockDynaActionForm is a mock implementation of
 * a Struts DynaActionForm which allows the user to
 * set expected values during testing.
 */
public class RhnMockDynaActionForm extends DynaActionForm
                                   implements Expectation {

    /** Name of FormBean */
    private String formName;

    /** Map of actual properties which have been set */
    private Map<String, Object> actual;
    /** Map of expected properties to be set */
    private Map<String, Object> expected;

    /**
     * True if we are not to expect anything but simply be a Form;
     * defaults to false
     */
    private boolean expectNothing;

    private Map<String, FormPropertyConfig> formPropertyConfigs;

    /**
     * Create class with a Form Name
     * @param formNameIn the name we want to assign
     */
    public RhnMockDynaActionForm(String formNameIn) {
        this();
        formName = formNameIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getMap() {
        return actual;
    }

    /**
     * Default constructor
     */
    public RhnMockDynaActionForm() {
        super();
        actual = new HashMap<>();
        expected = new HashMap<>();
        expectNothing = false;
        formPropertyConfigs = new HashMap<>();

        // Setup the empty config.
        FormBeanConfig beanConfig = new FormBeanConfig();
        beanConfig.setType(this.getClass().getName());
        DynaActionFormClass theDynaClass = DynaActionFormClass
                .createDynaActionFormClass(beanConfig);
        setDynamicActionFormClass(theDynaClass);
    }

    /**
     * Use this to set the DyanActionFormClass. Needed because the
     * setDynaActionFormClass method is package private in DynaActionForm.
     * @param dynaClass DynaActionFormClass used by this Mock implementation.
     */
    public void setDynamicActionFormClass(DynaActionFormClass dynaClass) {
        this.dynaClass = dynaClass;
    }

    /**
     * Stores the name with the given value.
     * @param name Name to be associated.
     * @param value Value to associate with name.
     */
    @Override
    public void set(String name, Object value) {
        // Nothin to do here if we are inserting a
        // null value.
        if (value == null) {
            return;
        }

        // This set of code below adds the name of this
        // property to the DynaClass that is used by
        // the DynaForm so we can actually treat this
        // Mock Form like a DynaBean.
        if (!formPropertyConfigs.containsKey(name)) {
            FormBeanConfig beanConfig = new FormBeanConfig();

            beanConfig.setName(getFormName());
            beanConfig.setType(this.getClass().getName());
            // Right now we only support Strings as dynamic members of
            // the dynaclass.
            FormPropertyConfig fc =
                new FormPropertyConfig(name, "java.lang.String", value.toString());
            formPropertyConfigs.put(name, fc);

            // Get existing properties as well as new one
            // and add it to the config.
            for (FormPropertyConfig config : formPropertyConfigs.values()) {
                beanConfig.addFormPropertyConfig(config);
            }

            // Construct a corresponding DynaActionFormClass
            DynaActionFormClass theDynaClass =
                 DynaActionFormClass.createDynaActionFormClass(beanConfig);
            setDynamicActionFormClass(theDynaClass);
        }
        // Add the actual value
        actual.put(name, value);
    }

    /**
     * Adds a property that is expected to be set via the set method.
     * @param name Property name
     * @param value Property value
     */
    public void addExpectedProperty(String name, Object value) {
        if (!expectNothing) {
            expected.put(name, value);
        }
    }

    /**
     * Returns the value mapped to name or null if not found.
     * @param name Property whose value you seek.
     * @return Object value found for given name.
     */
    @Override
    public Object get(String name) {
        return actual.get(name);
    }

    /**
     * Verifies the object received the expected values.
     */
    @Override
    public void verify() {
        // need to compare the values in the expected list with
        // those of the actual list.

        Set<String> keys = expected.keySet();
        for (String key : keys) {
            Object expValue = expected.get(key);
            Object actValue = actual.get(key);

            if (expValue == null) {
                throw new NullPointerException("Expected value of [" + key + "] is null.");
            }

            if (!expValue.equals(actValue)) {
                String msg = "Did not receive expected values.\n" + "key [" +
                        key +
                        "] expected value [" +
                        expValue +
                        "] actual value [" +
                        actValue +
                        "]";
                fail(msg);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasExpectations() {
        return !expected.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setExpectNothing() {
        expectNothing = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFailOnVerify() {
        // do nothing
    }

    /**
     * Get the formName
     * @return Returns the formName.
     */
    public String getFormName() {
        if (formName == null) {
            return "dynaForm";
        }
        return formName;
    }
    /**
     * Set the FormName
     * @param formNameIn The formName to set.
     */
    public void setFormName(String formNameIn) {
        this.formName = formNameIn;
    }
}
