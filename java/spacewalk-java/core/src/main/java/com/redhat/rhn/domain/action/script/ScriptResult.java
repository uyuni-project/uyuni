/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.domain.action.script;

import com.redhat.rhn.common.hibernate.HibernateFactory;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * ScriptResult
 */
@Entity
@Table(name = "rhnServerActionScriptResult")
public class ScriptResult implements Serializable {

    @Embeddable
    public static class ScriptResultId implements Serializable {
        @Column(name = "server_id")
        private Long serverId;

        @Column(name = "action_script_id")
        private Long actionScriptId;

        /**
         * Default Constructor.
         */
        public ScriptResultId() {
        }

        /**
         * Constructor with argument.
         * @param serverIdIn The serverId to set.
         * @param actionScriptIdIn The actionScriptId to set.
         */
        public ScriptResultId(Long serverIdIn, Long actionScriptIdIn) {
            this.serverId = serverIdIn;
            this.actionScriptId = actionScriptIdIn;
        }

        /**
         * @return Returns the serverId.
         */
        public Long getServerId() {
            return serverId;
        }

        /**
         * @param serverIdIn The serverId to set.
         */
        public void setServerId(Long serverIdIn) {
            this.serverId = serverIdIn;
        }

        /**
         * @return Returns the actionScriptId.
         */
        public Long getActionScriptId() {
            return actionScriptId;
        }

        /**
         * @param actionScriptIdIn The actionScriptId to set.
         */
        public void setActionScriptId(Long actionScriptIdIn) {
            this.actionScriptId = actionScriptIdIn;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ScriptResultId that = (ScriptResultId) o;
            return serverId.equals(that.serverId) && actionScriptId.equals(that.actionScriptId);
        }

        @Override
        public int hashCode() {
            return serverId.hashCode() + actionScriptId.hashCode();
        }
    }

    @EmbeddedId
    private ScriptResultId id;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "stop_date")
    private Date stopDate;

    @Column(name = "return_code")
    private Long returnCode;

    @Column(name = "output", columnDefinition = "bytea")
    private byte[] output;

    @ManyToOne
    @JoinColumn(name = "action_script_id", insertable = false, updatable = false, nullable = false)
    private ScriptActionDetails parentScriptActionDetails;

    /**
     * Default Constructor.
     */
    public ScriptResult() {
        this.id = new ScriptResultId();
    }

    /**
     * @return Returns the id.
     */
    public ScriptResultId getId() {
        return id;
    }

    /**
     * @param idIn The id to set.
     */
    public void setId(ScriptResultId idIn) {
        this.id = idIn;
    }


    /**
     * @return Returns the serverId.
     */
    public Long getServerId() {
        return this.getId().getServerId();
    }

    /**
     * @param s The serverId to set.
     */
    public void setServerId(Long s) {
        this.getId().setServerId(s);
    }

    /**
     * @return Returns the actionScriptId.
     */
    public Long getActionScriptId() {
        return this.getId().getActionScriptId();
    }

    /**
     * @param a The actionScriptId to set.
     */
    public void setActionScriptId(Long a) {
        this.getId().setActionScriptId(a);
    }

    /**
     * @return Returns the startDate.
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * @param s The startDate to set.
     */
    public void setStartDate(Date s) {
        this.startDate = s;
    }

    /**
     * @return Returns the stopDate.
     */
    public Date getStopDate() {
        return stopDate;
    }

    /**
     * @param s The stopDate to set.
     */
    public void setStopDate(Date s) {
        this.stopDate = s;
    }

    /**
     * @return Returns the returnCode.
     */
    public Long getReturnCode() {
        return returnCode;
    }

    /**
     * @param r The returnCode to set.
     */
    public void setReturnCode(Long r) {
        this.returnCode = r;
    }

    /**
     * Get the parent of this object.
     *
     * @return Returns the parentScriptActionDetails.
     */
    public ScriptActionDetails getParentScriptActionDetails() {
        return parentScriptActionDetails;
    }

    /**
     * Set the parent of this object.
     *
     * @param parentScriptActionDetailsIn The parentScriptActionDetails to set.
     */
    public void setParentScriptActionDetails(
            ScriptActionDetails parentScriptActionDetailsIn) {
        this.parentScriptActionDetails = parentScriptActionDetailsIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ScriptResult r)) {
            return false;
        }
        return new EqualsBuilder().append(this.getActionScriptId(), r.getActionScriptId())
                                  .append(this.getServerId(), r.getServerId())
                                  .append(this.getStartDate(), r.getStartDate())
                                  .append(this.getStopDate(), r.getStopDate())
                                  .append(this.getReturnCode(), r.getReturnCode())
                                  .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getActionScriptId())
                                    .append(getServerId())
                                    .append(getStartDate())
                                    .append(getStopDate())
                                    .append(getReturnCode())
                                    .toHashCode();
    }

    /**
     * Get the output.
     *
     * @return Returns the output.
     */
    public byte[] getOutput() {
        return output;
    }

    /**
     * set the output
     * @param outputIn the output
     */
    public void setOutput(byte[] outputIn) {
        this.output = outputIn;
    }

    /**
     * Get the String version of the Script contents
     * @return String version of the Script contents
     */
    public String getOutputContents() {
        return HibernateFactory.getByteArrayContents(getOutput());
    }

}
