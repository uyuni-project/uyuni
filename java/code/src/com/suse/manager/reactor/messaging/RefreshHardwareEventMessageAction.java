/**
 * Copyright (c) 2016 SUSE LLC
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
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.frontend.events.AbstractDatabaseAction;
import com.suse.manager.reactor.hardware.AbstractHardwareMapper;
import com.suse.manager.reactor.hardware.CpuArchUtil;
import com.suse.manager.reactor.hardware.CpuMapper;
import com.suse.manager.reactor.hardware.DevicesMapper;
import com.suse.manager.reactor.hardware.DmiMapper;
import com.suse.manager.reactor.hardware.SaltServiceInvoker;
import com.suse.manager.reactor.hardware.SysinfoMapper;
import com.suse.manager.reactor.hardware.VirtualizationMapper;
import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.webui.services.SaltGrains;
import com.suse.manager.webui.services.SaltService;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.exception.SaltException;
import com.suse.salt.netapi.results.Result;

import org.apache.log4j.Logger;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Refreshes the minion hardware and network information.
 */
public class RefreshHardwareEventMessageAction extends AbstractDatabaseAction {

    /* Logger for this class */
    private static final Logger LOG = Logger.getLogger(
            RefreshHardwareEventMessageAction.class);

    // Reference to the SaltService instance
    private final SaltService saltService;

    /**
     * The constructor
     * @param saltServiceIn an instance of {@link SaltService}
     */
    public RefreshHardwareEventMessageAction(SaltService saltServiceIn) {
        this.saltService = saltServiceIn;
    }

    @Override
    protected void doExecute(EventMessage msg) {
        RefreshHardwareEventMessage event = (RefreshHardwareEventMessage)msg;
        Action action = ActionFactory.lookupById(event.getActionId());
        if (action != null) {

            Optional<MinionServer> minionServerOpt = MinionServerFactory
                    .findByMinionId(event.getMinionId());

            minionServerOpt.ifPresent(minionServer -> {

                Optional<ServerAction> serverAction = action.getServerActions().stream()
                        .filter(sa -> sa.getServer().equals(minionServer)).findFirst();

                serverAction.ifPresent(sa -> {
                    LOG.debug("Refreshing hardware for: " + minionServer.getMinionId());

                    Boolean minionIsUp = null;
                    List<String> errors = new LinkedList<>();
                    try {
                        Map<String, Result<Boolean>> ping = saltService
                                .ping(new MinionList(minionServer.getMinionId()));
                        minionIsUp = ping.containsKey(minionServer.getMinionId());
                    }
                    catch (SaltException e) {
                        errors.add("Could not 'test.ping' minion: " + e.getMessage());
                    }
                    if (minionIsUp == null || Boolean.FALSE.equals(minionIsUp)) {
                        errors.add("Minion did not respond");
                    }
                    else {
                        SaltServiceInvoker saltInvoker =
                                new SaltServiceInvoker(saltService);

                        ValueMap grains = saltInvoker
                                .getGrains(event.getMinionId())
                                .map(ValueMap::new).orElseGet(ValueMap::new);

                        CpuMapper cpuMapper = new CpuMapper(saltInvoker);
                        cpuMapper.map(minionServer.getId(), grains);
                        checkErrors("CPU", errors, cpuMapper);

                        String cpuarch = grains
                                .getValueAsString(SaltGrains.CPUARCH.getValue());

                        if (CpuArchUtil.isDmiCapable(cpuarch)) {
                            DmiMapper dmiMapper = new DmiMapper(saltInvoker);
                            dmiMapper.map(minionServer.getId(), grains);
                            checkErrors("DMI", errors, dmiMapper);
                        }

                        DevicesMapper devicesMapper = new DevicesMapper(saltInvoker);
                        devicesMapper.map(minionServer.getId(), grains);
                        checkErrors("Devices", errors, devicesMapper);

                        if (CpuArchUtil.isS390(cpuarch)) {
                            SysinfoMapper sysinfoMapper = new SysinfoMapper(saltInvoker);
                            sysinfoMapper.map(minionServer.getId(), grains);
                            checkErrors("S390", errors, sysinfoMapper);
                        }

                        VirtualizationMapper virtMapper =
                                new VirtualizationMapper(saltInvoker);
                        virtMapper.map(minionServer.getId(), grains);
                        checkErrors("Virtualization", errors, virtMapper);

                        LOG.info("Done refreshing hardware info for: " +
                                event.getMinionId());

                        NetworkInfoMapper networkMapper =
                                new NetworkInfoMapper(saltInvoker);
                        networkMapper.map(minionServer.getId(), grains);
                        checkErrors("Network", errors, networkMapper);

                        LOG.info("Done refreshing network info for: " +
                                event.getMinionId());
                    }

                    if (errors.isEmpty()) {
                        sa.setStatus(ActionFactory.STATUS_COMPLETED);
                        sa.setResultMsg("hardware list refreshed");
                        sa.setResultCode(0L);
                    }
                    else {
                        sa.setStatus(ActionFactory.STATUS_FAILED);
                        sa.setResultMsg(
                            "Hardware list could not be refreshed completely\n" +
                                errors.stream().collect(Collectors.joining("\n"))
                        );
                        sa.setResultCode(-1L);
                    }
                    sa.setCompletionTime(new Date());

                    ActionFactory.save(sa);
                });

            });
        }
        else {
            LOG.error("Action not found: " + event.getActionId());
        }
    }

    private void checkErrors(String category, List<String> errors,
                             AbstractHardwareMapper<?> mapper) {
        mapper.getError().ifPresent(e -> errors.add(category + ": " + e));
    }

}
