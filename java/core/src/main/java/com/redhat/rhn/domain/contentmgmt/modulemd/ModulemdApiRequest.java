/*
 * Copyright (c) 2020 SUSE LLC
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

package com.redhat.rhn.domain.contentmgmt.modulemd;

import java.util.Collections;
import java.util.List;

/**
 * Request payload for modulemd Python API
 */
public class ModulemdApiRequest {
    private String function;
    private List<String> paths;
    private List<Module> streams;

    private static final String MODULE_PACKAGES = "module_packages";
    private static final String LIST_PACKAGES = "list_packages";
    private static final String LIST_MODULES = "list_modules";

    private ModulemdApiRequest(String functionIn, List<String> pathsIn, List<Module> streamsIn) {
        this.function = functionIn;
        this.paths = pathsIn;
        this.streams = streamsIn;
    }

    /**
     * Instantiate payload for a 'module_packages' call
     *
     * @param paths module metadata paths
     * @param streams selected module streams
     * @return the request payload to call the API with
     */
    public static ModulemdApiRequest modulePackagesRequest(List<String> paths, List<Module> streams) {
        return new ModulemdApiRequest(MODULE_PACKAGES, paths, streams);
    }

    /**
     * Instantiate payload for a 'list_packages' call
     *
     * @param paths module metadata paths
     * @return the request payload to call the API with
     */
    public static ModulemdApiRequest listPackagesRequest(List<String> paths) {
        return new ModulemdApiRequest(LIST_PACKAGES, paths, null);
    }

    /**
     * Instantiate payload for a 'list_modules' call
     *
     * @param path a single module metadata path
     * @return the request payload to call the API with
     */
    public static ModulemdApiRequest listModulesRequest(String path) {
        return new ModulemdApiRequest(LIST_MODULES, Collections.singletonList(path), null);
    }

    public String getFunction() {
        return function;
    }

    public List<String> getPaths() {
        return paths;
    }

    public List<Module> getStreams() {
        return streams;
    }
}
