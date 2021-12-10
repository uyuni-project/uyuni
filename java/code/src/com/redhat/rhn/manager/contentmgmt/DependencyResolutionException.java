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

import com.redhat.rhn.domain.contentmgmt.modulemd.Module;

import java.util.Optional;

/**
 * Exception thrown when CLM dependency resolution fails
 */
public class DependencyResolutionException extends Exception {

    private Module module;

    /**
     * Initialize exception with a message
     *
     * @param message the exception message
     */
    public DependencyResolutionException(String message) {
        super(message);
    }

    /**
     * Initialize exception with a message and a cause
     *
     * @param message the exception message
     * @param cause the inner cause
     */
    public DependencyResolutionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Initialize exception with a module object
     *
     * @param moduleIn the problematic module object
     */
    public DependencyResolutionException(Module moduleIn) {
        this.module = moduleIn;
    }

    /**
     * Get the problematic module, if any exists
     *
     * @return the module instance
     */
    public Optional<Module> getModule() {
        return Optional.ofNullable(module);
    }
}
