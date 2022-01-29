/*
 * Copyright (c) 2020--2021 SUSE LLC
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
package com.redhat.rhn.domain.contentmgmt.validation.test;

import static com.redhat.rhn.domain.contentmgmt.validation.ContentValidationMessage.TYPE_INFO;
import static com.redhat.rhn.domain.contentmgmt.validation.ContentValidationMessage.TYPE_WARN;

import com.redhat.rhn.domain.contentmgmt.validation.ModularSourcesValidator;

public class ModularSourcesValidatorTest extends ContentValidatorTestBase {

    private ModularSourcesValidator validator;

    private static final String ENTITY_SOURCES = "softwareSources";
    private final String MSG_NOMODULEFILTERS = getLoc().getMessage("contentmanagement.validation.nomodulefilters");
    private final String MSG_NOMODULARSOURCES = getLoc().getMessage("contentmanagement.validation.nomodularsources");

    @Override
    public void setUp() throws Exception {
        super.setUp();
        validator = new ModularSourcesValidator();
    }

    public void testNoSourcesNoFilters() {
        assertTrue(getProject().getActiveSources().isEmpty());
        assertTrue(getProject().getActiveFilters().isEmpty());

        // Should be valid with no messages
        assertTrue(validator.validate(getProject()).isEmpty());
    }

    public void testNonModularSourcesAndFilters() throws Exception {
        attachSource();
        attachFilter();

        // Should be valid with no messages
        assertTrue(validator.validate(getProject()).isEmpty());
    }

    public void testModularSourcesWithModuleFilters() throws Exception {
        attachModularSource();
        attachModularFilter();

        // Should be valid with no messages
        assertTrue(validator.validate(getProject()).isEmpty());
    }

    public void testModularSourcesWithNoModuleFilters() throws Exception {
        attachModularSource();
        assertSingleMessage(MSG_NOMODULEFILTERS, TYPE_INFO, ENTITY_SOURCES, validator.validate(getProject()));
        attachFilter();
        assertSingleMessage(MSG_NOMODULEFILTERS, TYPE_INFO, ENTITY_SOURCES, validator.validate(getProject()));
    }

    public void testNonModularSourcesWithModuleFilters() throws Exception {
        attachSource();
        attachModularFilter();
        assertSingleMessage(MSG_NOMODULARSOURCES, TYPE_WARN, ENTITY_SOURCES, validator.validate(getProject()));
    }
}
