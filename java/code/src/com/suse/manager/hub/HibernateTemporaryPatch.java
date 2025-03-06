/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.hub;

//=========================================================
//=========================================================
// WARNING!
// THIS CLASS WILL BE REMOVED; IT IS USED ONLY AS A TEMPORARY PATCH
//=========================================================
//=========================================================

import com.redhat.rhn.common.hibernate.HibernateFactory;

public class HibernateTemporaryPatch {

    static boolean into = false;
    private HibernateTemporaryPatch() {
        //
    }

    /**
     * empty
     */
    public static void rollback() {
        // cleanup the local side: explicit rollback
        // HubManager.createServer has already created an ISSPeripheral object
        //
        // Explicit rollback is needed since SparkApplicationHelper.setupHibernateSessionFilter sets Spark.after
        // which in turn calls HibernateFactory.commitTransaction() if there's an ongoing transaction
        HibernateFactory.rollbackTransaction();
        into = HibernateFactory.inTransaction();
    }

    /**
     * empty
     */
    public static boolean doInvalidate() {
        //authenticationService.invalidate(request.raw(), response.raw());
        return false;
    }
}
