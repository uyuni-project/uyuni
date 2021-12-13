/*
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
package com.redhat.rhn.domain.action.salt.build;

import com.redhat.rhn.domain.action.ActionFormatter;

/**
 * The type Image build action formatter.
 */
public class ImageBuildActionFormatter extends ActionFormatter {

    private final ImageBuildActionDetails actionDetails;

    /**
     * Instantiates a new Image build action formatter.
     *
     * @param actionIn the action in
     */
    public ImageBuildActionFormatter(ImageBuildAction actionIn) {
        super(actionIn);
        actionDetails = actionIn.getDetails();
    }

    /**
     * Gets the action type
     * @return the action type
     */
    @Override
    public String getActionType() {
        return "ImageBuildAction";
    }
}
