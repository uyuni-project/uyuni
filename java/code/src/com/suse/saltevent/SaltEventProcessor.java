package com.suse.saltevent;

import com.redhat.rhn.GlobalInstanceHolder;
import com.suse.manager.reactor.SaltReactor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SaltEventProcessor {
    private static final Logger LOG = LogManager.getLogger(SaltEventProcessor.class);

    public static void main(String[] argv) {
        try {
            LOG.info("Starting Salt Event Processor");

            LOG.debug("Creating SaltReactor...");
            SaltReactor saltReactor = new SaltReactor(
                    GlobalInstanceHolder.SALT_API,
                    GlobalInstanceHolder.SYSTEM_QUERY,
                    GlobalInstanceHolder.SALT_SERVER_ACTION_SERVICE,
                    GlobalInstanceHolder.SALT_UTILS,
                    GlobalInstanceHolder.PAYG_MANAGER,
                    GlobalInstanceHolder.ATTESTATION_MANAGER);

            LOG.info("SaltReactor created successfully");

            LOG.debug("Starting SaltReactor...");
            saltReactor.start();

            LOG.info("SaltReactor started");

        } catch (Exception e) {
            LOG.error("Salt Event Processor failed to start" + e.getMessage());
            System.exit(1);
        }
    }
}