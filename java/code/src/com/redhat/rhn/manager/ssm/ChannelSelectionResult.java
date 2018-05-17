/**
 * Copyright (c) 2018 SUSE LLC
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

package com.redhat.rhn.manager.ssm;

import com.redhat.rhn.domain.server.Server;
import com.suse.salt.netapi.utils.Xor;

/**
 * The result of applying a channel selection on a server
 * @version $Rev$
 */
public class ChannelSelectionResult {

    private Server server;
    private Xor<ChannelSelection, String> result;

    /**
     * Constructor for ChannelSelectionResult
     * @param serverIn the server
     * @param channelSelection the channelSelection
     */
    public ChannelSelectionResult(Server serverIn, ChannelSelection channelSelection) {
        this.server = serverIn;
        this.result = Xor.left(channelSelection);
    }

    /**
     * Constructor for ChannelSelectionResult
     * @param serverIn the server
     * @param error the error message
     */
    public ChannelSelectionResult(Server serverIn, String error) {
        this.server = serverIn;
        this.result = Xor.right(error);
    }

    /**
     * @return true if an error occurred when applying the channel selection
     */
    public boolean isError() {
        return this.result.isRight();
    }

    /**
     * @return the server
     */
    public Server getServer() {
        return server;
    }

    /**
     * @return the ChannelSelection if any
     */
    public ChannelSelection getChannelSelection() {
        return result.left().get();
    }

    /**
     * @return the error message if any
     */
    public String getError() {
        return result.right().get();
    }

    /**
     * @return the server id
     */
    public Long getServerId() {
        return this.server.getId();
    }

    @Override
    public String toString() {
        return "ChannelSelectionResult [server=" + server + ", result=" + result + "]";
    }
}
