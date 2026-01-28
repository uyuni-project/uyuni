/*
 * Copyright (c) 2021 SUSE LLC
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

package com.redhat.rhn.domain.action.errata;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.action.Action;

import org.hibernate.type.YesNoConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * ActionPackageDetails
 *
 * Handles the vendor change flag for patching, upgrading, installing
 */
@Entity
@Table(name = "rhnActionPackageDetails")
public class ActionPackageDetails extends BaseDomainHelper {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "actiondpd_seq")
    @SequenceGenerator(name = "actiondpd_seq", sequenceName = "rhn_actiondpd_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "allow_vendor_change")
    @Convert(converter = YesNoConverter.class)
    private boolean allowVendorChange = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "action_id", nullable = false)
    private Action parentAction;

    /**
     * Constructor
     */
    public ActionPackageDetails() {  }

    /**
     * Constructor
     * @param parentActionIn Action
     * @param allowVendorChangeIn boolean
     */
     public ActionPackageDetails(Action parentActionIn, boolean allowVendorChangeIn) {
         this.setParentAction(parentActionIn);
         this.allowVendorChange = allowVendorChangeIn;
     }

    /**
     * Return the ID.
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * Set the ID.
     * @param idIn id
     */
    protected void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * @return Returns allow vendor change
     */
    public boolean getAllowVendorChange() {
        return allowVendorChange;
    }

    /**
     * @param allowVendorChangeIn Set allow vendor change flag.
     */
    public void setAllowVendorChange(boolean allowVendorChangeIn) {
        this.allowVendorChange = allowVendorChangeIn;
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
