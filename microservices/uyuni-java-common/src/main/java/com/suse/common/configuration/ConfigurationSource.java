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

import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

public interface ConfigurationSource {

    Optional<Boolean> getBoolean(String property);

    Optional<String> getString(String property);

    Optional<Integer> getInteger(String property);

    Optional<Long> getLong(String property);

    Optional<Float> getFloat(String property);

    Optional<Double> getDouble(String property);

    <T> Optional<List<T>> getList(String property, Class<T> itemClass);

    Set<String> getPropertyNames();

    Properties toProperties();
}
