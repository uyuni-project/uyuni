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
package com.redhat.rhn.frontend.xmlrpc.image.profile;

import com.redhat.rhn.domain.image.DockerfileProfile;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageProfileFactory;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ImageStoreFactory;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * ImageProfileHandler
 */
public class ImageProfileHandler extends BaseHandler {

    /**
     * List available Image Profile Types
     * @param loggedInUser The current user
     * @return Array of ImageProfileType strings
     *
     * @xmlrpc.doc List available Image Store Types
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.returntype #array_single("string", "imageProfileTypes")
     */
    public List<String> listImageProfileTypes(User loggedInUser) {
        ensureImageAdmin(loggedInUser);
        List<String> imageTypes = new ArrayList<>();
        imageTypes.add(ImageProfile.TYPE_DOCKERFILE);
        return imageTypes;
    }

    /**
     * Create a new Image Profile
     * @param loggedInUser the current User
     * @param label the label
     * @param type the profile type label
     * @param storeLabel the image store label
     * @param path the path or git uri to the source
     * @param activationkey the activation key which defines the channels
     * @return 1 on success
     *
     * @xmlrpc.doc Create a new Image Profile
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("string", "label")
     * @xmlrpc.param #param("string", "type")
     * @xmlrpc.param #param("string", "storeLabel")
     * @xmlrpc.param #param("string", "path")
     * @xmlrpc.param #param("string", "activationkey")
     * @xmlrpc.returntype #return_int_success()
     */
    public int create(User loggedInUser, String label, String type, String storeLabel,
            String path, String activationkey) {
        ensureImageAdmin(loggedInUser);

        if (!listImageProfileTypes(loggedInUser).contains(type)) {
            throw new IllegalArgumentException("type does not exist.");
        }
        ImageStore store;
        try {
            store = ImageStoreFactory.lookupBylabelAndOrg(storeLabel,
                    loggedInUser.getOrg()).get();
        }
        catch (NoSuchElementException e) {
            throw new IllegalArgumentException("image store does not exist.");
        }

        ActivationKey ak = ActivationKeyFactory.lookupByKey(activationkey);
        if (ak == null) {
            throw new IllegalArgumentException("activationkey does not exist.");
        }

        ImageProfile profile;
        if (ImageProfile.TYPE_DOCKERFILE.equals(type)) {

            DockerfileProfile dockerfileProfile = new DockerfileProfile();

            dockerfileProfile.setLabel(label);
            dockerfileProfile.setPath(path);
            dockerfileProfile.setTargetStore(store);
            dockerfileProfile.setOrg(loggedInUser.getOrg());
            dockerfileProfile.setToken(ak.getToken());

            profile = dockerfileProfile;
        }
        else {
            throw new UnsupportedOperationException();
        }
        ImageProfileFactory.save(profile);

        return 1;
    }
}
