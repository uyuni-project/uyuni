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

import com.redhat.rhn.frontend.dto.PackageOverview;
import com.redhat.rhn.frontend.xmlrpc.packages.search.PackagesSearchHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class PackagesSearchHandlerContractTest extends BaseOpenApiTest {

    private static final String SESSION_KEY = "fake-session";

    @Override
    protected String getApiNamespace() {
        return "packages.search";
    }

    @Override
    protected Class<PackagesSearchHandler> getHandlerClass() {
        return PackagesSearchHandler.class;
    }

    private PackagesSearchHandler handler() {
        return (PackagesSearchHandler) handlerMock;
    }

    /**
     * Builds a package overview serialized by the registered PackageOverviewSerializer, so the
     * response is validated against the documented schema.
     */
    private PackageOverview packageOverview() {
        PackageOverview overview = new PackageOverview();
        overview.setId(1000L);
        overview.setPackageName("kernel");
        overview.setSummary("The Linux kernel");
        overview.setDescription("The kernel package contains the Linux kernel.");
        overview.setVersion("6.4.0");
        overview.setRelease("150600.1.1");
        overview.setPackageArch("x86_64");
        overview.setEpoch("1");
        overview.setProvider("SUSE");
        return overview;
    }

    @Test
    public void testName() throws Exception {
        String name = "kernel";

        context.checking(new Expectations() {{
            oneOf(handler()).name(with(SESSION_KEY), with(name));
            will(returnValue(List.of(packageOverview())));
        }});

        validateApiContract("/packages.search/name", "GET")
                .withParams(Map.of("name", new String[] {name}))
                .onHandlerMethod("name", String.class, String.class);
    }

    @Test
    public void testNameAndDescription() throws Exception {
        String query = "kernel";

        context.checking(new Expectations() {{
            oneOf(handler()).nameAndDescription(with(SESSION_KEY), with(query));
            will(returnValue(List.of(packageOverview())));
        }});

        validateApiContract("/packages.search/nameAndDescription", "GET")
                .withParams(Map.of("query", new String[] {query}))
                .onHandlerMethod("nameAndDescription", String.class, String.class);
    }

    @Test
    public void testNameAndSummary() throws Exception {
        String query = "kernel";

        context.checking(new Expectations() {{
            oneOf(handler()).nameAndSummary(with(SESSION_KEY), with(query));
            will(returnValue(List.of(packageOverview())));
        }});

        validateApiContract("/packages.search/nameAndSummary", "GET")
                .withParams(Map.of("query", new String[] {query}))
                .onHandlerMethod("nameAndSummary", String.class, String.class);
    }

    @Test
    public void testAdvanced() throws Exception {
        String luceneQuery = "name:kernel AND version:6.4.0";

        context.checking(new Expectations() {{
            oneOf(handler()).advanced(with(SESSION_KEY), with(luceneQuery));
            will(returnValue(List.of(packageOverview())));
        }});

        validateApiContract("/packages.search/advanced", "GET")
                .withParams(Map.of("luceneQuery", new String[] {luceneQuery}))
                .onHandlerMethod("advanced", String.class, String.class);
    }

    @Test
    public void testAdvancedWithChannel() throws Exception {
        String luceneQuery = "name:kernel";
        String channelLabel = "sle-product-sles15-sp6-pool-x86_64";

        context.checking(new Expectations() {{
            oneOf(handler()).advancedWithChannel(with(SESSION_KEY), with(luceneQuery), with(channelLabel));
            will(returnValue(List.of(packageOverview())));
        }});

        validateApiContract("/packages.search/advancedWithChannel", "GET")
                .withParams(Map.of(
                        "luceneQuery", new String[] {luceneQuery},
                        "channelLabel", new String[] {channelLabel}
                ))
                .onHandlerMethod("advancedWithChannel", String.class, String.class, String.class);
    }

    @Test
    public void testAdvancedWithActKey() throws Exception {
        String luceneQuery = "name:kernel";
        String activationKey = "1-my-activation-key";

        context.checking(new Expectations() {{
            oneOf(handler()).advancedWithActKey(with(SESSION_KEY), with(luceneQuery), with(activationKey));
            will(returnValue(List.of(packageOverview())));
        }});

        validateApiContract("/packages.search/advancedWithActKey", "GET")
                .withParams(Map.of(
                        "luceneQuery", new String[] {luceneQuery},
                        "activationKey", new String[] {activationKey}
                ))
                .onHandlerMethod("advancedWithActKey", String.class, String.class, String.class);
    }
}
