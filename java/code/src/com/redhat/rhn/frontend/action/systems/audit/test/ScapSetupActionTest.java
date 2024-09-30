/*
 * Copyright (c) 2024 SUSE LLC
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

package com.redhat.rhn.frontend.action.systems.audit.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.frontend.action.systems.audit.ScapSetupAction;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.testing.PackageTestUtils;
import com.redhat.rhn.testing.RhnMockHttpServletRequest;
import com.redhat.rhn.testing.RhnMockStrutsTestCase;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ScapSetupActionTest extends RhnMockStrutsTestCase {

    private TestScapSetupAction action;

    private RequestContext mockContext;

    private Server server;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        action = new TestScapSetupAction();

        server = MinionServerFactoryTest.createTestMinionServer(user);

        RhnMockHttpServletRequest request = TestUtils.getRequestWithSessionAndUser();
        request.setAttribute(RequestContext.SYSTEM, server);

        mockContext = new RequestContext(request);
    }

    @Test
    public void canSearchForCorrectPackageForSUSE() {
        server.setOsFamily("Suse");
        server.setOs(ServerConstants.SLES);
        server.setRelease("15.6");

        action.setupScapEnablementInfo(mockContext);

        // The package is not installed. Scap should be disabled and the correct package should be requested
        assertEquals(false, mockContext.getRequest().getAttribute(ScapSetupAction.SCAP_ENABLED));
        assertEquals("openscap-utils", mockContext.getRequest().getAttribute(ScapSetupAction.REQUIRED_PKG));
    }

    @Test
    public void canSearchForCorrectPackageForRHEL() {
        server.setOsFamily("RedHat");
        server.setOs(ServerConstants.ROCKY);
        server.setRelease("8");

        action.setupScapEnablementInfo(mockContext);

        // The package is not installed. Scap should be disabled and the correct package should be requested
        assertEquals(false, mockContext.getRequest().getAttribute(ScapSetupAction.SCAP_ENABLED));
        assertEquals("openscap-scanner", mockContext.getRequest().getAttribute(ScapSetupAction.REQUIRED_PKG));
    }

    @Test
    public void canSearchForCorrectPackageForDebianLegacy() {
        server.setOsFamily("Debian");
        server.setOs(ServerConstants.DEBIAN);
        server.setRelease("10");

        action.setupScapEnablementInfo(mockContext);

        // The package is not installed. Scap should be disabled and the correct package should be requested
        assertEquals(false, mockContext.getRequest().getAttribute(ScapSetupAction.SCAP_ENABLED));
        assertEquals("libopenscap8", mockContext.getRequest().getAttribute(ScapSetupAction.REQUIRED_PKG));
    }

    @Test
    public void canSearchForCorrectPackageForDebian() {
        server.setOsFamily("Debian");
        server.setOs(ServerConstants.DEBIAN);
        server.setRelease("12");

        action.setupScapEnablementInfo(mockContext);

        // The package is not installed. Scap should be disabled and the correct package should be requested
        assertEquals(false, mockContext.getRequest().getAttribute(ScapSetupAction.SCAP_ENABLED));
        assertEquals(
            "openscap-utils",
            mockContext.getRequest().getAttribute(ScapSetupAction.REQUIRED_PKG)
        );
    }

    @Test
    public void canSearchForCorrectPackageForUbuntuLegacy() {
        server.setOsFamily("Debian");
        server.setOs(ServerConstants.UBUNTU);
        server.setRelease("22.04");

        action.setupScapEnablementInfo(mockContext);

        // The package is not installed. Scap should be disabled and the correct package should be requested
        assertEquals(false, mockContext.getRequest().getAttribute(ScapSetupAction.SCAP_ENABLED));
        assertEquals("libopenscap8", mockContext.getRequest().getAttribute(ScapSetupAction.REQUIRED_PKG));
    }

    @Test
    public void canSearchForCorrectPackageForUbuntu() {
        server.setOsFamily("Debian");
        server.setOs(ServerConstants.UBUNTU);
        server.setRelease("24.04");

        action.setupScapEnablementInfo(mockContext);

        // The package is not installed. Scap should be disabled and the correct package should be requested
        assertEquals(false, mockContext.getRequest().getAttribute(ScapSetupAction.SCAP_ENABLED));
        assertEquals(
            "openscap-utils",
            mockContext.getRequest().getAttribute(ScapSetupAction.REQUIRED_PKG)
        );
    }

    @Test
    public void scapIsEnabledWhenCorrectPackageIsInstalled() {
        server.setOsFamily("Suse");
        server.setOs(ServerConstants.SLES);
        server.setRelease("15.6");

        Package scapPackage = PackageTest.createTestPackage(user.getOrg(), "openscap-utils");
        PackageTestUtils.installPackageOnServer(scapPackage, server);

        action.setupScapEnablementInfo(mockContext);

        // The package is installed. Scap should be enabled
        assertEquals(true, mockContext.getRequest().getAttribute(ScapSetupAction.SCAP_ENABLED));
        assertEquals("openscap-utils", mockContext.getRequest().getAttribute(ScapSetupAction.REQUIRED_PKG));
    }

    @Test
    public void scapIsEnabledWhenAllRequiredPackagesAreInstalled() {
        server.setOsFamily("Debian");
        server.setOs(ServerConstants.DEBIAN);
        server.setRelease("12");

        Package scapLibPackage = PackageTest.createTestPackage(user.getOrg(), "libopenscap25");
        PackageTestUtils.installPackageOnServer(scapLibPackage, server);

        action.setupScapEnablementInfo(mockContext);

        // One package is missing. Scap should be disabled and the correct package should be requested
        assertEquals(false, mockContext.getRequest().getAttribute(ScapSetupAction.SCAP_ENABLED));
        assertEquals(
            "openscap-utils",
            mockContext.getRequest().getAttribute(ScapSetupAction.REQUIRED_PKG)
        );

        Package scapCommonPackage = PackageTest.createTestPackage(user.getOrg(), "openscap-utils");
        PackageTestUtils.installPackageOnServer(scapCommonPackage, server);

        action.setupScapEnablementInfo(mockContext);

        // Now everything is installed and scap should be enabled
        assertEquals(true, mockContext.getRequest().getAttribute(ScapSetupAction.SCAP_ENABLED));
        assertEquals(
            "openscap-utils",
            mockContext.getRequest().getAttribute(ScapSetupAction.REQUIRED_PKG)
        );
    }

    private static class TestScapSetupAction extends ScapSetupAction {
        @Override
        public void setupScapEnablementInfo(RequestContext context) {
            super.setupScapEnablementInfo(context);
        }
    }
}
