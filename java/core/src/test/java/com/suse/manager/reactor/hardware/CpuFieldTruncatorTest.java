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
package com.suse.manager.reactor.hardware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.suse.manager.reactor.utils.ValueMap;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Tests for {@link CpuFieldTruncator}
 */
public class CpuFieldTruncatorTest {

    public static final String OTHER_KEY = "other_key";
    public static final String TEST_BOGOMIPS_KEY = "bogomips";
    public static final String TEST_CACHE_SIZE_KEY = "cache size";
    public static final String TEST_CPU_FAMILY_KEY = "cpu family";
    public static final String TEST_LONG_STRING = "A".repeat(50);
    public static final String TEST_MODEL_KEY = "model";
    public static final String TEST_STEPPING_KEY = "stepping";
    public static final String TEST_VENDOR_KEY = "vendor_id";

    @ParameterizedTest
    @MethodSource("vendorTestData")
    public void testVendorTruncation(String key, String value, String expected) {
        Map<String, Object> data = new HashMap<>();
        if (value != null) {
            data.put(TEST_VENDOR_KEY, value);
        }
        ValueMap cpuInfo = new ValueMap(data);

        String result = CpuFieldTruncator.vendor(cpuInfo, key);
        assertEquals(expected, result);
    }

    private static Stream<Arguments> vendorTestData() {
        return Stream.of(
            Arguments.of(null, "AuthenticAMD", ""),
            Arguments.of("", "AuthenticAMD", ""),
            Arguments.of(OTHER_KEY, "AuthenticAMD", ""),
            Arguments.of(TEST_VENDOR_KEY, "AuthenticAMD", "AuthenticAMD"),
            Arguments.of(TEST_VENDOR_KEY, TEST_LONG_STRING, "A".repeat(HardwareConstants.CPU_VENDOR_LENGTH)),
            Arguments.of(TEST_VENDOR_KEY, null, ""),
            Arguments.of(TEST_VENDOR_KEY, "", "")
        );
    }

    @ParameterizedTest
    @MethodSource("modelTestData")
    public void testModelTruncation(String input, String expected) {
        String result = CpuFieldTruncator.model(input);
        if (expected == null) {
            assertNull(result);
        }
        else {
            assertEquals(expected, result);
        }
    }

    private static Stream<Arguments> modelTestData() {
        return Stream.of(
            Arguments.of("QEMU Virtual CPU version 2.5+", "QEMU Virtual CPU version 2.5+"),
            Arguments.of(TEST_LONG_STRING, "A".repeat(HardwareConstants.CPU_MODEL_LENGTH)),
            Arguments.of(null, null),
            Arguments.of("", "")
        );
    }

    @ParameterizedTest
    @MethodSource("versionTestData")
    public void testVersionTruncation(String key, String value, String expected) {
        Map<String, Object> data = new HashMap<>();
        if (value != null) {
            data.put(TEST_MODEL_KEY, value);
        }
        ValueMap cpuInfo = new ValueMap(data);

        String result = CpuFieldTruncator.version(cpuInfo, key);
        assertEquals(expected, result);
    }

    private static Stream<Arguments> versionTestData() {
        return Stream.of(
            Arguments.of(null, "123", ""),
            Arguments.of("", "123", ""),
            Arguments.of(OTHER_KEY, "123", ""),
            Arguments.of(TEST_MODEL_KEY, "13", "13"),
            Arguments.of(TEST_MODEL_KEY, TEST_LONG_STRING, "A".repeat(HardwareConstants.CPU_VERSION_LENGTH)),
            Arguments.of(TEST_MODEL_KEY, null, ""),
            Arguments.of(TEST_MODEL_KEY, "", "")
        );
    }

    @ParameterizedTest
    @MethodSource("familyTestData")
    public void testFamilyTruncation(String key, String value, String expected) {
        Map<String, Object> data = new HashMap<>();
        if (value != null) {
            data.put(TEST_CPU_FAMILY_KEY, value);
        }
        ValueMap cpuInfo = new ValueMap(data);

        String result = CpuFieldTruncator.family(cpuInfo, key);
        assertEquals(expected, result);
    }

    private static Stream<Arguments> familyTestData() {
        return Stream.of(
            Arguments.of(null, "123", ""),
            Arguments.of("", "123", ""),
            Arguments.of(OTHER_KEY, "123", ""),
            Arguments.of(TEST_CPU_FAMILY_KEY, "6", "6"),
            Arguments.of(TEST_CPU_FAMILY_KEY, TEST_LONG_STRING, "A".repeat(HardwareConstants.CPU_FAMILY_LENGTH)),
            Arguments.of(TEST_CPU_FAMILY_KEY, null, ""),
            Arguments.of(TEST_CPU_FAMILY_KEY, "", "")
        );
    }

