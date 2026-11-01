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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.org.Org;

import org.junit.jupiter.api.Test;

public class ChannelDescriptionHelperTest {

    @Test
    public void convertsVendorChannelHtml() {
        Channel channel = new Channel();
        channel.setDescription("<p>SUSE Linux</p>");

        assertEquals("SUSE Linux", ChannelDescriptionHelper.getDisplayDescription(channel));
    }

    @Test
    public void convertsVendorChannelWithoutTags() {
        Channel channel = new Channel();
        channel.setDescription("SUSE&nbsp;Linux\nSecond line");

        assertEquals("SUSE Linux\nSecond line", ChannelDescriptionHelper.getDisplayDescription(channel));
    }

    @Test
    public void preservesCustomChannelHtml() {
        Channel channel = new Channel();
        channel.setOrg(new Org());
        channel.setDescription("<p>SUSE <strong>Linux</strong></p>");

        assertEquals("<p>SUSE <strong>Linux</strong></p>",
                ChannelDescriptionHelper.getDisplayDescription(channel));
    }

    @Test
    public void preservesCustomChannelPlainTextWithComparisonOperator() {
        Channel channel = new Channel();
        channel.setOrg(new Org());
        channel.setDescription("kernel < 5.0 only");

        assertEquals("kernel < 5.0 only", ChannelDescriptionHelper.getDisplayDescription(channel));
    }

    @Test
    public void preservesCustomChannelPlaceholder() {
        Channel channel = new Channel();
        channel.setOrg(new Org());
        channel.setDescription("Install <package-name> version 2");

        assertEquals("Install <package-name> version 2", ChannelDescriptionHelper.getDisplayDescription(channel));
    }

    @Test
    public void preservesCustomTextThatLooksLikeHtml() {
        Channel channel = new Channel();
        channel.setOrg(new Org());

        String[] descriptions = {
            "if a<b and c>d",
            "Set <var> to path",
            "Use <html> for docs",
            "Use <title> param and more",
            "x <select> y"
        };
        for (String description : descriptions) {
            channel.setDescription(description);
            assertEquals(description, ChannelDescriptionHelper.getDisplayDescription(channel));
        }
    }

    @Test
    public void handlesNullChannel() {
        assertNull(ChannelDescriptionHelper.getDisplayDescription(null));
    }
}
