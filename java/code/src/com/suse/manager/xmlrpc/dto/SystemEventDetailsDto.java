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
package com.suse.manager.xmlrpc.dto;

import com.redhat.rhn.frontend.dto.SystemEventDto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * DTO for the details of a history event linked with an action
 */
public class SystemEventDetailsDto extends SystemEventDto implements Serializable {

    private static final long serialVersionUID = 2847370808086390658L;

    private Date earliestAction;

    private String resultMsg;

    private Long resultCode;

    private List<Map<String, String>> additionalInfo;

    /**
     * Default constructor.
     */
    public SystemEventDetailsDto() {
        additionalInfo = new ArrayList<>();
    }

    public Date getEarliestAction() {
        return earliestAction;
    }

    public void setEarliestAction(Date earliestActionIn) {
        this.earliestAction = earliestActionIn;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsgIn) {
        this.resultMsg = resultMsgIn;
    }

    public Long getResultCode() {
        return resultCode;
    }

    public void setResultCode(Long resultCodeIn) {
        this.resultCode = resultCodeIn;
    }

    public List<Map<String, String>> getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(List<Map<String, String>> additionalInfoIn) {
        this.additionalInfo = additionalInfoIn;
    }

    /**
     * Check if this event has the additional information specific for the event type.
     *
     * @return <code>true</code> when there are additional pieces of information thus {@link #getAdditionalInfo()} will
     * return a collection with at least one valid entry.
     */
    public boolean hasAdditionInfo() {
        return additionalInfo != null && !additionalInfo.isEmpty();
    }
}
