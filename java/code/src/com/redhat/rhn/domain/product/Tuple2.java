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
 * Tuple with 2 independent generic elements.
 * @param <A> type of element 1
 * @param <B> type of element 2
 */
public class Tuple2<A, B> {
    private final A a;
    private final B b;

    /**
     * Construct a 2 element tuple.
     * @param aIn element 1
     * @param bIn element 2
     */
    public Tuple2(A aIn, B bIn) {
        this.a = aIn;
        this.b = bIn;
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
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getA())
                .append(getB())
                .toHashCode();
    }

    @Override
    public String toString() {
        return "(" + getA().toString() + "," + getB().toString() + ")";
    }

    @Override
    public boolean equals(Object otherObject) {
        if (!(otherObject instanceof Tuple2)) {
            return false;
        }
        Tuple2 other = (Tuple2) otherObject;
        return new EqualsBuilder()
                .append(getA(), other.getA())
                .append(getB(), other.getB())
                .isEquals();
    }
}
