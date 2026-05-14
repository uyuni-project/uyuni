/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
 * Copyright (c) 2026 SUSE LLC
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
package com.redhat.rhn.frontend.dto;

/**
 * SecurityErrataOverview is a marker subclass kept for backwards compatibility
 * with the {@code *_with_cves} datasource modes. The CVE row-callback logic
 * lives on {@link ErrataOverview} so any consumer of that DTO that runs the
 * {@code errata_cves_elab} elaborator has its CVE list populated.
 */
public class SecurityErrataOverview extends ErrataOverview {
}
