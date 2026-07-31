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

import java.util.List;

/**
 * A JSON representation of the CoCo attestation configuration for multiple servers, used to
 * transfer the data to the UI.
 *
 * @param serverIds the list of server ids the configuration applies to
 * @param settings the {@link CoCoSettingsJson} to apply to the servers
 */
public record SystemsCoCoSettingsJson(List<Long> serverIds, CoCoSettingsJson settings) {
}
