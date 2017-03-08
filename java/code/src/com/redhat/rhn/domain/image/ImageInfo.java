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
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.InstalledProduct;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerArch;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

/**
 * ImageInfo
 */
@Entity
@Table(name = "suseImageInfo")
public class ImageInfo extends BaseDomainHelper {

    private Long id;
    private String name;
    private String version;
    private String checksum;
    private ImageProfile profile;
    private ImageStore store;
    private MinionServer buildServer;
    private ImageBuildAction action;
    private ServerAction serverAction;
    private Set<ImageInfoCustomDataValue> customDataValues = new HashSet<>();
    private Set<Channel> channels = new HashSet<>();
    private Set<ImagePackage> packages = new HashSet<>();
    private Set<InstalledProduct> installedProducts = new HashSet<>();
    private Org org;
    private ServerArch imageArch;

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
     * @return the checksum
     */
    public String getChecksum() {
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
    @OneToOne
    @JoinColumn(name = "action_id")
    public ImageBuildAction getAction() {
        return action;
    }

    /**
     * @param actionIn the build action
     */
    public void setAction(ImageBuildAction actionIn) {
        this.action = actionIn;
    }

    /**
     * @return the build serverAction
     */
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "action_id", referencedColumnName = "action_id",
                    insertable = false, updatable = false),
            @JoinColumn(name = "build_server_id", referencedColumnName = "server_id",
                    insertable = false, updatable = false)
    })
    public ServerAction getServerAction() {
        return serverAction;
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
     * @param idIn id to set
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * @param checksumIn checksum to set
     */
    public void setChecksum(String checksumIn) {
        this.checksum = checksumIn;
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
     * @param actionIn build serverAction to set
     */
    public void setServerAction(ServerAction actionIn) {
        this.serverAction = actionIn;
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
     * {@inheritDoc}
     */
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
    public int hashCode() {
        return new HashCodeBuilder()
                .append(name)
                .append(version)
                .append(checksum)
                .toHashCode();
    }

}
