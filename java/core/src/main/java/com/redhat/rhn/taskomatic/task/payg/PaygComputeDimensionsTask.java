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

package com.redhat.rhn.taskomatic.task.payg;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.util.TimeUtils;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.taskomatic.task.RhnJavaJob;
import com.redhat.rhn.taskomatic.task.payg.dimensions.DimensionCalculator;
import com.redhat.rhn.taskomatic.task.payg.dimensions.DimensionRule;
import com.redhat.rhn.taskomatic.task.payg.dimensions.DimensionsConfiguration;

import com.suse.cloud.CloudPaygManager;
import com.suse.cloud.domain.PaygDimensionComputation;
import com.suse.cloud.domain.PaygDimensionFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PaygComputeDimensionsTask extends RhnJavaJob {

    private static final Logger LOGGER = LogManager.getLogger(PaygComputeDimensionsTask.class);
    public static final int DEFAULT_BATCH_SIZE = 500;

    private final DimensionsConfiguration configuration;

    private final CloudPaygManager cloudManager;

    private final PaygDimensionFactory dimensionFactory;

    /**
     * Default constructor.
     */
    public PaygComputeDimensionsTask() {
        this(DimensionsConfiguration.DEFAULT_CONFIGURATION, new PaygDimensionFactory(),
                GlobalInstanceHolder.PAYG_MANAGER);
    }

    /**
     * Builds an instance with the given configuration
     * @param configurationIn the dimensions configuration
     * @param dimensionFactoryIn the dimension factory
     * @param cloudManagerIn the cloud manager
     */
    public PaygComputeDimensionsTask(DimensionsConfiguration configurationIn, PaygDimensionFactory dimensionFactoryIn,
                                     CloudPaygManager cloudManagerIn) {
        this.configuration = configurationIn;
        this.dimensionFactory = dimensionFactoryIn;
        this.cloudManager = cloudManagerIn;
    }

    @Override
    public String getConfigNamespace() {
        return "payg.dimensions";
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        if (!cloudManager.isPaygInstance()) {
            LOGGER.debug("Not a PAYG instance. Exit");
            return;
        }

        Instant startTime = Instant.now();
        int batchSize = Config.get().getInt(getConfigurationKey("batch_size"), DEFAULT_BATCH_SIZE);

        LOGGER.info("Starting PAYG Dimension Computation");
        LOGGER.info("Using batchSize of {}", batchSize);

        DimensionCalculator calculator = new DimensionCalculator(batchSize);

        List<Long> allServerIds = ServerFactory.listAllServerIds();

        PaygDimensionComputation result = new PaygDimensionComputation();

        TimeUtils.logTime(LOGGER, "Computing all dimensions", () -> {
            try {
                configuration.forEachDimension((dimension, rules) -> {
                    LOGGER.info("Computing dimension {} with the following rules\n{} ", dimension, formatRules(rules));

                    long count = calculator.computeDimension(allServerIds, rules);
                    result.addDimensionResult(dimension, count);
                    LOGGER.info("Computation completed for dimension {}. Total count {}.", count, dimension);
                });

                result.setSuccess(true);
            }
            catch (Exception ex) {
                LOGGER.error("Unexpected exception while computing dimensions", ex);

                result.clearDimensions();
                result.setSuccess(false);
            }
        });

        try {
            dimensionFactory.save(result);
        }
        catch (RuntimeException ex) {
            LOGGER.error("Unable to store computation result", ex);
        }

        long duration = ChronoUnit.SECONDS.between(startTime, Instant.now());
        LOGGER.info("PAYG Dimension Computation completed. Process took {} seconds.", duration);
    }

    private String getConfigurationKey(String key) {
        return "taskomatic." + getConfigNamespace() + "." + key;
    }

    private static String formatRules(List<DimensionRule> rules) {
        return IntStream.range(0, rules.size())
                        .mapToObj(idx -> String.format("#%d: %s", idx, rules.get(idx).toString()))
                        .collect(Collectors.joining("\n"));
    }
}
