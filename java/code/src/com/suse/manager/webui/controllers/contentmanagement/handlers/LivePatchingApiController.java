/**
 * Copyright (c) 2021 SUSE LLC
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
package com.suse.manager.webui.controllers.contentmanagement.handlers;

import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.user.User;
import com.suse.manager.model.products.Product;
import com.suse.manager.webui.utils.gson.ResultJson;
import org.apache.http.HttpStatus;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.util.Comparator;
import java.util.stream.Stream;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static java.util.stream.Collectors.toList;
import static spark.Spark.get;

/**
 * Spark controller ContentManagement Live Patching API
 */
public class LivePatchingApiController {

    private LivePatchingApiController() { }

    /**
     * JSON object for kernel versions
     */
    public static class KernelVersion {
        private Long id;
        private String version;

        /**
         * Initialize an object from an EVR
         * @param evr the kernel EVR
         */
        public KernelVersion(PackageEvr evr) {
            this.id = evr.getId();
            this.version = evr.toUniversalEvrString();
        }
    }

    /** Init routes for ContentManagement Live Patching Api.*/
    public static void initRoutes() {
        get("/manager/api/contentmanagement/livepatching/products",
                withUser(LivePatchingApiController::getSuseProducts));
        get("/manager/api/contentmanagement/livepatching/kernels/:productId",
                withUser(LivePatchingApiController::getKernelVersions));
    }

    /**
     * Get all SUSE root products that support live patching
     *
     * @param req the http request
     * @param res the http response
     * @param user the current user
     * @return the JSON data
     */
    public static String getSuseProducts(Request req, Response res, User user) {
            Stream<Product> suseProducts = SUSEProductFactory.getLivePatchSupportedProducts()
                    .map(p -> new Product(p.getId(), p.getFriendlyName()));

            return json(res, ResultJson.success(suseProducts.collect(toList())));
    }

    /**
     * Get all kernel versions contained in a SUSE root product
     *
     * @param req the http request
     * @param res the http response
     * @param user the current user
     * @return the JSON data
     */
    public static String getKernelVersions(Request req, Response res, User user) {
        SUSEProduct product;
        try {
            long productId = Long.parseLong(req.params("productId"));
            product = SUSEProductFactory.getProductById(productId);
        }
        catch (NumberFormatException e) {
            throw Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }

        // Get kernel versions ordered from latest to earliest
        Stream<KernelVersion> kernelsInProductTree =
                SUSEProductFactory.getKernelVersionsInProduct(product)
                        .sorted(Comparator.reverseOrder())
                        .map(KernelVersion::new);

        return json(res, ResultJson.success(kernelsInProductTree.collect(toList())));
    }
}
