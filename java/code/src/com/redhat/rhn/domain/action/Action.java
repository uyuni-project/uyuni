/*
 * Copyright (c) 2025 SUSE LLC
 * Copyright (c) 2009--2015 Red Hat, Inc.
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
package com.redhat.rhn.domain.action;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.util.StringUtil;
import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.legacy.UserImpl;

import com.suse.manager.model.attestation.ServerCoCoAttestationReport;
import com.suse.manager.utils.SaltUtils;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.websocket.WebSocketActionIdProvider;
import com.suse.salt.netapi.calls.LocalCall;

import com.google.gson.JsonElement;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.DiscriminatorOptions;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import javax.servlet.http.HttpServletRequest;

/**
 * Action - Class representation of the table rhnAction.
 */
@Entity
@Table(name = "rhnAction")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "action_type", discriminatorType = DiscriminatorType.INTEGER)
@DiscriminatorOptions(insert = false)
@DiscriminatorValue("-1")
public class Action extends BaseDomainHelper implements Serializable, WebSocketActionIdProvider {
    protected static final Logger LOG = LogManager.getLogger(Action.class);

    public static final Integer NAME_LENGTH_LIMIT = 128;

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rhn_action_seq")
	@SequenceGenerator(name = "rhn_action_seq", sequenceName = "rhn_event_id_seq", allocationSize = 1)
    private Long id;

    @Column
    private String name;

    @Column(name = "earliest_action")
    private Date earliestAction;

    @Column
    private Long version;

