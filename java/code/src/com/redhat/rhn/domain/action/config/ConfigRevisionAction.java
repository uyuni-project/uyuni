/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.domain.action.config;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.redhat.rhn.domain.server.Server;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * ConfigRevisionAction - Class representation of the table rhnActionConfigRevision.
 *
 */
@Entity
@Table(name = "rhnActionConfigRevision")
public class ConfigRevisionAction extends BaseDomainHelper {

    @Id
    @GeneratedValue(generator = "actioncr_seq")
    @GenericGenerator(
        name = "actioncr_seq",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
                @Parameter(name = "sequence_name", value = "rhn_actioncr_id_seq"),
                @Parameter(name = "increment_size", value = "1")
        })
    private Long id;

    @Column(name = "failure_id")
    private Long failureId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id")
    private Server server;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "config_revision_id")
    private ConfigRevision configRevision;

    @OneToOne(mappedBy = "configRevisionAction", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private ConfigRevisionActionResult configRevisionActionResult;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id", updatable = false, nullable = false)
    private Action parentAction;

    /**
     * Get the id
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }

    /**
     * Set the id
     * @param idIn The id to set.
     */
    protected void setId(Long idIn) {
        this.id = idIn;
    }
    /**
     * Getter for failureId
     * @return Long to get
    */
    public Long getFailureId() {
        return this.failureId;
    }

    /**
     * Setter for failureId
     * @param failureIdIn to set
    */
    public void setFailureId(Long failureIdIn) {
        this.failureId = failureIdIn;
    }

    /**
     * Get the server object
     * @return Returns the server.
     */
    public Server getServer() {
        return server;
    }

    /**
     * Set the server object
     * @param serverIn The server to set.
     */
    public void setServer(Server serverIn) {
        this.server = serverIn;
    }

    /**
     * Get the configRevision object
     * @return ConfigRevision the configRevision.
     */
    public ConfigRevision getConfigRevision() {
        return configRevision;
    }

    /**
     * Set the configRevision object
     * @param configRevisionIn The configRevision to set.
     */
    public void setConfigRevision(ConfigRevision configRevisionIn) {
        this.configRevision = configRevisionIn;
    }

    /**
     * Get the ConfigRevisionActionResult
     * @return Returns the configRevisionActionResult.
     */
    public ConfigRevisionActionResult getConfigRevisionActionResult() {
        return configRevisionActionResult;
    }
    /**
     * Set the ConfigRevisionActionResult
     * @param configRevisionActionResultIn The configRevisionActionResult to set.
     */
    public void setConfigRevisionActionResult(
            ConfigRevisionActionResult configRevisionActionResultIn) {
        this.configRevisionActionResult = configRevisionActionResultIn;
    }

    /**
     * Gets the parent Action associated with this ServerAction record
     * @return Returns the parentAction.
     */
    public Action getParentAction() {
        return parentAction;
    }

    /**
     * Sets the parent Action associated with this ServerAction record
     * @param parentActionIn The parentAction to set.
     */
    public void setParentAction(Action parentActionIn) {
        this.parentAction = parentActionIn;
    }

}
