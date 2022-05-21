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
package com.redhat.rhn.frontend.xmlrpc.image.store;

import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ImageStoreFactory;
import com.redhat.rhn.domain.image.ImageStoreType;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchImageStoreException;

import com.suse.manager.api.ReadOnly;

import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ImageStoreHandler
 * @apidoc.namespace image.store
 * @apidoc.doc Provides methods to access and modify image stores.
 */
public class ImageStoreHandler extends BaseHandler {

    /**
     * Create a new Image Store
     * @param loggedInUser the current User
     * @param label the label
     * @param uri the uri
     * @param storeType the store type
     * @param credentials optional credentials
     * @return 1 on success
     *
     * @apidoc.doc Create a new image store
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "label")
     * @apidoc.param #param("string", "uri")
     * @apidoc.param #param("string", "storeType")
     * @apidoc.param #struct_desc("credentials", "optional")
     *   #prop("string", "username")
     *   #prop("string", "password")
     * #struct_desc_end()
     * @apidoc.returntype #return_int_success()
     */
    public int create(User loggedInUser, String label, String uri, String storeType,
            Map<String, String> credentials) {
        ensureImageAdmin(loggedInUser);
        if (StringUtils.isEmpty(label)) {
            throw new InvalidParameterException("Label cannot be empty.");
        }
        else if (ImageStoreFactory.lookupBylabelAndOrg(label, loggedInUser.getOrg()).isPresent()) {
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

        if (credentials != null) {
            imageStore.setCreds(ImageStoreFactory.createCredentials(credentials, st.get()));
        }
        ImageStoreFactory.save(imageStore);

        return 1;
    }

    /**
     * List available Image Store Types
     * @param loggedInUser The current user
     * @return Array of ImageStoreType objects
     *
     * @apidoc.doc List available image store types
     * @apidoc.param #session_key()
     * @apidoc.returntype #return_array_begin() $ImageStoreTypeSerializer #array_end()
     */
    @ReadOnly
    public List<ImageStoreType> listImageStoreTypes(User loggedInUser) {
        ensureImageAdmin(loggedInUser);
        return ImageStoreFactory.listImageStoreTypes();
    }

    /**
     * List all configured image stores visible for the logged in user
     * @param loggedInUser The current User
     * @return Array of ImageStore Objects
     *
     * @apidoc.doc List available image stores
     * @apidoc.param #session_key()
     * @apidoc.returntype #return_array_begin() $ImageStoreSerializer #array_end()
     */
    @ReadOnly
    public List<ImageStore> listImageStores(User loggedInUser) {
        ensureImageAdmin(loggedInUser);
        return ImageStoreFactory.listImageStores(loggedInUser.getOrg()).stream().collect(Collectors.toList());
    }

    /**
     * Get Image Store Details
     * @param loggedInUser The Current User
     * @param label the Image Store Label
     * @return ImageStore Object
     *
     * @apidoc.doc Get details of an image store
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "label")
     * @apidoc.returntype $ImageStoreSerializer
     */
    @ReadOnly
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
     * @apidoc.doc Delete an image store
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "label")
     * @apidoc.returntype #return_int_success()
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
     * @apidoc.doc Set details of an image store
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "label")
     * @apidoc.param
     *   #struct_desc("details", "image store details")
     *     #prop("string", "uri")
     *     #prop_desc("string", "username", "pass empty string to unset credentials")
     *     #prop("string", "password")
     *   #struct_end()
     * @apidoc.returntype #return_int_success()
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
