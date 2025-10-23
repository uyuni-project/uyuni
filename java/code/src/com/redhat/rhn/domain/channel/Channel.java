/*
 * Copyright (c) 2025 SUSE LLC
 * Copyright (c) 2009--2018 Red Hat, Inc.
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
package com.redhat.rhn.domain.channel;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.common.ChecksumType;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.product.SUSEProductChannel;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.system.IncompatibleArchException;
import com.redhat.rhn.manager.system.SystemManager;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * Channel
 */
@Entity
@Table(name = "rhnChannel")
@Inheritance(strategy = InheritanceType.JOINED)
public class Channel extends BaseDomainHelper implements Comparable<Channel> {

    /**
     * Logger for this class
     */
    private static Logger log = LogManager.getLogger(Channel.class);
    public static final String PUBLIC = "public";
    public static final String PROTECTED = "protected";
    public static final String PRIVATE = "private";

    private static List<String> archesToSkipRepodata = new ArrayList<>(Arrays
            .asList("channel-sparc-sun-solaris", "channel-i386-sun-solaris",
                    "channel-sparc"));

    @Id
    @GeneratedValue(generator = "rhn_channel_seq")
    @GenericGenerator(
            name = "rhn_channel_seq",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "sequence_name", value = "RHN_CHANNEL_ID_SEQ"),
                    @Parameter(name = "increment_size", value = "1")
            })
    private Long id;

    @Column
    private String baseDir;

    @Column
    private String description;

    @Column(name = "end_of_life")
    private Date endOfLife;

    @Column(name = "gpg_check")
    @Type(type = "yes_no")
    private boolean GPGCheck;

    @Column(name = "gpg_key_url")
    private String GPGKeyUrl;

    @Column(name = "gpg_key_id")
    private String GPGKeyId;

    @Column(name = "gpg_key_fp")
    private String GPGKeyFp;

    @Column
    private String label;

    @Column(name = "last_modified")
    private Date lastModified;

    @Column(name = "last_synced")
    private Date lastSynced;

    @Column
    private String name;

    @Column
    private String summary;

    @Column(name = "channel_access")
    private String access;

    @Column(name = "maint_name")
    private String maintainerName;

    @Column(name = "maint_email")
    private String maintainerEmail;

    @Column(name = "maint_phone")
    private String maintainerPhone;

    @Column(name = "support_policy")
    private String supportPolicy;

    @Column(name = "update_tag")
    private String updateTag;

    @Column(name = "installer_updates")
    @Type(type = "yes_no")
    private boolean installerUpdates;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id")
    private Org org;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_arch_id")
    private ChannelArch channelArch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checksum_type_id")
    private ChecksumType checksumType;

    @OneToMany(mappedBy = "channel", fetch = FetchType.LAZY)
    private Set<DistChannelMap> distChannelMaps;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_channel")
    private Channel parentChannel;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(name = "rhnChannelFamilyMembers",
               joinColumns = @JoinColumn(name = "channel_id"),
               inverseJoinColumns = @JoinColumn(name = "channel_family_id"))
    private Set<ChannelFamily> channelFamilies;

    @OneToOne(mappedBy = "channel", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Comps comps;

    @OneToOne(mappedBy = "channel", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Modules modules;

    @OneToOne(mappedBy = "channel", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private MediaProducts mediaProducts;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinTable(name = "rhnChannelTrust",
            joinColumns = @JoinColumn(name = "channel_id"),
            inverseJoinColumns = @JoinColumn(name = "org_trust_id"))
    private Set<Org> trustedOrgs;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinTable(name = "rhnChannelErrata",
            joinColumns = @JoinColumn(name = "channel_id"),
            inverseJoinColumns = @JoinColumn(name = "errata_id"))
    private Set<Errata> erratas;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinTable(name = "rhnChannelPackage",
            joinColumns = @JoinColumn(name = "channel_id"),
            inverseJoinColumns = @JoinColumn(name = "package_id"))
    private Set<Package> packages;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinTable(name = "rhnChannelContentSource",
            joinColumns = @JoinColumn(name = "channel_id"),
            inverseJoinColumns = @JoinColumn(name = "source_id"))
    private Set<ContentSource> sources;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_product_id")
    private ChannelProduct product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_name_id")
    private ProductName productName;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_id")
    private Set<ClonedChannel> clonedChannels;

    @OneToMany(mappedBy = "channel", fetch = FetchType.LAZY)
    private Set<SUSEProductChannel> suseProductChannels;

    @OneToOne(mappedBy = "channel", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn(name = "channel_id")
    private ChannelSyncFlag channelSyncFlag;

    /**
     * Channel Object Constructor
     */
    public Channel() {
        suseProductChannels = new HashSet<>();
        clonedChannels = new HashSet<>();
        installerUpdates = false;
        trustedOrgs = new HashSet<>();
        distChannelMaps = new HashSet<>();
        channelFamilies = new HashSet<>();
        sources = new HashSet<>();
        packages = new HashSet<>();
        erratas = new HashSet<>();
        access = PRIVATE;
        GPGCheck = true;
        channelSyncFlag = new ChannelSyncFlag();
        channelSyncFlag.setChannel(this);
    }

    /**
     * @param orgIn what org you want to know if it is globally subscribable in
     * @return Returns whether or not this channel is globally subscribable.
     */
    public boolean isGloballySubscribable(Org orgIn) {
        return ChannelFactory.isGloballySubscribable(orgIn, this);
    }

    /**
     * Sets the globally subscribable attribute for this channel
     * @param orgIn what org you want to set if it is globally subscribable in
     * @param value True if you want the channel to be globally subscribable,
     * false if not.
     */
    public void setGloballySubscribable(boolean value, Org orgIn) {
        ChannelFactory.setGloballySubscribable(orgIn, this, value);
    }

    /**
     * Returns true if this Channel is a mgr server channel.
     * @return true if this Channel is a mgr server channel.
     */
    public boolean isMgrServer() {
        return getChannelFamily().getLabel().startsWith(
                ChannelFamilyFactory.SATELLITE_CHANNEL_FAMILY_LABEL);
    }

    /**
     * Returns true if this Channel is a Proxy channel.
     * @return true if this Channel is a Proxy channel.
     */
    public boolean isProxy() {
        ChannelFamily cfam = getChannelFamily();

        if (cfam != null) {
            return List.of(
                    ChannelFamilyFactory.PROXY_CHANNEL_FAMILY_LABEL,
                    ChannelFamilyFactory.PROXY_ARM_CHANNEL_FAMILY_LABEL
            ).contains(cfam.getLabel());
        }
        return false;
    }

    /**
     * Returns true if this Channel is a Vendor channel.
     * @return true if this Channel is a Vendor channel.
     */
    public boolean isVendorChannel() {
        return org == null;
    }

    /**
     * Returns true if this channel is of specified release (f.e. RHEL6)
     * @param ver release version
     * @return true if this channel is of specified release
     */
    public boolean isReleaseXChannel(Integer ver) {
        if (getDistChannelMaps() != null) {
            for (DistChannelMap map : getDistChannelMaps()) {
                if (map.getRelease().contains(ver.toString())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return Returns the baseDir.
     */
    public String getBaseDir() {
        return baseDir;
    }

    /**
     * @param b The baseDir to set.
     */
    public void setBaseDir(String b) {
        this.baseDir = b;
    }

    /**
     * @return Returns the channelArch.
     */
    public ChannelArch getChannelArch() {
        return channelArch;
    }

    /**
     * @param c The channelArch to set.
     */
    public void setChannelArch(ChannelArch c) {
        this.channelArch = c;
    }

    /**
     * @return Returns the channelChecksum.
     */
    public ChecksumType getChecksumType() {
        return checksumType;
    }

    /**
     * @param checksumTypeIn The checksum to set.
     */
    public void setChecksumType(ChecksumType checksumTypeIn) {
        this.checksumType = checksumTypeIn;
    }


    /**
     * @param compsIn The Comps to set.
     */
    public void setComps(Comps compsIn) {
        this.comps = compsIn;
    }

    /**
     * @return Returns the Comps.
     */
    public Comps getComps() {
        return comps;
    }

    /**
     * @param modulesIn The Modules to set.
     */
    public void setModules(Modules modulesIn) {
        this.modules = modulesIn;
    }

    /**
     * Sets modules data from given channel.
     *
     * @param from the Channel
     */
    public void cloneModulesFrom(Channel from) {
        ChannelFactory.cloneModulesMetadata(from, this);
    }

    /**
     * @return Returns the Modules.
     */
    public Modules getModules() {
        return modules;
    }

    public boolean isModular() {
        return modules != null;
    }

    /**
     * @param mediaProductsIn The Media Products to set.
     */
    public void setMediaProducts(MediaProducts mediaProductsIn) {
        this.mediaProducts = mediaProductsIn;
    }

    /**
     * @return Returns the Modules.
     */
    public MediaProducts getMediaProducts() {
        return mediaProducts;
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
     * @return Returns the endOfLife.
     */
    public Date getEndOfLife() {
        return endOfLife;
    }

    /**
     * @param e The endOfLife to set.
     */
    public void setEndOfLife(Date e) {
        this.endOfLife = e;
    }

    /**
     * @return Returns the gPGKeyFp.
     */
    public String getGPGKeyFp() {
        return GPGKeyFp;
    }

    /**
     * @param k The gPGKeyFP to set.
     */
    public void setGPGKeyFp(String k) {
        GPGKeyFp = k;
    }

    /**
     * @return Returns the gPGKeyId.
     */
    public String getGPGKeyId() {
        return GPGKeyId;
    }

    /**
     * @param k The gPGKeyId to set.
     */
    public void setGPGKeyId(String k) {
        GPGKeyId = k;
    }

    /**
     * @return Returns the gPGKeyUrl.
     */
    public String getGPGKeyUrl() {
        return GPGKeyUrl;
    }

    /**
     * @param k The gPGKeyUrl to set.
     */
    public void setGPGKeyUrl(String k) {
        GPGKeyUrl = k;
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
     * @return Returns the lastSynced.
     */
    public Date getLastSynced() {
        return lastSynced;
    }

    /**
     * @param lastSyncedIn The lastSynced to set.
     */
    public void setLastSynced(Date lastSyncedIn) {
        this.lastSynced = lastSyncedIn;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param n The name to set.
     */
    public void setName(String n) {
        this.name = n;
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
     * @return Returns the parentChannel.
     */
    public Channel getParentChannel() {
        return parentChannel;
    }

    /**
     * @param p The parentChannel to set.
     */
    public void setParentChannel(Channel p) {
        this.parentChannel = p;
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
     * @return Returns the set of erratas for this channel.
     */
    public Set<Errata> getErratas() {
        return erratas;
    }

    /**
     * Sets the erratas set for this channel
     * @param erratasIn The set of erratas
     */
    public void setErratas(Set<Errata> erratasIn) {
        this.erratas = erratasIn;
    }

    /**
     * Adds a single errata to the channel
     * @param errataIn The errata to add
     */
    public void addErrata(Errata errataIn) {
        erratas.add(errataIn);
    }

    /**
     * Do not use this function to get the count of packages as this is not efficient.
     *
     * @return Returns the set of packages for this channel.
     */
    public Set<Package> getPackages() {
        return packages;
    }

    /**
     * @return Returns the size of the package set for this channel.
     */
    public int getPackageCount() {
        // we don;t want to use packages.size()
        // this could be a lot (we don't want to load all the packages
        // in Rhn-server to get a single number) ...
        // So we are better off using a hibernate query for the count...
        return ChannelFactory.getPackageCount(this);
    }

    /**
     * @return Returns the size of the package set for this channel.
     */
    public int getErrataCount() {
        return ChannelFactory.getErrataCount(this);
    }


    /**
     * Sets the packages set for this channel
     * @param packagesIn The set of erratas
     */
    public void setPackages(Set<Package> packagesIn) {
        this.packages = packagesIn;
    }

    /**
     *
     * @param sourcesIn The set of yum repo sources
     */
    public void setSources(Set<ContentSource> sourcesIn) {
        this.sources = sourcesIn;
    }

    /**
     *
     * @return set of yum repos for this channel
     */
    public Set<ContentSource> getSources() {
        return sources;
    }


    /**
     * Adds a single package to the channel
     * @param packageIn The package to add
     * @deprecated Do not use this method.
     */
    @Deprecated
    public void addPackage(Package packageIn) {
        if (!getChannelArch().isCompatible(packageIn.getPackageArch())) {
            throw new IncompatibleArchException(packageIn.getPackageArch(),
                    getChannelArch());
        }
        packages.add(packageIn);
    }

    /**
     * Removes a single package from the channel
     * @param user the user doing the remove
     * @param packageIn The package to remove
     */
    public void removePackage(Package packageIn, User user) {
            List<Long> list = new ArrayList<>();
            list.add(packageIn.getId());
            ChannelManager.removePackages(this, list, user);
    }

    /**
     * Some methods for hibernate to get and set channel families. However,
     * there should be only one channel family per channel.
     */

    /**
     * @return Returns the set of channelFamiliess for this channel.
     */
    public Set<ChannelFamily> getChannelFamilies() {
        return channelFamilies;
    }

    /**
     * Sets the channelFamilies set for this channel
     * @param channelFamiliesIn The set of channelFamilies
     */
    public void setChannelFamilies(Set<ChannelFamily> channelFamiliesIn) {
        if (channelFamiliesIn.size() > 1) {
            throw new TooManyChannelFamiliesException(this.getId(),
                    "A channel can only have one channel family");
        }
        this.channelFamilies = channelFamiliesIn;
    }

    /**
     *
     * @param trustedOrgsIn set of trusted orgs for this channel
     */
    public void setTrustedOrgs(Set<Org> trustedOrgsIn) {
        this.trustedOrgs = trustedOrgsIn;
    }

    /**
     *
     * @return set of trusted orgs for this channel
     */
    public Set<Org> getTrustedOrgs() {
        return this.trustedOrgs;
    }

    /**
     * @return number of trusted organizations that have access to this channel
     */
    public int getTrustedOrgsCount() {
        if (trustedOrgs != null) {
            return trustedOrgs.size();
        }
        return 0;
    }

    /**
     * Adds a single channelFamily to the channel
     * @param channelFamilyIn The channelFamily to add
     */
    public void addChannelFamily(ChannelFamily channelFamilyIn) {
        if (!this.getChannelFamilies().isEmpty()) {
            throw new TooManyChannelFamiliesException(this.getId(),
                    "A channel can only have one channel family");
        }
        channelFamilies.add(channelFamilyIn);
    }

    /**
     * Set the channel family for this channel.
     * @param channelFamilyIn The channelFamily to add
     */
    public void setChannelFamily(ChannelFamily channelFamilyIn) {
        channelFamilies.clear();
        this.addChannelFamily(channelFamilyIn);
    }

    /**
     * Get the channel family for this channel.
     * @return the channel's family, or null if none found
     */
    public ChannelFamily getChannelFamily() {
        if (this.getChannelFamilies().size() == 1) {
            Object[] cfams = this.getChannelFamilies().toArray();
            return (ChannelFamily) cfams[0];
        }

        return null;
    }

    /**
     * Returns true if this channel is considered a base channel.
     * @return true if this channel is considered a base channel.
     */
    public boolean isBaseChannel() {
        return (getParentChannel() == null);
    }

    /**
     * Returns true if this channel is a cloned channel.
     * @return whether the channel is cloned or not
     */
    public boolean isCloned() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (other instanceof SelectableChannel castOther) {
            return this.equals(castOther.getChannel());
        }
        if (!(other instanceof Channel castOther)) {
            return false;
        }
        return new EqualsBuilder()
                .append(getId(), castOther.getId())
                .append(isCloned(), castOther.isCloned())
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getId()).append(isCloned()).toHashCode();
    }

    /**
     * @return Returns the product.
     */
    public ChannelProduct getProduct() {
        return product;
    }

    /**
     * @param productIn The product to set.
     */
    public void setProduct(ChannelProduct productIn) {
        this.product = productIn;
    }

    /**
     * @return Returns the distChannelMaps.
     */
    public Set<DistChannelMap> getDistChannelMaps() {
        return distChannelMaps;
    }

    /**
     * @param distChannelMapsIn The distChannelMaps to set.
     */
    public void setDistChannelMaps(Set<DistChannelMap> distChannelMapsIn) {
        this.distChannelMaps = distChannelMapsIn;
    }

    /**
     * Check if this channel is subscribable by the Org passed in. Checks:
     *
     * 1) If channel is a Proxy or Spacewalk channel == false 2) If channel has
     * 0 (or less) available subscriptions == false.
     *
     * @param orgIn to check available subs
     * @param server to check if subscribable
     * @return boolean if subscribable or not
     */
    public boolean isSubscribable(Org orgIn, Server server) {

        if (log.isDebugEnabled()) {
            log.debug("isSubscribable.archComp: {}", SystemManager.verifyArchCompatibility(server, this));
            log.debug("isSatellite: {}", this.isMgrServer());
        }

        return SystemManager.verifyArchCompatibility(server, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("label", label).toString();
    }

    /**
     * @return the productName
     */
    public ProductName getProductName() {
        return productName;
    }

    /**
     * @param productNameIn the productName to set
     */
    public void setProductName(ProductName productNameIn) {
        this.productName = productNameIn;
    }

    /**
     * Returns true if the access provided is a valid value.
     * @param acc the access value being checked
     * @return true if the access provided is valid
     */
    public boolean isValidAccess(String acc) {
        return acc.equals(Channel.PUBLIC) || acc.equals(Channel.PRIVATE) || acc.equals(Channel.PROTECTED);
    }

    /**
     *@param acc public, protected, or private
     */
    public void setAccess(String acc) {
        access = acc;
    }

    /**
     * @return public, protected, or private
     */
    public String getAccess() {
        return access;
    }

    /**
     *
     * @return wheter channel is protected
     */
    public boolean isProtected() {
        return this.getAccess().equals(Channel.PROTECTED);
    }

    /**
     * Returns the child channels associated to a base channel
     * @param user the User needed for accessibility issues
     * @return a list of child channels or empty list if there are none.
     */
    public List<Channel> getAccessibleChildrenFor(User user) {
        if (isBaseChannel()) {
            return ChannelFactory.getAccessibleChildChannels(this, user);
        }
        return new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Channel o) {
        return this.getName().compareTo(o.getName());
    }

    /**
     * @return maintainer's name
     */
    public String getMaintainerName() {
        return maintainerName;
    }

    /**
     * @return maintainer's email
     */
    public String getMaintainerEmail() {
        return maintainerEmail;
    }

    /**
     * @return maintainer's phone number
     */
    public String getMaintainerPhone() {
        return maintainerPhone;
    }

    /**
     * @return channel's support policy
     */
    public String getSupportPolicy() {
        return supportPolicy;
    }

    /**
     * @param mname maintainer's name
     */
    public void setMaintainerName(String mname) {
        maintainerName = mname;
    }

    /**
     * @param email maintainer's email
     */
    public void setMaintainerEmail(String email) {
        maintainerEmail = email;
    }

    /**
     * @param phone maintainer's phone number (string)
     */
    public void setMaintainerPhone(String phone) {
        maintainerPhone = phone;
    }

    /**
     * @param policy channel support policy
     */
    public void setSupportPolicy(String policy) {
        supportPolicy = policy;
    }

    /**
     * Created for taskomatic -- probably shouldn't be called from the webui
     * @return returns if custom channel
     */
    public boolean isCustom() {
        return getOrg() != null;
    }

    /**
     * Does this channel need repodata generated
     * @return Returns a boolean if repodata generation Required
     */
    public boolean isChannelRepodataRequired() {
        // generate repodata for all channels having channel checksum set except solaris
        if (archesToSkipRepodata.contains(this.channelArch.getLabel())) {
            return true;
        }

        return checksumType != null;
    }

    /**
     * true if the channel contains any kickstartstartable distros
     * @return true if the channel contains any distros.
     */
    public boolean containsDistributions() {
        return ChannelFactory.containsDistributions(this);
    }

    /**
     * get the compatible checksum type to be used for repomd.xml
     * based on channel release.
     * If its a custom channel use the checksum_type_id from db set at creation time
     * If its RHEL-5 we use sha1 anything newer will be sha256.
     * @return checksumType
     */
    public String getChecksumTypeLabel() {

        if ((checksumType == null) || (checksumType.getLabel() == null)) {
            // each channel shall have set checksumType
            return null;
        }
        return checksumType.getLabel();
    }

    /**
     * @return the updateTag
     */
    public String getUpdateTag() {
        return updateTag;
    }

    /**
     * @param updateTagIn the update tag
     */
    public void setUpdateTag(String updateTagIn) {
        updateTag = updateTagIn;
    }

    /**
     * @return Returns the installerUpdates.
     */
    public boolean isInstallerUpdates() {
        return installerUpdates;
    }

    /**
     * @param installerUpdatesIn The installerUpdates to set.
     */
    public void setInstallerUpdates(boolean installerUpdatesIn) {
        installerUpdates = installerUpdatesIn;
    }

    /**
     * @return the clonedChannels
     */

    /**
     * Returns all cloned channels of this channel which includes all clones of clones.
     * @return all cloned channels
     */
    public Stream<ClonedChannel> allClonedChannels() {
        return getClonedChannels().stream().flatMap(c -> Stream.concat(Stream.of(c), c.allClonedChannels()));
    }

    public Set<ClonedChannel> getClonedChannels() {
        return clonedChannels;
    }

    /**
     * @param clonedChannelsIn the clonedChannels to set
     */
    public void setClonedChannels(Set<ClonedChannel> clonedChannelsIn) {
        this.clonedChannels = clonedChannelsIn;
    }

    /**
     * @return the GPGCheck
     */
    public boolean isGPGCheck() {
        return GPGCheck;
    }

    /**
     * @param gpgCheckIn the GPGCheck to set
     */
    public void setGPGCheck(boolean gpgCheckIn) {
        this.GPGCheck = gpgCheckIn;
    }

    /**
     * @return the original Channel the channel was cloned from
     */
    public Channel getOriginal() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a Stream starting with this channel and waking up the getOriginal chain
     * until getting to the original non cloned channel.
     *
     * @return stream of channels
     */
    public Stream<Channel> originChain() {
        return Stream.iterate(this, Objects::nonNull, c -> c.asCloned().map(ClonedChannel::getOriginal).orElse(null));
    }

    /**
     * @return the {@link SUSEProductChannel}
     */
    public Set<SUSEProductChannel> getSuseProductChannels() {
        return suseProductChannels;
    }

    /**
     * Set {@link SUSEProductChannel}
     * @param suseProductChannelsIn the product channel to set
     */
    public void setSuseProductChannels(Set<SUSEProductChannel> suseProductChannelsIn) {
        this.suseProductChannels = suseProductChannelsIn;
    }

    /**
     * Finds the suse product channel for a channel
     * Note: does not work for all channels see comment in source.
     * @return suse product channel
     */
    public Optional<SUSEProductChannel> findProduct() {
        Set<SUSEProductChannel> suseProducts = getSuseProductChannels();
        if (suseProducts.isEmpty()) {
            return Optional.empty();
        }
        else {
            // We take the first item since there can be more than one entry.
            // All entries should point to the same "product" with only arch differences.
            // The only exception to this is sles11 sp1/2 and caasp 1/2 but they are out of maintenance
            // and we decided to ignore this inconsistency until the great rewrite.
            return suseProducts.stream().findFirst();
        }
    }

    private String getArchTypeLabel() {
        return this.getChannelArch().getArchType().getLabel();
    }

    /**
     * @return whether the channel is a RPM chanel or not
     */
    public boolean isTypeRpm() {
        return PackageFactory.ARCH_TYPE_RPM.equalsIgnoreCase(getArchTypeLabel());
    }

    /**
     * @return whether the channel is a DEB chanel or not
     */
    public boolean isTypeDeb() {
        return PackageFactory.ARCH_TYPE_DEB.equalsIgnoreCase(getArchTypeLabel());
    }

    /**
     * Return the {@link Channel} as {@link ClonedChannel} if it is one
     *
     * @return the optional of {@link ClonedChannel}
     */
    public Optional<ClonedChannel> asCloned() {
        return Optional.empty();
    }

    public void setChannelSyncFlag(ChannelSyncFlag csf) {
        this.channelSyncFlag = csf;
    }

    /**
     * @return the channels sync flag settings
     */
    public ChannelSyncFlag getChannelSyncFlag() {
        if (channelSyncFlag == null) {
            channelSyncFlag = new ChannelSyncFlag();
            channelSyncFlag.setChannel(this);
        }
        return this.channelSyncFlag;
    }
}

