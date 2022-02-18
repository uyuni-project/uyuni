/*
 * Copyright (c) 2012--2015 Red Hat, Inc.
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
package com.redhat.rhn.domain.action.scap;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;

import org.apache.commons.lang3.StringEscapeUtils;
/**
 * ScapAction - Class representing TYPE_SCAP_*.
 */
public class ScapAction extends Action {

    private ScapActionDetails scapActionDetails;

    /**
     * @return Returns the scapActionDetails.
     */
    public ScapActionDetails getScapActionDetails() {
        return scapActionDetails;
    }

    /**
     * @param scapActionDetailsIn The scapActionDetails to set.
     */
    public void setScapActionDetails(ScapActionDetails scapActionDetailsIn) {
        scapActionDetailsIn.setParentAction(this);
        scapActionDetails = scapActionDetailsIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHistoryDetails(Server server, User currentUser) {
        LocalizationService ls = LocalizationService.getInstance();
        StringBuilder retval = new StringBuilder();
        retval.append("</br>");
        retval.append(ls.getMessage("system.event.scapPath"));
        retval.append(StringEscapeUtils.escapeHtml4(scapActionDetails.getPath()));
        retval.append("</br>");
        retval.append(ls.getMessage("system.event.scapParams"));
        retval.append(scapActionDetails.getParameters() == null ? "" :
            StringEscapeUtils.escapeHtml4(scapActionDetails.getParametersContents()));
        if (scapActionDetails.getOvalfiles() != null) {
            retval.append("</br>");
            retval.append(ls.getMessage("system.event.scapOvalFiles"));
            retval.append(StringEscapeUtils.escapeHtml4(scapActionDetails.getOvalfiles()));
        }
        return retval.toString();
    }

}
