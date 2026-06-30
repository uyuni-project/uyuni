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

import java.lang.reflect.Field;

public class UserForTestCaseExtension extends TestCaseExtension implements BeforeEachCallback, AfterEachCallback {
    protected User temporaryUser;

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
        boolean useUserName = StringUtils.isNotEmpty(annotation.userName());
        boolean useOrgName = StringUtils.isNotEmpty(annotation.orgName());
        boolean useClassNameForOrg = annotation.useClassNameForOrg();

        if (useClassNameForOrg && (useUserName || useOrgName)) {
            throw new IllegalStateException("Annotation param useClassNameForOrg must be used alone");
        }

        if (useUserName != useOrgName) {
            throw new IllegalStateException("Annotation params userName and orgName must be used together");
        }

        if (useClassNameForOrg) {
            temporaryUser = UserTestUtils.createUser(instance);
        }
        else if (useUserName && useOrgName) {
            temporaryUser = UserTestUtils.createUser(annotation.userName(), annotation.orgName());
        }
        else {
            temporaryUser = UserTestUtils.createUser();
        }

        field.set(instance, temporaryUser);
    }
}
