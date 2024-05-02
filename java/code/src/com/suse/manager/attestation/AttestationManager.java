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
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.CoCoAttestationAction;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.listview.PageControl;
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
        return scheduleAttestationAction(userIn, minionIn, earliest, null);
    }

    /**
     * Create an Attestation Action for a given minion.
     * @param userIn the user
     * @param minionIn the minion
     * @param earliest the earliest execution date
     * @param actionChain the action chain
     * @return returns a {@link CoCoAttestationAction}
     */
    public CoCoAttestationAction scheduleAttestationAction(User userIn, MinionServer minionIn, Date earliest,
                                                           ActionChain actionChain)
            throws TaskomaticApiException {
        ensureSystemAccessible(userIn, minionIn);
        ensureSystemConfigured(minionIn);
        List<CoCoAttestationAction> coCoAttestationActions = scheduleAttestationAction(Optional.of(userIn),
            userIn.getOrg(), Set.of(minionIn), earliest, actionChain);
        return coCoAttestationActions.get(0);
    }

    /**
     * Create an Attestation Action for a given minion.
     * @param orgIn the org
     * @param minionIn the minion
     * @param earliest the earliest execution date
     */
    public void scheduleAttestationActionFromSystem(Org orgIn, MinionServer minionIn, Date earliest)
            throws TaskomaticApiException {
        ensureSystemConfigured(minionIn);
        scheduleAttestationAction(Optional.empty(), orgIn, Set.of(minionIn), earliest, null);
    }

    /**
     * Create an Attestation Action for a given minion.
     * @param userIn the user
     * @param minionsSet the minions
     * @param earliest the earliest execution date
     * @param actionChain the action chain
     * @return the list of scheduled actions
     */
    public List<CoCoAttestationAction> scheduleAttestationActionForSystems(User userIn, Set<MinionServer> minionsSet,
            Date earliest, ActionChain actionChain) throws TaskomaticApiException {
        minionsSet.forEach(minion -> ensureSystemConfigured(minion));
        return scheduleAttestationAction(Optional.empty(), userIn.getOrg(), minionsSet, earliest, actionChain);
    }

    private List<CoCoAttestationAction> scheduleAttestationAction(Optional<User> userIn, Org orgIn,
            Set<MinionServer> minionsSet, Date earliest, ActionChain actionChain) throws TaskomaticApiException {
        if (actionChain != null) {
            return scheduleActionChain(userIn, orgIn, minionsSet, earliest, actionChain);
        }

        return scheduleSingleAction(userIn, orgIn, minionsSet, earliest);
    }

    private List<CoCoAttestationAction> scheduleSingleAction(Optional<User> userIn, Org orgIn,
                                                                  Set<MinionServer> minionsSet, Date earliest)
        throws TaskomaticApiException {
        CoCoAttestationAction action = createAttestationAction(userIn.orElse(null), orgIn, earliest);
        minionsSet.forEach(minionServer -> initializeReport(action, minionServer));

        Set<Long> minionIds = minionsSet.stream().map(Server::getId).collect(Collectors.toSet());
        ActionManager.scheduleForExecution(action, minionIds);

        CoCoAttestationAction updated = (CoCoAttestationAction) ActionFactory.save(action);
        taskomaticApi.scheduleActionExecution(updated);

        return List.of(updated);
    }

    private List<CoCoAttestationAction> scheduleActionChain(Optional<User> userIn, Org orgIn,
                                                            Set<MinionServer> minionsSet, Date earliest,
                                                            ActionChain actionChain) {
        int nextSortOrder = ActionChainFactory.getNextSortOrderValue(actionChain);

        List<CoCoAttestationAction> actionsList = new ArrayList<>();
        for (MinionServer server : minionsSet) {
            CoCoAttestationAction action = createAttestationAction(userIn.orElse(null), orgIn, earliest);
            initializeReport(action, server);

            ActionChainFactory.queueActionChainEntry(action, actionChain, server.getId(), nextSortOrder);
            actionsList.add(action);
        }

        return actionsList;
    }

    private static CoCoAttestationAction createAttestationAction(User user, Org org, Date earliest) {
        CoCoAttestationAction action = (CoCoAttestationAction) ActionFactory.createAction(
            ActionFactory.TYPE_COCO_ATTESTATION, earliest);
        action.setSchedulerUser(user);
        action.setOrg(org);
        action.setName("Confidential Compute Attestation");
        ActionFactory.save(action);
        return action;
    }

    private void initializeReport(CoCoAttestationAction action, MinionServer minion) {
        ServerCoCoAttestationReport initReport = factory.createReportForServer(minion);
        initReport.setAction(action);
        if (initReport.getEnvironmentType().isNonceRequired()) {
            SecureRandom rand = new SecureRandom();
            byte[] bytes = new byte[64];
            rand.nextBytes(bytes);
            initReport.setInData(Map.of("nonce", Base64.getEncoder().encodeToString(bytes)));
        }

        MinionPillarManager.INSTANCE.generatePillar(minion, false, MinionPillarManager.PillarSubset.GENERAL);
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
        return createConfig(userIn, serverIn, typeIn, enabledIn, false);
    }
        /**
         * Create a Attestation configuration for a given server
         * @param userIn the user
         * @param serverIn the server
         * @param typeIn the environment type
         * @param enabledIn should the config been enabled
         * @param attestOnBootIn should the attestation be performed on system boot
         * @return returns the configuration
         */
    public ServerCoCoAttestationConfig createConfig(User userIn, Server serverIn, CoCoEnvironmentType typeIn,
                                                    boolean enabledIn, boolean attestOnBootIn) {
        ensureSystemAccessible(userIn, serverIn);
        return factory.createConfigForServer(serverIn, typeIn, enabledIn, attestOnBootIn);
    }


    /**
     * Retrieves an existing configuration
     * @param userIn the user
     * @param serverIn the server
     * @return returns the configuration, if present
     */
    public Optional<ServerCoCoAttestationConfig> getConfig(User userIn, Server serverIn) {
        ensureSystemAccessible(userIn, serverIn);
        return factory.lookupConfigByServerId(serverIn.getId());
    }

    /**
     * Saves the specified configuration
     * @param userIn the user
     * @param configIn the configuration
     */
    public void saveConfig(User userIn, ServerCoCoAttestationConfig configIn) {
        ensureSystemAccessible(userIn, configIn.getServer());
        factory.save(configIn);
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
     * @param reportIn the report
     */
    public void initializeResults(ServerCoCoAttestationReport reportIn) {
        if (Optional.ofNullable(reportIn.getServer()).isEmpty()) {
            LOG.error("Report not linked to a system");
            throw new LookupException("Report not linked to a system");
        }
        factory.initResultsForReport(reportIn);
    }

    /**
     * @param serverIn the server
     * @param actionIn the action
     * @return returns the attestation report for this server and action if available
     */
    public Optional<ServerCoCoAttestationReport> lookupReportByServerAndAction(Server serverIn, Action actionIn) {
        return factory.lookupReportByServerAndAction(serverIn, actionIn);
    }

    /**
     * Return the number of existing reports for the given user
     *
     * @param userIn the user
     * @return returns a list of reports
     */
    public long countCoCoAttestationReportsForUser(User userIn) {
        return factory.countCoCoAttestationReportsForUser(userIn);
    }


    /**
     * Return the count of attestation report the given server
     *
     * @param userIn the user
     * @param serverIn the server
     * @return returns the number of attestation reports
     */
    public long countCoCoAttestationReportsForUserAndServer(User userIn, Server serverIn) {
        ensureSystemAccessible(userIn, serverIn);
        return factory.countCoCoAttestationReportsForServer(serverIn);
    }

    /**
     * Return a list of reports for the given server and filters
     *
     * @param userIn the user
     * @param serverIn the server
     * @param pc page control object
     * @return returns a list of reports
     */
    public List<ServerCoCoAttestationReport> listCoCoAttestationReportsForUserAndServer(User userIn, Server serverIn,
                                                                                        PageControl pc) {
        ensureSystemAccessible(userIn, serverIn);
        return factory.listCoCoAttestationReportsForServer(serverIn, pc);
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
    public List<ServerCoCoAttestationReport> listCoCoAttestationReportsForUserAndServer(User userIn, Server serverIn,
                                                                                        Date earliest, int offset,
                                                                                        int limit) {
        ensureSystemAccessible(userIn, serverIn);
        return factory.listCoCoAttestationReportsForServer(serverIn, earliest, offset, limit);
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

    /**
     * Return a list of reports for the given user
     *
     * @param userIn the user
     * @param pc page control object
     * @return returns a list of reports
     */
    public List<ServerCoCoAttestationReport> listCoCoAttestationReportsForUser(User userIn, PageControl pc) {
        return factory.listCoCoAttestationReportsForUser(userIn, pc);
    }

    /**
     * Return a list of reports for the given user
     *
     * @param userIn the user
     * @param offset number of reports to skip
     * @param limit maximum number of reports
     * @return returns a list of reports
     */
    public List<ServerCoCoAttestationReport> listCoCoAttestationReportsForUser(User userIn, int offset, int limit) {
        return factory.listCoCoAttestationReportsForUser(userIn, offset, limit);
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
