/**
 * Copyright (c) 2015 SUSE LLC
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

import com.redhat.rhn.common.security.CSRFTokenValidator;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;

import com.suse.manager.webui.services.SaltService;
import com.suse.manager.webui.services.impl.SaltAPIService;
import com.suse.saltstack.netapi.calls.wheel.Key;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import spark.ModelAndView;
import spark.Request;
import spark.Response;

/**
 * Controller class providing backend code for the minions page.
 */
public class MinionController {

    // Reference to the SaltService instance
    private static final SaltService SALT_SERVICE = SaltAPIService.INSTANCE;

    private MinionController() { }

    /**
     * Displays a list of minions.
     *
     * @param request the request object
     * @param response the response object
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView list(Request request, Response response) {
        Key.Names keys = SALT_SERVICE.getKeys();
        List<String> present = SALT_SERVICE.present();
        Map<String, Object> data = new HashMap<>();
        data.put("minions", keys.getMinions());
        data.put("unaccepted_minions", keys.getUnacceptedMinions());
        data.put("rejected_minions", keys.getRejectedMinions());
        data.put("present", present);
        data.put("csrf_token", CSRFTokenValidator.getToken(request.session().raw()));
        return new ModelAndView(data, "minions.jade");
    }

    /**
     * Handler for accept minion url.
     *
     * @param request the request object
     * @param response the response object
     * @return dummy string to satisfy spark
     */
    public static Object accept(Request request, Response response) {
        SALT_SERVICE.acceptKey(request.params("id"));
        response.redirect("/rhn/manager/minions");
        return "";
    }

    /**
     * Handler for delete minion url.
     *
     * @param request the request object
     * @param response the response object
     * @return dummy string to satisfy spark
     */
    public static Object destroy(Request request, Response response) {
        SALT_SERVICE.deleteKey(request.params("id"));
        response.redirect("/rhn/manager/minions");
        return "";
    }

    /**
     * Handler for reject minion url.
     *
     * @param request the request object
     * @param response the response object
     * @return dummy string to satisfy spark
     */
    public static Object reject(Request request, Response response) {
        SALT_SERVICE.rejectKey(request.params("id"));
        response.redirect("/rhn/manager/minions");
        return "";
    }

    /**
     * Handler for the realtime remote commands page.
     *
     * @param request the request object
     * @param response the response object
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView remoteCommands(Request request, Response response) {
        Map<String, Object> data = new HashMap<>();
        data.put("csrf_token", CSRFTokenValidator.getToken(request.session().raw()));
        return new ModelAndView(data, "remote-commands.jade");
    }

    /**
     * Displays a single minion.
     *
     * @param request the request object
     * @param response the response object
     * @return nothing
     */
    public static String show(Request request, Response response) {
        String minionId = request.params("id");
        String machineId = SaltAPIService.INSTANCE.getMachineId(minionId);
        Server server = ServerFactory.findRegisteredMinion(machineId);
        response.redirect("/rhn/systems/details/Overview.do?sid=" + server.getId());
        return "";
    }
}
