/*
 * Copyright (c) 2016 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.systems.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.frontend.action.systems.SystemSearchHelper;
import com.redhat.rhn.frontend.action.systems.SystemSearchHelperType;
import com.redhat.rhn.frontend.dto.SystemOverview;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JUnit primarily for SystemSearchHelper.SearchResultScoreComparator
 * Could/should be expanded to cover more of SystemSearchHelper (which
 * has zero Junit coverage currently...)
 *
 * @author ggainey
 *
 */
public class SystemSearchHelperTest  {

    // LABEL;SCORE;PROFILE-NAME;SID
    // SCORE==-1 => null-score
    static final int LABEL = 0;
    static final int SCORE = 1;
    static final int PROFILE_NAME = 2;
    static final int SID = 3;

    static final int A_EQUAL_B = 0;
    static final int A_FIRST = -1;
    static final int B_FIRST = 1;

    static final String[] TEST_DATA = {
                    "null-1-1101;-1.0d;profile1;1101",
                    "null-1-1102;-1.0d;profile1;1102",
                    "10-1-1100;10.0d;profile1;1100",
                    "20-1-1110;20.0d;profile1;1110",
                    "30-1-1115;30.0d;profile1;1115",
                    "null-2-1201;-1.0d;profile2;1201",
                    "10-2-1200;10.0d;profile2;1200",
                    "20-2-1210;20.0d;profile2;1210",
                    "30-2-1215;30.0d;profile2;1215",
                    "10-3-1300;10.0d;profile3;1300",
                    "10-3-1310;10.0d;profile3;1310",
                    "10-3-1315;10.0d;profile3;1315"
                    };

    static final long EMPTY_SID = 666L;

    protected Map<String, SystemOverview> dtos;
    protected Map<Long, Map<String, Object>> scores;
    protected SystemSearchHelper.SearchResultScoreComparator cmp;
    protected SystemSearchHelper.SearchResultScoreComparator nullCmp;

    @BeforeEach
    public void setUp() {
        dtos = new HashMap<>();
        scores = new HashMap<>();

        for (String vals : TEST_DATA) {
            String[] entries = vals.split(";");
            Map<String, Object> serverScore = new HashMap<>();
            SystemOverview aDto = new SystemOverview();
            aDto.setId(Long.parseLong(entries[SID]));
            aDto.setName(entries[PROFILE_NAME]);
            double aScore = Double.parseDouble(entries[SCORE]);
            if (aScore > 0.0d) {
                serverScore.put("score", aScore);
            }
            scores.put(aDto.getId(), serverScore);
            dtos.put(entries[LABEL], aDto);
        }

        SystemOverview aDto = new SystemOverview();
        aDto.setId(EMPTY_SID);
        aDto.setName("NO-RESULTS");
        scores.put(EMPTY_SID, null);
        dtos.put("NO_RESULTS", aDto);

        cmp = new SystemSearchHelper.SearchResultScoreComparator(scores);

        nullCmp = new SystemSearchHelper.SearchResultScoreComparator(null);
    }

    // No results?
    // Sort by profile then reverse-SID
    @Test
    public void testNullResultsSameDto() {
        assertEquals(A_EQUAL_B,
                        nullCmp.compare(dtos.get("10-1-1100"), dtos.get("10-1-1100")));
    }
    @Test
    public void testNullResultsSameScoreDiffProfAFirst() {
        assertEquals(A_FIRST,
                        nullCmp.compare(dtos.get("10-1-1100"), dtos.get("10-2-1200")));
    }
    @Test
    public void testNullResultsSameScoreDiffProfBFirst() {
        assertEquals(B_FIRST,
                        nullCmp.compare(dtos.get("10-2-1200"), dtos.get("10-1-1100")));
    }
    @Test
    public void testNullResultsSameScoreSameProfDiffSidAFirst() {
        assertEquals(A_FIRST,
                        nullCmp.compare(dtos.get("10-3-1310"), dtos.get("10-3-1300")));
    }
    @Test
    public void testNullResultsSameScoreSameProfDiffSidBFirst() {
        assertEquals(B_FIRST,
                        nullCmp.compare(dtos.get("10-3-1300"), dtos.get("10-3-1310")));
    }

