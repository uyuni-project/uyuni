/*
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

import static com.suse.manager.webui.utils.SparkApplicationHelper.result;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static java.util.stream.Collectors.toList;
import static spark.Spark.get;

import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.model.products.Product;
import com.suse.manager.webui.utils.gson.ResultJson;

import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import spark.Request;
import spark.Response;
import spark.Spark;

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
         *
         * @param evr the kernel EVR
         */
        public KernelVersion(PackageEvr evr) {
            this.id = evr.getId();
            this.version = evr.toUniversalEvrString();
        }
    }

    /**
     * JSON object for basic system ID and it's running kernel version
     */
    public static class System {
        private Long id;
        private String name;
        private String kernel;

        /**
         * Initialize an object from a server
         *
         * @param server the server
         */
        public System(Server server) {
            this.id = server.getId();
            this.name = server.getName();
            this.kernel = server.getRunningKernel();
        }
    }

    /** Init routes for ContentManagement Live Patching Api.*/
    public static void initRoutes() {
        get("/manager/api/contentmanagement/livepatching/products",
                withUser(LivePatchingApiController::getSuseProducts));
        get("/manager/api/contentmanagement/livepatching/systems",
                withUser(LivePatchingApiController::getSlesSystems));
        get("/manager/api/contentmanagement/livepatching/kernels/product/:productId",
                withUser(LivePatchingApiController::getProductKernels));
        get("/manager/api/contentmanagement/livepatching/kernels/system/:systemId",
                withUser(LivePatchingApiController::getSystemKernels));
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

            return result(res, ResultJson.success(suseProducts.collect(toList())), new TypeToken<>() { });
    }

    /**
     * Get all SLES systems available to user
     *
     * @param req the http request
     * @param res the http response
     * @param user the current user
     * @return the JSON data
     */
    public static String getSlesSystems(Request req, Response res, User user) {
        String query = StringUtils.defaultString(req.queryParams("q"));
        List<System> slesSystems = ServerFactory.querySlesSystems(query, 20, user)
                .map(System::new)
                .collect(Collectors.toList());

        return result(res, ResultJson.success(slesSystems), new TypeToken<>() { });
    }

    /**
     * Get all kernel versions contained in a SUSE root product
     *
     * @param req the http request
     * @param res the http response
     * @param user the current user
     * @return the JSON data
     */
    public static String getProductKernels(Request req, Response res, User user) {
        SUSEProduct product;
        try {
            long productId = Long.parseLong(req.params("productId"));
            product = SUSEProductFactory.getProductById(productId);
            if (product == null) {
                throw Spark.halt(HttpStatus.SC_NOT_FOUND);
            }
        }
        catch (NumberFormatException e) {
            throw Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }

        // Get kernel versions ordered from latest to earliest
        Stream<KernelVersion> kernelsInProductTree =
                SUSEProductFactory.getKernelVersionsInProduct(product)
                        .sorted(Comparator.reverseOrder())
                        .map(KernelVersion::new);

        return result(res, ResultJson.success(kernelsInProductTree.collect(toList())), new TypeToken<>() { });
    }

    /**
     * Get all kernel versions installed in a system
     *
     * @param req the http request
     * @param res the http response
     * @param user the current user
     * @return the JSON data
     */
    public static String getSystemKernels(Request req, Response res, User user) {
        Server server;
        try {
            long systemId = Long.parseLong(req.params("systemId"));
            server = ServerFactory.lookupByIdAndOrg(systemId, user.getOrg());
            if (server == null) {
                throw Spark.halt(HttpStatus.SC_NOT_FOUND);
            }
            SystemManager.ensureAvailableToUser(user, systemId);
        }
        catch (NumberFormatException e) {
            throw Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }
        catch (LookupException e) {
            throw Spark.halt(HttpStatus.SC_BAD_REQUEST, e.getLocalizedTitle());
        }

        // Get kernel versions ordered from latest to earliest
        Stream<KernelVersion> kernelsInSystem =
                ServerFactory.getInstalledKernelVersions(server)
                        .sorted(Comparator.reverseOrder())
                        .map(KernelVersion::new);

        return result(res, ResultJson.success(kernelsInSystem.collect(toList())), new TypeToken<>() { });
    }
}
