/*
 * Copyright (c) 2017--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.image;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.action.salt.build.ImageBuildAction;
import com.redhat.rhn.domain.action.salt.inspect.ImageInspectAction;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.common.Checksum;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.rhnpackage.PackageType;
import com.redhat.rhn.domain.server.InstalledProduct;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Pillar;
import com.redhat.rhn.domain.server.ServerArch;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.type.YesNoConverter;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * ImageInfo
 */
@Entity
@Table(name = "suseImageInfo")
public class ImageInfo extends BaseDomainHelper {


    @Id
    @Column(name = "id", insertable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "imginfo_seq")
    @SequenceGenerator(name = "imginfo_seq", sequenceName = "suse_imginfo_imgid_seq", allocationSize = 1)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "version")
    private String version;

    @Column(name = "image_type")
    private String imageType;

    @ManyToOne
    @JoinColumn(name = "checksum_id")
    private Checksum checksum;

    @Column(name = "curr_revision_num")
    private int revisionNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private ImageProfile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private ImageStore store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "build_server_id")
    private MinionServer buildServer;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "build_action_id")
    private ImageBuildAction buildAction;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspect_action_id")
    private ImageInspectAction inspectAction;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "imageInfo", cascade = CascadeType.ALL)
    private Set<ImageInfoCustomDataValue> customDataValues = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "suseImageInfoChannel",
               joinColumns = { @JoinColumn(name = "image_info_id") },
               inverseJoinColumns = { @JoinColumn(name = "channel_id") })
    private Set<Channel> channels = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "imageInfo")
    private Set<ImagePackage> packages = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "suseImageInfoInstalledProduct",
               joinColumns = { @JoinColumn(name = "image_info_id") },
               inverseJoinColumns = { @JoinColumn(name = "installed_product_id") })
    private Set<InstalledProduct> installedProducts = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "imageInfo", cascade = CascadeType.ALL)
    private Set<ImageRepoDigest> repoDigests = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private Org org;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_arch_id")
    private ServerArch imageArch;

    @Column(name = "external_image")
    @Convert(converter = YesNoConverter.class)
    private boolean externalImage;

    @Column(name = "obsolete")
    @Convert(converter = YesNoConverter.class)
    private boolean obsolete;

    @Column(name = "built")
    @Convert(converter = YesNoConverter.class)
    private boolean built;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "imageInfo", cascade = CascadeType.ALL)
    private Set<ImageFile> imageFiles = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pillar_id")
    private Pillar pillar;

    @OneToMany
    @JoinColumn(name = "source_image_id", updatable = false)
    private Set<DeltaImageInfo> deltaSourceFor = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_image_id", updatable = false)
    private Set<DeltaImageInfo> deltaTargetFor = new HashSet<>();

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "log")
    private String buildLog;

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @return the org
     */
    public Org getOrg() {
        return org;
    }

    /**
     * @return the image arch
     */
    public ServerArch getImageArch() {
        return imageArch;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return the image type
     */
    public String getImageType() {
        return imageType;
    }

    /**
     * @return the checksum
     */
    public Checksum getChecksum() {
        return checksum;
    }

    /**
     * @return the current (latest) revision number
     */
    public int getRevisionNumber() {
        return revisionNumber;
    }

    /**
     * @return the image profile
     */
    public ImageProfile getProfile() {
        return profile;
    }

    /**
     * @return the image store
     */
    public ImageStore getStore() {
        return store;
    }

    /**
     * @return the build server
     */
    public MinionServer getBuildServer() {
        return buildServer;
    }

    /**
     * @return the build action
     */
    public ImageBuildAction getBuildAction() {
        return buildAction;
    }

    /**
     * @param actionIn the build action
     */
    public void setBuildAction(ImageBuildAction actionIn) {
        this.buildAction = actionIn;
    }

    /**
     * @return the inspect action
     */
    public ImageInspectAction getInspectAction() {
        return inspectAction;
    }

    /**
     * @param actionIn the inspect action
     */
    public void setInspectAction(ImageInspectAction actionIn) {
        this.inspectAction = actionIn;
    }

    /**
     * @return the custom data values
     */
    public Set<ImageInfoCustomDataValue> getCustomDataValues() {
        return customDataValues;
    }

    /**
     * @return the packages
     */
    public Set<ImagePackage> getPackages() {
        return packages;
    }

    /**
     * @return the channels
     */
    public Set<Channel> getChannels() {
        return channels;
    }

    /**
     * @return the installed installedProducts
     */
    public Set<InstalledProduct> getInstalledProducts() {
        return installedProducts;
    }

    /**
     * @return true if the image has been built outside SUSE Manager
     */
    public boolean isExternalImage() {
        return externalImage;
    }

    /**
     * @return true if the image is obsolete (has been replaced in the store)
     */
    public boolean isObsolete() {
        return obsolete;
    }

    /**
     * @return true if the image has been successfully built
     */
    public boolean isBuilt() {
        return built;
    }

    /**
     * @return the repo digests
     */
    public Set<ImageRepoDigest> getRepoDigests() {
        return repoDigests;
    }

    /**
     * @return the files
     */
    public Set<ImageFile> getImageFiles() {
        return imageFiles;
    }

    /**
     * @return the pillar
     */
    public Pillar getPillar() {
        return pillar;
    }

    public Set<DeltaImageInfo> getDeltaSourceFor() {
        return deltaSourceFor;
    }

    public Set<DeltaImageInfo> getDeltaTargetFor() {
        return deltaTargetFor;
    }

    /**
     * @return build log
     */
    public String getBuildLog() {
        return buildLog;
    }

    /**
     * @param idIn id to set
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * @param checksumIn checksum to set
     */
    public void setChecksum(Checksum checksumIn) {
        this.checksum = checksumIn;
    }

    /**
     * @param revisionNumberIn the revision number
     */
    public void setRevisionNumber(int revisionNumberIn) {
        this.revisionNumber = revisionNumberIn;
    }

    /**
     * @param nameIn name to set
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * @param orgIn name to set
     */
    public void setOrg(Org orgIn) {
        this.org = orgIn;
    }

    /**
     * @param versionIn version to set
     */
    public void setVersion(String versionIn) {
        this.version = versionIn;
    }

    /**
     * @param imageTypeIn the image type
     */
    public void setImageType(String imageTypeIn) {
        this.imageType = imageTypeIn;
    }

    /**
     * @param profileIn profile to set
     */
    public void setProfile(ImageProfile profileIn) {
        this.profile = profileIn;
    }

    /**
     * @param storeIn store to set
     */
    public void setStore(ImageStore storeIn) {
        this.store = storeIn;
    }

    /**
     * @param buildServerIn build server to set
     */
    public void setBuildServer(MinionServer buildServerIn) {
        this.buildServer = buildServerIn;
    }

    /**
     * @param customDataValuesIn custom data values to set
     */
    public void setCustomDataValues(Set<ImageInfoCustomDataValue> customDataValuesIn) {
        this.customDataValues = customDataValuesIn;
    }

    /**
     * @param channelsIn channels to set
     */
    public void setChannels(Set<Channel> channelsIn) {
        this.channels = channelsIn;
    }

    /**
     * @param productsIn channels to set
     */
    public void setInstalledProducts(Set<InstalledProduct> productsIn) {
        this.installedProducts = productsIn;
    }

    /**
     * @param repoDigestsIn the repo digests
     */
    public void setRepoDigests(Set<ImageRepoDigest> repoDigestsIn) {
        this.repoDigests = repoDigestsIn;
    }

    /**
     * @param imageArchIn image arch to set
     */
    public void setImageArch(ServerArch imageArchIn) {
        this.imageArch = imageArchIn;
    }

    /**
     * @param packagesIn packages to set
     */
    public void setPackages(Set<ImagePackage> packagesIn) {
        this.packages = packagesIn;
    }

    /**
     * @param externalImageIn the external image
     */
    public void setExternalImage(boolean externalImageIn) {
        this.externalImage = externalImageIn;
    }

    /**
     * @param obsoleteIn the obsolete flag
     */
    public void setObsolete(boolean obsoleteIn) {
        this.obsolete = obsoleteIn;
    }

    /**
     * @param builtIn the built flag
     */
    public void setBuilt(boolean builtIn) {
        this.built = builtIn;
    }

    @Transient
    public PackageType getPackageType() {
        return getImageArch().getArchType().getPackageType();
    }


    /**
     * @param imageFilesIn the image files
     */
    public void setImageFiles(Set<ImageFile> imageFilesIn) {
        this.imageFiles = imageFilesIn;
    }

    /**
     * @param pillarIn file to set
     */
    public void setPillar(Pillar pillarIn) {
        this.pillar = pillarIn;
    }

    public void setBuildLog(String buildLogIn) {
        buildLog = buildLogIn;
    }

    public void setDeltaSourceFor(Set<DeltaImageInfo> deltaSourceForIn) {
        deltaSourceFor = deltaSourceForIn;
    }

    public void setDeltaTargetFor(Set<DeltaImageInfo> deltaTargetForIn) {
        deltaTargetFor = deltaTargetForIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ImageInfo castOther)) {
            return false;
        }
        return new EqualsBuilder()
                .append(id, castOther.id)
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .toHashCode();
    }

}
