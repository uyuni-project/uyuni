/*
 * Copyright (c) 2024--2025 SUSE LLC
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

package com.redhat.rhn.domain;

import java.sql.Types;
import java.util.Arrays;
import java.util.Objects;

/**
 * A {@link CustomEnumType} that maps a Java enum to a PostgreSQL enum type.
 *
 * <p>This type handles the conversion between Java enum values and their corresponding
 * string representations in the PostgreSQL database.
 *
 * <p>If the enum type implements the {@link Labeled} interface, the
 * {@link Labeled#getLabel()} method is used to obtain the value stored in the database.
 * Otherwise, the standard {@link Enum#name()} method is called.
 *
 * @param <T> the type of the enum
 */
public abstract class DatabaseEnumType<T extends Enum<T>> extends CustomEnumType<T, String> {

    /**
     * Construct an instance for the specified enum class.
     *
     * @param enumClassIn the enum class
     */
    protected DatabaseEnumType(Class<T> enumClassIn) {
        super(enumClassIn, String.class, e -> getLabel(e), v -> findByLabel(enumClassIn, v));
    }

    // Converts the given enum constant to the string representation used in the database.
    private static <T extends Enum<T>> String getLabel(T value) {
        if (value instanceof Labeled labeled) {
            return labeled.getLabel();
        }

        return value.name();
    }

    /**
     * Converts a string value from the database to the proper enum value.
     * @param enumType the class of the enum
     * @param label the database value
     * @return an instance of the specified enum
     * @param <T> the enum
      */
    public static <T extends Enum<T>> T findByLabel(Class<T> enumType, String label) {
        return Arrays.stream(enumType.getEnumConstants())
            .filter(e -> Objects.equals(label, getLabel(e)))
            .findFirst()
            .orElseThrow(
                () -> new IllegalArgumentException("Invalid %s value %s".formatted(enumType.getName(), label))
            );
    }

    /**
     * {@inheritDoc}
     * <p>
     * @returns {@link Types#OTHER}, as this instance is mapping the enum values to a PostgreSQL enum.
     */
    @Override
    public int getSqlType() {
        return Types.OTHER;
    }
}
