/**
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

/**
 * Exception thrown when a selected module is not found
 */
public class ModuleNotFoundException extends Exception {

    private Module module;

    /**
     * Initialize a new instance
     * @param moduleIn the selected module
     */
    public ModuleNotFoundException(Module moduleIn) {
        this.module = moduleIn;
    }

    public Module getModule() {
        return module;
    }
}
