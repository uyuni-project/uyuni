/*
 * Copyright (c) 2024 SUSE LLC
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
package com.suse.manager.attestation;

import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.CoCoAttestationAction;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.model.attestation.AttestationFactory;
import com.suse.manager.model.attestation.CoCoAttestationResult;
import com.suse.manager.model.attestation.CoCoEnvironmentType;
import com.suse.manager.model.attestation.ServerCoCoAttestationConfig;
import com.suse.manager.model.attestation.ServerCoCoAttestationReport;
import com.suse.manager.webui.services.pillar.MinionPillarManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Attestation Manager
 */
public class AttestationManager {
    private static final Logger LOG = LogManager.getLogger(AttestationManager.class);
    private final AttestationFactory factory;
    private final TaskomaticApi taskomaticApi;

    /**
     * Constructor
     */
    public AttestationManager() {
        this(new AttestationFactory(), new TaskomaticApi());
    }

    /**
     * Constructor
     * @param factoryIn the attestation factory
     * @param taskomaticApiIn the taskomatic api
     */
    public AttestationManager(AttestationFactory factoryIn, TaskomaticApi taskomaticApiIn) {
        factory = factoryIn;
        taskomaticApi = taskomaticApiIn;
    }

    /**
     * Create an Attestation Action for a given minion.
     * @param userIn the user
     * @param minionIn the minion
     * @param earliest the earliest execution date
     * @return returns a {@link CoCoAttestationAction}
     */
    public CoCoAttestationAction scheduleAttestationAction(User userIn, MinionServer minionIn, Date earliest)
            throws TaskomaticApiException {
        ensureSystemAccessible(userIn, minionIn);
        ensureSystemConfigured(minionIn);
        CoCoAttestationAction action = (CoCoAttestationAction) ActionFactory.createAction(
                ActionFactory.TYPE_COCO_ATTESTATION, earliest);
        action.setSchedulerUser(userIn);
        action.setOrg(userIn.getOrg());
        action.setName("Confidential Compute Attestation");
        ActionFactory.save(action);

        //Initialize the report
        ServerCoCoAttestationReport initReport = initializeReport(userIn, minionIn);
        initReport.setAction(action);
        if (initReport.getEnvironmentType().isNonceRequired()) {
            SecureRandom rand = new SecureRandom();
            byte[] bytes = new byte[64];
            rand.nextBytes(bytes);
            initReport.setInData(Map.of("nonce", Base64.getEncoder().encodeToString(bytes)));
        }

        MinionPillarManager.INSTANCE.generatePillar(minionIn, false, MinionPillarManager.PillarSubset.GENERAL);
        ActionManager.scheduleForExecution(action, Set.of(minionIn.getId()));
        action = (CoCoAttestationAction) ActionFactory.save(action);
        taskomaticApi.scheduleActionExecution(action);
        return action;
    }

    /**
     * Create a Attestation configuration for a given server
     * @param userIn the user
     * @param serverIn the server
     * @param typeIn the environment type
     * @param enabledIn should the config been enabled
     * @return returns the configuration
     */
    public ServerCoCoAttestationConfig createConfig(User userIn, Server serverIn, CoCoEnvironmentType typeIn,
                                                    boolean enabledIn) {
        ensureSystemAccessible(userIn, serverIn);
        return factory.createConfigForServer(serverIn, typeIn, enabledIn);
    }

    /**
     * Initialize a new Attestation Report with defaults from the Server Configuration
     * @param userIn the user
     * @param serverIn the server
     * @return returns the initialized report
     */
    public ServerCoCoAttestationReport initializeReport(User userIn, Server serverIn) {
        ensureSystemAccessible(userIn, serverIn);
        return factory.createReportForServer(serverIn);
    }

    /**
     * Initialze the Attestation Results for a given report
     * @param userIn the user
     * @param reportIn the report
     */
    public void initializeResults(User userIn, ServerCoCoAttestationReport reportIn) {
        Server server = reportIn.getServer();
        ensureSystemAccessible(userIn, server);
        factory.initResultsForReport(reportIn);
    }

    /**
     * @param userIn the user
     * @param serverIn the server
     * @param actionIn the action
     * @return returns the attestation report for this server and action if available
     */
    public Optional<ServerCoCoAttestationReport> lookupReportByServerAndAction(
            User userIn, Server serverIn, Action actionIn) {
        ensureSystemAccessible(userIn, serverIn);
        return factory.lookupReportByServerAndAction(serverIn, actionIn);
    }

    /**
     * Return a list of reports for the given server and filters
     *
     * @param userIn the user
     * @param serverIn the server
     * @param earliest earliest report
     * @param offset number of reports to skip
     * @param limit maximum number of reports
     * @return returns a list of reports
     */
    public List<ServerCoCoAttestationReport> listCoCoAttestationReports(User userIn, Server serverIn, Date earliest,
                                                                        int offset, int limit) {
        ensureSystemAccessible(userIn, serverIn);
        return factory.listCoCoAttestationReports(serverIn, earliest, offset, limit);
    }

    /**
     * Return latest attestation report for the given server
     * @param userIn the user
     * @param serverIn the server
     * @return return the latest report or throws an excpetion
     */
    public ServerCoCoAttestationReport lookupLatestCoCoAttestationReport(User userIn, Server serverIn) {
        ensureSystemAccessible(userIn, serverIn);
        return factory.lookupLatestReportByServer(serverIn).orElseThrow(() -> {
            LOG.error("No Report found for server: {}", serverIn);
            return new LookupException("No Report found");
        });
    }

    /**
     * Lookup a specific attestation result identified by the result id for a given server.
     * It is checked if the user manages the server and if the result belong to a report of that server.
     *
     * @param userIn the user
     * @param serverIn the server
     * @param resultId the result id
     * @return optional result
     */
    public Optional<CoCoAttestationResult> lookupCoCoAttestationResult(User userIn, Server serverIn, int resultId) {
        ensureSystemAccessible(userIn, serverIn);
        Optional<CoCoAttestationResult> optRes = factory.lookupResultById(resultId);
        if (optRes
                .map(CoCoAttestationResult::getReport)
                .map(ServerCoCoAttestationReport::getServer)
                .stream().noneMatch(s -> s.equals(serverIn))) {
            String msg = "Result not for the given server";
            LOG.error(msg);
            throw new LookupException(msg);
        }
        return optRes;
    }

    private void ensureSystemAccessible(User userIn, Server serverIn) {
        if (serverIn == null) {
            LOG.error("Server not found");
            throw new LookupException("Server not found");
        }
        else if (!SystemManager.isAvailableToUser(userIn, serverIn.getId())) {
            String msg = String.format("User '%s' can't access system '%s'", userIn, serverIn.getName());
            LOG.error(msg);
            throw new PermissionException(msg);
        }
    }

    private void ensureSystemConfigured(Server serverIn) {
        if (serverIn == null) {
            LOG.error("Server not found");
            throw new LookupException("Server not found");
        }
        ServerCoCoAttestationConfig cnf = serverIn.getOptCocoAttestationConfig()
                .orElseThrow(() -> {
                    LOG.error("Configuration not initialized");
                    return new LookupException("Configuration not found");
                });
        if (!cnf.isEnabled()) {
            LOG.error("Attestation disabled");
            throw new AttestationDisabledException();
        }
    }

}
