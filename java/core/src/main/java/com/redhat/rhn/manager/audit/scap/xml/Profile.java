/*
 * Copyright (c) 2017 SUSE LLC
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
package com.redhat.rhn.manager.audit.scap.xml;

import org.simpleframework.xml.Attribute;

import java.util.Optional;

/**
 * Bean used to unmarshall an intermediary SCAP report.
 */
public class Profile {

    @Attribute
    private String title;

    @Attribute
    private String id;

    @Attribute(required = false)
    private String description;

    /**
     * No arg constructor.
     */
    public Profile() {
    }

    /**
     * @param idIn profile id
     * @param titleIn title
     * @param descriptionIn description
     */
    public Profile(String idIn, String titleIn, String descriptionIn) {
        this.title = titleIn;
        this.id = idIn;
        this.description = descriptionIn;
    }

    /**
     * @return title to get
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param titleIn to set
     */
    public void setTitle(String titleIn) {
        this.title = titleIn;
    }

    /**
     * @return id to get
     */
    public String getId() {
        return id;
    }

    /**
     * @param idIn to set
     */
    public void setId(String idIn) {
        this.id = idIn;
    }

    /**
     * @return description to get
     */
    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    /**
     * @param descriptionIn to set
     */
    public void setDescription(String descriptionIn) {
        this.description = descriptionIn;
    }
}
