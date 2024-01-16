package com.redhat.rhn.domain.channel;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "rhnChannelSyncFlag")
public class ChannelSyncFlag implements Serializable {

    @Id
    @Column(name = "channel_id", nullable = false)
    private Long id;
        
    @OneToOne(mappedBy = "channelSyncFlags")
    private Channel rhnChannel;

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

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isNoStrict() {
        return noStrict;
    }

    public void setNoStrict(boolean noStrict) {
        this.noStrict = noStrict;
    }

    public boolean isNoErrata() {
        return noErrata;
    }

    public void setNoErrata(boolean noErrata) {
        this.noErrata = noErrata;
    }

    public boolean isOnlyLatest() {
        return onlyLatest;
    }

    public void setOnlyLatest(boolean onlyLatest) {
        this.onlyLatest = onlyLatest;
    }

    public boolean isCreateTree() {
        return createTree;
    }

    public void setCreateTree(boolean createTree) {
        this.createTree = createTree;
    }

    public boolean isQuitOnError() {
        return quitOnError;
    }

    public void setQuitOnError(boolean quitOnError) {
        this.quitOnError = quitOnError;
    }

    public Channel getRhnChannel() {
        return rhnChannel;
    }

    public void setRhnChannel(Channel rhnChannel) {
        this.rhnChannel = rhnChannel;
    }
}