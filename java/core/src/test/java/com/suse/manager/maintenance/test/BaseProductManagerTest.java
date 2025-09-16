/*
 * Copyright (c) 2023 SUSE LLC
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

package com.suse.manager.maintenance.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.suse.manager.maintenance.BaseProductManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Objects;

public class BaseProductManagerTest {

    private URL baseProductResource;

    @BeforeEach
    public void init() {
        baseProductResource = Objects.requireNonNull(BaseProductManagerTest.class.getResource("baseproduct.xml"),
            "Base Product test file not found");
    }

    @Test
    public void canParseProductFile() throws URISyntaxException {
        BaseProductManager productManager = new BaseProductManager(baseProductResource.toURI());

        assertEquals("SUSE-Manager-Server", productManager.getName());
        assertEquals("4.3", productManager.getVersion());
        assertEquals("x86_64", productManager.getArch());

        assertEquals("SUSE Manager Server 4.3", productManager.getSummary());
        assertEquals(LocalDate.of(2025, 6, 30), productManager.getEndOfLifeDate());
    }
}
