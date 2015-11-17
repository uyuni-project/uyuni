package com.suse.manager.webui.utils;

import spark.Filter;
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

    /**
     * Puts object to the flash scope.
     * @param request
     * @param object
     */
    public static void flash(Request request, Object object) {
        request.session(true).attribute(FLASH, object);
    }

    /**
     * Gets data from the flash scope.
     * @param request
     * @param <T>
     * @return
     */
    public static <T> T flash(Request request) {
        return request.attribute(FLASH);
    }

    /**
     * Spark filter for passing the data from (temporary) session attribute to the request
     * attribute.
     * Deletes the flash data from the cache.
     * @return filter
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
