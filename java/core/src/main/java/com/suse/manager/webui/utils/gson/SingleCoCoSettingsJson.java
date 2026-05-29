/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.webui.utils.gson;

import com.suse.manager.model.attestation.ServerCoCoAttestationConfig;

/**
 * A JSON representation of the CoCo attestation configuration for single servers, used to
 * transfer the data to the UI.
 *
 * @param supported if confidential computing is supported on the server
 * @param settings the {@link CoCoSettingsJson} to apply to the servers
 */
public record SingleCoCoSettingsJson(boolean supported, CoCoSettingsJson settings) {

    /**
     * Create an empty response
     * @param supportedIn
     */
    public SingleCoCoSettingsJson(boolean supportedIn) {
        this(supportedIn, new CoCoSettingsJson());
    }

    /**
     * Create a response from the given configuration
     * @param attestationConfig
     */
    public SingleCoCoSettingsJson(ServerCoCoAttestationConfig attestationConfig) {
        this(true, new CoCoSettingsJson(attestationConfig));
    }
}
