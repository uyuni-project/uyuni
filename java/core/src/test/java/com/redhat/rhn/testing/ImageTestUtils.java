/*
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

package com.redhat.rhn.testing;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.RegistryCredentials;
import com.redhat.rhn.domain.image.DockerfileProfile;
import com.redhat.rhn.domain.image.ImageFile;
import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.image.ImageInfoCustomDataValue;
import com.redhat.rhn.domain.image.ImagePackage;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ImageStoreFactory;
import com.redhat.rhn.domain.image.ImageStoreType;
import com.redhat.rhn.domain.image.KiwiProfile;
import com.redhat.rhn.domain.image.ProfileCustomDataValue;
import com.redhat.rhn.domain.org.CustomDataKey;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;

import java.util.HashSet;
import java.util.Set;

/**
 * Utility methods for testing of the image features.
 */
public class ImageTestUtils {

    private ImageTestUtils() { }

    /**
     * Create an image info.
     *
     * @param profileLabel the profile label
     * @param version      the version
     * @param store        the store
     * @param user         the user
     * @return the image info
     */
    public static ImageInfo createImageInfo(String profileLabel, String version,
            ImageStore store, User user) {
        ImageInfo inf = new ImageInfo();
        inf.setName(profileLabel);
        inf.setVersion(version);
        inf.setImageType(ImageProfile.TYPE_DOCKERFILE);
        inf.setChecksum(null);
        inf.setImageArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
        inf.setOrg(user.getOrg());
        inf.setStore(store);
        TestUtils.saveAndFlush(inf);
        return inf;
    }

    /**
     * Create an image info.
     *
     * @param profile   the profile
     * @param buildHost the build host
     * @param version   the version
     * @param user      the user
     * @return the image info
     */
    public static ImageInfo createImageInfo(ImageProfile profile, MinionServer buildHost,
            String version, User user) {
        ImageInfo info = new ImageInfo();
        info.setName(profile.getLabel());
        info.setVersion(version);
        info.setImageType(profile.getImageType());
        info.setStore(profile.getTargetStore());
        info.setOrg(user.getOrg());
        info.setProfile(profile);
        info.setImageType(profile.getImageType());
        info.setBuildServer(buildHost);

        if (profile.getToken() != null) {
            info.setChannels(new HashSet<>(profile.getToken().getChannels()));
        }

        info.setImageArch(buildHost.getServerArch());

        if (profile.getCustomDataValues() != null) {
            profile.getCustomDataValues().forEach(cdv -> info.getCustomDataValues()
                    .add(new ImageInfoCustomDataValue(cdv, info)));
        }

        TestUtils.saveAndFlush(info);
        return info;
    }

    /**
     * Create an image info.
     * @param channels the channels used for building the image
     * @param user the user
     * @param built mark the image as built
     * @return the image info
     */
    public static ImageInfo createImageInfo(Set<Channel> channels, User user, boolean built) {
        return createImageInfo("image-" + TestUtils.randomString(), "latest", channels,
                user, built);
    }

    /**
     * Create an image info.
     * @param name the name of the image
     * @param version the version of the image
     * @param channels the channels used for building the image
     * @param user the user
     * @param built mark the image as built
     * @return the image info
     */
    public static ImageInfo createImageInfo(String name, String version,
            Set<Channel> channels, User user, boolean built) {
        ImageInfo image = new ImageInfo();
        image.setName(name);
        image.setVersion(version);
        image.setImageType(ImageProfile.TYPE_DOCKERFILE);
        image.setImageArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
        image.setOrg(user.getOrg());
        image.setChannels(channels);
        image.setBuilt(built);
        TestUtils.saveAndFlush(image);
        return image;
    }

    /**
     * Create an image info.
     * @param name the name of the image
     * @param version the version of the image
     * @param user the user
     * @return the image info
     */
    public static ImageInfo createImageInfo(String name, String version, User user) {
        return createImageInfo(name, version, (Set<Channel>)null, user, false);
    }

    /**
     * Create a custom data value for a profile.
     *
     * @param value   the value
     * @param key     the key
     * @param profile the profile
     * @param user    the user
     * @return the profile custom data value
     */
    public static ProfileCustomDataValue createProfileCustomDataValue(String value,
            CustomDataKey key, ImageProfile profile, User user) {
        ProfileCustomDataValue val = new ProfileCustomDataValue();
        val.setCreator(user);
        val.setLastModifier(user);
        val.setKey(key);
        val.setProfile(profile);
        val.setValue(value);
        TestUtils.saveAndFlush(val);

        if (profile.getCustomDataValues() == null) {
            profile.setCustomDataValues(new HashSet<>());
        }

        profile.getCustomDataValues().add(val);
        TestUtils.saveAndFlush(profile);

        return val;
    }

    /**
     * Create a custom data value for an image info.
     *
     * @param value the value
     * @param key   the key
     * @param info  the info
     * @param user  the user
     * @return the image info custom data value
     */
    public static ImageInfoCustomDataValue createImageInfoCustomDataValue(String value,
            CustomDataKey key, ImageInfo info, User user) {
        ImageInfoCustomDataValue val = new ImageInfoCustomDataValue();
        val.setImageInfo(info);
        val.setKey(key);
        val.setValue(value);
        val.setCreator(user);
        val.setLastModifier(user);
        TestUtils.saveAndFlush(val);

        if (info.getCustomDataValues() == null) {
            info.setCustomDataValues(new HashSet<>());
        }

        info.getCustomDataValues().add(val);
        TestUtils.saveAndFlush(info);

        return val;
    }

