/*
 * Copyright (c) 2021 SUSE LLC
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
package com.redhat.rhn.domain.errata;

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
    public K nullSafeGet(ResultSet var1, int var2, SharedSessionContractImplementor var3,
            @Deprecated Object var4)
            throws SQLException {
        return null;
    }

    @Override
    public void nullSafeSet(PreparedStatement var1, Object var2, int var3,
            SharedSessionContractImplementor var4)
            throws SQLException {
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
