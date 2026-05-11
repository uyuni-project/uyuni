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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.server.CPU;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.testing.RhnJmockBaseTestCase;

import com.suse.manager.reactor.utils.ValueMap;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Tests for {@link CpuInfoMapper}
 */
public class CpuInfoMapperTest extends RhnJmockBaseTestCase {

    private MinionServer mockServer;


    @BeforeEach
    public void setUp() {
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        mockServer = context.mock(MinionServer.class);
    }

    @ParameterizedTest
    @MethodSource("cpuArchData")
    public void testGetCpuArch(String input, String expected) {
        Map<String, Object> grains = new HashMap<>();
        grains.put("cpuarch", input);

        CpuInfoMapper mapper = new CpuInfoMapper(mockServer, new ValueMap(grains));
        assertEquals(expected, mapper.getCpuArch());
    }

    private static Stream<Arguments> cpuArchData() {
        return Stream.of(
            // null/empty
            Arguments.of(null, ""),
            Arguments.of("", ""),
            // x86 variants should be normalized to i386
            Arguments.of("i686", "i386"),
            Arguments.of("i586", "i386"),
            Arguments.of("i486", "i386"),
            Arguments.of("i386", "i386"),
            // x86_64 should remain x86_64
            Arguments.of("x86_64", "x86_64"),
            // Other architectures should remain unchanged
            Arguments.of("aarch64", "aarch64"),
            Arguments.of("ppc64le", "ppc64le"),
            Arguments.of("s390x", "s390x"),
            Arguments.of("armv7l", "armv7l"),
            Arguments.of("SOMEOTHER", "someother")
        );
    }

    @Test
    public void testMapX86CpuInfoSetsExpectedFields() {
        Map<String, Object> grains = new HashMap<>();
        grains.put("cpuarch", "x86_64");
        grains.put("cpu_model", "Intel(R) Xeon(R) Gold 6226R CPU @ 2.90GHz");

        Map<String, Object> cpuInfoData = new HashMap<>();
        cpuInfoData.put("cpu MHz", "2000.000");
        cpuInfoData.put("vendor_id", "GenuineIntel");
        cpuInfoData.put("stepping", "10");
        cpuInfoData.put("cpu family", "6");
        cpuInfoData.put("cache size", "8192 KB");
        cpuInfoData.put("bogomips", "3999.93");
        cpuInfoData.put("flags", Arrays.asList("fpu", "vme", "de", "pse", "tsc"));
        cpuInfoData.put("model", "142");

        CPU cpu = new CPU();
        ValueMap cpuInfo = new ValueMap(cpuInfoData);
        CpuInfoMapper mapper = new CpuInfoMapper(mockServer, new ValueMap(grains));

        mapper.mapX86CpuInfo(cpu, cpuInfo);

        assertEquals("Intel(R) Xeon(R) Gold 6226R CPU ", cpu.getModel());
        assertEquals("2000.000", cpu.getMHz());
        assertEquals("GenuineIntel", cpu.getVendor());
        assertEquals("10", cpu.getStepping());
        assertEquals("6", cpu.getFamily());
        assertEquals("8192 KB", cpu.getCache());
        assertEquals("3999.93", cpu.getBogomips());
        assertTrue(cpu.getFlags().contains("fpu"));
        assertEquals("142", cpu.getVersion());
    }

    @Test
    public void testMapX86CpuInfoWhenMissingGrains() {
        Map<String, Object> grains = new HashMap<>();
        grains.put("cpuarch", "x86_64");

        CPU cpu = new CPU();
        ValueMap cpuInfo = new ValueMap(new HashMap<>());
        CpuInfoMapper mapper = new CpuInfoMapper(mockServer, new ValueMap(grains));

        mapper.mapX86CpuInfo(cpu, cpuInfo);

        assertTrue(cpu.getModel().isEmpty());
        assertEquals("-1", cpu.getMHz());
        assertTrue(cpu.getVendor().isEmpty());
        assertTrue(cpu.getStepping().isEmpty());
        assertTrue(cpu.getFamily().isEmpty());
        assertTrue(cpu.getCache().isEmpty());
        assertTrue(cpu.getBogomips().isEmpty());
        assertNull(cpu.getFlags());
        assertTrue(cpu.getVersion().isEmpty());
    }

