/**
 * Copyright (c) 2013 SUSE
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

package com.suse.manager.model.products;

import org.simpleframework.xml.Attribute;

public class Channel {

    /** Status attributed to channels that have begun synchronization. */
    public static final String STATE_SYNCHRONIZING = "P";
    /** Status attributed to channels that have not begun synchronization. */
    public static final String STATE_NOT_SYNCHRONIZING = ".";

    @Attribute
    private String label;

    @Attribute
    private String status;

    /**
     * Instantiates a new channel.
     *
     * @param label the label
     * @param status the status
     */
    public Channel(String labelIn, String statusIn) {
        super();
        label = labelIn;
        status = statusIn;
    }

    public String getLabel() {
        return label;
    }

    public String getStatus() {
        return status;
    }

    /**
     * Returns true iff this channel has already been synchronized or it is
     * synchronizing at the moment.
     * @return true or false
     */
    public boolean isSynchronizing() {
        return STATE_SYNCHRONIZING.equals(status);
    }
}
