/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.suse.manager.webui.utils.gson;

import com.redhat.rhn.manager.audit.SystemCVEResult;

import java.util.List;

/**
 * JSON row of the system CVE list page
 */
public class SystemCVEJson {

    private final long id;
    private final String cveName;
    private final String status;
    private final String severity;
    private final List<SystemCVEResult.Errata> errata;

    /**
     * @param result the manager result to convert
     */
    public SystemCVEJson(SystemCVEResult result) {
        id = result.getId();
        cveName = result.getCveName();
        status = result.getStatus().name();
        severity = result.getSeverity().name();
        errata = result.getErrata();
    }

    /**
     * @return the CVE id
     */
    public long getId() {
        return id;
    }

    /**
     * @return the CVE name
     */
    public String getCveName() {
        return cveName;
    }

    /**
     * @return the status name
     */
    public String getStatus() {
        return status;
    }

    /**
     * @return the severity name
     */
    public String getSeverity() {
        return severity;
    }

    /**
     * @return the advisories containing the CVE
     */
    public List<SystemCVEResult.Errata> getErrata() {
        return errata;
    }
}
