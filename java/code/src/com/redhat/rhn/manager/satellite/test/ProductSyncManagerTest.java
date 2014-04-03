package com.redhat.rhn.manager.satellite.test;

import com.redhat.rhn.manager.setup.ProductSyncManager;
import com.redhat.rhn.manager.setup.ProductSyncManagerException;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.model.products.Product;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

public class ProductSyncManagerTest extends TestCase {

    private ProductSyncManager helper;
    private String productsXml;

    public ProductSyncManagerTest(String name) throws ClassNotFoundException, IOException {
        super(name);
        productsXml = TestUtils.readAll(TestUtils.findTestData("mgr_ncc_sync_products.xml"));
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        helper = new ProductSyncManager();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        helper = null;
    }

    /**
     * Tests parsing of SUSE products
     * @throws ProductSyncManagerException if the xml cannot be parsed
     */
    public void testParseProducts() throws ProductSyncManagerException {
        Map<Product, List<Product>> productHierarchy = helper.parseProducts(productsXml);

        List<Product> baseProducts = new LinkedList<Product>(productHierarchy.keySet());
        assertEquals(3, baseProducts.size());
        assertEquals("res-4-i386-1321-rhel-i386-es-4", baseProducts.get(0).getIdent());
        assertEquals("res-4-x86_64-1321-rhel-x86_64-as-4", baseProducts.get(1).getIdent());
        assertEquals("res-6-x86_64-2580-rhel-x86_64-server-6",
                     baseProducts.get(2).getIdent());

        assertTrue(productHierarchy.get(baseProducts.get(0)).isEmpty());
        assertTrue(productHierarchy.get(baseProducts.get(1)).isEmpty());

        List<Product> res6Addons = productHierarchy.get(baseProducts.get(2));
        assertEquals(1, res6Addons.size());
        assertEquals("rhel-6-expanded-support-x86_64-3200-rhel-x86_64-server-6",
                     res6Addons.get(0).getIdent());
    }

}
