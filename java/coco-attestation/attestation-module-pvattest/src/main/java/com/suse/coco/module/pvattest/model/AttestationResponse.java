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
package com.suse.coco.module.pvattest.model;

public record AttestationResponse(String attestationResponseContent) {
    /**
     * @return true if attestation response has succeeded
     */
    public boolean succeeded() {
        return null != attestationResponseContent;
    }

    /**
     * @return true if attestation response has failed
     */
    public boolean failed() {
        return null == attestationResponseContent;
    }
}
