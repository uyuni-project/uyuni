/**
 * Copyright (c) 2016 SUSE LLC
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
package com.redhat.rhn.domain.server;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * ServerFactory - the singleton class used to fetch and store
 * {@link ServerNetAddress4} and {@link ServerNetAddress6} objects
 * from the database.
 * @version $Rev$
 */
public class ServerNetworkFactory extends HibernateFactory {

    private static ServerNetworkFactory singleton = new ServerNetworkFactory();
    private static Logger log = Logger.getLogger(ServerNetworkFactory.class);

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Find list of {@link ServerNetAddress4} for the given interface.
     * @param interfaceId the id of the {@link NetworkInterface}
     * @return a List of {@link ServerNetAddress4} or null
     * @throws org.hibernate.NonUniqueResultException
     * if there is more than one matching result
     */
    public static List<ServerNetAddress4> findServerNetAddress4(Long interfaceId) {
        return (List<ServerNetAddress4>) HibernateFactory.getSession()
                .getNamedQuery("ServerNetAddress4.lookup")
                .setParameter("interface_id", interfaceId)
                .list();
    }

    /**
     * Find a list of {@link ServerNetAddress6} for the given interface.
     * @param interfaceId the id of the {@link NetworkInterface}
     * @return a list of {@link ServerNetAddress6} or null
     * @throws org.hibernate.NonUniqueResultException
     * if there is more than one matching result
     */
    public static List<ServerNetAddress6> findServerNetAddress6(Long interfaceId) {
        return (List<ServerNetAddress6>) HibernateFactory.getSession()
                .getNamedQuery("ServerNetAddress6.lookup_by_id")
                .setParameter("interface_id", interfaceId)
                .list();
    }

    /**
     * Save ServerNetAddress4
     * @param serverNetAddress4 the address to save
     */
    public static void saveServerNetAddress4(ServerNetAddress4 serverNetAddress4) {
        singleton.saveObject(serverNetAddress4);
    }

    /**
     * Delete ServerNetAddress4
     * @param serverNetAddress4 the address to delete
     */
    public static void removeServerNetAddress4(ServerNetAddress4 serverNetAddress4) {
        singleton.removeObject(serverNetAddress4);
    }

    /**
     * Save ServerNetAddress4
     * @param serverNetAddress6 the address to save
     */
    public static void saveServerNetAddress6(ServerNetAddress6 serverNetAddress6) {
        singleton.saveObject(serverNetAddress6);
    }

    /**
     * Delete ServerNetAddress6
     * @param serverNetAddress6 the address to delete
     */
    public static void removeServerNetAddress6(ServerNetAddress6 serverNetAddress6) {
        singleton.saveObject(serverNetAddress6);
    }

}
