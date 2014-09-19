/**
 * Copyright (c) 2014 SUSE
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

package com.redhat.rhn.manager.setup.test;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.manager.satellite.Executor;
import com.redhat.rhn.manager.setup.NCCProductSyncManager;
import com.redhat.rhn.manager.setup.ProductSyncManager;
import com.redhat.rhn.taskomatic.TaskoRun;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import com.suse.manager.model.products.Channel;
import com.suse.manager.model.products.Product;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import static junit.framework.Assert.assertEquals;

import static junit.framework.Assert.assertEquals;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

/**
 *
 * @author bo
 */
public class SCCProductSyncManagerTest extends BaseProductSyncManagerTestCase {
    /**
     * {@inheritDoc}}
     * @throws java.lang.Exception
     */
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    protected Product getProductWithAllChannelsProvided() throws Exception {
        List<Product> parsedProds = getProducts().getProducts();

        Product prod = null;
        for (Product p : parsedProds) {
            if (p.getIdent().equals(this.getProvidedProductIdent())) {
                prod = p;
            }
        }
        return prod;
    }

    /**
     * Tests refreshProducts().
     * @throws Exception if anything goes wrong
     */
    public void testConvertProduct() throws Exception {

        Product p = getProductWithAllChannelsProvided();
        productGenerateFakeMetadata(p.getIdent());

        Channel last = p.getMandatoryChannels().get(p.getMandatoryChannels().size() - 1);
        com.redhat.rhn.domain.channel.Channel rhnCh = ChannelFactory.lookupByLabel(last.getLabel());

        // set the last sync date of the last channel to some months ago
        rhnCh.setLastSynced(new Date(System.currentTimeMillis() - (60 * 1000 * 60 * 60 * 24)));

        ProductSyncManager productSyncManager = ProductSyncManager.createInstanceFromImpl("SCC");
        //Method method = ProductSyncManager.class.getDeclaredMethod("getProductSyncStatus", Product.class);
        //method.setAccessible(true);
        //Product.SyncStatus status = (Product.SyncStatus) method.invoke(productSyncManager, p);
        Product.SyncStatus status = productSyncManager.getProductSyncStatus(p);
        assertEquals(status.FINISHED, status);
        assertEquals(new Date(), status.getLastSyncDate());
    }
}
