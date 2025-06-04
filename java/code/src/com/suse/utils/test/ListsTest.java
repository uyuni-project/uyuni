/*
 * Copyright (c) 2016--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.utils.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.suse.utils.Lists;

import org.junit.jupiter.api.Test;

import java.util.List;

public class ListsTest  {

    @Test
   public void testEmpty() {
       assertTrue(Lists.combinations(List.of()).isEmpty());
   }

    @Test
   public void testCombinations() {
      assertTrue(
          Lists.combinations(List.of(
                  List.of(1, 2),
                  List.of(),
                  List.of(5, 6)
          )).isEmpty()
      );
      assertTrue(
          Lists.combinations(List.of(
                  List.of(),
                  List.of(1, 2),
                  List.of(5, 6)
          )).isEmpty()
      );
      assertTrue(
          Lists.combinations(List.of(
                  List.of(1, 2),
                  List.of(5, 6),
                  List.of()
          )).isEmpty()
      );
   }

    @Test
   public void testCombination() {
       List<List<Integer>> combinations = Lists.combinations(List.of(
               List.of(1, 2),
               List.of(3, 4),
               List.of(5, 6)
       ));

       List<List<Integer>> lists = List.of(
               List.of(1, 3, 5),
               List.of(1, 3, 6),
               List.of(1, 4, 5),
               List.of(1, 4, 6),
               List.of(2, 3, 5),
               List.of(2, 3, 6),
               List.of(2, 4, 5),
               List.of(2, 4, 6)
       );
       assertEquals(lists, combinations);
   }

    @Test
    public void testUnion() {
        List<Integer> result = Lists.union(List.of(1, 2, 3), List.of(3, 4, 5));
        assertEquals(List.of(1, 2, 3, 3, 4, 5), result);
    }

    @Test
    public void testNullSafetyUnion() {
        List<Integer> result = Lists.union(null, List.of(3, 4, 5));
        assertEquals(List.of(3, 4, 5), result);

        result = Lists.union(List.of(1, 2, 3), null);
        assertEquals(List.of(1, 2, 3), result);
    }
}
