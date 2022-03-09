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

/*
 * AUTOMATICALLY GENERATED FILE, DO NOT EDIT.
 */
package com.redhat.rhn.domain.channel;

import com.redhat.rhn.common.RhnRuntimeException;

/**
 * A channel may have only one channel family.
 * <p>

 *
 * @version definition($Rev: 76724 $)/template($Rev: 67725 $)
 */
public class TooManyChannelFamiliesException extends RhnRuntimeException  {

    private final Long chanId;

    /////////////////////////
    // Constructors
    /////////////////////////
    /**
     * Constructor
     * @param channelId channel id
     * @param message exception message
     */
    public TooManyChannelFamiliesException(Long channelId, String message) {
        super(message);
        this.chanId =  channelId;
    }

    /////////////////////////
    // Getters/Setters
    /////////////////////////
    /**
     * Returns the value of channelId
     * @return Long channelId
     */
    public Long getChannelId() {
        return chanId;
    }
}
