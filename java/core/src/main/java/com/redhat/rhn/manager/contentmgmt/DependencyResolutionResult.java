/*
 * Copyright (c) 2020 SUSE LLC
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

package com.redhat.rhn.manager.contentmgmt;

import com.redhat.rhn.domain.contentmgmt.ContentFilter;
import com.redhat.rhn.domain.contentmgmt.modulemd.Module;

import java.util.List;

/**
 * Class to report the result of a modular dependency resolution
 *
 * Modular dependency resolution involves discovery of additional dependent modules and mutating a set of module filters
 * into resolved package filters on-the-go. An instance of this class stores information on this process.
 */
public class DependencyResolutionResult {
    private List<ContentFilter> filters;
    private List<Module> modules;

    /**
     * Create an instance with a list of filters and a list of modules
     * @param filtersIn the filter list
     * @param modulesIn the module list
     */
    public DependencyResolutionResult(List<ContentFilter> filtersIn, List<Module> modulesIn) {
        this.filters = filtersIn;
        this.modules = modulesIn;
    }

    public List<ContentFilter> getFilters() {
        return filters;
    }

    public List<Module> getModules() {
        return modules;
    }
}
