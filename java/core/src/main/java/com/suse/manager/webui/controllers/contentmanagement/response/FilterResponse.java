/*
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


import com.suse.manager.webui.utils.ViewHelper;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
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
    private String rule;
    private List<ImmutablePair<String, String>> projects;

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

    /**
     * Sets the criteria value
     *
     * @param criteriaValueIn the criteria value
     */
    public void setCriteriaValue(String criteriaValueIn) {
        // If we have a date as a criteria value we need to format it with the current user timezone
        if (this.criteriaKey.equals("issue_date")) {
            DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_DATE_TIME;
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(
                    criteriaValueIn, timeFormatter);
            Date criteriaValueDate = Date.from(Instant.from(offsetDateTime));
            criteriaValueIn = ViewHelper.getInstance().renderDate(criteriaValueDate);
        }
        this.criteriaValue = criteriaValueIn;
    }

    public void setRule(String ruleIn) {
        this.rule = ruleIn;
    }

    public void setName(String nameIn) {
        this.name = nameIn;
    }

    public String getName() {
        return name;
    }

    public void setProjects(List<ImmutablePair<String, String>> projectsIn) {
         this.projects = projectsIn;
    }
}
