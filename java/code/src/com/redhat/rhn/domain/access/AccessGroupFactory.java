/*
 * Copyright (c) 2025 SUSE LLC
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

package com.redhat.rhn.domain.access;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.org.Org;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.type.StandardBasicTypes;

public class AccessGroupFactory extends HibernateFactory {

    private static final Logger LOG = LogManager.getLogger(AccessGroupFactory.class);

    private AccessGroupFactory() {
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    /**
     * Get the access group by label and org.
     * @param label label of the access group
     * @param org org of the access group
     * @return AccessGroup whose label matches the given label.
     */
    public static AccessGroup lookupByOrgAndLabel(Org org, String label) {
        Session session = HibernateFactory.getSession();
        return session.createNativeQuery(
                    "SELECT * FROM access.accessGroup WHERE label = :label and org = :org",
                    AccessGroup.class
                )
                .setParameter("label", label, StandardBasicTypes.STRING)
                .setParameter("org", org)
                .setCacheable(true)
                .uniqueResult();
    }
}
