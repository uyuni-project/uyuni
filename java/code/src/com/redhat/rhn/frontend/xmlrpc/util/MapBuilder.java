/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.frontend.xmlrpc.util;

import com.redhat.rhn.common.util.StringUtil;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Builds a map that could be used to
 * convert a bean object to xml rpc
 * MapBuilder
 */
public class MapBuilder {
    private final Set<String> excludes = new HashSet<>();
    private final Set<String> includes = new HashSet<>();
    private static Logger log = LogManager.getLogger(MapBuilder.class);
    /**
     * Add a debeanified name that must be excluded from the final map
     * @param fieldName the debeanified property name to exclude
     */
    public void exclude(String fieldName) {
        excludes.add(fieldName);
    }

    /**
     *
     * @param fieldName the debeanified property name
     *              to be included in the final map.
     */
    public void include(String fieldName) {
        includes.add(fieldName);
    }


    /**
     * Basically you pass in a bean object to it
     * and it spits out a Map of name value pairs
     * where the name is the debeanified method name
     * and the value is what ever the method returns.
     *
     * Basic operation is (Master List ^ includes) - excludes
     * Where ^ = intersection
     * However there is on catch ...
     * If "includes" is empty, it includes everything..
     * ie... {@literal includes  = empty ==> Master List - excludes}
     *
     * @param bean the bean object to be mapified
     * @return a map containing the debeanified values.
     */
    public Map<String, String> mapify(Object bean) {
        Map<String, String> retval = new HashMap<>();
        try {
            Map<String, String> properties = BeanUtils.describe(bean);
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                if ((includes.isEmpty() || includes.contains(entry.getKey())) && !excludes.contains(entry.getKey())) {
                    if (entry.getValue() != null) {
                        retval.put(StringUtil.debeanify(entry.getKey()), entry.getValue());
                    }
                    else {
                        retval.put(StringUtil.debeanify(entry.getKey()), "");
                    }
                }
            }
        }
        catch (Exception e) {
            log.error(e);
            throw new RuntimeException("Caught error trying to describe a bean. ", e);
        }
        return retval;
    }
}
