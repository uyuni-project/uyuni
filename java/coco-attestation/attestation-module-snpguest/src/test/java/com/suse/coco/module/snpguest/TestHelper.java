/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.coco.module.snpguest;

import com.suse.coco.module.snpguest.model.EpycGeneration;

import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

public class TestHelper {

    public static final String CPU_USING_VLEK_NAME = "Cpu: {0} using VLEK: {1}";

    public static Stream<Arguments> listCpuAndUsingVlek() {
        return Stream.of(
                Arguments.of(EpycGeneration.MILAN, false),
                Arguments.of(EpycGeneration.MILAN, true),
                Arguments.of(EpycGeneration.GENOA, false),
                Arguments.of(EpycGeneration.GENOA, true),
                Arguments.of(EpycGeneration.BERGAMO, false),
                Arguments.of(EpycGeneration.BERGAMO, true),
                Arguments.of(EpycGeneration.SIENA, false),
                Arguments.of(EpycGeneration.SIENA, true),
                Arguments.of(EpycGeneration.TURIN, false),
                Arguments.of(EpycGeneration.TURIN, true)
        );
    }

    private TestHelper() {
        // utility classes should not have a public or default constructor
    }
}
