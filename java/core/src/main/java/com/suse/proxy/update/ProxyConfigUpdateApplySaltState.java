/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.suse.proxy.update;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.manager.action.ActionManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Optional;

/**
 * Applies proxy configuration salt state
 */
public class ProxyConfigUpdateApplySaltState implements ProxyConfigUpdateContextHandler {
    private static final Logger LOG = LogManager.getLogger(ProxyConfigUpdateApplySaltState.class);

    private static final String FAIL_APPLY_MESSAGE = "Failed to schedule proxy configuration action.";

    @Override
    public void handle(ProxyConfigUpdateContext context) {

        try {
            Action action = ActionManager.scheduleApplyProxyConfig(context.getUser(),
                Collections.singletonList(context.getProxyMinion().getId()),
                Optional.of(context.getProxyConfigFiles())
                );
            context.setAction(action);
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            context.getErrorReport().register(FAIL_APPLY_MESSAGE);
            LOG.error(FAIL_APPLY_MESSAGE, e);
        }
    }
}
