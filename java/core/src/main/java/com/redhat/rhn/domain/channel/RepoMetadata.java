/*
 * Copyright (c) 2018 Red Hat, Inc.
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
package com.redhat.rhn.domain.channel;

import com.redhat.rhn.domain.BaseDomainHelper;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 *
 *
 */
@Entity
@Table(name = "rhnChannelComps")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "comps_type_id")
@DiscriminatorValue("-1")
public class RepoMetadata extends BaseDomainHelper {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "channelcomps_seq")
    @SequenceGenerator(name = "channelcomps_seq", sequenceName = "rhn_channelcomps_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "relative_filename", nullable = false)
    private String relativeFilename;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "channel_id", nullable = false, unique = true)
    @Fetch(FetchMode.SELECT)
    private Channel channel;

    /**
     *
     * @return Returns Id
     */
    public Long getId() {
        return id;
    }

    /**
     *
     * @param idIn The Id to set.
     */
    protected void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     *
     * @return Returns Relative filename
     */
    public String getRelativeFilename() {
        return relativeFilename;
    }

    /**
     *
     * @param relativeFilenameIn The filename to set.
     */
    public void setRelativeFilename(String relativeFilenameIn) {
        this.relativeFilename = relativeFilenameIn;
    }

    /**
     *
     * @param channelIn The channel to set.
     */
    public void setChannel(Channel channelIn) {
        this.channel = channelIn;
    }

    /**
     *
     * @return Returns channel object
     */
    public Channel getChannel() {
        return channel;
    }
}
