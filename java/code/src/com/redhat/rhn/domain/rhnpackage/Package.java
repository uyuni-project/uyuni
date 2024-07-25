/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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

import com.redhat.rhn.common.util.StringUtil;
import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.common.Checksum;
import com.redhat.rhn.domain.errata.AdvisoryStatus;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.rpm.SourceRpm;
import com.redhat.rhn.frontend.xmlrpc.packages.PackageHelper;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Package
 */
public class Package extends BaseDomainHelper {

    private Long id;
    private String rpmVersion;
    private String description;
    private String summary;
    private Long packageSize;
    private Long payloadSize;
    private Long installedSize;
    private String buildHost;
    private Date buildTime;
    private Checksum checksum;
    private String vendor;
    private String payloadFormat;
    private Long compat;
    private String path;
    private String headerSignature;
    private String copyright;
    private Boolean isPtfPackage = false;
    private Boolean isPartOfPtfPackage = false;
    private String cookie;
    private Date lastModified;
    private Boolean lockPending = Boolean.FALSE;
    private Set<Errata> errata = new HashSet<>();
    private Set<Channel> channels = new HashSet<>();
    private Set<PackageFile> packageFiles = new HashSet<>();

    private Org org;
    private PackageName packageName;
    private PackageEvr packageEvr;
    private PackageGroup packageGroup;
    private SourceRpm sourceRpm;
    private PackageArch packageArch;
    private Set<PackageKey> packageKeys = new HashSet();

    private Long headerStart = 0L;
    private Long headerEnd = 0L;

    private Set<PackageProvides> provides = new HashSet();
    private Set<PackageRequires> requires = new HashSet();
    private Set<PackageObsoletes> obsoletes = new HashSet();
    private Set<PackageConflicts> conflicts = new HashSet();
    private Set<PackageRecommends> recommends = new HashSet();
    private Set<PackageSuggests> suggests = new HashSet();
    private Set<PackageSupplements> supplements = new HashSet();
    private Set<PackageEnhances> enhances = new HashSet();
    private Set<PackagePreDepends> preDepends = new HashSet();
    private Set<PackageBreaks> breaks = new HashSet();

    private Map<PackageExtraTagsKeys, String> extraTags = new HashMap<>();

    /**
     * @param lockPendingIn Set pending status. Default is False.
     */
    public void setLockPending(Boolean lockPendingIn) {
        this.lockPending = lockPendingIn;
    }

    /**
     * @return Returns the status of being locked.
     */
    public Boolean isLockPending() {
        return lockPending;
    }

    /** Check if the package is part of a retracted patch
     *
     * @return true if the package is part of a retracted patch
     */
    public Boolean isPartOfRetractedPatch() {
        return errata.stream().anyMatch(e -> e.getAdvisoryStatus() == AdvisoryStatus.RETRACTED);
    }

    /**
     * Check if this package is the main one of a PTF.
     *
     * @return true if the package is PTF master package
     */
    public boolean isMasterPtfPackage() {
        return provides.stream().anyMatch(p -> SpecialCapabilityNames.PTF.equals(p.getCapability().getName()));
    }

    /**
     * Check if the package is part of a PTF.
     *
     * @return true if the package is part of PTF
     */
    public boolean isPartOfPtf() {
        return provides.stream().anyMatch(p -> SpecialCapabilityNames.PTF_PACKAGE.equals(p.getCapability().getName()));
    }

    /**
     * @return Returns the provides.
     */
    public Set<PackageProvides> getProvides() {
        return provides;
    }

    /**
     * @param providesIn The provides to set.
     */
    public void setProvides(Set<PackageProvides> providesIn) {
        this.provides = providesIn;
    }

    /**
     * Retrieves the file portion of the path. For example, if
     * path=/foo/bar/baz.rpm, getFile() would return 'baz.rpm'.
     * @return Returns the file portion of the path.
     */
    public String getFile() {
       return PackageHelper.getPackageFileFromPath(getPath());
    }

    /**
     * @return Returns the buildHost.
     */
    public String getBuildHost() {
        return buildHost;
    }

    /**
     * @param b The buildHost to set.
     */
    public void setBuildHost(String b) {
        this.buildHost = b;
    }

    /**
     * @return Returns the buildTime.
     */
    public Date getBuildTime() {
        return buildTime;
    }

    /**
     * @param b The buildTime to set.
     */
    public void setBuildTime(Date b) {
        this.buildTime = b;
    }

    /**
     * @return Returns the compat.
     */
    public Long getCompat() {
        return compat;
    }

