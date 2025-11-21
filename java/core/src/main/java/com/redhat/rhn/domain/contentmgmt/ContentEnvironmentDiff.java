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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
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
    private DiffAction action;
    private long entryId;
    private EntryType entryType;
    private String entryName;
    private String entryDescription;

    /**
     * Constructor
     */
    public ContentEnvironmentDiff() {
    }

    /**
     * Constructor
     * @param projectIn
     * @param environmentIn
     * @param channelIn
     * @param actionIn
     * @param entryIdIn
     * @param entryTypeIn
     * @param entryNameIn
     * @param entryDescriptionIn
     */
    public ContentEnvironmentDiff(ContentProject projectIn, ContentEnvironment environmentIn,
                                  Channel channelIn, DiffAction actionIn, long entryIdIn, EntryType entryTypeIn,
                                  String entryNameIn, String entryDescriptionIn) {
        project = projectIn;
        environment = environmentIn;
        channel = channelIn;
        action = actionIn;
        entryId = entryIdIn;
        entryType = entryTypeIn;
        entryName = entryNameIn;
        entryDescription = entryDescriptionIn;
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
    @JoinColumn(name = "env_id")
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

    @Column(name = "diff_action")
    @Type(type = "com.redhat.rhn.domain.contentmgmt.DiffActionEnumType")
    public DiffAction getAction() {
        return action;
    }

    public void setAction(DiffAction actionIn) {
        action = actionIn;
    }

    @Column(name = "entry_id")
    public long getEntryId() {
        return entryId;
    }

    public void setEntryId(long entryIdIn) {
        entryId = entryIdIn;
    }

    @Column(name = "entry_type")
    @Type(type = "com.redhat.rhn.domain.contentmgmt.EntryTypeEnumType")
    public EntryType getEntryType() {
        return entryType;
    }

    public void setEntryType(EntryType entryTypeIn) {
        entryType = entryTypeIn;
    }

    @Column(name = "entry_name")
    public String getEntryName() {
        return entryName;
    }

    public void setEntryName(String entryNameIn) {
        entryName = entryNameIn;
    }

    @Column(name = "entry_description")
    public String getEntryDescription() {
        return entryDescription;
    }

    public void setEntryDescription(String entryDescriptionIn) {
        entryDescription = entryDescriptionIn;
    }

    /**
     * Update this entry with data from other object
     * @param other the other object
     */
    public void update(ContentEnvironmentDiff other) {
        setAction(other.getAction());
        setEntryName(other.getEntryName());
        setEntryDescription(other.getEntryDescription());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object object) {
        if (object == null || object.getClass() != getClass()) {
            return false;
        }

        ContentEnvironmentDiff that = (ContentEnvironmentDiff) object;

        return new EqualsBuilder()
                .append(this.getProject(), that.getProject())
                .append(this.getEnvironment(), that.getEnvironment())
                .append(this.getChannel(), that.getChannel())
                .append(this.getEntryType(), that.getEntryType())
                .append(this.getEntryId(), that.getEntryId())
                .append(this.getAction(), that.getAction())
                .append(this.getEntryName(), that.getEntryName())
                .append(this.getEntryDescription(), that.getEntryDescription())
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getProject()).append(getEnvironment())
                .append(getChannel()).append(getEntryType()).append(getEntryId())
                .toHashCode();
    }
}
