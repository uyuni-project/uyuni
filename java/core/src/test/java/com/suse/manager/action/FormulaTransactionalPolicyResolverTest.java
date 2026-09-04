/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.formula.FormulaFactory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FormulaTransactionalPolicyResolverTest {

    @TempDir
    private Path formulaMetadataRoot;

    private Path standaloneMetadataDir;
    private Path managerMetadataDir;
    private Path customMetadataDir;
    private String originalStandaloneMetadataDir;
    private String originalManagerMetadataDir;
    private String originalCustomMetadataDir;

    @BeforeEach
    public void setUp() throws Exception {
        originalStandaloneMetadataDir = metadataDir("metadataDirStandaloneSalt");
        originalManagerMetadataDir = metadataDir("metadataDirManager");
        originalCustomMetadataDir = metadataDir("metadataDirCustom");

        standaloneMetadataDir = formulaMetadataRoot.resolve("standalone");
        managerMetadataDir = formulaMetadataRoot.resolve("manager");
        customMetadataDir = formulaMetadataRoot.resolve("custom");
        Files.createDirectories(standaloneMetadataDir);
        Files.createDirectories(managerMetadataDir);
        Files.createDirectories(customMetadataDir);

        FormulaFactory.setMetadataDirStandaloneSalt(standaloneMetadataDir.toString());
        FormulaFactory.setMetadataDirOfficial(managerMetadataDir.toString());
        FormulaFactory.setMetadataDirCustom(customMetadataDir.toString());
    }

    @AfterEach
    public void tearDown() {
        FormulaFactory.setMetadataDirStandaloneSalt(originalStandaloneMetadataDir);
        FormulaFactory.setMetadataDirOfficial(originalManagerMetadataDir);
        FormulaFactory.setMetadataDirCustom(originalCustomMetadataDir);
    }

    @Test
    public void testFormulaOnlyInCustomDirectoryIsRecognizedAsCustom() throws Exception {
        writeMetadata(customMetadataDir, "custom-formula", "{}\n");

        assertEquals(FormulaFactory.MetadataOrigin.CUSTOM, FormulaFactory.getMetadataOrigin("custom-formula"));
        assertTrue(FormulaFactory.isCustomFormula("custom-formula"));
    }

    @Test
    public void testStandaloneFormulaWinsOverCustomFormulaWithSameName() throws Exception {
        writeMetadata(standaloneMetadataDir, "same-name", "{}\n");
        writeMetadata(customMetadataDir, "same-name", "transactional:\n  mode: live\n");

        assertEquals(FormulaFactory.MetadataOrigin.STANDALONE, FormulaFactory.getMetadataOrigin("same-name"));
        assertFalse(FormulaFactory.isCustomFormula("same-name"));
    }

    @Test
    public void testManagerFormulaWinsOverCustomFormulaWithSameName() throws Exception {
        writeMetadata(managerMetadataDir, "same-name", "{}\n");
        writeMetadata(customMetadataDir, "same-name", "transactional:\n  mode: live\n");

        assertEquals(FormulaFactory.MetadataOrigin.MANAGER, FormulaFactory.getMetadataOrigin("same-name"));
        assertFalse(FormulaFactory.isCustomFormula("same-name"));
    }

    @Test
    public void testConfiguredCustomDirectoryIsUsedForCustomDetectionAndMetadata() throws Exception {
        writeMetadata(customMetadataDir, "custom-live", "transactional:\n  mode: live\n");

        FormulaTransactionalPlan plan = FormulaTransactionalPlan.fromFormulas(
                List.of("custom-live"),
                FormulaFactory::getMetadata,
                FormulaFactory::isCustomFormula,
                Map.of());

        assertEquals(List.of(), plan.transactionalFormulas());
        assertEquals(List.of("custom-live"), plan.postTransactionalFormulas());
        assertEquals(List.of(), plan.liveStateIds());
        assertEquals(List.of(), plan.unsupportedFormulas());
    }

    @Test
    public void testMissingCustomDirectoryOrMetadataIsNotClassifiedAsCustom() throws Exception {
        FormulaFactory.setMetadataDirCustom(formulaMetadataRoot.resolve("missing-custom").toString());

        assertEquals(FormulaFactory.MetadataOrigin.NONE, FormulaFactory.getMetadataOrigin("missing-formula"));
        assertFalse(FormulaFactory.isCustomFormula("missing-formula"));
    }

    @Test
    public void testCustomMetadataDoesNotOverrideBuiltInPolicy() {
        Map<String, Object> metadata = Map.of("transactional", Map.of("mode", "disabled"));

        FormulaTransactionalPlan plan = FormulaTransactionalPlan.fromFormulas(
                List.of("locale"),
                formula -> metadata,
                formula -> true,
                FormulaTransactionalPlan.builtInPolicy());

        assertEquals(List.of("locale"), plan.transactionalFormulas());
        assertEquals(List.of("locale"), plan.postTransactionalFormulas());
        assertEquals(List.of(
                "mgr_timezone_setting",
                "mgr_kb_settings",
                "mgr_language_settings"), plan.liveStateIds());
        assertEquals(List.of(), plan.unsupportedFormulas());
    }

    @Test
    public void testCustomTransactionalModeRunsOnlyTransactionally() {
        FormulaTransactionalPlan plan = customPlan("custom-transactional",
                Map.of("transactional", Map.of("mode", "transactional")));

        assertEquals(List.of("custom-transactional"), plan.transactionalFormulas());
        assertEquals(List.of(), plan.postTransactionalFormulas());
        assertEquals(List.of(), plan.liveStateIds());
        assertEquals(List.of(), plan.unsupportedFormulas());
    }

    @Test
    public void testCustomLiveModeRunsOnlyPostTransactionally() {
        FormulaTransactionalPlan plan = customPlan("custom-live", Map.of("transactional", Map.of("mode", "live")));

        assertEquals(List.of(), plan.transactionalFormulas());
        assertEquals(List.of("custom-live"), plan.postTransactionalFormulas());
        assertEquals(List.of(), plan.liveStateIds());
        assertEquals(List.of(), plan.unsupportedFormulas());
    }

    @Test
    public void testCustomTransactionalThenLiveModePreservesLiveStateIdsOrder() {
        FormulaTransactionalPlan plan = customPlan("custom-mixed", Map.of(
                "transactional",
                Map.of(
                        "mode", "transactional_then_live",
                        "live_state_ids", List.of("configure_runtime", "restart_service"))));

        assertEquals(List.of("custom-mixed"), plan.transactionalFormulas());
        assertEquals(List.of("custom-mixed"), plan.postTransactionalFormulas());
        assertEquals(List.of("configure_runtime", "restart_service"), plan.liveStateIds());
        assertEquals(List.of(), plan.unsupportedFormulas());
    }

    @Test
    public void testCustomDisabledModeIsUnsupported() {
        FormulaTransactionalPlan plan = customPlan("custom-disabled",
                Map.of("transactional", Map.of("mode", "disabled")));

        assertEquals(List.of(), plan.transactionalFormulas());
        assertEquals(List.of(), plan.postTransactionalFormulas());
        assertEquals(List.of(), plan.liveStateIds());
        assertEquals(List.of("custom-disabled"), plan.unsupportedFormulas());
    }

    @Test
    public void testCustomFormulaWithoutTransactionalSectionFallsBackToTransactional() {
        FormulaTransactionalPlan plan = customPlan("custom-default", Map.of());

        assertEquals(List.of("custom-default"), plan.transactionalFormulas());
        assertEquals(List.of(), plan.postTransactionalFormulas());
        assertEquals(List.of(), plan.liveStateIds());
        assertEquals(List.of(), plan.unsupportedFormulas());
    }

    @Test
    public void testCustomPillarOnlyFormulaIsIgnoredWithoutValidatingTransactionalSection() {
        FormulaTransactionalPlan plan = customPlan("custom-pillar", Map.of(
                "pillar_only", true,
                "transactional", "invalid"));

        assertEquals(List.of(), plan.transactionalFormulas());
        assertEquals(List.of(), plan.postTransactionalFormulas());
        assertEquals(List.of(), plan.liveStateIds());
        assertEquals(List.of(), plan.unsupportedFormulas());
    }

    @Test
    public void testDirectAndInheritedCustomFormulasAreConsideredAndDeduplicated() {
        FormulaTransactionalPlan plan = FormulaTransactionalPlan.fromAssignedFormulas(
                List.of("custom-live"),
                List.of("custom-live", "custom-default"),
                formula -> switch (formula) {
                    case "custom-live" -> Map.of("transactional", Map.of("mode", "live"));
                    case "custom-default" -> Map.of();
                    default -> Map.of();
                },
                formula -> true,
                Map.of());

        assertEquals(List.of("custom-default"), plan.transactionalFormulas());
        assertEquals(List.of("custom-live"), plan.postTransactionalFormulas());
    }

    @Test
    public void testCustomOrderingUsesFormulaFactoryMetadata() throws Exception {
        writeMetadata(customMetadataDir, "aaa-custom", "after:\n  - zzz-custom\ntransactional:\n  mode: disabled\n");
        writeMetadata(customMetadataDir, "zzz-custom", "transactional:\n  mode: disabled\n");

        FormulaTransactionalPlan plan = FormulaTransactionalPlan.fromFormulas(
                List.of("aaa-custom", "zzz-custom"),
                FormulaFactory::getMetadata,
                FormulaFactory::isCustomFormula,
                Map.of());

        assertEquals(List.of("zzz-custom", "aaa-custom"), plan.unsupportedFormulas());
    }

    @Test
    public void testExplicitCustomFormulaSubsetCanBePlanned() {
        FormulaTransactionalPlan plan = FormulaTransactionalPlan.fromFormulas(
                List.of("selected-custom"),
                formula -> Map.of("transactional", Map.of("mode", "live")),
                formula -> true,
                Map.of());

        assertEquals(List.of(), plan.transactionalFormulas());
        assertEquals(List.of("selected-custom"), plan.postTransactionalFormulas());
    }

    @Test
    public void testCustomPlanListsAndPolicyAreImmutable() {
        List<String> ids = new ArrayList<>(List.of("first", "second"));
        FormulaTransactionalPolicy policy = FormulaTransactionalPolicy.transactionalThenLive(ids);
        ids.add("third");

        assertEquals(List.of("first", "second"), policy.liveStateIds());
        assertThrows(UnsupportedOperationException.class, () -> policy.liveStateIds().add("third"));

        FormulaTransactionalPlan plan = customPlan("custom-mixed", Map.of(
                "transactional",
                Map.of("mode", "transactional_then_live", "live_state_ids", List.of("first", "second"))));

        assertThrows(UnsupportedOperationException.class, () -> plan.transactionalFormulas().add("other"));
        assertThrows(UnsupportedOperationException.class, () -> plan.postTransactionalFormulas().add("other"));
        assertThrows(UnsupportedOperationException.class, () -> plan.liveStateIds().add("other"));
        assertThrows(UnsupportedOperationException.class, () -> plan.unsupportedFormulas().add("other"));
    }

    @Test
    public void testInvalidTransactionalSectionMustBeAMap() {
        assertInvalid("transactional", Map.of("transactional", "invalid"));
    }

    @Test
    public void testInvalidTransactionalModeIsRequired() {
        assertInvalid("mode", Map.of("transactional", Map.of()));
    }

    @Test
    public void testInvalidTransactionalModeMustBeString() {
        assertInvalid("mode", Map.of("transactional", Map.of("mode", 1)));
    }

    @Test
    public void testInvalidTransactionalModeRejectsUnknownAndUppercaseValues() {
        assertInvalid("mode", Map.of("transactional", Map.of("mode", "LIVE")));
    }

    @Test
    public void testInvalidLiveStateIdsMustBeAList() {
        assertInvalid("live_state_ids", Map.of(
                "transactional",
                Map.of("mode", "transactional_then_live", "live_state_ids", "configure_runtime")));
    }

    @Test
    public void testInvalidLiveStateIdsMustContainOnlyStrings() {
        assertInvalid("live_state_ids", Map.of(
                "transactional",
                Map.of("mode", "transactional_then_live", "live_state_ids", List.of(1))));
    }

    @Test
    public void testInvalidLiveStateIdsRejectsBlankIds() {
        assertInvalid("live_state_ids", Map.of(
                "transactional",
                Map.of("mode", "transactional_then_live", "live_state_ids", List.of(" "))));
    }

    @Test
    public void testInvalidLiveStateIdsRejectsDuplicates() {
        assertInvalid("live_state_ids", Map.of(
                "transactional",
                Map.of("mode", "transactional_then_live", "live_state_ids", List.of("same", "same"))));
    }

    @Test
    public void testInvalidTransactionalThenLiveRequiresIds() {
        assertInvalid("live_state_ids", Map.of("transactional", Map.of("mode", "transactional_then_live")));
    }

    @Test
    public void testInvalidLiveStateIdsRejectedForIncompatibleModes() {
        assertInvalid("live_state_ids", Map.of(
                "transactional",
                Map.of("mode", "live", "live_state_ids", List.of("restart_service"))));
    }

    @Test
    public void testInvalidTransactionalSectionRejectsUnknownKeys() {
        assertInvalid("unknown_field", Map.of("transactional", Map.of("mode", "live", "unknown_field", true)));
    }

    @Test
    public void testFormulaTransactionalPolicyRejectsInvalidLiveStateIds() {
        assertThrows(IllegalArgumentException.class, () ->
                FormulaTransactionalPolicy.transactionalThenLive(List.of("same", "same")));
        assertThrows(IllegalArgumentException.class, () ->
                FormulaTransactionalPolicy.transactionalThenLive(List.of(" ")));
    }

    private FormulaTransactionalPlan customPlan(String formula, Map<String, Object> metadata) {
        return FormulaTransactionalPlan.fromFormulas(
                List.of(formula),
                ignored -> metadata,
                ignored -> true,
                Map.of());
    }

    private void assertInvalid(String field, Map<String, Object> metadata) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> customPlan("bad-formula", metadata));

        assertTrue(exception.getMessage().contains("bad-formula"));
        assertTrue(exception.getMessage().contains("field '" + field + "'"));
    }

    private static void writeMetadata(Path metadataDir, String formula, String metadata) throws Exception {
        Path formulaDir = metadataDir.resolve(formula);
        Files.createDirectories(formulaDir);
        Files.writeString(formulaDir.resolve("metadata.yml"), metadata);
    }

    private static String metadataDir(String fieldName) throws Exception {
        Field field = FormulaFactory.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (String) field.get(null);
    }
}
