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

import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * ImageProfileHandler
 */
public class ImageProfileHandler extends BaseHandler {

    /**
     * List available Image Store Types
     * @param loggedInUser The current user
     * @return Array of ImageStoreType objects
     *
     * @xmlrpc.doc List available Image Store Types
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.returntype #array() $ImageStoreTypeSerializer #array_end()
     */
    public List<String> listImageTypes(User loggedInUser) {
        ensureImageAdmin(loggedInUser);
        List<String> imageTypes = new ArrayList<>();
        imageTypes.add(ImageProfile.TYPE_DOCKERFILE);
        return imageTypes;
    }

}
