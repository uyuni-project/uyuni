/**
 * Copyright (c) 2018 SUSE LLC
 * <p>
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 * <p>
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.domain.contentmgmt.test;

import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

public class ContentProjectFactoryTest extends BaseTestCaseWithUser {

    //todo
    public void testFoo() {
        ContentProject cp = new ContentProject();
        cp.setLabel("cplabel");
        cp.setDescription("cpdesc");

        ContentProjectFactory.getInstance().save(cp);

        System.out.println(ContentProjectFactory.getInstance());
    }
}
