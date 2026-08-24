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
package com.suse.manager.api.test.contract;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.audit.CVEAuditHandler;
import com.redhat.rhn.manager.audit.AuditChannelInfo;
import com.redhat.rhn.manager.audit.CVEAuditImage;
import com.redhat.rhn.manager.audit.CVEAuditServer;
import com.redhat.rhn.manager.audit.ErrataIdAdvisoryPair;
import com.redhat.rhn.manager.audit.PatchStatus;
import com.redhat.rhn.manager.audit.ScanDataSource;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class CVEAuditHandlerContractTest extends BaseOpenApiTest {

    private static final String CVE_ID = "CVE-2024-1234";

    @Override
    protected String getApiNamespace() {
        return "audit";
    }

    @Override
    protected Class<CVEAuditHandler> getHandlerClass() {
        return CVEAuditHandler.class;
    }

    private CVEAuditHandler handler() {
        return (CVEAuditHandler) handlerMock;
    }

    private Set<AuditChannelInfo> channels() {
        return Set.of(new AuditChannelInfo(101L, "SLES15 SP6 Updates", "sles15-sp6-updates-x86_64", 1L));
    }

    private Set<ErrataIdAdvisoryPair> erratas() {
        return Set.of(new ErrataIdAdvisoryPair(201L, "SUSE-SLE-Module-Basesystem-15-SP6-2024-1234"));
    }

    private CVEAuditServer auditedServer() {
        return new CVEAuditServer(1000010000L, "test-system.example.com",
                PatchStatus.AFFECTED_FULL_PATCH_APPLICABLE, channels(), erratas(),
                Set.of(ScanDataSource.CHANNELS));
    }

    private CVEAuditImage auditedImage() {
        return new CVEAuditImage(1000020000L, "test-image", PatchStatus.AFFECTED_PATCH_INAPPLICABLE,
                channels(), erratas());
    }

    @Test
    public void testListSystemsByPatchStatus() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listSystemsByPatchStatus(with(mockUser), with(CVE_ID));
            will(returnValue(List.of(auditedServer())));
        }});

        validateApiContract("/audit/listSystemsByPatchStatus", "GET")
                .withParams(Map.of("cveIdentifier", new String[] {CVE_ID}))
                .onHandlerMethod("listSystemsByPatchStatus", User.class, String.class);
    }

    /**
     * The patch status filter is the optional second argument, so passing it has to select the
     * longer overload while still matching the one documented path.
     */
    @Test
    public void testListSystemsByPatchStatusFiltered() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listSystemsByPatchStatus(with(mockUser), with(CVE_ID),
                    with(List.of("AFFECTED_FULL_PATCH_APPLICABLE")));
            will(returnValue(List.of(auditedServer())));
        }});

        validateApiContract("/audit/listSystemsByPatchStatus", "GET")
                .withParams(Map.of(
                        "cveIdentifier", new String[] {CVE_ID},
                        "patchStatusLabels", new String[] {"AFFECTED_FULL_PATCH_APPLICABLE"}))
                .onHandlerMethod("listSystemsByPatchStatus", User.class, String.class, List.class);
    }

    @Test
    public void testListImagesByPatchStatus() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listImagesByPatchStatus(with(mockUser), with(CVE_ID));
            will(returnValue(List.of(auditedImage())));
        }});

        validateApiContract("/audit/listImagesByPatchStatus", "GET")
                .withParams(Map.of("cveIdentifier", new String[] {CVE_ID}))
                .onHandlerMethod("listImagesByPatchStatus", User.class, String.class);
    }

    @Test
    public void testListImagesByPatchStatusFiltered() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listImagesByPatchStatus(with(mockUser), with(CVE_ID),
                    with(List.of("NOT_AFFECTED")));
            will(returnValue(List.of(auditedImage())));
        }});

        validateApiContract("/audit/listImagesByPatchStatus", "GET")
                .withParams(Map.of(
                        "cveIdentifier", new String[] {CVE_ID},
                        "patchStatusLabels", new String[] {"NOT_AFFECTED"}))
                .onHandlerMethod("listImagesByPatchStatus", User.class, String.class, List.class);
    }
}