    @Column
    private Long archived;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "prerequisite")
    private Action prerequisite;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "action_type")
    private ActionType actionType;

    @OneToMany(mappedBy = "parentAction", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<ServerAction> serverActions;

    @OneToMany(mappedBy = "action", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<ServerCoCoAttestationReport> cocoAttestationReports;

    @ManyToOne(targetEntity = UserImpl.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "scheduler")
    private User schedulerUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id")
    private Org org;

    /**
     * The ActionFormatter associated with this Action.  Protected
     * so subclasses can init it.
     */
    @Transient
    protected transient ActionFormatter formatter;

    /**
     * Getter for id
     * @return Long to get
    */
    public Long getId() {
        return this.id;
    }

    /**
     * Setter for id
     * @param idIn to set
    */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * Getter for actionType
     * @return ActionType to get
    */
    public ActionType getActionType() {
        return this.actionType;
    }

    /**
     * Setter for actionType
     * @param actionTypeIn to set
    */
    public void setActionType(ActionType actionTypeIn) {
        this.actionType = actionTypeIn;
    }

    /**
     * Getter for name
     * @return String to get
    */
    public String getName() {
        return this.name;
    }

    /**
     * Setter for name
     * @param nameIn to set
    */
    public void setName(String nameIn) {
        if (nameIn != null) {
            this.name = StringUtil.getBytesTruncatedString(nameIn, NAME_LENGTH_LIMIT);
        }
    }

    /**
     * Getter for earliestAction
     * @return Date to get
    */
    public Date getEarliestAction() {
        return this.earliestAction;
    }

    /**
     * Setter for earliestAction
     * @param earliestActionIn to set
    */
    public void setEarliestAction(Date earliestActionIn) {
        this.earliestAction = earliestActionIn;
    }

    /**
     * Getter for version
     * @return Long to get
    */
    public Long getVersion() {
        return this.version;
    }

    /**
     * Setter for version
     * @param versionIn to set
    */
    public void setVersion(Long versionIn) {
        this.version = versionIn;
    }

    /**
     * Getter for archived
     * @return Long to get
    */
    public Long getArchived() {
        return this.archived;
    }

    /**
     * Setter for archived
     * @param archivedIn to set
    */
    public void setArchived(Long archivedIn) {
        this.archived = archivedIn;
    }

    /**
     * Getter for CocoAttestationReports
     * @return return the attestation reports
     */
    public Set<ServerCoCoAttestationReport> getCocoAttestationReports() {
        return cocoAttestationReports;
    }

    /**
     * Setter for CocoAttestationReports
     * @param cocoAttestationReportsIn the reports
     */
    public void setCocoAttestationReports(Set<ServerCoCoAttestationReport> cocoAttestationReportsIn) {
        cocoAttestationReports = cocoAttestationReportsIn;
    }

    /**
     * Add CocoAttestationReport to Action
     * @param cocoAttestationReportIn the report to add
     */
    public void addCocoAttestationReport(ServerCoCoAttestationReport cocoAttestationReportIn) {
        if (cocoAttestationReports == null) {
            cocoAttestationReports = new HashSet<>();
        }
        cocoAttestationReports.add(cocoAttestationReportIn);
    }

    /**
     * Getter for prerequisite
     * @return Long to get
    */
    public Action getPrerequisite() {
        return this.prerequisite;
    }

    /**
     * Setter for prerequisite
     * @param prerequisiteIn to set
    */
    public void setPrerequisite(Action prerequisiteIn) {
        this.prerequisite = prerequisiteIn;
    }

    /**
     * Getter for serverActions.  Contains:
     * a collection of: com.redhat.rhn.domain.action.server.ServerAction classes
     * @return Set of com.redhat.rhn.domain.action.server.ServerAction classes
    */
    public Set<ServerAction> getServerActions() {
        return this.serverActions;
    }

    /**
     * Setter for serverActions.   Contains:
     * a collection of: com.redhat.rhn.domain.action.server.ServerAction classes
     * @param serverActionsIn to set
    */
    public void setServerActions(Set<ServerAction> serverActionsIn) {
        this.serverActions = serverActionsIn;
    }

    /**
     * Add a ServerAction to this Action
     * @param saIn serverAction to add
     */
    public void addServerAction(ServerAction saIn) {
        if (serverActions == null) {
            serverActions = new HashSet<>();
        }
        saIn.setParentActionWithCheck(this);
        serverActions.add(saIn);
    }

    /**
    * Set the Scheduler User who scheduled this Action
    * @param schedulerIn the User who did the scheduling
    */
    public void setSchedulerUser(User schedulerIn) {
        this.schedulerUser = schedulerIn;
    }

    /**
     * Get the User who scheduled this Action.
     * @return User who scheduled this Action
     */
    public User getSchedulerUser() {
        return this.schedulerUser;
    }

    /**
     * @return Returns the org.
     */
    public Org getOrg() {
        return org;
    }
    /**
     * @param orgIn The org to set.
     */
    public void setOrg(Org orgIn) {
        this.org = orgIn;
    }
    /**
     * Get the formatter for this class.  Subclasses may override
     * the ActionFormatter to provide custom output.
     * @return ActionFormatter for this class.
     */
    public ActionFormatter getFormatter() {
        if (formatter == null) {
            formatter = new ActionFormatter(this);
        }
        return formatter;
    }

    /**
     * Get the count of the number of times this action has failed.
     * @return Count of failed actions.
     */
    public Long getFailedCount() {
        return getActionStatusCount(ActionFactory.STATUS_FAILED);
    }

    /**
     * Get the count of the number of times this action has succeeded.
     * @return Count of successful actions.
     */
    public Long getSuccessfulCount() {
        return getActionStatusCount(ActionFactory.STATUS_COMPLETED);
    }

    // Get the number of ServerAction objects that match
    // the passed in ActionStatus
    private long getActionStatusCount(ActionStatus status) {
        return ActionFactory.getServerActionCountByStatus(this, status);
    }

    /**
     * @return true if all servers have finished the action as either COMPLETED or FAILED
     */
    public boolean allServersFinished() {
        if (getServerActions() == null) {
            return true;
        }
        return getServerActions().stream().allMatch(sa -> sa.isStatusCompleted() ||
                sa.isStatusFailed());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Action castOther)) {
            return false;
        }
        return new EqualsBuilder().append(this.getId(), castOther.getId())
                                  .append(this.getOrg(), castOther.getOrg())
                                  .append(this.getName(), castOther.getName())
                                  .append(this.getEarliestAction(),
                                          castOther.getEarliestAction())
                                  .append(this.getVersion(), castOther.getVersion())
                                  .append(this.getArchived(), castOther.getArchived())
                                  .append(this.getCreated(), castOther.getCreated())
                                  .append(this.getModified(), castOther.getModified())
                                  .append(this.getPrerequisite(),
                                          castOther.getPrerequisite())
                                  .append(this.getActionType(), castOther.getActionType())
                                  .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.getId()).append(this.getOrg())
                                    .append(this.getName())
                                    .append(this.getEarliestAction())
                                    .append(this.getVersion())
                                    .append(this.getArchived())
                                    .append(this.getCreated())
                                    .append(this.getModified())
                                    .append(this.getPrerequisite())
                                    .append(this.getActionType()).toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return " : " + name;
    }

    /**
     * Hook when action is canceled.
     */
    public void onCancelAction() {
        // Something to do, when action is canceled.
        // Override this method for specific action.
    }

    /**
     * Hook when an action failed.
     * @param serverActionIn the {@link ServerAction} which failed
     */
    public void onFailAction(ServerAction serverActionIn) {
        // Something to do, when an action failed.
        // Override this method for specific action.
    }

    /**
     * @param server server to which action is linked
     * @param currentUser user
     * @return string which is used on system history details
     */
    public String getHistoryDetails(Server server, User currentUser) {
        return StringUtils.EMPTY;
    }

    @Override
    public String getWebSocketActionId() {
        return null;
    }

    /**
     * @return Returns the type name.
     */
    public String getActionTypeName() {
        return this.actionType.getName();
    }

    /**
     * @param minionSummaries a list of minion summaries of the minions involved in the given Action
     * @return minion summaries grouped by local call
     */
    public Map<LocalCall<?>, List<MinionSummary>> getSaltCalls(List<MinionSummary> minionSummaries) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Action type {} is not supported with Salt", actionType != null ? actionType.getName() : "");
        }
        return Collections.emptyMap();
    }

    public static class UpdateAuxArgs {
        private final long retcode;
        private final boolean success;
        private final String jid;
        private final SaltUtils saltUtils;
        private final SaltApi saltApi;
        private final SystemQuery systemQuery;

        /**
         * @param retcodeIn
         * @param successIn
         * @param jidIn
         * @param saltUtilsIn
         * @param saltApiIn
         * @param systemQueryIn
         */
        public UpdateAuxArgs(long retcodeIn, boolean successIn, String jidIn, SaltUtils saltUtilsIn, SaltApi saltApiIn,
                             SystemQuery systemQueryIn) {
            retcode = retcodeIn;
            success = successIn;
            jid = jidIn;
            saltUtils = saltUtilsIn;
            saltApi = saltApiIn;
            systemQuery = systemQueryIn;
        }

        public long getRetcode() {
            return retcode;
        }

        public boolean getSuccess() {
            return success;
        }

        public String getJid() {
            return jid;
        }

        public SaltUtils getSaltUtils() {
            return saltUtils;
        }

        public SaltApi getSaltApi() {
            return saltApi;
        }

        public SystemQuery getSystemQuery() {
            return systemQuery;
        }
    }

    /**
     * @param serverAction the server action to update
     * @param jsonResult the action result
     * @param auxArgs object containing auxiliary arguments to the call
     */
    public void handleUpdateServerAction(ServerAction serverAction, JsonElement jsonResult, UpdateAuxArgs auxArgs) {
        serverAction.setResultMsg(SaltUtils.getJsonResultWithPrettyPrint(jsonResult));
    }

    /**
     * Removes results of queued action.
     */
    public void removeInvalidResults() {
        //default does nothing
    }

    /**
     * formatByosListToStringErrorMsg formats a list of MinionSummary to show it as error message.
     * If there are 2 or less it will return the names of the BYOS instances. If more than two, it will return a
     * String with two of the BYOS instances plus "... and X more" to avoid having endless error message.
     * @param byosMinions
     * @return the error message formated
     */
    private String formatByosListToStringErrorMsg(List<MinionSummary> byosMinions) {
        if (byosMinions.size() <= 2) {
            return byosMinions.stream()
                    .map(MinionSummary::getMinionId)
                    .collect(Collectors.joining(","));
        }

        String errorMsg = byosMinions.stream()
                .map(MinionSummary::getMinionId)
                .limit(2)
                .collect(Collectors.joining(","));

        int numberOfLeftByosServers = byosMinions.size() - 2;

        return String.format("%s and %d more", errorMsg, numberOfLeftByosServers);
    }

    /**
     * rejectScheduleActionIfByos rejects an action if any of the servers within it is byos
     * @return true if the action was stopped due to byos servers within it, false otherwise
     */
    public boolean rejectScheduleActionIfByos() {
        List<MinionSummary> byosMinions = MinionServerFactory.findByosServers(this);
        if (CollectionUtils.isNotEmpty(byosMinions)) {
            LOG.error("To manage BYOS or DC servers from SUSE Multi-Linux Manager PAYG, SCC credentials must be " +
                    "in place.");
            Object[] args = {formatByosListToStringErrorMsg(byosMinions)};
            ActionFactory.rejectScheduledActions(List.of(getId()),
                    LocalizationService.getInstance()
                            .getMessage("task.action.rejection.notcompliantPaygByos", args));
            return true;
        }
        return false;
    }

    /**
     * depending on the event type, we need to retrieve additional information and store that information in the result
     *
     * @param serverAction the server action
     * @return additionalInfo list of info items
     */
    public List<Map<String, String>> createActionSpecificDetails(ServerAction serverAction) {
        return new ArrayList<>();
    }

    /**
     * Prepare to execute staging job via Salt
     * @param minionSummaries a list of minion summaries of the minions involved in the given Action
     * @return a call with the impacted minions
     */
    public LocalCall<?> prepareStagingTargets(List<MinionSummary> minionSummaries) {
        return null;
    }

    /**
     * cleanup old reboot actions
     * @param bootTime date of boot time
     * @param sa server action connected to this action
     * @return true if should clean up old reboot actions
     * */
    public boolean shouldCleanupAction(Date bootTime, ServerAction sa) {
        return false;
    }

    /**
     * Check if an action can be scheduled anyway at given date for given systems.
     * @return true if an action can be scheduled anyway
     */
    public boolean canBeScheduledAnyway() {
        return false;
    }

    /**
     * Returns the pkg_parameter parameter to the schedule_action queries in Action_queries.xml
     * @return a parameter value
     */
    public String getPackageParameter() {
        return "";
    }

    /**
     * sets the "dry run" attribute to a servlet request
     * @param request servlet request where to set the attribute
     * @return true if "dry run"
     */
    public boolean setRequestAttributeDryRun(HttpServletRequest request) {
        request.setAttribute("typeDistUpgradeDryRun", false);
        return false;
    }

    /**
     * sets the "playbook" attribute to a servlet request
     * @param request      servlet request where to set the attribute
     * @param serverAction the server action to get a formatted list of inventory systems accessible to the user
     * @param user         the current user
     */
    public void setRequestAttributePlaybook(HttpServletRequest request, ServerAction serverAction, User user) {
        request.setAttribute("typePlaybook", false);
    }

    /**
     * sets the "packages" attribute to a servlet request
     * @param request servlet request where to set the attribute
     */
    public void setRequestAttributeTypePackages(HttpServletRequest request) {
        //default does nothing
    }

    /**
     * checks if client execution of action returns yaml formatted info
     * @return true if client execution of action returns yaml formatted info
     */
    public boolean clientExecutionReturnsYamlFormat() {
        return false; //default
    }

}

