/*
 * Copyright (c) 2009--2013 Red Hat, Inc.
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
package com.redhat.rhn.domain.server.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.redhat.rhn.domain.server.CPU;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.Test;

/**
 * CPUTest
 */
public class CPUTest extends BaseTestCaseWithUser {

    public static final String ARCH_NAME = "x86_64";
    public static final String FAMILY = "6";
    public static final String MODEL = "Intel(R) Xeon(R) Gold 5115 CPU @ 2.40GHz";
    public static final String MHZ = "2400";
    public static final long MHZ_NUMERIC = 2400;
    public static final long NR_SOCKET = 2;
    public static final long NR_CORES = 10;
    public static final long NR_THREADS = 2;

    @Test
    public void testCreateLookup() throws Exception {
        Server server = ServerFactoryTest.createTestServer(user);
        CPU unit = createTestCpu(server);

        unit.setServer(server);
        server.setCpu(unit);

        ServerFactory.save(server);
        TestUtils.flushAndEvict(server);

        assertNotNull(unit.getId());
        Server server2 = ServerFactory.lookupByIdAndOrg(server.getId(),
                user.getOrg());
        assertNotNull(server2.getCpu());
        assertEquals(unit.getFamily(), server2.getCpu().getFamily());
        assertEquals(unit.getArch(), server2.getCpu().getArch());
    }

    /**
     * Helper method to create a test CPU object
     * @param s server to attach the CPU to
     * @return Returns test CPU object
     * @throws Exception something bad happened
     */
    public static CPU createTestCpu(Server s) throws Exception {
        CPU cpu = new CPU();

        cpu.setArch(ServerFactory.lookupCPUArchByName(ARCH_NAME));
        cpu.setServer(s);
        cpu.setFamily(FAMILY);
        cpu.setMHz(MHZ);
        cpu.setModel(MODEL);
        cpu.setNrsocket(NR_SOCKET);
        cpu.setNrCore(NR_CORES);
        cpu.setNrThread(NR_THREADS);
        cpu.setNrCPU(NR_SOCKET * NR_CORES * NR_THREADS);

        assertNull(cpu.getId());
        TestUtils.saveAndFlush(cpu);
        assertNotNull(cpu.getId());

        return cpu;
    }

}
