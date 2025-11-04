/*
 * Copyright (c) 2016--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.server;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.channel.AccessToken;
import com.redhat.rhn.domain.channel.AccessTokenFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.configuration.SaltConfigSubscriptionService;
import com.redhat.rhn.manager.configuration.SaltConfigurable;

import com.suse.manager.webui.controllers.utils.ContactMethodUtil;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * MinionServer
 */
@Entity
@Table(name = "suseMinionInfo")
@PrimaryKeyJoinColumn(name = "server_id")
public class MinionServer extends Server implements SaltConfigurable {

    @Column(name = "minion_id")
    private String minionId;

    @Column(name = "kernel_live_version")
    private String kernelLiveVersion;

    @Column(name = "ssh_push_port")
    private Integer sshPushPort;

    @OneToMany(mappedBy = "minion", fetch = FetchType.LAZY)
    private Set<AccessToken> accessTokens = new HashSet<>();

    @OneToMany(mappedBy = "minion", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private final Set<Pillar> pillars = new HashSet<>();

    @Column(name = "reboot_required_after")
    private Date rebootRequiredAfter;

    @Column(name = "container_runtime")
    private String containerRuntime;

    @Column
    private String uname;

    @Column(name = "os_family")
    private String osFamily;


    /**
     * Constructs a MinionServer instance.
     */
    public MinionServer() {
        super();
    }

    /**
     * Minimal constructor used to avoid loading all properties in SSM config channel subscription
     *
     * @param idIn the server id
     * @param machineIdIn the machine id
     */
    public MinionServer(long idIn, String machineIdIn) {
        super(idIn, machineIdIn);
    }

    /**
     * @return the minion id
     */
    @Override
    public String getMinionId() {
        return minionId;
    }

    /**
     * @param minionIdIn the minion id to set
     */
    public void setMinionId(String minionIdIn) {
        this.minionId = minionIdIn;
    }

    /**
     * Gets kernel live version.
     *
     * @return the kernel live version
     */
    public String getKernelLiveVersion() {
        return kernelLiveVersion;
    }

    /**
     * Sets kernel live version.
     *
     * @param kernelLiveVersionIn the kernel live version
     */
    public void setKernelLiveVersion(String kernelLiveVersionIn) {
        this.kernelLiveVersion = kernelLiveVersionIn;
    }

    public Integer getSSHPushPort() {
        return sshPushPort;
    }

    public void setSSHPushPort(Integer sshPushPortIn) {
        this.sshPushPort = sshPushPortIn;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void subscribeConfigChannels(List<ConfigChannel> configChannelList, User user) {
        super.subscribeConfigChannels(configChannelList, user);
        SaltConfigSubscriptionService.subscribeChannels(this, configChannelList, user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubscribeConfigChannels(List<ConfigChannel> configChannelList, User user) {
        super.unsubscribeConfigChannels(configChannelList, user);
        SaltConfigSubscriptionService.unsubscribeChannels(this, configChannelList, user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConfigChannels(List<ConfigChannel> configChannelList, User user) {
        super.setConfigChannels(configChannelList, user);
        SaltConfigSubscriptionService.setConfigChannels(this, getConfigChannels(), user);
    }

    /**
     * @return value of pillars
     */
    public Set<Pillar> getPillars() {
        return pillars;
    }

    /**
     * @param pillarsIn value of pillars
     */
    public void setPillars(Set<Pillar> pillarsIn) {
        pillars.clear();
        pillars.addAll(pillarsIn);
    }

    /**
     * @param pillarIn value of pillar
     */
    public void addPillar(Pillar pillarIn) {
        pillars.add(pillarIn);
    }

    /**
     * Get the pillar corresponding to a category.
     *
     * @param category the category of the pillar to look for
     * @return the pillar if found
     */
    public Optional<Pillar> getPillarByCategory(String category) {
        return pillars.stream().filter(pillar -> pillar.getCategory().equals(category)).findFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof MinionServer otherMinion)) {
            return false;
        }
        return new EqualsBuilder()
                .appendSuper(super.equals(otherMinion))
                .append(getMachineId(), otherMinion.getMachineId())
                .append(getMinionId(), otherMinion.getMinionId())
                .isEquals();
    }


    /**
     * @return Return true when all assigned software channels have valid access tokens.
     */
    public boolean hasValidTokensForAllChannels() {

        Set<Channel> tokenChannels = AccessTokenFactory.listByMinion(this)
                .stream()
                .filter(AccessToken::getValid)
                .filter(t -> t.getExpiration().toInstant().isAfter(Instant.now()))
                .flatMap(t -> t.getChannels().stream())
                .collect(Collectors.toSet());

        return tokenChannels.containsAll(getChannels()) && getChannels().containsAll(tokenChannels);
    }

    /**
     * Get channel access tokens assigned to this minion.
     * @return set of access tokens
     */
    public Set<AccessToken> getAccessTokens() {
        return accessTokens;
    }

    /**
     * @deprecated do not used (its only here for hibernate)
     * @param accessTokensIn access token
     */
    @Deprecated
    public void setAccessTokens(Set<AccessToken> accessTokensIn) {
        this.accessTokens = accessTokensIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(getMachineId())
                .append(getMinionId())
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("machineId", getMachineId())
                .append("minionId", getMinionId())
                .toString();
    }

    /**
     * Converts this server to a MinionServer if it is one.
     *
     * @return optional of MinionServer
     */
    @Override
    public Optional<MinionServer> asMinionServer() {
        return Optional.of(this);
    }

    /**
     * Updates Server Path according to Salt master/proxy hostname.
     * @param hostname hostname of Salt master the minion is connected to
     * @return <code>true</code> if the path has been changed
     */
    public boolean updateServerPaths(String hostname) {
        Optional<Server> proxy = Optional.empty();
        // Only search for the hostname when it is not our own. We are not a proxy!
        if (!hostname.equals(ConfigDefaults.get().getJavaHostname())) {
            proxy = ServerFactory.lookupProxyServer(hostname);
        }

        return updateServerPaths(proxy, Optional.of(hostname));
    }

    /**
     * Updates Server Path according to proxyId.
     * @param proxyId Id of a proxy the minion is connected to
     * @return <code>true</code> if the path has been changed
     */
    public boolean updateServerPaths(Optional<Long> proxyId) {
        return updateServerPaths(proxyId.map(ServerFactory::lookupById), Optional.empty());
    }

    private boolean updateServerPaths(Optional<Server> proxy, Optional<String> hostname) {

        boolean changed = false;

        if (proxy.isPresent()) {
                // the system is connected to a proxy
                // check if serverPath already exists
                Optional<ServerPath> path = ServerFactory.findServerPath(this, proxy.get());
                if (path.isEmpty() || path.get().getPosition() != 0) {
                    // proxy path does not exist -> create it
                    Set<ServerPath> proxyPaths = ServerFactory.createServerPaths(this, proxy.get(),
                                                 hostname.orElse(proxy.get().getHostname()));
                    getServerPaths().clear();
                    getServerPaths().addAll(proxyPaths);

                    changed = true;
                }
         }
         else {
                if (!getServerPaths().isEmpty()) {
                    // reconnecting from proxy to master
                    getServerPaths().clear();

                    changed = true;
                }
        }
        return changed;
    }

    public boolean isSSHPush() {
        return getContactMethodLabel()
                .map(ContactMethodUtil::isSSHPushContactMethod)
                .orElse(false);
    }

    public boolean isRebootNeeded() {
        return getLastBoot() != null && rebootRequiredAfter != null && getLastBootAsDate().before(rebootRequiredAfter);
    }

    public Date getRebootRequiredAfter() {
        return rebootRequiredAfter;
    }

    public void setRebootRequiredAfter(Date rebootRequiredAfterIn) {
        rebootRequiredAfter = rebootRequiredAfterIn;
    }

    /**
     * @return the container runtime
     */
    public String getContainerRuntime() {
        return containerRuntime;
    }

    /**
     * @param containerRuntimeIn the container runtime to set
     */
    public void setContainerRuntime(String containerRuntimeIn) {
        this.containerRuntime = containerRuntimeIn;
    }

    /**
     * @return the uname
     */
    public String getUname() {
        return uname;
    }

    /**
     * @param unameIn the uname to set
     */
    public void setUname(String unameIn) {
        uname = unameIn;
    }

    /**
     * Getter for os family
     *
     * @return String to get
     */
    @Override
    public String getOsFamily() {
        return this.osFamily;
    }

    /**
     * Predicate to check for Suse os family
     * @return true is Suse os family
     */
    @Override
    public boolean isOsFamilySuse() {
        return this.osFamily.equals(ServerConstants.OS_FAMILY_SUSE);
    }

    /**
     * Setter for os family
     *
     * @param osFamilyIn to set
     */
    @Override
    public void setOsFamily(String osFamilyIn) {
        this.osFamily = osFamilyIn;
    }

    /**
     * Setter for Suse os family
     */
    @Override
    public void setOsFamilySuse() {
        this.osFamily = ServerConstants.OS_FAMILY_SUSE;
    }

}
