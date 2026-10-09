/*
 * Copyright (c) 2021 SUSE LLC
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
package com.redhat.rhn.manager.content.ubuntu;

/**
 * Basic package data share between the fields {@code binaries} (which is of type {@link PackageInfo}) and
 * {@code allbinaries} (which is of type {@link Binary})
 */
abstract class BasePackageInfo {
    protected String pocket;
    protected String version;

    public String getPocket() {
        return pocket;
    }

    public String getVersion() {
        return version;
    }
}
