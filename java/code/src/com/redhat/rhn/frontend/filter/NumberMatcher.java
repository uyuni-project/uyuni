/*
 * Copyright (c) 2022 SUSE LLC
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
package com.redhat.rhn.frontend.filter;

import com.redhat.rhn.common.util.MethodUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

/**
 * Matcher for numbers, handling comparisons, not only equality.
 */
public class NumberMatcher implements Matcher {

    private static final Logger LOG = LogManager.getLogger(NumberMatcher.class);

    // First parameter is the value to test, the second is the criteria
    private static final Map<String, BiFunction<Number, Number, Boolean>> COMPARATORS = Map.of(
            "<", (a, b) -> a.doubleValue() < b.doubleValue(),
            ">", (a, b) -> a.doubleValue() > b.doubleValue(),
            "<=", (a, b) -> a.doubleValue() <= b.doubleValue(),
            ">=", (a, b) -> a.doubleValue() >= b.doubleValue(),
            "!=", (a, b) -> a.doubleValue() != b.doubleValue(),
            "=", (a, b) -> a.doubleValue() == b.doubleValue(),
            "==", (a, b) -> a.doubleValue() == b.doubleValue(),
            "", (a, b) -> a.doubleValue() == b.doubleValue()
    );

    @Override
    public boolean include(Object obj, String filterData, String filterColumn) {
        if (StringUtils.isBlank(filterData) ||
                StringUtils.isBlank(filterColumn)) {
            return true; ///show all if I entered a blank value
        }

        java.util.regex.Matcher matcher = Pattern.compile("^([<>!=]*) *(-?[0-9.]+)$").matcher(filterData.trim());
        if (!matcher.matches()) {
            return true;
        }

        String operator = matcher.group(1) != null ? matcher.group(1) : "";
        Double criteria = Double.parseDouble(matcher.group(2));

        Object value = null;
        try {
            value = MethodUtil.getAccessor(obj, filterColumn).invoke(obj);
        }
        catch (Exception eIn) {
            LOG.error("Could not find function for {}.{} field", obj.getClass().getName(), filterColumn);
        }

        if (value != null && value instanceof Number) {
            return COMPARATORS.get(operator).apply((Number)value, criteria);
        }

        return false;
    }
}
