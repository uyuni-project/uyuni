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

package com.redhat.rhn.domain.contentmgmt.modulemd;

import java.util.List;

/**
 * modulemd API response from 'module_packages' call
 */
public class ModulePackagesResponse {

    private List<String> apis;
    private List<String> packages;
    private List<ModuleInfo> selected;

    /**
     * Initialize a new instance
     *
     * @param apisIn the 'rpm apis' to be selected
     * @param packagesIn the 'rpm artifacts' to be selected
     * @param selectedIn the details of the modules to be selected
     */
    public ModulePackagesResponse(List<String> apisIn, List<String> packagesIn, List<ModuleInfo> selectedIn) {
        this.apis = apisIn;
        this.packages = packagesIn;
        this.selected = selectedIn;
    }

    public List<String> getRpmApis() {
        return apis;
    }

    public List<String> getRpmPackages() {
        return packages;
    }

    public List<ModuleInfo> getSelected() {
        return selected;
    }
}
