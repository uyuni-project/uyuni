/*
 * Copyright (c) 2023 SUSE LLC
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
package com.suse.manager.webui.controllers.image.beans;

public class RepositoryImageList {
    private String name;
    private String description;
    private Integer starCount;
    private Boolean isTrusted;
    private Boolean isOfficial;

    /**
     * Constructor with all the paramters
     * @param nameIn
     * @param descriptionIn
     * @param starCountIn
     * @param isTrustedIn
     * @param isOfficialIn
     */
    public RepositoryImageList(String nameIn, String descriptionIn, Integer starCountIn, Boolean isTrustedIn, Boolean isOfficialIn) {
        name = nameIn;
        description = descriptionIn;
        starCount = starCountIn;
        isTrusted = isTrustedIn;
        isOfficial = isOfficialIn;
    }

    public String getName() {
        return name;
    }

    public void setName(String nameIn) {
        name = nameIn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String descriptionIn) {
        description = descriptionIn;
    }

    public Integer getStarCount() {
        return starCount;
    }

    public void setStarCount(Integer starCountIn) {
        starCount = starCountIn;
    }

    public Boolean getTrusted() {
        return isTrusted;
    }

    public void setTrusted(Boolean trustedIn) {
        isTrusted = trustedIn;
    }

    public Boolean getOfficial() {
        return isOfficial;
    }

    public void setOfficial(Boolean officialIn) {
        isOfficial = officialIn;
    }
}
