/*
 * Copyright (c) 2025 SUSE LLC
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

package com.redhat.rhn.domain.action.config;

import com.redhat.rhn.domain.action.Action;

import java.io.Serializable;
import java.util.Objects;

public class ConfigDateFileActionId implements Serializable {
    private String fileName;
    private Action parentAction;

    /**
     * Constructor
     */
    public ConfigDateFileActionId() { }

    /**
     * Constructor
     * @param fileNameIn the file name
     * @param parentActionIn the parent action
     */
    public ConfigDateFileActionId(String fileNameIn, Action parentActionIn) {
        fileName = fileNameIn;
        parentAction = parentActionIn;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileNameIn) {
        fileName = fileNameIn;
    }

    public Action getParentAction() {
        return parentAction;
    }

    public void setParentAction(Action parentActionIn) {
        parentAction = parentActionIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (!(oIn instanceof ConfigDateFileActionId that)) {
            return false;
        }
        return Objects.equals(fileName, that.fileName) &&
                Objects.equals(parentAction, that.parentAction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, parentAction);
    }
}
