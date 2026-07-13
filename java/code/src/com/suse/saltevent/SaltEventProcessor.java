package com.suse.saltevent;

import com.redhat.rhn.GlobalInstanceHolder;

import com.suse.manager.reactor.SaltReactor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SaltEventProcessor {

    private static final Logger LOG = LogManager.getLogger(SaltEventProcessor.class);


    public static void main(String[] argv) {
        SaltReactor saltReactor = new SaltReactor(
                GlobalInstanceHolder.SALT_API,
                GlobalInstanceHolder.SYSTEM_QUERY,
                GlobalInstanceHolder.SALT_SERVER_ACTION_SERVICE,
                GlobalInstanceHolder.SALT_UTILS,
                GlobalInstanceHolder.PAYG_MANAGER,
                GlobalInstanceHolder.ATTESTATION_MANAGER);

        saltReactor.start();
//        if (LOG.isDebugEnabled()) {
        LOG.error("Salt reactor started");
//        }
    }
}
