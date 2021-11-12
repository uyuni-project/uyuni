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

package com.suse.manager.webui.utils;

import spark.Request;
import spark.Response;
import spark.Session;

/**
 * Put/get data to/from "flash" scope (param in session that survives only one
 * request, handy for passing temporary data between redirects).
 *
 * Currently supports just one piece of data to be stored/retrieved to/from flash scope.
 */
public class FlashScopeHelper {

    // name of the request and session attribute with the flash content
    private static final String FLASH = "___flash";

    private FlashScopeHelper() { }

    /**
     * Puts object to the flash scope.
     *
     * @param request - request with the session into which the flash data will be inserted
     * @param object - object to be added to the flash scope
     */
    public static void flash(Request request, Object object) {
        request.session(true).attribute(FLASH, object);
    }

    /**
     * Gets data from the flash scope.
     *
     * @param request - request with the session containing the flash data
     * @param <T> - type of the object to be retrieved from the flash scope
     * @return the object stored in the flash scope
     */
    public static <T> T flash(Request request) {
        return request.attribute(FLASH);
    }

    /**
     * Spark filter for passing the data from (temporary) session attribute to the request
     * attribute.
     * Deletes the flash data from the cache in the session.
     *
     * @param request - request with the session containing the flash object
     * @param response - not used
     */
    public static void handleFlashData(Request request, Response response) {
        Session session = request.session();
        Object fromSession = session.attribute(FLASH);
        if (fromSession != null) {
            request.attribute(FLASH, fromSession);
        }
        session.attribute(FLASH, null);
    }

}
