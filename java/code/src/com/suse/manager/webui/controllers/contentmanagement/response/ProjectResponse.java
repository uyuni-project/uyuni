/**
 * Copyright (c) 2019 SUSE LLC
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
package com.suse.manager.webui.controllers.contentmanagement.response;

import java.util.List;

/**
 * JSON response wrapper for a content project.
 */
public class ProjectResponse {

    private ProjectPropertiesResponse properties;
    private List<ProjectSoftwareSourceResponse> softwareSources;
    private List<ProjectFilterResponse> filters;
    private List<EnvironmentResponse> environments;

    public void setProperties(ProjectPropertiesResponse propertiesIn) {
        this.properties = propertiesIn;
    }

    public void setFilters(List<ProjectFilterResponse> filtersIn) {
        this.filters = filtersIn;
    }

    public void setEnvironments(List<EnvironmentResponse> environmentsIn) {
        this.environments = environmentsIn;
    }

    public void setSoftwareSources(List<ProjectSoftwareSourceResponse> sourcesIn) {
        this.softwareSources = sourcesIn;
    }
}