    @Test
    public void testEqual() {
        assertEquals(A_EQUAL_B, cmp.compare(dtos.get("10-1-1100"), dtos.get("10-1-1100")));
    }

    @Test
    public void testEqualScoreDiffProfile() {
        assertEquals(A_FIRST, cmp.compare(dtos.get("10-1-1100"), dtos.get("10-2-1200")));
        assertEquals(B_FIRST, cmp.compare(dtos.get("10-2-1200"), dtos.get("10-1-1100")));
    }

    @Test
    public void testEqualScoreSameProfile() {
        assertEquals(B_FIRST, cmp.compare(dtos.get("10-3-1310"), dtos.get("10-3-1315")));
        assertEquals(A_FIRST, cmp.compare(dtos.get("10-3-1315"), dtos.get("10-3-1310")));
    }

    @Test
    public void testNullScoreBothSame() {
        assertEquals(A_EQUAL_B,
                        cmp.compare(dtos.get("null-1-1101"), dtos.get("null-1-1101")));
    }
    @Test
    public void testNullScoreBothDiffProfile() {
        assertEquals(A_FIRST,
                        cmp.compare(dtos.get("null-1-1101"), dtos.get("null-2-1201")));
        assertEquals(B_FIRST,
                        cmp.compare(dtos.get("null-2-1201"), dtos.get("null-1-1101")));
    }
    @Test
    public void testNullScoreBothDiffSID() {
        assertEquals(A_FIRST,
                        cmp.compare(dtos.get("null-1-1102"), dtos.get("null-1-1101")));
        assertEquals(B_FIRST,
                        cmp.compare(dtos.get("null-1-1101"), dtos.get("null-1-1102")));
    }
    @Test
    public void testNullScoreSecond() {
        assertEquals(A_FIRST, cmp.compare(dtos.get("10-1-1100"), dtos.get("null-1-1101")));
    }
    @Test
    public void testNullScoreFirst() {
        assertEquals(B_FIRST, cmp.compare(dtos.get("null-1-1101"), dtos.get("10-1-1100")));
    }

/*
    "null-1-1101;-1.0d;profile1;1101",
    "null-1-1102;-1.0d;profile1;1102",
    "10-1-1100;10.0d;profile1;1100",
    "20-1-1110;20.0d;profile1;1110",
    "30-1-1115;30.0d;profile1;1115",
    "null-2-1201;-1.0d;profile2;1201",
    "10-2-1200;10.0d;profile2;1200",
    "20-2-1210;20.0d;profile2;1210",
    "30-2-1215;30.0d;profile2;1215",
    "10-3-1300;10.0d;profile3;1300",
    "10-3-1310;10.0d;profile3;1310",
    "10-3-1315;10.0d;profile3;1315"
should sort to
    "30-1-1115;30.0d;profile1;1115",
    "30-2-1215;30.0d;profile2;1215",
    "20-1-1110;20.0d;profile1;1110",
    "20-2-1210;20.0d;profile2;1210",
    "10-1-1100;10.0d;profile1;1100",
    "10-2-1200;10.0d;profile2;1200",
    "10-3-1315;10.0d;profile3;1315"
    "10-3-1310;10.0d;profile3;1310",
    "10-3-1300;10.0d;profile3;1300",
    "null-1-1102;-1.0d;profile1;1102",
    "null-1-1101;-1.0d;profile1;1101",
    "null-2-1201;-1.0d;profile2;1201",
*/
    @Test
    public void testListSort() {
        List<SystemOverview> systems = new ArrayList<>(dtos.values());
        assertEquals(dtos.size(), systems.size());
        systems.sort(cmp);
        assertEquals(1115L, (long) systems.get(0).getId());
        assertEquals(1215L, (long) systems.get(1).getId());
        assertEquals(1110L, (long) systems.get(2).getId());
        assertEquals(1210L, (long) systems.get(3).getId());
        assertEquals(1100L, (long) systems.get(4).getId());
        assertEquals(1200L, (long) systems.get(5).getId());
        assertEquals(1315L, (long) systems.get(6).getId());
        assertEquals(1310L, (long) systems.get(7).getId());
        assertEquals(1300L, (long) systems.get(8).getId());
        assertEquals(1102L, (long) systems.get(9).getId());
        assertEquals(1101L, (long) systems.get(10).getId());
        assertEquals(1201L, (long) systems.get(11).getId());
    }

