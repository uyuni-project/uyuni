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
 * JSON response wrapper for a content filter resume.
 */
public class FilterResponse {

    private Long id;
    private String name;
    private String entityType;
    private String matcher;
    private String criteriaKey;
    private String criteriaValue;
    private Boolean deny;
    private List<String> projects;

    public void setId(Long idIn) {
        this.id = idIn;
    }

    public void setMatcher(String matcherIn) {
        this.matcher = matcherIn;
    }

    public void setEntityType(String entityTypeIn) {
        this.entityType = entityTypeIn;
    }

    public void setCriteriaKey(String criteriaKeyIn) {
        this.criteriaKey = criteriaKeyIn;
    }

    public void setCriteriaValue(String criteriaValueIn) {
        this.criteriaValue = criteriaValueIn;
    }

    public void setDeny(Boolean denyIn) {
        this.deny = denyIn;
    }

    public void setName(String nameIn) {
        this.name = nameIn;
    }

    public void setProjects(List<String> projectsIn) {
        this.projects = projectsIn;
    }
}
