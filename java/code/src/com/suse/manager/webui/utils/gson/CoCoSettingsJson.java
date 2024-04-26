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
import com.suse.manager.model.attestation.ServerCoCoAttestationConfig;

import java.util.Objects;
import java.util.StringJoiner;

public class CoCoSettingsJson {
    private final boolean supported;

    private final boolean enabled;

    private final CoCoEnvironmentType environmentType;

    private final boolean attestOnBoot;

    /**
     * Builds a json configuration from an existing attestation config
     * @param attestationConfig the current attestation configuration
     */
    public CoCoSettingsJson(ServerCoCoAttestationConfig attestationConfig) {
        this(true, attestationConfig.isEnabled(), attestationConfig.getEnvironmentType(),
            attestationConfig.isAttestOnBoot());
    }

    /**
     * Creates an empty json configuration
     * @param supportedIn if confidential computing is supported
     */
    public CoCoSettingsJson(boolean supportedIn) {
        this(supportedIn, false, CoCoEnvironmentType.NONE, false);
    }

    /**
     * Default constructor
     * @param supportedIn if confidential computing is supported
     * @param enabledIn if the configuration is enabled
     * @param environmentTypeIn the environment type
     * @param attestOnBootIn true if attestation is performed on boot
     */
    public CoCoSettingsJson(boolean supportedIn, boolean enabledIn, CoCoEnvironmentType environmentTypeIn,
                            boolean attestOnBootIn) {
        this.supported = supportedIn;
        this.enabled = enabledIn;
        this.environmentType = environmentTypeIn;
        this.attestOnBoot = attestOnBootIn;
    }

    public boolean isSupported() {
        return supported;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public CoCoEnvironmentType getEnvironmentType() {
        return environmentType;
    }

    public boolean isAttestOnBoot() {
        return attestOnBoot;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CoCoSettingsJson)) {
            return false;
        }
        CoCoSettingsJson that = (CoCoSettingsJson) o;
        return supported == that.supported &&
            enabled == that.enabled &&
            attestOnBoot == that.attestOnBoot &&
            environmentType == that.environmentType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(supported, enabled, environmentType, attestOnBoot);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CoCoSettingsJson.class.getSimpleName() + "[", "]")
            .add("supported=" + isSupported())
            .add("enabled=" + isEnabled())
            .add("environmentType=" + getEnvironmentType())
            .add("attestOnBoot=" + isAttestOnBoot())
            .toString();
    }
}
