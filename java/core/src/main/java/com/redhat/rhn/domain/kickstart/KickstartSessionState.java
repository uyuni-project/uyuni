/*
 * Copyright (c) 2025 SUSE LLC
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
package com.redhat.rhn.domain.kickstart;

import com.redhat.rhn.domain.BaseDomainHelper;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * KickstartSessionState - Class representation of the table rhnkickstartsessionstate.
 */
@Entity
@Table(name = "rhnkickstartsessionstate")
public class KickstartSessionState extends BaseDomainHelper {

    public static final String CREATED                 = "created";
    public static final String DEPLOYED                = "deployed";
    public static final String INJECTED                = "injected";
    public static final String RESTARTED               = "restarted";
    public static final String CONFIGURATION_ACCESSED  = "configuration_accessed";
    public static final String STARTED                 = "started";
    public static final String IN_PROGRESS             = "in_progress";
    public static final String REGISTERED              = "registered";
    public static final String PACKAGE_SYNCH           = "package_synch";
    public static final String PACKAGE_SYNCH_SCHEDULED = "package_synch_scheduled";
    public static final String CONFIGURATION_DEPLOY    = "configuration_deploy";
    public static final String COMPLETE                = "complete";
    public static final String FAILED                  = "failed";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RHN_KS_SESSION_STATE_ID_SEQ")
    @SequenceGenerator(
            name = "RHN_KS_SESSION_STATE_ID_SEQ", sequenceName = "RHN_KS_SESSION_STATE_ID_SEQ", allocationSize = 1
    )
    private Long id;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

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
    protected void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * Getter for label
     * @return String to get
    */
    public String getLabel() {
        return this.label;
    }

    /**
     * Setter for label
     * @param labelIn to set
    */
    public void setLabel(String labelIn) {
        this.label = labelIn;
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
        this.name = nameIn;
    }

    /**
     * Getter for description
     * @return String to get
    */
    public String getDescription() {
        return this.description;
    }

    /**
     * Setter for description
     * @param descriptionIn to set
    */
    public void setDescription(String descriptionIn) {
        this.description = descriptionIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof KickstartSessionState castOther)) {
            return false;
        }
        return new EqualsBuilder().append(this.getId(),
                castOther.getId()).append(this.getLabel(),
                castOther.getLabel()).isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.getId()).
            append(this.getLabel()).toHashCode();
    }

}
