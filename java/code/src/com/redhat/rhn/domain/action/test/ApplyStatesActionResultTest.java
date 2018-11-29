/**
 * Copyright (c) 2018 SUSE LLC
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
package com.redhat.rhn.domain.action.test;

import com.redhat.rhn.domain.action.salt.ApplyStatesActionResult;
import com.redhat.rhn.domain.action.salt.StateResult;
import com.redhat.rhn.testing.RhnBaseTestCase;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;


/**
 * ApplyStatesActionResultTest
 */
public class ApplyStatesActionResultTest extends RhnBaseTestCase {

    /**
     * Tests getResult Method for invalid Output
     * @throws AssertionFailedError exception if test fails
     */
    public void testGetOptionalEmptyForMalformedOutput() throws AssertionFailedError {
        ApplyStatesActionResult stateResult = new ApplyStatesActionResult();
        stateResult.setOutput("Gibberish".getBytes(StandardCharsets.UTF_8));
        Optional<List<StateResult>> result = stateResult.getResult();

        Assert.assertFalse(result.isPresent());
    }

    /**
     * Tests getResult Method for valid Output
     * @throws AssertionFailedError exception if test fails
     */
    public void testResultIsPresentForValidStateRun() throws AssertionFailedError {
        ApplyStatesActionResult stateResult = new ApplyStatesActionResult();

        String stdout  =
                "cmd_|-date_|-date_|-run:\n" +
                "    comment: Command \"date\" run\n" +
                "    name: date\n" +
                "    start_time: '08:17:16.063154'\n" +
                "    result: true\n" +
                "    duration: 36.353\n" +
                "    __run_num__: 0.0\n" +
                "    __sls__: manager_org_1.testchannel\n" +
                "    changes:\n" +
                "        pid: 1346.0\n" +
                "        retcode: 0.0\n" +
                "        stderr: ''\n" +
                "        stdout: Wed Nov 28 08:17:16 CET 2018\n" +
                "    __id__: date\n";

        stateResult.setOutput(stdout.getBytes(StandardCharsets.UTF_8));

        Optional<List<StateResult>> result = stateResult.getResult();

        Assert.assertTrue(result.isPresent());
    }
}
