/**
 * Copyright (c) 2016 SUSE LLC
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
package com.redhat.rhn.domain.action.salt.inspect; import static org.junit.jupiter.api.Assertions.*;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFormatter;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;

/**
 * ImageInspectAction
 */
public class ImageInspectAction extends Action {

    private ImageInspectActionDetails details;

    /**
     * Return the details.
     * @return details
     */
    public ImageInspectActionDetails getDetails() {
        return details;
    }

    /**
     * Set the details.
     * @param detailsIn details
     */
    public void setDetails(ImageInspectActionDetails detailsIn) {
        if (detailsIn != null) {
            detailsIn.setParentAction(this);
        }
        this.details = detailsIn;
    }

    @Override
    public ActionFormatter getFormatter() {
        if (formatter == null) {
            formatter = new ImageInspectActionFormatter(this);
        }
        return formatter;
    }

    @Override
    public String getHistoryDetails(Server server, User currentUser) {
        // LocalizationService ls = LocalizationService.getInstance();
        StringBuilder retval = new StringBuilder();
        retval.append("</br>");
        for (ImageInspectActionResult result : getDetails().getResults()) {
            if (result.getServerId().equals(server.getId())) {
                retval.append("Results:");
                retval.append("</br>");
                retval.append("<pre>");
                retval.append("");
                retval.append("</pre>");
            }
        }
        return retval.toString();
    }
}
