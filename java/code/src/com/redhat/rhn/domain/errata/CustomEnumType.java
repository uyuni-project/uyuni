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
import java.util.function.Function;

/**
 * CustomEnumType
 * @param <T> type
 */
public class CustomEnumType<T extends Enum<T>> implements UserType {

    private final Class<T> clazz;
    private final Function<T, String> toDb;
    private final Function<String, T> fromDb;

    /**
     * Constructor
     * @param clazzIn the class
     * @param toDbIn to db
     * @param fromDbIn from db
     */
    public CustomEnumType(Class<T> clazzIn, Function<T, String> toDbIn, Function<String, T> fromDbIn) {
        this.clazz = clazzIn;
        this.toDb = toDbIn;
        this.fromDb = fromDbIn;
    }

    @Override
    public int[] sqlTypes() {
        return new int[]{12};
    }

    @Override
    public Class<T> returnedClass() {
        return clazz;
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
        String value = resultSet.getString(name);
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
        String jdbcValue = value == null ? null : toDb.apply((T)value);
        if (jdbcValue == null) {
            statement.setNull(position, 12);
        }
        else {
            statement.setString(position, jdbcValue);
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
