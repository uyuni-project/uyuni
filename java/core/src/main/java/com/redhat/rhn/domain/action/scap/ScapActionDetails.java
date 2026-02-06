/*
 * Copyright (c) 2025 SUSE LLC
 * Copyright (c) 2012 Red Hat, Inc.
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
package com.redhat.rhn.domain.action.scap;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.action.Action;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * ScapActionDetails
 */
@Entity
@Table(name = "rhnActionScap")
public class ScapActionDetails extends BaseDomainHelper {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RHN_ACT_SCAP_ID_SEQ")
    @SequenceGenerator(name = "RHN_ACT_SCAP_ID_SEQ", sequenceName = "RHN_ACT_SCAP_ID_SEQ", allocationSize = 1)
    private Long id;

    @Column
    private String path;

    @Column
    private String ovalfiles;

    @Column
    private byte[] parameters;

    @Column(name = "scap_policy_id")
    private Integer scapPolicyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id", updatable = false, nullable = false, insertable = true)
    private Action parentAction;

    /**
     * Default constructor.
     */
    public ScapActionDetails() {
        super();
    }

    /**
     * ScapActionDetails constructor.
     * @param pathIn New setting for the path.
     * @param parametersIn New setting for the parameters.
     * @param ovalFilesIn New setting for the OVAL files.
     */
    public ScapActionDetails(String pathIn, String parametersIn, String ovalFilesIn) {
        super();
        this.setPath(pathIn);
        this.setParameters(parametersIn);
        this.setOvalfiles(ovalFilesIn);
    }

    /**
     * Set the path to the main scap content.
     * @param pathIn New setting for the path.
     */
    public void setPath(String pathIn) {
        path = pathIn;
    }

    /**
     * Get the path to the scap content.
     * @return The path settings.
     */
    public String getPath() {
        return path;
    }

    /**
     * Set the paths to OVAL files.
     * @param ovalFilesIn New setting for the ovalFiles.
     */
    public void setOvalfiles(String ovalFilesIn) {
        ovalfiles = ovalFilesIn;
    }

    /**
     * Get the paths to OVAL files.
     * @return The ovalFiles settings.
     */
    public String getOvalfiles() {
        return ovalfiles;
    }

    /**
     * Set the additional parameters for the oscap tool.
     * @param parametersIn New setting for the parameters.
     */
    public void setParameters(String parametersIn) {
        parameters = HibernateFactory.stringToByteArray(parametersIn);
    }

    /**
     * Set the additional parameters for the oscap tool.
     * @param parametersIn New setting for the parameters.
     */
    public void setParameters(byte[] parametersIn) {
        parameters = parametersIn;
    }

    /**
     * Get the parameters for the oscap tool.
     * @return The parameters for oscap tool.
     */
    public byte[] getParameters() {
        return parameters;
    }

    /**
     * Get the parameters for the oscap tool.
     * @return The parameters for oscap tool.
     */
    public String getParametersContents() {
        return HibernateFactory.getByteArrayContents(parameters);
    }

    /**
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }

    /**
     * @param i The id to set.
     */
    protected void setId(Long i) {
        this.id = i;
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

    /**
     * Gets the SCAP policy ID associated with this action
     * @return Returns the scapPolicyId.
     */
    public Integer getScapPolicyId() {
        return scapPolicyId;
    }

    /**
     * Sets the SCAP policy ID associated with this action
     * @param scapPolicyIdIn The scapPolicyId to set.
     */
    public void setScapPolicyId(Integer scapPolicyIdIn) {
        this.scapPolicyId = scapPolicyIdIn;
    }
}
