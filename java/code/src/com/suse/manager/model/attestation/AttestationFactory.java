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
package com.suse.manager.model.attestation;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.listview.PageControl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public class AttestationFactory extends HibernateFactory {

    private static final Logger LOG = LogManager.getLogger(AttestationFactory.class);

    /**
     * Save a {@link ServerCoCoAttestationConfig} object
     * @param cnf object to save
     */
    public void save(ServerCoCoAttestationConfig cnf) {
        saveObject(cnf);
    }

    /**
     * Save a {@link ServerCoCoAttestationReport} object
     * @param report object to save
     */
    public void save(ServerCoCoAttestationReport report) {
        saveObject(report);
    }

    /**
     * Save a {@link CoCoAttestationResult} object
     * @param result object to save
     */
    public void save(CoCoAttestationResult result) {
        saveObject(result);
    }

    /**
     * @param serverId the server id
     * @return returns the optional attestation config for the selected system
     */
    public Optional<ServerCoCoAttestationConfig> lookupConfigByServerId(long serverId) {
        return getSession()
                .createQuery("FROM ServerCoCoAttestationConfig WHERE server_id = :serverId",
                        ServerCoCoAttestationConfig.class)
                .setParameter("serverId", serverId)
                .uniqueResultOptional();
    }

    /**
     * @param reportId the report id
     * @return returns the optional attestation report for the given id
     */
    public Optional<ServerCoCoAttestationReport> lookupReportById(long reportId) {
        return getSession()
                .createQuery("FROM ServerCoCoAttestationReport WHERE id = :id",
                        ServerCoCoAttestationReport.class)
                .setParameter("id", reportId)
                .uniqueResultOptional();
    }

    /**
     * @param serverIn the server
     * @return returns the latest attestation report for the given server
     */
    public Optional<ServerCoCoAttestationReport> lookupLatestReportByServer(Server serverIn) {
        return getSession()
                .createQuery("FROM ServerCoCoAttestationReport WHERE server = :server " +
                        " ORDER BY created DESC", ServerCoCoAttestationReport.class)
                .setParameter("server", serverIn)
                .setMaxResults(1)
                .uniqueResultOptional();
    }

    /**
     * @param serverIn the server
     * @param actionIn the action
     * @return returns the attestation report for this server and action if available
     */
    public Optional<ServerCoCoAttestationReport> lookupReportByServerAndAction(Server serverIn, Action actionIn) {
        return getSession()
                .createQuery("FROM ServerCoCoAttestationReport WHERE server = :server AND action = :action",
                        ServerCoCoAttestationReport.class)
                .setParameter("server", serverIn)
                .setParameter("action", actionIn)
                .uniqueResultOptional();
    }

    /**
     * @param resultId the result id
     * @return returns the optional attestation result for the given id
     */
    public Optional<CoCoAttestationResult> lookupResultById(long resultId) {
        return getSession()
                .createQuery("FROM CoCoAttestationResult WHERE id = :id",
                        CoCoAttestationResult.class)
                .setParameter("id", resultId)
                .uniqueResultOptional();
    }

    /**
     * Create a Confidential Compute Attestation Config for a given Server ID
     * @param serverIn the server
     * @param typeIn the environment type
     * @param enabledIn enabled status
     * @return returns the Confidential Compute Attestation Config
     */
    public ServerCoCoAttestationConfig createConfigForServer(Server serverIn, CoCoEnvironmentType typeIn,
                                                             boolean enabledIn) {
        return createConfigForServer(serverIn, typeIn, enabledIn, false);
    }

    /**
     * Create a Confidential Compute Attestation Config for a given Server ID
     * @param serverIn the server
     * @param typeIn the environment type
     * @param enabledIn enabled status
     * @param attestOnBootIn perform attestation on boot
     * @return returns the Confidential Compute Attestation Config
     */
    public ServerCoCoAttestationConfig createConfigForServer(Server serverIn, CoCoEnvironmentType typeIn,
                                                             boolean enabledIn, boolean attestOnBootIn) {
        ServerCoCoAttestationConfig cnf = new ServerCoCoAttestationConfig();
        cnf.setServer(serverIn);
        cnf.setEnvironmentType(typeIn);
        cnf.setEnabled(enabledIn);
        cnf.setAttestOnBoot(attestOnBootIn);
        save(cnf);
        serverIn.setCocoAttestationConfig(cnf);
        return cnf;
    }

    /**
     * Create a Confidential Compute Attestation Report entry for the given server.
     * The entry is initialized with the environment type of the config with the
     * status "pending".
     * @param serverIn the server
     * @return returns a report
     * @throws LookupException when {@link ServerCoCoAttestationConfig} is not available or disabled
     */
    public ServerCoCoAttestationReport createReportForServer(Server serverIn) {
        Optional<ServerCoCoAttestationConfig> cnf = serverIn.getOptCocoAttestationConfig();
        if (cnf.filter(ServerCoCoAttestationConfig::isEnabled).isPresent()) {
            ServerCoCoAttestationReport rpt = new ServerCoCoAttestationReport();
            rpt.setServer(serverIn);
            rpt.setEnvironmentType(cnf.get().getEnvironmentType());
            rpt.setStatus(CoCoAttestationStatus.PENDING);
            save(rpt);
            serverIn.addCocoAttestationReports(rpt);
            return rpt;
        }
        else {
            throw new LookupException("CoCoAttestation Config not found or disabled");
        }
    }

    /**
     * Initialize expected results for a given report.
     * @param report the report
     */
    public void initResultsForReport(ServerCoCoAttestationReport report) {
        CoCoEnvironmentType envType = report.getEnvironmentType();
        for (CoCoResultType t : envType.getSupportedResultTypes()) {
            CoCoAttestationResult result = new CoCoAttestationResult();
            result.setResultType(t);
            result.setReport(report);
            result.setStatus(CoCoAttestationStatus.PENDING);
            result.setDescription(t.getTypeDescription());
            save(result);
            report.addResults(result);
        }
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    /**
     * Return the total number of reports available for a given user
     * @param user the user
     * @return returns a list or reports
     */
    public long countCoCoAttestationReportsForUser(User user) {
        return getSession()
            .createQuery("SELECT COUNT(r.id) FROM UserImpl u " +
                "JOIN u.servers s " +
                "JOIN ServerCoCoAttestationReport r ON r.server = s " +
                "WHERE u = :user", Long.class)
            .setParameter("user", user)
            .uniqueResult();
    }

    /**
     * Return the total number of reports available for a given server
     * @param serverIn the server
     * @return returns a list or reports
     */
    public long countCoCoAttestationReportsForServer(Server serverIn) {
        return getSession()
            .createQuery("SELECT COUNT(*) FROM ServerCoCoAttestationReport r WHERE r.server = :server", Long.class)
            .setParameter("server", serverIn)
            .uniqueResult();
    }

    /**
     * Return a list of reports for a given server with filters
     * @param serverIn the server
     * @param pc page control object
     * @return returns a list or reports
     */
    public List<ServerCoCoAttestationReport> listCoCoAttestationReportsForServer(Server serverIn, PageControl pc) {
        return listCoCoAttestationReportsForServer(serverIn, new Date(0), pc.getStart() - 1, pc.getPageSize());
    }

    /**
     * Return a list of reports for a given server with filters
     * @param serverIn the server
     * @param earliestIn earliest report
     * @param offsetIn number of reports to skip
     * @param limitIn maximal number of reports
     * @return returns a list or reports
     */
    public List<ServerCoCoAttestationReport> listCoCoAttestationReportsForServer(Server serverIn, Date earliestIn,
                                                                                 int offsetIn, int limitIn) {
        return getSession()
                .createQuery("FROM ServerCoCoAttestationReport " +
                        "WHERE server = :server " +
                        "AND created >= :earliest " +
                        "ORDER BY created DESC", ServerCoCoAttestationReport.class)
                .setParameter("server", serverIn)
                .setParameter("earliest", earliestIn)
                .setMaxResults(limitIn)
                .setFirstResult(offsetIn)
                .list();
    }

    /**
     * Return a list of reports for a given server with filters
     * @param user the user
     * @param pc page control object
     * @return returns a list or reports
     */
    public List<ServerCoCoAttestationReport> listCoCoAttestationReportsForUser(User user, PageControl pc) {
        return listCoCoAttestationReportsForUser(user, pc.getStart() - 1, pc.getPageSize());
    }

    /**
     * Return a list of reports for a given server with filters
     * @param user the user
     * @param offsetIn number of reports to skip
     * @param limitIn maximal number of reports
     * @return returns a list or reports
     */
    public List<ServerCoCoAttestationReport> listCoCoAttestationReportsForUser(User user, int offsetIn, int limitIn) {
        return getSession()
            .createQuery("SELECT r FROM UserImpl u " +
                "JOIN u.servers s " +
                "JOIN ServerCoCoAttestationReport r ON r.server = s " +
                "WHERE u = :user " +
                "ORDER BY r.created DESC", ServerCoCoAttestationReport.class)
            .setParameter("user", user)
            .setMaxResults(limitIn)
            .setFirstResult(offsetIn)
            .list();
    }
}
