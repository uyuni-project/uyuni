/*
 * Copyright (c) 2023 SUSE LLC
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

package com.redhat.rhn.testing;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.rhn.common.hibernate.HibernateFactory;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import java.io.Serializable;

/**
 * Utils for handling hibernate entities during the session. Implemented as interface default methods in order
 * to be shared among {@link  RhnBaseTestCase} and {@link RhnJmockBaseTestCase}
 */
public interface HibernateTestCaseUtils {

    /**
     * Clears hibernate session
     */
    default void clearSession() {
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();
    }

    /**
     * PLEASE Refrain from using this unless you really have to.
     *
     * Try clearSession() instead
     * @throws HibernateException hibernate exception
     */
    default void commitAndCloseSession() throws HibernateException {
        HibernateFactory.commitTransaction();
        HibernateFactory.closeSession();
    }

    /**
     * Flush an object and removes it from the session
     * @param obj the object
     * @throws HibernateException hibernate exception
     */
    default void flushAndEvict(Object obj) throws HibernateException {
        Session session = HibernateFactory.getSession();
        session.flush();
        session.evict(obj);
    }

    /**
     * Reload a Hibernate entity.
     * @param objClass The class of the object
     * @param id the id
     * @return the request object reloaded
     * @param <T> the type of the object
     * @throws HibernateException hibernate exception
     */
    default <T> T reload(Class<T> objClass, Serializable id) throws HibernateException {
        assertNotNull(id);
        T obj = TestUtils.reload(objClass, id);
        return reload(obj);
    }

    /**
     * Reload a Hibernate entity.
     * @param obj the entity to reload
     * @param <T> type of object to reload
     * @return the new instance
     * @throws HibernateException in case of error
     */
    default <T> T reload(T obj) throws HibernateException {
        assertNotNull(obj);
        return TestUtils.reload(obj);
    }

}
