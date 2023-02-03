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
package com.suse.manager.tasks;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.hibernate.HibernateFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Task cleaning up sessions in the database.
 */
public class SessionCleanup {

    private static final Logger LOG = LogManager.getLogger(SessionCleanup.class);

    private SessionCleanup() {
    }

    /**
     * Main entry point
     *
     * @param args no expected arguments
     */
    public static void main(String[] args) {
        Config c = Config.get();

        HibernateFactory.createSessionFactory();

        //retrieves info from user preferences
        long window = c.getInt("web.session_database_lifetime");

        long bound = (System.currentTimeMillis() / 1000) - (2 * window);

        LOG.debug("session_cleanup: starting delete of stale sessions");
        if (LOG.isDebugEnabled()) {
            LOG.debug("Session expiry threshold is {}", bound);
        }

        int sessionsDeleted = HibernateFactory.getSession()
                .createQuery("delete from com.redhat.rhn.domain.session.WebSessionImpl w where w.expires < :bound")
                .setParameter("bound", bound)
                .executeUpdate();

        //logs number of sessions deleted
        if (sessionsDeleted > 0) {
            LOG.info("{} stale session(s) deleted", sessionsDeleted);
        }
        else {
            LOG.debug("No stale sessions deleted");
        }
        System.exit(0);
    }
}
