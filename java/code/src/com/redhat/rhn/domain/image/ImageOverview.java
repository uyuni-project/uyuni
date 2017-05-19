/**
 * Copyright (c) 2017 SUSE LLC
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

package com.redhat.rhn.domain.image;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.common.Checksum;
import com.redhat.rhn.domain.errata.impl.PublishedErrata;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.InstalledProduct;
import com.redhat.rhn.domain.server.MinionServer;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Immutable;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * View: suseImageOverview
 */
@Entity
@Table(name = "suseImageOverview")
@Immutable
public class ImageOverview extends BaseDomainHelper {

    private Long id;
    private String name;
    private String version;
    private Checksum checksum;
    private String arch;
    private ImageProfile profile;
    private ImageStore store;
    private MinionServer buildServer;
    private ServerAction buildAction;
    private ServerAction inspectAction;
    private Set<ImageInfoCustomDataValue> customDataValues;
    private Set<Channel> channels;
    private Set<InstalledProduct> installedProducts;
    private Set<ImagePackage> packages;
    private Set<PublishedErrata> patches;
    private Org org;
    private Integer securityErrata;
    private Integer bugErrata;
    private Integer enhancementErrata;
    private Integer outdatedPackages;
    private Integer installedPackages;

    /**
     * @return the id
     */
    @Id
    @Column(name = "image_id")
    public Long getId() {
        return id;
    }

    /**
     * @return the org
     */
    @ManyToOne
    public Org getOrg() {
        return org;
    }


    /**
     * @return the name
     */
    @Column(name = "image_name")
    public String getName() {
        return name;
    }

    /**
     * @return the version
     */
    @Column(name = "image_version")
    public String getVersion() {
        return version;
    }

    /**
     * @return the checksum
     */
    @ManyToOne
    @JoinColumn(name = "checksum_id")
    public Checksum getChecksum() {
        return checksum;
    }


    /**
     * @return the image profile
     */
    @ManyToOne
    @JoinColumn(name = "profile_id")
    public ImageProfile getProfile() {
        return profile;
    }

    /**
     * @return the image store
     */
    @ManyToOne
    @JoinColumn(name = "store_id")
    public ImageStore getStore() {
        return store;
    }

    /**
     * @return the build server
     */
    @ManyToOne
    @JoinColumn(name = "build_server_id")
    public MinionServer getBuildServer() {
        return buildServer;
    }

