/*
 * Copyright (c) 2010--2012 Red Hat, Inc.
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

import com.redhat.rhn.common.localization.LocalizationService;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;


/**
 * OperationsDto
 */
public class OperationDetailsDto {
    private Long id;
    private String description;
    private String status;
    private Date started;
    private Date modified;
    private long serverCount;

    /**
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }

    /**
     * @param idIn The id to set.
     */
    public void setId(Long idIn) {
        id = idIn;
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return Returns the description.
     */
    public String getTranslatedDescription() {
        LocalizationService ls = LocalizationService.getInstance();
        if (ls.hasMessage(getDescription())) {
            return ls.getMessage(getDescription());
        }
        return description;
    }


    /**
     * @param descriptionIn The description to set.
     */
    public void setDescription(String descriptionIn) {
        description = descriptionIn;
    }

    /**
     * @return Returns the status.
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param statusIn The status to set.
     */
    public void setStatus(String statusIn) {
        status = statusIn;
    }

    /**
     * @return Returns the started.
     */
    public Date getStarted() {
        return started;
    }

    /**
     * @param startedIn The started to set.
     */
    public void setStarted(Date startedIn) {
        started = startedIn;
    }

    /**
     * @return Returns the modified.
     */
    public Date getModified() {
        return modified;
    }

    /**
     * @param modifiedIn The modified to set.
     */
    public void setModified(Date modifiedIn) {
        modified = modifiedIn;
    }

    /**
     * @return Returns the serverCount.
     */
    public long getServerCount() {
        return serverCount;
    }

    /**
     * @param serverCountIn The serverCount to set.
     */
    public void setServerCount(long serverCountIn) {
        serverCount = serverCountIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
