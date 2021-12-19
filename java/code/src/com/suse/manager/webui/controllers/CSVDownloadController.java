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
import com.redhat.rhn.frontend.dto.VirtualSystemOverview;
import com.redhat.rhn.manager.system.SystemManager;

import java.io.StringWriter;
import java.util.Arrays;

import spark.Request;
import spark.Response;

/**
 * Controller class providing backend code for CSV downloads.
 */
public class CSVDownloadController {

    private CSVDownloadController() {
    }

    /**
     * Invoked from Router. Initialize routes for CSV downloads
     */
    public static void initRoutes() {
        get("/manager/systems/csv/virtualSystems", withUser(CSVDownloadController::virtualSystemsCSV));
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

        // write data to csv file using csvWriter
        CSVWriter csvWriterObj = new CSVWriter(new StringWriter());
        csvWriterObj.setColumns(Arrays.asList("name", "id", "securityErrata", "bugErrata", "enhancementErrata",
                "outdatedPackages", "lastCheckin", "entitlementLevel", "channelLabels"));
        try {
            csvWriterObj.write(virtual);
        }
        catch (Exception e) {
            System.out.println("err" + e);
        }

        response.header("Content-Disposition", "attachment; filename=\"virtual-systems.csv\"");
        response.header("Content-Length", Integer.toString(csvWriterObj.getContents().length()));
        response.type("text/csv");
        return csvWriterObj.getContents();
    }
}
