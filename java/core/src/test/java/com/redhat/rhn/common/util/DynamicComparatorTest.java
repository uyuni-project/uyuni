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
package com.redhat.rhn.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.validator.TestObject;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.testing.RhnJmockBaseTestCase;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DynamicComparatorTest extends RhnJmockBaseTestCase {

    @Test
    public void testComparatorMaps() {
        List<TestObject> list = generateRandomList();
        DynamicComparator<TestObject> comp = new DynamicComparator<>("stringField",
                RequestContext.SORT_ASC);
        list.sort(comp);
        assertEquals("A", list.get(0).getStringField());
        assertEquals("Z", list.get(list.size() - 1).getStringField());
    }

    public static List<TestObject> generateRandomList() {
        List<String> letters = LocalizationService.getInstance().getAlphabet();
        Collections.shuffle(letters);
        return letters.stream().map(letter -> {
            TestObject to = new TestObject();
            to.setStringField(letter);
            return to;
        }).collect(Collectors.toList());
    }
}
