/**
 * Copyright (c) 2018 SUSE LLC
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

package com.suse.manager.webui.controllers;

import com.redhat.rhn.domain.iss.IssFactory;
import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.manager.content.MgrSyncProductDto;
import com.redhat.rhn.taskomatic.TaskoFactory;
import com.redhat.rhn.taskomatic.domain.TaskoRun;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.suse.manager.model.products.Product;

import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import spark.ModelAndView;
import spark.Request;
import spark.Response;

/**
 * Controller class providing backend code for the Products
 */
public class ProductsController {

    private static final String ISS_MASTER = "issMaster";
    private static final String REFRESH_NEEDED = "refreshNeeded";
    private static final String REFRESH_RUNNING = "refreshRunning";

    private static Logger log = Logger.getLogger(ImageBuildController.class);

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Date.class, new ECMAScriptDateAdapter())
            .serializeNulls()
            .create();

    private ProductsController() { }

    /**
     * Displays the Products page
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView show(Request request, Response response, User user) {
        TaskoRun latestRun = TaskoFactory.getLatestRun("mgr-sync-refresh-bunch");

        Map<String, Object> data = new HashMap<>();

        data.put(ISS_MASTER, String.valueOf(IssFactory.getCurrentMaster() == null));
        data.put(REFRESH_NEEDED, String.valueOf(SCCCachingFactory.refreshNeeded()));
        data.put(REFRESH_RUNNING, String.valueOf(latestRun != null && latestRun.getEndTime() == null));

        return new ModelAndView(data, "products/show.jade");
    }

    /**
     * Returns JSON data for the SUSE products
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return JSON result of the API call
     */
    public static String data(Request request, Response response, User user) {
        Map<String, Object> data = new HashMap<>();
        try {
            ContentSyncManager csm = new ContentSyncManager();
            Collection<MgrSyncProductDto> products = csm.listProducts(csm.listChannels());
            List<Product> jsonProducts = products.stream().map(
                syncProduct -> new Product(
                    syncProduct.getId(),
                    syncProduct.getIdent(),
                    syncProduct.getFriendlyName(),
                    syncProduct.getArch(),
                    syncProduct.isRecommended()
                )
            ).collect(Collectors.toList());
            data.put("baseProducts", jsonProducts);
        }
        catch (Exception e) {
            log.error("Exception while rendering products: " + e.getMessage());
            data.put("error", "Exception while fetching products: " + e.getMessage());
        }

        response.type("application/json");
        return GSON.toJson(data);
    }
}
