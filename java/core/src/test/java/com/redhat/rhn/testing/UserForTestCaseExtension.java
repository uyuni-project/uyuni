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

import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.UserForTest.UserRole;
import com.redhat.rhn.testing.building.OrgBuilder;
import com.redhat.rhn.testing.building.UserBuilder;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserForTestCaseExtension extends TestCaseExtension implements BeforeEachCallback, AfterEachCallback {

    private final Map<String, Org> organizationMap;

    private final Map<String, User> userMap;

    public UserForTestCaseExtension() {
        organizationMap = new HashMap<>();
        userMap = new HashMap<>();
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        injectAnnotatedFields(extensionContext, UserForTest.class, User.class, true, true);
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        organizationMap.clear();
        userMap.clear();
    }

    @Override
    protected Object resolveFieldValue(ExtensionContext extensionContext, Field targetField) {
        UserForTest annotation = targetField.getAnnotation(UserForTest.class);

        Org org = organizationMap.computeIfAbsent(annotation.orgName(), name -> createOrg(extensionContext, name));
        return userMap.computeIfAbsent(annotation.userName(), name -> createUser(name, annotation.role(), org));
    }

    private static User createUser(String userName, UserRole role, Org org) {
        UserBuilder userBuilder = new UserBuilder()
                .withUserName(userName)
                .withOrganizationId(org.getId());

        switch (role) {
            case REGULAR:
                userBuilder.asRegular();
                break;
            case ORG_ADMIN:
                userBuilder.asOrgAdmin();
                break;
            case SAT_ADMIN:
                userBuilder.asSatAdmin();
                break;
            default:
                throw new IllegalArgumentException("Unexpected UserRole " + role);
        }

        return userBuilder.build();
    }

    private static Org createOrg(ExtensionContext extensionContext, String orgName) {
        // Build a suffix Test.method to track the test which created the organization
        String suffix = Stream.concat(
                            extensionContext.getTestClass().map(Class::getSimpleName).stream(),
                            extensionContext.getTestMethod().map(Method::getName).stream()
                        ).collect(Collectors.joining("-"));

        return new OrgBuilder().withName(orgName).withSuffix(suffix).build();
    }
}
