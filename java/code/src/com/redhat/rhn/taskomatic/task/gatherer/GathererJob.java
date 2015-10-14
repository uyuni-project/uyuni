package com.redhat.rhn.taskomatic.task.gatherer;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManager;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerFactory;
import com.redhat.rhn.taskomatic.task.RhnJavaJob;
import com.suse.manager.gatherer.GathererRunner;
import com.suse.manager.gatherer.JsonHost;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;
import java.util.Map;

/**
 * Taskomatic job for running gatherer on all Virtual Host Managers and processing its
 * results.
 */
public class GathererJob extends RhnJavaJob {

    @Override
    public void execute(JobExecutionContext jobExecutionContext)
            throws JobExecutionException {
        List<VirtualHostManager> managers
                = VirtualHostManagerFactory.getInstance().listVirtualHostManagers();
        if (managers == null || managers.isEmpty()) {
            log.debug("No Virtual Host Managers to run the gatherer job");
            return;
        }

        log.debug(String.format("Running gatherer for %d Virtual Host Managers",
                managers.size()));

        try {
            Map<String, Map<String, JsonHost>> results = new GathererRunner().run(managers);
            log.debug(String.format("Got %d Virtual Host Managers from gatherer",
                    results.size()));

            managers.stream()
                    .filter(manager -> results.containsKey(manager.getLabel()))
                    .forEach(manager -> {
                        String label = manager.getLabel();
                        log.debug("Processing " + label);
                        new VirtualHostManagerProcessor(manager, results.get(label))
                                .processMapping();
                    });
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            HibernateFactory.rollbackTransaction();
        } finally {
            HibernateFactory.closeSession();
        }
    }
}
