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

import static com.redhat.rhn.common.ExceptionMessage.NOT_INSTANTIABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.UyuniError;

import com.suse.proxy.ProxyContainerImagesEnum;
import com.suse.proxy.update.ProxyConfigUpdateContext;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utils for ProxyConfigUpdate tests
 */
public class ProxyConfigUpdateTestUtils {

    public static final String DUMMY_PROXY_FQDN = "proxy.fqdn.com";
    public static final String DUMMY_PARENT_FQDN = "parent.fqdn.com";
    public static final String DUMMY_ROOT_CA = "rootCA";
    public static final String DUMMY_INTERMEDIATE_CA_1 = "intermediateCA1";
    public static final String DUMMY_INTERMEDIATE_CA_2 = "intermediateCA2";
    public static final String DUMMY_PROXY_CERT = "proxyCert";
    public static final String DUMMY_PROXY_KEY = "proxyKey";
    public static final String DUMMY_TAG = "tag";
    public static final String DUMMY_ADMIN_MAIL = "admin@suse.com";
    public static final String DUMMY_URL_PREFIX = "http://suse.com/images/";
    public static final String DUMMY_SSH_PARENT = "dummyServerSshKeyPub";
    public static final String DUMMY_SSH_PUB = "dummyServerSshPush";
    public static final String DUMMY_SSH_KEY = "dummyServerSshPushPub";

    public static final long DUMMY_SERVER_ID = 123L;
    public static final int DUMMY_PROXY_PORT = 8080;
    public static final int DUMMY_MAX_CACHE = 1024;

    private ProxyConfigUpdateTestUtils() {
        throw new UnsupportedOperationException(NOT_INSTANTIABLE);
    }

    /**
     * Asserts the expected error messages are present in the context
     *
     * @param expectedErrorMessages the expected error messages array string
     * @param context               the context
     */
    public static void assertExpectedErrors(String[] expectedErrorMessages, ProxyConfigUpdateContext context) {
        assertTrue(context.getErrorReport().hasErrors());
        assertEquals(expectedErrorMessages.length, context.getErrorReport().getErrors().size());

        Set<String> actualErrorMessages =
                context.getErrorReport().getErrors().stream().map(UyuniError::getMessage).collect(Collectors.toSet());
        assertTrue(actualErrorMessages.containsAll(Set.of(expectedErrorMessages)));
    }

    /**
     * Generates well-formed dummy URL for the given image.
     * @param proxyContainerImagesEnum the image
     * @return the dummy URL
     */
    public static String getDummyUrl(ProxyContainerImagesEnum proxyContainerImagesEnum) {
        return DUMMY_URL_PREFIX + proxyContainerImagesEnum.getImageName();
    }

    /**
     * Generates a dynamic dummy tag for the given image.
     * @param proxyContainerImagesEnum the image
     * @return the dummy tag
     */
    public static String getDummyTag(ProxyContainerImagesEnum proxyContainerImagesEnum) {
        return DUMMY_TAG + "_" + proxyContainerImagesEnum.getImageName();
    }

}
