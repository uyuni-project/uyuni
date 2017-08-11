/**
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

package com.suse.manager.webui.controllers;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;

import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.rhnset.RhnSetManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.suse.manager.webui.utils.gson.JsonResult;

import java.util.List;

import spark.Request;
import spark.Response;

/**
 * Spark controller for SSM system addition/removal.
 */
public class SSMController {

    private static final Gson GSON = new GsonBuilder().create();

    private SSMController() { }

    /**
     * Adds a list of system ids to the System Set Manager. Returns the new
     * count wrapped in a JsonResult
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static Object add(Request req, Response res, User user) {
        List<Long> serverIds = GSON.fromJson(req.body(),
                new TypeToken<List<Long>>() { }.getType());

        RhnSet set = RhnSetDecl.SYSTEMS.get(user);
        set.addAll(serverIds);
        RhnSetManager.store(set);

        return json(res, new JsonResult<>(true, set.size()));
    }
}