    /**
     * Create an image profile.
     *
     * @param label the label
     * @param store the store
     * @param user  the user
     * @return the image profile
     */
    public static ImageProfile createImageProfile(String label, ImageStore store,
            User user) {
        DockerfileProfile profile = new DockerfileProfile();
        profile.setLabel(label);
        profile.setOrg(user.getOrg());
        profile.setPath("my/test/path");
        profile.setTargetStore(store);
        TestUtils.saveAndFlush(profile);
        return profile;
    }

    /**
     * Create an image profile.
     *
     * @param label the label
     * @param store the store
     * @param key   the key
     * @param user  the user
     * @return the image profile
     */
    public static ImageProfile createImageProfile(String label, ImageStore store,
            ActivationKey key, User user) {
        DockerfileProfile profile = new DockerfileProfile();
        profile.setLabel(label);
        profile.setOrg(user.getOrg());
        profile.setPath("my/test/path");
        profile.setTargetStore(store);
        if (key != null) {
            profile.setToken(key.getToken());
        }
        TestUtils.saveAndFlush(profile);
        return profile;
    }

    /**
     * Create a Kiwi image profile.
     *
     * @param label the label
     * @param key   the key
     * @param user  the user
     * @return the image profile
     */
    public static KiwiProfile createKiwiImageProfile(String label, ActivationKey key, User user) {
        KiwiProfile profile = new KiwiProfile();
        profile.setLabel(label);
        profile.setOrg(user.getOrg());
        profile.setPath("my/test/path");
        profile.setTargetStore(ImageStoreFactory
                .lookupBylabelAndOrg("SUSE Manager OS Image Store", user.getOrg()).get());
        profile.setToken(key.getToken());

        TestUtils.saveAndFlush(profile);
        return profile;
    }

    /**
     * Create an activation key.
     *
     * @param user the user
     * @return the activation key
     * @throws Exception exception
     */
    public static ActivationKey createActivationKey(User user) throws Exception {
        Channel baseChannel = ChannelTestUtils.createBaseChannel(user);
        Channel childChannel = ChannelTestUtils.createChildChannel(user, baseChannel);
        Set<Channel> channels = new HashSet<>();
        channels.add(baseChannel);
        channels.add(childChannel);
        ActivationKey key = ActivationKeyFactory.createNewKey(user, "mykey");
        key.setChannels(channels);
        key.setBaseChannel(baseChannel);
        TestUtils.saveAndFlush(key);
        return key;
    }

    /**
     * Create image store credentials.
     *
     * @return the credentials
     */
    public static RegistryCredentials createCredentials() {
        RegistryCredentials registryCredentials = CredentialsFactory.createRegistryCredentials("testuser", "testpass");
        CredentialsFactory.storeCredentials(registryCredentials);
        return registryCredentials;
    }

    /**
     * Create an image store.
     *
     * @param label the label
     * @param user  the user
     * @return the image store
     */
    public static ImageStore createImageStore(String label, User user) {
        return createImageStore(label, user, ImageStoreFactory.TYPE_REGISTRY);
    }

    /**
     * Create an image store.
     *
     * @param label the label
     * @param creds the credentials
     * @param user  the user
     * @return the image store
     */
    public static ImageStore createImageStore(String label, RegistryCredentials creds, User user) {
        return createImageStore(label, creds, user, ImageStoreFactory.TYPE_REGISTRY);
    }

    /**
     * Create an image store.
     *
     * @param label the label
     * @param user  the user
     * @param type  the type
     * @return the image store
     */
    public static ImageStore createImageStore(String label, User user, ImageStoreType type) {
        return createImageStore(label, null, user, type);
    }

    /**
     * Create an image store.
     *
     * @param label the label
     * @param creds the credentials
     * @param user  the user
     * @param type  the type
     * @return the image store
     */
    public static ImageStore createImageStore(String label, RegistryCredentials creds, User user, ImageStoreType type) {
        ImageStore store = new ImageStore();
        store.setLabel(label);
        store.setUri("my.store.uri");
        store.setStoreType(type);
        store.setOrg(user.getOrg());

        if (creds != null) {
            store.setCreds(creds);
        }

        TestUtils.saveAndFlush(store);
        return store;
    }

    /**
     * Create an {@link ImagePackage} (reification of the installation of a
     * package onto an image).
     * @param packageIn the package to install
     * @param image the image
     * @return the newly created installed package
     */
    public static ImagePackage createImagePackage(Package packageIn,
            ImageInfo image) {
        ImagePackage result = new ImagePackage();
        result.setEvr(packageIn.getPackageEvr());
        result.setArch(packageIn.getPackageArch());
        result.setName(packageIn.getPackageName());
        result.setImageInfo(image);
        TestUtils.saveAndFlush(result);

        return result;
    }

    /**
     * Create a {@link MinionServer} with Container Build Host entitlement.
     *
     * @param entitlementManager system entitlement manager
     * @param user the user
     * @return the minion server
     * @throws Exception the exception
     */
    public static MinionServer createBuildHost(SystemEntitlementManager entitlementManager, User user)
            throws Exception {
        MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);
        entitlementManager.addEntitlementToServer(server, EntitlementManager.CONTAINER_BUILD_HOST);
        return server;
    }

    /**
     * Test create image file
     * @param image
     * @param filename
     * @param type
     */
    public static void createImageFile(ImageInfo image, String filename, String type) {
        ImageFile file = new ImageFile();
        file.setFile(filename);
        file.setType(type);
        file.setImageInfo(image);
        image.getImageFiles().add(file);
    }
}
