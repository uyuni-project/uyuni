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

package com.redhat.rhn.domain.contentmgmt.validation;

/**
 * A content project validation message of a specific type
 *
 * The type can be one of TYPE_INFO, TYPE_WARN, TYPE_ERROR.
 */
public class ContentValidationMessage {

    public static final String TYPE_INFO = "info";
    public static final String TYPE_WARN = "warning";
    public static final String TYPE_ERROR = "error";

    private static final String ENTITY_PROPERTIES = "properties";
    private static final String ENTITY_SOURCES = "softwareSources";
    private static final String ENTITY_FILTERS = "filters";
    private static final String ENTITY_ENVIRONMENTS = "environments";

    private String message;
    private String type;
    private String entity;

    private ContentValidationMessage(String messageIn, String typeIn, String entityIn) {
        this.message = messageIn;
        this.type = typeIn;
        this.entity = entityIn;
    }

    /**
     * Create a new validation message for content properties
     *
     * @param message the message text
     * @param type the message type
     * @return the validation message
     */
    public static ContentValidationMessage contentPropertiesMessage(String message, String type) {
        return new ContentValidationMessage(message, type, ENTITY_PROPERTIES);
    }

    /**
     * Create a new validation message for software sources
     *
     * @param message the message text
     * @param type the message type
     * @return the validation message
     */
    public static ContentValidationMessage softwareSourcesMessage(String message, String type) {
        return new ContentValidationMessage(message, type, ENTITY_SOURCES);
    }

    /**
     * Create a new validation message for content filters
     *
     * @param message the message text
     * @param type the message type
     * @return the validation message
     */
    public static ContentValidationMessage contentFiltersMessage(String message, String type) {
        return new ContentValidationMessage(message, type, ENTITY_FILTERS);
    }

    /**
     * Create a new validation message for project environments
     *
     * @param message the message text
     * @param type the message type
     * @return the validation message
     */
    public static ContentValidationMessage projectEnvironmentsMessage(String message, String type) {
        return new ContentValidationMessage(message, type, ENTITY_ENVIRONMENTS);
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public String getEntity() {
        return entity;
    }
}
