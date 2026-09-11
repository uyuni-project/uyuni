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
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.redhat.rhn.domain.formula.FormulaFactory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FormulaTransactionalPlanTest {

    private static final List<String> LOCALE_LIVE_STATE_IDS = List.of(
            "mgr_timezone_setting",
            "mgr_kb_settings",
            "mgr_language_settings");
    private static final List<String> PXE_LIVE_STATE_IDS = List.of(
            "srv_tftpboot_default",
            "srv_tftpboot_default_efi",
            "srv_tftpboot_base_efi",
            "pxe_copy",
            "pxe_copy_grub_efi",
            "pxe_copy_shim_efi",
            "pxe_copy_efi_dir",
            "pxe_copy_grub_arm64_efi",
            "pxe_copy_arm64_efi_dir",
            "srv_tftpboot_default_arm64_efi");

    @TempDir
    private Path formulaMetadataDir;

    private String originalMetadataDir;

    @BeforeEach
    public void setUp() throws Exception {
        originalMetadataDir = metadataDirManager();
        FormulaFactory.setMetadataDirOfficial(formulaMetadataDir + "/");
    }

    @AfterEach
    public void tearDown() throws Exception {
        setMetadataDirManager(originalMetadataDir);
    }

    @Test
    public void testEmptyPlanWhenNoFormulaIsAssigned() {
        FormulaTransactionalPlan plan = plan(List.of(), List.of());

        assertEquals(List.of(), plan.transactionalFormulas());
        assertEquals(List.of(), plan.postTransactionalFormulas());
        assertEquals(List.of(), plan.liveStateIds());
        assertEquals(List.of(), plan.unsupportedFormulas());
    }

    @Test
    public void testLocaleRunsTransactionalThenLive() {
        FormulaTransactionalPlan plan = plan(List.of("locale"), List.of());

        assertEquals(List.of("locale"), plan.transactionalFormulas());
        assertEquals(List.of("locale"), plan.postTransactionalFormulas());
        assertEquals(LOCALE_LIVE_STATE_IDS, plan.liveStateIds());
        assertEquals(List.of(), plan.unsupportedFormulas());
    }

    @Test
    public void testPxeRunsTransactionalThenLiveWithExactLiveStateIds() {
        FormulaTransactionalPlan plan = plan(List.of("pxe"), List.of());

        assertEquals(List.of("pxe"), plan.transactionalFormulas());
        assertEquals(List.of("pxe"), plan.postTransactionalFormulas());
        assertEquals(PXE_LIVE_STATE_IDS, plan.liveStateIds());
        assertEquals(List.of(), plan.unsupportedFormulas());
    }

    @Test
    public void testTftpdRunsTransactionalThenLiveWithServiceLiveState() {
        FormulaTransactionalPlan plan = plan(List.of("tftpd"), List.of());

        assertEquals(List.of("tftpd"), plan.transactionalFormulas());
        assertEquals(List.of("tftpd"), plan.postTransactionalFormulas());
        assertEquals(List.of("enable_and_start_tftpd"), plan.liveStateIds());
        assertEquals(List.of(), plan.unsupportedFormulas());
    }

    @Test
    public void testTransactionalFormulasRunOnlyTransactionally() {
        FormulaTransactionalPlan plan = plan(List.of("bind", "dhcpd", "branch-network"), List.of());

        assertEquals(3, plan.transactionalFormulas().size());
        assertEquals(Set.of("branch-network", "bind", "dhcpd"), Set.copyOf(plan.transactionalFormulas()));
        assertEquals(List.of(), plan.postTransactionalFormulas());
        assertEquals(List.of(), plan.liveStateIds());
        assertEquals(List.of(), plan.unsupportedFormulas());
    }

    @Test
    public void testLiveFormulaRunsOnlyPostTransactionally() {
        FormulaTransactionalPlan plan = FormulaTransactionalPlan.fromFormulas(
                List.of("live-formula"),
                formula -> Map.of(),
                Map.of("live-formula", FormulaTransactionalPolicy.live()));

        assertEquals(List.of(), plan.transactionalFormulas());
        assertEquals(List.of("live-formula"), plan.postTransactionalFormulas());
        assertEquals(List.of(), plan.liveStateIds());
        assertEquals(List.of(), plan.unsupportedFormulas());
    }

    @Test
    public void testUnsupportedFormulaIsReported() {
        FormulaTransactionalPlan plan = plan(List.of("vsftpd"), List.of());

        assertEquals(List.of(), plan.transactionalFormulas());
        assertEquals(List.of(), plan.postTransactionalFormulas());
        assertEquals(List.of(), plan.liveStateIds());
        assertEquals(List.of("vsftpd"), plan.unsupportedFormulas());
    }

    @Test
    public void testFormulaWithoutPolicyFallsBackToTransactional() {
        FormulaTransactionalPlan plan = plan(List.of("openvpn"), List.of());

        assertEquals(List.of("openvpn"), plan.transactionalFormulas());
        assertEquals(List.of(), plan.postTransactionalFormulas());
        assertEquals(List.of(), plan.liveStateIds());
        assertEquals(List.of(), plan.unsupportedFormulas());
    }

    @Test
    public void testMixedFormulaModesAreSeparated() {
        FormulaTransactionalPlan plan = FormulaTransactionalPlan.fromFormulas(
                List.of("locale", "bind", "live-formula", "vsftpd", "openvpn"),
                formula -> Map.of(),
                Map.of(
                        "locale", FormulaTransactionalPolicy.transactionalThenLive(LOCALE_LIVE_STATE_IDS),
                        "bind", FormulaTransactionalPolicy.transactional(),
                        "live-formula", FormulaTransactionalPolicy.live(),
                        "vsftpd", FormulaTransactionalPolicy.unsupported()));

        assertEquals(List.of("bind", "locale", "openvpn"), plan.transactionalFormulas());
        assertEquals(List.of("live-formula", "locale"), plan.postTransactionalFormulas());
        assertEquals(LOCALE_LIVE_STATE_IDS, plan.liveStateIds());
        assertEquals(List.of("vsftpd"), plan.unsupportedFormulas());
    }

    @Test
    public void testDirectAndInheritedFormulasAreConsidered() {
        FormulaTransactionalPlan plan = plan(List.of("locale"), List.of("bind"));

        assertEquals(List.of("bind", "locale"), plan.transactionalFormulas());
        assertEquals(List.of("locale"), plan.postTransactionalFormulas());
        assertEquals(LOCALE_LIVE_STATE_IDS, plan.liveStateIds());
        assertEquals(List.of(), plan.unsupportedFormulas());
    }

    @Test
    public void testDuplicateDirectAndInheritedFormulaIsRemoved() {
        FormulaTransactionalPlan plan = plan(List.of("locale"), List.of("locale"));

        assertEquals(List.of("locale"), plan.transactionalFormulas());
        assertEquals(List.of("locale"), plan.postTransactionalFormulas());
        assertEquals(LOCALE_LIVE_STATE_IDS, plan.liveStateIds());
        assertEquals(List.of(), plan.unsupportedFormulas());
    }

    @Test
    public void testPillarOnlyFormulaIsIgnored() {
        FormulaTransactionalPlan plan = FormulaTransactionalPlan.fromAssignedFormulas(
                List.of("pillar-formula"),
                List.of(),
                formula -> Map.of("pillar_only", true));

        assertEquals(List.of(), plan.transactionalFormulas());
        assertEquals(List.of(), plan.postTransactionalFormulas());
        assertEquals(List.of(), plan.liveStateIds());
        assertEquals(List.of(), plan.unsupportedFormulas());
    }

    @Test
    public void testOrderUsesAfterMetadata() throws IOException {
        writeFormulaMetadata("aaa-formula", "after:\n  - zzz-formula\n");
        writeFormulaMetadata("zzz-formula", "");

        FormulaTransactionalPlan plan = FormulaTransactionalPlan.fromAssignedFormulas(
                List.of("aaa-formula", "zzz-formula"),
                List.of(),
                formula -> Map.of(),
                Map.of(
                        "aaa-formula", FormulaTransactionalPolicy.unsupported(),
                        "zzz-formula", FormulaTransactionalPolicy.unsupported()));

        assertEquals(List.of("zzz-formula", "aaa-formula"), plan.unsupportedFormulas());
    }

    @Test
    public void testCircularDependencyDoesNotDropFormulasFromPlan() throws IOException {
        writeFormulaMetadata("aaa-formula", "after:\n  - zzz-formula\n");
        writeFormulaMetadata("zzz-formula", "after:\n  - aaa-formula\n");

        FormulaTransactionalPlan plan = FormulaTransactionalPlan.fromAssignedFormulas(
                List.of("aaa-formula", "zzz-formula"),
                List.of(),
                formula -> Map.of(),
                Map.of(
                        "aaa-formula", FormulaTransactionalPolicy.unsupported(),
                        "zzz-formula", FormulaTransactionalPolicy.unsupported()));

        assertEquals(2, plan.unsupportedFormulas().size());
        assertEquals(Set.of("aaa-formula", "zzz-formula"), Set.copyOf(plan.unsupportedFormulas()));
    }

    @Test
    public void testPlanListsAreImmutable() {
        FormulaTransactionalPlan plan = plan(List.of("locale"), List.of());

        assertThrows(UnsupportedOperationException.class, () -> plan.transactionalFormulas().add("bind"));
        assertThrows(UnsupportedOperationException.class, () -> plan.postTransactionalFormulas().add("bind"));
        assertThrows(UnsupportedOperationException.class, () -> plan.liveStateIds().add("other"));
        assertThrows(UnsupportedOperationException.class, () -> plan.unsupportedFormulas().add("bind"));
    }

    @Test
    public void testPolicyRejectsLiveStateIdsForModesThatDoNotUseThem() {
        assertThrows(IllegalArgumentException.class, () ->
                new FormulaTransactionalPolicy(FormulaTransactionalMode.TRANSACTIONAL, List.of("some-id")));
        assertThrows(IllegalArgumentException.class, () ->
                new FormulaTransactionalPolicy(FormulaTransactionalMode.LIVE, List.of("some-id")));
        assertThrows(IllegalArgumentException.class, () ->
                new FormulaTransactionalPolicy(FormulaTransactionalMode.UNSUPPORTED, List.of("some-id")));
    }

    @Test
    public void testPolicyRejectsTransactionalThenLiveWithoutLiveStateIds() {
        assertThrows(IllegalArgumentException.class, () ->
                new FormulaTransactionalPolicy(FormulaTransactionalMode.TRANSACTIONAL_THEN_LIVE, List.of()));
    }

    @Test
    public void testExplicitFormulaSubsetCanBePlanned() {
        FormulaTransactionalPlan plan = FormulaTransactionalPlan.fromFormulas(
                List.of("locale", "bind"),
                formula -> Map.of(),
                FormulaTransactionalPlan.builtInPolicy());

        assertEquals(List.of("bind", "locale"), plan.transactionalFormulas());
        assertEquals(List.of("locale"), plan.postTransactionalFormulas());
        assertEquals(LOCALE_LIVE_STATE_IDS, plan.liveStateIds());
    }

    private static FormulaTransactionalPlan plan(List<String> directFormulas, List<String> inheritedFormulas) {
        return FormulaTransactionalPlan.fromAssignedFormulas(directFormulas, inheritedFormulas, formula -> Map.of());
    }

    private void writeFormulaMetadata(String formula, String metadata) throws IOException {
        Path formulaDir = formulaMetadataDir.resolve(formula);
        Files.createDirectories(formulaDir);
        Files.writeString(formulaDir.resolve("metadata.yml"), metadata.isEmpty() ? "{}\n" : metadata);
    }

    private static String metadataDirManager() throws Exception {
        Field field = metadataDirManagerField();
        return (String) field.get(null);
    }

    private static void setMetadataDirManager(String metadataDir) throws Exception {
        Field field = metadataDirManagerField();
        field.set(null, metadataDir);
    }

    private static Field metadataDirManagerField() throws Exception {
        Field field = FormulaFactory.class.getDeclaredField("metadataDirManager");
        field.setAccessible(true);
        return field;
    }
}
