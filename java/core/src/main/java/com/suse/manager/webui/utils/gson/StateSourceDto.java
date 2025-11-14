/*
 * Copyright (c) 2021 SUSE LLC
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
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.manager.configuration.SaltConfigurable;

public class StateSourceDto {
    private enum Type {
        STATE,
        CONFIG,
        FORMULA,
        INTERNAL
    }

    private enum SourceType {
        SYSTEM,
        GROUP,
        ORG
    }

    private Long id;
    private String name;
    private Type type;
    private Long sourceId;
    private String sourceName;
    private SourceType sourceType;

    private StateSourceDto(Long idIn, String nameIn, Type typeIn, Long sourceIdIn, String sourceNameIn,
            SourceType sourceTypeIn) {
        this.id = idIn;
        this.name = nameIn;
        this.type = typeIn;
        this.sourceId = sourceIdIn;
        this.sourceName = sourceNameIn;
        this.sourceType = sourceTypeIn;
    }

    private StateSourceDto(ConfigChannel channelIn, Long sourceIdIn, String sourceNameIn, SourceType sourceTypeIn) {
        this(channelIn.getId(), channelIn.getDisplayName(), channelIn.isStateChannel() ? Type.STATE : Type.CONFIG,
                sourceIdIn, sourceNameIn, sourceTypeIn);
    }

    /**
     * Return a new state source object for internal states
     *
     * @return a new {@link StateSourceDto} object
     */
    public static StateSourceDto internalState() {
        return new StateSourceDto(null, null, Type.INTERNAL, null, null, null);
    }

    /**
     * Return a new state source object for a config or state channel assigned via any kind of {@link SaltConfigurable}
     *
     * @param channel the config or state channel
     * @param configurable the assigned entity
     * @return a new {@link StateSourceDto} object
     */
    public static StateSourceDto sourceFrom(ConfigChannel channel, SaltConfigurable configurable) {
        SourceType type;
        if (configurable instanceof MinionServer) {
            type = SourceType.SYSTEM;
        }
        else if (configurable instanceof ServerGroup) {
            type = SourceType.GROUP;
        }
        else if (configurable instanceof Org) {
            type = SourceType.ORG;
        }
        else {
            throw new IllegalArgumentException("Invalid state source");
        }

        return new StateSourceDto(channel, configurable.getId(), configurable.getName(), type);
    }

    /**
     * Return a new state source object for a formula assigned via any kind of {@link SaltConfigurable}
     *
     * @param formulaIndex the index of the formula to be used for frontend linking
     * @param formulaName the name of the formula
     * @param configurable the assigned entity
     * @return a new {@link StateSourceDto} object
     */
    public static StateSourceDto sourceFrom(Integer formulaIndex, String formulaName, SaltConfigurable configurable) {
        Long formulaId = formulaIndex != null ? Long.valueOf(formulaIndex) : null;
        SourceType type;
        if (configurable instanceof MinionServer) {
            type = SourceType.SYSTEM;
        }
        else if (configurable instanceof ServerGroup) {
            type = SourceType.GROUP;
        }
        else {
            throw new IllegalArgumentException("Invalid formula source");
        }

        return new StateSourceDto(formulaId, formulaName, Type.FORMULA, configurable.getId(), configurable.getName(),
                type);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type.toString();
    }

    public Long getSourceId() {
        return sourceId;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getSourceType() {
        return sourceType.toString();
    }
}
