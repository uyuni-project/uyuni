/**
 * Copyright (c) 2014 SUSE
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

package com.suse.mgrsync;

import java.util.ArrayList;
import java.util.List;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "channels", strict = false)
public class MgrSyncChannels {
    @ElementList(name = "channel", inline = true, required = false)
    private List<MgrSyncChannel> channels;

    /**
     * Return the list of {@link Subscription} objects.
     * @return subscriptions
     */
    public List<MgrSyncChannel> getChannels() {
        if (this.channels == null) {
            this.channels = new ArrayList<MgrSyncChannel>();
        }

        return this.channels;
    }
}
