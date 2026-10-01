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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Content Environment Diff
 */
@Entity
@Table(name = "suseContentEnvironmentDiff")
public class ContentEnvironmentDiff extends BaseDomainHelper {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private ContentProject project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "env_id")
    private ContentEnvironment environment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private Channel channel;

    @Column(name = "diff_action")
    @Type(value = com.redhat.rhn.domain.contentmgmt.DiffActionEnumType.class)
    private DiffAction action;

    @Column(name = "entry_id")
    private long entryId;

    @Column(name = "entry_type")
    @Type(value = com.redhat.rhn.domain.contentmgmt.EntryTypeEnumType.class)
    private EntryType entryType;

    @Column(name = "entry_name")
    private String entryName;

    @Column(name = "entry_description")
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

    public Long getId() {
        return id;
    }

    public void setId(Long idIn) {
        id = idIn;
    }

    public ContentProject getProject() {
        return project;
    }

    public void setProject(ContentProject projectIn) {
        project = projectIn;
    }

    public ContentEnvironment getEnvironment() {
        return environment;
    }

    public void setEnvironment(ContentEnvironment environmentIn) {
        environment = environmentIn;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channelIn) {
        channel = channelIn;
    }

    public DiffAction getAction() {
        return action;
    }

    public void setAction(DiffAction actionIn) {
        action = actionIn;
    }

    public long getEntryId() {
        return entryId;
    }

    public void setEntryId(long entryIdIn) {
        entryId = entryIdIn;
    }

    public EntryType getEntryType() {
        return entryType;
    }

    public void setEntryType(EntryType entryTypeIn) {
        entryType = entryTypeIn;
    }

    public String getEntryName() {
        return entryName;
    }

    public void setEntryName(String entryNameIn) {
        entryName = entryNameIn;
    }

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
