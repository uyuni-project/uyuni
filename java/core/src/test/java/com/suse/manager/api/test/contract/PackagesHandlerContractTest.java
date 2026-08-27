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

import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageName;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.PackageOverview;
import com.redhat.rhn.frontend.xmlrpc.packages.PackagesHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PackagesHandlerContractTest extends BaseOpenApiTest {

    private static final Integer PACKAGE_ID = 101;

    @Override
    protected String getApiNamespace() {
        return "packages";
    }

    @Override
    protected Class<PackagesHandler> getHandlerClass() {
        return PackagesHandler.class;
    }

    private PackagesHandler handler() {
        return (PackagesHandler) handlerMock;
    }

    /**
     * Builds a package serialized by the registered PackageSerializer. The domain object is
     * mocked because the serializer reads through four associations that would otherwise need
     * a session to resolve.
     *
     * @return the package
     */
    private Package packageObject() {
        Package pkg = context.mock(Package.class, "package");
        PackageName name = context.mock(PackageName.class, "packageName");
        PackageEvr evr = context.mock(PackageEvr.class, "packageEvr");
        PackageArch arch = context.mock(PackageArch.class, "packageArch");

        context.checking(new Expectations() {{
            allowing(name).getName();
            will(returnValue("test-package"));
            allowing(evr).getVersion();
            will(returnValue("1.0.0"));
            allowing(evr).getRelease();
            will(returnValue("1"));
            allowing(evr).getEpoch();
            will(returnValue(null));
            allowing(arch).getLabel();
            will(returnValue("x86_64"));

            allowing(pkg).getPackageName();
            will(returnValue(name));
            allowing(pkg).getPackageEvr();
            will(returnValue(evr));
            allowing(pkg).getPackageArch();
            will(returnValue(arch));
            allowing(pkg).getId();
            will(returnValue(PACKAGE_ID.longValue()));
            allowing(pkg).getLastModified();
            will(returnValue(new Date(0)));
            allowing(pkg).getPath();
            will(returnValue("redhat/1/abc/test-package-1.0.0-1.x86_64.rpm"));
            allowing(pkg).isPartOfRetractedPatch();
            will(returnValue(false));
            allowing(pkg).getPackageKeys();
            will(returnValue(Collections.emptySet()));
        }});

        return pkg;
    }

    /**
     * Builds a package overview serialized by the registered PackageOverviewSerializer.
     *
     * @return the package overview
     */
    private PackageOverview packageOverview() {
        PackageOverview overview = context.mock(PackageOverview.class, "packageOverview");

        context.checking(new Expectations() {{
            allowing(overview).getId();
            will(returnValue(PACKAGE_ID.longValue()));
            allowing(overview).getPackageName();
            will(returnValue("test-package"));
            allowing(overview).getSummary();
            will(returnValue("A test package"));
            allowing(overview).getDescription();
            will(returnValue("A package used by the contract tests"));
            allowing(overview).getVersion();
            will(returnValue("1.0.0"));
            allowing(overview).getRelease();
            will(returnValue("1"));
            allowing(overview).getEpoch();
            will(returnValue(null));
            allowing(overview).getPackageArch();
            will(returnValue("x86_64"));
            allowing(overview).getPackageNvre();
            will(returnValue("test-package-1.0.0-1"));
            allowing(overview).getNvrea();
            will(returnValue("test-package-1.0.0-1.x86_64"));
            allowing(overview).getPackageChannels();
            will(returnValue(List.of()));
            allowing(overview).getProvider();
            will(returnValue("SUSE"));
        }});

        return overview;
    }

    @Test
    public void testGetDetails() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getDetails(with(mockUser), with(PACKAGE_ID));
            will(returnValue(Map.ofEntries(
                    Map.entry("id", PACKAGE_ID),
                    Map.entry("name", "test-package"),
                    Map.entry("epoch", ""),
                    Map.entry("version", "1.0.0"),
                    Map.entry("release", "1"),
                    Map.entry("arch_label", "x86_64"),
                    Map.entry("providing_channels", List.of("test-channel")),
                    Map.entry("build_host", "build.example.com"),
                    Map.entry("description", "A package used by the contract tests"),
                    Map.entry("checksum", "0123456789abcdef"),
                    Map.entry("checksum_type", "sha256"),
                    Map.entry("vendor", "SUSE"),
                    Map.entry("summary", "A test package"),
                    Map.entry("cookie", "build.example.com 1600000000"),
                    Map.entry("license", "GPL-2.0"),
                    Map.entry("file", "test-package-1.0.0-1.x86_64.rpm"),
                    Map.entry("build_date", "2026-01-01 00:00:00"),
                    Map.entry("last_modified_date", "2026-01-02 00:00:00"),
                    Map.entry("size", "12345"),
                    Map.entry("path", "redhat/1/abc/test-package-1.0.0-1.x86_64.rpm"),
                    Map.entry("payload_size", "23456"))));
        }});

        validateApiContract("/packages/getDetails", "GET")
                .withParams(Map.of("pid", new String[]{PACKAGE_ID.toString()}))
                .onHandlerMethod("getDetails", User.class, Integer.class);
    }

    @Test
    public void testListProvidingChannels() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listProvidingChannels(with(mockUser), with(PACKAGE_ID));
            will(returnValue(new Object[]{Map.of(
                    "label", "test-channel",
                    "parent_label", "test-parent-channel",
                    "name", "Test Channel")}));
        }});

        validateApiContract("/packages/listProvidingChannels", "GET")
                .withParams(Map.of("pid", new String[]{PACKAGE_ID.toString()}))
                .onHandlerMethod("listProvidingChannels", User.class, Integer.class);
    }

    @Test
    public void testListProvidingErrata() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listProvidingErrata(with(mockUser), with(PACKAGE_ID));
            will(returnValue(new Object[]{Map.of(
                    "advisory", "test-advisory",
                    "issue_date", "2026-01-01 00:00:00",
                    "last_modified_date", "2026-01-02 00:00:00",
                    "update_date", "2026-01-03 00:00:00",
                    "synopsis", "A test erratum",
                    "type", "Security Advisory")}));
        }});

        validateApiContract("/packages/listProvidingErrata", "GET")
                .withParams(Map.of("pid", new String[]{PACKAGE_ID.toString()}))
                .onHandlerMethod("listProvidingErrata", User.class, Integer.class);
    }

    @Test
    public void testListFiles() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listFiles(with(mockUser), with(PACKAGE_ID));
            will(returnValue(new Object[]{Map.of(
                    "path", "/usr/bin/test-package",
                    "type", "file",
                    "last_modified_date", "2026-01-02 00:00:00",
                    "checksum", "0123456789abcdef",
                    "checksum_type", "sha256",
                    "size", 12345L,
                    "linkto", "")}));
        }});

        validateApiContract("/packages/listFiles", "GET")
                .withParams(Map.of("pid", new String[]{PACKAGE_ID.toString()}))
                .onHandlerMethod("listFiles", User.class, Integer.class);
    }

    @Test
    public void testListChangelog() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listChangelog(with(mockUser), with(PACKAGE_ID));
            will(returnValue("* Thu Jan 01 2026 packager@example.com\n- initial build\n"));
        }});

        validateApiContract("/packages/listChangelog", "GET")
                .withParams(Map.of("pid", new String[]{PACKAGE_ID.toString()}))
                .onHandlerMethod("listChangelog", User.class, Integer.class);
    }

    @Test
    public void testListDependencies() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listDependencies(with(mockUser), with(PACKAGE_ID));
            will(returnValue(new Object[]{Map.of(
                    "dependency", "libtest.so.1",
                    "dependency_type", "requires",
                    "dependency_modifier", ">= 1.0.0")}));
        }});

        validateApiContract("/packages/listDependencies", "GET")
                .withParams(Map.of("pid", new String[]{PACKAGE_ID.toString()}))
                .onHandlerMethod("listDependencies", User.class, Integer.class);
    }

    @Test
    public void testListSourcePackages() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listSourcePackages(with(mockUser));
            will(returnValue(new Object[]{packageOverview()}));
        }});

        validateApiContract("/packages/listSourcePackages", "GET")
                .onHandlerMethod("listSourcePackages", User.class);
    }

    /**
     * The documentation recommends an empty epoch. An empty query string value parses to a JSON
     * null, so the handler is handed null rather than the empty string XML-RPC would deliver.
     */
    @Test
    public void testFindByNvrea() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).findByNvrea(with(mockUser), with("test-package"), with("1.0.0"),
                    with("1"), with(aNull(String.class)), with("x86_64"));
            will(returnValue(List.of(packageObject())));
        }});

        validateApiContract("/packages/findByNvrea", "GET")
                .withParams(Map.of(
                        "name", new String[]{"test-package"},
                        "version", new String[]{"1.0.0"},
                        "release", new String[]{"1"},
                        "epoch", new String[]{""},
                        "archLabel", new String[]{"x86_64"}))
                .onHandlerMethod("findByNvrea", User.class, String.class, String.class, String.class,
                        String.class, String.class);
    }

    @Test
    public void testGetPackageUrl() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getPackageUrl(with(mockUser), with(PACKAGE_ID));
            will(returnValue("https://example.com/rpc/api/packages/101/download"));
        }});

        validateApiContract("/packages/getPackageUrl", "GET")
                .withParams(Map.of("pid", new String[]{PACKAGE_ID.toString()}))
                .onHandlerMethod("getPackageUrl", User.class, Integer.class);
    }

    @Test
    public void testGetPackage() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getPackage(with(mockUser), with(PACKAGE_ID));
            will(returnValue(new byte[]{1, 2, 3, 4}));
        }});

        validateApiContract("/packages/getPackage", "GET")
                .withParams(Map.of("pid", new String[]{PACKAGE_ID.toString()}))
                .onHandlerMethod("getPackage", User.class, Integer.class);
    }

    @Test
    public void testRemovePackage() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).removePackage(with(mockUser), with(PACKAGE_ID));
            will(returnValue(1));
        }});

        validateApiContract("/packages/removePackage", "POST")
                .withBody(Map.of("pid", PACKAGE_ID))
                .onHandlerMethod("removePackage", User.class, Integer.class);
    }

    @Test
    public void testRemoveSourcePackage() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).removeSourcePackage(with(mockUser), with(PACKAGE_ID));
            will(returnValue(1));
        }});

        validateApiContract("/packages/removeSourcePackage", "POST")
                .withBody(Map.of("psid", PACKAGE_ID))
                .onHandlerMethod("removeSourcePackage", User.class, Integer.class);
    }
}
