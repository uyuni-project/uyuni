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
package com.redhat.rhn.domain.server;

/**
 * Mode indicated by the Salt transactional grain.
 */
public enum TransactionalMode {
    UNKNOWN,
    NON_TRANSACTIONAL,
    TRANSACTIONAL;

    /**
     * Resolve the effective transactional classification of a system.
     *
     * A valid observation of the grain always wins over the operating system name, because
     * a single distribution (i.e. SLES 16.1) can be installed in both transactional and
     * conventional mode. As long as no valid observation has been collected the mode is
     * {@link #UNKNOWN} and the classification by the operating system name is used as a
     * fallback.
     *
     * @param osName the operating system name
     * @return <code>true</code> if the system must be treated as transactional
     */
    public boolean isTransactional(String osName) {
        return switch (this) {
            case TRANSACTIONAL -> true;
            case NON_TRANSACTIONAL -> false;
            case UNKNOWN -> ServerConstants.isTransactionalByOsName(osName);
        };
    }
}
