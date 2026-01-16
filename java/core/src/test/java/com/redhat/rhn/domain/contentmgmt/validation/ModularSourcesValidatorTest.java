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
package com.redhat.rhn.domain.contentmgmt.validation;

import static com.redhat.rhn.domain.contentmgmt.validation.ContentValidationMessage.TYPE_INFO;
import static com.redhat.rhn.domain.contentmgmt.validation.ContentValidationMessage.TYPE_WARN;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ModularSourcesValidatorTest extends ContentValidatorTestBase {

    private ModularSourcesValidator validator;

    private static final String ENTITY_SOURCES = "softwareSources";
    private final String msgNoModuleFilters = getLoc().getMessage("contentmanagement.validation.nomodulefilters");
    private final String msgNoModularSources = getLoc().getMessage("contentmanagement.validation.nomodularsources");

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        validator = new ModularSourcesValidator();
    }

    @Test
    public void testNoSourcesNoFilters() {
        assertTrue(getProject().getActiveSources().isEmpty());
        assertTrue(getProject().getActiveFilters().isEmpty());

        // Should be valid with no messages
        assertTrue(validator.validate(getProject()).isEmpty());
    }

    @Test
    public void testNonModularSourcesAndFilters() throws Exception {
        attachSource();
        attachFilter();

        // Should be valid with no messages
        assertTrue(validator.validate(getProject()).isEmpty());
    }

    @Test
    public void testModularSourcesWithModuleFilters() throws Exception {
        attachModularSource();
        attachModularFilter();

        // Should be valid with no messages
        assertTrue(validator.validate(getProject()).isEmpty());
    }

    @Test
    public void testModularSourcesWithNoModuleFilters() throws Exception {
        attachModularSource();
        assertSingleMessage(msgNoModuleFilters, TYPE_INFO, ENTITY_SOURCES, validator.validate(getProject()));
        attachFilter();
        assertSingleMessage(msgNoModuleFilters, TYPE_INFO, ENTITY_SOURCES, validator.validate(getProject()));
    }

    @Test
    public void testNonModularSourcesWithModuleFilters() throws Exception {
        attachSource();
        attachModularFilter();
        assertSingleMessage(msgNoModularSources, TYPE_WARN, ENTITY_SOURCES, validator.validate(getProject()));
    }
}
