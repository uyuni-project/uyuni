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

package com.suse.manager.webui.utils.gson;

import com.google.gson.JsonObject;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.common.Checksum;
import com.redhat.rhn.domain.image.ImageInfoCustomDataValue;
import com.redhat.rhn.domain.image.ImageOverview;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.server.InstalledProduct;
import com.redhat.rhn.domain.server.MinionServer;
import com.suse.manager.webui.utils.ViewHelper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The Image info JSON object
 */
public class ImageInfoJson {

    private Long id;
    private String name;
    private String version;
    private Integer revision;
    private String checksum;
    private boolean external;
    private JsonObject profile;
    private JsonObject store;
    private JsonObject buildServer;
    private JsonObject buildAction;
    private JsonObject inspectAction;
    private ChannelsJson channels;
    private InstalledProductsJson installedProducts;
    private Map<String, String> customData;
    private JsonObject patches;
    private Integer packages;
    private Integer installedPackages;

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param idIn the id
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param nameIn the name
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param versionIn the version
     */
    public void setVersion(String versionIn) {
        this.version = versionIn;
    }

    /**
     * @return the revision
     */
    public Integer getRevision() {
        return revision;
    }

    /**
     * @param revisionIn the revision
     */
    public void setRevision(Integer revisionIn) {
        this.revision = revisionIn;
    }

    /**
     * @return the checksum
     */
    public String getChecksum() {
        return checksum;
    }

    /**
     * @param checksumIn the checksum
     */
    public void setChecksum(String checksumIn) {
        this.checksum = checksumIn;
    }

    /**
     * @return true if the image is externally built
     */
    public boolean isExternal() {
        return external;
    }

    /**
     * @param externalIn the external boolean
     */
    public void setExternal(boolean externalIn) {
        this.external = externalIn;
    }

    /**
     * @return the image profile
     */
    public JsonObject getProfile() {
        return profile;
    }

    /**
     * @param profileIn the image profile
     */
    public void setProfile(ImageProfile profileIn) {
        if (profileIn == null) {
            this.profile = null;
            return;
        }
        JsonObject json = new JsonObject();
        json.addProperty("id", profileIn.getProfileId());
        json.addProperty("label", profileIn.getLabel());
        json.addProperty("type", profileIn.getImageType());
        this.profile = json;
    }

    /**
     * @return the image store
     */
    public JsonObject getStore() {
        return store;
    }

    /**
     * @param storeIn the image store
     */
    public void setStore(ImageStore storeIn) {
        if (storeIn == null) {
            this.store = null;
            return;
        }
        JsonObject json = new JsonObject();
        json.addProperty("id", storeIn.getId());
        json.addProperty("label", storeIn.getLabel());
        json.addProperty("uri", storeIn.getUri());
        json.addProperty("type", storeIn.getStoreType().getLabel());
        this.store = json;
    }

    /**
     * @return the build server
     */
    public JsonObject getBuildServer() {
        return buildServer;
    }

    /**
     * @param serverIn the build server
     */
    public void setBuildServer(MinionServer serverIn) {
        if (serverIn == null) {
            this.buildServer = null;
            return;
        }
        JsonObject json = new JsonObject();
        json.addProperty("id", serverIn.getId());
        json.addProperty("name", serverIn.getName());
        this.buildServer = json;
    }

    /**
     * @return the build action
     */
    public JsonObject getBuildAction() {
        return buildAction;
    }

    /**
     * @param action the build action
     */
    public void setBuildAction(ServerAction action) {
        this.buildAction = jsonFromServerAction(action);
    }

    /**
     * @return the inspect action
     */
    public JsonObject getInspectAction() {
        return inspectAction;
    }

    /**
     * @param action the inspect action
     */
    public void setInspectAction(ServerAction action) {
        this.inspectAction = jsonFromServerAction(action);
    }

    /**
     * @return the custom data
     */
    public Map<String, String> getCustomData() {
        return customData;
    }

    /**
     * @param customDataIn the custom data
     */
    public void setCustomData(Set<ImageInfoCustomDataValue> customDataIn) {
        if (customDataIn == null) {
            this.customData = null;
            return;
        }

        this.customData = customDataIn.stream().collect(Collectors.toMap(
                (cd) -> cd.getKey().getLabel(), ImageInfoCustomDataValue::getValue));
    }

    /**
     * @return the channels
     */
    public ChannelsJson getChannels() {
        return channels;
    }

