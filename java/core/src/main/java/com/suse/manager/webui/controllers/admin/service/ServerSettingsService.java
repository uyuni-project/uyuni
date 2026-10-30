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

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.common.validator.ValidatorResult;
import com.redhat.rhn.domain.common.RhnConfiguration;
import com.redhat.rhn.domain.common.RhnConfigurationFactory;

import com.suse.manager.webui.controllers.admin.beans.ServerSettingsEntry;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        return factory.listAll().stream()
                .filter(configuration -> !configuration.getKey().isHidden())
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
        ValidatorResult result = new ValidatorResult();
        Map<RhnConfiguration.KEYS, Object> values = new EnumMap<>(RhnConfiguration.KEYS.class);
        valuesIn.forEach((name, value) -> lookupKey(name, result)
                .ifPresent(key -> parseValue(key, value, result).ifPresent(v -> values.put(key, v))));
        if (result.hasErrors()) {
            throw new ValidatorException(result);
        }
        values.forEach(factory::setValue);
    }

    /**
     * Reset a key to the default value
     *
     * @param nameIn the key name
     * @throws ValidatorException when the key is unknown or hidden
     */
    public void reset(String nameIn) throws ValidatorException {
        ValidatorResult result = new ValidatorResult();
        Optional<RhnConfiguration.KEYS> key = lookupKey(nameIn, result);
        if (key.isEmpty()) {
            throw new ValidatorException(result);
        }
        factory.resetToDefault(key.get());
    }

    private ServerSettingsEntry toEntry(RhnConfiguration configuration) {
        RhnConfiguration.KEYS key = configuration.getKey();
        boolean isBoolean = key.getType() == RhnConfiguration.ValueType.BOOLEAN;
        String value = isBoolean ? String.valueOf(factory.getBoolean(key)) : factory.getString(key);
        String defaultValue = isBoolean ? String.valueOf(factory.getDefaultBoolean(key)) :
                factory.getDefaultString(key);
        return new ServerSettingsEntry(key.name(), getDescription(configuration), key.getType(), value,
                defaultValue, StringUtils.isNotBlank(configuration.getValue()));
    }

    /**
     * @return the translated description, the description from the database when there is no translation
     */
    private static String getDescription(RhnConfiguration configuration) {
        LocalizationService ls = LocalizationService.getInstance();
        String messageId = getDescriptionMessageId(configuration.getKey());
        return ls.hasMessage(messageId) ? ls.getMessage(messageId) : configuration.getDescription();
    }

    /**
     * @param key the configuration key
     * @return the id of the translatable description in the database string resources
     */
    public static String getDescriptionMessageId(RhnConfiguration.KEYS key) {
        return "server_settings.description." + key.name();
    }

    private Optional<RhnConfiguration.KEYS> lookupKey(String nameIn, ValidatorResult result) {
        Optional<RhnConfiguration.KEYS> key = Arrays.stream(RhnConfiguration.KEYS.values())
                .filter(k -> k.name().equals(nameIn))
                .findFirst();
        if (key.isEmpty()) {
            result.addError("server_settings.error.unknown_key", nameIn);
        }
        else if (key.get().isHidden()) {
            result.addError("server_settings.error.hidden_key", nameIn);
            return Optional.empty();
        }
        return key;
    }

    /**
     * @return the value to store (an empty string resets to the default value), empty when the value is invalid
     */
    private Optional<Object> parseValue(RhnConfiguration.KEYS key, String valueIn, ValidatorResult result) {
        String value = StringUtils.trimToEmpty(valueIn);
        if (value.isEmpty()) {
            return Optional.of(value);
        }
        switch (key.getType()) {
            case INTEGER -> {
                try {
                    return Optional.of(Long.parseLong(value));
                }
                catch (NumberFormatException e) {
                    result.addFieldError(key.name(), "server_settings.error.integer", key.name());
                    return Optional.empty();
                }
            }
            case BOOLEAN -> {
                // stored as Boolean, so it is compared with the default value by meaning
                if (!value.equals("true") && !value.equals("false")) {
                    result.addFieldError(key.name(), "server_settings.error.boolean", key.name());
                    return Optional.empty();
                }
                return Optional.of(Boolean.valueOf(value));
            }
            default -> {
                return Optional.of(value);
            }
        }
    }
}
