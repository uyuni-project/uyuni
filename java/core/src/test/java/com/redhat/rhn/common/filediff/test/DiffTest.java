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
package com.redhat.rhn.common.filediff.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.filediff.ChangeHunk;
import com.redhat.rhn.common.filediff.Diff;
import com.redhat.rhn.common.filediff.Hunk;
import com.redhat.rhn.common.filediff.InsertHunk;
import com.redhat.rhn.common.filediff.MatchHunk;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;

public class DiffTest extends RhnBaseTestCase {

    @Test
    public void testDiff() {
        String[] testOld = {"one", "two", "three", "four"};
        String[] testNew = {"one", "too", "three", "for"};
        Class<?>[] testTypes = { MatchHunk.class, ChangeHunk.class,
                MatchHunk.class, ChangeHunk.class};
        checkDiff(testOld, testNew, testTypes);

        String[] testOld1 = {"one", "two", "three", "four"};
        String[] testNew1 = {"one", "two", "too", "three", "four", "for"};
        Class<?>[] testTypes1 = { MatchHunk.class, InsertHunk.class,
                MatchHunk.class, InsertHunk.class};
        checkDiff(testOld1, testNew1, testTypes1);

        String[] testOld2 = {"one", "two", "three", "four"};
        String[] testNew2 = {"one", "for"};
        Class<?>[] testTypes2 = { MatchHunk.class, ChangeHunk.class };
        checkDiff(testOld2, testNew2, testTypes2);
    }

    @Test
    public void testEmptyFiles() {
        String[] testOld = new String[0];
        String[] testNew = new String[0];
        Class<?>[] testType = new Class[0];
        checkDiff(testOld, testNew, testType);
    }

    private void checkDiff(String[] oldFile, String[] newFile, Class<?>[] types) {
        Diff diff = new Diff(oldFile, newFile);
        List<Hunk> hunks = diff.diffFiles();
        assertEquals(types.length, hunks.size());
        Iterator<Hunk> i = hunks.iterator();
        int a = 0;
        while (i.hasNext()) {
            assertTrue(types[a].isInstance(i.next()));
            a++;
        }
    }

}
