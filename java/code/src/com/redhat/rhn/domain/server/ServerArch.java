/*
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
package com.redhat.rhn.domain.server;

import com.redhat.rhn.domain.AbstractLabelNameHelper;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.common.ArchType;

/**
 * ServerArch
 */
public class ServerArch extends AbstractLabelNameHelper {

    private ArchType archType;

    /**
     * @return Returns the archType.
     */
    public ArchType getArchType() {
        return archType;
    }

    /**
     * @param a The archType to set.
     */
    public void setArchType(ArchType a) {
        this.archType = a;
    }

    /**
     * Return the compatible {@link ChannelArch}.
     * @return channel arch
     */
    public ChannelArch getCompatibleChannelArch() {
        return ServerFactory.findCompatibleChannelArch(this);
    }
}
