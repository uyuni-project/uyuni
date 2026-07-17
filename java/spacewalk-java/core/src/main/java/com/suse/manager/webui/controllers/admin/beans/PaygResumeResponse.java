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

package com.suse.manager.webui.controllers.admin.beans;

import com.redhat.rhn.domain.cloudpayg.PaygSshData;

/**
 * JSON response wrapper for the properties of a content project.
 */
public class PaygResumeResponse {

    private String id;
    private String host;
    private String description;
    private PaygSshData.Status status;
    private String statusMessage;
    private String lastChange;

    public String getId() {
        return id;
    }

    public void setId(String idIn) {
        this.id = idIn;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String hostIn) {
        this.host = hostIn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String descriptionIn) {
        this.description = descriptionIn;
    }

    public PaygSshData.Status getStatus() {
        return status;
    }

    public void setStatus(PaygSshData.Status statusIn) {
        this.status = statusIn;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessageIn) {
        this.statusMessage = statusMessageIn;
    }

    public String getLastChange() {
        return lastChange;
    }

    public void setLastChange(String lastChangeIn) {
        this.lastChange = lastChangeIn;
    }
}
