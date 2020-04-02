/**
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

import com.redhat.rhn.domain.contentmgmt.validation.ModularDependencyValidator;
import com.redhat.rhn.manager.contentmgmt.test.MockModulemdApi;

import static com.redhat.rhn.domain.contentmgmt.validation.ContentValidationMessage.TYPE_ERROR;

public class ModularDependencyValidatorTest extends ContentValidatorTestBase {

    private ModularDependencyValidator validator;

    private static final String ENTITY_FILTERS = "filters";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        validator = new ModularDependencyValidator(new MockModulemdApi());
    }

    public void testNonModularSources() throws Exception {
        attachSource();
        assertTrue(validator.validate(project).isEmpty());
        attachModularFilter();
        assertTrue(validator.validate(project).isEmpty());
    }

    public void testNoModuleFilters() throws Exception {
        attachModularSource();
        assertTrue(validator.validate(project).isEmpty());
        attachFilter();
        assertTrue(validator.validate(project).isEmpty());
    }

    public void testMatchingFilter() throws Exception {
        attachModularSource();
        attachModularFilter();
        assertTrue(validator.validate(project).isEmpty());
    }

    public void testNonMatchingFilter() throws Exception {
        attachModularSource();
        attachModularFilter("nonexistent:stream");
        assertSingleMessage(loc.getMessage("contentmanagement.validation.modulenotfound", "nonexistent:stream"),
                TYPE_ERROR, ENTITY_FILTERS, validator.validate(project));
    }

    public void testConflictingFilters() throws Exception {
        attachModularSource();
        attachModularFilter("postgresql:10");
        attachModularFilter("postgresql:12");
        assertSingleMessage(loc.getMessage("contentmanagement.validation.moduleconflict", "postgresql:10",
                "postgresql:12"), TYPE_ERROR, ENTITY_FILTERS, validator.validate(project));
    }
}
