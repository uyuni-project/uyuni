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
package com.suse.spec.channel.software.dto;

import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.rhnpackage.Package;

import java.util.Set;

/**
 * Internal response from facade sync operations.
 * Contains the erratas and packages that were synced.
 * @param erratas Set of erratas that were merged/cloned
 * @param packages Set of packages that were merged/cloned
 */
public record SyncResponse(
    Set<Errata> erratas,
    Set<Package> packages
) {
}
