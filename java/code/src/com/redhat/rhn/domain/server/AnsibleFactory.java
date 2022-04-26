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

package com.redhat.rhn.domain.server;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.server.ansible.AnsiblePath;
import com.redhat.rhn.domain.server.ansible.InventoryPath;
import com.redhat.rhn.domain.server.ansible.PlaybookPath;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Hibernate factory with Ansible-related methods
 */
public class AnsibleFactory extends HibernateFactory {

    private static Logger log = LogManager.getLogger(AnsibleFactory.class);

    /**
     * Lookup an {@link AnsiblePath} by id
     *
     * @param id the id
     * @return the {@link AnsiblePath}
     */
    public static Optional<AnsiblePath> lookupAnsiblePathById(long id) {
        return HibernateFactory.getSession()
                .createQuery("SELECT p FROM AnsiblePath p WHERE id = :id")
                .setParameter("id", id)
                .uniqueResultOptional();
    }

    /**
     * Lookup {@link AnsiblePath} by path and minion id
     *
     * @param path the path
     * @param minionServerId the minion id
     * @return optional of {@link AnsiblePath}
     */
    public static Optional<AnsiblePath> lookupAnsiblePathByPathAndMinion(Path path, long minionServerId) {
        return HibernateFactory.getSession().createQuery("SELECT p FROM AnsiblePath p " +
                "WHERE p.path = :path " +
                "AND p.minionServer.id = :minionServerId")
                .setParameter("path", path)
                .setParameter("minionServerId", minionServerId)
                .uniqueResultOptional();
    }

    /**
     * List {@link AnsiblePath}s associated with a {@link MinionServer} with given id
     *
     * @param minionServerId the id of {@link MinionServer}
     * @return the list of {@link AnsiblePath}s
     */
    public static List<AnsiblePath> listAnsiblePaths(long minionServerId) {
        return HibernateFactory.getSession()
                .createQuery("SELECT p FROM AnsiblePath p " +
                        "WHERE p.minionServer.id = :sid ")
                .setParameter("sid", minionServerId)
                .list();
    }

    /**
     * List {@link PlaybookPath}s associated with a {@link MinionServer} with given id
     *
     * @param minionId the id of {@link MinionServer}
     * @return the list of {@link PlaybookPath}s
     */
    public static List<PlaybookPath> listAnsiblePlaybookPaths(long minionId) {
        return HibernateFactory.getSession()
                .createQuery("SELECT p FROM PlaybookPath p " +
                        "WHERE p.minionServer.id = :mid ")
                .setParameter("mid", minionId)
                .list();
    }

    /**
     * List {@link InventoryPath}s associated with a {@link MinionServer} with given id
     *
     * @param minionId the id of {@link MinionServer}
     * @return the list of {@link PlaybookPath}s
     */
    public static List<InventoryPath> listAnsibleInventoryPaths(long minionId) {
        return HibernateFactory.getSession()
                .createQuery("SELECT p FROM InventoryPath p " +
                        "WHERE p.minionServer.id = :mid ")
                .setParameter("mid", minionId)
                .list();
    }

    /**
     * Save an {@link AnsiblePath}
     *
     * @param path the {@link AnsiblePath}
     * @return the updated {@link AnsiblePath}
     */
    public static AnsiblePath saveAnsiblePath(AnsiblePath path) {
        HibernateFactory.getSession().saveOrUpdate(path);
        return path;
    }

    /**
     * Remove an {@link AnsiblePath}
     *
     * @param path the {@link AnsiblePath}
     */
    public static void removeAnsiblePath(AnsiblePath path) {
        HibernateFactory.getSession().delete(path);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

}
