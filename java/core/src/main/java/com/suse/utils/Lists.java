/*
 * Copyright (c) 2016--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.suse.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;


/**
 * Utility functions for lists
 */
public class Lists {

    private Lists() {
    }


    private static <T> List<List<T>> combinations(List<List<T>> acc, List<T> list) {
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
        if (lists.isEmpty()) {
            return new ArrayList<>();
        }

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

    /**
     * Create a list comparator to compare lists by comparing its elements from start
     * end. The shorter list will win if all its elements are equal to the other list.
     * @param elementComparator comparator for list elements
     * @param <T> list element type
     * @return a comparator for lists of T
     */
    public static <T> Comparator<List<T>> listOfListComparator(Comparator<T> elementComparator) {
        return (o1, o2) -> {
            if (o1 == o2) {
                return 0;
            }

            for (int i = 0; i < o1.size(); i++) {
                T t1 = o1.get(i);
                T t2 = o2.get(i);

                if (t1 == null && t2 != null) {
                    return 1;
                }

                if (t2 == null && t1 != null) {
                    return -1;
                }

                int compare = elementComparator.compare(t1, t2);
                if (compare != 0) {
                    return compare;
                }
            }

            return 0;
        };
    }

    /**
     * Returns a new list containing all elements from the given lists, with duplicates preserved.
     * If either list is null, it is treated as an empty list.
     *
     * @param <T>   The type of elements in the lists.
     * @param list1 The first list. May be null.
     * @param list2 The second list. May be null.
     * @return      A new list containing all elements from both input lists.
     */
    public static <T> List<T> union(List<T> list1, List<T> list2) {
        ArrayList<T> result = new ArrayList<>(Objects.requireNonNullElseGet(list1, List::of));
        result.addAll(Objects.requireNonNullElseGet(list2, List::of));
        return result;
    }

    /**
     * Returns a new list containing all elements from the given lists, without duplicates
     * If either list is null, it is treated as an empty list.
     *
     * @param <T>   The type of elements in the lists.
     * @param list1 The first list. May be null.
     * @param list2 The second list. May be null.
     * @return      A new list containing all elements from both input lists, without duplicates.
     */
    public static <T> List<T> merge(List<T> list1, List<T> list2) {
        Set<T> resultSet = new HashSet<>(Objects.requireNonNullElseGet(list1, List::of));
        resultSet.addAll(Objects.requireNonNullElseGet(list2, List::of));
        return new ArrayList<>(resultSet);
    }

    /**
     * Returns a new list containing all the elements from list1 that are not present in list2, without duplicates.
     * If either list is null, it is treated as an empty list.
     *
     * @param <T>   The type of elements in the lists.
     * @param list1 The first list. May be null.
     * @param list2 The second list. May be null.
     * @return      A new list containing all the elements from list1 that are not present in list2, without duplicates
     */
    public static <T> List<T> subtract(List<T> list1, List<T> list2) {
        Set<T> resultSet = new HashSet<>(Objects.requireNonNullElseGet(list1, List::of));
        Objects.requireNonNullElseGet(list2, List::<T>of).forEach(resultSet::remove);
        return new ArrayList<>(resultSet);
    }
}
