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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.manager.utils.test;

import com.redhat.rhn.frontend.dto.BaseTupleDto;

import java.util.Objects;

import javax.persistence.Tuple;

/**
 * Test Dto class for PagedSqlQueryBuilder test
 */
public class TestDto extends BaseTupleDto {
        private final Long id;
        private final String name;

        public TestDto(Long idIn, String nameIn) {
            id = idIn;
            name = nameIn;
        }

        public TestDto(Tuple tuple) {
            id = tuple.get("id", Number.class).longValue();
            name = tuple.get("name", String.class);
        }

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public boolean equals(Object oIn) {
            if (this == oIn) {
                return true;
            }
            if (oIn == null || getClass() != oIn.getClass()) {
                return false;
            }
            TestDto testDto = (TestDto) oIn;
            return Objects.equals(id, testDto.id) &&
                    Objects.equals(name, testDto.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name);
        }
}
