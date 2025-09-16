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

package com.redhat.rhn.taskomatic.task.payg.dimensions;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;

import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Class that handles the computation logic for a dimension and a rule list
 */
public class DimensionCalculator {

    private static final Logger LOGGER = LogManager.getLogger(DimensionCalculator.class);

    private final int batchSize;

    /**
     * Default constructor
     * @param batchSizeIn the number of server to process at a time
     */
    public DimensionCalculator(int batchSizeIn) {
        this.batchSize = Math.min(batchSizeIn, HibernateFactory.LIST_BATCH_MAX_SIZE);
    }

    /**
     * Compute a dimension with the specified list of rules. The rules will be applied in the given order.
     *
     * @param serverIds the list of ids of the servers to process
     * @param rulesList the list of rules used to compute this dimension
     *
     * @return the number of server that satisfy all the given rules.
     */
    public long computeDimension(List<Long> serverIds, List<DimensionRule> rulesList) {
        if (CollectionUtils.isEmpty(serverIds)) {
            return 0L;
        }

        return IntStream.iterate(0, i -> i < serverIds.size(), i -> i + batchSize)
                        // Split the ids in batches
                        .mapToObj(i -> serverIds.subList(i, Math.min(i + batchSize, serverIds.size())))
                        // Read all the servers in the batch from the database
                        .map(ids -> ServerFactory.lookupByIds(ids))
                        // Count the servers in this batch
                        .map(servers -> servers.stream()
                                               // Take only the server that are included by all the rules
                                               .filter(server -> applyRules(server, rulesList))
                                               // Count how many servers we still have
                                               .count())
                        // Aggregate the results for all the batches
                        .reduce(0L, Long::sum);
    }

    /**
     * Applies all the rules sequentially
     * @param server the server to check
     * @param rulesList the list of rules
     * @return true if all the rules includes the server, false otherwise.
     */
    private static boolean applyRules(Server server, List<DimensionRule> rulesList) {
        for (int i = 0; i < rulesList.size(); i++) {
            if (!rulesList.get(i).includes(server)) {
                LOGGER.info("Server {} is excluded by rule #{}", server.getId(), i);
                return false;
            }
        }

        return true;
    }
}