    @Test
    public void testSystemSearchHelperTypePrefix() {
        final String termsString = "TEST";
        assertEquals("name:(TEST) description:(TEST)",
                SystemSearchHelperType.NAME_AND_DESCRIPTION.getQuery(termsString));
        assertEquals("server", SystemSearchHelperType.NAME_AND_DESCRIPTION.getIndexLabel());

        assertEquals("system_id:(TEST)", SystemSearchHelperType.ID.getQuery(termsString));
        assertEquals("server", SystemSearchHelperType.ID.getIndexLabel());

        assertEquals("value:(TEST)", SystemSearchHelperType.CUSTOM_INFO.getQuery(termsString));
        assertEquals("serverCustomInfo", SystemSearchHelperType.CUSTOM_INFO.getIndexLabel());

        assertEquals("name:(TEST)", SystemSearchHelperType.SNAPSHOT_TAG.getQuery(termsString));
        assertEquals("snapshotTag", SystemSearchHelperType.SNAPSHOT_TAG.getIndexLabel());

        assertEquals("cpuModel:(TEST)", SystemSearchHelperType.CPU_MODEL.getQuery(termsString));
        assertEquals("server", SystemSearchHelperType.CPU_MODEL.getIndexLabel());

        assertEquals("description:(TEST)", SystemSearchHelperType.HW_DESCRIPTION.getQuery(termsString));
        assertEquals("hwdevice", SystemSearchHelperType.HW_DESCRIPTION.getIndexLabel());

        assertEquals("driver:(TEST)", SystemSearchHelperType.HW_DRIVER.getQuery(termsString));
        assertEquals("hwdevice", SystemSearchHelperType.HW_DRIVER.getIndexLabel());

        assertEquals("deviceId:(TEST)", SystemSearchHelperType.HW_DEVICE_ID.getQuery(termsString));
        assertEquals("hwdevice", SystemSearchHelperType.HW_DEVICE_ID.getIndexLabel());

        assertEquals("vendorId:(TEST)", SystemSearchHelperType.HW_VENDOR_ID.getQuery(termsString));
        assertEquals("hwdevice", SystemSearchHelperType.HW_VENDOR_ID.getIndexLabel());

        assertEquals("dmiSystem:(TEST)", SystemSearchHelperType.DMI_SYSTEM.getQuery(termsString));
        assertEquals("server", SystemSearchHelperType.DMI_SYSTEM.getIndexLabel());

        assertEquals("dmiBiosVendor:(TEST) dmiBiosVersion:(TEST) dmiBiosRelease:(TEST)",
                SystemSearchHelperType.DMI_BIOS.getQuery(termsString));
        assertEquals("server", SystemSearchHelperType.DMI_BIOS.getIndexLabel());

        assertEquals("dmiAsset:(TEST)", SystemSearchHelperType.DMI_ASSET.getQuery(termsString));
        assertEquals("server", SystemSearchHelperType.DMI_ASSET.getIndexLabel());

        assertEquals("hostname:(TEST)", SystemSearchHelperType.HOSTNAME.getQuery(termsString));
        assertEquals("server", SystemSearchHelperType.HOSTNAME.getIndexLabel());

        assertEquals("ipaddr:(TEST)", SystemSearchHelperType.IP.getQuery(termsString));
        assertEquals("server", SystemSearchHelperType.IP.getIndexLabel());

        assertEquals("ip6addr:(TEST)", SystemSearchHelperType.IP6.getQuery(termsString));
        assertEquals("server", SystemSearchHelperType.IP6.getIndexLabel());

        assertEquals("runningKernel:(TEST)", SystemSearchHelperType.RUNNING_KERNEL.getQuery(termsString));
        assertEquals("server", SystemSearchHelperType.RUNNING_KERNEL.getIndexLabel());

        assertEquals("country:(TEST)", SystemSearchHelperType.LOC_COUNTRY_CODE.getQuery(termsString));
        assertEquals("server", SystemSearchHelperType.LOC_COUNTRY_CODE.getIndexLabel());

        assertEquals("state:(TEST)", SystemSearchHelperType.LOC_STATE.getQuery(termsString));
        assertEquals("server", SystemSearchHelperType.LOC_STATE.getIndexLabel());

        assertEquals("city:(TEST)", SystemSearchHelperType.LOC_CITY.getQuery(termsString));
        assertEquals("server", SystemSearchHelperType.LOC_CITY.getIndexLabel());

        assertEquals("address1:(TEST) address2:(TEST)",
                SystemSearchHelperType.LOC_ADDRESS.getQuery(termsString));
        assertEquals("server", SystemSearchHelperType.LOC_ADDRESS.getIndexLabel());

        assertEquals("building:(TEST)", SystemSearchHelperType.LOC_BUILDING.getQuery(termsString));
        assertEquals("server", SystemSearchHelperType.LOC_BUILDING.getIndexLabel());

        assertEquals("room:(TEST)", SystemSearchHelperType.LOC_ROOM.getQuery(termsString));
        assertEquals("server", SystemSearchHelperType.LOC_ROOM.getIndexLabel());

        assertEquals("rack:(TEST)", SystemSearchHelperType.LOC_RACK.getQuery(termsString));
        assertEquals("server", SystemSearchHelperType.LOC_RACK.getIndexLabel());

        assertEquals("uuid:(TEST)", SystemSearchHelperType.UUID.getQuery(termsString));
        assertEquals("server", SystemSearchHelperType.UUID.getIndexLabel());
    }

