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
package com.redhat.rhn.domain.rhnpackage.profile;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.Server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.util.List;

/**
 * ProfileFactory
 */
public class ProfileFactory extends HibernateFactory {

    private static ProfileFactory singleton = new ProfileFactory();

    private static Logger log = LogManager.getLogger(ProfileFactory.class);
    /** The constant representing normal profile type. */
    public static final ProfileType TYPE_NORMAL = lookupByLabel("normal");
    /** The constant representing sync profile type. */
    public static final ProfileType TYPE_SYNC_PROFILE = lookupByLabel("sync_profile");



    private ProfileFactory() {
        super();
    }

    /**
     * Creates a new Profile
     * @param type Type of profile desired.
     * @return a new Profile
     * @see ProfileFactory#TYPE_NORMAL
     * @see ProfileFactory#TYPE_SYNC_PROFILE
     */
    public static Profile createProfile(ProfileType type) {
        return new Profile(type);
    }

    /**
     * Get the statetype by name.
     * @param name Name of statetype
     * @return statetype whose name matches the given name.
     */
    public static ProfileType lookupByLabel(String name) {
        return singleton.lookupObjectByParam(ProfileType.class, "label", name, true);
    }

    /**
     * Lookup a Profile by their id
     * @param id the id to search for
     * @param org The org in which this profile should be.
     * @return the Profile found
     */
    public static Profile lookupByIdAndOrg(Long id, Org org) {
        Session session = HibernateFactory.getSession();
        return session.createQuery("FROM Profile AS p WHERE p.id = :id AND p.org.id = :org_id", Profile.class)
                .setParameter("id", id)
                .setParameter("org_id", org.getId())
                //Retrieve from cache if there
                .setCacheable(true)
                .uniqueResult();

    }

    /**
     * Returns a list of Profiles which are compatible with the given server.
     * @param server Server whose profiles we want.
     * @param org Org owner
     * @return  a list of Profiles which are compatible with the given server.
     */
    public static List<Profile> compatibleWithServer(Server server, Org org) {
        Session session = HibernateFactory.getSession();
        return session.createNativeQuery("""
                        SELECT DISTINCT P.* FROM rhnServer S, rhnServerProfile P
                        WHERE P.org_id = S.org_id
                        AND S.id = :sid
                        AND P.profile_type_id = (SELECT id FROM rhnServerProfileType WHERE label = 'normal')
                        AND (EXISTS (SELECT 1 FROM rhnServerChannel SC
                                     WHERE SC.server_id = S.id
                                     AND SC.channel_id = P.base_channel)
                            OR EXISTS (SELECT 1 FROM rhnChannel C
                                     WHERE C.id = P.base_channel
                                     AND C.org_id = :org_id
                                     AND C.parent_channel IS NULL) )
                        ORDER BY P.name""", Profile.class)
                .setParameter("sid", server.getId())
                .setParameter("org_id", org.getId())
                .list();
    }

     /**
      * Store the profile.
      *
      * @param profile The object we are commiting.
      * @return the managed {@link Profile} instance
      */
    public static Profile save(Profile profile) {
        return singleton.saveObject(profile);
    }

    /**
     * Deletes the profile.
     * @param profile The object we are deleting.
     * @return number of objects affected (usually 1 or 0).
     */
    public static int remove(Profile profile) {
        return singleton.removeObject(profile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Returns a Profile whose name matches the given name and is in the
     * given org, or null if none found.
     * @param name Profile name
     * @param orgid OrgId which owns profile.
     * @return a Profile whose name matches the given name and is in the
     * given org, or null if none found.
     */
    public static Profile findByNameAndOrgId(String name, Long orgid) {
        Session session = HibernateFactory.getSession();
        return session.createNativeQuery(
                "SELECT P.* FROM rhnServerProfile P WHERE P.name = :name AND P.org_id = :org_id",
                        Profile.class)
                .setParameter("name", name)
                .setParameter("org_id", orgid)
                //Retrieve from cache if there
                .setCacheable(true)
                .uniqueResult();
    }

}
