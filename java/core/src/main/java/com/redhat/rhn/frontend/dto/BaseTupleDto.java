/*
 * Copyright (c) 2022 SUSE LLC
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
package com.redhat.rhn.frontend.dto;

import java.util.Optional;

import javax.persistence.Tuple;

/**
 * Base class for DTO objects that can be constructed from a Tuple
 */
public abstract class BaseTupleDto extends BaseDto {

    /**
     * Needed for the legacy way
     */
    protected BaseTupleDto() {
        super();
    }

    /**
     * Build the DTO from a tuple
     *
     * @param tuple the query returned tuple to init the object from
     */
    protected BaseTupleDto(Tuple tuple) {
        super();
    }

    protected static <T> Optional<T> getTupleValue(Tuple tuple, String name, Class<T> clazz) {
        try {
            return Optional.ofNullable(tuple.get(name, clazz));
        }
        catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
