/*
 * Copyright (c) 2018--2021 SUSE LLC
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

import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static spark.Spark.get;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.util.CSVWriter;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.frontend.dto.VirtualSystemOverview;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.webui.utils.PageControlHelper;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import spark.Request;
import spark.Response;
import spark.Spark;

/**
 * Controller class providing backend code for CSV downloads.
 */
public class CSVDownloadController {

    private static final Logger LOG = LogManager.getLogger(CSVDownloadController.class);

    private CSVDownloadController() {
    }

    /**
     * Invoked from Router. Initialize routes for CSV downloads
     */
    public static void initRoutes() {
        get("/manager/systems/csv/virtualSystems", withUser(CSVDownloadController::virtualSystemsCSV));
        get("/manager/systems/csv/all", withUser(CSVDownloadController::allSystemsCSV));
    }

    /**
     * Download virtual systems list CSV
     *
     * @param request  the http request
     * @param response the http response
     * @param user     the user
     * @return the json response
     */
    public static String virtualSystemsCSV(Request request, Response response, User user) {
        // Querying the data again may not be optimal... but caching them in the
        // session may grow big!
        DataResult<VirtualSystemOverview> virtual = SystemManager.virtualSystemsList(user, null);
        virtual.elaborate();

        List<String> columns = Arrays.asList("name", "id", "securityErrata", "bugErrata", "enhancementErrata",
                "outdatedPackages", "lastCheckin", "entitlementLevel", "channelLabels");
        return writeCsv(response, virtual, columns, "virtual-systems.csv");
    }

    /**
     * Download all systems list CSV
     *
     * @param request  the http request
     * @param response the http response
     * @param user     the user
     * @return the json response
     */
    public static String allSystemsCSV(Request request, Response response, User user) {
        PageControlHelper pageHelper = new PageControlHelper(request, "server_name");
        PageControl pc = pageHelper.getPageControl();
        pc.setStart(1);
        pc.setPageSize(0);

        var parser = SystemsController.getFilterParser(pc);
        DataResult<SystemOverview> all = SystemManager.systemListNew(user, parser, pc);

        List<String> columns = Arrays.asList("serverName", "id", "securityErrata", "bugErrata", "enhancementErrata",
                "outdatedPackages", "extraPkgCount", "configFilesWithDifferences", "lastCheckin", "entitlementLevel",
                "channelLabels", "proxy", "mgrServer", "virtualHost", "virtualGuest", "requiresReboot",
                "statusType");
        return writeCsv(response, all, columns, "systems.csv");
    }

    private static String writeCsv(Response response, List<?> data, List<String> columns, String filename) {
        CSVWriter csvWriterObj = new CSVWriter(new StringWriter());
        csvWriterObj.setColumns(columns);
        try {
            csvWriterObj.write(data);
        }
        catch (Exception e) {
            LOG.error("Failed to write CSV", e);
            Spark.halt(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        response.header("Content-Disposition", String.format("attachment; filename=\"%s\"", filename));
        response.header("Content-Length", Integer.toString(csvWriterObj.getContents().length()));
        response.type("text/csv");
        return csvWriterObj.getContents();
    }
}
