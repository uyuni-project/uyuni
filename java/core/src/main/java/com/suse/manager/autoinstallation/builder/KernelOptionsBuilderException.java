/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.autoinstallation.builder;

import com.redhat.rhn.common.RhnRuntimeException;

/**
 * Exception indicating an error during kernel options building.
 */
public class KernelOptionsBuilderException extends RhnRuntimeException {

    /**
     * Create a new KernelOptionsBuilderException.
     *
     * @param message error message
     */
    public KernelOptionsBuilderException(String message) {
        super(message);
    }

    /**
     * Create a new KernelOptionsBuilderException with message and cause.
     *
     * @param message error message
     * @param cause original cause
     */
    public KernelOptionsBuilderException(String message, Throwable cause) {
        super(message, cause);
    }
}
