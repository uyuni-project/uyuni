/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.frontend.action.channel;

import com.redhat.rhn.common.util.StringUtil;
import com.redhat.rhn.domain.channel.Channel;

/**
 * Prepares software channel descriptions for display.
 */
public final class ChannelDescriptionHelper {
    private ChannelDescriptionHelper() { }

    /**
     * Converts vendor descriptions to plain text because they come from SCC as HTML. Descriptions
     * of custom channels are preserved exactly as entered by the user.
     *
     * @param channel the software channel
     * @return the channel description suitable for display
     */
    public static String getDisplayDescription(Channel channel) {
        if (channel == null) {
            return null;
        }

        String description = channel.getDescription();
        return channel.isVendorChannel() ? StringUtil.htmlToPlainText(description) : description;
    }
}
