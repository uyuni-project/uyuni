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

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import java.util.Collection;
import java.util.LinkedList;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * FileList
 */
@Entity
@Table(name = "rhnFileList")
public class FileList extends BaseDomainHelper implements Identifiable {

    @Id
    @GeneratedValue(generator = "rhn_filelist_seq")
    @GenericGenerator(
            name = "rhn_filelist_seq",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "sequence_name", value = "RHN_FILELIST_ID_SEQ"),
                    @Parameter(name = "increment_size", value = "1")
            })
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
