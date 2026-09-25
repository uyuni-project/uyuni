/*
 * Copyright (c) 2022 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.rhnpackage;

/**
 * Constants for tracking special package capabilities used by the application logic
 */
public final class SpecialCapabilityNames {

    /** Capability provided by PTF master packages */
    public static final String PTF = "ptf()";

    /** Capability provided by packages part of a PTF */
    public static final String PTF_PACKAGE = "ptf-package()";

    private SpecialCapabilityNames() {
        // Prevent instantiation
    }
}
