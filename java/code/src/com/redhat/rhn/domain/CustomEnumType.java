/*
 * Copyright (c) 2021--2024 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain;

import org.hibernate.HibernateException;
import org.hibernate.type.descriptor.WrapperOptions;
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
 */
public abstract class CustomEnumType<T extends Enum<T>> implements UserType<T> {

    private final Class<T> enumClass;

    private final Function<T, String> toDb;
    private final Function<String, T> fromDb;

    /**
     * Constructor
     * @param enumClassIn the enum class
     * @param toDbIn to db
     * @param fromDbIn from db
     */
    protected CustomEnumType(Class<T> enumClassIn, Function<T, String> toDbIn, Function<String, T> fromDbIn) {
        this.enumClass = enumClassIn;
        this.toDb = toDbIn;
        this.fromDb = fromDbIn;
    }

    /**
     * Defines the sql type to use
     * @return a value from {@link java.sql.SQLType}
     */
    public int getSqlType() {
            return Types.VARCHAR;
        }

    @Override
    public Class<T> returnedClass() {
        return enumClass;
    }

    @Override
    public boolean equals(T o, T o1) throws HibernateException {
        return o == o1;
    }

    @Override
    public int hashCode(T o) throws HibernateException {
        return o == null ? 0 : o.hashCode();
    }

    @Override
    public T nullSafeGet(ResultSet resultSet, int position, WrapperOptions options) throws SQLException {
        String value = resultSet.getObject(position, String.class);
        if (resultSet.wasNull()) {
            return null;
        }
        else {
            return fromDb.apply(value);
        }
    }

    @Override
    public void nullSafeSet(PreparedStatement statement, T value, int position, WrapperOptions options) throws SQLException {
        String jdbcValue = value == null ? null : toDb.apply(enumClass.cast(value));
        if (jdbcValue == null) {
            statement.setNull(position, 12);
        }
        else {
            statement.setObject(position, jdbcValue, getSqlType());
        }
    }

    @Override
    public T deepCopy(T o) throws HibernateException {
        return o;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(T o) throws HibernateException {
        return (Serializable)o;
    }

    @Override
    public T assemble(Serializable serializable, Object o) throws HibernateException {
        return (T)serializable;
    }

    @Override
    public T replace(T o, T o1, Object o2) throws HibernateException {
        return o;
    }
}