    @Test
    public void testMapPpc64CpuInfoSetsExpectedFields() {
        Map<String, Object> grains = new HashMap<>();
        grains.put("cpuarch", "ppc64le");

        Map<String, Object> cpuInfoData = new HashMap<>();
        cpuInfoData.put("cpu", "POWER9, altivec supported");
        cpuInfoData.put("revision", "2.2 (pvr 004e 1202)");
        cpuInfoData.put("bogomips", "4000.00");
        cpuInfoData.put("machine", "PowerNV 8335-GTH");
        cpuInfoData.put("clock", "3800.000000MHz");

        CPU cpu = new CPU();
        ValueMap cpuInfo = new ValueMap(cpuInfoData);
        CpuInfoMapper mapper = new CpuInfoMapper(mockServer, new ValueMap(grains));

        mapper.mapPpc64CpuInfo(cpu, cpuInfo);

        assertTrue(cpu.getModel().contains("POWER9"));
        assertTrue(cpu.getVersion().contains("2.2"));
        assertEquals("4000.00", cpu.getBogomips());
        assertTrue(cpu.getVendor().contains("PowerNV"));
        assertEquals("3800.000000", cpu.getMHz());
    }


    @Test
    public void testMapPpc64CpuInfoWhenMissingGrains() {
        Map<String, Object> grains = new HashMap<>();
        grains.put("cpuarch", "ppc64le");

        CPU cpu = new CPU();
        ValueMap cpuInfo = new ValueMap(new HashMap<>());
        CpuInfoMapper mapper = new CpuInfoMapper(mockServer, new ValueMap(grains));

        mapper.mapPpc64CpuInfo(cpu, cpuInfo);

        assertTrue(cpu.getModel().isEmpty());
        assertTrue(cpu.getVersion().isEmpty());
        assertTrue(cpu.getBogomips().isEmpty());
        assertTrue(cpu.getVendor().isEmpty());
        assertEquals("-1", cpu.getMHz());
    }

    @Test
    public void testMapS390CpuInfoSetsExpectedFields() {
        String expectedFlags = "esan3 zarch stfle msa ldisp eimm dfp etf3eh highgprs";

        Map<String, Object> grains = new HashMap<>();
        grains.put("cpuarch", "s390x");

        Map<String, Object> cpuInfoData = new HashMap<>();
        cpuInfoData.put("vendor_id", "IBM/S390");
        cpuInfoData.put("bogomips per cpu", "3241.00");
        cpuInfoData.put("features", expectedFlags);

        CPU cpu = new CPU();
        ValueMap cpuInfo = new ValueMap(cpuInfoData);
        CpuInfoMapper mapper = new CpuInfoMapper(mockServer, new ValueMap(grains));

        mapper.mapS390CpuInfo(cpu, cpuInfo, "s390x");

        assertEquals("IBM/S390", cpu.getVendor());
        assertEquals("s390x", cpu.getModel());
        assertEquals("3241.00", cpu.getBogomips());
        assertEquals(expectedFlags, cpu.getFlags());
        assertEquals("0", cpu.getMHz());
    }

    @Test
    public void testMapS390CpuInfoWhenMissingGrains() {
        Map<String, Object> grains = new HashMap<>();
        grains.put("cpuarch", "s390x");

        CPU cpu = new CPU();
        ValueMap cpuInfo = new ValueMap(new HashMap<>());
        CpuInfoMapper mapper = new CpuInfoMapper(mockServer, new ValueMap(grains));

        mapper.mapS390CpuInfo(cpu, cpuInfo, "s390x");

        assertTrue(cpu.getVendor().isEmpty());
        assertEquals("s390x", cpu.getModel());
        assertTrue(cpu.getBogomips().isEmpty());
        assertNull(cpu.getFlags());
        assertEquals("0", cpu.getMHz());
    }

    @Test
    public void testMapAarch64CpuInfoSetsExpectedFields() {
        Map<String, Object> grains = new HashMap<>();
        grains.put("cpuarch", "aarch64");
        grains.put("cpu_vendor", "ARM");
        grains.put("cpu_model", "Cortex-A72");
        grains.put("bogomips", "108.00");
        grains.put("cpu_stepping", "r0p2");

        CPU cpu = new CPU();
        CpuInfoMapper mapper = new CpuInfoMapper(mockServer, new ValueMap(grains));

        mapper.mapAarch64CpuInfo(cpu);

        assertEquals("108.00", cpu.getBogomips());
        assertEquals("ARM", cpu.getVendor());
        assertEquals("r0p2", cpu.getStepping());
        assertEquals("Cortex-A72", cpu.getModel());
    }

    @Test
    public void testMapAarch64CpuInfoWhenMissingGrains() {
        Map<String, Object> grains = new HashMap<>();
        grains.put("cpuarch", "aarch64");

        CPU cpu = new CPU();
        CpuInfoMapper mapper = new CpuInfoMapper(mockServer, new ValueMap(grains));

        mapper.mapAarch64CpuInfo(cpu);

        assertTrue(cpu.getBogomips().isEmpty());
        assertTrue(cpu.getVendor().isEmpty());
        assertTrue(cpu.getStepping().isEmpty());
        assertTrue(cpu.getModel().isEmpty());
    }

