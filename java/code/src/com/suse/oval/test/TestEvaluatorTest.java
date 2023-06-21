package com.suse.oval.test;

import com.suse.oval.TestEvaluator;
import com.suse.oval.UyuniAPI;
import com.suse.oval.manager.OvalObjectManager;
import com.suse.oval.manager.OvalStateManager;
import com.suse.oval.manager.OvalTestManager;
import com.suse.oval.ovaltypes.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestEvaluatorTest {
    TestEvaluator testEvaluator;
    OvalObjectManager ovalObjectManager = new OvalObjectManager(Collections.emptyList());
    OvalStateManager ovalStateManager = new OvalStateManager(Collections.emptyList());
    OvalTestManager ovalTestManager = new OvalTestManager(Collections.emptyList());

    TestType t1;
    TestType t2;
    TestType t3;
    TestType t4;
    TestType t5;
    TestType t6;
    TestType t7;
    TestType t8;
    TestType t9;
    TestType t10;
    TestType t11;

    @BeforeEach
    void setUp() {
        ovalObjectManager = new OvalObjectManager(Collections.emptyList());
        ovalStateManager = new OvalStateManager(Collections.emptyList());
        ovalTestManager = new OvalTestManager(Collections.emptyList());

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

        ObjectType o1 = newObjectType("obj:1", "libsoftokn3-hmac-32bit");
        ObjectType o2 = newObjectType("obj:2", "libsha1detectcoll1");
        ObjectType o3 = newObjectType("obj:3", "postgresql12-plperl");
        ObjectType o4 = newObjectType("obj:4", "sles-release");

        StateType s1 = new StateTypeBuilder("ste:1")
                .withEVR("0:3.68.3-150400.1.7", OperationEnumeration.LESS_THAN)
                .build();

        StateType s2 = new StateTypeBuilder("ste:2")
                .withEVR("0:3.68.3-150400.1.7", OperationEnumeration.GREATER_THAN)
                .build();

        StateType s3 = new StateTypeBuilder("ste:3")
                .withEVR("0:3.68.3-150400.1.7", OperationEnumeration.EQUALS)
                .build();

        StateType s4 = new StateTypeBuilder("ste:4")
                .withEVR("0:3.68.3-150400.1.7", OperationEnumeration.GREATER_THAN)
                .withArch("aarch64", OperationEnumeration.EQUALS)
                .build();

        StateType s5 = new StateTypeBuilder("ste:5")
                .withEVR("0:3.68.3-150400.1.7", OperationEnumeration.GREATER_THAN)
                .withArch("(aarch64|noarch)", OperationEnumeration.PATTERN_MATCH)
                .build();

        StateType s6 = new StateTypeBuilder("ste:6")
                .withVersion("15.4", OperationEnumeration.EQUALS)
                .build();

        t1 = newTestType("tst:1", o1, s1);
        t2 = newTestType("tst:2", o1, s2);
        t3 = newTestType("tst:3", o1, s3);
        t4 = newTestType("tst:4", o2, s1);
        t5 = newTestType("tst:5", o2, s2);
        t6 = newTestType("tst:6", o2, s3);
        t7 = newTestType("tst:7", o1, s4);
        t8 = newTestType("tst:8", o2, s4);
        t9 = newTestType("tst:9", o3, s4);
        t10 = newTestType("tst:10", o2, s5);
        t11 = newTestType("tst:11", o4, s6);

        testEvaluator = new TestEvaluator(ovalTestManager, ovalObjectManager, ovalStateManager, systemCvePatchStatusList);
    }

    /**
     * Test T1 ensures that if the evr state operation is LESS_THAN and the system has a package with an evr less than the
     * state evr, then the evaluation should return 'true'
     */
    @Test
    void testT1() {
        assertFalse(testEvaluator.evaluate(t1.getId()));
    }

    @Test
    void testT2() {
        assertFalse(testEvaluator.evaluate(t2.getId()));
    }

    @Test
    void testT3() {
        assertTrue(testEvaluator.evaluate(t3.getId()));
    }

    @Test
    void testT4() {
        assertTrue(testEvaluator.evaluate(t4.getId()));
    }

    @Test
    void testT5() {
        assertTrue(testEvaluator.evaluate(t5.getId()));
    }

    @Test
    void testT6() {
        assertTrue(testEvaluator.evaluate(t6.getId()));
    }

    @Test
    void testT7() {
        assertFalse(testEvaluator.evaluate(t7.getId()));
    }

    /**
     * Tests when both arch and evr properties satisfied
     */
    @Test
    void testT8() {
        assertTrue(testEvaluator.evaluate(t8.getId()));
    }

    /**
     * Tests when arch property is satisfied but evr is not satisfied
     */
    @Test
    void testT9() {
        assertFalse(testEvaluator.evaluate(t9.getId()));
    }

    /**
     * Test when arch is a pattern
     */
    @Test
    void testT10() {
        assertTrue(testEvaluator.evaluate(t10.getId()));
    }

    @Test
    void testT11() {
        assertTrue(testEvaluator.evaluate(t11.getId()));
    }

    TestType newTestType(String id, ObjectType object, List<StateType> states) {
        ObjectRefType objectRefType = new ObjectRefType();
        objectRefType.setObjectRef(object.getId());

        List<StateRefType> stateRefs = states.stream().map(state -> {
            StateRefType stateRefType = new StateRefType();
            stateRefType.setStateRef(state.getId());
            return stateRefType;
        }).collect(Collectors.toList());

        TestType testType = new TestType();
        testType.setId(id);
        testType.setObject(objectRefType);
        testType.setStates(stateRefs);

        ovalTestManager.add(testType);

        return testType;
    }

    TestType newTestType(String id, ObjectType object, StateType state) {
        return newTestType(id, object, List.of(state));
    }

    ObjectType newObjectType(String id, String packageName) {
        ObjectRefType objectRefType = new ObjectRefType();
        objectRefType.setObjectRef(id);

        ObjectType object = new ObjectType();
        object.setId(objectRefType.getObjectRef());
        object.setPackageName(packageName);

        ovalObjectManager.add(object);

        return object;
    }

    private class StateTypeBuilder {
        private StateType state = new StateType();

        public StateTypeBuilder(String id) {
            state.setId(id);
        }

        public StateTypeBuilder withEVR(String evr, OperationEnumeration operation) {
            EVRType evrType = new EVRType();
            evrType.setDatatype(EVRDataTypeEnum.RPM_EVR);
            evrType.setOperation(operation);
            evrType.setValue(evr);

            state.setPackageEVR(evrType);

            return this;
        }

        public StateTypeBuilder withArch(String arch, OperationEnumeration operation) {
            ArchType archType = new ArchType();
            archType.setValue(arch);
            archType.setOperation(operation);

            state.setPackageArch(archType);

            return this;
        }

        public StateTypeBuilder withVersion(String version, OperationEnumeration operation) {
            VersionType versionType = new VersionType();
            versionType.setValue(version);
            versionType.setOperation(operation);

            state.setPackageVersion(versionType);

            return this;
        }

        public StateType build() {
            ovalStateManager.add(state);
            return state;
        }
    }
}
