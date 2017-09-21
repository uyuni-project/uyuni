/**
 * Copyright (c) 2016 SUSE LLC
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
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * MinionServer
 */
public class MinionServer extends Server {

    private String minionId;
    private String osFamily;
    private String kernelLiveVersion;
    private Set<AccessToken> accessTokens = new HashSet<>();

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
    public Optional<MinionServer> asMinionServer() {
        return Optional.of(this);
    }
}