    /**
     * Tests {@link CpuInfoMapper#mapCpuInfo(ValueMap)} with empty grains
     */
    @Test
    public void testFailMapCpuInfoWhenEmptyCpuIfoGrains() {
        Map<String, Object> grains = new HashMap<>();
        ValueMap cpuInfo = new ValueMap(new HashMap<>());

        context().checking(new Expectations() {{
            allowing(mockServer).getMinionId();
            will(returnValue("test-minion"));
            allowing(mockServer).getCpu();
            will(returnValue(null));
        }});

        CpuInfoMapper mapper = new CpuInfoMapper(mockServer, new ValueMap(grains));
        Optional<String> error = mapper.mapCpuInfo(cpuInfo);

        assertTrue(error.isPresent());
        assertTrue(error.get().contains("cpuarch"));
    }

    /**
     * Tests {@link CpuInfoMapper#mapCpuInfo(ValueMap)} when cpu arch does not exist in db
     */
    @Test
    public void testFailMapCpuInfoWhenCpuArchDoesntExist() {
        Map<String, Object> grains = new HashMap<>();
        grains.put("cpuarch", "NON_EXISTING_ARCH");

        ValueMap cpuInfo = new ValueMap(new HashMap<>());

        context().checking(new Expectations() {{
            allowing(mockServer).getMinionId();
            will(returnValue("test-minion"));
            allowing(mockServer).getCpu();
            will(returnValue(null));
        }});

        CpuInfoMapper mapper = new CpuInfoMapper(mockServer, new ValueMap(grains));
        Optional<String> error = mapper.mapCpuInfo(cpuInfo);

        assertTrue(error.isPresent());
        assertTrue(error.get().contains("Could not find CPUArch in db for value"));
    }


    /**
     * Tests {@link CpuInfoMapper#mapCpuInfo(ValueMap)} when no common data is provided
     */
    @Test
    public void testSuccessMapCpuInfoWhenMinimalGrainData() {
        Map<String, Object> grains = new HashMap<>();
        grains.put("cpuarch", "x86_64");

        ValueMap cpuInfo = new ValueMap(new HashMap<>());

        context().checking(new Expectations() {{
            allowing(mockServer).getMinionId();
            will(returnValue("test-minion"));
            allowing(mockServer).getCpu();
            will(returnValue(null));
            allowing(mockServer).setCpu(with(any(CPU.class)));
        }});

        CpuInfoMapper mapper = new CpuInfoMapper(mockServer, new ValueMap(grains));
        Optional<String> error = mapper.mapCpuInfo(cpuInfo);

        assertTrue(error.isEmpty());
    }

    /**
     * Tests {@link CpuInfoMapper#mapCpuInfo(ValueMap)} processing when common data is provided
     */
    @Test
    public void testSuccessMapCpuInfoWithCommonGrainData() {
        Map<String, Object> grains = new HashMap<>();
        grains.put("cpuarch", "i586");

        grains.put("cpu_sockets", "14");
        grains.put("cpu_cores", "12");
        grains.put("cpu_threads", "10");
        grains.put("total_num_cpus", "8");
        Map<String, Object> cpuArchSpecs = new HashMap<>();
        cpuArchSpecs.put("a", "1");
        cpuArchSpecs.put("b", 2);
        grains.put("cpu_arch_specs", cpuArchSpecs);

        ValueMap cpuInfo = new ValueMap(grains);
        CPU cpu = new CPU();

        context().checking(new Expectations() {{
            allowing(mockServer).getMinionId();
            will(returnValue("test-minion"));
            allowing(mockServer).getCpu();
            will(returnValue(cpu));
            allowing(mockServer).setCpu(with(any(CPU.class)));
        }});

        CpuInfoMapper mapper = new CpuInfoMapper(mockServer, new ValueMap(grains));
        Optional<String> error = mapper.mapCpuInfo(cpuInfo);

        assertTrue(error.isEmpty());
        assertNotNull(cpu.getArch());
        assertEquals("i386", cpu.getArch().getLabel());
        assertEquals(14, cpu.getNrsocket());
        assertEquals(12, cpu.getNrCore());
        assertEquals(10, cpu.getNrThread());
        assertEquals(8, cpu.getNrCPU());
        assertEquals("1", cpu.getArchSpecs().get("a"));
        assertEquals(2, cpu.getArchSpecs().get("b"));
    }
}
