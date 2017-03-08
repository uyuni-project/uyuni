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
package com.redhat.rhn.frontend.xmlrpc.image.store.test;

import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ImageStoreType;
import com.redhat.rhn.frontend.xmlrpc.NoSuchImageStoreException;
import com.redhat.rhn.frontend.xmlrpc.image.store.ImageStoreHandler;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ImageStoreHandlerTest extends BaseHandlerTestCase {

    private ImageStoreHandler handler = new ImageStoreHandler();

    public void testListImageStoreTypes() throws Exception {
        List<ImageStoreType> types = handler.listImageStoreTypes(admin);
        assertFalse("No image store types found", types.isEmpty());
        assertEquals(ImageStore.TYPE_REGISTRY, types.get(0).getLabel());
    }

    public void testCreateImageStore() throws Exception {
        int ret = handler.create(admin, "registry.mgr", "registry.domain.top",
                ImageStore.TYPE_REGISTRY, null);
        assertEquals(1, ret);

        List<ImageStore> stores = handler.listImageStores(admin);
        assertTrue(stores.size() == 1);
        ImageStore store = stores.get(0);
        assertEquals("registry.mgr", store.getLabel());
        assertEquals("registry.domain.top", store.getUri());
        assertEquals(ImageStore.TYPE_REGISTRY, store.getStoreType().getLabel());
        assertNull("no credentials expected", store.getCreds());
    }

    public void testGetImageStore() throws Exception {
        int ret = handler.create(admin, "registry.mgr", "registry.domain.top",
                ImageStore.TYPE_REGISTRY, null);
        assertEquals(1, ret);

        ImageStore store = handler.getDetails(admin, "registry.mgr");
        assertEquals("registry.mgr", store.getLabel());
        assertEquals("registry.domain.top", store.getUri());
        assertEquals(ImageStore.TYPE_REGISTRY, store.getStoreType().getLabel());
        assertNull("no credentials expected", store.getCreds());
    }

    public void testDeleteImageStore() throws Exception {
        int ret = handler.create(admin, "registry.mgr", "registry.domain.top",
                ImageStore.TYPE_REGISTRY, null);
        assertEquals(1, ret);

        ret = handler.deleteImageStore(admin, "registry.mgr");
        assertEquals(1, ret);

        try {
            handler.getDetails(admin, "registry.mgr");
        }
        catch (NoSuchImageStoreException e) {
            assertContains(e.getMessage(), "registry.mgr");
            return;
        }
        assertFalse(true);
    }

    public void testSetImageStore() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("username", "admin");
        params.put("password", "secret");
        int ret = handler.create(admin, "registry.mgr", "portus.domain.top",
                ImageStore.TYPE_REGISTRY, params);
        assertEquals(1, ret);

        ImageStore store = handler.getDetails(admin, "registry.mgr");
        assertEquals("registry.mgr", store.getLabel());
        assertEquals("portus.domain.top", store.getUri());
        assertEquals(ImageStore.TYPE_REGISTRY, store.getStoreType().getLabel());
        assertNotNull("credentials expected", store.getCreds());

        Map<String, String> details = new HashMap<>();
        details.put("uri", "registry.domain.top");
        details.put("username", "");
        ret = handler.setDetails(admin, "registry.mgr", details);
        assertEquals(1, ret);

        store = handler.getDetails(admin, "registry.mgr");
        assertEquals("registry.mgr", store.getLabel());
        assertEquals("registry.domain.top", store.getUri());
        assertEquals(ImageStore.TYPE_REGISTRY, store.getStoreType().getLabel());
        assertNull("no credentials expected", store.getCreds());

        details = new HashMap<>();
        details.put("username", "root");
        details.put("password", "verysecret");
        ret = handler.setDetails(admin, "registry.mgr", details);
        assertEquals(1, ret);

        store = handler.getDetails(admin, "registry.mgr");
        assertEquals("registry.mgr", store.getLabel());
        assertEquals("registry.domain.top", store.getUri());
        assertEquals(ImageStore.TYPE_REGISTRY, store.getStoreType().getLabel());
        assertEquals("root", store.getCreds().getUsername());
        assertEquals("verysecret", store.getCreds().getPassword());
    }
}
