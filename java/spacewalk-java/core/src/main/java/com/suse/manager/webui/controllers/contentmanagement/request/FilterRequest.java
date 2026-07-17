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
package com.suse.manager.webui.controllers.contentmanagement.request;

/**
 * JSON request wrapper for an environment of a content project.
 */
public class FilterRequest {

    private String projectLabel;
    private String name;
    private String entityType;
    private String matcher;
    private String criteriaKey;
    private String criteriaValue;
    private String rule;

    // Filter template params
    private String template;
    private String prefix;
    // Live patching
    private Long kernelEvrId;
    // AppStreams
    private Long channelId;

    public String getProjectLabel() {
        return projectLabel;
    }

    public String getName() {
        return name;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getMatcher() {
        return matcher;
    }

    public String getCriteriaKey() {
        return criteriaKey;
    }

    public String getCriteriaValue() {
        return criteriaValue;
    }

    public String getRule() {
        return rule;
    }

    public String getTemplate() {
        return template;
    }

    public String getPrefix() {
        return prefix;
    }

    public Long getKernelEvrId() {
        return kernelEvrId;
    }

    public Long getChannelId() {
        return channelId;
    }
}
