/**
 * Copyright (c) 2014--2018 SUSE LLC
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
package com.redhat.rhn.manager.content.test;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.product.MgrSyncChannelDto;
import com.redhat.rhn.domain.product.test.SUSEProductTestUtils;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.manager.content.MgrSyncProductDto;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Tests for {@link ContentSyncManager} against expected output from previous
 * tool, mgr-ncc-sync.
 */
public class ContentSyncManagerNonRegressionTest extends BaseTestCaseWithUser {
    private static final String JARPATH = "/com/redhat/rhn/manager/content/test/";

    // manually compiled (eg. with LibreOffice)
    private static final String EXPECTED_PRODUCTS_CSV = JARPATH + "expected_products.csv";

    /** Logger instance. */
    private static Logger logger = Logger
            .getLogger(ContentSyncManagerNonRegressionTest.class);

    private static List<String> archs = Arrays.asList("i586", "ia64", "ppc64le", "ppc64", "ppc", "s390x", "s390", "x86_64", "aarch64");

    /** The failure strings. */
    private List<String> failures = new LinkedList<>();

    /**
     * Just here to prevent error about no test specified
     * @throws Exception
     */
    public void testNothing() throws Exception {
        assertTrue(true);
    }

    /**
     * Tests listProducts() against known correct output (originally from
     * mgr-ncc-sync).
     * @throws Exception if anything goes wrong
     */
    public void skipTestListProducts() throws Exception {
        File expectedProductsCSV = new File(TestUtils.findTestData(EXPECTED_PRODUCTS_CSV).getPath());

        SUSEProductTestUtils.createVendorSUSEProductEnvironment(user, null, true);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();
        try {
            ContentSyncManager csm = new ContentSyncManager();

            Collection<MgrSyncProductDto> baseProducts = csm.listProducts();

            Collection<MgrSyncProductDto> products = Stream.concat(
                    baseProducts.stream(),
                    baseProducts.stream()
                        .flatMap(p -> p.getExtensions().stream())
            ).collect(Collectors.toList());

            Set<MgrSyncProductDto> checkedProducts = new LinkedHashSet<>();

            List<String> lines = (List<String>) FileUtils.readLines(expectedProductsCSV);
            List<ExpectedProductDto> expectedProducts = lines.stream()
                .map(l -> Arrays.asList(l.split(",")))
                    .map(a -> new ExpectedProductDto(a.get(0), a.get(1), a.get(2),
                            a.get(3).equals("base"), a.subList(4, a.size())))
                    .collect(Collectors.toList());

            Map<ExpectedProductDto, ExpectedProductDto> parents =
                IntStream.range(0, expectedProducts.size())
                .boxed()
                .collect(Collectors.toMap(expectedProducts::get, i -> {
                    for (int j = i; j >= 0; j--) {
                        if (expectedProducts.get(j).isBase()) {
                            return expectedProducts.get(j);
                        }
                    }
                    return null;
                }));

            for (ExpectedProductDto ep : expectedProducts) {
                Optional<MgrSyncProductDto> actual = products.stream()
                    .filter(p ->
                        trimProductFriendlyName(p.getFriendlyName()).equals(ep.getName()) &&
                        p.getVersion().equals(ep.getVersion()) &&
                        p.getArch().orElse("").equals(ep.getArch()) &&
                        parents.get(ep).getChannelLabels().contains(
                                toChannelLabel(p.getBaseChannel()))
                    )
                    .findFirst();

                if (actual.isPresent()) {
                    MgrSyncProductDto product = actual.get();
                    checkedProducts.add(product);

                    boolean actualBase = product.getChannels().stream().anyMatch(c ->
                        StringUtils.isBlank(c.getParentLabel()));
                    if (actualBase != ep.isBase()) {
                        failures.add("Product " + product.toString() + " should be " +
                            (ep.isBase() ? "a base product" : "an extension product") +
                            " but it's not");
                    }

                    List<MgrSyncChannelDto> unexpectedChannels = product.getChannels().stream()
                        .filter(c -> !ep.getChannelLabels().contains(toChannelLabel(c)))
                        .collect(Collectors.toList());

                    unexpectedChannels.forEach(c -> {
                        failures.add("Product " + product.toString() +
                                " has unexpected channel " + toChannelLabel(c));
                    });

                    List<String> missingChannels = ep.getChannelLabels().stream().filter(
                        label -> !product.getChannels().stream()
                            .anyMatch(c -> toChannelLabel(c).equals(label))
                    ).collect(Collectors.toList());

                    missingChannels.forEach(c -> {
                        failures.add("Product " + product.toString() +
                                " does not have expected channel " + c);
                    });
                }
                else {
                    failures.add("Product was expected but not found: " + ep +
                            "(base channel in " + parents.get(ep).getChannelLabels() + ")");
                    failures.add(">>> Maybe you need to request a Beta Key?");
                }
            }

            products.removeAll(checkedProducts);
            products.forEach(p -> {
                failures.add("Product was not expected: " + p);
            });
        }
        finally {
            SUSEProductTestUtils.deleteIfTempFile(expectedProductsCSV);
        }

        if (!failures.isEmpty()) {
            for (String string : failures) {
                java.lang.System.out.println(string);
                logger.error(string);
            }
            fail("See log for output");
        }
    }

    private String trimProductFriendlyName(String friendlyName) {
        for (String arch : archs) {
            friendlyName = friendlyName.replaceAll(arch, "");
        }
        return friendlyName.trim();
    }

    private String toChannelLabel(MgrSyncChannelDto c) {
        return c.getLabel() + (c.isMandatory() ? "*" : "");
    }

}
