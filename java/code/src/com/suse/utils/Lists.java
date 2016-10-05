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
package com.suse.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;


/**
 * Utility functions for lists
 */
public class Lists {

    private Lists() {
    }

    private static <T> List<List<T>>  combinations(List<List<T>> acc, List<T> list) {
        List<List<T>> result = new LinkedList<>();
        for (List<T> ts : acc) {
            for (T t : list) {
                ArrayList<T> c = new ArrayList<>(ts.size() + 1);
                c.addAll(ts);
                c.add(t);
                result.add(c);
            }
        }
        return result;
    }

    /**
     * returns the cartesian product of the input lists
     *
     * @param lists input lists
     * @param <T> list element type
     * @return cartesian product of the input
     */
    public static <T> List<List<T>>  combinations(List<List<T>> lists) {
        if (lists.size() < 1) {
            return new ArrayList<>();
        }
        else {
            List<List<T>> result = new ArrayList<>(lists.get(0).size());
            for (T t : lists.get(0)) {
                result.add(Collections.singletonList(t));
            }
            List<List<T>> rest = lists.subList(1, lists.size());
            for (List<T> list : rest) {
                result = combinations(result, list);
            }
            return result;
        }
    }

    /**
     * Create a list comparator to compare lists by comparing its elements from start
     * end. The shorter list will win if all its elements are equal to the other list.
     * @param elementComparator comparator for list elements
     * @param <T> list element type
     * @return a comparator for lists of T
     */
    public static <T> Comparator<List<T>> listOfListComparator(
            Comparator<T> elementComparator) {
        return new Comparator<List<T>>() {
            @Override
            public int compare(List<T> o1, List<T> o2) {
                if (o1 == o2) {
                    return 0;
                }
                else {
                    for (int i = 0; i < o1.size(); i++) {
                        T t1 = o1.get(i);
                        T t2 = o2.get(i);
                        if (t1 == null && t2 != null) {
                            return 1;
                        }
                        else if (t2 == null && t1 != null) {
                            return -1;
                        }
                        else {
                            int compare = elementComparator.compare(t1, t2);
                            if (compare != 0) {
                                return compare;
                            }
                        }
                    }
                    return 0;
                }
            }
        };
    }
}
