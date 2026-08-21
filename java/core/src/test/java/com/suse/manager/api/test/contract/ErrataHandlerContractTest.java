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

import com.redhat.rhn.domain.errata.AdvisoryStatus;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ErrataHandlerContractTest extends BaseOpenApiTest {

    private static final String ADVISORY_NAME = "SUSE-2026-1234";
    private static final String CHANNEL_LABEL = "sle-product-sles15-sp6-updates-x86_64";
    private static final String CVE_NAME = "CVE-2026-3270";

    @Override
    protected String getApiNamespace() {
        return "errata";
    }

    @Override
    protected Class<ErrataHandler> getHandlerClass() {
        return ErrataHandler.class;
    }

    private ErrataHandler handler() {
        return (ErrataHandler) handlerMock;
    }

    private Errata errata() {
        Errata errata = new Errata();
        errata.setId(42L);
        errata.setAdvisoryName(ADVISORY_NAME);
        errata.setAdvisoryType("Security Advisory");
        errata.setAdvisoryStatus(AdvisoryStatus.FINAL);
        errata.setSynopsis("important update for the kernel");
        errata.setUpdateDate(new Date());
        return errata;
    }

    private SystemOverview affectedSystem() {
        SystemOverview system = new SystemOverview();
        system.setId(1000010000L);
        system.setName("client.example.com");
        system.setCreated(new Date());
        system.setLastBoot(1755000000L);
        system.setExtraPkgCount(3L);
        system.setOutdatedPackages(5L);
        return system;
    }

    /**
     * The handler builds this map itself, so the fixture reproduces the documented keys rather
     * than running a serializer.
     *
     * @return the details of an erratum
     */
    private Map<String, Object> erratumDetails() {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("id", 42);
        details.put("issue_date", "2026-08-01");
        details.put("update_date", "2026-08-02");
        details.put("last_modified_date", "2026-08-03 10:15:00.0");
        details.put("release", 1);
        details.put("advisory_status", "final");
        details.put("vendor_advisory", "https://www.suse.com/support/update/announcement/");
        details.put("product", "SUSE Linux Enterprise");
        details.put("errataFrom", "maint-coord@suse.de");
        details.put("solution", "Update the affected packages.");
        details.put("description", "This update fixes a security issue in the kernel.");
        details.put("synopsis", "important update for the kernel");
        details.put("topic", "An update that fixes one vulnerability is now available.");
        details.put("references", "");
        details.put("notes", "");
        details.put("type", "Security Advisory");
        details.put("severity", "Important");
        details.put("reboot_suggested", true);
        details.put("restart_suggested", false);
        return details;
    }

    private Map<String, Object> packageDetails() {
        Map<String, Object> pkg = new LinkedHashMap<>();
        pkg.put("id", 101);
        pkg.put("name", "kernel-default");
        pkg.put("epoch", "");
        pkg.put("version", "6.4.0");
        pkg.put("release", "150600.23.7.1");
        pkg.put("arch_label", "x86_64");
        pkg.put("providing_channels", List.of(CHANNEL_LABEL));
        pkg.put("build_host", "build.example.com");
        pkg.put("description", "The standard kernel.");
        pkg.put("checksum", "0f5b1e0e1a5f9a9e");
        pkg.put("checksum_type", "sha256");
        pkg.put("vendor", "SUSE LLC");
        pkg.put("summary", "The standard kernel");
        pkg.put("cookie", "build.example.com 1755000000");
        pkg.put("license", "GPL-2.0-only");
        pkg.put("path", "1/0f5/kernel-default/6.4.0/x86_64/kernel-default.rpm");
        pkg.put("file", "kernel-default-6.4.0-150600.23.7.1.x86_64.rpm");
        pkg.put("build_date", "2026-07-30 08:00:00.0");
        pkg.put("last_modified_date", "2026-07-31 08:00:00.0");
        pkg.put("size", "94371840");
        pkg.put("payload_size", "104857600");
        return pkg;
    }

    @Test
    public void testGetDetails() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getDetails(with(mockUser), with(ADVISORY_NAME));
            will(returnValue(erratumDetails()));
        }});

        validateApiContract("/errata/getDetails", "GET")
                .withParams(Map.of("advisoryName", new String[] {ADVISORY_NAME}))
                .onHandlerMethod("getDetails", User.class, String.class);
    }

    @Test
    public void testSetDetails() throws Exception {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("synopsis", "important update for the kernel");
        details.put("advisory_name", ADVISORY_NAME);
        details.put("advisory_release", 1);
        details.put("advisory_type", "Security Advisory");
        details.put("product", "SUSE Linux Enterprise");
        details.put("errataFrom", "maint-coord@suse.de");
        details.put("topic", "An update that fixes one vulnerability is now available.");
        details.put("description", "This update fixes a security issue in the kernel.");
        details.put("references", "");
        details.put("notes", "");
        details.put("solution", "Update the affected packages.");
        details.put("severity", "Important");
        details.put("keywords", List.of("reboot_suggested"));
        details.put("cves", List.of(CVE_NAME));

        context.checking(new Expectations() {{
            oneOf(handler()).setDetails(with(mockUser), with(ADVISORY_NAME), with(details));
            will(returnValue(1));
        }});

        validateApiContract("/errata/setDetails", "POST")
                .withBody(Map.of("advisoryName", ADVISORY_NAME, "details", details))
                .onHandlerMethod("setDetails", User.class, String.class, Map.class);
    }

    @Test
    public void testListAffectedSystems() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listAffectedSystems(with(mockUser), with(ADVISORY_NAME));
            will(returnValue(new Object[] {affectedSystem()}));
        }});

        validateApiContract("/errata/listAffectedSystems", "GET")
                .withParams(Map.of("advisoryName", new String[] {ADVISORY_NAME}))
                .onHandlerMethod("listAffectedSystems", User.class, String.class);
    }

    @Test
    public void testBugzillaFixes() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).bugzillaFixes(with(mockUser), with(ADVISORY_NAME));
            will(returnValue(Map.of(208144L, "errata.bugzillaFixes Method Returns different results")));
        }});

        validateApiContract("/errata/bugzillaFixes", "POST")
                .withBody(Map.of("advisoryName", ADVISORY_NAME))
                .onHandlerMethod("bugzillaFixes", User.class, String.class);
    }

    @Test
    public void testListKeywords() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listKeywords(with(mockUser), with(ADVISORY_NAME));
            will(returnValue(new Object[] {"reboot_suggested", "restart_suggested"}));
        }});

        validateApiContract("/errata/listKeywords", "GET")
                .withParams(Map.of("advisoryName", new String[] {ADVISORY_NAME}))
                .onHandlerMethod("listKeywords", User.class, String.class);
    }

    @Test
    public void testApplicableToChannels() throws Exception {
        Map<String, Object> channel = new LinkedHashMap<>();
        channel.put("channel_id", 101);
        channel.put("label", CHANNEL_LABEL);
        channel.put("name", "SLE-Product-SLES15-SP6-Updates x86_64");
        channel.put("parent_channel_label", "sle-product-sles15-sp6-pool-x86_64");

        context.checking(new Expectations() {{
            oneOf(handler()).applicableToChannels(with(mockUser), with(ADVISORY_NAME));
            will(returnValue(new Object[] {channel}));
        }});

        validateApiContract("/errata/applicableToChannels", "GET")
                .withParams(Map.of("advisoryName", new String[] {ADVISORY_NAME}))
                .onHandlerMethod("applicableToChannels", User.class, String.class);
    }

    @Test
    public void testListCves() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listCves(with(mockUser), with(ADVISORY_NAME));
            will(returnValue(List.of(CVE_NAME)));
        }});

        validateApiContract("/errata/listCves", "GET")
                .withParams(Map.of("advisoryName", new String[] {ADVISORY_NAME}))
                .onHandlerMethod("listCves", User.class, String.class);
    }

    @Test
    public void testListPackages() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listPackages(with(mockUser), with(ADVISORY_NAME));
            will(returnValue(List.of(packageDetails())));
        }});

        validateApiContract("/errata/listPackages", "GET")
                .withParams(Map.of("advisoryName", new String[] {ADVISORY_NAME}))
                .onHandlerMethod("listPackages", User.class, String.class);
    }

    @Test
    public void testAddPackages() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).addPackages(with(mockUser), with(ADVISORY_NAME), with(List.of(101, 102)));
            will(returnValue(2));
        }});

        validateApiContract("/errata/addPackages", "POST")
                .withBody(Map.of("advisoryName", ADVISORY_NAME, "packageIds", List.of(101, 102)))
                .onHandlerMethod("addPackages", User.class, String.class, List.class);
    }

    @Test
    public void testRemovePackages() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).removePackages(with(mockUser), with(ADVISORY_NAME), with(List.of(101, 102)));
            will(returnValue(2));
        }});

        validateApiContract("/errata/removePackages", "POST")
                .withBody(Map.of("advisoryName", ADVISORY_NAME, "packageIds", List.of(101, 102)))
                .onHandlerMethod("removePackages", User.class, String.class, List.class);
    }

    @Test
    public void testClone() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).clone(with(mockUser), with(CHANNEL_LABEL), with(List.of(ADVISORY_NAME)));
            will(returnValue(new Object[] {errata()}));
        }});

        validateApiContract("/errata/clone", "POST")
                .withBody(Map.of("channelLabel", CHANNEL_LABEL, "advisoryNames", List.of(ADVISORY_NAME)))
                .onHandlerMethod("clone", User.class, String.class, List.class);
    }

    @Test
    public void testCloneAsync() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).cloneAsync(with(mockUser), with(CHANNEL_LABEL), with(List.of(ADVISORY_NAME)));
            will(returnValue(1));
        }});

        validateApiContract("/errata/cloneAsync", "POST")
                .withBody(Map.of("channelLabel", CHANNEL_LABEL, "advisoryNames", List.of(ADVISORY_NAME)))
                .onHandlerMethod("cloneAsync", User.class, String.class, List.class);
    }

    @Test
    public void testCloneAsOriginal() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).cloneAsOriginal(with(mockUser), with(CHANNEL_LABEL), with(List.of(ADVISORY_NAME)));
            will(returnValue(new Object[] {errata()}));
        }});

        validateApiContract("/errata/cloneAsOriginal", "POST")
                .withBody(Map.of("channelLabel", CHANNEL_LABEL, "advisoryNames", List.of(ADVISORY_NAME)))
                .onHandlerMethod("cloneAsOriginal", User.class, String.class, List.class);
    }

    @Test
    public void testCloneAsOriginalAsync() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).cloneAsOriginalAsync(with(mockUser), with(CHANNEL_LABEL),
                    with(List.of(ADVISORY_NAME)));
            will(returnValue(1));
        }});

        validateApiContract("/errata/cloneAsOriginalAsync", "POST")
                .withBody(Map.of("channelLabel", CHANNEL_LABEL, "advisoryNames", List.of(ADVISORY_NAME)))
                .onHandlerMethod("cloneAsOriginalAsync", User.class, String.class, List.class);
    }

    @Test
    public void testCreate() throws Exception {
        Map<String, Object> errataInfo = new LinkedHashMap<>();
        errataInfo.put("synopsis", "important update for the kernel");
        errataInfo.put("advisory_name", ADVISORY_NAME);
        errataInfo.put("advisory_release", 1);
        errataInfo.put("advisory_type", "Security Advisory");
        errataInfo.put("advisory_status", "final");
        errataInfo.put("product", "SUSE Linux Enterprise");
        errataInfo.put("errataFrom", "maint-coord@suse.de");
        errataInfo.put("topic", "An update that fixes one vulnerability is now available.");
        errataInfo.put("description", "This update fixes a security issue in the kernel.");
        errataInfo.put("references", "");
        errataInfo.put("notes", "");
        errataInfo.put("solution", "Update the affected packages.");
        errataInfo.put("severity", "Important");

        Map<String, Object> bug = new LinkedHashMap<>();
        bug.put("id", 208144);
        bug.put("summary", "kernel panic on boot");
        bug.put("url", "https://bugzilla.suse.com/show_bug.cgi?id=208144");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("errataInfo", errataInfo);
        body.put("bugs", List.of(bug));
        body.put("keywords", List.of("reboot_suggested"));
        body.put("packageIds", List.of(101));
        body.put("channelLabels", List.of(CHANNEL_LABEL));

        context.checking(new Expectations() {{
            oneOf(handler()).create(with(mockUser), with(errataInfo), with(List.of(bug)),
                    with(List.of("reboot_suggested")), with(List.of(101)), with(List.of(CHANNEL_LABEL)));
            will(returnValue(errata()));
        }});

        validateApiContract("/errata/create", "POST")
                .withBody(body)
                .onHandlerMethod("create", User.class, Map.class, List.class, List.class, List.class,
                        List.class);
    }

    @Test
    public void testDelete() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).delete(with(mockUser), with(ADVISORY_NAME));
            will(returnValue(1));
        }});

        validateApiContract("/errata/delete", "POST")
                .withBody(Map.of("advisoryName", ADVISORY_NAME))
                .onHandlerMethod("delete", User.class, String.class);
    }

    @Test
    public void testPublish() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).publish(with(mockUser), with(ADVISORY_NAME), with(List.of(CHANNEL_LABEL)));
            will(returnValue(errata()));
        }});

        validateApiContract("/errata/publish", "POST")
                .withBody(Map.of("advisoryName", ADVISORY_NAME, "channelLabels", List.of(CHANNEL_LABEL)))
                .onHandlerMethod("publish", User.class, String.class, List.class);
    }

    @Test
    public void testPublishAsOriginal() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).publishAsOriginal(with(mockUser), with(ADVISORY_NAME),
                    with(List.of(CHANNEL_LABEL)));
            will(returnValue(errata()));
        }});

        validateApiContract("/errata/publishAsOriginal", "POST")
                .withBody(Map.of("advisoryName", ADVISORY_NAME, "channelLabels", List.of(CHANNEL_LABEL)))
                .onHandlerMethod("publishAsOriginal", User.class, String.class, List.class);
    }

    @Test
    public void testFindByCve() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).findByCve(with(mockUser), with(CVE_NAME));
            will(returnValue(List.of(errata())));
        }});

        validateApiContract("/errata/findByCve", "GET")
                .withParams(Map.of("cveName", new String[] {CVE_NAME}))
                .onHandlerMethod("findByCve", User.class, String.class);
    }
}
