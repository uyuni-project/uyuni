/**
 * Copyright (c) 2018 SUSE LLC
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

package com.redhat.rhn.domain.contentmgmt;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.image.ImageInfoFactory;
import org.apache.log4j.Logger;

/**
 *  todo
 */
public class ContentProjectFactory extends HibernateFactory {

    private static ContentProjectFactory INSTANCE;

    private static Logger log = Logger.getLogger(ImageInfoFactory.class);

    private ContentProjectFactory() {
        super();
    }

    // todo
    public static synchronized ContentProjectFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ContentProjectFactory();
        }
        return INSTANCE;
    }

    // todo
    public void save(ContentProject contentProject) {
        saveObject(contentProject);
    }

    // todo
    @Override
    protected Logger getLogger() {
        return log;
    }
}
