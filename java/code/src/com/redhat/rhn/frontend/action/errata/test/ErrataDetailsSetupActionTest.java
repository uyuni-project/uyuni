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
package com.redhat.rhn.frontend.action.errata.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.rhn.domain.errata.AdvisoryStatus;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.errata.test.ErrataFactoryTest;
import com.redhat.rhn.frontend.action.errata.ErrataDetailsSetupAction;
import com.redhat.rhn.frontend.context.Context;
import com.redhat.rhn.testing.ActionHelper;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * ErrataDetailsSetupActionTest
 */
public class ErrataDetailsSetupActionTest extends RhnBaseTestCase {

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        // Set the locale explicitly to check the dates in an known format
        Context.getCurrentContext().setLocale(Locale.US);
    }

    @Test
    public void testExecute() throws Exception {
        ErrataDetailsSetupAction action = new ErrataDetailsSetupAction();
        ActionHelper sah = new ActionHelper();
        sah.setUpAction(action);

        Errata errata = ErrataFactoryTest.createTestErrata(sah.getUser().getOrg().getId());

        errata.setAdvisory("SUSE-15-SP3-2021-3413");
        errata.setAdvisoryName("SUSE-15-SP3-2021-3413");
        errata.setAdvisoryRel(1L);
        errata.setAdvisoryType(ErrataFactory.ERRATA_TYPE_BUG);
        errata.setAdvisoryStatus(AdvisoryStatus.STABLE);
        errata.setProduct("SUSE Updates SLE-Module-Basesystem 15-SP3 x86 64");
        errata.setDescription("This update for suse-module-tools fixes bugs.");
        errata.setSynopsis("Recommended update for suse-module-tools");
        errata.setIssueDate(new GregorianCalendar(2021, Calendar.OCTOBER, 13).getTime());
        errata.setUpdateDate(new GregorianCalendar(2021, Calendar.OCTOBER, 13).getTime());
        errata.setErrataFrom("maint-coord@suse.de");
        errata.addKeyword("test");

        ErrataFactory.save(errata);

        sah.getRequest().setupAddParameter("eid", errata.getId().toString());

        sah.executeAction();

        assertEquals(errata, sah.getRequest().getAttribute("errata"));
        assertEquals("10/13/21", sah.getRequest().getAttribute("issued"));
        assertEquals("10/13/21", sah.getRequest().getAttribute("updated"));
        assertEquals("This update for suse-module-tools fixes bugs.", sah.getRequest().getAttribute("description"));
        assertEquals("Stable", sah.getRequest().getAttribute("advisoryStatus"));


        String advisoryLink = "<a target=\"_blank\" " +
                "href=\"https://www.suse.com/support/update/announcement/2021/suse-ru-20213413-1/\">" +
                "SUSE-RU-2021:3413-1</a>";
        assertEquals(advisoryLink, sah.getRequest().getAttribute("vendorAdvisory"));

        assertEquals("test topic", sah.getRequest().getAttribute("topic"));
        assertEquals("Test solution", sah.getRequest().getAttribute("solution"));
        assertEquals("Test notes for test errata", sah.getRequest().getAttribute("notes"));
        assertEquals("rhn unit tests", sah.getRequest().getAttribute("references"));

        assertNotNull(sah.getRequest().getAttribute("channels"));
        assertNotNull(sah.getRequest().getAttribute("fixed"));
        assertNotNull(sah.getRequest().getAttribute("cve"));

        assertEquals("keyword, test", sah.getRequest().getAttribute("keywords"));
    }
}
