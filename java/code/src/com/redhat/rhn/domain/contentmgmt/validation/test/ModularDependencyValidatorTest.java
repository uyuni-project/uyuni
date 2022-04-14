/*
 * Copyright (c) 2020 SUSE LLC
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

import static com.redhat.rhn.domain.contentmgmt.validation.ContentValidationMessage.TYPE_ERROR;
import static com.redhat.rhn.domain.contentmgmt.validation.ContentValidationMessage.TYPE_INFO;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.contentmgmt.validation.ModularDependencyValidator;
import com.redhat.rhn.manager.contentmgmt.test.MockModulemdApi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ModularDependencyValidatorTest extends ContentValidatorTestBase {

    private ModularDependencyValidator validator;

    private static final String ENTITY_FILTERS = "filters";

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        validator = new ModularDependencyValidator(new MockModulemdApi());
    }

    @Test
    public void testNonModularSources() throws Exception {
        attachSource();
        assertTrue(validator.validate(getProject()).isEmpty());
        attachModularFilter();
        assertTrue(validator.validate(getProject()).isEmpty());
    }

    @Test
    public void testNoModuleFilters() throws Exception {
        attachModularSource();
        assertTrue(validator.validate(getProject()).isEmpty());
        attachFilter();
        assertTrue(validator.validate(getProject()).isEmpty());
    }

    @Test
    public void testMatchingFilter() throws Exception {
        attachModularSource();
        attachModularFilter();
        // There should be no ERR/WARN messages
        assertTrue(validator.validate(getProject()).stream().allMatch(m -> TYPE_INFO.equals(m.getType())));
    }

    @Test
    public void testNonMatchingFilter() throws Exception {
        attachModularSource();
        attachModularFilter("nonexistent:stream");
        assertSingleMessage(getLoc().getMessage("contentmanagement.validation.modulenotfound", "nonexistent:stream"),
                TYPE_ERROR, ENTITY_FILTERS, validator.validate(getProject()));
    }

    @Test
    public void testConflictingFilters() throws Exception {
        attachModularSource();
        attachModularFilter("postgresql:10");
        attachModularFilter("postgresql:12");
        assertSingleMessage(getLoc().getMessage("contentmanagement.validation.moduleconflict", "postgresql:10",
                "postgresql:12"), TYPE_ERROR, ENTITY_FILTERS, validator.validate(getProject()));
    }
}
