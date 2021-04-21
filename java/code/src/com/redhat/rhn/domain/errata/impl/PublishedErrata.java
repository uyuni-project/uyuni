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
package com.redhat.rhn.domain.errata.impl;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.errata.AbstractErrata;
import com.redhat.rhn.domain.errata.Cve;

import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * Errata - Class representation of the table rhnErrata.
 * @version $Rev: 51306 $
 */
public class PublishedErrata extends AbstractErrata {

    private static Logger log = Logger.getLogger(PublishedErrata.class);

    private Set<Channel> channels = new HashSet<>();
    private Set<Cve> cves = new HashSet<Cve>();

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Channel> getChannels() {
        return channels;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setChannels(Set<Channel> channelsIn) {
        this.channels = channelsIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addChannel(Channel channelIn) {
        log.debug("addChannel called: " + channelIn.getLabel());
        if (this.channels == null) {
            this.channels = new HashSet<>();
        }
        channels.add(channelIn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPublished() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCloned() {
        return false;
    }
    /**
     * @param cvesIn sets cves
     */
    @Override
    public void setCves(Set<Cve> cvesIn) {
        this.cves = cvesIn;
    }
    /**
     * @return Returns cves
     */
    @Override
    public Set<Cve> getCves() {
        return cves;
    }

}
