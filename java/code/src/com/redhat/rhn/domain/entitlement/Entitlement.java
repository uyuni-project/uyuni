/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.domain.entitlement;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.server.Server;

import com.suse.manager.reactor.utils.ValueMap;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Entitlements
 */
public abstract class Entitlement implements Comparable<Entitlement> {
    private String label;

    /**
     * Constructs an Entitlement labeled <code>lbl</code>.
     * @param lbl Entitlement label.
     */
    protected Entitlement(String lbl) {
        label = lbl;
    }

    /**
     * Returns the Entitlement label
     * @return the Entitlement label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns whether or not an Entitlement is permanent.
     * @return true if entitlement is permanent, false otherwise
     */
    public abstract boolean isPermanent();

    /**
     * Returns human readable version of the entitlement's label
     * @return human readable version of the entitlement's label
     */
    public String getHumanReadableLabel() {
        LocalizationService ls = LocalizationService.getInstance();

        return ls.getMessage(getLabel());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
            return new ToStringBuilder(this).append("label", label).toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Entitlement other) {
        return new CompareToBuilder().append(label, other.label).toComparison();
    }

    /**
     * @return true if this is a base entitlement..
     */
    public abstract boolean isBase();


    /**
     * Returns a human readable version of an entitlement type (base/add on)
     * @return human readable version of an entitlement type
     */
    public String getHumanReadableTypeLabel() {
        LocalizationService ls = LocalizationService.getInstance();
        if (isBase()) {
            return ls.getMessage("base");
        }
        return ls.getMessage("add-on");
    }

    /**
     * Check to see if the entitlement can be applied to a specific server.
     *
     * @param server to check
     * @return boolean if the entitlement is compatible with the specified server.
     */
    public boolean isAllowedOnServer(Server server) {
        if (server.getBaseEntitlement() instanceof ForeignEntitlement ||
                server.getBaseEntitlement() instanceof BootstrapEntitlement) {
            // no addon entitlement allowed for these
            return false;
        }

        return true;
    }

    /**
     * Check to see if the entitlement can be applied to a specific server.
     *
     * @param server to check
     * @param grains to check
     * @return boolean if the entitlement is compatible with the specified server.
     */
    public boolean isAllowedOnServer(Server server, ValueMap grains) {
        if (server.getBaseEntitlement() instanceof ForeignEntitlement ||
                server.getBaseEntitlement() instanceof BootstrapEntitlement) {
            // no addon entitlement allowed for these
            return false;
        }

        return true;
    }
}
