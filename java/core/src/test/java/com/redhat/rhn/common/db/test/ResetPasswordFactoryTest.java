/*
 * Copyright (c) 2015 Red Hat, Inc.
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
package com.redhat.rhn.common.db.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.db.ResetPasswordFactory;
import com.redhat.rhn.domain.common.ResetPassword;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Iterator;

public class ResetPasswordFactoryTest extends BaseTestCaseWithUser {

    @Test
    public void testToken() {
        String tok = ResetPasswordFactory.generatePasswordToken(user);
        assertNotNull(tok);
        assertEquals(64, tok.length());
    }

    @Test
    public void testDirectCreate() {
        ResetPassword rp = new ResetPassword(user.getId(),
                        ResetPasswordFactory.generatePasswordToken(user));
        assertNotNull(rp);
        assertEquals(rp.getUserId(), user.getId());
        assertNotNull(rp.getToken());
        assertTrue(rp.isValid());
        assertFalse(rp.isExpired());
    }

    @Test
    public void testExpired() {
        ResetPassword rp = new ResetPassword(user.getId(),
                        ResetPasswordFactory.generatePasswordToken(user));
        assertNotNull(rp);
        assertFalse(rp.isExpired());

        int expirationHours = Config.get().getInt(ResetPasswordFactory.EXPIRE_TIME, 48);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -(expirationHours - 1));
        rp.setCreated(cal.getTime());
        assertFalse(rp.isExpired());

        cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -(expirationHours + 1));
        rp.setCreated(cal.getTime());
        assertTrue(rp.isExpired());

        cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);
        rp.setCreated(cal.getTime());
        assertTrue(rp.isExpired());
    }

    @Test
    public void testFactoryCreate() {
        ResetPassword rp = ResetPasswordFactory.createNewEntryFor(user);
        assertNotNull(rp);
        assertNotNull(rp);
        assertEquals(rp.getUserId(), user.getId());
        assertNotNull(rp.getToken());
        assertTrue(rp.isValid());
    }

    @Test
    public void testNoToken() {
        ResetPassword rp = ResetPasswordFactory.lookupByToken("Thistokencannotexist");
        assertNull(rp);
    }

    @Test
    public void testRecoverAndDelete() {
        ResetPassword rp = ResetPasswordFactory.createNewEntryFor(user);
        ResetPassword found = ResetPasswordFactory.lookupByToken(rp.getToken());
        assertEquals(found.getUserId(), user.getId());
        assertNotNull(found.getToken());
        assertTrue(found.isValid());
        assertEquals(rp.getToken(), found.getToken());
        int rmv = ResetPasswordFactory.deleteUserTokens(user.getId());
        assertEquals(1, rmv);
    }

    @Test
    public void testInvalidateOne() {
        ResetPassword rp = ResetPasswordFactory.createNewEntryFor(user);
        assertNotNull(rp);
        ResetPasswordFactory.invalidateToken(rp.getToken());
        ResetPassword found = ResetPasswordFactory.lookupByToken(rp.getToken());
        assertFalse(found.isValid());
    }

    @Test
    public void testInvalidate() {
        ResetPassword rp = ResetPasswordFactory.createNewEntryFor(user);
        assertNotNull(rp);
        int rmvd = ResetPasswordFactory.deleteUserTokens(user.getId());
        assertEquals(1, rmvd);
        ResetPassword rp1 = ResetPasswordFactory.createNewEntryFor(user);
        ResetPassword rp2 = ResetPasswordFactory.createNewEntryFor(user);
        ResetPassword rp3 = ResetPasswordFactory.createNewEntryFor(user);
        assertNotSame(rp1, rp2);
        assertNotSame(rp1, rp3);
        assertNotSame(rp2, rp3);

        ResetPassword found = ResetPasswordFactory.lookupByToken(rp1.getToken());
        assertNotNull(found);
        assertTrue(found.isValid());

        found = ResetPasswordFactory.lookupByToken(rp2.getToken());
        assertNotNull(found);
        assertTrue(found.isValid());

        found = ResetPasswordFactory.lookupByToken(rp3.getToken());
        assertNotNull(found);
        assertTrue(found.isValid());

        int inv = ResetPasswordFactory.invalidateUserTokens(user.getId());
        assertEquals(3, inv);

        rmvd = ResetPasswordFactory.deleteUserTokens(user.getId());
        assertEquals(3, rmvd);
    }

    @Test
    public void testFindErrors() {
        ResetPassword rp = ResetPasswordFactory.createNewEntryFor(user);
        assertNotNull(rp);

        // Everything OK
        ActionErrors errors = ResetPasswordFactory.findErrors(rp);
        assertNotNull(errors);
        assertTrue(errors.isEmpty());

        // No Token
        errors = ResetPasswordFactory.findErrors(null);
        assertEquals(1, errors.size());
        Iterator<ActionMessage> iter = errors.get(ActionMessages.GLOBAL_MESSAGE);
        while (iter.hasNext()) {
            ActionMessage am = iter.next();
            assertEquals("resetpassword.jsp.error.notoken", am.getKey());
        }

        // Invalid token
        ResetPassword rp1 = ResetPasswordFactory.createNewEntryFor(user);
        rp1.setIsValid(false);
        errors = ResetPasswordFactory.findErrors(rp1);
        assertEquals(1, errors.size());
        iter = errors.get(ActionMessages.GLOBAL_MESSAGE);
        while (iter.hasNext()) {
            ActionMessage am = iter.next();
            assertEquals("resetpassword.jsp.error.invalidtoken", am.getKey());
        }
        rp1.setIsValid(true);

        // Expired token
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);
        rp1.setCreated(cal.getTime());
        errors = ResetPasswordFactory.findErrors(rp1);
        assertEquals(1, errors.size());
        iter = errors.get(ActionMessages.GLOBAL_MESSAGE);
        while (iter.hasNext()) {
            ActionMessage am = iter.next();
            assertEquals("resetpassword.jsp.error.expiredtoken", am.getKey());
        }
    }
}
