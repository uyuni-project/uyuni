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

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * DTO representing a CLM project build history entry.
 */
public class ClmBuildHistoryDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long version;
    private Date created;
    private String message;
    private String userName;
    private boolean automated;
    private boolean current;

    /**
     * @return the version
     */
    public Long getVersion() {
        return version;
    }

    /**
     * @param versionIn the version
     */
    public void setVersion(Long versionIn) {
        this.version = versionIn;
    }

    /**
     * @return the creation date
     */
    public Date getCreated() {
        return created;
    }

    /**
     * @param createdIn the creation date
     */
    public void setCreated(Date createdIn) {
        this.created = createdIn;
    }

    /**
     * @return the build message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param messageIn the build message
     */
    public void setMessage(String messageIn) {
        this.message = messageIn;
    }

    /**
     * @return the user login
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userNameIn the user login
     */
    public void setUserName(String userNameIn) {
        this.userName = userNameIn;
    }

    /**
     * @return true if automated from recurring action
     */
    public boolean isAutomated() {
        return automated;
    }

    /**
     * @param automatedIn whether automated
     */
    public void setAutomated(boolean automatedIn) {
        this.automated = automatedIn;
    }

    /**
     * @return true if this version is currently in the target environment
     */
    public boolean isCurrent() {
        return current;
    }

    /**
     * @param currentIn whether current
     */
    public void setCurrent(boolean currentIn) {
        this.current = currentIn;
    }
}
