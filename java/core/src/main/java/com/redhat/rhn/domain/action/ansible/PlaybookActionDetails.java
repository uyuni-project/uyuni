/*
 * Copyright (c) 2021--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.action.ansible;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.action.Action;

import org.hibernate.annotations.Type;

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
 * PlaybookActionDetails - Class representation of the table rhnActionPlaybook.
 */
@Entity
@Table(name = "rhnActionPlaybook")
public class PlaybookActionDetails extends BaseDomainHelper {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "act_playbook_seq")
    @SequenceGenerator(name = "act_playbook_seq", sequenceName = "rhn_act_playbook_id_seq", allocationSize = 1)
    private long id;

    @Column(name = "playbook_path")
    private String playbookPath;

    @Column(name = "inventory_path")
    private String inventoryPath;

    @Column(name = "test_mode")
    @Type(type = "yes_no")
    private boolean testMode;

    @Column(name = "flush_cache")
    @Type(type = "yes_no")
    private boolean flushCache;

    @Column(name = "extra_vars")
    private byte[] extraVars;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id", updatable = false, nullable = false)
    private Action parentAction;

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param idIn the id to set
     */
    protected void setId(long idIn) {
        this.id = idIn;
    }

    public String getPlaybookPath() {
        return playbookPath;
    }

    public void setPlaybookPath(String playbookPathIn) {
        this.playbookPath = playbookPathIn;
    }

    public String getInventoryPath() {
        return inventoryPath;
    }

    public void setInventoryPath(String inventoryPathIn) {
        this.inventoryPath = inventoryPathIn;
    }

    public boolean isTestMode() {
        return testMode;
    }

    public void setTestMode(boolean testModeIn) {
        this.testMode = testModeIn;
    }

    public boolean isFlushCache() {
        return flushCache;
    }

    public void setFlushCache(boolean flushCacheIn) {
        this.flushCache = flushCacheIn;
    }

    public byte[] getExtraVars() {
        return extraVars;
    }

    public void setExtraVars(byte[] extraVarsIn) {
        extraVars = extraVarsIn;
    }

    public String getExtraVarsContents() {
        return HibernateFactory.getByteArrayContents(getExtraVars());
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
