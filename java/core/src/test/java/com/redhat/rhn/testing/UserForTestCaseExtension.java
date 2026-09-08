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

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.testing.UserForTest.UserRole;
import com.redhat.rhn.testing.building.OrgBuilder;
import com.redhat.rhn.testing.building.UserBuilder;

import org.hibernate.TransactionException;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserForTestCaseExtension extends TestCaseExtension
    implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private final Map<String, Org> organizationMap;

    private final Map<String, User> userMap;

    private boolean isCommitted;

    public UserForTestCaseExtension() {
        organizationMap = new HashMap<>();
        userMap = new HashMap<>();
        isCommitted = false;
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        isCommitted = false;

        HibernateFactory.addCommitListener(() -> isCommitted = true);
        injectAnnotatedFields(extensionContext, UserForTest.class, User.class, true, true);
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        try {
            rollbackAndCloseSession();
        }
        finally {
            isCommitted = false;

            organizationMap.clear();
            userMap.clear();
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
        throws ParameterResolutionException {
        Parameter parameter = parameterContext.getParameter();
        return User.class.equals(parameter.getType()) && parameter.isAnnotationPresent(UserForTest.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
        throws ParameterResolutionException {
        return createNewUser(extensionContext, parameterContext.getParameter().getAnnotation(UserForTest.class));
    }

    @Override
    protected Object resolveFieldValue(ExtensionContext extensionContext, Field targetField) {
        return createNewUser(extensionContext, targetField.getAnnotation(UserForTest.class));
    }

    private Object createNewUser(ExtensionContext extensionContext, UserForTest annotation) {
        Org org = organizationMap.computeIfAbsent(annotation.orgName(), name -> createOrg(extensionContext, name));
        return userMap.computeIfAbsent(annotation.userName(), name -> createUser(name, annotation.role(), org));
    }


    private void rollbackAndCloseSession() {
        HibernateFactory.removeAllCommitListeners();

        TransactionException rollbackException = null;
        if (HibernateFactory.inTransaction()) {
            try {
                HibernateFactory.rollbackTransaction();
            }
            catch (TransactionException e) {
                rollbackException = e;
            }
        }

        HibernateFactory.closeSession();

        if (isCommitted) {
            Map<String, List<User>> orgUsersMap = userMap.values().stream()
                    .collect(Collectors.groupingBy(user -> user.getOrg().getName()));

            // Remove all the created organization, cascading all the data
            organizationMap.values().forEach(org -> {
                List<User> users = orgUsersMap.get(org.getName());
                User firstUser = users.isEmpty() ? null : users.remove(0);

                users.forEach(user -> UserFactory.deleteUser(user.getId()));
                OrgFactory.deleteOrg(org.getId(), firstUser);
            });

            // Remove additional data not connected to an organization
            TestUtils.deleteAllAccessTokens();

            TestUtils.commitAndCloseSession();
            isCommitted = false;
        }

        if (rollbackException != null) {
            throw rollbackException;
        }
    }

    private static User createUser(String userName, UserRole role, Org org) {
        UserBuilder userBuilder = new UserBuilder()
                .withUserName(userName)
                .withOrganization(org);

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
