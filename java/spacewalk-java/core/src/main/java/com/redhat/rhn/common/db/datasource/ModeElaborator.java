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

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * ModeElaborator
 */
public class ModeElaborator implements Elaborator {
    private SelectMode mode;
    private final HashMap<String, Object> params;

    // increase this number on any data change
    private static final long serialVersionUID = 1L;

    /**
     * @param select Select mode
     * @param elabParams elaborator params
     */
    public ModeElaborator(SelectMode select, Map<String, Object> elabParams) {
        mode = select;
        if (elabParams != null) {
            params = new HashMap<>(elabParams);
        }
        else {
            params = null;
        }
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void elaborate(List objectsToElaborate) {
        mode.elaborate(objectsToElaborate, params);
    }

    @Override
    public void elaborate(List objectsToElaborate, Session session) {
        mode = ModeFactory.getMode(session, mode);
        mode.elaborate(objectsToElaborate, params);
    }
}
