/*
 * Copyright (c) 2024 SUSE LLC
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

import static com.redhat.rhn.common.ExceptionMessage.NOT_INSTANTIABLE;
import static java.util.Objects.isNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

import spark.utils.StringUtils;

/**
 * A collection of utility methods for evaluating object presence and absence.
 */
public final class Predicates {

    /**
     * Determines whether an object is considered to be absent, as per {@link #isProvided(Object)}.
     *
     * @param value the object to be evaluated
     * @return true if the object does not contain meaningful content
     */
    public static boolean isAbsent(Object value) {
        return !(isProvided(value));
    }

    /**
     * Determines whether the value obtained from the given supplier is considered to be absent, as per
     * {@link #isAbsent(Object)}.
     *
     * @param supplier the supplier from which to obtain the value
     * @return true if the value obtained from the supplier is absent based on the predicate, otherwise false
     */
    public static boolean isAbsent(Supplier<?> supplier) {
        return Optional.ofNullable(supplier).map(Supplier::get).map(Predicates::isAbsent).orElse(true);
    }

    /**
     * Determines whether the value obtained from the given supplier is considered to be provided, as per
     * {@link #isAbsent(Object)}.
     *
     * @param supplier the supplier from which to obtain the value
     * @return true if the value obtained from the supplier is provided based on the predicate, otherwise false
     */
    public static boolean isProvided(Supplier<?> supplier) {
        return Optional.ofNullable(supplier).map(Supplier::get).map(Predicates::isProvided).orElse(false);
    }

    /**
     * Determines if an object is whether or not considered to be provided.
     * An object is considered provided if it is non-null and contains meaningful data.
     * Exceptions to this rule include:
     * a) If the object is assignable from CharSequence, it is considered provided if it contains any non-space
     * character.
     * b) If the object is assignable from Collection or an array, it is considered provided if its contents contain
     * any non-null object.
     *
     * <pre>
     * Predicates.isProvided(null)         = false
     * Predicates.isProvided(new Object()) = true
     * Predicates.isProvided("")           = false
     * Predicates.isProvided(" ")          = false
     * Predicates.isProvided(new String()) = false
     * Predicates.isProvided("suse")       = true
     * </pre>
     *
     * @param value the object to be evaluated
     * @return true if the object contains meaningful content
     */
    public static boolean isProvided(Object value) {
        if (isNull(value)) {
            return false;
        }

        if (CharSequence.class.isAssignableFrom(value.getClass())) {
            return !StringUtils.isBlank((CharSequence) value);
        }

        if (Optional.class.isAssignableFrom(value.getClass())) {
            return ((Optional<?>) value).isPresent();
        }

        if (Collection.class.isAssignableFrom(value.getClass())) {
            Collection<?> collection = (Collection<?>) value;
            return !collection.isEmpty() && !allAbsent(collection.toArray());
        }

        if (Object[].class.isAssignableFrom(value.getClass())) {
            Object[] objArray = (Object[]) value;
            return objArray.length > 0 && !allAbsent(objArray);
        }

        return true;
    }

    /**
     * Determines whether ALL provided objects are considered to be provided, as per {@link #isProvided(Object)}.
     *
     * @param args the varargs to be evaluated
     * @return true if all provided objects are considered provided, otherwise false
     */
    public static boolean allProvided(Object... args) {
        return Arrays.stream(args).allMatch(Predicates::isProvided);
    }

    /**
     * Determines whether ALL provided objects are considered to be absent, as per {@link #isAbsent(Object)}.
     *
     * @param collection the collection to be evaluated
     * @return true if none of the elements in the collection are considered provided, otherwise false
     */
    public static boolean noneProvided(Collection<?> collection) {
        if ((collection == null) || collection.isEmpty()) {
            return true;
        }
        return !isProvided(collection);
    }

    /**
     * Determines whether ANY provided objects are considered to be provided, as per {@link #isProvided(Object)}.
     *
     * @param collection the collection to be evaluated
     * @return true if any of the elements in the collection are considered provided, otherwise false
     */
    public static boolean anyProvided(Collection<?> collection) {
        return !(noneProvided(collection));
    }

    /**
     * Determines whether ALL provided objects are considered to be absent, as per {@link #isAbsent(Object)}.
     *
     * @param args the varargs to be evaluated
     * @return true if all provided objects are considered absent, otherwise false
     */
    public static boolean allAbsent(Object... args) {
        return Arrays.stream(args).allMatch(Predicates::isAbsent);
    }

    private Predicates() {
        throw new UnsupportedOperationException(NOT_INSTANTIABLE);
    }


}
