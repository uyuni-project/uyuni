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
package com.redhat.rhn.domain.errata;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.common.Checksum;
import com.redhat.rhn.domain.rhnpackage.Package;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * ErrataFile
 */
public class ErrataFile extends BaseDomainHelper {
    protected Long id;

    protected ErrataFileType fileType;

    protected Checksum checksum;

    protected String fileName;

    protected Errata owningErrata;

    protected Date created;

    protected Date modified;

    protected Set<Channel> channels;

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
    public void setId(Long idIn) {
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
     * Owning errata
     * @param errata owning errata
     */
    public void setErrata(Errata errata) {
        owningErrata = errata;
    }

    /**
     * Owning errata
     * @return owning errata
     */
    public Errata getErrata() {
        return owningErrata;
    }

    /**
     * Created
     * @param createdIn created
     */
    @Override
    public void setCreated(Date createdIn) {
        created = createdIn;
    }

    /**
     * Created
     * @return created
     */
    @Override
    public Date getCreated() {
        return created;
    }

    /**
     * Modified
     * @param mod modified
     */
    @Override
    public void setModified(Date mod) {
        modified = mod;
    }

    /**
     * Modified
     * @return modified
     */
    @Override
    public Date getModified() {
        return modified;
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
