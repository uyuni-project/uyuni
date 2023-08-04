package com.redhat.rhn.taskomatic.task;

import com.redhat.rhn.manager.audit.CVEAuditManagerOVAL;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class OVALDataSync extends RhnJavaJob {
    @Override
    public String getConfigNamespace() {
        return "oval_data_sync";
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        if (log.isDebugEnabled()) {
            log.debug("Syncing OVAL data");
        }

        CVEAuditManagerOVAL.syncOVAL();

        if (log.isDebugEnabled()) {
            log.debug("Done syncing OVAL data");
        }
    }
}
