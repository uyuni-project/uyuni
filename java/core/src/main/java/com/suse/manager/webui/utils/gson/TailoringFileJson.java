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
 * JSON DTO for TailoringFile entity
 */
public class TailoringFileJson {

    private Long id;
    private String name;
    private String fileName;
    private String displayFileName;
    private String description;

    /**
     * Default constructor
     */
    public TailoringFileJson() {
    }

    /**
     * Constructor
     * @param idIn the id
     * @param nameIn the name
     * @param fileNameIn the filename
     * @param displayFileNameIn the display filename
     * @param descriptionIn the description
     */
    public TailoringFileJson(Long idIn, String nameIn, String fileNameIn, String displayFileNameIn,
                             String descriptionIn) {
        this.id = idIn;
        this.name = nameIn;
        this.fileName = fileNameIn;
        this.displayFileName = displayFileNameIn;
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
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileNameIn the fileName to set
     */
    public void setFileName(String fileNameIn) {
        this.fileName = fileNameIn;
    }

    /**
     * @return the displayFileName
     */
    public String getDisplayFileName() {
        return displayFileName;
    }

    /**
     * @param displayFileNameIn the displayFileName to set
     */
    public void setDisplayFileName(String displayFileNameIn) {
        this.displayFileName = displayFileNameIn;
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
