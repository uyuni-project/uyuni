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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.errata;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.common.Checksum;
import com.redhat.rhn.domain.rhnpackage.Package;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * ErrataFile
 */
@Entity
@Table(name = "rhnErrataFile")
public class ErrataFile extends BaseDomainHelper {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RHN_ERRATAFILE_ID_SEQ")
    @SequenceGenerator(name = "RHN_ERRATAFILE_ID_SEQ", sequenceName = "RHN_ERRATAFILE_ID_SEQ", allocationSize = 1)
    protected Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type")
    protected ErrataFileType fileType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checksum_id")
    protected Checksum checksum;

    @Column
    protected String fileName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "errata_id")
    protected Errata owningErrata;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "rhnErrataFileChannel",
            joinColumns = @JoinColumn(name = "errata_file_id"),
            inverseJoinColumns = @JoinColumn(name = "channel_id"))
    protected Set<Channel> channels;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "rhnErrataFilePackage",
            joinColumns = @JoinColumn(name = "errata_file_id"),
            inverseJoinColumns = @JoinColumn(name = "package_id"))
    @OrderBy("package_id asc")
    protected Set<Package> packages;

    /**
     * @return Returns the channels.
     */
    public Set<Channel> getChannels() {
        return channels;
    }

    /**
     * @param channelsIn The channels to set.
     */
    public void setChannels(Set<Channel> channelsIn) {
        this.channels = channelsIn;
    }

    /**
     * Add a Channel to this ErrataFile
     * @param c to add
     */
    public void addChannel(Channel c) {
        if (this.getChannels() == null) {
            this.channels = new HashSet<>();
        }
        this.channels.add(c);
    }

    /**
     * Id
     * @param idIn id
     */
    protected void setId(Long idIn) {
        id = idIn;
    }

    /**
     * Id
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * File type
     * @param ft file type
     */
    public void setFileType(ErrataFileType ft) {
        fileType = ft;
    }


    /**
     * File type
     * @return file type
     */
    public ErrataFileType getFileType() {
        return fileType;
    }

    /**
     * checksum
     * @param cs checksums
     */
    public void setChecksum(Checksum cs) {
        checksum = cs;
    }

    /**
     * checksum
     * @return checksum
     */
    public Checksum getChecksum() {
        return checksum;
    }

    /**
     * File name
     * @param name file name
     */
    public void setFileName(String name) {
        fileName = name;
    }

    /**
     * File name
     * @return file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @return Returns the owningErrata.
     */
    public Errata getOwningErrata() {
        return owningErrata;
    }

    /**
     * @param owningErrataIn The owningErrata to set.
     */
    public void setOwningErrata(Errata owningErrataIn) {
        this.owningErrata = owningErrataIn;
    }

    /**
     * @return Returns the packages for this errata file.
     */
    public Set<Package> getPackages() {
        return packages;
    }

    /**
     * @param packagesIn The packages to set.
     */
    public void setPackages(Set<Package> packagesIn) {
        this.packages = packagesIn;
    }

    /**
     * Add a Package to the ErrataFile
     * @param p package to add
     */
    public void addPackage(Package p) {
        if (this.packages == null) {
            this.packages = new HashSet<>();
        }
        this.packages.add(p);
    }

    /**
     * Returns whether this errata file has the given Package.
     * @param pkg the package
     * @return Returns true if this errata file has the given Package. Otherwise return false.
     */
    public boolean hasPackage(Package pkg) {
        return this.packages.contains(pkg);
    }
}
