/**
 * Copyright (c) 2015 SUSE LLC
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
package com.redhat.rhn.domain.state;

import com.redhat.rhn.common.hibernate.HibernateFactory;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;

import java.util.List;

/**
 * Factory class for working with package states.
 */
public class PackageStateFactory extends HibernateFactory {

    private static Logger log = Logger.getLogger(PackageStateFactory.class);
    private static PackageStateFactory singleton = new PackageStateFactory();

    private PackageStateFactory() {
    }

    /**
     * Save a {@link PackageState}.
     *
     * @param packageState the package state to save
     */
    public static void savePackageState(PackageState packageState) {
        singleton.saveObject(packageState);
    }

    /**
     * Lookup all package states belonging to a state group given by group id.
     *
     * @param groupId the group id
     * @return list of states with a given state id
     */
    @SuppressWarnings("unchecked")
    public static List<PackageState> lookupPackageStates(long groupId) {
        return getSession().createCriteria(PackageState.class)
                .add(Restrictions.eq("groupId", groupId))
                .list();
    }

    /**
     * Clear all package states from the database.
     */
    public static void clearPackageStates() {
        getSession().getNamedQuery("PackageState.deleteAll").executeUpdate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Logger getLogger() {
        return log;
    }
}
