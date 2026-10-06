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
package com.suse.manager.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.server.ServerAction;

import org.junit.jupiter.api.Test;

public class SaltUtilsTransactionalResultTest {

    @Test
    public void testNonTransactionalResultDoesNotRequireTransactionalHistory() {
        Action action = new Action();
        action.setId(10L);
        ServerAction serverAction = new ServerAction();
        serverAction.setParentAction(action);

        assertFalse(SaltUtils.hasPostTransactionalStateForResult(action, serverAction, false));
    }
}
