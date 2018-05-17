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
import com.redhat.rhn.domain.action.salt.build.ImageBuildAction;
import com.redhat.rhn.domain.action.salt.inspect.ImageInspectAction;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.common.Checksum;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.InstalledProduct;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerArch;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Type;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * ImageInfo
 */
@Entity
@Table(name = "suseImageInfo")
public class ImageInfo extends BaseDomainHelper {

    private Long id;
    private String name;
    private String version;
    private String imageType;
    private Checksum checksum;
    private int revisionNumber;
    private ImageProfile profile;
    private ImageStore store;
    private MinionServer buildServer;
    private ImageBuildAction buildAction;
    private ImageInspectAction inspectAction;
    private Set<ImageInfoCustomDataValue> customDataValues = new HashSet<>();
    private Set<Channel> channels = new HashSet<>();
    private Set<ImagePackage> packages = new HashSet<>();
    private Set<InstalledProduct> installedProducts = new HashSet<>();
    private Set<ImageBuildHistory> buildHistory = new HashSet<>();
    private Org org;
    private ServerArch imageArch;
    private boolean externalImage;

    /**
     * @return the id
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "imginfo_seq")
    @SequenceGenerator(name = "imginfo_seq", sequenceName = "suse_imginfo_imgid_seq",
                       allocationSize = 1)
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
     * @return the image arch
     */
    @ManyToOne
    @JoinColumn(name = "image_arch_id")
    public ServerArch getImageArch() {
        return imageArch;
    }

    /**
     * @return the name
     */
    @Column(name = "name")
    public String getName() {
        return name;
    }

    /**
     * @return the version
     */
    @Column(name = "version")
    public String getVersion() {
        return version;
    }

    /**
     * @return the image type
     */
    @Column(name = "image_type")
    public String getImageType() {
        return imageType;
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
     * @return the current (latest) revision number
     */
    @Column(name = "curr_revision_num")
    public int getRevisionNumber() {
        return revisionNumber;
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
    @OneToOne
    @JoinColumn(name = "build_action_id")
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
    @OneToOne
    @JoinColumn(name = "inspect_action_id")
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
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "imageInfo", cascade = CascadeType.ALL)
    public Set<ImageInfoCustomDataValue> getCustomDataValues() {
        return customDataValues;
    }

    /**
     * @return the packages
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "imageInfo")
    public Set<ImagePackage> getPackages() {
        return packages;
    }

    /**
     * @return the channels
     */
    @ManyToMany
    @JoinTable(name = "suseImageInfoChannel",
               joinColumns = { @JoinColumn(name = "image_info_id") },
               inverseJoinColumns = { @JoinColumn(name = "channel_id") })
    public Set<Channel> getChannels() {
        return channels;
    }

    /**
     * @return the installed installedProducts
     */
    @ManyToMany
    @JoinTable(name = "suseImageInfoInstalledProduct",
               joinColumns = { @JoinColumn(name = "image_info_id") },
               inverseJoinColumns = { @JoinColumn(name = "installed_product_id") })
    public Set<InstalledProduct> getInstalledProducts() {
        return installedProducts;
    }

    /**
     * @return true if the image has been built outside SUSE Manager
     */
    @Column(name = "external_image")
    @Type(type = "yes_no")
    public boolean isExternalImage() {
        return externalImage;
    }

    /**
     * @return the build history
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "imageInfo", cascade = CascadeType.ALL)
    public Set<ImageBuildHistory> getBuildHistory() {
        return buildHistory;
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
     * @param buildHistoryIn the build history
     */
    public void setBuildHistory(Set<ImageBuildHistory> buildHistoryIn) {
        this.buildHistory = buildHistoryIn;
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
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ImageInfo)) {
            return false;
        }
        ImageInfo castOther = (ImageInfo) other;
        return new EqualsBuilder()
                .append(name, castOther.name)
                .append(version, castOther.version)
                .append(checksum, castOther.checksum)
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(name)
                .append(version)
                .append(checksum)
                .toHashCode();
    }

}
