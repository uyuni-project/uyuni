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
package com.redhat.rhn.domain.action.kickstart;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.common.FileList;


import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

/**
 *
 * BaseKickstartActionDetails
 */
@MappedSuperclass
public abstract class BaseKickstartActionDetails extends BaseDomainHelper {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RHN_ACTIONKS_ID_SEQ")
    @SequenceGenerator(name = "RHN_ACTIONKS_ID_SEQ", sequenceName = "RHN_ACTIONKS_ID_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "cobbler_system_name")
    private String cobblerSystemName;

    @Column(name = "append_string")
    private String appendString;

    @Column(name = "kickstart_host")
    private String kickstartHost;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "rhnActionKickstartFileList",
            joinColumns = @JoinColumn(name = "action_ks_id"),
            inverseJoinColumns = @JoinColumn(name = "file_list_id"))
    private Set<FileList> fileLists;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id", updatable = false, nullable = false, insertable = true)
    private Action parentAction;

    /**
     * This is the PK for this object.  Its not the
     * actual ID to a KickstartData object
     * @return Returns the id.
     */
    protected Long getId() {
        return id;
    }

    /**
     * This is the PK for this object.  Its not the
     * actual ID to a KickstartData object.  Making
     * this protected because nobody should really touch this.
     * @param i The id to set.
     */
    protected void setId(Long i) {
        this.id = i;
    }


    /**
     * @return Returns the appendString.
     */
    public String getAppendString() {
        return appendString;
    }

    /**
     * @param a The appendString to set.
     */
    public void setAppendString(String a) {
        this.appendString = a;
    }


    /**
     * @return the kickstartHost
     */
    public String getKickstartHost() {
        return kickstartHost;
    }


    /**
     * @param kickstartHostIn the kickstartHost to set
     */
    public void setKickstartHost(String kickstartHostIn) {
        this.kickstartHost = kickstartHostIn;
    }


    /**
     * @return Returns the cobblerSystemName.
     */
    public String getCobblerSystemName() {
        return cobblerSystemName;
    }


    /**
     * @param cobblerSystemNameIn The cobblerSystemName to set.
     */
    public void setCobblerSystemName(String cobblerSystemNameIn) {
        this.cobblerSystemName = cobblerSystemNameIn;
    }

    /**
     * Adds a FileList object to fileLists.
     * @param f FileList to add
     */
    public void addFileList(FileList f) {
        if (fileLists == null) {
            fileLists = new HashSet<>();
        }
        fileLists.add(f);
    }

    /**
     * @return Returns the fileLists.
     */
    public Set<FileList> getFileLists() {
        return fileLists;
    }

    /**
     * @param f The fileLists to set.
     */
    public void setFileLists(Set<FileList> f) {
        this.fileLists = f;
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
