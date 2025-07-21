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
 */
package com.redhat.rhn.domain.contentmgmt;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.channel.Channel;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Content Environment Diff
 */
@Entity
@Table(name = "suseContentEnvironmentDiff")
public class ContentEnvironmentDiff extends BaseDomainHelper {

    private long id;
    private ContentProject project;
    private ContentEnvironment environment;
    private Channel channel;
    private long entryId;
    private String entryType;
    private String entryDiff;
    private String entryName;
    private String entryVersion;
    private boolean filtered;

    /**
     * Constructor
     * @param projectIn
     * @param environmentIn
     * @param channelIn
     * @param entryTypeIn
     * @param entryDiffIn
     * @param entryIdIn
     * @param entryNameIn
     * @param entryVersionIn
     * @param filteredIn
     */
    public ContentEnvironmentDiff(ContentProject projectIn, ContentEnvironment environmentIn,
                                  Channel channelIn, long entryIdIn, String entryTypeIn, String entryDiffIn,
                                  String entryNameIn, String entryVersionIn, boolean filteredIn) {
        project = projectIn;
        environment = environmentIn;
        channel = channelIn;
        entryId = entryIdIn;
        entryType = entryTypeIn;
        entryDiff = entryDiffIn;
        entryName = entryNameIn;
        entryVersion = entryVersionIn;
        filtered = filteredIn;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long getId() {
        return id;
    }

    public void setId(long idIn) {
        id = idIn;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    public ContentProject getProject() {
        return project;
    }

    public void setProject(ContentProject projectIn) {
        project = projectIn;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "srv_env_id")
    public ContentEnvironment getEnvironment() {
        return environment;
    }

    public void setEnvironment(ContentEnvironment environmentIn) {
        environment = environmentIn;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channelIn) {
        channel = channelIn;
    }

    public long getEntryId() {
        return entryId;
    }

    public void setEntryId(long entryIdIn) {
        entryId = entryIdIn;
    }

    public String getEntryType() {
        return entryType;
    }

    public void setEntryType(String entryTypeIn) {
        entryType = entryTypeIn;
    }

    public String getEntryDiff() {
        return entryDiff;
    }

    public void setEntryDiff(String entryDiffIn) {
        entryDiff = entryDiffIn;
    }

    public String getEntryName() {
        return entryName;
    }

    public void setEntryName(String entryNameIn) {
        entryName = entryNameIn;
    }

    public String getEntryVersion() {
        return entryVersion;
    }

    public void setEntryVersion(String entryVersionIn) {
        entryVersion = entryVersionIn;
    }

    public boolean isFiltered() {
        return filtered;
    }

    public void setFiltered(boolean filteredIn) {
        filtered = filteredIn;
    }
}
