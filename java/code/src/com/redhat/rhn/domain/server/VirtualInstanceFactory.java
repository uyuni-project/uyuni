/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
import com.redhat.rhn.domain.org.Org;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.type.StandardBasicTypes;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Tuple;

/**
 * VirtualInstanceFactory provides data access operations for virtual instances.
 *
 * @see VirtualInstance
 */
public class VirtualInstanceFactory extends HibernateFactory {

    private static VirtualInstanceFactory instance = new VirtualInstanceFactory();

    /**
     * Logger for this class
     */
    private static Logger log = LogManager.getLogger(VirtualInstanceFactory.class);

    private interface HibernateCallback {
        Object executeInSession(Session session);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    private Object execute(HibernateCallback command) {
        return command.executeInSession(HibernateFactory.getSession());
    }

    /**
     * Get instance of this factory.
     * @return VirtualInstanceFactory instance
     */
    public static VirtualInstanceFactory getInstance() {
        return instance;
    }

    /**
     * Saves the virtual instance to the database. The save is cascading so that if the
     * virtual instance is a registered guest, then any changes to this virtual instance's
     * guest server will be persisted as well.
     *
     * @param virtualInstance The virtual instance to save
     */
    public void saveVirtualInstance(VirtualInstance virtualInstance) {
        saveObject(virtualInstance);
    }

    /**
     * Gets the virtual Instance for a given Sid for a guest
     * @param id the system id of the guest
     * @return the guest's virtual instance
     */
    public VirtualInstance lookupByGuestId(Long id) {
        Session session = HibernateFactory.getSession();
        return session.createQuery("""
                        FROM VirtualInstance guest
                        WHERE guest.guestSystem.id = :sid
                """, VirtualInstance.class)
                .setParameter("sid", id, StandardBasicTypes.LONG)
                .uniqueResult();
    }

    /**
     * Retrieves the virtual instance with the specified ID.
     *
     * @param id The primary key
     * @return The virtual instance with the specified ID or <code>null</code> if no match
     * is found.
     */
    public VirtualInstance lookupById(final Long id) {
        return instance.lookupObjectByParam(VirtualInstance.class, "id", id);
    }

    /**
     * Deletes the virtual instance from the database.
     * If the virtual instance has an association to a guest system (i.e. it is
     * a registered guest), remove this association.
     * If the virtual instance has an association to a host system, remove this
     * association.
     *
     * @param virtualInstance The virtual instance to delete
     */
    public void deleteVirtualInstanceOnly(VirtualInstance virtualInstance) {
        log.debug("Deleting virtual instance without removing associated objects {}", virtualInstance);
        Server hostSystem = virtualInstance.getHostSystem();
        if (hostSystem != null) {
            hostSystem.removeGuest(virtualInstance);
        }
        Server guestSystem = virtualInstance.getGuestSystem();
        if (guestSystem != null) {
            guestSystem.setVirtualInstance(null);
        }
        removeObject(virtualInstance);
    }

    /**
     * Finds all registered guests, within a particular org, whose hosts do not have any
     * virtualization entitlements.
     *
     * @param org The org to search in
     *
     * @return A set of GuestAndNonVirtHostView objects
     *
     * @see GuestAndNonVirtHostView
     */
    public Set<GuestAndNonVirtHostView> findGuestsWithNonVirtHostByOrg(Org org) {
        Session session = HibernateFactory.getSession();
        return session.createNativeQuery("""
                        select
                              guest.id as guest_id,
                              guest.org_id as guest_org_id,
                              guest.name as guest_name,
                              host.id as host_id,
                              host.org_id as host_org_id,
                              host.name as host_name
                        from
                          RhnVirtualInstance vi
                              inner join rhnServer guest on vi.virtual_system_id = guest.id
                              inner join rhnServer host on vi.host_system_id = host.id
                        where
                          guest.org_id = :org_id and
                          (host.org_id != :org_id  or
                          not exists (
                            select 1 from rhnServerGroupMembers sgm
                                      inner join rhnServerGroup sg on sgm.server_group_id = sg.id
                                      inner join rhnServerGroupType sgt on sg.group_type = sgt.id
                             where
                                  sgt.label = 'virtualization_host'
                                  and sgm.server_id = host.id
                          ))
                        """, Tuple.class)
                .setParameter("org_id", org.getId(), StandardBasicTypes.LONG)
                .addScalar("guest_id", StandardBasicTypes.LONG)
                .addScalar("guest_org_id", StandardBasicTypes.LONG)
                .addScalar("guest_name", StandardBasicTypes.STRING)
                .addScalar("host_id", StandardBasicTypes.LONG)
                .addScalar("host_org_id", StandardBasicTypes.LONG)
                .addScalar("host_name", StandardBasicTypes.STRING)
                .stream()
                .map(t -> new GuestAndNonVirtHostView(
                        t.get(0, Long.class),
                        t.get(1, Long.class),
                        t.get(2, String.class),
                        t.get(3, Long.class),
                        t.get(4, Long.class),
                        t.get(5, String.class)))
                .collect(Collectors.toSet());
    }

