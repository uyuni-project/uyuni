/**
 * Copyright (c) 2015 SUSE LLC
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
        if (this.parameter == null) {
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
        if (this.parameter == null) {
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
