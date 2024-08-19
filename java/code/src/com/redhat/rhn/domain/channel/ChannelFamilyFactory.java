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
package com.redhat.rhn.domain.channel;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.frontend.dto.ChannelOverview;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * ChannelFamilyFactory
 */
public class ChannelFamilyFactory extends HibernateFactory {

    private static ChannelFamilyFactory singleton = new ChannelFamilyFactory();
    private static Logger log = LogManager.getLogger(ChannelFamilyFactory.class);
    public static final String TOOLS_CHANNEL_FAMILY_LABEL = "SLE-M-T";
    public static final String SATELLITE_CHANNEL_FAMILY_LABEL = "SMS";
    public static final String PROXY_CHANNEL_FAMILY_LABEL = "SMP";
    public static final String PROXY_ARM_CHANNEL_FAMILY_LABEL = "SMP-ARM64";
    public static final String MODULE_CHANNEL_FAMILY_LABEL = "MODULE";
    public static final String OPENSUSE_CHANNEL_FAMILY_LABEL = "OPENSUSE";

    private ChannelFamilyFactory() {
        super();
    }

    /**
     * Get the Logger for the derived class so log messages
     * show up on the correct class
     */
    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Lookup a ChannelFamily by its id
     * @param id the id to search for
     * @return the ChannelFamily found
     */
    public static ChannelFamily lookupById(Long id) {
        return HibernateFactory.getSession().get(ChannelFamily.class, id);
    }

    /**
     * Lookup a ChannelFamily by its label
     * @param label the label to search for
     * @param org the Org the Family belongs to, use null if looking for
     *        official RedHat ChannelFamilies
     * @return the ChannelFamily found
     */
    public static ChannelFamily lookupByLabel(String label, Org org) {
        Session session = getSession();
        Criteria c = session.createCriteria(ChannelFamily.class);
        c.add(Restrictions.eq("label", label));
        c.add(Restrictions.or(Restrictions.eq("org", org),
              Restrictions.isNull("org")));
        return (ChannelFamily) c.uniqueResult();
    }

    /**
     * Lookup a ChannelFamily by org - this is the org's private
     * channel family, which has all of the org's custom channels in
     * it.
     * @param orgIn the org who's family this is
     * @return the ChannelFamily found
     */
    public static ChannelFamily lookupByOrg(Org orgIn) {
        return singleton.lookupObjectByNamedQuery("ChannelFamily.findByOrgId", Map.of("orgId", orgIn.getId()));
    }

    /**
     * Checks that an org has a channel family associated with it.  If
     * not, creates the org's channel family.
     *
     * @param orgIn the org to verify
     * @return the ChannelFamily found or created
     */
    public static ChannelFamily lookupOrCreatePrivateFamily(Org orgIn) {
        ChannelFamily cfam = lookupByOrg(orgIn);

        if (cfam == null) {
            String label = "private-channel-family-" + orgIn.getId();
            String suffix = " (" + orgIn.getId() + ") Channel Family";
            String prefix = orgIn.getName();
            int len = prefix.length() + suffix.length();
            if (len > 128) {
                int diff = len - 128;
                prefix = prefix.substring(1, prefix.length() - diff);
            }

            String name = prefix.concat(suffix);

            cfam = new ChannelFamily();
            cfam.setOrg(orgIn);
            cfam.setLabel(label);
            cfam.setName(name);

            ChannelFamilyFactory.save(cfam);

            //If we're creating a new channel fam, make sure the org has
            updateFamilyPermissions(orgIn);
            //permission to use it.
        }
        return cfam;
    }

    /**
     * Checks if the org has permission to its channel family.
     * If it does not, grants permissions.
     * Based on modules/rhn/RHN/DB/ChannelEditor.pm->verify_family_permissions
     * @param org The org for which we are verifing channel family permissions.
     * @return A list of ids as Longs of the channel families for which
     *         permissions were updated.
     */
    private static List<Long> updateFamilyPermissions(Org org) {
        //Get a list of channel families that belong to this org
        //for which this org does not have appropriate permissions
        SelectMode m = ModeFactory.getMode("Channel_queries",
                "families_for_org_without_permissions");
        Map<String, Object> params = new HashMap<>();
        params.put("org_id", org.getId());
        DataResult<ChannelOverview> dr = m.execute(params);
        Iterator<ChannelOverview> i = dr.iterator();

        //Insert permissions for this org
        List<Long> ids = new ArrayList<>();
        WriteMode m2 = ModeFactory.getWriteMode("Channel_queries", "insert_family_perms");
        while (i.hasNext()) {
            Long next = i.next().getId();
            ids.add(next);

            params.clear();
            params.put("org_id", org.getId());
            params.put("id", next);
            m2.executeUpdate(params);
        }

        //return the list of ids
        return ids;
    }

    /**
     * Insert or Update a ChannelFamily.
     * @param cfam ChannelFamily to be stored in database.
     */
    public static void save(ChannelFamily cfam) {
        singleton.saveObject(cfam);
    }

    /**
     * Remove a ChannelFamily from the DB
     * @param cfam ChannelFamily to be removed from database.
     */
    public static void remove(ChannelFamily cfam) {
        if (cfam.isPublic()) {
            singleton.removeObject(cfam.getPublicChannelFamily());
        }
        else {
            cfam.getPrivateChannelFamilies()
                    .forEach(pcf -> singleton.removeObject(pcf));
        }
        singleton.removeObject(cfam);
    }

    /**
     * Insert or Update a PrivateChannelFamily.
     * @param pcfam PrivateChannelFamily to be stored in database.
     */
    public static void save(PrivateChannelFamily pcfam) {
        singleton.saveObject(pcfam);
    }

    /**
     * Insert or Update a PublicChannelFamily.
     * @param pcf PublicChannelFamily to be stored in database.
     */
    public static void save(PublicChannelFamily pcf) {
        singleton.saveObject(pcf);
    }

    /**
     * Lookup the List of ChannelFamily objects that are labled starting
     * with the passed in label param
     * @param label to query against
     * @param orgIn owning the Channel.  Pass in NULL if you want a NULL org channel
     * @return List of Channel objects
     */
    @SuppressWarnings("unchecked")
    public static List<ChannelFamily> lookupByLabelLike(String label, Org orgIn) {
        Session session = getSession();
        Criteria c = session.createCriteria(ChannelFamily.class);
        c.add(Restrictions.like("label", label + "%"));
        c.add(Restrictions.or(Restrictions.eq("org", orgIn),
              Restrictions.isNull("org")));
        return c.list();
    }

    /**
     * Return the name for a given channel family label.
     * @param label channel family label
     * @return name for given label or null
     */
    public static String getNameByLabel(String label) {
        Map<String, Object> params = new HashMap<>();
        params.put("label", label);
        Object o = singleton.lookupObjectByNamedQuery(
                "ChannelFamily.getNameByLabel", params, false);
        return (String) o;
    }

    /**
     * Return all channel families from the database.
     * @return list of all channel families
     */
    @SuppressWarnings("unchecked")
    public static List<ChannelFamily> getAllChannelFamilies() {
        Session session = getSession();
        Criteria c = session.createCriteria(ChannelFamily.class);
        return c.list();
    }
}
