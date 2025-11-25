/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.domain.org;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.server.Server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.util.List;

 /**
  * A small wrapper around hibernate files to remove some of the complexities
  * of writing to hibernate.
 */
 public class SystemMigrationFactory extends HibernateFactory {


    private static SystemMigrationFactory singleton = new SystemMigrationFactory();
    private static Logger log = LogManager.getLogger(SystemMigrationFactory.class);

    private SystemMigrationFactory() {
        super();
    }

    /**
    * Get the Logger for the derived class so log messages
    * show up on the correct class
    * @return Logger to use
    */
    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Find the system migrations that were initiated from the org provided.
     * @param fromOrg the org that the systems were migrated from
     * @return list of SystemMigrations found
     */
    public static List<SystemMigration> lookupByFromOrg(Org fromOrg) {
        Session session = HibernateFactory.getSession();
        return session.createQuery("FROM SystemMigration AS sm WHERE sm.fromOrg = :fromOrg ORDER BY sm.migrated DESC",
                        SystemMigration.class)
                .setParameter("fromOrg", fromOrg)
                .list();
    }

    /**
     * Find the system migrations where the systems were migrated to the org
     * provided.
     * @param toOrg the org that the systems were migrated to
     * @return list of SystemMigrations found
     */
    public static List<SystemMigration> lookupByToOrg(Org toOrg) {
        Session session = HibernateFactory.getSession();
        return session.createQuery("FROM SystemMigration AS sm WHERE sm.toOrg = :toOrg ORDER BY sm.migrated DESC",
                        SystemMigration.class)
                .setParameter("toOrg", toOrg)
                .list();
    }

    /**
     * Find the system migrations associated with the server provided.
     * @param server the server migrated
     * @return list of SystemMigrations found
     */
    public static List<SystemMigration> lookupByServer(Server server) {
        Session session = HibernateFactory.getSession();
        return session.createQuery("FROM SystemMigration AS sm WHERE sm.server = :server ORDER BY sm.migrated DESC",
                        SystemMigration.class)
                .setParameter("server", server)
                .list();
    }

    /**
     * Commit the SystemMigration
     * @param migration SystemMigration object we want to commit.
     */
    public static void save(SystemMigration migration) {
        singleton.saveObject(migration);
    }
}

