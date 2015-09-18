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

package com.redhat.rhn.domain.server.virtualhostmanager;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * VirtualHostManagerConfig - represtentation of a single configuration entry of
 * VirtualHostManager.
 */
public class VirtualHostManagerConfig {

    private Long id;
    private VirtualHostManager virtualHostManager;
    private String parameter;
    private String value;

    /**
     * Gets the id
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the id
     * @param idIn - the new id
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * Gets the associated Virtual Host Manager
     * @return the associated Virtual Host Manager
     */
    public VirtualHostManager getVirtualHostManager() {
        return virtualHostManager;
    }

    /**
     * Sets the Virtual Host Manager
     * @param virtualHostManagerIn - the new virtualHostManager
     */
    public void setVirtualHostManager(VirtualHostManager virtualHostManagerIn) {
        this.virtualHostManager = virtualHostManagerIn;
    }

    /**
     * Gets the config's parameter name
     * @return the config's parameter name
     */
    public String getParameter() {
        return parameter;
    }

    /**
     * Sets the config's parameter name
     * @param parameterIn - the config's parameter name
     */
    public void setParameter(String parameterIn) {
        this.parameter = parameterIn;
    }

    /**
     * Gets the config value
     * @return the config value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the config value
     * @param valueIn - the config value
     */
    public void setValue(String valueIn) {
        this.value = valueIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        VirtualHostManagerConfig that = (VirtualHostManagerConfig) o;

        return new EqualsBuilder()
                .append(virtualHostManager, that.virtualHostManager)
                .append(parameter, that.parameter)
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(virtualHostManager)
                .append(parameter)
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "VirtualHostManagerConfig{" +
                "id=" + id +
                ", virtualHostManager=" + virtualHostManager +
                ", parameter='" + parameter + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
