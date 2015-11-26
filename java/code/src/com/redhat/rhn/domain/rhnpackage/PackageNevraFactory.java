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
package com.redhat.rhn.domain.rhnpackage;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.state.PackageState;

import org.apache.log4j.Logger;

/**
 * PackageNevraFactory
 */
public class PackageNevraFactory extends HibernateFactory {

    private static Logger log = Logger.getLogger(PackageNevraFactory.class);
    private static PackageNevraFactory singleton = new PackageNevraFactory();

    /**
     * Private Constructor
     */
    private PackageNevraFactory() {
    }

    /**
     * Save a {@link PackageState}.
     *
     * @param packageState the package state to save
     */
    public static void savePackageNevra(PackageNevra packageNevra) {
        singleton.saveObject(packageNevra);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
