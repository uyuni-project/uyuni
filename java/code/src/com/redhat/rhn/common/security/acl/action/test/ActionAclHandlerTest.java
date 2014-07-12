/**
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
package com.redhat.rhn.common.security.acl.action.test;

import com.redhat.rhn.common.security.acl.action.ActionAclHandler;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.test.ActionFactoryTest;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.UserTestUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * ActionAclHandlerTest
 * @version $Rev$
 */
public class ActionAclHandlerTest extends RhnBaseTestCase {

    public void testAclGenericActionType() throws Exception {
        ActionAclHandler access = new ActionAclHandler();
        Action newA = ActionFactoryTest.createAction(UserTestUtils.createUser(
                "testUser",
                UserTestUtils.createOrg("testOrg" + this.getClass().getSimpleName())),
                ActionFactory.TYPE_PACKAGES_REMOVE);

        String[] foo = {"remove"};
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("aid", newA.getId().toString());
        boolean rc = access.aclGenericActionType(params, foo);
        assertTrue(rc);
    }
}
