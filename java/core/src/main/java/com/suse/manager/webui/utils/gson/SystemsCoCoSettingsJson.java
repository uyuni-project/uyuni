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

package com.suse.manager.webui.utils.gson;

import com.suse.manager.model.attestation.CoCoEnvironmentType;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class SystemsCoCoSettingsJson extends CoCoSettingsJson {

    private final List<Long> serverIds;

    /**
     * Default constructor
     * @param serverIdIn the list of servers id
     * @param supportedIn if confidential computing is supported
     * @param enabledIn if the configuration is enabled
     * @param environmentTypeIn the environment type
     * @param attestOnBootIn true if attestation is performed on boot
     */
    public SystemsCoCoSettingsJson(List<Long> serverIdIn, boolean supportedIn, boolean enabledIn,
                                   CoCoEnvironmentType environmentTypeIn, boolean attestOnBootIn) {
        super(supportedIn, enabledIn, environmentTypeIn, attestOnBootIn);
        this.serverIds = serverIdIn;
    }

    public List<Long> getServerIds() {
        return serverIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SystemsCoCoSettingsJson that)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        return Objects.equals(serverIds, that.serverIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), serverIds);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SystemsCoCoSettingsJson.class.getSimpleName() + "[", "]")
            .add("serverIds=" + getServerIds())
            .add("supported=" + isSupported())
            .add("enabled=" + isEnabled())
            .add("environmentType=" + getEnvironmentType())
            .add("attestOnBoot=" + isAttestOnBoot())
            .toString();
    }
}
