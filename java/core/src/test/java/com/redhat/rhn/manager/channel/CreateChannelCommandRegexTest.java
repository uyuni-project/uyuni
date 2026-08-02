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
package com.redhat.rhn.manager.channel;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

/**
 * CreateChannelCommandRegexTest
 */
public class CreateChannelCommandRegexTest {

    @Test
    public void testChannelNameRegexAllowsUppercaseAndLeadingDigit() {
        Pattern channelNamePattern = Pattern.compile(CreateChannelCommand.CHANNEL_NAME_REGEX);

        assertTrue(channelNamePattern.matcher("Uppercase Channel Name").matches());
        assertTrue(channelNamePattern.matcher("0Starts With Digit").matches());
        assertTrue(channelNamePattern.matcher("O'Reilly Channel").matches());
    }

    @Test
    public void testChannelLabelRegexAllowsLeadingDigitButRejectsUppercase() {
        Pattern channelLabelPattern = Pattern.compile(CreateChannelCommand.CHANNEL_LABEL_REGEX);

        assertTrue(channelLabelPattern.matcher("0starts-with-digit").matches());
        assertFalse(channelLabelPattern.matcher("Uppercase-label").matches());
    }
}
