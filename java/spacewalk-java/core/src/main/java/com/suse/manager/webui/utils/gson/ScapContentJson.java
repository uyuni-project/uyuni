/*
 * Copyright (c) 2025 SUSE LLC
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
package com.suse.manager.webui.utils.gson;

/**
 * DTO for SCAP content information
 */
public class ScapContentJson {

    private Long id;
    private String name;
    private String dataStreamFileName;
    private String xccdfFileName;
    private String description;

    /**
     * Default constructor for GSON
     */
    public ScapContentJson() {
    }

    /**
     * Constructor
     * @param idIn the content ID
     * @param nameIn the content name
     * @param dataStreamFileNameIn the DataStream filename
     * @param xccdfFileNameIn the XCCDF filename
     * @param descriptionIn the content description
     */
    public ScapContentJson(Long idIn, String nameIn, String dataStreamFileNameIn,
                          String xccdfFileNameIn, String descriptionIn) {
        this.id = idIn;
        this.name = nameIn;
        this.dataStreamFileName = dataStreamFileNameIn;
        this.xccdfFileName = xccdfFileNameIn;
        this.description = descriptionIn;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param idIn the id to set
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param nameIn the name to set
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * @return the dataStreamFileName
     */
    public String getDataStreamFileName() {
        return dataStreamFileName;
    }

    /**
     * @param dataStreamFileNameIn the dataStreamFileName to set
     */
    public void setDataStreamFileName(String dataStreamFileNameIn) {
        this.dataStreamFileName = dataStreamFileNameIn;
    }

    /**
     * @return the xccdfFileName
     */
    public String getXccdfFileName() {
        return xccdfFileName;
    }

    /**
     * @param xccdfFileNameIn the xccdfFileName to set
     */
    public void setXccdfFileName(String xccdfFileNameIn) {
        this.xccdfFileName = xccdfFileNameIn;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param descriptionIn the description to set
     */
    public void setDescription(String descriptionIn) {
        this.description = descriptionIn;
    }
}
