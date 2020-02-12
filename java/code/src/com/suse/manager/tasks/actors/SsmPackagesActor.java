package com.suse.manager.tasks.actors;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.ssm.SsmOperationManager;

import org.apache.log4j.Logger;

public class SsmPackagesActor {

    private final static Logger LOG = Logger.getLogger(SsmPackagesActor.class);

    public static void execute(User user, String operationName, Runnable action) {

        /*
         * Comment below applies to more than just SsmPackageUpgrade. Moving here into the
         * (new) base-class for all actions, since it applies generally - there are often
         * systems in RhnSet.SYSTEM to which an Ssm action WILL NOT apply, since SSM
         * allows for heterogeneous sets of systems -- GRG 2013-APR-3
         */
        /*
         * The following isn't 100% accurate. All systems in the SSM are associated with
         * the operation, however only systems on which the package already exists (since
         * this is an upgrade) will actually have events scheduled.
         *
         * The problem is that the list of servers to which the package upgrades apply is
         * never stored in an RhnSet, which is used to make the impact of this call
         * minimal. The correct list is showed to the user before selecting confirm, so
         * the only potential issue is in viewing the SSM task log after the user has
         * confirmed the operation. Again, the events themselves are correctly scheduled
         * on only the systems to which they apply.
         *
         * For now, this small potential for logging inaccuracy is acceptable given the
         * proxmity of this fix to the Satellite 5.3 release (as opposed to omitting the
         * server association to the task entirely).
         *
         * jdobies, Aug 12, 2009
         */

        long operationId = SsmOperationManager.createOperation(user,
                operationName, RhnSetDecl.SYSTEMS.getLabel());

        // Explicitly call handle transactions here so the operation creation above
        // is persisted before the potentially long running logic below
        HibernateFactory.commitTransaction();

        try {
            long actionStart = System.currentTimeMillis();

            action.run();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Time to schedule all actions: " +
                        (System.currentTimeMillis() - actionStart));
            }
        }
        catch (Exception e) {
            LOG.error("Error scheduling package installations for event", e);
        }
        finally {
            // This should stay in the finally block so the operation is
            // not perpetually left in an in progress state
            SsmOperationManager.completeOperation(user, operationId);
        }
    }
}
