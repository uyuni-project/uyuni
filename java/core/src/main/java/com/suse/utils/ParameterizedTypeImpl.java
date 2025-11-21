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
package com.suse.utils;

import java.lang.reflect.Type;

/** Implementation of Java's ParametrizedType for internal use */
public class ParameterizedTypeImpl implements java.lang.reflect.ParameterizedType {

    private final Type owner;
    private final Type raw;
    private final Type[] argumentsTypes;

    /**
     * Construct a parametrized type
     *
     * @param ownerIn the ownerIn type
     * @param rawTypeIn the raw type
     * @param argumentsTypesIn the arguments actual types
     */
    public ParameterizedTypeImpl(Type ownerIn, Type rawTypeIn, Type[] argumentsTypesIn) {
        owner = ownerIn;
        raw = rawTypeIn;
        argumentsTypes = argumentsTypesIn;
    }

    /**
     * Construct a parametrized type for a single argument
     *
     * @param ownerIn the ownerIn type
     * @param rawTypeIn the raw type
     * @param argumentTypeIn the argument actual types
     */
    public ParameterizedTypeImpl(Type ownerIn, Type rawTypeIn, Type argumentTypeIn) {
        this(ownerIn, rawTypeIn, new Type[]{argumentTypeIn});
    }

    @Override
    public Type[] getActualTypeArguments() {
        return argumentsTypes;
    }

    @Override
    public Type getRawType() {
        return raw;
    }

    @Override
    public Type getOwnerType() {
        return owner;
    }
}
