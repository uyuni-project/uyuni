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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.common.validator;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *  The <code>DataConverter</code> class provides utility
 *    methods to convert XML Schema data types to Java
 *    types and Java data types to XML Schema types.
 * </p>
 *
 */
public class DataConverter {

    /** Singleton instance */
    private static DataConverter instance = null;

    /** Mappings from Java to XML Schema */
    private final Map<String, String> schemaMappings;

    /** Mappings from XML Schema to Java */
    private final Map<String, String> javaMappings;

    /**
     * <p>
     *  This (intentionally left) private constructor handles initialization
     *    of the data mappings. It can only be created internally, and forces
     *    the singleton pattern to be used.
     * </p>
     */
    private DataConverter() {
        schemaMappings = getSchemaMappings();
        javaMappings = new HashMap<>();
        for (Map.Entry<String, String> entry : schemaMappings.entrySet()) {
            javaMappings.put(entry.getValue(), entry.getKey());
        }
    }

    /**
     * <p>
     *  This will retrieve the singleton instance of this class, allowing
     *    it to be used across applications.
     * </p>
     *
     * @return <code>DataConverter</code> - the singleton instance to use.
     */
    public static DataConverter getInstance() {
        if (instance == null) {
            synchronized (DataConverter.class) {
                instance = new DataConverter();
            }
        }
        return instance;
    }

    /**
     * <p>
     *  This will return the Java data type given an XML Schema data type.
     * </p>
     *
     * @param schemaType XML Schema data type (<code>String</code> format).
     * @return <code>String</code> - Java data type that is comparable.
     */
    public String getJavaType(String schemaType) {
        return javaMappings.get(schemaType);
    }

    /**
     * <p>
     *  This will return the XML Schema data type given a Java data type.
     * </p>
     *
     * @param javaType Java data type (<code>String</code> format).
     * @return <code>String</code> - XML Schema data type that is comparable.
     */
    public String getSchemaType(String javaType) {
        return schemaMappings.get(javaType);
    }

    /**
     * <p>
     *  This will generate the data mappings from XML Schema to Java.
     * </p>
     *
     * @return <code>Map</code> - data type mappings.
     */
    private Map<String, String> getSchemaMappings() {
        Map<String, String> map = new HashMap<>();

        // Key is Java type, value is XML Schema type
        map.put("String", "string");
        map.put("boolean", "boolean");
        map.put("float", "float");
        map.put("double", "double");

        map.put("Long", "long");
        map.put("long", "long");
        map.put("int", "int");
        map.put("short", "short");
        map.put("byte", "byte");
        map.put("requiredIf", "requiredIf");
        map.put("Date", "date");

        return map;
    }
}
