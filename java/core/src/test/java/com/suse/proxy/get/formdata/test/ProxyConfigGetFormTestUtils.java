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

package com.suse.proxy.get.formdata.test;

import static com.redhat.rhn.common.ExceptionMessage.NOT_INSTANTIABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.UyuniError;
import com.redhat.rhn.common.UyuniErrorReport;
import com.redhat.rhn.common.conf.ConfigDefaults;

import org.jmock.Expectations;
import org.jmock.api.Invocation;
import org.jmock.junit5.JUnit5Mockery;
import org.jmock.lib.action.CustomAction;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class ProxyConfigGetFormTestUtils {

    public static final Long SERVER_ID = 123L;

    /**
     * Overrides the {@link ConfigDefaults#isUyuni()} method for mocking purposes
     *
     * @param context the JUnit5Mockery context
     * @param expectedIsUyuni return value for the isUyuni method
     */
    @SuppressWarnings({"java:S1171", "java:S3599"})
    public static void mockConfigDefaults(JUnit5Mockery context, boolean expectedIsUyuni) {
        ConfigDefaults configDefaults = ConfigDefaults.get();
        ConfigDefaults mockConfigDefaults = context.mock(ConfigDefaults.class);

        context.checking(new Expectations() {{
            allowing(mockConfigDefaults).isUyuni();
            will(returnValue(expectedIsUyuni));

            allowing(mockConfigDefaults);
            will(new CustomAction("delegate to real object") {
                public Object invoke(Invocation invocation) throws Throwable {
                    Method m = invocation.getInvokedMethod();
                    return m.invoke(configDefaults, invocation.getParametersAsArray());
                }
            });
        }});

        try {
            setConfigDefaultsInstance(mockConfigDefaults);
        }
        catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Overrides the ConfigDefaults instance
     * @param configDefaults the ConfigDefaults instance
     * @throws NoSuchFieldException if a field with the specified name is not found.
     * @throws IllegalAccessException if the field is not accessible.
     */
    public static void setConfigDefaultsInstance(ConfigDefaults configDefaults)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = ConfigDefaults.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(null, configDefaults);
    }

    /**
     * Helper method to assert no errors in error report
     * @param errorReport the error report to check
     */
    public static void assertNoErrors(UyuniErrorReport errorReport) {
        assertFalse(errorReport.hasErrors(),
                "Expected no errors but found: " + errorReport.getErrors());
    }

    /**
     * Helper method to assert errors in context
     * @param errorReport the error report to check
     * @param expectedMessages the expected error messages
     */
    public static void assertErrors(UyuniErrorReport errorReport, String... expectedMessages) {
        List<UyuniError> errors = errorReport.getErrors();
        assertEquals(expectedMessages.length, errors.size());
        for (UyuniError error : errors) {
            assertTrue(Arrays.stream(expectedMessages).anyMatch(message -> error.getMessage().startsWith(message)),
                    "Expected error message to start with one of '" + Arrays.toString(expectedMessages) +
                            "' but was: " + error.getMessage());
        }
    }

    /**
     * Private constructor to avoid instantiation
     */
    private ProxyConfigGetFormTestUtils() {
        throw new UnsupportedOperationException(NOT_INSTANTIABLE);
    }
}
