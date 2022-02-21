/*
 * Copyright (c) 2013 SUSE LLC
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
 * A pair of server and channel id together with an integer number to indicate
 * how 'close' this channel currently is to that server. A ranking index of 0
 * means that the channel is currently assigned to the server, while a higher
 * number indicates either a clone of an assigned channel or an unassigned
 * channel.
 *
 */
public class ServerChannelIdPair {
    private long sid;
    private long cid;
    private int channelRank;

    /**
     * Default constructor.
     */
    public ServerChannelIdPair() {
        super();
    }

    /**
     * Constructor for implicit object initialization.
     *
     * @param sidIn the server ID
     * @param cidIn the channel ID
     * @param rankIn the channel rank
     */
    public ServerChannelIdPair(long sidIn, long cidIn, int rankIn) {
        super();
        this.sid = sidIn;
        this.cid = cidIn;
        this.channelRank = rankIn;
    }

    /**
     * @param sidIn the server id to set
     */
    public void setSid(long sidIn) {
        this.sid = sidIn;
    }

    /**
     * @return the server id
     */
    public long getSid() {
        return sid;
    }

    /**
     * @param cidIn the channel id to set
     */
    public void setCid(long cidIn) {
        this.cid = cidIn;
    }

    /**
     * @return the channel id
     */
    public long getCid() {
        return cid;
    }

    /**
     * @return the rank
     */
    public int getChannelRank() {
        return channelRank;
    }

    /**
     * @param rank the rank to set
     */
    public void setChannelRank(int rank) {
        this.channelRank = rank;
    }

    /**
     * {@inheritDoc} Note: Channel ranking is not considered for equals() and
     * hashCode().
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (cid ^ (cid >>> 32));
        result = prime * result + (int) (sid ^ (sid >>> 32));
        return result;
    }

    /**
     * {@inheritDoc} Note: Channel ranking is not considered for equals() and
     * hashCode().
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ServerChannelIdPair other = (ServerChannelIdPair) obj;
        if (cid != other.cid) {
            return false;
        }
        if (sid != other.sid) {
            return false;
        }
        return true;
    }
}