    /**
     * @param c The compat to set.
     */
    public void setCompat(Long c) {
        this.compat = c;
    }

    /**
     * @return Returns the cookie.
     */
    public String getCookie() {
        return cookie;
    }

    /**
     * @param c The cookie to set.
     */
    public void setCookie(String c) {
        this.cookie = c;
    }

    /**
     * @return Returns the copyright.
     */
    public String getCopyright() {
        return copyright;
    }

    /**
     * @param c The copyright to set.
     */
    public void setCopyright(String c) {
        this.copyright = c;
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param d The description to set.
     */
    public void setDescription(String d) {
        this.description = d;
    }

    /**
     * @return Returns the headerSignature.
     */
    public String getHeaderSignature() {
        return headerSignature;
    }

    /**
     * @param h The headerSig to set.
     */
    public void setHeaderSignature(String h) {
        this.headerSignature = h;
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
    public void setId(Long i) {
        this.id = i;
    }

    /**
     * @return Returns the isPtfPackage.
     */
    public Boolean getIsPtfPackage() {
        return isPtfPackage;
    }

    /**
     * @param p The isPtfPackage to set.
     */
    public void setIsPtfPackage(Boolean p) {
        this.isPtfPackage = p;
    }

    /**
     * @return Returns the isPartOfPtfPackage.
     */
    public Boolean getIsPartOfPtfPackage() {
        return isPartOfPtfPackage;
    }

    /**
     * @param p The isPartOfPtfPackage to set.
     */
    public void setIsPartOfPtfPackage(Boolean p) {
        this.isPartOfPtfPackage = p;
    }

    /**
     * @return Returns the lastModified.
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * @param l The lastModified to set.
     */
    public void setLastModified(Date l) {
        this.lastModified = l;
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

    /**
     * @return Returns the org.
     */
    public Org getOrg() {
        return org;
    }

    /**
     * @param o The org to set.
     */
    public void setOrg(Org o) {
        this.org = o;
    }

    /**
     * @return Returns the packageArch.
     */
    public PackageArch getPackageArch() {
        return packageArch;
    }

    /**
     * @param p The packageArch to set.
     */
    public void setPackageArch(PackageArch p) {
        this.packageArch = p;
    }

    /**
     * @return Returns the packageEvr.
     */
    public PackageEvr getPackageEvr() {
        return packageEvr;
    }

    /**
     * @param p The packageEvr to set.
     */
    public void setPackageEvr(PackageEvr p) {
        this.packageEvr = p;
    }

    /**
     * @return Returns the packageGroup.
     */
    public PackageGroup getPackageGroup() {
        return packageGroup;
    }

    /**
     * @param p The packageGroup to set.
     */
    public void setPackageGroup(PackageGroup p) {
        this.packageGroup = p;
    }

    /**
     * @return Returns the packageName.
     */
    public PackageName getPackageName() {
        return packageName;
    }

    /**
     * @param p The packageName to set.
     */
    public void setPackageName(PackageName p) {
        this.packageName = p;
    }

    /**
     * @return Returns the packageSize.
     */
    public Long getPackageSize() {
        return packageSize;
    }

    /**
     * Get a display friendly version of the size
     * @return the size
     */
    public String getPackageSizeString() {
        return StringUtil.displayFileSize(this.getPackageSize());
    }

    /**
     * @param p The packageSize to set.
     */
    public void setPackageSize(Long p) {
        this.packageSize = p;
    }

    /**
     * @return Returns the path.
     */
    public String getPath() {
        return path;
    }

    /**
     * @param p The path to set.
     */
    public void setPath(String p) {
        this.path = p;
    }

    /**
     * @return Returns the payloadFormat.
     */
    public String getPayloadFormat() {
        return payloadFormat;
    }

    /**
     * @param p The payloadFormat to set.
     */
    public void setPayloadFormat(String p) {
        this.payloadFormat = p;
    }

    /**
     * @return Returns the payloadSize.
     */
    public Long getPayloadSize() {
        return payloadSize;
    }

    /**
     * Get a display friendly version of the payload size
     * @return the size
     */
    public String getPayloadSizeString() {
        return StringUtil.displayFileSize(this.getPayloadSize());
    }

    /**
     * @param p The payloadSize to set.
     */
    public void setPayloadSize(Long p) {
        this.payloadSize = p;
    }

    /**
     * @return Returns the installedSize.
     */
    public Long getInstalledSize() {
        return installedSize;
    }

    /**
     * Get a display friendly version of the installed size
     * @return the size
     */
    public String getInstalledSizeString() {
        return StringUtil.displayFileSize(this.getInstalledSize());
    }

    /**
     * @param p The installedSize to set.
     */
    public void setInstalledSize(Long p) {
        this.installedSize = p;
    }

    /**
     * @return Returns the rpmVersion.
     */
    public String getRpmVersion() {
        return rpmVersion;
    }

    /**
     * @param r The rpmVersion to set.
     */
    public void setRpmVersion(String r) {
        this.rpmVersion = r;
    }

    /**
     * @return Returns the sourceRpm.
     */
    public SourceRpm getSourceRpm() {
        return sourceRpm;
    }

    /**
     * @param s The sourceRpm to set.
     */
    public void setSourceRpm(SourceRpm s) {
        this.sourceRpm = s;
    }

    /**
     * @return Returns the summary.
     */
    public String getSummary() {
        return summary;
    }

    /**
     * @param s The summary to set.
     */
    public void setSummary(String s) {
        this.summary = s;
    }

    /**
     * @return Returns the vendor.
     */
    public String getVendor() {
        return vendor;
    }

    /**
     * @param v The vendor to set.
     */
    public void setVendor(String v) {
        this.vendor = v;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", getId()).append("packageName",
                getPackageName()).toString();
    }

    /**
     * Util to output package name + evr: krb5-devel-1.3.4-47
     * @return String name and evr
     */
    public String getNameEvr() {
        return this.getPackageName().getName() + "-" + this.getPackageEvr().toString();
    }

    /**
     * Util to output package name + evr: krb5-devel-1.3.4-47.i386
     * @return String name and evra
     */
    public String getNameEvra() {
        return this.getPackageName().getName() + "-" + this.getPackageEvr().toString() +
                "." + this.getPackageArch().getLabel();
    }

    /**
     * Util to output package name + evr.
     * As opposed to {@link Package#getNameEvra()}, the output of this method always includes an epoch value, which is
     * 0 by default.
     * @return the package name and evra
     */
    public String getNevraWithEpoch() {
        PackageEvr evr = new PackageEvr(this.getPackageEvr());
        if (evr.getEpoch() == null || evr.getEpoch().isEmpty()) {
            evr.setEpoch("0");
        }
        return this.getPackageName().getName() + "-" + evr.toString() + "." + this.getPackageArch().getLabel();
    }

    /**
     * Util to output package nvrea: vim-enhanced-7.0.109-7.2.el5:2.x86_64
     * @return nvrea string
     */
    public String getNvrea() {
        PackageEvr evr = this.getPackageEvr();
        String nvrea = this.getPackageName().getName() + "-" + evr.getVersion() + "-" +
            evr.getRelease();
        if (evr.getEpoch() != null) {
            nvrea += ":" + evr.getEpoch();
        }
        nvrea += "." + this.getPackageArch().getLabel();
        return nvrea;
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof Package) {
            Package otherPack = (Package) other;
            return new EqualsBuilder().append(this.getId(), otherPack.getId())
                    .append(this.getPackageName(), otherPack.getPackageName())
                    .append(this.getPackageArch(), otherPack.getPackageArch())
                    .append(this.getPackageEvr(), otherPack.getPackageEvr()).isEquals();
        }
        return false;
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.getId()).append(this.getPackageName())
                .append(this.getPackageArch()).append(this.getPackageEvr()).toHashCode();
    }

