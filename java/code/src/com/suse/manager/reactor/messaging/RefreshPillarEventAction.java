/**
 * Copyright (c) 2020 SUSE LLC
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
package com.suse.manager.reactor.messaging;

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageAction;
import com.redhat.rhn.frontend.events.RefreshPillarEvent;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.salt.netapi.datatypes.target.MinionList;
import org.apache.log4j.Logger;

import java.util.function.Consumer;

/**
 * Refresh pillar on minions.
 */
public class RefreshPillarEventAction implements MessageAction {

    /* Logger for this class */
    private static final Logger LOG = Logger.getLogger(RefreshPillarEventAction.class);

    private SaltService saltService;

    /**
     * Constructor
     */
    public RefreshPillarEventAction() {
        this.saltService = SaltService.INSTANCE;
    }

    @Override
    public void execute(EventMessage msg) {
        RefreshPillarEvent event = (RefreshPillarEvent)msg;
        LOG.debug("Refreshing pillar on: " + String.join(" ", event.getMinionIds()));
        saltService.refreshPillar(new MinionList(event.getMinionIds()));
    }

    @Override
    public boolean canRunConcurrently() {
        return true;
    }

    @Override
    public boolean needsTransactionHandling() {
        return false;
    }

    @Override
    public Consumer<Exception> getExceptionHandler() {
        return null;
    }
}
