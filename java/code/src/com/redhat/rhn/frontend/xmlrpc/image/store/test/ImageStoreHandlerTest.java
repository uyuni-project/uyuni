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
package com.redhat.rhn.frontend.xmlrpc.image.store.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ImageStoreFactory;
import com.redhat.rhn.domain.image.ImageStoreType;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchImageStoreException;
import com.redhat.rhn.frontend.xmlrpc.image.store.ImageStoreHandler;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ImageStoreHandlerTest extends BaseHandlerTestCase {

    private ImageStoreHandler handler = new ImageStoreHandler();

    @Test
    public void testListImageStoreTypes() throws Exception {
        List<ImageStoreType> types = handler.listImageStoreTypes(admin);
        assertFalse(types.isEmpty(), "No image store types found");
        assertTrue(types.stream().anyMatch(t -> t.equals(ImageStoreFactory.TYPE_REGISTRY)));
        assertTrue(types.stream().anyMatch(t -> t.equals(ImageStoreFactory.TYPE_OS_IMAGE)));
    }

    @Test
    public void testCreateImageStore() throws Exception {
        int ret = handler.create(admin, "registry.mgr", "registry.domain.top",
                ImageStoreFactory.TYPE_REGISTRY.getLabel(), null);
        assertEquals(1, ret);

        handler.listImageStores(admin);
        ImageStore store = handler.getDetails(admin, "registry.mgr");
        assertEquals("registry.mgr", store.getLabel());
        assertEquals("registry.domain.top", store.getUri());
        assertEquals(ImageStoreFactory.TYPE_REGISTRY, store.getStoreType());
        assertNull(store.getCreds(), "no credentials expected");
    }

    @Test
    public void testGetImageStore() throws Exception {
        int ret = handler.create(admin, "registry.mgr", "registry.domain.top",
                ImageStoreFactory.TYPE_REGISTRY.getLabel(), null);
        assertEquals(1, ret);

        // Try with no label
        try {
            handler.getDetails(admin, "");
            fail("Should throw InvalidParameterException.");
        }
        catch (InvalidParameterException e) {
            assertEquals("Label cannot be empty.", e.getMessage());
        }

        ImageStore store = handler.getDetails(admin, "registry.mgr");
        assertEquals("registry.mgr", store.getLabel());
        assertEquals("registry.domain.top", store.getUri());
        assertEquals(ImageStoreFactory.TYPE_REGISTRY, store.getStoreType());
        assertNull(store.getCreds(), "no credentials expected");
    }

    @Test
    public void testDeleteImageStore() throws Exception {
        int ret = handler.create(admin, "registry.mgr", "registry.domain.top",
                ImageStoreFactory.TYPE_REGISTRY.getLabel(), null);
        assertEquals(1, ret);

        ret = handler.delete(admin, "registry.mgr");
        assertEquals(1, ret);

        try {
            handler.getDetails(admin, "registry.mgr");
            fail("Should throw NoSuchImageStoreException.");
        }
        catch (NoSuchImageStoreException e) {
            assertContains(e.getMessage(), "registry.mgr");
            return;
        }
        assertFalse(true);
    }

    @Test
    public void testSetImageStore() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("username", "admin");
        params.put("password", "secret");
        int ret = handler.create(admin, "registry.mgr", "portus.domain.top",
                ImageStoreFactory.TYPE_REGISTRY.getLabel(), params);
        assertEquals(1, ret);

        ImageStore store = handler.getDetails(admin, "registry.mgr");
        assertEquals("registry.mgr", store.getLabel());
        assertEquals("portus.domain.top", store.getUri());
        assertEquals(ImageStoreFactory.TYPE_REGISTRY, store.getStoreType());
        assertNotNull(store.getCreds(), "credentials expected");

        Map<String, String> details = new HashMap<>();
        details.put("uri", "registry.domain.top");
        details.put("username", "");
        ret = handler.setDetails(admin, "registry.mgr", details);
        assertEquals(1, ret);

        store = handler.getDetails(admin, "registry.mgr");
        assertEquals("registry.mgr", store.getLabel());
        assertEquals("registry.domain.top", store.getUri());
        assertEquals(ImageStoreFactory.TYPE_REGISTRY, store.getStoreType());
        assertNull(store.getCreds(), "no credentials expected");

        details = new HashMap<>();
        details.put("username", "root");
        details.put("password", "verysecret");
        ret = handler.setDetails(admin, "registry.mgr", details);
        assertEquals(1, ret);

        store = handler.getDetails(admin, "registry.mgr");
        assertEquals("registry.mgr", store.getLabel());
        assertEquals("registry.domain.top", store.getUri());
        assertEquals(ImageStoreFactory.TYPE_REGISTRY, store.getStoreType());
        assertEquals("root", store.getCreds().getUsername());
        assertEquals("verysecret", store.getCreds().getPassword());
    }
}