    /**
     * @return Returns the package keys.
     */
    public Set<PackageKey> getPackageKeys() {
        return packageKeys;
    }

    /**
     * @param keys The keys to set.
     */
    public void setPackageKeys(Set<PackageKey> keys) {
        this.packageKeys = keys;
    }

    /**
     * @return Returns the errata.
     */
    public Set<Errata> getErrata() {
        return errata;
    }

    /**
     * @param errataIn The errata to set.
     */
    public void setErrata(Set<Errata> errataIn) {
        this.errata = errataIn;
    }

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
     * @return Returns the packageFiles.
     */
    public Set<PackageFile> getPackageFiles() {
        return packageFiles;
    }

    /**
     * @param packageFilesIn The packageFiles to set.
     */
    public void setPackageFiles(Set<PackageFile> packageFilesIn) {
        this.packageFiles = packageFilesIn;
    }

    /**
     * @return Returns the requires.
     */
    public Set<PackageRequires> getRequires() {
        return requires;
    }

    /**
     * @param requiresIn The requires to set.
     */
    public void setRequires(Set<PackageRequires> requiresIn) {
        this.requires = requiresIn;
    }

    /**
     * @return Returns the obsoletes.
     */
    public Set<PackageObsoletes> getObsoletes() {
        return obsoletes;
    }

