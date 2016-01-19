/**
 * Copyright (c) 2015 SUSE LLC
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
package com.suse.manager.reactor;

import com.suse.manager.reactor.hardware.CpuArchUtil;
import com.suse.manager.reactor.hardware.SaltServiceInvoker;
import com.suse.manager.reactor.hardware.SysinfoMapper;
import com.suse.manager.reactor.hardware.VirtualizationMapper;
import org.apache.log4j.Logger;

import java.util.Optional;

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.frontend.events.AbstractDatabaseAction;
import com.suse.manager.reactor.hardware.CpuMapper;
import com.suse.manager.reactor.hardware.DevicesMapper;
import com.suse.manager.reactor.hardware.DmiMapper;
import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.webui.services.SaltService;

/**
 * Get and process hardware information from a minion.
 */
public class GetHardwareInfoEventMessageAction extends AbstractDatabaseAction {

    // Logger for this class
    private static final Logger LOG = Logger
            .getLogger(GetHardwareInfoEventMessageAction.class);

    // Reference to the SaltService instance
    private final SaltService SALT_SERVICE;

    /**
     * The constructor.
     * @param saltService a {@link SaltService} instance
     */
    public GetHardwareInfoEventMessageAction(SaltService saltService) {
        this.SALT_SERVICE = saltService;
    }

    @Override
    protected void doExecute(EventMessage msg) {
        GetHardwareInfoEventMessage event = (GetHardwareInfoEventMessage) msg;

        Optional<MinionServer> optionalServer = MinionServerFactory
                .lookupById(event.getServerId());
        optionalServer.ifPresent(server -> {
            String minionId = server.getMinionId();
            SaltServiceInvoker saltInvoker = new SaltServiceInvoker(SALT_SERVICE);
            ValueMap grains = new ValueMap(saltInvoker.getGrains(minionId));

            CpuMapper cpuMapper = new CpuMapper(saltInvoker);
            cpuMapper.map(server, grains);

            String cpuarch = grains.getValueAsString("cpuarch");

            if (!CpuArchUtil.isS390(cpuarch)) {
                DmiMapper dmiMapper = new DmiMapper(saltInvoker);
                dmiMapper.map(server, grains);
            }

            DevicesMapper devicesMapper = new DevicesMapper(saltInvoker);
            devicesMapper.map(server, grains);

            SysinfoMapper sysinfoMapper = new SysinfoMapper(saltInvoker);
            sysinfoMapper.map(server, grains);

            VirtualizationMapper virtMapper = new VirtualizationMapper(saltInvoker);
            virtMapper.map(server, grains);

            LOG.info("Finished getting hardware info for: " + minionId);
        });

        if (!optionalServer.isPresent()) {
            LOG.warn("Server entry not found: " + event.getServerId());
        }
    }
}
