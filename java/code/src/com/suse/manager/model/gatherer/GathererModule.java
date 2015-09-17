package com.suse.manager.model.gatherer;

import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a Gatherer Module class parsed from JSON
 */
public class GathererModule {

    private String name;
    private Map<String, String> parameter;

    /**
     * Default Constructor
     */
    public GathererModule() {
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param nameIn the name to set
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * @return the parameter
     */
    public Map<String, String> getParameter() {
        return parameter;
    }

    /**
     * @param parameterIn the parameter to set
     */
    public void setParameter(Map<String, String> parameterIn) {
        if(this.parameter == null) {
            this.parameter = new HashMap<String, String>();
        }
        this.parameter = parameterIn;
    }

    /**
     * Add a paramter to the map
     *
     * @param key the key
     * @param value the value
     */
    public void addParameter(String key, String value) {
        if(this.parameter == null) {
            this.parameter = new HashMap<String, String>();
        }
        this.parameter.put(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).toHashCode();
    }

    /**
     * {@inheritDoc}
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
        GathererModule other = (GathererModule) obj;
        if (name != other.name) {
            return false;
        }
        return true;
    }
}
