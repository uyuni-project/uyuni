/*
 * Copyright (c) 2021--2024 SUSE LLC
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
package com.redhat.rhn.domain;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.function.Function;

/**
 * Allows to maps enum in a custom way
 * @param <T> enum class
 * @param <K> type class
 */
public abstract class CustomEnumType<T extends Enum<T>, K> implements UserType {

    private final Class<T> enumClass;

    private final Class<K> typeClass;
    private final Function<T, K> toDb;
    private final Function<K, T> fromDb;

    /**
     * Constructor
     * @param enumClassIn the enum class
     * @param typeClassIn the type class to store in the database. Currently supported: String, Integer
     * @param toDbIn to db
     * @param fromDbIn from db
     */
    protected CustomEnumType(Class<T> enumClassIn, Class<K> typeClassIn, Function<T, K> toDbIn,
                             Function<K, T> fromDbIn) {
        // Enforce type check
        if (!typeClassIn.equals(Integer.class) && !typeClassIn.equals(String.class)) {
            throw new IllegalArgumentException("Unsupported type class " + typeClassIn.getSimpleName());
        }

        this.enumClass = enumClassIn;
        this.typeClass = typeClassIn;
        this.toDb = toDbIn;
        this.fromDb = fromDbIn;
    }

    /**
     * Defines the sql type to use
     * @return a value from {@link java.sql.SQLType}
     */
    public int getSqlType() {
        if (typeClass.equals(String.class)) {
            return Types.VARCHAR;
        }

        // Return numeric as type check is enforced in the constructor
        return Types.NUMERIC;
    }

    @Override
    public int[] sqlTypes() {
        return new int[]{getSqlType()};
    }

    @Override
    public Class<T> returnedClass() {
        return enumClass;
    }

    @Override
    public boolean equals(Object o, Object o1) throws HibernateException {
        return o == o1;
    }

    @Override
    public int hashCode(Object o) throws HibernateException {
        return o == null ? 0 : o.hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet resultSet, String[] names, SharedSessionContractImplementor session, Object o)
            throws HibernateException, SQLException {
        String name = names[0];
        K value = resultSet.getObject(name, typeClass);
        if (resultSet.wasNull()) {
            return null;
        }
        else {
            return fromDb.apply(value);
        }
    }

    @Override
    public void nullSafeSet(PreparedStatement statement, Object value, int position,
            SharedSessionContractImplementor session) throws HibernateException, SQLException {
        K jdbcValue = value == null ? null : toDb.apply(enumClass.cast(value));
        if (jdbcValue == null) {
            statement.setNull(position, 12);
        }
        else {
            statement.setObject(position, jdbcValue, getSqlType());
        }
    }

    @Override
    public Object deepCopy(Object o) throws HibernateException {
        return o;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Object o) throws HibernateException {
        return (Serializable)o;
    }

    @Override
    public Object assemble(Serializable serializable, Object o) throws HibernateException {
        return serializable;
    }

    @Override
    public Object replace(Object o, Object o1, Object o2) throws HibernateException {
        return o;
    }
}
