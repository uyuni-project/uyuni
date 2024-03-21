/*
 * Copyright (c) 2024 SUSE LLC
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

package com.suse.coco;

import com.suse.coco.attestation.AttestationQueueProcessor;
import com.suse.coco.configuration.Configuration;
import com.suse.coco.configuration.DefaultConfiguration;
import com.suse.coco.module.AttestationModuleLoader;
import com.suse.common.database.DatabaseSessionFactory;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;

/**
 * Application entry class.
 */
public class CoCoAttestation {

    private static final Logger LOGGER = LogManager.getLogger(CoCoAttestation.class);

    private CoCoAttestation() {
        // Prevent instantiation
    }

    /**
     * Application entry point
     * @param args command line args
     */
    public static void main(String[] args) {
        try {
            LOGGER.info("Starting process at {}", LocalDateTime.now());

            LOGGER.debug("Loading application configuration configuration");
            Configuration configuration = new DefaultConfiguration();

            LOGGER.debug("Initialize Mybatis session factory");
            DatabaseSessionFactory.initialize("mybatis-config.xml", configuration.toProperties());
            SqlSessionFactory sessionFactory = DatabaseSessionFactory.getSessionFactory();

            LOGGER.debug("Loading attestation modules");
            AttestationModuleLoader moduleLoader = new AttestationModuleLoader();
            long count = moduleLoader.loadModules();
            if (count == 0) {
                LOGGER.error("No attestation module found on the classpath. " +
                    "Please install at least one attestation module package.");
                System.exit(0);
                return;
            }
            else {
                LOGGER.info("Supported result type are {}", moduleLoader.getSupportedResultTypes());
            }

            LOGGER.debug("Initializing attestation queue processor");
            var attestationQueueProcessor = new AttestationQueueProcessor(sessionFactory, configuration, moduleLoader);
            attestationQueueProcessor.run();

            LOGGER.debug("Execution completed");
            System.exit(0);
        }
        catch (Exception ex) {
            LOGGER.error("Unexpected exception", ex);
            System.exit(1);
        }

    }
}
