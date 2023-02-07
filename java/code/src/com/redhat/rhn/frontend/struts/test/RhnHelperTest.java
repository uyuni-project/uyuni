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
package com.redhat.rhn.frontend.struts.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.RhnMockDynaActionForm;
import com.redhat.rhn.testing.RhnMockHttpServletRequest;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.DynaActionForm;
import org.junit.jupiter.api.Test;

/**
 * RhnHelperTest - test our RhnHelper class
 */
public class RhnHelperTest extends RhnBaseTestCase {

    @Test
    public void testEmptySelectionError() {
        RhnMockHttpServletRequest request = new RhnMockHttpServletRequest();
        RhnHelper.handleEmptySelection(request);
        assertNotNull(request.getAttribute(Globals.MESSAGE_KEY));
        assertNotNull(request.getSession().getAttribute(Globals.MESSAGE_KEY));
        ActionMessages am = (ActionMessages) request.getAttribute(Globals.MESSAGE_KEY);
        assertEquals(1, am.size());
        ActionMessage key =   new ActionMessage(
                                            RhnHelper.DEFAULT_EMPTY_SELECTION_KEY);
        assertEquals(key.getKey(), ((ActionMessage)am.get().next()).getKey());
    }


    @Test
    public void testGetTextAreaValue() {
        String value = "asdf\r\nasdfwerwer\rasdf\n\radsfhjhhasdf";
        DynaActionForm form = new RhnMockDynaActionForm();
        form.set("somevalue", value);
        String stripped = RhnHelper.getTextAreaValue(form, "somevalue");
        assertNotNull(stripped);
        assertEquals(-1, stripped.indexOf('\r'));
    }

    @Test
    public void testGetParameterWithSpecialCharacters() {
        RhnMockHttpServletRequest request = new RhnMockHttpServletRequest();
        request.setupQueryString("   ");
        assertNull(RhnHelper.getParameterWithSpecialCharacters(request, "zzzz"));

        request.setupQueryString(null);
        assertNull(RhnHelper.getParameterWithSpecialCharacters(request, "zzzz"));

        request.setupQueryString("asdf12354");
        assertNull(RhnHelper.getParameterWithSpecialCharacters(request, "zzzz"));

        request.setupQueryString("foo=bar");
        assertNull(RhnHelper.getParameterWithSpecialCharacters(request, "zzzz"));

        request.setupQueryString("foo=bar");
        assertEquals("bar", RhnHelper.
                getParameterWithSpecialCharacters(request, "foo"));

        request.setupQueryString("foo=bar&baz=bloop&blippy=blorg");
        assertEquals("bar", RhnHelper.
                getParameterWithSpecialCharacters(request, "foo"));

        request.setupQueryString("foo=bar+++&baz=bloop&blippy=blorg");
        assertEquals("bar+++", RhnHelper.
                    getParameterWithSpecialCharacters(request, "foo"));

    }

}