    @Test
    public void testSystemSearchHelperTypePackage() {
        final String termsString = "TEST";

        assertEquals("filename:(TEST*)", SystemSearchHelperType.INSTALLED_PACKAGES.getQuery(termsString));
        assertEquals("package", SystemSearchHelperType.INSTALLED_PACKAGES.getIndexLabel());

        assertEquals("name:(TEST) filename:(TEST)", SystemSearchHelperType.NEEDED_PACKAGES.getQuery(termsString));
        assertEquals("package", SystemSearchHelperType.NEEDED_PACKAGES.getIndexLabel());

    }

    @Test
    public void testSystemSearchHelperTypeDates() {
        assertTrue(SystemSearchHelperType.CHECKIN.getQuery("10").startsWith("checkin:[\"197001010000\" TO \""));
        assertTrue(SystemSearchHelperType.CHECKIN.getQuery("10").endsWith("\"]"));
        assertEquals("server", SystemSearchHelperType.CHECKIN.getIndexLabel());

        assertTrue(SystemSearchHelperType.REGISTERED.getQuery("10").startsWith("registered:{\""));
        assertTrue(SystemSearchHelperType.REGISTERED.getQuery("10").contains("\" TO \""));
        assertTrue(SystemSearchHelperType.REGISTERED.getQuery("10").endsWith("\"}"));
        assertEquals("server", SystemSearchHelperType.REGISTERED.getIndexLabel());
    }

    @Test
    public void testSystemSearchHelperTypeRanges() {
        final String termsString = "TEST";
        assertEquals("cpuMHz:{0 TO TEST}", SystemSearchHelperType.CPU_MHZ_LT.getQuery(termsString));
        assertEquals("server", SystemSearchHelperType.CPU_MHZ_LT.getIndexLabel());

        assertEquals("cpuMHz:{TEST TO 9223372036854775807}",
                SystemSearchHelperType.CPU_MHZ_GT.getQuery(termsString));
        assertEquals("server", SystemSearchHelperType.CPU_MHZ_GT.getIndexLabel());

        assertEquals("cpuNumberOfCpus:{0 TO TEST}", SystemSearchHelperType.NUM_CPUS_LT.getQuery(termsString));
        assertEquals("server", SystemSearchHelperType.NUM_CPUS_LT.getIndexLabel());

        assertEquals("cpuNumberOfCpus:{TEST TO 9223372036854775807}",
                SystemSearchHelperType.NUM_CPUS_GT.getQuery(termsString));
        assertEquals("server", SystemSearchHelperType.NUM_CPUS_GT.getIndexLabel());

        assertEquals("ram:{0 TO TEST}", SystemSearchHelperType.RAM_LT.getQuery(termsString));
        assertEquals("server", SystemSearchHelperType.RAM_LT.getIndexLabel());

        assertEquals("ram:{TEST TO 9223372036854775807}", SystemSearchHelperType.RAM_GT.getQuery(termsString));
        assertEquals("server", SystemSearchHelperType.RAM_GT.getIndexLabel());

    }
}
