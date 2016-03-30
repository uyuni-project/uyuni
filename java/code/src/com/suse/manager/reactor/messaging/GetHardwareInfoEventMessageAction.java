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
package com.suse.manager.reactor.messaging;

import com.redhat.rhn.common.messaging.MessageAction;
import com.suse.manager.reactor.hardware.CpuArchUtil;
import com.suse.manager.reactor.hardware.SaltServiceInvoker;
import com.suse.manager.reactor.hardware.SysinfoMapper;
import com.suse.manager.reactor.hardware.VirtualizationMapper;
import com.suse.manager.webui.services.SaltGrains;
import org.apache.log4j.Logger;

import com.redhat.rhn.common.messaging.EventMessage;
import com.suse.manager.reactor.hardware.CpuMapper;
import com.suse.manager.reactor.hardware.DevicesMapper;
import com.suse.manager.reactor.hardware.DmiMapper;
import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.webui.services.SaltService;

/**
 * Get and process hardware information from a minion.
 */
public class GetHardwareInfoEventMessageAction implements MessageAction {

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

    /**
     * {@inheritDoc}
     */
    public void execute(EventMessage msg) {
        GetHardwareInfoEventMessage event = (GetHardwareInfoEventMessage) msg;

        SaltServiceInvoker saltInvoker = new SaltServiceInvoker(SALT_SERVICE);
        ValueMap grains = new ValueMap(saltInvoker.getGrains(event.getMinionId()));

        CpuMapper cpuMapper = new CpuMapper(saltInvoker);
        cpuMapper.map(event.getServerId(), grains);

        String cpuarch = grains.getValueAsString(SaltGrains.CPUARCH.getValue());

        if (!CpuArchUtil.isS390(cpuarch)) {
            DmiMapper dmiMapper = new DmiMapper(saltInvoker);
            dmiMapper.map(event.getServerId(), grains);
        }

        DevicesMapper devicesMapper = new DevicesMapper(saltInvoker);
        devicesMapper.map(event.getServerId(), grains);

        if (CpuArchUtil.isS390(cpuarch)) {
            SysinfoMapper sysinfoMapper = new SysinfoMapper(saltInvoker);
            sysinfoMapper.map(event.getServerId(), grains);
        }

        VirtualizationMapper virtMapper = new VirtualizationMapper(saltInvoker);
        virtMapper.map(event.getServerId(), grains);

        LOG.info("Finished getting hardware info for: " + event.getMinionId());
    }
}
