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
package com.suse.manager.reactor.messaging;

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageAction;
import com.redhat.rhn.domain.server.MinionServerFactory;

import com.suse.manager.action.TransactionalActionManager;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.exception.SaltException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

/**
 * Executes a pending transaction check without creating an Uyuni action.
 */
public class CheckTransactionalPendingTransactionEventMessageAction implements MessageAction {

    private static final Logger LOG =
            LogManager.getLogger(CheckTransactionalPendingTransactionEventMessageAction.class);

    private final SaltApi saltApi;

    /**
     * Standard constructor.
     *
     * @param saltApiIn Salt API
     */
    public CheckTransactionalPendingTransactionEventMessageAction(SaltApi saltApiIn) {
        saltApi = saltApiIn;
    }

    @Override
    public void execute(EventMessage msg) {
        CheckTransactionalPendingTransactionEventMessage message =
                (CheckTransactionalPendingTransactionEventMessage) msg;

        MinionServerFactory.lookupById(message.getServerId())
                .ifPresentOrElse(
                        minion -> {
                            try {
                                saltApi.callAsync(
                                        TransactionalActionManager.getPendingTransactionCheckSaltCall(),
                                        new MinionList(minion.getMinionId()),
                                        Optional.empty());
                            }
                            catch (SaltException e) {
                                LOG.warn("Unable to check pending transaction for server {}",
                                        message.getServerId(), e);
                            }
                        },
                        () -> LOG.warn("Unable to check pending transaction for server {}: minion not found",
                                message.getServerId()));
    }

    @Override
    public boolean canRunConcurrently() {
        return true;
    }
}
