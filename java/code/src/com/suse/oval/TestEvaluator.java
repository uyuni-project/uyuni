package com.suse.oval;

import com.suse.oval.manager.OvalObjectManager;
import com.suse.oval.manager.OvalStateManager;
import com.suse.oval.manager.OvalTestManager;
import com.suse.oval.ovaltypes.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class TestEvaluator {
    private final OvalTestManager ovalTestManager;
    private final OvalObjectManager ovalObjectManager;
    private final OvalStateManager ovalStateManager;
    private final List<UyuniAPI.CVEPatchStatus> systemCvePatchStatusList;

    public TestEvaluator(OvalTestManager ovalTestManager, OvalObjectManager ovalObjectManager,
                         OvalStateManager ovalStateManager, List<UyuniAPI.CVEPatchStatus> systemCvePatchStatusList) {
        Objects.requireNonNull(ovalTestManager);

        this.ovalStateManager = ovalStateManager;
        this.ovalObjectManager = ovalObjectManager;
        this.systemCvePatchStatusList = systemCvePatchStatusList == null ? new ArrayList<>() : systemCvePatchStatusList;
        this.ovalTestManager = ovalTestManager;
    }

    public boolean evaluate(String testId) {
        TestType test = ovalTestManager.get(testId);
        if (test.getObject() == null) {
            throw new IllegalStateException();
        }

        ObjectType object = ovalObjectManager.get(test.getObject().getObjectRef());
        List<UyuniAPI.CVEPatchStatus> packageVersionsOnSystem = listPackageVersionsInstalledOnSystem(object.getPackageName());
        long packageVersionsCount = packageVersionsOnSystem.size();

        ExistenceEnum checkExistence = test.getCheckExistence();
        switch (checkExistence) {
            case NONE_EXIST:
                if (packageVersionsCount != 0) {
                    return false;
                }
                break;
            case ONLY_ONE_EXISTS:
                if (packageVersionsCount != 1) {
                    return false;
                }
                break;
            // We have only one component under consideration that is the package name,
            // thus 'all_exist' and 'at_least_one_exists' are logically equivalent.
            case ALL_EXIST:
            case AT_LEAST_ONE_EXISTS:
                if (packageVersionsCount < 1) {
                    return false;
                }
                break;
        }

        Optional<String> ovalState = test.getStateRef();
        if (ovalState.isEmpty()) {
            return true;
        }

        List<Boolean> stateEvaluations = ovalState.stream()
                .map(ovalStateManager::get)
                .map(state -> evaluatePackageState(packageVersionsOnSystem, state))
                .collect(Collectors.toList());

        return combineBooleans(test.getStateOperator(), stateEvaluations);
    }

    private boolean evaluatePackageState(List<UyuniAPI.CVEPatchStatus> packageVersionsOnSystem, StateType expectedState) {
        return packageVersionsOnSystem.stream().anyMatch(cvePatchStatus -> {
            // This list holds the evaluation results of each of the specified state entities .e.g. arch,
            // evr, version, etc.
            List<Boolean> stateEntitiesEvaluations = new ArrayList<>();

            EVRType expectedEvr = expectedState.getPackageEVR();
            if (expectedEvr != null) {
                cvePatchStatus.getPackageEvr().ifPresent(packageOnSystemEVR -> {
                    UyuniAPI.PackageEvr packageOnOvalEVR = UyuniAPI.PackageEvr
                            .parsePackageEvr(toPackageType(expectedEvr.getDatatype()), expectedEvr.getValue());

                    int evrComparisonResult = packageOnSystemEVR.compareTo(packageOnOvalEVR);

                    stateEntitiesEvaluations.add(checkPackageEVR(evrComparisonResult, expectedEvr.getOperation()));
                });
            }


            ArchType expectedArch = expectedState.getPackageArch();
            if (expectedArch != null) {
                stateEntitiesEvaluations.add(checkPackageArch(cvePatchStatus.getPackageArch().orElse(""),
                        expectedArch.getValue(), expectedArch.getOperation()));
            }

            VersionType expectedVersion = expectedState.getPackageVersion();
            if (expectedVersion != null) {
                cvePatchStatus.getPackageEvr().ifPresent(packageOnSystemEVR -> {
                    stateEntitiesEvaluations.add(checkPackageVersion(packageOnSystemEVR.getVersion(),
                            expectedVersion.getValue(), expectedVersion.getOperation()));
                });
            }

            return combineBooleans(expectedState.getOperator(), stateEntitiesEvaluations);
        });
    }

    private boolean checkPackageEVR(int evrComparisonResult, OperationEnumeration operation) {
        return (evrComparisonResult == 0 && operation == OperationEnumeration.EQUALS) ||
                (evrComparisonResult != 0 && operation == OperationEnumeration.NOT_EQUAL) ||
                (evrComparisonResult > 0 && operation == OperationEnumeration.GREATER_THAN) ||
                (evrComparisonResult >= 0 && operation == OperationEnumeration.GREATER_THAN_OR_EQUAL) ||
                (evrComparisonResult < 0 && operation == OperationEnumeration.LESS_THAN) ||
                (evrComparisonResult <= 0 && operation == OperationEnumeration.LESS_THAN_OR_EQUAL);
    }

    private boolean checkPackageArch(String systemPackageArch, String expectedArch, OperationEnumeration operation) {
        switch (operation) {
            case PATTERN_MATCH:
                return systemPackageArch.matches(expectedArch);
            case EQUALS:
                return systemPackageArch.equals(expectedArch);
            case NOT_EQUAL:
                return !systemPackageArch.equals(expectedArch);
            default:
                throw new IllegalArgumentException("The specified operation is not supported");
        }
    }

    private boolean checkPackageVersion(String systemPackageVersion, String expectedVersion, OperationEnumeration operation) {
        if (!(operation == OperationEnumeration.EQUALS || operation == OperationEnumeration.NOT_EQUAL)) {
            throw new IllegalArgumentException("Operation '" + operation + "' is not supported for the version state entity");
        }

        return systemPackageVersion.equals(expectedVersion);
    }

    private List<UyuniAPI.CVEPatchStatus> listPackageVersionsInstalledOnSystem(String packageName) {
        return systemCvePatchStatusList.stream()
                .filter(cvePatchStatus -> Optional.ofNullable(packageName).equals(cvePatchStatus.getPackageName()))
                .collect(Collectors.toList());
    }

    private UyuniAPI.PackageType toPackageType(EVRDataTypeEnum evrDataTypeEnum) {
        Objects.requireNonNull(evrDataTypeEnum);

        return evrDataTypeEnum == EVRDataTypeEnum.DEBIAN_EVR ? UyuniAPI.PackageType.DEB : UyuniAPI.PackageType.RPM;
    }

    private boolean combineBooleans(LogicOperatorType operator, List<Boolean> booleans) {
        switch (operator) {
            case AND:
                return booleans.stream().allMatch(Boolean::booleanValue);
            case OR:
                return booleans.stream().anyMatch(Boolean::booleanValue);
            case XOR:
                return booleans.stream().reduce((a, b) -> a ^ b).orElse(false);
            case ONE:
                return booleans.stream().filter(Boolean::booleanValue).count() == 1L;
        }
        return false;
    }
}
