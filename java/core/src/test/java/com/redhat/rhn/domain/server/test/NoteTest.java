/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.server.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.server.Note;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import org.hibernate.Session;
import org.hibernate.type.StandardBasicTypes;
import org.junit.jupiter.api.Test;

import java.util.Date;

/**
 * NoteTest
 */
public class NoteTest extends RhnBaseTestCase {

    /**
     * Test Note creation and equals method
     */
    @Test
    public void testEquals() {
        Note note1 = createTestNote();
        Note note2 = new Note();

        assertNotEquals(note1, note2);
        assertNotEquals(note1, new Date());

        Session session = HibernateFactory.getSession();
        note2 = (Note) session.createQuery("FROM Note AS n WHERE n.id = :id")
                                  .setParameter("id", note1.getId(), StandardBasicTypes.LONG)
                                  .uniqueResult();
        assertEquals(note1, note2);

        TestUtils.removeObject(note1);
    }

    /**
     * Helper method to create a test Note
     * @return A new Note.
     */
    public static Note createTestNote() {
        Note note = new Note();

        User user = UserTestUtils.createUser();
        note.setCreator(user);
        note.setServer(ServerFactoryTest.createTestServer(user));
        note.setSubject("RHN-JAVA Unit tests are good");
        note.setNote("I will write them always.");

        assertNull(note.getId());
        TestUtils.saveAndFlush(note);
        assertNotNull(note.getId());

        return note;
    }
}
