/*
 * Copyright (c) 2020 SUSE LLC
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
package com.redhat.rhn.common.util;

import java.util.Comparator;

/**
 * DebVersionComparator
 */
public class DebVersionComparator implements Comparator<String> {

    /**
     * {@inheritDoc}
     * <p>
     * Compare two versions, *a* and *b*, and return an integer value which has
     * the same meaning as the built-in :func:`cmp` function's return value has,
     * see the following table for details.
     * <p>
     * .. table:: Return values
     * <p>
     * ===== =============================================
     * Value      Meaning
     * ===== =============================================
     * > 0   The version *a* is greater than version *b*.
     * = 0   Both versions are equal.
     * < 0   The version *a* is less than version *b*.
     * ===== =============================================
     *
     * See: https://www.debian.org/doc/debian-policy/ch-controlfields.html#version
     * See: https://salsa.debian.org/dpkg-team/dpkg/blob/master/lib/dpkg/version.c#L140
     */
    @Override
    public int compare(String o1, String o2) {
        String version1 = o1, revision1 = null, version2 = o2, revision2 = null;
        /* Split version and revision: https://www.debian.org/doc/debian-policy/ch-controlfields.html#version */

        int hyphen = version1.lastIndexOf('-');
        if (hyphen > 0) {
            revision1 = version1.substring(hyphen + 1);
            version1 = version1.substring(0, hyphen);
        }
        hyphen = version2.lastIndexOf('-');
        if (hyphen > 0) {
            revision2 = version2.substring(hyphen + 1);
            version2 = version2.substring(0, hyphen);
        }
        int rc = verrevcmp(version1, version2);
        if (rc > 0) {
            return 1;
        }
        else if (rc < 0) {
            return -1;
        }
        else { /* (rc == 0) */
            int rv = verrevcmp(revision1, revision2);
            if (rv > 0) {
                return 1;
            }
            else if (rv < 0) {
                return -1;
            }
            return 0;
        }
    }

    private int order(int c) {
        if (Character.isDigit(c)) {
            return 0;
        }
        else if (Character.isLetter(c)) {
            return c;
        }
        else if (c == '~') {
            return -1;
        }
        else if (c != 0) {
            return c + 256;
        }
        else {
            return 0;
        }
    }

    private int verrevcmp(String a1, String b1) {
        char[] a, b;

        if (a1 == null) {
            a1 = "";
        }
        if (b1 == null) {
            b1 = "";
        }

        a = a1.toCharArray();
        b = b1.toCharArray();

        int i = 0;
        int j = 0;

        while (i < a.length || j < b.length) {
            int firstDiff = 0;

            while ((i < a.length && !Character.isDigit(a[i])) || (j < b.length && !Character.isDigit(b[j]))) {
                int ac = i >= a.length ? 0 : order(a[i]);
                int bc = j >= b.length ? 0 : order(b[j]);

                if (ac != bc) {
                    return ac - bc;
                }

                i++;
                j++;
            }
            while (i < a.length && a[i] == '0') {
                i++;
            }
            while (j < b.length && b[j] == '0') {
                j++;
            }
            while (i < a.length && j < b.length && Character.isDigit(a[i]) && Character.isDigit((b[j]))) {
                if (firstDiff == 0) {
                    firstDiff = a[i] - b[j];
                }
                i++;
                j++;
            }

            if (i < a.length && Character.isDigit(a[i])) {
                return 1;
            }
            if (j < b.length && Character.isDigit(b[j])) {
                return -1;
            }
            if (firstDiff != 0) {
                return firstDiff;
            }
        }
        return 0;
    }
}

