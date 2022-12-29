/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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

import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.struts.StrutsDelegate;
import com.redhat.rhn.testing.RhnMockDynaActionForm;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.DynaActionForm;
import org.junit.jupiter.api.Test;

/**
 * StrutsDelegateImplTest
 */
public class StrutsDelegateTest  {

    private class StrutsDelegateStub extends StrutsDelegate {
    }

    /**
     *
     */
    @Test
    public final void testForwardParams() {
        ActionForward success = new ActionForward(RhnHelper.DEFAULT_FORWARD, "path", false);

        StrutsDelegate strutsDelegate = new StrutsDelegateStub();

        ActionForward fwdWithParams = strutsDelegate.forwardParam(success, "foo", "bar");
        assertEquals(success.getName(), fwdWithParams.getName());
        assertEquals(fwdWithParams.getPath(), "path?foo=bar");
    }

    /**
     *
     */
    @Test
    public void testGetTextAreaValue() {
        String value = "asdf\r\nasdfwerwer\rasdf\n\radsfhjhhasdf";
        DynaActionForm form = new RhnMockDynaActionForm();
        form.set("somevalue", value);

        StrutsDelegate strutsDelegate = new StrutsDelegateStub();

        String stripped = strutsDelegate.getTextAreaValue(form, "somevalue");
        assertNotNull(stripped);
        assertEquals(-1, stripped.indexOf('\r'));
    }

}
