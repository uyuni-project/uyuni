/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.suse.manager.webui.controllers.admin.beans;

import com.redhat.rhn.domain.common.RhnConfiguration;

/**
 * A server setting as shown in the UI. Boolean values are normalized to "true" and "false".
 * @param key the configuration key
 * @param description the description
 * @param type the type of the value
 * @param value the effective value, the default value when no value is set
 * @param defaultValue the default value
 * @param modified true when a value is set, false when the default value is used
 */
public record ServerSettingsEntry(String key, String description, RhnConfiguration.ValueType type, String value,
                                  String defaultValue, boolean modified) {
}
