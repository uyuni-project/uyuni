/*
 * Copyright (c) 2017 SUSE LLC
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
package com.redhat.rhn.manager.audit;

/**
 * RankedChannel
 */
public class RankedChannel {

    private final long channelId;
    private final int rank;

    /**
     * Constructor
     * @param channelIdIn channel id
     * @param rankIn the rank
     */
    public RankedChannel(long channelIdIn, int rankIn) {
        this.channelId = channelIdIn;
        this.rank = rankIn;
    }

    /**
     * Get the Rank
     * @return the rank
     */
    public int getRank() {
        return rank;
    }

    /**
     * Get the Channel ID
     * @return the channel id
     */
    public long getChannelId() {
        return channelId;
    }
}
