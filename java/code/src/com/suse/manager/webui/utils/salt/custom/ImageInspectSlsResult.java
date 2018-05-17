/**
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
package com.suse.manager.webui.utils.salt.custom;

import com.google.gson.annotations.SerializedName;
import com.suse.manager.webui.utils.salt.custom.ImageChecksum.Checksum;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Object representation of the results of a call to state.apply
 * images.profileupdate.
 */
public class ImageInspectSlsResult {
    @SerializedName("Id")
    private Checksum id;
    @SerializedName("Architecture")
    private String architecture;
    @SerializedName("Author")
    private String author;
    @SerializedName("Comment")
    private String comment;
    @SerializedName("Container")
    private String container;
    @SerializedName("Created")
    private ZonedDateTime created;
    @SerializedName("DockerVersion")
    private String dockerVersion;
    @SerializedName("Os")
    private String os;
    @SerializedName("Size")
    private long size;
    @SerializedName("VirtualSize")
    private long virtualSize;
    @SerializedName("RepoDigests")
    private List<String> repoDigests;

    /**
     * @return the id
     */
    public Checksum getId() {
        return id;
    }

    /**
     * @return the architecture
     */
    public String getArchitecture() {
        return architecture;
    }

    /**
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * @return the container
     */
    public String getContainer() {
        return container;
    }

    /**
     * @return the created
     */
    public ZonedDateTime getCreated() {
        return created;
    }

    /**
     * @return the dockerVersion
     */
    public String getDockerVersion() {
        return dockerVersion;
    }

    /**
     * @return the os
     */
    public String getOs() {
        return os;
    }

    /**
     * @return the size
     */
    public long getSize() {
        return size;
    }

    /**
     * @return the virtualSize
     */
    public long getVirtualSize() {
        return virtualSize;
    }

    /**
     * @return the repoDigests
     */
    public List<String> getRepoDigests() {
        return repoDigests;
    }
}
