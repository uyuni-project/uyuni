/*
 * Copyright (c) 2026 SUSE LLC
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

package com.suse.oval.vulnerablepkgextractor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.rhnpackage.PackageEvr;

import com.suse.oval.OsFamily;
import com.suse.oval.ovaltypes.CriteriaType;
import com.suse.oval.ovaltypes.CriterionType;
import com.suse.oval.ovaltypes.DefinitionClassEnum;
import com.suse.oval.ovaltypes.DefinitionType;

import org.junit.jupiter.api.Test;

import java.util.List;

public class UbuntuVulnerablePackageExtractorTest {

    private DefinitionType createUbuntuDefinition(String osVersion, String cve, String criterionComment) {
        DefinitionType definition = new DefinitionType();
        definition.setOsFamily(OsFamily.UBUNTU);
        definition.setOsVersion(osVersion);
        definition.setDefinitionClass(DefinitionClassEnum.VULNERABILITY);
        definition.setSingleCve(cve);

        CriteriaType criteriaRoot = new CriteriaType();
        CriterionType criterion = new CriterionType();
        criterion.setComment(criterionComment);
        criteriaRoot.getChildren().add(criterion);
        definition.setCriteria(criteriaRoot);

        return definition;
    }

    @Test
    public void testExtractCurrentUbuntuAffectedPackage() {
        DefinitionType definition = createUbuntuDefinition("24.04", "CVE-2024-1234",
                "gcc-arm-none-eabi source package in noble, might be affected and may need fixing.");

        UbuntuVulnerablePackageExtractor extractor = new UbuntuVulnerablePackageExtractor(definition);
        List<ProductVulnerablePackages> result = extractor.extract();

        assertEquals(1, result.size());
        ProductVulnerablePackages pvp = result.get(0);
        assertEquals("cpe:/o:canonical:ubuntu_linux:24.04", pvp.getProductCpe());
        assertEquals("CVE-2024-1234", pvp.getCves().get(0));
        assertEquals(1, pvp.getVulnerablePackages().size());

        VulnerablePackage vp = pvp.getVulnerablePackages().get(0);
        assertEquals("gcc-arm-none-eabi", vp.getName());
        assertTrue(vp.getFixVersion().isEmpty());
    }

    @Test
    public void testExtractCurrentUbuntuEsmAffectedPackage() {
        DefinitionType definition = createUbuntuDefinition("24.04", "CVE-2024-5678",
                "swish-e source package in esm-apps/noble, might be affected and may need fixing.");

        UbuntuVulnerablePackageExtractor extractor = new UbuntuVulnerablePackageExtractor(definition);
        List<ProductVulnerablePackages> result = extractor.extract();

        assertEquals(1, result.size());
        VulnerablePackage vp = result.get(0).getVulnerablePackages().get(0);
        assertEquals("swish-e", vp.getName());
        assertTrue(vp.getFixVersion().isEmpty());
    }

    @Test
    public void testExtractCurrentUbuntuPatchedPackage() {
        DefinitionType definition = createUbuntuDefinition("24.04", "CVE-2006-10003",
                "libxml-parser-perl source package in noble, is affected and has been fixed " +
                        "(note: '2.47-1ubuntu0.24.04.1').");

        UbuntuVulnerablePackageExtractor extractor = new UbuntuVulnerablePackageExtractor(definition);
        List<ProductVulnerablePackages> result = extractor.extract();

        assertEquals(1, result.size());
        ProductVulnerablePackages pvp = result.get(0);
        assertEquals("cpe:/o:canonical:ubuntu_linux:24.04", pvp.getProductCpe());
        assertEquals("CVE-2006-10003", pvp.getCves().get(0));

        VulnerablePackage vp = pvp.getVulnerablePackages().get(0);
        assertEquals("libxml-parser-perl", vp.getName());
        assertTrue(vp.getFixVersion().isPresent());
        PackageEvr expectedEvr = PackageEvr.parseDebian("2.47-1ubuntu0.24.04.1");
        assertEquals(expectedEvr.getVersion(), vp.getFixVersion().get().getVersion());
        assertEquals(expectedEvr.getRelease(), vp.getFixVersion().get().getRelease());
    }

    @Test
    public void testExtractCurrentUbuntuPatchedKernel() {
        DefinitionType definition = createUbuntuDefinition("24.04", "CVE-2024-9999",
                "'linux' kernel in noble was vulnerable but has been fixed (note: '6.8.0-31.31').");

        UbuntuVulnerablePackageExtractor extractor = new UbuntuVulnerablePackageExtractor(definition);
        List<ProductVulnerablePackages> result = extractor.extract();

        assertEquals(1, result.size());
        VulnerablePackage vp = result.get(0).getVulnerablePackages().get(0);
        assertEquals("linux", vp.getName());
        assertTrue(vp.getFixVersion().isPresent());
        assertEquals("6.8.0", vp.getFixVersion().get().getVersion());
        assertEquals("31.31", vp.getFixVersion().get().getRelease());
    }

    @Test
    public void testExtractCurrentUbuntuPatchedKernelWithoutQuotes() {
        DefinitionType definition = createUbuntuDefinition("24.04", "CVE-2024-9999",
                "linux-aws kernel in noble was vulnerable but has been fixed (note: '6.8.0-1009.9').");

        UbuntuVulnerablePackageExtractor extractor = new UbuntuVulnerablePackageExtractor(definition);
        List<ProductVulnerablePackages> result = extractor.extract();

        assertEquals(1, result.size());
        VulnerablePackage vp = result.get(0).getVulnerablePackages().get(0);
        assertEquals("linux-aws", vp.getName());
        assertTrue(vp.getFixVersion().isPresent());
        assertEquals("6.8.0", vp.getFixVersion().get().getVersion());
        assertEquals("1009.9", vp.getFixVersion().get().getRelease());
    }

    @Test
    public void testExtractLegacyUbuntuAffectedPackage() {
        DefinitionType definition = createUbuntuDefinition("22.04", "CVE-2022-1111",
                "sqlite3 package in jammy is affected and may need fixing.");

        UbuntuVulnerablePackageExtractor extractor = new UbuntuVulnerablePackageExtractor(definition);
        List<ProductVulnerablePackages> result = extractor.extract();

        assertEquals(1, result.size());
        assertEquals("sqlite3", result.get(0).getVulnerablePackages().get(0).getName());
        assertTrue(result.get(0).getVulnerablePackages().get(0).getFixVersion().isEmpty());
    }

    @Test
    public void testExtractLegacyUbuntuAffectedPackageV2() {
        DefinitionType definition = createUbuntuDefinition("20.04", "CVE-2020-2222",
                "sqlite3 package in focal is affected and needs fixing.");

        UbuntuVulnerablePackageExtractor extractor = new UbuntuVulnerablePackageExtractor(definition);
        List<ProductVulnerablePackages> result = extractor.extract();

        assertEquals(1, result.size());
        assertEquals("sqlite3", result.get(0).getVulnerablePackages().get(0).getName());
        assertTrue(result.get(0).getVulnerablePackages().get(0).getFixVersion().isEmpty());
    }

    @Test
    public void testExtractLegacyUbuntuAffectedPackageV3Deferred() {
        DefinitionType definition = createUbuntuDefinition("22.04", "CVE-2022-3333",
                "krb5 package in jammy is affected, but a decision has been made to defer addressing it " +
                        "(note: '2025-04-17')");

        UbuntuVulnerablePackageExtractor extractor = new UbuntuVulnerablePackageExtractor(definition);
        List<ProductVulnerablePackages> result = extractor.extract();

        assertEquals(1, result.size());
        assertEquals("krb5", result.get(0).getVulnerablePackages().get(0).getName());
        assertTrue(result.get(0).getVulnerablePackages().get(0).getFixVersion().isEmpty());
    }

    @Test
    public void testExtractLegacyUbuntuPatchedPackage() {
        DefinitionType definition = createUbuntuDefinition("22.04", "CVE-2022-4444",
                "curl package in jammy, is related to the CVE in some way and has been fixed " +
                        "(note: '7.81.0-1ubuntu1.4').");

        UbuntuVulnerablePackageExtractor extractor = new UbuntuVulnerablePackageExtractor(definition);
        List<ProductVulnerablePackages> result = extractor.extract();

        assertEquals(1, result.size());
        VulnerablePackage vp = result.get(0).getVulnerablePackages().get(0);
        assertEquals("curl", vp.getName());
        assertTrue(vp.getFixVersion().isPresent());
        assertEquals("7.81.0", vp.getFixVersion().get().getVersion());
        assertEquals("1ubuntu1.4", vp.getFixVersion().get().getRelease());
    }

    @Test
    public void testExtractLegacyUbuntuPatchedPackageV2() {
        DefinitionType definition = createUbuntuDefinition("20.04", "CVE-2020-5555",
                "nginx package in focal was vulnerable but has been fixed (note: '1.18.0-0ubuntu1.2').");

        UbuntuVulnerablePackageExtractor extractor = new UbuntuVulnerablePackageExtractor(definition);
        List<ProductVulnerablePackages> result = extractor.extract();

        assertEquals(1, result.size());
        VulnerablePackage vp = result.get(0).getVulnerablePackages().get(0);
        assertEquals("nginx", vp.getName());
        assertTrue(vp.getFixVersion().isPresent());
    }

    @Test
    public void testIgnoredNonPackageCriteria() {
        DefinitionType definition1 = createUbuntuDefinition("24.04", "CVE-2024-0001",
                "Is kernel 'linux' running?");
        DefinitionType definition2 = createUbuntuDefinition("24.04", "CVE-2024-0002",
                "The host is running Ubuntu 24.04 LTS (noble).");
        DefinitionType definition3 = createUbuntuDefinition("24.04", "CVE-2024-0003",
                "The host is part of the unix family.");

        assertTrue(new UbuntuVulnerablePackageExtractor(definition1).extract().isEmpty());
        assertTrue(new UbuntuVulnerablePackageExtractor(definition2).extract().isEmpty());
        assertTrue(new UbuntuVulnerablePackageExtractor(definition3).extract().isEmpty());
    }

    @Test
    public void testIsValidDefinition() {
        DefinitionType validDef = createUbuntuDefinition("24.04", "CVE-2024-0001", "comment");
        UbuntuVulnerablePackageExtractor extractor = new UbuntuVulnerablePackageExtractor(validDef);
        assertTrue(extractor.isValidDefinition(validDef));

        DefinitionType debianDef = createUbuntuDefinition("24.04", "CVE-2024-0001", "comment");
        debianDef.setOsFamily(OsFamily.DEBIAN);
        assertFalse(extractor.isValidDefinition(debianDef));

        DefinitionType patchDef = createUbuntuDefinition("24.04", "CVE-2024-0001", "comment");
        patchDef.setDefinitionClass(DefinitionClassEnum.PATCH);
        assertFalse(extractor.isValidDefinition(patchDef));

        DefinitionType inventoryDef = createUbuntuDefinition("24.04", "CVE-2024-0001", "comment");
        inventoryDef.setDefinitionClass(DefinitionClassEnum.INVENTORY);
        assertFalse(extractor.isValidDefinition(inventoryDef));
    }
}
