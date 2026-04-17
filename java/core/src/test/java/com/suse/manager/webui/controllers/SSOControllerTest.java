/*
 * Copyright (c) 2019--2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.webui.controllers;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import spark.Request;

public class SSOControllerTest extends BaseControllerTestCase {

    private SSOController ssoController;

    private boolean ssoEnabled;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        ssoEnabled = Config.get().getBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED);
        ssoController = new SSOController(SSOTestUtils.getSaml2Settings());
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        // Restore the original SSO Enabled value
        Config.get().setBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED, Boolean.toString(ssoEnabled));

        super.tearDown();
    }

    @Test
    public void testACSWithoutSSO() {
        Config.get().setBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED, "false");

        assertNull(ssoController.getACS(getRequestWithCsrf("/manager/sso/acs"), response));
    }

    @Test
    public void testMetadataWithSSO() throws IOException {
        Config.get().setBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED, "true");

        Request requestWithCsrf = getRequestWithCsrf("/manager/sso/metadata");
        String metadata = (String) ssoController.getMetadata(requestWithCsrf, response);
        assertTrue(response.type().contains("text/xml"));
        Assertions.assertNotNull(metadata);
        assertTrue(metadata.contains("md:EntityDescriptor"));
    }

    @Test
    public void testMetadataWithoutSSO() throws IOException {
        Config.get().setBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED, "false");

        Request requestWithCsrf = getRequestWithCsrf("/manager/sso/metadata");
        String metadata = (String) ssoController.getMetadata(requestWithCsrf, response);
        Assertions.assertNull(metadata);
    }
}
