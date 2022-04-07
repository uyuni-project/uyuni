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
package com.redhat.rhn.frontend.action.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.frontend.action.OptionsCollectionBean;

import org.junit.jupiter.api.Test;

/**
 * OptionsCollectionBean
 */
public class OptionsCollectionBeanTest  {

    @Test
    public void testGettersSetters() {
        OptionsCollectionBean ocb = new OptionsCollectionBean("label", "value");
        assertEquals("label", ocb.getLabel());
        assertEquals("value", ocb.getValue());
        ocb.setLabel("changelabel");
        ocb.setValue("changevalue");
        assertEquals("changelabel", ocb.getLabel());
        assertEquals("changevalue", ocb.getValue());
    }
}
