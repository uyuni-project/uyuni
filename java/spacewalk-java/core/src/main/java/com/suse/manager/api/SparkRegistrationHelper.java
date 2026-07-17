/*
 * Copyright (c) 2022 SUSE LLC
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
package com.suse.manager.api;

import spark.Route;
import spark.Spark;

/**
 * Helper class that registers a GET or POST {@link Route} to {@link Spark}
 *
 * The registration is handled by a standalone class to make unit testing possible by mocking it.
 */
public class SparkRegistrationHelper {

    /**
     * Register a GET {@link Route} to {@link Spark}
     * @param path the URL path
     * @param route the route object
     */
    public void addGetRoute(String path, Route route) {
        Spark.get(path, route);
    }

    /**
     * Register a POST {@link Route} to {@link Spark}
     * @param path the URL path
     * @param route the route object
     */
    public void addPostRoute(String path, Route route) {
        Spark.post(path, route);
    }
}