    /**
     * @return the build action
     */
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "build_action_id", referencedColumnName = "action_id",
                    insertable = false, updatable = false),
            @JoinColumn(name = "build_server_id", referencedColumnName = "server_id",
                    insertable = false, updatable = false)
    })
    public ServerAction getBuildAction() {
        return buildAction;
    }

    /**
     * @return the inspect action
     */
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "inspect_action_id", referencedColumnName = "action_id",
                    insertable = false, updatable = false),
            @JoinColumn(name = "build_server_id", referencedColumnName = "server_id",
                    insertable = false, updatable = false)
    })
    public ServerAction getInspectAction() {
        return inspectAction;
    }

    /**
     * @return the custom data values
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "imageInfo")
    public Set<ImageInfoCustomDataValue> getCustomDataValues() {
        return customDataValues;
    }

    /**
     * @return the channels
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "suseImageInfoChannel",
        joinColumns = {
            @JoinColumn(name = "image_info_id", nullable = false, updatable = false)},
        inverseJoinColumns = {
            @JoinColumn(name = "channel_id", nullable = false, updatable = false)}
    )
    public Set<Channel> getChannels() {
        return channels;
    }

    /**
     * @return the installed products
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "suseImageInfoInstalledProduct",
        joinColumns = {
            @JoinColumn(name = "image_info_id", nullable = false, updatable = false)},
        inverseJoinColumns = {
            @JoinColumn(name = "installed_product_id", nullable = false, updatable = false)
    })
    public Set<InstalledProduct> getInstalledProducts() {
        return installedProducts;
    }

    /**
     * @return the packages
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "imageInfo")
    public Set<ImagePackage> getPackages() {
        return packages;
    }

    /**
     * @return the patches
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "rhnImageNeededErrataCache",
            joinColumns = {@JoinColumn(name = "image_id")},
            inverseJoinColumns = {@JoinColumn(name = "errata_id")}
    )
    public Set<PublishedErrata> getPatches() {
        return patches;
    }

    /**
     * @return the arch string
     */
    @Column(name = "image_arch_name")
    public String getArch() {
        return arch;
    }

    /**
     * @return the security errata
     */
    @Column(name = "security_errata")
    public Integer getSecurityErrata() {
        return securityErrata;
    }

    /**
     * @return the bug errata count
     */
    @Column(name = "bug_errata")
    public Integer getBugErrata() {
        return bugErrata;
    }

    /**
     * @return the enhancement errata count
     */
    @Column(name = "enhancement_errata")
    public Integer getEnhancementErrata() {
        return enhancementErrata;
    }

    /**
     * @return the outdated package count
     */
    @Column(name = "outdated_packages")
    public Integer getOutdatedPackages() {
        return outdatedPackages;
    }

    /**
     * @return the installed package count
     */
    @Column(name = "installed_packages")
    public Integer getInstalledPackages() {
        return installedPackages;
    }

    @Override
    @Column(name = "modified")
    public Date getModified() {
        return super.getModified();
    }

    /**
     * @param idIn the id
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * @param nameIn the name
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * @param versionIn the version
     */
    public void setVersion(String versionIn) {
        this.version = versionIn;
    }

    /**
     * @param checksumIn the checksum
     */
    public void setChecksum(Checksum checksumIn) {
        this.checksum = checksumIn;
    }

    /**
     * @param archIn the arch
     */
    public void setArch(String archIn) {
        this.arch = archIn;
    }

    /**
     * @param profileIn the profile
     */
    public void setProfile(ImageProfile profileIn) {
        this.profile = profileIn;
    }

    /**
     * @param storeIn the store
     */
    public void setStore(ImageStore storeIn) {
        this.store = storeIn;
    }

    /**
     * @param buildServerIn the build server
     */
    public void setBuildServer(MinionServer buildServerIn) {
        this.buildServer = buildServerIn;
    }

    /**
     * @param actionIn the build action
     */
    public void setBuildAction(ServerAction actionIn) {
        this.buildAction = actionIn;
    }

    /**
     * @param actionIn the inspect action
     */
    public void setInspectAction(ServerAction actionIn) {
        this.inspectAction = actionIn;
    }

    /**
     * @param customDataValuesIn the custom data values
     */
    public void setCustomDataValues(Set<ImageInfoCustomDataValue> customDataValuesIn) {
        this.customDataValues = customDataValuesIn;
    }

    /**
     * @param channelsIn the channels
     */
    public void setChannels(Set<Channel> channelsIn) {
        this.channels = channelsIn;
    }

    /**
     * @param packagesIn the packages
     */
    public void setPackages(Set<ImagePackage> packagesIn) {
        this.packages = packagesIn;
    }

    /**
     * @param patchesIn the patches
     */
    public void setPatches(Set<PublishedErrata> patchesIn) {
        this.patches = patchesIn;
    }

    /**
     * @param orgIn the org
     */
    public void setOrg(Org orgIn) {
        this.org = orgIn;
    }

    /**
     * @param securityErrataIn the security errata
     */
    public void setSecurityErrata(Integer securityErrataIn) {
        this.securityErrata = securityErrataIn;
    }

    /**
     * @param bugErrataIn the bug errata
     */
    public void setBugErrata(Integer bugErrataIn) {
        this.bugErrata = bugErrataIn;
    }

    /**
     * @param enhancementErrataIn the enhancement errata
     */
    public void setEnhancementErrata(Integer enhancementErrataIn) {
        this.enhancementErrata = enhancementErrataIn;
    }

    /**
     * @param outdatedPackagesIn the outdated packages
     */
    public void setOutdatedPackages(Integer outdatedPackagesIn) {
        this.outdatedPackages = outdatedPackagesIn;
    }

    /**
     * @param installedPackagesIn the installed packages
     */
    public void setInstalledPackages(Integer installedPackagesIn) {
        this.installedPackages = installedPackagesIn;
    }

    /**
     * @deprecated do not use. Only for hibernate.
     * @param installedProductsIn the installed products
     */
    @Deprecated
    public void setInstalledProducts(Set<InstalledProduct> installedProductsIn) {
        this.installedProducts = installedProductsIn;
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(final Object other) {
        if (!(other instanceof ImageOverview)) {
            return false;
        }
        ImageOverview castOther = (ImageOverview) other;
        return new EqualsBuilder()
                .append(name, castOther.name)
                .append(version, castOther.version)
                .append(checksum, castOther.checksum)
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return new HashCodeBuilder()
                .append(name)
                .append(version)
                .append(checksum)
                .toHashCode();
    }

}
