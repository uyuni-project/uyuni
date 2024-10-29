/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.testing;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.image.ImageInfoFactory;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.action.MinionActionManager;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.manager.recurringactions.RecurringActionManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;

import com.suse.manager.virtualization.VirtualizationActionHelper;

import org.cobbler.test.MockConnection;
import org.hibernate.TransactionException;

/**
 * TestCaseHelper - helper class to contain some common logic
 * between a few of our base unit test classes.
 */
public class TestCaseHelper {

    private TestCaseHelper() {
    }

    /**
     * shared logic for tearing down resources used in our unit tests
     */
    public static void tearDownHelper() {
        TransactionException rollbackException = null;
        if (HibernateFactory.inTransaction()) {
            try {
                HibernateFactory.rollbackTransaction();
            }
            catch (TransactionException e) {
                rollbackException = e;
            }
        }
        HibernateFactory.closeSession();
        if (rollbackException != null) {
            throw rollbackException;
        }

        // Clear the mock MockConnection
        MockConnection.clear();

        // In case someone disabled it and forgot to renable it.
        TestUtils.enableLocalizationLogging();

        // Restore taskomatic API default implementations, in case test mocked it
        restoreTaskomaticApi();
    }

    private static void restoreTaskomaticApi() {
        TaskomaticApi taskomaticApi = new TaskomaticApi();

        ActionChainManager.setTaskomaticApi(taskomaticApi);
        ActionChainFactory.setTaskomaticApi(taskomaticApi);
        ActionManager.setTaskomaticApi(taskomaticApi);
        MinionActionManager.setTaskomaticApi(taskomaticApi);
        ChannelManager.setTaskomaticApi(taskomaticApi);
        ErrataManager.setTaskomaticApi(taskomaticApi);
        RecurringActionManager.setTaskomaticApi(taskomaticApi);
        VirtualizationActionHelper.setTaskomaticApi(taskomaticApi);
        ImageInfoFactory.setTaskomaticApi(taskomaticApi);
    }
}
