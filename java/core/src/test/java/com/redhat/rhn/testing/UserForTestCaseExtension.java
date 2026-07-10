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

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Field;

public class UserForTestCaseExtension extends TestCaseExtension implements BeforeEachCallback, AfterEachCallback {
    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        injectAnnotatedFields(extensionContext, UserForTest.class, User.class);
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        //empty
    }

    @Override
    protected Object resolveFieldValue(ExtensionContext extensionContext, Field targetField) {
        UserForTest annotation = targetField.getAnnotation(UserForTest.class);
        return UserTestUtils.createUser(annotation.userName(), annotation.orgName());
    }
}
