/**
 * Copyright (c) 2018 SUSE LLC
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
package com.redhat.rhn.domain.product;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Tuple with 3 independent generic elements.
 * @param <A> type of element 1
 * @param <B> type of element 2
 * @param <C> type of element 3
 */
public class Tuple3<A, B, C> {
    private final A a;
    private final B b;
    private final C c;

    /**
     * Construct a 3 element tuple.
     * @param aIn element 1
     * @param bIn element 2
     * @param cIn element 3
     */
    public Tuple3(A aIn, B bIn, C cIn) {
        this.a = aIn;
        this.b = bIn;
        this.c = cIn;
    }

    /**
     * @return return first element.
     */
    public A getA() {
        return a;
    }

    /**
     * @return return second element.
     */
    public B getB() {
        return b;
    }

    /**
     * @return return third element.
     */
    public C getC() {
        return c;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getA())
                .append(getB())
                .append(getC())
                .toHashCode();
    }

    @Override
    public String toString() {
        return "(" + getA().toString() + "," + getB().toString() + "," + getC().toString() + ")";
    }

    @Override
    public boolean equals(Object otherObject) {
        if (!(otherObject instanceof Tuple3)) {
            return false;
        }
        Tuple3 other = (Tuple3) otherObject;
        return new EqualsBuilder()
                .append(getA(), other.getA())
                .append(getB(), other.getB())
                .append(getC(), other.getC())
                .isEquals();
    }
}
