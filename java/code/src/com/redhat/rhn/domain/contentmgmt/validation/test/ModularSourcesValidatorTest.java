package com.redhat.rhn.domain.contentmgmt.validation.test;

import static com.redhat.rhn.domain.contentmgmt.validation.ContentValidationMessage.TYPE_INFO;
import static com.redhat.rhn.domain.contentmgmt.validation.ContentValidationMessage.TYPE_WARN;
import static org.junit.jupiter.api.Assertions.*;

import com.redhat.rhn.domain.contentmgmt.validation.ModularSourcesValidator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ModularSourcesValidatorTest extends ContentValidatorTestBase {

    private ModularSourcesValidator validator;

    private static final String ENTITY_SOURCES = "softwareSources";
    private final String MSG_NOMODULEFILTERS = loc.getMessage("contentmanagement.validation.nomodulefilters");
    private final String MSG_NOMODULARSOURCES = loc.getMessage("contentmanagement.validation.nomodularsources");

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        validator = new ModularSourcesValidator();
    }

    @Test
    public void testNoSourcesNoFilters() {
        assertTrue(project.getActiveSources().isEmpty());
        assertTrue(project.getActiveFilters().isEmpty());

        // Should be valid with no messages
        assertTrue(validator.validate(project).isEmpty());
    }

    @Test
    public void testNonModularSourcesAndFilters() throws Exception {
        attachSource();
        attachFilter();

        // Should be valid with no messages
        assertTrue(validator.validate(project).isEmpty());
    }

    @Test
    public void testModularSourcesWithModuleFilters() throws Exception {
        attachModularSource();
        attachModularFilter();

        // Should be valid with no messages
        assertTrue(validator.validate(project).isEmpty());
    }

    @Test
    public void testModularSourcesWithNoModuleFilters() throws Exception {
        attachModularSource();
        assertSingleMessage(MSG_NOMODULEFILTERS, TYPE_INFO, ENTITY_SOURCES, validator.validate(project));
        attachFilter();
        assertSingleMessage(MSG_NOMODULEFILTERS, TYPE_INFO, ENTITY_SOURCES, validator.validate(project));
    }

    @Test
    public void testNonModularSourcesWithModuleFilters() throws Exception {
        attachSource();
        attachModularFilter();
        assertSingleMessage(MSG_NOMODULARSOURCES, TYPE_WARN, ENTITY_SOURCES, validator.validate(project));
    }
}
