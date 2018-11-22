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

package com.suse.manager.utils;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;

/**
 * Utility for software channels.
 */
public class ChannelUtils {

    private ChannelUtils() { }

    /**
     * @param channel the channel to check
     * @return whether the channel is a RPM chanel or not
     */
    public static boolean isTypeRpm(Channel channel) {
        return "rpm".equalsIgnoreCase(getArchTypeLabel(channel));
    }

    /**
     * @param channel the channel to check
     * @return whether the channel is a DEB chanel or not
     */
    public static boolean isTypeDeb(Channel channel) {
        return "deb".equalsIgnoreCase(getArchTypeLabel(channel));
    }

    private static String getArchTypeLabel(Channel chan) {
        return chan.getChannelArch().getArchType().getLabel();
    }

    /**
     * Ger the channel architecture as string
     * @param channelArch the channel arch
     * @return a string
     */
    public static String getChannelArch(ChannelArch channelArch) {
        return channelArch.getLabel().replaceAll("channel-", "").replaceAll("-deb", "");
    }
}
