/*
 * Copyright (c) 2010 Red Hat, Inc.
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
package com.redhat.rhn.frontend.xmlrpc.taskomatic;

import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;

import java.util.List;


/**
 * TaskomaticOrgHandler
 * @apidoc.namespace taskomatic.org
 * @apidoc.ignore
 * @apidoc.doc provides methods to access orgaznizational taskomatic tasks
 */
public class TaskomaticOrgHandler extends TaskomaticHandler {
    @Override
    protected void checkUserRole(User user) {
        ensureUserRole(user, RoleFactory.ORG_ADMIN);
    }

    @Override
    protected void addParameters(User user, List params) {
        params.add(0, user.getOrg().getId());
    }
}
