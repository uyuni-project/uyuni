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

package com.redhat.rhn.domain.contentmgmt;

import com.redhat.rhn.domain.rhnpackage.Package;

/**
 * Modular package filter that filters out every modular package in a project.
 * <p>This is an internal filter that cannot be created or manipulated by the user.</p>
 */
public class ModularPackageFilter extends PackageFilter {
    /**
     * Initialize a new filter instance
     */
    public ModularPackageFilter() {
        super.setRule(Rule.DENY);
    }

    @Override
    public boolean test(Object o) {
        if (!(o instanceof Package pack)) {
            return false;
        }
        return pack.getPackageEvr().getRelease().contains(".module");
    }

    @Override
    public void setCriteria(FilterCriteria criteriaIn) {
        throw new UnsupportedOperationException("Criteria cannot be set for the modular package filter.");
    }

    @Override
    public void setRule(Rule ruleIn) {
        throw new UnsupportedOperationException("Rule cannot be set for the modular package filter.");
    }
}
