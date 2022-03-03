/*
 * Copyright (c) 2021 SUSE LLC
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
package com.suse.manager.virtualization;

import org.jdom.Element;

import java.util.Optional;
import java.util.function.Function;

public class Range<T> {
    private final T start;
    private final T end;

    /**
     * Construct a range object.
     *
     * @param startIn the start value of the range
     * @param endIn the end value of the range
     *
     */
    public Range(T startIn, T endIn) {
        this.start = startIn;
        this.end = endIn;
    }
    /**
     * @return start value of the range
     */
    public T getStart() {
        return start;
    }

    /**
     * @return end value of the range
     */
    public T getEnd() {
        return end;
    }

    /**
     * Parse a range XML node
     *
     * @param node the node to parse
     *
     * @return the Range definition
     */
    public static Optional<Range<String>> parse(Element node) {
        return parse(node, Function.identity());
    }

    /**
     * Parse a range XML node
     *
     * @param node the node to parse
     * @param converter function converting the string representation to the destination type
     * @param <T> The type of the range to create
     *
     * @return the Range definition
     */
    public static <T> Optional<Range<T>> parse(Element node, Function<String, T> converter) {
        if (node == null) {
            return Optional.empty();
        }
        Range<T> def = new Range<>(
                converter.apply(node.getAttributeValue("start")),
                converter.apply(node.getAttributeValue("end"))
        );
        return Optional.of(def);
    }
}
