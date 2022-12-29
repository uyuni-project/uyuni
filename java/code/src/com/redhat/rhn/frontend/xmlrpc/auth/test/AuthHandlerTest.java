/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.frontend.xmlrpc.auth.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.domain.session.InvalidSessionIdException;
import com.redhat.rhn.domain.session.WebSession;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.UserLoginException;
import com.redhat.rhn.frontend.xmlrpc.auth.AuthHandler;
import com.redhat.rhn.manager.session.SessionManager;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import org.junit.jupiter.api.Test;

public class AuthHandlerTest extends RhnBaseTestCase {

    @Test
    public void testLogoutWithInvalidKey() {
        AuthHandler handler = new AuthHandler();
        try {
            handler.logout("foo");
            fail("a key of foo passed into logout should throw an exception");
        }
        catch (InvalidSessionIdException e) {
            // success
        }
    }

    @Test
    public void testLoginLogout() throws Exception {
        AuthHandler handler = new AuthHandler();
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        long dbLifetime = Long.parseLong(Config.get().getString("session_database_lifetime"));
        long currentTime = System.currentTimeMillis() / 1000;

        Integer invalidDuration = Integer.valueOf(String.valueOf(dbLifetime + 100));

        //Test the login(String username, String password) method
        // - make sure we create a valid session
        // - make sure the expires got set correctly
        String key1 = handler.login(user.getLogin(), "password");
        WebSession s = SessionManager.loadSession(key1);
        //make sure the getExpires is sometime in the future
        assertTrue(s.getExpires() > currentTime);

        //Test bad login
        try {
            handler.login("-21jkfskljs23412390233219", "foo");
        }
        catch (UserLoginException e) {
            //success
        }

        //Test invalid session
        try {
            handler.login(user.getLogin(), "password", invalidDuration);
        }
        catch (Exception e) {
            //success
        }

        /*
         * Since we're here and we have a sessionkey and a logged in user,
         * let's test BaseHandler.getLoggedInUser.
         */
        User user2 = AuthHandler.getLoggedInUser(key1);
        assertEquals(user, user2);

        try {
            user2 = AuthHandler.getLoggedInUser("foo");
            fail("BaseHandler.getLoggedInUser() took in an invalid session key");
        }
        catch (InvalidSessionIdException e) {
            //success
        }

        //Make sure logout works
        try {
            handler.logout("foo");
            fail("AuthHandler.logout() took an invalid session key");
        }
        catch (InvalidSessionIdException e) {
            //success
        }
        handler.logout(key1);

        //make sure key1 was removed
        try {
            SessionManager.lookupByKey(key1);
            fail("AuthHandler.logout() didn't kill session");
        }
        catch (LookupException e) {
            //success
        }
    }

    @Test
    public void testSessionKeyValidity() throws Exception {
        AuthHandler handler = new AuthHandler();
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());

        String key = handler.login(user.getLogin(), "password");

        //Make sure key is a valid one
        boolean isValid = handler.isSessionKeyValid(key);
        assertTrue(isValid);
        handler.logout(key);

        //Make sure key is not a valid which means we get the exception
        try {
            handler.isSessionKeyValid(key);
            fail("AuthHandler.logout() didn't kill session");
        }
        catch (LookupException e) {
            //success
        }
    }

    @Test
    public void testCheckAuthToken() {
        AuthHandler handler = new AuthHandler();
        assertEquals(0, handler.checkAuthToken(TestUtils.randomString(),
                TestUtils.randomString()));
    }
}
