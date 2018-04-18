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
package com.redhat.rhn.frontend.xmlrpc.image.store;

import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ImageStoreFactory;
import com.redhat.rhn.domain.image.ImageStoreType;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchImageStoreException;

import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ImageStoreHandler
 * @xmlrpc.namespace image.store
 * @xmlrpc.doc Provides methods to access and modify image stores.
 */
public class ImageStoreHandler extends BaseHandler {

    /**
     * Create a new Image Store
     * @param loggedInUser the current User
     * @param label the label
     * @param uri the uri
     * @param storeType the store type
     * @param parameters optional credentials
     * @return 1 on success
     *
     * @xmlrpc.doc Create a new Image Store
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("string", "label")
     * @xmlrpc.param #param("string", "uri")
     * @xmlrpc.param #param("string", "storeType")
     * @xmlrpc.param #struct_desc("credentials", "optional")
     *   #prop("string", "username")
     *   #prop("string", "password")
     * #struct_desc_end()
     * @xmlrpc.returntype #return_int_success()
     */
    public int create(User loggedInUser, String label, String uri, String storeType,
            Map<String, String> parameters) {
        ensureImageAdmin(loggedInUser);
        if (StringUtils.isEmpty(label)) {
            throw new InvalidParameterException("Label cannot be empty.");
        }
        else if (ImageStoreFactory.lookupBylabel(label).isPresent()) {
            throw new InvalidParameterException("Image store already exists.");
        }
        if (StringUtils.isEmpty(uri)) {
            throw new InvalidParameterException("Uri cannot be empty.");
        }
        Optional<ImageStoreType> st = ImageStoreFactory.lookupStoreTypeByLabel(storeType);
        if (!st.isPresent()) {
            throw new InvalidParameterException("Unknown image store type: " + storeType);
        }
        ImageStore imageStore = new ImageStore();
        imageStore.setLabel(label);
        imageStore.setUri(uri);
        imageStore.setStoreType(st.get());
        imageStore.setOrg(loggedInUser.getOrg());

        if (parameters != null) {
            imageStore.setCreds(ImageStoreFactory.createCredentials(parameters, st.get()));
        }
        ImageStoreFactory.save(imageStore);

        return 1;
    }

    /**
     * List available Image Store Types
     * @param loggedInUser The current user
     * @return Array of ImageStoreType objects
     *
     * @xmlrpc.doc List available Image Store Types
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.returntype #array() $ImageStoreTypeSerializer #array_end()
     */
    public List<ImageStoreType> listImageStoreTypes(User loggedInUser) {
        ensureImageAdmin(loggedInUser);
        return ImageStoreFactory.listImageStoreTypes();
    }

    /**
     * List all configured image stores visible for the logged in user
     * @param loggedInUser The current User
     * @return Array of ImageStore Objects
     *
     * @xmlrpc.doc List available Image Stores
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.returntype #array() $ImageStoreSerializer #array_end()
     */
    public List<ImageStore> listImageStores(User loggedInUser) {
        ensureImageAdmin(loggedInUser);
        // Only list 'registry' stores
        return ImageStoreFactory.listImageStores(loggedInUser.getOrg()).stream()
                .filter(s -> s.getStoreType().equals(ImageStoreFactory.TYPE_REGISTRY))
                .collect(Collectors.toList());
    }

    /**
     * Get Image Store Details
     * @param loggedInUser The Current User
     * @param label the Image Store Label
     * @return ImageStore Object
     *
     * @xmlrpc.doc Get details of an Image Store
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("string", "label")
     * @xmlrpc.returntype $ImageStoreSerializer
     */
    public ImageStore getDetails(User loggedInUser, String label) {
        ensureImageAdmin(loggedInUser);

        if (StringUtils.isEmpty(label)) {
            throw new InvalidParameterException("Label cannot be empty.");
        }

        Optional<ImageStore> store = ImageStoreFactory.lookupBylabelAndOrg(label,
                loggedInUser.getOrg());
        if (!store.isPresent()) {
            throw new NoSuchImageStoreException(label);
        }
        return store.get();
    }

    /**
     * Delete an Image Store
     * @param loggedInUser The current User
     * @param label the image store label
     * @return i on success
     *
     * @xmlrpc.doc Delete an Image Store
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("string", "label")
     * @xmlrpc.returntype #return_int_success()
     */
    public int delete(User loggedInUser, String label) {
        ensureImageAdmin(loggedInUser);
        Optional<ImageStore> store = ImageStoreFactory.lookupBylabelAndOrg(label,
                loggedInUser.getOrg());
        if (!store.isPresent()) {
            throw new NoSuchImageStoreException(label);
        }
        ImageStoreFactory.delete(store.get());
        return 1;
    }

    /**
     * Set details of an Image Store
     * @param loggedInUser the current User
     * @param label the label
     * @param details A map containing the new details
     * @return 1 on success
     *
     * @xmlrpc.doc Set details of an Image Store
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("string", "label")
     * @xmlrpc.param
     *   #struct("image store details")
     *     #prop("string", "uri")
     *     #prop_desc("string", "username", "pass empty string to unset credentials")
     *     #prop("string", "password")
     *   #struct_end()
     * @xmlrpc.returntype #return_int_success()
     */
    public int setDetails(User loggedInUser, String label, Map details) {
        ensureImageAdmin(loggedInUser);
        Set<String> validKeys = new HashSet<>();
        validKeys.add("uri");
        validKeys.add("username");
        validKeys.add("password");
        validateMap(validKeys, details);

        if (StringUtils.isEmpty(label)) {
            throw new InvalidParameterException("Label cannot be empty.");
        }
        Optional<ImageStore> optstore = ImageStoreFactory.lookupBylabelAndOrg(label,
                loggedInUser.getOrg());
        if (!optstore.isPresent()) {
            throw new NoSuchImageStoreException(label);
        }
        ImageStore store = optstore.get();

        if (details.containsKey("uri")) {
            store.setUri((String) details.get("uri"));
        }
        if (details.containsKey("username")) {
            store.setCreds(ImageStoreFactory.createCredentials(details,
                    store.getStoreType()));
        }
        ImageStoreFactory.save(store);
        return 1;
    }
}