    /**
     * Finds all registered guests, within a particular org, who do not have a registered
     * host.
     *
     * @param org The org to search in
     *
     * @return set A set of GuestAndNonVirtHostView objects
     *
     * @see GuestAndNonVirtHostView
     */
    public Set<GuestAndNonVirtHostView> findGuestsWithoutAHostByOrg(Org org) {
        Session session = HibernateFactory.getSession();

        List<GuestAndNonVirtHostView> results = session.createQuery("""
                        select
                          new com.redhat.rhn.domain.server.GuestAndNonVirtHostView(guest.id, guest.org.id, guest.name)
                        from
                          VirtualInstance virtualInstance join virtualInstance.guestSystem guest
                        where
                          virtualInstance.hostSystem is null and
                          guest.org = :org
                        """, GuestAndNonVirtHostView.class)
                .setParameter("org", org)
                .list();

        return new HashSet<>(results);
    }

    /**
     * Returns the para-virt type.
     *
     * @return  The para-virt type
     */
    public VirtualInstanceType getParaVirtType() {
        return getVirtualInstanceType("para_virtualized");
    }

    /**
     * Returns the fully-virt type.
     *
     * @return The fully-virt type.
     */
    public VirtualInstanceType getFullyVirtType() {
        return getVirtualInstanceType("fully_virtualized");
    }

    /**
     * Returns the requested virtual instance type.
     *
     * @param label the type label
     * @return The type or null
     */
    public VirtualInstanceType getVirtualInstanceType(String label) {
        return getSession().createQuery("FROM VirtualInstanceType AS vit WHERE vit.label = :label",
                        VirtualInstanceType.class)
                .setParameter("label", label, StandardBasicTypes.STRING)
                .setCacheable(true)
                .uniqueResult();
    }

    /**
     * Returns the running state.
     *
     * @return The running state
     */
    public VirtualInstanceState getRunningState() {
        return getState("running").orElse(null);
    }

    /**
     * Returns the stopped state.
     *
     * @return The stopped state
     */
    public VirtualInstanceState getStoppedState() {
        return getState("stopped").orElse(null);
    }

    /**
     * Returns the paused state.
     *
     * @return The paused state
     */
    public VirtualInstanceState getPausedState() {
        return getState("paused").orElse(null);
    }

    /**
     * Return the crashed state.
     *
     * @return The crashed state
     */
    public VirtualInstanceState getCrashedState() {
        return getState("crashed").orElse(null);
    }

    /**
     * Return the unknown state
     *
     *  @return The unknown state
     */
    public VirtualInstanceState getUnknownState() {
        return getState("unknown").orElse(null);
    }

    /**
     * Returns state of the given label
     *
     * @param label state label
     * @return virtualInstanceState found by label or null
     */
    public Optional<VirtualInstanceState> getState(String label) {
        return getSession().createQuery("FROM VirtualInstanceState AS state WHERE state.label = :label",
                                VirtualInstanceState.class)
                .setParameter("label", label, StandardBasicTypes.STRING)
                .uniqueResultOptional();
    }

    /**
     * Returns a VirtualInstance with given uuid
     * @param uuid - uuid of the vm
     * @return VirtualInstance with given uuid
     */
    public List<VirtualInstance> lookupVirtualInstanceByUuid(String uuid) {
        return getSession()
                .createQuery("""
                        FROM VirtualInstance guestVI
                        WHERE guestVI.uuid = :uuid
                        """, VirtualInstance.class)
                .setParameter("uuid", uuid, StandardBasicTypes.STRING)
                .list();
    }

    /**
     * Returns a VirtualInstance that is linked to the host system with given id.
     * @param hostId - id of the host system
     * @return VirtualInstance linked to the host with given id
     */
    public VirtualInstance lookupHostVirtInstanceByHostId(Long hostId) {
        return getSession()
                .createQuery("""
                        FROM  VirtualInstance hostVI
                        WHERE hostVI.uuid IS NULL
                        AND   hostVI.hostSystem.id = :hostId
                        """, VirtualInstance.class)
                .setParameter("hostId", hostId, StandardBasicTypes.LONG)
            .uniqueResult();
    }

    /**
     * Returns a VirtualInstance with given uuid and host id.
     * @param hostId - id of the host system
     * @param uuid - uuid of the guest
     * @return VirtualInstance with uuid running on host matching hostId
     */
    public VirtualInstance lookupVirtualInstanceByHostIdAndUuid(Long hostId, String uuid) {
        return getSession()
                .createQuery("""
                        FROM  VirtualInstance guestVI
                        WHERE guestVI.uuid = :uuid
                        AND   guestVI.hostSystem.id = :hostId
                        """, VirtualInstance.class)
                .setParameter("hostId", hostId, StandardBasicTypes.LONG)
                .setParameter("uuid", uuid, StandardBasicTypes.STRING)
            .uniqueResult();
    }
}