    /**
     * @param obsoletesIn The obsoletes to set.
     */
    public void setObsoletes(Set<PackageObsoletes> obsoletesIn) {
        this.obsoletes = obsoletesIn;
    }

    /**
     * @return Returns the conflicts.
     */
    public Set<PackageConflicts> getConflicts() {
        return conflicts;
    }

    /**
     * @param conflictsIn The conflicts to set.
     */
    public void setConflicts(Set<PackageConflicts> conflictsIn) {
        this.conflicts = conflictsIn;
    }

    /**
     * @return Returns the recommends.
     */
    public Set<PackageRecommends> getRecommends() {
        return recommends;
    }

    /**
     * @param recommendsIn The recommends to set.
     */
    public void setRecommends(Set<PackageRecommends> recommendsIn) {
        this.recommends = recommendsIn;
    }

    /**
     * @return Returns the suggests.
     */
    public Set<PackageSuggests> getSuggests() {
        return suggests;
    }

    /**
     * @param suggestsIn The recommends to set.
     */
    public void setSuggests(Set<PackageSuggests> suggestsIn) {
        this.suggests = suggestsIn;
    }

    /**
     * @return supplements to get
     */
    public Set<PackageSupplements> getSupplements() {
        return supplements;
    }

    public void setSupplements(Set<PackageSupplements> supplementsIn) {
        this.supplements = supplementsIn;
    }

    /**
     * @return enhances to get
     */
    public Set<PackageEnhances> getEnhances() {
        return enhances;
    }

    public void setEnhances(Set<PackageEnhances> enhancesIn) {
        this.enhances = enhancesIn;
    }

    /**
     * @return preDepends to get
     */
    public Set<PackagePreDepends> getPreDepends() {
        return preDepends;
    }

    public void setPreDepends(Set<PackagePreDepends> preDependsIn) {
        this.preDepends = preDependsIn;
    }

    /**
     * @return breaks to get
     */
    public Set<PackageBreaks> getBreaks() {
        return breaks;
    }

    public void setBreaks(Set<PackageBreaks> breaksIn) {
        this.breaks = breaksIn;
    }

    /**
     * @return Returns the headerStart.
     */
    public Long getHeaderStart() {
        return headerStart;
    }

    /**
     * @param headerStartIn The headerStart to set.
     */
    public void setHeaderStart(Long headerStartIn) {
        this.headerStart = headerStartIn;
    }

    /**
     * @return Returns the headerEnd.
     */
    public Long getHeaderEnd() {
        return headerEnd;
    }

    /**
     * @param headerEndIn The headerEnd to set.
     */
    public void setHeaderEnd(Long headerEndIn) {
        this.headerEnd = headerEndIn;
    }

    /**
     * @return extraTags to get
     */
    public Map<PackageExtraTagsKeys, String> getExtraTags() {
        return extraTags;
    }

    /**
     * Get an rpm tag value for a specific key
     *
     * @param key the rpm tag key
     * @return the rpm tag value
     */
    public String getExtraTag(String key) {
        PackageExtraTagsKeys headerKey = new PackageExtraTagsKeys();
        headerKey.setName(key);
        return this.extraTags.get(headerKey);
    }

    /**
     * @param extraTagsIn to set
     */
    public void setExtraTags(Map<PackageExtraTagsKeys, String> extraTagsIn) {
        this.extraTags = extraTagsIn;
    }

    /**
     * @return Returns the pkgFile.
     */
    public String getFilename() {
        String pkgFile = getFile();
        if (pkgFile == null) {
            StringBuilder buf = new StringBuilder();
            buf.append(getPackageName().getName());
            buf.append("-");
            buf.append(getPackageEvr().getVersion());
            buf.append("-");
            buf.append(getPackageEvr().getRelease());
            buf.append(".");
            if (getPackageEvr().getEpoch() != null) {
                buf.append(getPackageEvr().getEpoch() + ".");
            }
            buf.append(getPackageArch().getLabel());
            buf.append(".");
            buf.append(getPackageArch().getArchType().getLabel());
            pkgFile = buf.toString();
        }
        return pkgFile;
    }


    public PackageType getPackageType() {
        return getPackageEvr().getPackageType();
    }

    /**
     * @return whether the package is an .rpm package or not
     */
    public boolean isTypeRpm() {
        return getPackageType() == PackageType.RPM;
    }

    /**
     * @return whether the package is a .deb package or not
     */
    public boolean isTypeDeb() {
        return getPackageType() == PackageType.DEB;
    }

}
