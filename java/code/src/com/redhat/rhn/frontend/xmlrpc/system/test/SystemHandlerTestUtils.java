/**
 * Copyright (c) 2018 SUSE LLC
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

package com.redhat.rhn.frontend.xmlrpc.system.test;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.channel.SubscribeChannelsAction;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.system.SystemHandler;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit3.JUnit3Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;

public class SystemHandlerTestUtils {

    private final Mockery MOCK_CONTEXT = new JUnit3Mockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    public SystemHandler getMockedHandler() throws Exception {
        TaskomaticApi taskomaticMock = MOCK_CONTEXT.mock(TaskomaticApi.class);
        SystemHandler systemHandler = new SystemHandler(taskomaticMock);

        MOCK_CONTEXT.checking(new Expectations() {{
            allowing(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
            allowing(taskomaticMock)
                    .scheduleSubscribeChannels(with(any(User.class)), with(any(SubscribeChannelsAction.class)));
        }});

        return systemHandler;
    }
}
