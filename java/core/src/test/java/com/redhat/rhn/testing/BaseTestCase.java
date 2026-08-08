/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.testing;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.image.ImageInfoFactory;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.action.MinionActionManager;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.manager.recurringactions.RecurringActionManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;

import org.cobbler.MockConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * root class for all test cases
 */
@ExtendWith(UserForTestCaseExtension.class)
public abstract class BaseTestCase {

    @AfterEach
    void tearDownAfterAllTests() {
        // Clear the mock MockConnection
        MockConnection.clear();

        // In case someone disabled it and forgot to re-enable it.
        TestUtils.enableLocalizationLogging();

        // Restore taskomatic API default implementations, in case test mocked it
        restoreTaskomaticApi();

        // Restore the default configuration
        Config.clear();
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
        ImageInfoFactory.setTaskomaticApi(taskomaticApi);
    }
}
