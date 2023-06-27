package com.suse.oval.test;

import com.suse.oval.TestEvaluator;
import com.suse.oval.UyuniAPI;
import com.suse.oval.db.*;
import com.suse.oval.ovaltypes.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestEvaluatorTest {
    TestEvaluator testEvaluator;
    OVALPackageTest t1;
    OVALPackageTest t2;
    OVALPackageTest t3;
    OVALPackageTest t4;
    OVALPackageTest t5;
    OVALPackageTest t6;
    OVALPackageTest t7;
    OVALPackageTest t8;
    OVALPackageTest t9;
    OVALPackageTest t10;
    OVALPackageTest t11;

    @BeforeEach
    void setUp() {

        List<UyuniAPI.CVEPatchStatus> systemCvePatchStatusList = List.of(
                new UyuniAPI.CVEPatchStatus(1, Optional.of("libsoftokn3-hmac-32bit"),
                        Optional.of(UyuniAPI.PackageEvr.parseRpm("0:3.68.3-150400.1.7")), true),
                new UyuniAPI.CVEPatchStatus(1, Optional.of("libsha1detectcoll1"),
                        Optional.of(UyuniAPI.PackageEvr.parseRpm("0:3.68.2-150400.1.7")), true),
                new UyuniAPI.CVEPatchStatus(1, Optional.of("libsha1detectcoll1"),
                        Optional.of(UyuniAPI.PackageEvr.parseRpm("0:3.68.3-150400.1.7")), true),
                new UyuniAPI.CVEPatchStatus(1, Optional.of("libsha1detectcoll1"),
                        Optional.of(UyuniAPI.PackageEvr.parseRpm("0:3.68.4-150400.1.7")), true, Optional.of("aarch64")),
                new UyuniAPI.CVEPatchStatus(1, Optional.of("postgresql12-plperl"),
                        Optional.of(UyuniAPI.PackageEvr.parseRpm("0:3.68.3-150400.1.7")), true, Optional.of("aarch64")),
                new UyuniAPI.CVEPatchStatus(1, Optional.of("sles-release"),
                        Optional.of(UyuniAPI.PackageEvr.parseRpm("0:15.4-0")), true)
        );

        OVALPackageObject o1 = newOVALPackageObject("libsoftokn3-hmac-32bit");
        OVALPackageObject o2 = newOVALPackageObject("libsha1detectcoll1");
        OVALPackageObject o3 = newOVALPackageObject("postgresql12-plperl");
        OVALPackageObject o4 = newOVALPackageObject("sles-release");

        OVALPackageState s1 = new OVALStateBuilder()
                .withEVR("0:3.68.3-150400.1.7", OperationEnumeration.LESS_THAN)
                .build();

        OVALPackageState s2 = new OVALStateBuilder()
                .withEVR("0:3.68.3-150400.1.7", OperationEnumeration.GREATER_THAN)
                .build();

        OVALPackageState s3 = new OVALStateBuilder()
                .withEVR("0:3.68.3-150400.1.7", OperationEnumeration.EQUALS)
                .build();

        OVALPackageState s4 = new OVALStateBuilder()
                .withEVR("0:3.68.3-150400.1.7", OperationEnumeration.GREATER_THAN)
                .withArch("aarch64", OperationEnumeration.EQUALS)
                .build();

        OVALPackageState s5 = new OVALStateBuilder()
                .withEVR("0:3.68.3-150400.1.7", OperationEnumeration.GREATER_THAN)
                .withArch("(aarch64|noarch)", OperationEnumeration.PATTERN_MATCH)
                .build();

        OVALPackageState s6 = new OVALStateBuilder()
                .withVersion("15.4", OperationEnumeration.EQUALS)
                .build();

        t1 = newOVALPackageTest(o1, s1);
        t2 = newOVALPackageTest(o1, s2);
        t3 = newOVALPackageTest(o1, s3);
        t4 = newOVALPackageTest(o2, s1);
        t5 = newOVALPackageTest(o2, s2);
        t6 = newOVALPackageTest(o2, s3);
        t7 = newOVALPackageTest(o1, s4);
        t8 = newOVALPackageTest(o2, s4);
        t9 = newOVALPackageTest(o3, s4);
        t10 = newOVALPackageTest(o2, s5);
        t11 = newOVALPackageTest(o4, s6);

        testEvaluator = new TestEvaluator(systemCvePatchStatusList);
    }

    /**
     * Test T1 ensures that if the evr state operation is LESS_THAN and the system has a package with an evr less than the
     * state evr, then the evaluation should return 'true'
     */
    @Test
    void testT1() {
        assertFalse(testEvaluator.evaluate(t1));
    }

    @Test
    void testT2() {
        assertFalse(testEvaluator.evaluate(t2));
    }

    @Test
    void testT3() {
        assertTrue(testEvaluator.evaluate(t3));
    }

    @Test
    void testT4() {
        assertTrue(testEvaluator.evaluate(t4));
    }

    @Test
    void testT5() {
        assertTrue(testEvaluator.evaluate(t5));
    }

    @Test
    void testT6() {
        assertTrue(testEvaluator.evaluate(t6));
    }

    @Test
    void testT7() {
        assertFalse(testEvaluator.evaluate(t7));
    }

    /**
     * Tests when both arch and evr properties satisfied
     */
    @Test
    void testT8() {
        assertTrue(testEvaluator.evaluate(t8));
    }

    /**
     * Tests when arch property is satisfied but evr is not satisfied
     */
    @Test
    void testT9() {
        assertFalse(testEvaluator.evaluate(t9));
    }

    /**
     * Test when arch is a pattern
     */
    @Test
    void testT10() {
        assertTrue(testEvaluator.evaluate(t10));
    }

    @Test
    void testT11() {
        assertTrue(testEvaluator.evaluate(t11));
    }

    OVALPackageTest newOVALPackageTest(OVALPackageObject object, OVALPackageState state) {
        OVALPackageTest test = new OVALPackageTest();
        test.setId(RandomStringUtils.randomAlphabetic(24));
        test.setPackageObject(object);
        test.setPackageState(state);

        return test;
    }

    OVALPackageObject newOVALPackageObject(String packageName) {
        OVALPackageObject object = new OVALPackageObject();
        object.setId(RandomStringUtils.randomAlphabetic(24));
        object.setPackageName(packageName);

        return object;
    }

    private class OVALStateBuilder {
        private OVALPackageState state = new OVALPackageState();

        public OVALStateBuilder() {
            state.setId(RandomStringUtils.randomAlphabetic(24));
        }

        public OVALStateBuilder withEVR(String evr, OperationEnumeration operation) {
            OVALPackageEvrStateEntity evrState = new OVALPackageEvrStateEntity();
            evrState.setDatatype(EVRDataTypeEnum.RPM_EVR);
            evrState.setOperation(operation);
            evrState.setEvr(evr);

            state.setPackageEvrState(evrState);

            return this;
        }

        public OVALStateBuilder withArch(String arch, OperationEnumeration operation) {
            OVALPackageArchStateEntity archState = new OVALPackageArchStateEntity();
            archState.setValue(arch);
            archState.setOperation(operation);

            state.setPackageArchState(archState);

            return this;
        }

        public OVALStateBuilder withVersion(String version, OperationEnumeration operation) {
            OVALPackageVersionStateEntity versionState = new OVALPackageVersionStateEntity();
            versionState.setValue(version);
            versionState.setOperation(operation);

            state.setPackageVersionState(versionState);

            return this;
        }

        public OVALPackageState build() {
            return state;
        }
    }
}
