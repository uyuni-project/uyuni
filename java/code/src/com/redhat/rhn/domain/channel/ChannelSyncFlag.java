/*
 * Copyright (c) 2024 SUSE LLC
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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "rhnChannelSyncFlag")
public class ChannelSyncFlag implements Serializable {

    @Id
    @Column(name = "channel_id", nullable = false)
    private Long id;

    @OneToOne(mappedBy = "channelSyncFlag")
    @MapsId
    @JoinColumn(name = "channel_id")
    private Channel channel;

    @Column(name = "no_strict", nullable = false)
    private boolean noStrict;

    @Column(name = "no_errata", nullable = false)
    private boolean noErrata;

    @Column(name = "only_latest", nullable = false)
    private boolean onlyLatest;

    @Column(name = "create_tree", nullable = false)
    private boolean createTree;

    @Column(name = "quit_on_error", nullable = false)
    private boolean quitOnError;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long idnr) {
        this.id = idnr;
    }

    public boolean isNoStrict() {
        return noStrict;
    }

    public void setNoStrict(boolean flag) {
        this.noStrict = flag;
    }

    public boolean isNoErrata() {
        return noErrata;
    }

    public void setNoErrata(boolean flag) {
        this.noErrata = flag;
    }

    public boolean isOnlyLatest() {
        return onlyLatest;
    }

    public void setOnlyLatest(boolean flag) {
        this.onlyLatest = flag;
    }

    public boolean isCreateTree() {
        return createTree;
    }

    public void setCreateTree(boolean flag) {
        this.createTree = flag;
    }

    public boolean isQuitOnError() {
        return quitOnError;
    }

    public void setQuitOnError(boolean flag) {
        this.quitOnError = flag;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel ch) {
        this.channel = ch;
    }

    /**
     * set a specific channel reposync flag
     * @param flagName the flags name
     * @param value value to set
     */
    public void setFlag(String flagName, boolean value) {
        switch (flagName) {
            case "no-strict":
                this.noStrict = value;
                break;
            case "no-errata":
                this.noErrata = value;
                break;
            case "latest":
                this.onlyLatest = value;
                break;
            case "sync-kickstart":
                this.createTree = value;
                break;
            case "fail":
                this.quitOnError = value;
                break;
            default:
                throw new IllegalArgumentException("Invalid attribute name: " + flagName);
        }
    }
}
