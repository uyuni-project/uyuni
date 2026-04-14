/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.domain.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * ArchTypeTest
 */
public class ChecksumTypeTest extends RhnBaseTestCase {

    @Test
    public void testChecksumType() {
        Map<String, Long> typeIdRelation = Map.of("md5", 1L, "sha1", 2L, "sha256", 3L, "sha384", 4L, "sha512", 5L);

        typeIdRelation.forEach((k, v) -> {
            ChecksumType cType = ChannelFactory.findChecksumTypeByLabel(k);
            assertEquals(v, cType.getId());
        });
    }

    @Test
    public void testGuessChecksumType() {
        assertEquals("md5", ChecksumFactory.guessChecksumTypeByLength("401b30e3b8b5d629635a5c613cdb7919"));
        assertEquals("sha1", ChecksumFactory.guessChecksumTypeByLength("6fcf9dfbd479ed82697fee719b9f8c610a11ff2a"));
        assertEquals("sha256", ChecksumFactory.guessChecksumTypeByLength(
                "73cb3858a687a8494ca3323053016282f3dad39d42cf62ca4e79dda2aac7d9ac"));
        assertEquals("sha384", ChecksumFactory.guessChecksumTypeByLength(
                "bed4e0f8b9c0ec8bee077c2d5ffea39f5b8858458f2694cbe3bd50e137dedb806c76781c53e7cf25dd074855dbbfe3d4"));
        assertEquals("sha512", ChecksumFactory.guessChecksumTypeByLength(
                "45843648ecf9da8e513286f136e3f271e7d6dee4d29b947a50dde8c61f3e197694c13bcdc279ce" +
                        "459839757cd8de19c11b23b33565384a97afcf360483578cd4"));
        assertThrows(IllegalArgumentException.class, () ->
                ChecksumFactory.guessChecksumTypeByLength("401b30e3b8b5d629635a5c613"));
    }
}
