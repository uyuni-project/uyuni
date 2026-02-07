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
import static org.junit.jupiter.api.Assertions.assertNotSame;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
        T result = TestUtils.reload(obj);
        assertNotSame(obj, result);
        return result;
    }

    /**
     * Deletes the given servers and commits the transaction.
     * Use with care, as it will delete all servers if no argument is given.
     * @param servers the servers to delete, or all if null or empty
     */
    default void cleanupServers(Server ...servers) {
        List<Server> serversToDelete = servers.length == 0 ?
                ServerFactory.lookupByIds(ServerFactory.listAllServerIds()) :
                Arrays.stream(servers).filter(Objects::nonNull).toList();

        if (!serversToDelete.isEmpty()) {
            serversToDelete.forEach(ServerFactory::delete);
            HibernateFactory.commitTransaction();
        }
    }

}
