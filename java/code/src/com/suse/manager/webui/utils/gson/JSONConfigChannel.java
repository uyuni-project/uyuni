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
package com.suse.manager.webui.utils.gson;

import com.redhat.rhn.domain.config.ConfigChannel;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * JSON representation of a state config channel.
 */
public class JSONConfigChannel {

    private Long id;
    private String name;
    private String label;
    private String type;
    private Integer position;
    private boolean assigned;

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param idIn the id
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * Instantiates a new unassigned channel object
     *
     * @param channelIn the channel
     */
    public JSONConfigChannel(ConfigChannel channelIn) {
        this.id = channelIn.getId();
        this.name = channelIn.getName();
        this.label = channelIn.getLabel();
        this.type = channelIn.getConfigChannelType().getLabel();
        this.position = null;
        this.assigned = false;
    };

    /**
     * Instantiates a new channel object assigned in a specific position
     *
     * @param channelIn the channel
     * @param positionIn the ordering of the channel
     */
    public JSONConfigChannel(ConfigChannel channelIn, int positionIn) {
        this(channelIn);
        this.position = positionIn;
        this.assigned = true;
    }

    /**
     * @return the name of the config channel
     */
    public String getName() {
        return name;
    }

    /**
     * @param nameIn the name of the config channel
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param labelIn the label
     */
    public void setLabel(String labelIn) {
        this.label = labelIn;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param typeIn the type
     */
    public void setType(String typeIn) {
        this.type = typeIn;
    }

    /**
     * @return the position
     */
    public Integer getPosition() {
        return position;
    }

    /**
     * @param positionIn the position
     */
    public void setPosition(Integer positionIn) {
        this.position = positionIn;
    }

    /**
     * @return true if the channel is assigned
     */
    public boolean isAssigned() {
        return assigned;
    }

    /**
     * @param assignedIn true if channel is assigned
     */
    public void setAssigned(boolean assignedIn) {
        this.assigned = assignedIn;
    }

    /**
     * Creates a list of {@link JSONConfigChannel} objects with position values in the order of input
     * @param channelsIn list of config channels to be included in the list
     * @return the list of {@link JSONConfigChannel} objects
     */
    public static List<JSONConfigChannel> listOrdered(List<ConfigChannel> channelsIn) {
        return IntStream.range(0, channelsIn.size())
                .mapToObj(i -> new JSONConfigChannel(channelsIn.get(i), i + 1))
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof JSONConfigChannel)) {
            return false;
        }
        JSONConfigChannel castOther = (JSONConfigChannel) other;
        return new EqualsBuilder()
                .append(name, castOther.name)
                .append(label, castOther.label)
                .append(type, castOther.type)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(name)
                .append(label)
                .append(type).toHashCode();
    }
}
