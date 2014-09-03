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
package com.redhat.rhn.manager.setup;

import com.suse.manager.model.products.Product;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author duncan
 */
public class SCCProductSyncManager extends ProductSyncManager {

    public List<Product> getBaseProducts() throws ProductSyncManagerCommandException, ProductSyncManagerParseException {
        return new ArrayList<Product>();
    }

    public void addProducts(List<String> productIdents) throws ProductSyncManagerCommandException {
    }

    public void addProduct(String productIdent) throws ProductSyncManagerCommandException {
    }

    public void refreshProducts() throws ProductSyncManagerCommandException, InvalidMirrorCredentialException,
        ConnectionException {
    }
}
