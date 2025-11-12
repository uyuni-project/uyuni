/*
 * Copyright (c) 2015--2024 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.suse.manager.webui.utils.token.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.suse.manager.webui.utils.token.DefaultTokenBuilder;
import com.suse.manager.webui.utils.token.Token;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Tests for the AbstractTokenBuilder class.
 */
public class DefaultTokenBuilderTest {

    @Test
    public void testDefaultValues() throws Exception {
        // Truncate the reference time to the seconds to avoid flakiness
        Instant referenceTime = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        Token token = new DefaultTokenBuilder()
            .issuedAt(referenceTime)
            .usingServerSecret()
            .build();

        // Check the issuing time is correct
        assertNotNull(token.getIssuingTime());
        assertEquals(referenceTime, token.getIssuingTime());

        // By default, expiration should be 1 year
        assertNotNull(token.getExpirationTime());
        assertEquals(365, Duration.between(referenceTime, token.getExpirationTime()).toDays());

        // By default, the token is valid even 2 minutes before being issued
        assertNotNull(token.getNotBeforeTime());
        assertEquals(2, Duration.between(token.getNotBeforeTime(), referenceTime).toMinutes());
    }
}
