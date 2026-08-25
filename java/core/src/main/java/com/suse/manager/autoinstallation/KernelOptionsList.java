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

package com.suse.manager.autoinstallation;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Utility class to manage a list of kernel command line options.
 * Preserves insertion order, supports multiple values for the same key, and handles quoted values containing spaces.
 */
public class KernelOptionsList {

    private final List<Map.Entry<String, String>> optionsList = new ArrayList<>();

    /**
     * Default constructor.
     */
    public KernelOptionsList() {
    }

    /**
     * Constructor that parses an initial string of options.
     *
     * @param options The initial kernel options string.
     */
    public KernelOptionsList(String options) {
        if (options == null || options.trim().isEmpty()) {
            return;
        }

        StringBuilder currentToken = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < options.length(); i++) {
            char c = options.charAt(i);

            if (c == '"') {
                insideQuotes = !insideQuotes;
                currentToken.append(c);
            }
            else if (Character.isWhitespace(c) && !insideQuotes) {
                if (!currentToken.isEmpty()) {
                    addOption(currentToken.toString());
                    currentToken.setLength(0); // Reset for the next token
                }
            }
            else {
                currentToken.append(c);
            }
        }

        // Add the last token if there's any
        if (!currentToken.isEmpty()) {
            addOption(currentToken.toString());
        }
    }

    /**
     * Constructor that parses options from a map.
     * Designed for SystemRecord::getResolvedKernelOptions()
     * Values can be a single String or a List of Strings.
     *
     * @param options The map of kernel options.
     */
    public KernelOptionsList(Map<String, Object> options) {
        if (options != null) {
            for (Map.Entry<String, Object> entry : options.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if (value == null) {
                    addOption(key, null);
                }
                else if (value instanceof List<?> list) {
                    if (list.isEmpty()) {
                        addOption(key, null);
                    }
                    else {
                        for (Object item : list) {
                            addOption(key, item == null ? null : item.toString());
                        }
                    }
                }
                else {
                    addOption(key, value.toString());
                }
            }
        }
    }

    /**
     * Sets an option. If the option already exists, behaves according to the replace parameter.
     * If replace is false, the existing value is preserved and the new one is ignored.
     * If replace is true, the first occurrence is updated and all subsequent duplicates of this key are removed.
     * If the key does not exist, it is added.
     *
     * @param key The option key.
     * @param value The option value.
     * @param replace Whether to replace existing entries.
     * @return This instance for method chaining.
     */
    public KernelOptionsList setOption(String key, String value, boolean replace) {
        if (key == null || key.isEmpty()) {
            return this;
        }

        boolean found = false;
        Iterator<Map.Entry<String, String>> iterator = optionsList.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            if (key.equals(entry.getKey())) {
                if (!found) {
                    if (replace) {
                        entry.setValue(value);
                    }
                    found = true;
                }
                else {
                    if (replace) {
                        iterator.remove();
                    }
                }
            }
        }

        if (!found) {
            addOption(key, value);
        }

        return this;
    }

    /**
     * Sets an option. If the option already exists, the new value is ignored.
     *
     * @param key The option key.
     * @param value The option value.
     * @return This instance for method chaining.
     */
    public KernelOptionsList setOptionIfNotPresent(String key, String value) {
        return setOption(key, value, false);
    }

    /**
     * Sets an option. If the option already exists, the new value is ignored.
     *
     * @param flag The option key.
     * @return This instance for method chaining.
     */
    public KernelOptionsList setFlagIfNotPresent(String flag) {
        return setOption(flag, null, false);
    }

    /**
     * Sets an option. If the option already exists, it is replaced
     *
     * @param key The option key.
     * @param value The option value.
     * @return This instance for method chaining.
     */
    public KernelOptionsList setOptionOrReplace(String key, String value) {
        return setOption(key, value, true);
    }

    /**
     * Removes all occurrences of a specified key.
     *
     * @param key The option key to remove.
     * @return This instance for method chaining.
     */
    public KernelOptionsList removeOption(String key) {
        if (key != null && !key.isEmpty()) {
            optionsList.removeIf(stringStringEntry -> key.equals(stringStringEntry.getKey()));
        }
        return this;
    }

    /**
     * Merges another KernelOptionsList into this one.
     * Every option in the 'overrides' list will replace any existing occurrences
     * of the same key in this list, and then the overrides are appended.
     *
     * @param overrides The KernelOptionsList containing override options.
     * @return This instance for method chaining.
     */
    public KernelOptionsList applyOverrides(KernelOptionsList overrides) {
        if (overrides != null) {
            for (Map.Entry<String, String> entry : overrides.optionsList) {
                this.removeOption(entry.getKey());
            }
            this.addOptions(overrides);
        }
        return this;
    }

    /**
     * Merges another KernelOptionsList into this, skipping existing entries
     *
     * @param options The KernelOptionsList containing override options.
     * @return This instance for method chaining.
     */
    public KernelOptionsList addMissingOptions(KernelOptionsList options) {
        if (options != null) {
            for (Map.Entry<String, String> entry : options.optionsList) {
                this.setOptionIfNotPresent(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    /**
     * Adds an option with a key and a value.
     * Multiple values for the same key are allowed.
     *
     * @param key The option key.
     * @param value The option value (can be null for a flag option).
     * @return This instance for method chaining.
     */
    public KernelOptionsList addOption(String key, String value) {
        if (key != null && !key.isEmpty()) {
            optionsList.add(new AbstractMap.SimpleEntry<>(key, value));
        }
        return this;
    }

    /**
     * Adds a single option parsed from a string, which can be a key=value pair or a flag.
     *
     * @param param The option string to add.
     * @return This instance for method chaining.
     */
    public KernelOptionsList addOption(String param) {
        if (param == null || param.trim().isEmpty()) {
            return this;
        }

        int equalsIndex = param.indexOf('=');
        if (equalsIndex == -1) {
            addOption(param, null);
        }
        else {
            String key = param.substring(0, equalsIndex);
            String value = param.substring(equalsIndex + 1);
            addOption(key, value);
        }
        return this;
    }

    /**
     * Adds all options from another KernelOptionsList.
     *
     * @param other The other KernelOptionsList from which to add options.
     * @return This instance for method chaining.
     */
    public KernelOptionsList addOptions(KernelOptionsList other) {
        if (other != null) {
            for (Map.Entry<String, String> entry : other.optionsList) {
                addOption(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    /**
     * Returns true if the option already exists
     * @param key The option name to lookup.
     * @return true if option is found.
     */
    public boolean hasOption(String key) {
        if (key != null && !key.isEmpty()) {
            for (Map.Entry<String, String> stringStringEntry : optionsList) {
                if (key.equals(stringStringEntry.getKey())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if the option already exists and has the value
     * @param key The option name to lookup.
     * @param value Value to check for the option.
     * @return true if option is found.
     */
    public boolean hasOption(String key, String value) {
        if (key != null && !key.isEmpty()) {
            for (Map.Entry<String, String> stringStringEntry : optionsList) {
                if (key.equals(stringStringEntry.getKey()) &&
                        stringStringEntry.getValue() != null &&
                        stringStringEntry.getValue().equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if there is no option set
     * @return true if option list it empty
     */
    public boolean isEmpty() {
        return optionsList.isEmpty();
    }

    /**
     * Returns the kernel options as a space-separated string.
     *
     * @return The formatted kernel options string.
     */
    @Override
    public String toString() {
        if (optionsList.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : optionsList) {
            if (!sb.isEmpty()) {
                sb.append(" ");
            }
            sb.append(entry.getKey());
            if (entry.getValue() != null) {
                sb.append("=").append(entry.getValue());
            }
        }
        return sb.toString();
    }
}
