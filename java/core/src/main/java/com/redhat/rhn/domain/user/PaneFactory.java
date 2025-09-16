/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.domain.user;

import com.redhat.rhn.common.hibernate.HibernateFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class provides entry point to
 * retrieve Panes through hibernate..
 * PaneFactory
 */
public class PaneFactory {

    private static Logger log = LogManager.getLogger(PaneFactory.class);

    private PaneFactory() {
        super();
    }

    /**
     * This returns a a list of all the panes in the RHNINFOPANE table.
     * @return Map of Pane objects, with the label as the key
     *            Also preserves the ordering returned by the Pane query.
     */
    public static Map<String, Pane> getAllPanes() {
        Session session;
        try {
            session = HibernateFactory.getSession();
            List<Pane> list = session.getNamedQuery("Pane.findAllPanes").list();
            Map<String, Pane> paneMap = new LinkedHashMap<>();
            for (Pane pane : list) {
                paneMap.put(pane.getLabel(), pane);
            }
            return paneMap;
        }
        catch (HibernateException he) {
            log.error("Hibernate exception while retrieving panes: ", he);

        }
        return Collections.emptyMap();
    }

}
