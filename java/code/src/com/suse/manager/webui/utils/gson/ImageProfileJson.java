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

import com.redhat.rhn.domain.image.DockerfileProfile;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.KiwiProfile;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * The Image profile JSON class
 */
public class ImageProfileJson {

    private Long profileId;
    private String label;
    private String imageType;
    private String path;
    private String store;
    private ActivationKeyJson activationKey;
    private ChannelsJson channels;
    private Map<String, String> customData;

    /**
     * @return the profile id
     */
    public Long getProfileId() {
        return profileId;
    }

    /**
     * @param profileIdIn the profile id
     */
    public void setProfileId(Long profileIdIn) {
        this.profileId = profileIdIn;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param labelIn the label
     */
    public void setLabel(String labelIn) {
        this.label = labelIn;
    }

    /**
     * @return the image type
     */
    public String getImageType() {
        return imageType;
    }

    /**
     * @param imageTypeIn the image type
     */
    public void setImageType(String imageTypeIn) {
        this.imageType = imageTypeIn;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param pathIn the path
     */
    public void setPath(String pathIn) {
        this.path = pathIn;
    }

    /**
     * @return the store
     */
    public String getStore() {
        return store;
    }

    /**
     * @param storeIn the store
     */
    public void setStore(String storeIn) {
        this.store = storeIn;
    }

    /**
     * @return the activation key
     */
    public ActivationKeyJson getActivationKey() {
        return activationKey;
    }

    /**
     * @param activationKeyIn the activation key
     */
    public void setActivationKey(ActivationKeyJson activationKeyIn) {
        this.activationKey = activationKeyIn;
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
     * @return the custom data
     */
    public Map<String, String> getCustomData() {
        return customData;
    }

    /**
     * @param customDataIn the custom data
     */
    public void setCustomData(Map<String, String> customDataIn) {
        this.customData = customDataIn;
    }

    /**
     * Creates a JSON object from an image profile
     *
     * @param profile the image profile
     * @return the image profile json
     */
    public static ImageProfileJson fromImageProfile(ImageProfile profile) {
        ImageProfileJson json = new ImageProfileJson();
        json.setProfileId(profile.getProfileId());
        json.setLabel(profile.getLabel());
        json.setImageType(profile.getImageType());
        json.setStore(profile.getTargetStore().getLabel());

        if (profile.getToken() != null) {
            ActivationKey ak = ActivationKeyFactory.lookupByToken(profile.getToken());
            json.setActivationKey(ActivationKeyJson.fromActivationKey(ak));
            json.setChannels(ChannelsJson.fromChannelSet(ak.getChannels()));
        }

        if (profile instanceof DockerfileProfile) {
            json.setPath(((DockerfileProfile) profile).getPath());
        }
        else if (profile instanceof KiwiProfile) {
            json.setPath(((KiwiProfile) profile).getPath());
        }

        json.setCustomData(profile.getCustomDataValues().stream().collect(Collectors.toMap(
                v -> v.getKey().getLabel(), v -> StringUtils.defaultString(v.getValue()))));

        return json;
    }
}
