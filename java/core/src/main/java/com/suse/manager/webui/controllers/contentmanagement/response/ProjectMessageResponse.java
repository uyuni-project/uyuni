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
package com.suse.manager.webui.controllers.contentmanagement.response;


/**
 * JSON response wrapper for a content project feedback message.
 */
public class ProjectMessageResponse {
    private String text;
    private String type;
    private String entity;

    /**
     * Initialize a new project message JSON object
     *
     * @param textIn the message text
     * @param typeIn the message type
     * @param entityIn the entity the message belongs to
     */
    public ProjectMessageResponse(String textIn, String typeIn, String entityIn) {
        this.text = textIn;
        this.type = typeIn;
        this.entity = entityIn;
    }

    public String getText() {
        return text;
    }

    public String getType() {
        return type;
    }

    public String getEntity() {
        return entity;
    }
}
