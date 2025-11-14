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

package com.redhat.rhn.common.translation;

import com.redhat.rhn.common.db.DatabaseException;
import com.redhat.rhn.common.db.WrappedSQLException;

import java.sql.SQLException;

/**
 * Translator The class that actually does the object translations for us.
 *
 */

public class SqlExceptionTranslator extends Translations {

    private SqlExceptionTranslator() {
    }

    /**
     * Gets the appropriate runtime exception depending on whether the DB is oracle or not
     * @param e the exception
     * @return the RuntimeException of the wrapped exception
     */
    public static DatabaseException sqlException(SQLException e) {
        return new WrappedSQLException(e.getMessage(), e);
    }
}