    /**
     * @param channelsIn the channels
     */
    public void setChannels(ChannelsJson channelsIn) {
        this.channels = channelsIn;
    }

    /**
     * @return the patches
     */
    public JsonObject getPatches() {
        return patches;
    }

    /**
     * Sets number of patches
     *
     * @param security    the number of security patches
     * @param bug         the number of bug patches
     * @param enhancement the number of enhancement patches
     */
    public void setPatches(Integer security, Integer bug, Integer enhancement) {
        if (security == null && bug == null && enhancement == null) {
            return;
        }
        JsonObject json = new JsonObject();
        json.addProperty("security", security);
        json.addProperty("bug", bug);
        json.addProperty("enhancement", enhancement);
        this.patches = json;
    }

    /**
     * @return the number of outadated packages
     */
    public Integer getPackages() {
        return packages;
    }

    /**
     * @param packagesIn the number of outdated packages
     */
    public void setPackages(Integer packagesIn) {
        this.packages = packagesIn;
    }

    /**
     * @return the number of installed packages
     */
    public Integer getInstalledPackages() {
        return installedPackages;
    }

    /**
     * @param packagesIn the number of installed packages
     */
    public void setInstalledPackages(Integer packagesIn) {
        this.installedPackages = packagesIn;
    }

    /**
     * @param installedProductsIn installed products
     */
    public void setInstalledProducts(InstalledProductsJson installedProductsIn) {
        this.installedProducts = installedProductsIn;
    }

    /**
     * Creates a JSON object from an image overview object
     *
     * @param imageOverview the image overview
     * @return the image info json
     */
    public static ImageInfoJson fromImageInfo(ImageOverview imageOverview) {
        Checksum c = imageOverview.getChecksum();
        ImageInfoJson json = new ImageInfoJson();
        json.setId(imageOverview.getId());
        json.setName(imageOverview.getName());
        json.setVersion(imageOverview.getVersion());
        json.setRevision(imageOverview.getCurrRevisionNum());
        json.setChecksum(c != null ? c.getChecksum() : "");
        json.setExternal(imageOverview.isExternalImage());
        json.setProfile(imageOverview.getProfile());
        json.setStore(imageOverview.getStore());
        json.setBuildServer(imageOverview.getBuildServer());
        imageOverview.getBuildServerAction().ifPresent(json::setBuildAction);
        imageOverview.getInspectServerAction().ifPresent(json::setInspectAction);
        json.setChannels(ChannelsJson.fromChannelSet(imageOverview.getChannels()));
        json.setPatches(imageOverview.getSecurityErrata(), imageOverview.getBugErrata(),
                imageOverview.getEnhancementErrata());
        json.setPackages(imageOverview.getOutdatedPackages());
        json.setInstalledPackages(imageOverview.getInstalledPackages());

        Map<Boolean, List<InstalledProduct>> collect = imageOverview.getInstalledProducts()
                .stream().collect(Collectors.partitioningBy(InstalledProduct::isBaseproduct));
        Optional<InstalledProductsJson> installedProductsJson =
                collect.get(true).stream().findFirst().map(base -> new InstalledProductsJson(getProductName(base),
                        collect.get(false).stream().map(ImageInfoJson::getProductName).collect(Collectors.toSet())));
        json.setInstalledProducts(installedProductsJson.orElse(null));
        json.setCustomData(imageOverview.getCustomDataValues());

        return json;
    }

    /**
     * @param actionIn the server action
     */
    private JsonObject jsonFromServerAction(ServerAction actionIn) {
        if (actionIn == null) {
            return null;
        }

        ViewHelper vh = ViewHelper.getInstance();

        JsonObject json = new JsonObject();
        json.addProperty("id", actionIn.getParentAction().getId());
        json.addProperty("name", actionIn.getParentAction().getName());
        json.addProperty("status", actionIn.getStatus().getId());
        json.addProperty("pickup_time", actionIn.getPickupTime() != null ?
                vh.renderDate(actionIn.getPickupTime()) : null);
        json.addProperty("completion_time", actionIn.getCompletionTime() != null ?
                vh.renderDate(actionIn.getCompletionTime()) : null);
        return json;
    }

    private static String getProductName(InstalledProduct product) {
        SUSEProduct suseProduct = product.getSUSEProduct();
        return suseProduct != null ? suseProduct.getFriendlyName() : product.getName() + " " + product.getVersion();
    }
}
