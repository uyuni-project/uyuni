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

package com.suse.manager.webui.controllers.reporting;

import static com.suse.manager.webui.utils.SparkApplicationHelper.asJson;
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withDocsLocale;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserPreferences;
import static spark.Spark.get;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.manager.report.InventoryReport;
import com.redhat.rhn.manager.report.dto.SystemInventoryOverview;

import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.utils.PagedSqlQueryBuilder;
import com.suse.manager.webui.utils.PageControlHelper;
import com.suse.manager.webui.utils.gson.PagedDataResultJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.jade.JadeTemplateEngine;

/**
 * Controller class providing backend code for the systems page.
 */
public class ReportsController {

    // Logger for this class
    private static final Logger LOG = LogManager.getLogger(ReportsController.class);

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .serializeNulls()
            .create();

    /**
     * Invoked from Router. Initialize routes for Systems Views.
     *
     * @param jade Jade template engine
     */
    public void initRoutes(JadeTemplateEngine jade) {
        get("/manager/report/inventory",
                withUserPreferences(withCsrfToken(withDocsLocale(withUser(this::inventoryReportPage)))), jade);
        get("/manager/api/report/inventory", asJson(withUser(this::inventoryReport)));
        //get("/manager/api/report/inventory/csv", asJson(withUser(this::inventoryCSV)));
        get("/manager/report/cvesearch",
                withUserPreferences(withCsrfToken(withDocsLocale(withUser(this::cveSearchReportPage)))), jade);
    }

    private Object inventoryReport(Request request, Response response, User user) {
        PageControlHelper pageHelper = new PageControlHelper(request, "profile_name");
        PageControl pc = pageHelper.getPageControl();

        Map<String, Function<Optional<PageControl>, PagedSqlQueryBuilder.FilterWithValue>> mapping = Map.of(
                "packages_out_of_date", PagedSqlQueryBuilder::parseFilterAsNumber,
                "total_errata_count",
                pageControl -> {
                    pageControl.ifPresent(c -> c.setFilterColumn("errata_out_of_date"));
                    return PagedSqlQueryBuilder.parseFilterAsNumber(pageControl);
                }
        );

        Function<Optional<PageControl>, PagedSqlQueryBuilder.FilterWithValue> parser =
                PagedSqlQueryBuilder::parseFilterAsText;

        if (pc.getFilterColumn() != null && mapping.containsKey(pc.getFilterColumn())) {
            parser = mapping.get(pc.getFilterColumn());
        }

        DataResult<SystemInventoryOverview> systems = InventoryReport.getSystemsInventory(user,
                parser, pc);

        return json(response, new PagedDataResultJson<>(systems, systems.getTotalSize(), Set.of()));
    }

//    public String inventoryCSV(Request request, Response response, User user) {
//        // Querying the data again may not be optimal... but caching them in the
//        // session may grow big!
//        DataResult<SystemInventoryOverview> systems = InventoryReport.getSystemsInventory(user,
//                PagedSqlQueryBuilder::parseFilterAsText, null);
//
//        List<String> columns = Arrays.asList("mgmId", "systemId", "minionId", "machineId", "profileName",
//                "hostname", "lastCheckinTime", "syncedDate", "kernelVersion",
//                "packagesOutOfDate", "errataOutOfDate", "organization", "architecture");
//        return writeCsv(response, systems, columns, "virtual-systems.csv");
//    }
//
//    private static String writeCsv(Response response, List<?> data, List<String> columns, String filename) {
//        CSVWriter csvWriterObj = new CSVWriter(new StringWriter());
//        csvWriterObj.setColumns(columns);
//        try {
//            csvWriterObj.write(data);
//        }
//        catch (Exception e) {
//            LOG.error("Failed to write CSV", e);
//            Spark.halt(HttpStatus.SC_INTERNAL_SERVER_ERROR);
//        }
//
//        response.header("Content-Disposition", String.format("attachment; filename=\"%s\"", filename));
//        response.header("Content-Length", Integer.toString(csvWriterObj.getContents().length()));
//        response.type("text/csv");
//        return csvWriterObj.getContents();
//    }

    /**
     * Get the all systems list page
     *
     * @param requestIn the request
     * @param responseIn the response
     * @param userIn the user
     * @return the jade rendered template
     */
    private ModelAndView inventoryReportPage(Request requestIn, Response responseIn, User userIn) {
        Map<String, Object> data = new HashMap<>();

        String filterColumn = requestIn.queryParams("qc");
        String filterQuery = requestIn.queryParams("q");

        data.put("query", filterQuery != null ? String.format("'%s'", filterQuery) : "null");
        data.put("queryColumn", filterColumn != null ? String.format("'%s'", filterColumn) : "null");
        return new ModelAndView(data, "templates/reporting/inventory.jade");
    }
    /**
     * System systems potentially affected by a CVE
     *
     * @param requestIn the request
     * @param responseIn the response
     * @param userIn the user
     * @return the jade rendered template
     */
    private ModelAndView cveSearchReportPage(Request requestIn, Response responseIn, User userIn) {
        Map<String, Object> data = new HashMap<>();
        String filterQuery = requestIn.queryParams("q");
        data.put("query", filterQuery != null ? String.format("'%s'", filterQuery) : "null");
        return new ModelAndView(data, "templates/reporting/cveSearch.jade");
    }
}
