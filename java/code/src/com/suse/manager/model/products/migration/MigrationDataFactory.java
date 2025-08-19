/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.model.products.migration;

import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductExtension;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.product.SUSEProductSet;

import com.suse.utils.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory to create the API objects for the product migration feature.
 */
public class MigrationDataFactory {

    private final List<SUSEProductExtension> productExtensions;

    /**
     * Builds an instance of the factory.
     */
    public MigrationDataFactory() {
        this.productExtensions = SUSEProductFactory.findAllSUSEProductExtensions();
    }

    /**
     * Create a {@link MigrationTarget} from a set of products and the servers involved in the migration.
     * @param targetProductSet the product set
     * @return an instance of {@link MigrationTarget}
     */
    public MigrationTarget toMigrationTarget(SUSEProductSet targetProductSet) {
        MigrationProduct targetProduct = toMigrationProduct(targetProductSet);
        List<String> missingChannels = Collections.unmodifiableList(targetProductSet.getMissingChannels());

        // Get the unique serialized id of the product set
        String serializedId = targetProductSet.getSerializedProductIDs();

        return new MigrationTarget(serializedId, targetProduct, missingChannels);
    }

    /**
     * Create a {@link MigrationProduct} from a set of products.
     * @param productSet the product set
     * @return an instance of {@link MigrationProduct}
     */
    public MigrationProduct toMigrationProduct(SUSEProductSet productSet) {

        var extensionsMap = productExtensions.stream()
            .filter(extension -> extension.getRootProduct().equals(productSet.getBaseProduct()))
            .collect(Collectors.toMap(
                ext -> ext.getBaseProduct().getProductId(),
                ext -> List.of(ext.getExtensionProduct().getProductId()),
                Lists::union
            ));

        var addonsMap = productSet.getAddonProducts().stream()
            .collect(Collectors.toMap(SUSEProduct::getProductId, Function.identity()));

        return toMigrationProduct(productSet.getBaseProduct(), addonsMap, extensionsMap);
    }

    private static MigrationProduct toMigrationProduct(SUSEProduct product, Map<Long, SUSEProduct> addonsMap,
                                                       Map<Long, List<Long>> extensionsMap) {
        List<MigrationProduct> extensions = extensionsMap.getOrDefault(product.getProductId(), List.of()).stream()
            .flatMap(addonId -> Optional.ofNullable(addonsMap.get(addonId)).stream())
            .map(addonProduct -> toMigrationProduct(addonProduct, addonsMap, extensionsMap))
            .toList();

        return new MigrationProduct(product.getProductId(), product.getFriendlyName(), extensions);
    }

}
