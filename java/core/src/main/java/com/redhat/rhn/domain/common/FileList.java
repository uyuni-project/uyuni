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
package com.redhat.rhn.domain.common;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.Identifiable;
import com.redhat.rhn.domain.config.ConfigFileName;
import com.redhat.rhn.domain.config.ConfigurationFactory;
import com.redhat.rhn.domain.org.Org;


import java.util.Collection;
import java.util.LinkedList;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * FileList
 */
@Entity
@Table(name = "rhnFileList")
public class FileList extends BaseDomainHelper implements Identifiable {

    @Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rhn_filelist_seq")
	@SequenceGenerator(name = "rhn_filelist_seq", sequenceName = "RHN_FILELIST_ID_SEQ", allocationSize = 1)
    private Long id;

    @Column
    private String label;

    @ManyToOne
    @JoinColumn(name = "org_id")
    private Org org;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(
            name = "rhnFileListMembers",
            joinColumns = { @JoinColumn(name = "file_list_id") },
            inverseJoinColumns = { @JoinColumn(name = "config_file_name_id") }
    )
    private Collection<ConfigFileName> fileNames;

    /**
     * @return Returns the id.
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * @param i The id to set.
     */
    public void setId(Long i) {
        this.id = i;
    }

    /**
     * @return Returns the label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param l The label to set.
     */
    public void setLabel(String l) {
        this.label = l;
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
     * Add a filename to this list.
     * @param fileIn to add
     */
    public void addFileName(String fileIn) {
        if (this.fileNames == null) {
            this.fileNames = new LinkedList<>();
        }
        ConfigFileName cfn = ConfigurationFactory.lookupOrInsertConfigFileName(fileIn);
        if (!this.fileNames.contains(cfn)) {
            cfn.setPath(fileIn);
            this.fileNames.add(cfn);
        }
    }


    /**
     * Returns Set of ConfigFileName instances.
     * @return Returns the fileNames.
     */
    public Collection<ConfigFileName> getFileNames() {
        if (this.fileNames == null) {
            this.fileNames = new LinkedList<>();
        }
        return fileNames;
    }


    /**
     * @param fileNamesIn The fileNames to set.
     */
    public void setFileNames(Collection<ConfigFileName> fileNamesIn) {
        this.fileNames = fileNamesIn;
    }

}
