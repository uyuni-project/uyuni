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
package com.redhat.rhn.common.db.datasource;

import org.hibernate.Session;

import java.io.Serializable;
import java.util.List;


/**
 * Elaboratable
 */
public interface Elaborator extends Serializable {

    /**
     * Returns an elaborated list for the given List of objects
     * @param objectsToElaborate the list of objects to elaborate
     */
    void elaborate(List objectsToElaborate);

    /**
     * Returns an elaborated list for the given List of objects with an updated session
     * @param objectsToElaborate the list of objects to elaborate
     * @param session session to use
     */
    void elaborate(List objectsToElaborate, Session session);
}
