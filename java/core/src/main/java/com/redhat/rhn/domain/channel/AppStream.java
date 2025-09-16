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

import com.redhat.rhn.domain.rhnpackage.Package;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "suseAppstream")
public class AppStream {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "appstreams_module_seq")
    @SequenceGenerator(name = "appstreams_module_seq", sequenceName = "suse_as_module_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String stream;

    @Column(nullable = false)
    private String version;

    @Column(nullable = false)
    private String context;

    @Column(nullable = false)
    private String arch;

    @ManyToOne
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "suseAppstreamPackage",
            joinColumns = {
                    @JoinColumn(name = "module_id", nullable = false, updatable = false)},
            inverseJoinColumns = {
                    @JoinColumn(name = "package_id", nullable = false, updatable = false)}
    )
    private Set<Package> artifacts;

    @OneToMany(mappedBy = "appStream", cascade = CascadeType.ALL)
    private Set<AppStreamApi> rpms;

    public Long getId() {
        return id;
    }

    public void setId(Long idIn) {
        id = idIn;
    }

    public String getName() {
        return name;
    }

    public void setName(String nameIn) {
        name = nameIn;
    }

    public String getStream() {
        return stream;
    }

    public void setStream(String streamIn) {
        stream = streamIn;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String versionIn) {
        version = versionIn;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String contextIn) {
        context = contextIn;
    }

    public String getArch() {
        return arch;
    }

    public void setArch(String archIn) {
        arch = archIn;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channelIn) {
        channel = channelIn;
    }

    public Set<Package> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(Set<Package> artifactsIn) {
        artifacts = artifactsIn;
    }

    public Set<AppStreamApi> getRpms() {
        return rpms;
    }

    public void setRpms(Set<AppStreamApi> rpmsIn) {
        rpms = rpmsIn;
    }

    public String getAppStreamKey() {
        return name + ":" + stream;
    }
}
