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

package com.suse.cloud.domain.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.testing.BaseTestCaseWithUser;

import com.suse.cloud.domain.BillingDimension;
import com.suse.cloud.domain.PaygDimensionComputation;
import com.suse.cloud.domain.PaygDimensionFactory;
import com.suse.cloud.domain.PaygDimensionResult;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

public class PaygDimensionFactoryTest extends BaseTestCaseWithUser {

    private PaygDimensionFactory factory;

    @Test
    public void canSaveAndRetrieveResult() {
        PaygDimensionComputation result = new PaygDimensionComputation();

        result.setSuccess(true);
        result.setTimestamp(new Date());
        result.addDimensionResult(BillingDimension.MANAGED_SYSTEMS, 4L);

        factory = new PaygDimensionFactory();
        factory.save(result);

        assertNotNull(result.getId());

        PaygDimensionComputation reloaded = factory.lookupById(result.getId());
        assertTrue(reloaded.isSuccess());
        assertEquals(1, reloaded.getDimensionResults().size());

        Optional<PaygDimensionResult> managedSystems = reloaded.getResultForDimension(BillingDimension.MANAGED_SYSTEMS);
        assertTrue(managedSystems.isPresent());
        managedSystems.ifPresent(dimensionResult -> {
            assertEquals(reloaded.getId(), dimensionResult.getComputationId());
            assertEquals(4L, dimensionResult.getCount());
        });
    }

    @Test
    public void canRetrieveLatestSuccessfulResult() {
        factory = new PaygDimensionFactory();

        PaygDimensionComputation result1 = new PaygDimensionComputation();
        result1.setSuccess(true);
        result1.setTimestamp(Date.from(Instant.now().minus(5, ChronoUnit.MINUTES)));
        result1.addDimensionResult(BillingDimension.MANAGED_SYSTEMS, 10);
        factory.save(result1);

        PaygDimensionComputation result2 = new PaygDimensionComputation();
        result2.setSuccess(true);
        result2.setTimestamp(Date.from(Instant.now().minus(10, ChronoUnit.MINUTES)));
        result2.addDimensionResult(BillingDimension.MANAGED_SYSTEMS, 4);
        factory.save(result2);

        PaygDimensionComputation result3 = new PaygDimensionComputation();
        result3.setSuccess(false);
        result3.setTimestamp(Date.from(Instant.now()));
        factory.save(result3);

        PaygDimensionComputation latest = factory.getLatestSuccessfulComputation();
        assertNotNull(latest);
        assertTrue(latest.isSuccess());
        assertEquals(1, latest.getDimensionResults().size());

        Optional<PaygDimensionResult> managedSystems = latest.getResultForDimension(BillingDimension.MANAGED_SYSTEMS);
        assertTrue(managedSystems.isPresent());
        managedSystems.ifPresent(dimensionResult -> {
            assertEquals(latest.getId(), dimensionResult.getComputationId());
            assertEquals(10L, dimensionResult.getCount());
        });
    }
}
