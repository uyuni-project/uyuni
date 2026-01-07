/*
 * Copyright (c) 2023 SUSE LLC
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

package com.suse.manager.ssl.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.suse.manager.ssl.SSLCertData;
import com.suse.manager.webui.utils.gson.ProxyContainerConfigJson;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class SSLCertDataTest {
    private SSLCertData sslCertData;

    @BeforeEach
    void setUp() {
        sslCertData = new SSLCertData(null, null, null, null, null, null, null, null);
    }



    @ParameterizedTest
    @MethodSource("machineNameTestData")
    void testGetMachineNameWithCustomObject(SSLCertDataTestData sslCertDataTestDataIn) {
        sslCertData = new SSLCertData(sslCertDataTestDataIn.getCn(), null, null, null, null, null, null, null);

        String result = sslCertData.getMachineName();
        assertEquals(sslCertDataTestDataIn.getExpectedMachineName(), result);
    }


    /**
     * Test the getMachineName method.
     * Note that cn pattern is matched at {@link ProxyContainerConfigJson}
     */
    private static Stream<SSLCertDataTestData> machineNameTestData() {
        return Stream.of(
                new SSLCertDataTestData("xxx.yyy.zzz.com", "xxx.yyy"),
                new SSLCertDataTestData("yyy.zzz.com", "yyy"),
                new SSLCertDataTestData("zzz.com", "zzz.com"),
                new SSLCertDataTestData("xxx", "xxx")
        );
    }

    static class SSLCertDataTestData {
        private final String cn;
        private final String expectedMachineName;

        SSLCertDataTestData(String cnIn, String expectedMachineNameIn) {
            this.cn = cnIn;
            this.expectedMachineName = expectedMachineNameIn;
        }

        String getCn() {
            return cn;
        }

        String getExpectedMachineName() {
            return expectedMachineName;
        }
    }
}
