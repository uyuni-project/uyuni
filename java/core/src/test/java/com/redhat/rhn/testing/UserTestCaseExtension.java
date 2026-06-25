/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.testing;

import com.redhat.rhn.domain.user.User;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class UserTestCaseExtension implements BeforeEachCallback, AfterEachCallback {
    private User user;

    private Object suffixObject = null;
    private String userName = null;
    private String orgName = null;

    public UserTestCaseExtension() {
        //empty
    }

    public UserTestCaseExtension(Object suffixObjectIn) {
        suffixObject = suffixObjectIn;
    }

    public UserTestCaseExtension(String userNameIn, String orgNameIn) {
        userName = userNameIn;
        orgName = orgNameIn;
    }

    @Override
    public void beforeEach(ExtensionContext extensionContextIn) throws Exception {
        if (null != suffixObject) {
            user = UserTestUtils.createUser(suffixObject);
        }
        else if (StringUtils.isNotEmpty(userName) && StringUtils.isNotEmpty(orgName)) {
            user = UserTestUtils.createUser(userName, orgName);
        }
        else {
            user = UserTestUtils.createUser();
        }
    }

    @Override
    public void afterEach(ExtensionContext extensionContextIn) throws Exception {
        //empty
    }

    public User getTestUser() {
        return user;
    }

    public void nullifyTestUser() {
        user = null;
    }

    public void saveAndFlushTestUser() {
        user = TestUtils.saveAndFlush(user);
    }
}
