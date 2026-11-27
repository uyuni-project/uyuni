/*
 * Copyright (c) 2026 SUSE LLC
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
package com.suse.manager.webui.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.ActivationKeyFactory;

import org.junit.jupiter.api.Test;

import java.util.Map;

public class MinionControllerTest {

    @Test
    public void testToBootstrapActivationKeyIncludesDescription() {
        ActivationKey key = new ActivationKey();
        key.setKey("1-centos7");
        key.setNote("CentOS 7");

        Map<String, String> json = MinionController.toBootstrapActivationKey(key);

        assertEquals("1-centos7", json.get("key"));
        assertEquals("CentOS 7", json.get("description"));
    }

    @Test
    public void testToBootstrapActivationKeyOmitsDefaultDescription() {
        ActivationKey key = new ActivationKey();
        key.setKey("1-random");
        key.setNote(ActivationKeyFactory.DEFAULT_DESCRIPTION);

        Map<String, String> json = MinionController.toBootstrapActivationKey(key);

        assertEquals("1-random", json.get("key"));
        assertEquals("", json.get("description"));
    }

    @Test
    public void testToBootstrapActivationKeyOmitsBlankDescription() {
        ActivationKey key = new ActivationKey();
        key.setKey("1-random");
        key.setNote("   ");

        Map<String, String> json = MinionController.toBootstrapActivationKey(key);

        assertEquals("1-random", json.get("key"));
        assertEquals("", json.get("description"));
    }
}
