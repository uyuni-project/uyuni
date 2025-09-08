/*
 * Copyright (c) 2025 SUSE LLC
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
package com.redhat.rhn.testing;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.servlets.PxtSessionDelegate;
import com.redhat.rhn.frontend.servlets.PxtSessionDelegateFactory;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.type.StandardBasicTypes;
import org.jmock.Expectations;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * MockTestUtils, a simple package for utility functions helpful when
 * writing unit tests
 */
public class MockTestUtils {

    /**
     * Private constructor to hide the implicit public one
     */
    private MockTestUtils() {
        throw new IllegalStateException("Cannot initialize utility class MockTestUtils");
    }

    /**
     * Create a MockHttpServletRequest with session and user setup.
     * This is a simplified version that just creates the basic mock without full user/session setup.
     * @return MockHttpServletRequest with basic setup
     */
    public static MockHttpServletRequest getRequestWithSessionAndUser() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpSession session = new MockHttpSession();
        request.setupServerName("mlm.dev.suse.com");
        request.setSession(session);

        // Create a test user
        User user = UserTestUtils.findNewUser(
                "testUser",
                "testOrg_getRequestWithSessionAndUser" + RandomStringUtils.randomAlphanumeric(5)
        );
        Long userid = user.getId();

        // Set up the user context using PxtSessionDelegate
        PxtSessionDelegateFactory pxtDelegateFactory = PxtSessionDelegateFactory.getInstance();
        PxtSessionDelegate pxtDelegate = pxtDelegateFactory.newPxtSessionDelegate();

        // Update the web user id in the request context
        // required for getCurrentUser() to work
        pxtDelegate.updateWebUserId(request, response, userid);

        // Set the uid parameter
        request.addCookie(response.getCookie("pxt-session-cookie"));
        request.setupAddParameter("uid", userid.toString());

        return request;
    }

    /**
     * Return a random 13 letter string.  Useful for creating unique
     * string labels/names in your tests.
     * @return String that is 13 chars long and alphanumeric
     */
    public static String randomString() {
        return RandomStringUtils.randomAlphanumeric(13);
    }

    /**
     * Return a random letter string.  Useful for creating unique
     * string labels/names in your tests.
     * @param length of the string
     * @return A random alphanumeric string of the specified length
     */
    public static String randomString(int length) {
        return RandomStringUtils.randomAlphanumeric(length);
    }

    /**
     * Create a MockHttpServletRequest with session and user setup using a test case.
     * @param testCase the test case to use for mocking
     * @return MockHttpServletRequest with session and user setup
     * @param <T> the type of the test case
     */
    public static <T extends MockObjectTestCase> HttpServletRequest getRequestWithSessionAndUser(T testCase) {
        MockHttpServletRequest request = testCase.mock(MockHttpServletRequest.class);
        MockHttpServletResponse response = testCase.mock(MockHttpServletResponse.class);
        HttpSession session = testCase.mock(HttpSession.class);

        testCase.context.checking(new Expectations() {{
                allowing(request).getServerName();
                will(returnValue("mlm.dev.suse.com"));
                allowing(request).getSession();
                will(returnValue(session));
        }});

        // Create a test user
        User user = UserTestUtils.findNewUser(
                "testUser",
                "testOrg_getRequestWithSessionAndUser" + RandomStringUtils.randomAlphanumeric(5)
        );

        // Set up the user context using PxtSessionDelegate
        PxtSessionDelegateFactory pxtDelegateFactory = PxtSessionDelegateFactory.getInstance();
        PxtSessionDelegate pxtDelegate = pxtDelegateFactory.newPxtSessionDelegate();

        // Update the web user id in the request context
        // required for getCurrentUser() to work
        pxtDelegate.updateWebUserId(request, response, user.getId());

        // Set the uid parameter
        request.addCookie(response.getCookie("pxt-session-cookie"));
        request.setupAddParameter("uid", user.getId().toString());

        return request;
    }

    /**
     * Sets the Config to indicate we want to be in
     * Debug mode for Localization.  Usefull for checking
     * if a set of strings is l10ned.
     *
     */
    public static void enableLocalizationDebugMode() {
        Config.get().setString("java.l10n_debug", "true");
    }

    /**
     * Check the string to see if it passed through the LocalizationService.
     * @param checkMe String to check if it was l10ned
     * @return boolean if or not it was localized
     */
    public static boolean isLocalized(String checkMe) {
        if (!Boolean.valueOf(
                Config.get().getString("java.l10n_debug", "false"))) {
            throw new
                    IllegalArgumentException("java.l10n_debug is set to false.  " +
                    "This test doesnt mean anything if its set to false. ");
        }
        return (checkMe.startsWith(
                Config.get().getString("java.l10n_debug_marker", "$$$")));
    }

    /**
     * Turns of the Config setting for L10N debug mode
     */
    public static void disableLocalizationDebugMode() {
        Config.get().setString("java.l10n_debug", "false");
    }

    /**
     * Helper method to get a single object from the 2nd level cache by id
     * @param id Id of the object you want
     * @param queryname Queryname for the query you want to run.
     *        queryname *MUST* have an :id attribute in it.
     * @return Returns the object corresponding to id
     */
    public static Object lookupFromCacheById(Long id, String queryname) {
        Session session = HibernateFactory.getSession();
        return session.getNamedQuery(queryname)
                .setParameter("id", id, StandardBasicTypes.LONG)
                //Retrieve from cache if there
                .setCacheable(true)
                .uniqueResult();
    }

    /**
     * Enable the *** ERROR: Message with id: [asciiString] not found.***
     * errors.   Some tests pass in non-translated strings which is OK.
     */
    public static void enableLocalizationLogging() {
        Configurator.setLevel(LocalizationService.class.getName(), Level.WARN);
    }
}