    @ParameterizedTest
    @MethodSource("steppingTestData")
    public void testSteppingTruncation(String key, String value, String expected) {
        Map<String, Object> data = new HashMap<>();
        if (value != null) {
            data.put(TEST_STEPPING_KEY, value);
        }
        ValueMap cpuInfo = new ValueMap(data);

        String result = CpuFieldTruncator.stepping(cpuInfo, key);
        assertEquals(expected, result);
    }

    private static Stream<Arguments> steppingTestData() {
        return Stream.of(
            Arguments.of(null, "123", ""),
            Arguments.of("", "123", ""),
            Arguments.of(OTHER_KEY, "123", ""),
            Arguments.of(TEST_STEPPING_KEY, "31", "31"),
            Arguments.of(TEST_STEPPING_KEY, TEST_LONG_STRING, "A".repeat(HardwareConstants.CPU_STEPPING_LENGTH)),
            Arguments.of(TEST_STEPPING_KEY, null, ""),
            Arguments.of(TEST_STEPPING_KEY, "", "")
        );
    }

    @ParameterizedTest
    @MethodSource("flagsTestData")
    public void testFlagsTruncation(String input, String expected, Integer expectedLength) {
        String result = CpuFieldTruncator.flags(input);
        if (input == null) {
            assertNull(result);
        }
        else {
            assertEquals(expected, result);
            if (expectedLength != null) {
                assertEquals(expectedLength, result.length());
            }
        }
    }

    private static Stream<Arguments> flagsTestData() {
        String longFlags = "flag ".repeat(500);
        return Stream.of(
            Arguments.of("fpu vme de pse tsc", "fpu vme de pse tsc", 18),
            Arguments.of("fpu", "fpu", 3),
            Arguments.of(longFlags, longFlags.substring(0, HardwareConstants.CPU_FLAGS_LENGTH),
                        HardwareConstants.CPU_FLAGS_LENGTH),
            Arguments.of(null, null, null),
            Arguments.of("", "", 0)
        );
    }

    @ParameterizedTest
    @MethodSource("bogomipsTestData")
    public void testBogoMipsTruncation(String key, String value, String expected) {
        Map<String, Object> data = new HashMap<>();
        if (value != null) {
            data.put(TEST_BOGOMIPS_KEY, value);
        }
        ValueMap cpuInfo = new ValueMap(data);

        String result = CpuFieldTruncator.bogomips(cpuInfo, key);
        assertEquals(expected, result);
    }

    private static Stream<Arguments> bogomipsTestData() {
        return Stream.of(
            Arguments.of(null, "4589.35", ""),
            Arguments.of("", "3999.93", ""),
            Arguments.of(OTHER_KEY, "3241.00", ""),
            Arguments.of(TEST_BOGOMIPS_KEY, "4589.35", "4589.35"),
            Arguments.of(TEST_BOGOMIPS_KEY, TEST_LONG_STRING, "A".repeat(HardwareConstants.CPU_BOGOMIPS_LENGTH)),
            Arguments.of(TEST_BOGOMIPS_KEY, null, ""),
            Arguments.of(TEST_BOGOMIPS_KEY, "", "")
        );
    }

    @ParameterizedTest
    @MethodSource("cacheTestData")
    public void testCacheTruncation(String key, String value, String expected) {
        Map<String, Object> data = new HashMap<>();
        if (value != null) {
            data.put(TEST_CACHE_SIZE_KEY, value);
        }
        ValueMap cpuInfo = new ValueMap(data);

        String result = CpuFieldTruncator.cache(cpuInfo, key);
        assertEquals(expected, result);
    }

    private static Stream<Arguments> cacheTestData() {
        return Stream.of(
            Arguments.of(null, "8192 KB", ""),
            Arguments.of("", "8192 KB", ""),
            Arguments.of(OTHER_KEY, "8192 KB", ""),
            Arguments.of(TEST_CACHE_SIZE_KEY, "8192 KB", "8192 KB"),
            Arguments.of(TEST_CACHE_SIZE_KEY, TEST_LONG_STRING, "A".repeat(HardwareConstants.CPU_CACHE_LENGTH)),
            Arguments.of(TEST_CACHE_SIZE_KEY, null, ""),
            Arguments.of(TEST_CACHE_SIZE_KEY, "", "")
        );
    }

    @ParameterizedTest
    @MethodSource("mhzTestData")
    public void testMHzTruncation(String input, String expected) {
        String result = CpuFieldTruncator.mhz(input);
        if (expected == null) {
            assertNull(result);
        }
        else {
            assertEquals(expected, result);
        }
    }

    private static Stream<Arguments> mhzTestData() {
        return Stream.of(
            Arguments.of("3800.000000", "3800.000000"),
            Arguments.of(TEST_LONG_STRING, "A".repeat(HardwareConstants.CPU_MHZ_LENGTH)),
            Arguments.of(null, null),
            Arguments.of("", "")
        );
    }
}
