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

package com.redhat.rhn.domain.rhnpackage;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.common.Checksum;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

/**
 * PackageArch
 */
@Entity
@Table(name = "rhnPackageFile", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"package_id", "capability_id"})
})
public class PackageFile extends BaseDomainHelper {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 8009150853428038205L;

    // Composite primary key
    @EmbeddedId
    private PackageFileId id;  // This will use the composite key class

    @Column(name = "package_id", insertable = false, updatable = false)
    private Package pack;

    @Column(name = "capability_id", insertable = false, updatable = false)
    private PackageCapability capability;

    @Column(name = "device")
    private Long device;

    @Column(name = "inode")
    private Long inode;

    @Column(name = "file_mode")
    private Long fileMode;

    @Column(name = "username")
    private String username;

    @Column(name = "groupname")
    private String groupname;

    @Column(name = "rdev")
    private Long rdev;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "mtime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date mtime;

    @ManyToOne
    @JoinColumn(name = "checksum_id")
    private Checksum checksum;

    @Column(name = "linkto")
    private String linkTo;

    @Column(name = "flags")
    private Long flags;

    @Column(name = "verifyflags")
    private Long verifyFlags;

    @Column(name = "lang")
    private String lang;

    /**
     * Default Constructor
     */
    public PackageFile() {
        this.id = new PackageFileId();
        this.pack = this.id.getPack();
        this.capability = this.id.getCapability();
    }

    /**
     * Constructor with parameters.
     * @param packIn package
     * @param capabilityIn package capability
     */
    public PackageFile(Package packIn, PackageCapability capabilityIn) {
        this.pack = packIn;
        this.capability = capabilityIn;
        this.id = new PackageFileId(packIn, capabilityIn);
    }

    /**
     * @return Id
     */
    public PackageFileId getId() {
        return id;
    }

    /**
     * @param idIn the Id to set.
     */
    public void setId(PackageFileId idIn) {
        id = idIn;
    }

    /**
     * @return Returns the pack.
     */
    public Package getPack() {
        return pack;
    }

    /**
     * @param packIn The pack to set.
     */
    public void setPack(Package packIn) {
        this.pack = packIn;
    }

    /**
     * @return Returns the capability.
     */
    public PackageCapability getCapability() {
        return capability;
    }

    /**
     * @param capabilityIn The capability to set.
     */
    public void setCapability(PackageCapability capabilityIn) {
        this.capability = capabilityIn;
    }

    /**
     * @return Returns the device.
     */
    public Long getDevice() {
        return device;
    }

    /**
     * @param deviceIn The device to set.
     */
    public void setDevice(Long deviceIn) {
        this.device = deviceIn;
    }

    /**
     * @return Returns the inode.
     */
    public Long getInode() {
        return inode;
    }

    /**
     * @param inodeIn The inode to set.
     */
    public void setInode(Long inodeIn) {
        this.inode = inodeIn;
    }

    /**
     * @return Returns the fileMode.
     */
    public Long getFileMode() {
        return fileMode;
    }

    /**
     * @param fileModeIn The fileMode to set.
     */
    public void setFileMode(Long fileModeIn) {
        this.fileMode = fileModeIn;
    }

    /**
     * @return Returns the username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param usernameIn The username to set.
     */
    public void setUsername(String usernameIn) {
        this.username = usernameIn;
    }

    /**
     * @return Returns the groupname.
     */
    public String getGroupname() {
        return groupname;
    }

    /**
     * @param groupnameIn The groupname to set.
     */
    public void setGroupname(String groupnameIn) {
        this.groupname = groupnameIn;
    }

    /**
     * @return Returns the rdev.
     */
    public Long getRdev() {
        return rdev;
    }

    /**
     * @param rdevIn The rdev to set.
     */
    public void setRdev(Long rdevIn) {
        this.rdev = rdevIn;
    }

    /**
     * @return Returns the fileSize.
     */
    public Long getFileSize() {
        return fileSize;
    }

    /**
     * @param fileSizeIn The fileSize to set.
     */
    public void setFileSize(Long fileSizeIn) {
        this.fileSize = fileSizeIn;
    }

    /**
     * @return Returns the mtime.
     */
    public Date getMtime() {
        return mtime;
    }

    /**
     * @param mtimeIn The mtime to set.
     */
    public void setMtime(Date mtimeIn) {
        this.mtime = mtimeIn;
    }


    /**
     * @return Returns the linkTo.
     */
    public String getLinkTo() {
        return linkTo;
    }

    /**
     * @param linkToIn The linkTo to set.
     */
    public void setLinkTo(String linkToIn) {
        this.linkTo = linkToIn;
    }

    /**
     * @return Returns the flags.
     */
    public Long getFlags() {
        return flags;
    }

    /**
     * @param flagsIn The flags to set.
     */
    public void setFlags(Long flagsIn) {
        this.flags = flagsIn;
    }

    /**
     * @return Returns the verifyFlags.
     */
    public Long getVerifyFlags() {
        return verifyFlags;
    }

    /**
     * @param verifyFlagsIn The verifyFlags to set.
     */
    public void setVerifyFlags(Long verifyFlagsIn) {
        this.verifyFlags = verifyFlagsIn;
    }

    /**
     * @return Returns the lang.
     */
    public String getLang() {
        return lang;
    }

    /**
     * @param langIn The lang to set.
     */
    public void setLang(String langIn) {
        this.lang = langIn;
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PackageFile fileIn)) {
            return false;
        }
        EqualsBuilder equals = new EqualsBuilder();
        equals.append(this.getPack(), fileIn.getPack());
        equals.append(this.getCapability(), fileIn.getCapability());
        return equals.isEquals();
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(this.getPack());
        hash.append(this.getCapability());
        return hash.toHashCode();
    }


    /**
     * @return Returns the checksum.
     */
    public Checksum getChecksum() {
        return checksum;
    }


    /**
     * @param checksumIn The checksum to set.
     */
    public void setChecksum(Checksum checksumIn) {
        this.checksum = checksumIn;
    }

}
