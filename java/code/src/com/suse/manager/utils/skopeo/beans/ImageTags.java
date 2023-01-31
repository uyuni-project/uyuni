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
package com.suse.manager.utils.skopeo.beans;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ImageTags {

    @SerializedName("Repository")
    private String repository;
    @SerializedName("Tags")
    private List<String> tags;

    /**
     * Constructor with all fields
     * @param repositoryIn
     * @param tagsIn
     */
    public ImageTags(String repositoryIn, List<String> tagsIn) {
        repository = repositoryIn;
        tags = tagsIn;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repositoryIn) {
        repository = repositoryIn;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tagsIn) {
        tags = tagsIn;
    }
}
