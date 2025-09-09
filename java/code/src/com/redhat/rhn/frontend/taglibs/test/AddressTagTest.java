/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.taglibs.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.domain.user.Address;
import com.redhat.rhn.frontend.action.user.AddressesAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.taglibs.AddressTag;
import com.redhat.rhn.testing.ActionHelper;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.RhnMockJspWriter;
import com.redhat.rhn.testing.TagTestHelper;
import com.redhat.rhn.testing.TagTestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.jsp.tagext.Tag;

/**
 * AddressMenuTagTest
 */
public class AddressTagTest extends RhnBaseTestCase {

    private ActionHelper sah;

    /**
     * Called once per test method.
     * @throws Exception if an error occurs during setup.
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        sah = new ActionHelper();
        sah.setUpAction(new AddressesAction());
        sah.getRequest().setRequestURL("foo");
        sah.executeAction();
    }

    /**
     * Test tag output
     * @throws Exception something bad happened
     */
    @Test
    public void testTagOutput() throws Exception {

        AddressTag addtg = new AddressTag();
        TagTestHelper tth = TagTestUtils.setupTagTest(addtg, null, sah.getRequest());

        // setup mock objects
        RhnMockJspWriter out = (RhnMockJspWriter)tth.getPageContext().getOut();
        String expectedData = getPopulatedReturnValue(sah.getUser().getId());
        addtg.setType(Address.TYPE_MARKETING);
        addtg.setUser(sah.getUser());
        addtg.setAddress(
                (Address) sah.getRequest().getAttribute(RhnHelper.TARGET_ADDRESS_MARKETING));

        // ok let's test the tag
        tth.assertDoStartTag(Tag.SKIP_BODY);
        assertEquals(expectedData, out.toString());
    }

    /* Test rendering an empty Address
     */
    @Test
    public void testEmptyAddress() throws Exception {
        AddressTag addtg = new AddressTag();
        TagTestHelper tth = TagTestUtils.setupTagTest(addtg, null, sah.getRequest());
        // setup mock objects
        RhnMockJspWriter out = (RhnMockJspWriter)tth.getPageContext().getOut();
        String expectedData = getEmptyReturnValue(sah.getUser().getId());

        // The test User in the super class shouldn't have
        // a SHIPPING address
        addtg.setType(Address.TYPE_MARKETING);
        addtg.setUser(sah.getUser());
        // ok let's test the tag
        tth.assertDoStartTag(Tag.SKIP_BODY);
        assertEquals(expectedData, out.toString());
    }

    private String getPopulatedReturnValue(Long uid) {
        return "<strong>Mailing Address</strong>" +
                "<address>444 Castro<br />" +
                "#1<br />" +
                "Mountain View, CA 94043<br />" +
                "Phone: 650-555-1212<br />" +
                "Fax: 650-555-1212<br />" +
                "</address>" +
                "<a class=\"btn btn-default\" href=\"/EditAddress.do?type=M&amp;uid=" +
                uid + "\">" + "Edit</a>";
    }

    private String getEmptyReturnValue(Long uid) {
        return "<strong>Mailing Address</strong>" +
                "<div class=\"alert alert-info\">Address not filled in</div>" +
                "<a class=\"btn btn-default\" href=\"/EditAddress.do?type=M&amp;uid=" +
                uid + "\">Add address</a>";
    }

}
