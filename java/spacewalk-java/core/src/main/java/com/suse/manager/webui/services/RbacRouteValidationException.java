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
package com.suse.manager.webui.services;

/**
 * Exception thrown if RBAC route validation fails on startup.
 */
public class RbacRouteValidationException extends RuntimeException {
    /**
     * Constructs a RbacRouteValidationException
     */
    public RbacRouteValidationException() {
        super("RBAC data validation failed. Please ensure all defined endpoints have a corresponding RBAC mapping.");
    }
}
