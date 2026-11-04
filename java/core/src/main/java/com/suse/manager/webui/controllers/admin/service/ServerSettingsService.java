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

package com.suse.manager.webui.controllers.admin.service;

import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.common.validator.ValidatorResult;
import com.redhat.rhn.domain.common.RhnConfiguration;
import com.redhat.rhn.domain.common.RhnConfigurationFactory;

import com.suse.manager.webui.controllers.admin.beans.ServerSettingsEntry;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lists and changes the configuration values stored in the database.
 * Hidden keys are not listed and can't be changed.
 */
public class ServerSettingsService {

    private final RhnConfigurationFactory factory = RhnConfigurationFactory.getSingleton();

    /**
     * @return the settings of all keys which are not hidden, sorted by key name
     */
    public List<ServerSettingsEntry> list() {
        return factory.listVisible().stream()
                .map(this::toEntry)
                .sorted(Comparator.comparing(ServerSettingsEntry::key))
                .toList();
    }

    /**
     * Change the values of the given keys. A blank value resets the key to the default value.
     * Nothing is changed when one of the values is invalid.
     *
     * @param valuesIn the new values by key name
     * @throws ValidatorException when a key is unknown or hidden, or a value doesn't match the type of the key
     */
    public void update(Map<String, String> valuesIn) throws ValidatorException {
        Map<RhnConfiguration, String> items = new HashMap<>();

        ValidatorResult result = new ValidatorResult();
        valuesIn.forEach((name, value) -> {
            RhnConfiguration configuration = factory.lookupVisibleConfiguration(name);
            configuration.validate(value, result);
            items.put(configuration, value);
        });
        if (result.hasErrors()) {
            throw new ValidatorException(result);
        }

        items.forEach(RhnConfiguration::setValue);
    }

    /**
     * Reset a key to the default value
     *
     * @param nameIn the key name
     * @throws ValidatorException when the key is unknown or hidden
     */
    public void reset(String nameIn) throws ValidatorException {
        RhnConfiguration configuration = factory.lookupVisibleConfiguration(nameIn);
        configuration.resetToDefault();
    }

    //CARLO: this method should "in theory" be transferred into a different "translator" object
    // this is called the "adapter" pattern (what Ricardo refers as "Mapper")
    private ServerSettingsEntry toEntry(RhnConfiguration configuration) {
        return new ServerSettingsEntry(configuration.getUIName(), configuration.getTranslatedDescription(),
                configuration.getUIType(), configuration.getUIValue(),
                configuration.getUIDefaultValue(), configuration.isUIModified());
    }

}
