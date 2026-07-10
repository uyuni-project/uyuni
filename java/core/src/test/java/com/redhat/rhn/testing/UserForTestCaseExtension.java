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
    public void beforeEach(ExtensionContext extensionContextIn) throws Exception {
        extendAnnotatedValue(extensionContextIn, UserForTest.class, null, User.class);
    }

    @Override
    public void afterEach(ExtensionContext extensionContextIn) throws Exception {
        //empty
    }

    @Override
    protected void setInstanceField(Field field, Object instance, Object extensionObjectValue)
            throws IllegalAccessException {
        UserForTest annotation = field.getAnnotation(UserForTest.class);
        User user = UserTestUtils.createUser(annotation.userName(), annotation.orgName());
        field.set(instance, user);
    }
}
