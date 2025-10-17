/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.domain.channel;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serial;
import java.io.Serializable;

public class ReleaseChannelMapId implements Serializable {

    @Serial
    private static final long serialVersionUID = 1402976102911713357L;

    private String product;

    private String version;

    private String release;

    private ChannelArch channelArch;

    private Channel channel;

    /**
     * Constructor
     */
    public ReleaseChannelMapId() {
    }

    /**
     * Constructor
     *
     * @param productIn     the input product
     * @param versionIn     the input version
     * @param releaseIn     the input release
     * @param channelArchIn the input channelArch
     * @param channelIn     the input channel
     */
    public ReleaseChannelMapId(String productIn, String versionIn, String releaseIn,
                               ChannelArch channelArchIn, Channel channelIn) {
        product = productIn;
        version = versionIn;
        release = releaseIn;
        channelArch = channelArchIn;
        channel = channelIn;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String productIn) {
        product = productIn;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String versionIn) {
        version = versionIn;
    }

    public String getRelease() {
        return release;
    }

    public void setRelease(String releaseIn) {
        release = releaseIn;
    }

    public ChannelArch getChannelArch() {
        return channelArch;
    }

    public void setChannelArch(ChannelArch channelArchIn) {
        channelArch = channelArchIn;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channelIn) {
        channel = channelIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (!(oIn instanceof ReleaseChannelMapId that)) {
            return false;
        }

        return new EqualsBuilder()
                .append(product, that.product)
                .append(version, that.version)
                .append(release, that.release)
                .append(channelArch, that.channelArch)
                .append(channel, that.channel)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(product)
                .append(version)
                .append(release)
                .append(channelArch)
                .append(channel)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "ReleaseChannelMapId{" +
                "product='" + product + '\'' +
                ", version='" + version + '\'' +
                ", release='" + release + '\'' +
                ", channelArch=" + channelArch +
                ", channel=" + channel +
                '}';
    }
}
