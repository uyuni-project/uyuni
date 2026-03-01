/*
 * Copyright (c) 2026 SUSE LCC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.webui.utils.salt.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.testing.MockObjectTestCase;

import com.suse.manager.webui.utils.salt.custom.VmInfoSlsResult;
import com.suse.salt.netapi.results.Ret;
import com.suse.salt.netapi.results.StateApplyResult;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

class VmInfoSlsResultTest extends MockObjectTestCase {

    @BeforeEach
    void setUp() {
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }

    /**
     * Test the scenario where state result is false (virt.vm_info module not available, e.g., libvirtd not installed)
     * This reflects as the following example:
     * mgrcompat_|-mgr_virt_profile_|-virt.vm_info_|-module_run": {
     * "id": "mgr_virt_profile",
     * "run_num": ...,
     * "sls": "hardware.virtprofile",
     * "changes": {},
     * "comment": "Module function virt.vm_info is not available",
     * "duration": ...,
     * "name": "virt.vm_info",
     * "result": false,
     * "start_time": ...
     * }
     *
     * @throws Exception if an error occurs
     */
    @Test
    void testGetVmInfosWhenStateResultIsFalse() throws Exception {
        StateApplyResult<Ret<Map<String, Map<String, Object>>>> failedState = context.mock(StateApplyResult.class);

        context.checking(new Expectations() {{
            oneOf(failedState).isResult();
            will(returnValue(false));
        }});

        VmInfoSlsResult result = new VmInfoSlsResult();
        setVmInfoByReflection(result, failedState);

        assertTrue(result.getVmInfos().isEmpty());
    }

    /**
     * Test the scenario where vminfo is null
     * @throws Exception if an error occurs
     */
    @Test
    void testGetVmInfosWhenVmInfoIsNull() throws Exception {
        VmInfoSlsResult result = new VmInfoSlsResult();
        setVmInfoByReflection(result, null);

        assertTrue(result.getVmInfos().isEmpty());
    }

    /**
     * Test the scenario where changes is null
     * @throws Exception if an error occurs
     */
    @Test
    void testGetVmInfosWhenChangesIsNull() throws Exception {
        StateApplyResult<Ret<Map<String, Map<String, Object>>>> successfulState = context.mock(StateApplyResult.class);

        context.checking(new Expectations() {{
            oneOf(successfulState).isResult();
            will(returnValue(true));
            oneOf(successfulState).getChanges();
            will(returnValue(null));
        }});

        VmInfoSlsResult result = new VmInfoSlsResult();
        setVmInfoByReflection(result, successfulState);

        assertTrue(result.getVmInfos().isEmpty());
    }

    /**
     * Test the scenario where ret is null
     * @throws Exception if an error occurs
     */
    @Test
    void testGetVmInfosWhenRetIsNull() throws Exception {
        StateApplyResult<Ret<Map<String, Map<String, Object>>>> failedStateRet = context.mock(StateApplyResult.class);
        Ret<Map<String, Map<String, Object>>> mockRet = context.mock(Ret.class);

        context.checking(new Expectations() {{
            oneOf(failedStateRet).isResult();
            will(returnValue(true));
            oneOf(failedStateRet).getChanges();
            will(returnValue(mockRet));
            oneOf(mockRet).getRet();
            will(returnValue(null));
        }});

        VmInfoSlsResult result = new VmInfoSlsResult();
        setVmInfoByReflection(result, failedStateRet);

        assertTrue(result.getVmInfos().isEmpty());
    }


    /**
     * Test the scenario where ret is an empty map
     * @throws Exception if an error occurs
     */
    @Test
    void testGetVmInfosWithSuccessfulWhenEmptyVmData() throws Exception {
        StateApplyResult<Ret<Map<String, Map<String, Object>>>> emptyState = context.mock(StateApplyResult.class);
        Ret<Map<String, Map<String, Object>>> mockRet = context.mock(Ret.class);

        context.checking(new Expectations() {{
            oneOf(emptyState).isResult();
            will(returnValue(true));
            oneOf(emptyState).getChanges();
            will(returnValue(mockRet));
            oneOf(mockRet).getRet();
            will(returnValue(new HashMap<>()));
        }});

        VmInfoSlsResult result = new VmInfoSlsResult();
        setVmInfoByReflection(result, emptyState);

        assertTrue(result.getVmInfos().isEmpty());
    }

    /**
     * Test the scenario where ret contains multiple VMs
     * @throws Exception if an error occurs
     */
    @Test
    void testGetVmInfosWithSuccessfulWhenMultipleVms() throws Exception {
        StateApplyResult<Ret<Map<String, Map<String, Object>>>> multiVmState = context.mock(StateApplyResult.class);
        Ret<Map<String, Map<String, Object>>> mockRet = context.mock(Ret.class);
        Map<String, Map<String, Object>> vmData =
                Map.of(
                        "vm1", Map.of(
                                "state", "running",
                                "vcpus", 2
                        ),
                        "vm2", Map.of(
                                "state", "paused",
                                "vcpus", 4
                        )
                );

        context.checking(new Expectations() {{
            oneOf(multiVmState).isResult();
            will(returnValue(true));
            oneOf(multiVmState).getChanges();
            will(returnValue(mockRet));
            oneOf(mockRet).getRet();
            will(returnValue(vmData));
        }});

        VmInfoSlsResult result = new VmInfoSlsResult();
        setVmInfoByReflection(result, multiVmState);

        //
        Map<String, Map<String, Object>> vmInfos = result.getVmInfos();

        assertEquals(2, vmInfos.size());
        Map<String, Object> vm1 = vmInfos.get("vm1");
        Map<String, Object> vm2 = vmInfos.get("vm2");

        assertNotNull(vm1);
        assertEquals("running", vm1.get("state"));
        assertEquals(2, vm1.get("vcpus"));

        assertNotNull(vm2);
        assertEquals("paused", vm2.get("state"));
        assertEquals(4, vm2.get("vcpus"));
    }

    /**
     * Set the vminfo field of VmInfoSlsResult using reflection
     * @param result the VmInfoSlsResult instance
     * @param vminfo the StateApplyResult to set
     * @throws Exception if an error occurs
     */
    private void setVmInfoByReflection(
            VmInfoSlsResult result,
            StateApplyResult<Ret<Map<String, Map<String, Object>>>> vminfo
    ) throws Exception {
        Field field = VmInfoSlsResult.class.getDeclaredField("vminfo");
        field.setAccessible(true);
        field.set(result, vminfo);
    }
}
