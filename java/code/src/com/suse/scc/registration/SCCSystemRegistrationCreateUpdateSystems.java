/*
 * Copyright (c) 2023--2024 SUSE LLC
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

package com.suse.scc.registration;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;

import com.suse.scc.client.SCCClientException;
import com.suse.scc.model.SCCOrganizationSystemsUpdateResponse;
import com.suse.scc.model.SCCRegisterSystemJson;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class is responsible for the registration process of the systems in SCC.
 * It splits the systems into batches. Each batch is included in the body of a rest call is executed to the SCC API.
 * A successful call returns a set of @{link SCCSystemCredentialsJson} objects.
 */
public class SCCSystemRegistrationCreateUpdateSystems implements SCCSystemRegistrationContextHandler {
    private static final Logger LOG = LogManager.getLogger(SCCSystemRegistrationCreateUpdateSystems.class);

    @Override
    public void handle(SCCSystemRegistrationContext context) {
        final int batchSize = Config.get().getInt(ConfigDefaults.REG_BATCH_SIZE, 50);
        final List<SCCRegisterSystemJson> pendingRegistrationSystems =
                new ArrayList<>(context.getPendingRegistrationSystemsByLogin().values());

        // split items into batches
        List<List<SCCRegisterSystemJson>> systemsBatches = splitListIntoBatches(pendingRegistrationSystems, batchSize);

        for (List<SCCRegisterSystemJson> batch : systemsBatches) {
            try {
                SCCOrganizationSystemsUpdateResponse response = context.getSccClient().createUpdateSystems(
                        batch,
                        context.getPrimaryCredential().getUsername(),
                        context.getPrimaryCredential().getPassword()
                );
                context.getRegisteredSystems().addAll(response.getSystems());
            }
            catch (SCCClientException e) {
                LOG.error("SCC error while registering systems", e);
            }
            catch (Exception e) {
                LOG.error("Error registering systems", e);
            }
        }
    }

    private List<List<SCCRegisterSystemJson>> splitListIntoBatches(List<SCCRegisterSystemJson> list, int batchSize) {
        return IntStream.range(0, (list.size() + batchSize - 1) / batchSize)
                .mapToObj(i -> list.subList(i * batchSize, Math.min((i + 1) * batchSize, list.size())))
                .collect(Collectors.toList());
    }
}
