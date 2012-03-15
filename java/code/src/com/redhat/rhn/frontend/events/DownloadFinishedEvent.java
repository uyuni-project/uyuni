/**
 * Copyright (c) 2009--2010 Red Hat, Inc.
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

package com.redhat.rhn.frontend.events;

import com.redhat.rhn.common.messaging.EventMessage;

/**
 * An event representing finished image download.
 */
public class DownloadFinishedEvent extends BaseEvent implements EventMessage  {

    private final String url;

    /**
     * Constructor taking ...
     * @param id
     */
    public DownloadFinishedEvent(String url) {
        this.url = url;
    }

    @Override
    public String toText() {
        return "Download finished from " + url;
    }
}
