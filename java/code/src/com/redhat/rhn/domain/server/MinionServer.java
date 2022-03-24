/*
 * Copyright (c) 2016--2021 SUSE LLC
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
package com.redhat.rhn.domain.server;

import com.redhat.rhn.domain.channel.AccessToken;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.configuration.SaltConfigSubscriptionService;
import com.redhat.rhn.manager.configuration.SaltConfigurable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * MinionServer
 */
public class MinionServer extends Server implements SaltConfigurable {

    private String minionId;
    private String osFamily;
    private String kernelLiveVersion;
    private Integer sshPushPort;
    private Set<AccessToken> accessTokens = new HashSet<>();
    private Set<Pillar> pillars = new HashSet<>();

    /**
     * Constructs a MinionServer instance.
     */
    public MinionServer() {
        super();
    }

    /**
     * @return the minion id
     */
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
     * Getter for os family
     *
     * @return String to get
     */
    public String getOsFamily() {
        return this.osFamily;
    }

    /**
     * Setter for os family
     *
     * @param osFamilyIn to set
     */
    public void setOsFamily(String osFamilyIn) {
        this.osFamily = osFamilyIn;
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
        SaltConfigSubscriptionService.setConfigChannels(this, configChannelList, user);
    }

    /**
     * Return <code>true</code> if OS on this system supports OS Image building,
     * <code>false</code> otherwise.
     *
     * @return <code>true</code> if OS supports OS Image building
     */
    @Override
    public boolean doesOsSupportsOSImageBuilding() {
        return isSLES11() || isSLES12() || isSLES15() || isLeap15();
    }

    /**
     * Return <code>true</code> if OS on this system supports Containerization,
     * <code>false</code> otherwise.
     * <p>
     * Note: For SLES, we are only checking if it's not 10 nor 11.
     * Older than SLES 10 are not being checked.
     * </p>
     *
     * @return <code>true</code> if OS supports Containerization
     */
    @Override
    public boolean doesOsSupportsContainerization() {
        return !isSLES10() && !isSLES11();
    }

    /**
     * Return <code>true</code> if OS on this system supports Transactional Update,
     * <code>false</code> otherwise.
     *
     * @return <code>true</code> if OS supports Transactional Update
     */
    public boolean doesOsSupportsTransactionalUpdate() {
        return isSLEMicro();
    }

    @Override
    public boolean doesOsSupportsMonitoring() {
        return isSLES12() || isSLES15() || isLeap15() || isUbuntu1804() || isUbuntu2004() || isRedHat6() ||
                isRedHat7() || isRedHat8() || isAlibaba2() || isAmazon2() || isRocky8() ||
                isDebian11() || isDebian10();
    }

    /**
     * @return true if the installer type is of SLES 10
     */
    private boolean isSLES10() {
        return ServerConstants.SLES.equals(getOs()) && getRelease().startsWith("10");
    }

    /**
     * @return true if the installer type is of SLES 11
     */
    private boolean isSLES12() {
        return ServerConstants.SLES.equals(getOs()) && getRelease().startsWith("12");
    }

    /**
     * @return true if the installer type is of SLES 11
     */
    private boolean isSLES11() {
        return ServerConstants.SLES.equals(getOs()) && getRelease().startsWith("11");
    }

    /**
     * @return true if the installer type is of SLE Micro
     */
    private boolean isSLEMicro() {
        return ServerConstants.SLEMICRO.equals(getOs());
    }

    /**
     * @return true if the installer type is of SLES 15
     */
    private boolean isSLES15() {
        return ServerConstants.SLES.equals(getOs()) && getRelease().startsWith("15");
    }

    private boolean isLeap15() {
        return ServerConstants.LEAP.equalsIgnoreCase(getOs()) && getRelease().startsWith("15");
    }

    private boolean isUbuntu1804() {
        return ServerConstants.UBUNTU.equals(getOs()) && getRelease().equals("18.04");
    }

    private boolean isUbuntu2004() {
        return ServerConstants.UBUNTU.equals(getOs()) && getRelease().equals("20.04");
    }

    private boolean isDebian11() {
        return ServerConstants.DEBIAN.equals(getOs()) && getRelease().equals("11");
    }

    private boolean isDebian10() {
        return ServerConstants.DEBIAN.equals(getOs()) && getRelease().equals("10");
    }

    /**
     * This is supposed to cover all RedHat flavors (incl. RHEL, RES and CentOS Linux)
     */
    private boolean isRedHat6() {
        return ServerConstants.REDHAT.equals(getOsFamily()) && getRelease().equals("6");
    }

    private boolean isRedHat7() {
        return ServerConstants.REDHAT.equals(getOsFamily()) && getRelease().equals("7");
    }

    private boolean isRedHat8() {
        return ServerConstants.REDHAT.equals(getOsFamily()) && getRelease().equals("8");
    }

    private boolean isAlibaba2() {
        return ServerConstants.ALIBABA.equals(getOs());
    }

    private boolean isAmazon2() {
        return ServerConstants.AMAZON.equals(getOsFamily()) && getRelease().equals("2");
    }

    private boolean isRocky8() {
        return ServerConstants.ROCKY.equals(getOs()) && getRelease().startsWith("8.");
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
        if (!(other instanceof MinionServer)) {
            return false;
        }
        MinionServer otherMinion = (MinionServer) other;
        return new EqualsBuilder()
                .appendSuper(super.equals(otherMinion))
                .append(getMachineId(), otherMinion.getMachineId())
                .append(getMinionId(), otherMinion.getMinionId())
                .isEquals();
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
        Optional<Server> proxy = ServerFactory.lookupProxyServer(hostname);

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
                if (!path.isPresent() || path.get().getPosition() != 0) {
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
}
