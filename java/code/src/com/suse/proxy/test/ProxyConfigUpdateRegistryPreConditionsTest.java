/*
 * Copyright (c) 2025 SUSE LLC
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

package com.suse.proxy.test;

import static com.suse.proxy.ProxyConfigUtils.SOURCE_MODE_REGISTRY;
import static com.suse.proxy.ProxyContainerImagesEnum.PROXY_HTTPD;
import static com.suse.proxy.ProxyContainerImagesEnum.PROXY_SALT_BROKER;
import static com.suse.proxy.ProxyContainerImagesEnum.PROXY_SQUID;
import static com.suse.proxy.ProxyContainerImagesEnum.PROXY_SSH;
import static com.suse.proxy.ProxyContainerImagesEnum.PROXY_TFTPD;
import static com.suse.proxy.test.ProxyConfigUpdateTestUtils.assertExpectedErrors;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.redhat.rhn.testing.MockObjectTestCase;

import com.suse.manager.api.ParseException;
import com.suse.manager.webui.utils.gson.ProxyConfigUpdateJson;
import com.suse.proxy.ProxyContainerImagesEnum;
import com.suse.proxy.ProxyRegistryUtils;
import com.suse.proxy.RegistryUrl;
import com.suse.proxy.update.ProxyConfigUpdateAcquisitor;
import com.suse.proxy.update.ProxyConfigUpdateContext;
import com.suse.proxy.update.ProxyConfigUpdateRegistryPreConditions;
import com.suse.proxy.update.ProxyConfigUpdateValidation;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.Test;

import java.util.EnumMap;
import java.util.Map;

/**
 * Tests for the {@link ProxyConfigUpdateRegistryPreConditions} class
 * These will assume the previous step in the chain of responsibility {@link ProxyConfigUpdateAcquisitor} and
 * {@link ProxyConfigUpdateValidation} have been executed and, no errors have been added to the context.
 */
@SuppressWarnings({"java:S1171", "java:S3599"})
public class ProxyConfigUpdateRegistryPreConditionsTest extends MockObjectTestCase {

    /**
     * Test a scenario where ProxyConfigUpdateJson is resolved as being empty (basically, when sourceMode is not set)
     */
    @Test
    public void testSuccessWhenBlankRequest() {
        ProxyConfigUpdateContext proxyConfigUpdateContext =
                new ProxyConfigUpdateContext(new ProxyConfigUpdateJson(), null, null, null);
        new ProxyConfigUpdateRegistryPreConditions().handle(proxyConfigUpdateContext);
        assertFalse(proxyConfigUpdateContext.getErrorReport().hasErrors());
    }

    /**
     * Test a scenario when sourceMode is set to "Registry"
     */
    @Test
    public void testSuccessWhenSourceModeIsRpm() {
        ProxyConfigUpdateJson request = new ProxyConfigUpdateJsonBuilder().sourceRPM().build();
        ProxyConfigUpdateContext context = new ProxyConfigUpdateContext(request, null, null, null);
        new ProxyConfigUpdateRegistryPreConditions().handle(context);
        assertFalse(context.getErrorReport().hasErrors());
    }

    /**
     * Test a scenario when sourceMode is set to "Registry" but proxy RegistryUrls failed to be created
     * (on previous steps)
     */
    @Test
    public void testFailWhenRegistryUrlNotProvided() {
        final String[] expectedErrorMessages = {
                "No registry URL provided for image PROXY_HTTPD",
                "No registry URL provided for image PROXY_SALT_BROKER",
                "No registry URL provided for image PROXY_SQUID",
                "No registry URL provided for image PROXY_SSH",
                "No registry URL provided for image PROXY_TFTPD"
        };

        ProxyConfigUpdateJson request = new ProxyConfigUpdateJsonBuilder().sourceMode(SOURCE_MODE_REGISTRY).build();
        ProxyConfigUpdateContext proxyConfigUpdateContext =
                new ProxyConfigUpdateContext(request, null, null, null);

        //
        new ProxyConfigUpdateRegistryPreConditions().handle(proxyConfigUpdateContext);
        assertExpectedErrors(expectedErrorMessages, proxyConfigUpdateContext);
    }

    @SuppressWarnings("java:S1130") // Suppress the ParseException warning (due to the mocked getTags() method)
    @Test
    public void testWhenGetTagsThrowsParseException() throws ParseException {
        final String[] expectedErrorMessages = {
                "Failed to get tags for: proxy-httpd",
                "Failed to get tags for: proxy-salt-broker",
                "Failed to get tags for: proxy-squid",
                "Failed to get tags for: proxy-ssh",
                "Failed to get tags for: proxy-tftpd"
        };

        //
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        ProxyConfigUpdateJson request = new ProxyConfigUpdateJsonBuilder().sourceMode(SOURCE_MODE_REGISTRY).build();
        ProxyConfigUpdateContext proxyConfigUpdateContext =
                new ProxyConfigUpdateContext(request, null, null, null);

        Map<ProxyContainerImagesEnum, RegistryUrl> registryUrls = new EnumMap<>(ProxyContainerImagesEnum.class);
        RegistryUrl registryUrl = context.mock(RegistryUrl.class);
        registryUrls.put(PROXY_HTTPD, registryUrl);
        registryUrls.put(PROXY_SALT_BROKER, registryUrl);
        registryUrls.put(PROXY_SQUID, registryUrl);
        registryUrls.put(PROXY_SSH, registryUrl);
        registryUrls.put(PROXY_TFTPD, registryUrl);
        proxyConfigUpdateContext.getRegistryUrls().putAll(registryUrls);

        // Inject the mock service so registryUtils.getTags() will not throw an exception
        ProxyRegistryUtils proxyRegistryUtils = context.mock(ProxyRegistryUtils.class);
        ProxyConfigUpdateRegistryPreConditions preConditions =
                new ProxyConfigUpdateRegistryPreConditions(proxyRegistryUtils);

        context.checking(new Expectations() {{
            exactly(5).of(proxyRegistryUtils).getTags(with(any(RegistryUrl.class)));
            will(throwException(new ParseException("Test exception")));
        }});

        //
        preConditions.handle(proxyConfigUpdateContext);
        assertExpectedErrors(expectedErrorMessages, proxyConfigUpdateContext);
    }

}
