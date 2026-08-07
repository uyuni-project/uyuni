/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.model.ldap;

import com.redhat.rhn.common.hibernate.HibernateFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Persistence operations for {@link LdapAuthServer} records.
 */
public class LdapAuthServerFactory extends HibernateFactory {

    private static final LdapAuthServerFactory SINGLETON = new LdapAuthServerFactory();
    private static final Logger LOG = LogManager.getLogger(LdapAuthServerFactory.class);

    private LdapAuthServerFactory() {
    }

    /**
     * Saves a directory record, refreshing its modification timestamp.
     *
     * @param serverIn the record to save
     * @return the saved record
     */
    public static LdapAuthServer save(LdapAuthServer serverIn) {
        serverIn.setModified(new Date());
        return SINGLETON.saveObject(serverIn);
    }

    /**
     * Deletes a directory record.
     *
     * @param serverIn the record to delete
     */
    public static void remove(LdapAuthServer serverIn) {
        SINGLETON.removeObject(serverIn);
    }

    /**
     * @param idIn the record id
     * @return the directory with the given id, if it exists
     */
    public static Optional<LdapAuthServer> lookupById(long idIn) {
        return Optional.ofNullable(getSession().find(LdapAuthServer.class, idIn));
    }

    /**
     * @param labelIn the administrator-facing unique name
     * @return the directory with the given label, if it exists
     */
    public static Optional<LdapAuthServer> lookupByLabel(String labelIn) {
        return getSession()
                .createQuery("FROM LdapAuthServer WHERE label = :label", LdapAuthServer.class)
                .setParameter("label", labelIn)
                .uniqueResultOptional();
    }

    /**
     * @return every configured directory, enabled or not, in probe order
     */
    public static List<LdapAuthServer> listAll() {
        return getSession()
                .createQuery("FROM LdapAuthServer ORDER BY priority, id", LdapAuthServer.class)
                .list();
    }

    /**
     * Lists the directories the login layer may consult, in the order they should be probed.
     *
     * @return the enabled directories ordered by ascending priority
     */
    public static List<LdapAuthServer> listEnabled() {
        return getSession()
                .createQuery("FROM LdapAuthServer WHERE enabled = true ORDER BY priority, id",
                        LdapAuthServer.class)
                .list();
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
