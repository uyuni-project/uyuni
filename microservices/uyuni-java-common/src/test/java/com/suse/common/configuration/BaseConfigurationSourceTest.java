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

package com.suse.common.configuration;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

class BaseConfigurationSourceTest extends AbstractConfigurationSourceTest {

    private Map<String, String> propertiesMap;

    protected ConfigurationSource createConfigurationSource() {

        propertiesMap = Map.ofEntries(
            Map.entry("prefix.simple_value", "value"),
            Map.entry("prefix.with_spaces", "    all trailing spaces  should be  trimmed    "),
            Map.entry("prefix.empty", ""),
            Map.entry("prefix.array_one_element", "some value"),
            Map.entry("prefix.comma_separated", "every,good,boy,does,fine"),
            Map.entry("prefix.comma_no_trim", "every, good , boy , does,fine"),
            Map.entry("prefix.boolean_true", "true"),
            Map.entry("prefix.boolean_false", "false"),
            Map.entry("prefix.boolean_1", "1"),
            Map.entry("prefix.boolean_0", "0"),
            Map.entry("prefix.boolean_y", "y"),
            Map.entry("prefix.boolean_Y", "Y"),
            Map.entry("prefix.boolean_n", "n"),
            Map.entry("prefix.boolean_yes", "yes"),
            Map.entry("prefix.boolean_no", "no"),
            Map.entry("prefix.boolean_foo", "foo"),
            Map.entry("prefix.boolean_10", "10"),
            Map.entry("prefix.boolean_empty", ""),
            Map.entry("prefix.boolean_on", "on"),
            Map.entry("prefix.boolean_off", "off"),
            Map.entry("prefix.int_minus10", "-10"),
            Map.entry("prefix.int_zero", "0"),
            Map.entry("prefix.int_100", "100"),
            Map.entry("prefix.int_y", "y"),
            Map.entry("prefix.double", "10.0"),
            Map.entry("prefix.float", "10.0")
        );

        // Implement a configuration source backed by the map
        return new BaseConfigurationSource() {

            @Override
            protected String getRawValue(String property) {
                return propertiesMap.get(property);
            }

            @Override
            public Set<String> getPropertyNames() {
                return Collections.unmodifiableSet(propertiesMap.keySet());
            }
        };
    }
}
