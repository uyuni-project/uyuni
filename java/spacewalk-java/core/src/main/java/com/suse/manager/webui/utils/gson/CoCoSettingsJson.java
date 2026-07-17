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

import java.util.Map;

/**
 * A JSON representation of the CoCo attestation configuration
 *
 * @param enabled if the configuration is enabled
 * @param environmentType the environment type
 * @param inputData the optional input data for the attestation
 * @param attestOnBoot true if attestation is performed on boot
*/
public record CoCoSettingsJson(
    boolean enabled,
    CoCoEnvironmentType environmentType,
    Map<String, Object> inputData,
    boolean attestOnBoot) {

    /**
     * Builds a json configuration from an existing attestation config
     * @param attestationConfig the current attestation configuration
     */
    public CoCoSettingsJson(ServerCoCoAttestationConfig attestationConfig) {
        this(attestationConfig.isEnabled(), attestationConfig.getEnvironmentType(),
            attestationConfig.getInData(), attestationConfig.isAttestOnBoot());
    }

    /**
     * Creates an empty json configuration
     */
    public CoCoSettingsJson() {
        this(false, CoCoEnvironmentType.getDefault(), Map.of(), false);
    }
}
