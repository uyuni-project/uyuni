/*
 * Copyright (c) 2021--2026 SUSE LLC
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
import com.redhat.rhn.common.hibernate.PathConverter;
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
     * Lookup {@link InventoryPath} associated with a {@link MinionServer} with given path
     *
     * @param minionId the id of {@link MinionServer}
     * @param inventoryPath the path of the inventory
     * @return optional of {@link InventoryPath}
     */
    public static Optional<InventoryPath> lookupAnsibleInventoryPath(long minionId, String inventoryPath) {
        return HibernateFactory.getSession()
                .createQuery("SELECT p FROM InventoryPath p " +
                        "WHERE p.minionServer.id = :mid " +
                        "AND p.path = :inventoryPath")
                .setParameter("mid", minionId)
                .setParameter("inventoryPath", new PathConverter().convertToEntityAttribute(inventoryPath))
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
     * List all ansible managed {@link Server}s linked to given {@link Server}
     *
     * @param minionId the id of the contorl node
     * @return the list of inventory servers
     */
    public static List<Server> listAnsibleInventoryServersByControlNode(long minionId) {
        return HibernateFactory.getSession().createNativeQuery("""
                 SELECT DISTINCT s.*, 0 as clazz_ FROM suseAnsiblePath ap
                 JOIN suseAnsibleInventoryServers ais ON ap.id = ais.inventory_id
                 JOIN rhnServer s ON ais.server_id = s.id
                 WHERE ap.server_id = :server_id""", Server.class)
                .setParameter("server_id", minionId)
                .getResultList();
    }

    /**
     * List all {@link Server}s linked to inventories excluding given {@link InventoryPath}
     *
     * @param path the inventory to exclude
     * @return the list of inventory servers
     */
    public static List<Server> listAnsibleInventoryServersExcludingPath(InventoryPath path) {
        return HibernateFactory.getSession().createQuery("""
                 SELECT DISTINCT p.inventoryServers
                 FROM InventoryPath p
                 WHERE p.id != :inventoryId""", Server.class)
                .setParameter("inventoryId", path.getId())
                .getResultList();
    }

    /**
     * List all {@link Server}s linked to inventories from control nodes excluding given {@link MinionServer}
     *
     * @param minionId the id of the control node to exclude
     * @return the list of inventory servers
     */
    public static List<Server> listAnsibleInventoryServersExcludingControlNode(long minionId) {
        return HibernateFactory.getSession().createNativeQuery("""
                 SELECT DISTINCT s.*, 0 as clazz_ FROM suseAnsiblePath ap
                 JOIN suseAnsibleInventoryServers ais ON ap.id = ais.inventory_id
                 JOIN rhnServer s ON ais.server_id = s.id
                 WHERE ap.server_id != :server_id""", Server.class)
                .setParameter("server_id", minionId)
                .getResultList();
    }

    /**
     * Save an {@link AnsiblePath}
     *
     * @param path the {@link AnsiblePath}
     * @return the updated {@link AnsiblePath}
     */
    public static AnsiblePath saveAnsiblePath(AnsiblePath path) {
        return HibernateFactory.getSession().merge(path);
    }

    /**
     * Remove an {@link AnsiblePath}
     *
     * @param path the {@link AnsiblePath}
     */
    public static void removeAnsiblePath(AnsiblePath path) {
        HibernateFactory.getSession().remove(path);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

}
