/**
 * Copyright (c) 2016 SUSE LLC
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
package com.suse.utils.test;

import com.suse.utils.Lists;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ListsTest extends TestCase {

    public void testEmpty() {
        List<Integer> emptyList = new LinkedList<Integer>();
        assertTrue(Lists.combinations(Arrays.asList(emptyList)).isEmpty());
    }

    public void testCombinations() {
        List<Integer> emptyList = new LinkedList<Integer>();

        assertTrue(
                Lists.combinations(Arrays.asList(
                        Arrays.asList(1, 2),
                        emptyList,
                        Arrays.asList(5, 6)
                )).isEmpty()
        );
        assertTrue(
                Lists.combinations(Arrays.asList(
                        emptyList,
                        Arrays.asList(1, 2),
                        Arrays.asList(5, 6)
                )).isEmpty()
        );
        assertTrue(
                Lists.combinations(Arrays.asList(
                        Arrays.asList(1, 2),
                        Arrays.asList(5, 6),
                        emptyList
                )).isEmpty()
        );
    }

    public void testCombination() {
        List<List<Integer>> combinations = Lists.combinations(Arrays.asList(
                Arrays.asList(1, 2),
                Arrays.asList(3, 4),
                Arrays.asList(5, 6)
        ));

        List<List<Integer>> lists = Arrays.asList(
                Arrays.asList(1, 3, 5),
                Arrays.asList(1, 3, 6),
                Arrays.asList(1, 4, 5),
                Arrays.asList(1, 4, 6),
                Arrays.asList(2, 3, 5),
                Arrays.asList(2, 3, 6),
                Arrays.asList(2, 4, 5),
                Arrays.asList(2, 4, 6)
        );
        assertEquals(lists, combinations);
    }
}
