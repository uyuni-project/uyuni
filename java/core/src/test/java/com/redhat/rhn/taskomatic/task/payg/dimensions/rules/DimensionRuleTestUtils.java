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
package com.redhat.rhn.taskomatic.task.payg.dimensions.rules;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductSet;
import com.redhat.rhn.domain.server.InstalledProduct;
import com.redhat.rhn.domain.server.Server;

import org.apache.commons.lang3.RandomStringUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;

import java.util.Optional;
import java.util.Set;

/**
 * Utility class providing factory methods to create JMock mocks of domain objects commonly used
 * in {@link com.redhat.rhn.taskomatic.task.payg.dimensions.rules} unit tests.
 *
 * <p>All methods require a {@link Mockery} context, which is typically obtained from
 * {@link com.redhat.rhn.testing.MockObjectTestCase}.
 */
class DimensionRuleTestUtils {

    private DimensionRuleTestUtils() {
        // Prevent instantiation
    }

    /**
     * Creates a mock PAYG {@link Server} with the given installed products and no subscribed channels.
     *
     * @param context the JMock mockery context
     * @param installedProducts the set of installed products to associate with the server
     * @return a mock Server with {@code isPayg()} returning {@code true}
     */
    public static Server mockPaygServer(Mockery context, Set<InstalledProduct> installedProducts) {
        return mockServer(context, true, installedProducts, Set.of());
    }

    /**
     * Creates a mock PAYG {@link Server} with the given installed products and subscribed channels.
     *
     * @param context the JMock mockery context
     * @param installedProducts the set of installed products to associate with the server
     * @param channels the set of channels the server is subscribed to
     * @return a mock Server with {@code isPayg()} returning {@code true}
     */
    public static Server mockPaygServer(Mockery context, Set<InstalledProduct> installedProducts,
                                        Set<Channel> channels) {
        return mockServer(context, true, installedProducts, channels);
    }

    /**
     * Creates a mock BYOS {@link Server} with the given installed products and no subscribed channels.
     *
     * @param context the JMock mockery context
     * @param installedProducts the set of installed products to associate with the server
     * @return a mock Server with {@code isPayg()} returning {@code false}
     */
    public static Server mockServer(Mockery context, Set<InstalledProduct> installedProducts) {
        return mockServer(context, false, installedProducts, Set.of());
    }

    /**
     * Creates a mock BYOS {@link Server} with the given installed products and subscribed channels.
     *
     * @param context the JMock mockery context
     * @param installedProducts the set of installed products to associate with the server
     * @param channels the set of channels the server is subscribed to
     * @return a mock Server with {@code isPayg()} returning {@code false}
     */
    public static Server mockServer(Mockery context, Set<InstalledProduct> installedProducts, Set<Channel> channels) {
        return mockServer(context, false, installedProducts, channels);
    }

    private static Server mockServer(Mockery context, boolean payg, Set<InstalledProduct> installedProducts,
                                    Set<Channel> channels) {
        Server server = context.mock(Server.class, RandomStringUtils.insecure().nextNumeric(10));
        context.checking(new Expectations() {{
            allowing(server).isPayg();
            will(returnValue(payg));

            allowing(server).getInstalledProducts();
            will(returnValue(installedProducts));

            allowing(server).getInstalledProductSet();
            will(returnValue(Optional.of(new SUSEProductSet(installedProducts))));

            allowing(server).getChannels();
            will(returnValue(channels));
        }});

        return server;
    }

    /**
     * Creates a mock {@link Channel} with the given label and channel family.
     *
     * @param context the JMock mockery context
     * @param channelLabel the label to return from {@code getLabel()}
     * @param channelFamily the label of the channel family
     * @return a mock Channel
     */
    public static Channel mockChannel(Mockery context, String channelLabel, String channelFamily) {
        Channel channel = context.mock(Channel.class, channelLabel);

        context.checking(new Expectations() {{
            allowing(channel).getLabel();
            will(returnValue(channelLabel));

            allowing(channel).getChannelFamilies();
            will(returnValue(Set.of(new ChannelFamily(channelFamily))));
        }});

        return channel;
    }

    /**
     * Creates a mock base {@link InstalledProduct} with default version {@code "1.0"} and channel family
     * {@code "BASE"}.
     *
     * @param context the JMock mockery context
     * @param productName the product name
     * @return a mock InstalledProduct with {@code isBaseproduct()} returning {@code true}
     */
    public static InstalledProduct mockBaseProduct(Mockery context, String productName) {
        return mockProduct(context, productName, "1.0", true, "BASE");
    }

    /**
     * Creates a mock base {@link InstalledProduct} with the given version and channel family.
     *
     * @param context the JMock mockery context
     * @param productName the product name
     * @param version the product version
     * @param channelFamily the label of the associated channel family
     * @return a mock InstalledProduct with {@code isBaseproduct()} returning {@code true}
     */
    public static InstalledProduct mockBaseProduct(Mockery context, String productName, String version,
                                                   String channelFamily) {
        return mockProduct(context, productName, version, true, channelFamily);
    }

    /**
     * Creates a mock addon (non-base) {@link InstalledProduct} with default version {@code "1.0"} and channel
     * family {@code "MODULE"}.
     *
     * @param context the JMock mockery context
     * @param productName the product name
     * @return a mock InstalledProduct with {@code isBaseproduct()} returning {@code false}
     */
    public static InstalledProduct mockAddonProduct(Mockery context, String productName) {
        return mockProduct(context, productName, "1.0", false, "MODULE");
    }

    /**
     * Creates a mock addon (non-base) {@link InstalledProduct} with the given version and channel family.
     *
     * @param context the JMock mockery context
     * @param productName the product name
     * @param version the product version
     * @param channelFamily the label of the associated channel family
     * @return a mock InstalledProduct with {@code isBaseproduct()} returning {@code false}
     */
    public static InstalledProduct mockAddonProduct(Mockery context, String productName, String version,
                                                   String channelFamily) {
        return mockProduct(context, productName, version, false, channelFamily);
    }

    private static InstalledProduct mockProduct(Mockery context, String productName, String version, boolean isBase,
                                               String channelFamily) {
        InstalledProduct installedProduct = context.mock(InstalledProduct.class, "installed_" + productName);
        SUSEProduct suseProduct = context.mock(SUSEProduct.class, productName);

        context.checking(new Expectations() {{
            allowing(suseProduct).isBase();
            will(returnValue(isBase));

            allowing(suseProduct).getName();
            will(returnValue(productName));

            allowing(suseProduct).getVersion();
            will(returnValue(version));

            allowing(suseProduct).getFriendlyName();
            will(returnValue("Mock for " + productName + " " + version));

            allowing(suseProduct).getChannelFamily();
            will(returnValue(new ChannelFamily(channelFamily)));

            allowing(installedProduct).isBaseproduct();
            will(returnValue(isBase));

            allowing(installedProduct).getSUSEProduct();
            will(returnValue(suseProduct));
        }});

        return installedProduct;
    }
}
