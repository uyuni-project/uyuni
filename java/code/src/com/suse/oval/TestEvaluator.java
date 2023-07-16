package com.suse.oval;

import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageType;
import com.suse.oval.db.*;
import com.suse.oval.ovaltypes.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class TestEvaluator {
    private static final Logger LOG = LogManager.getLogger(TestEvaluator.class);
    private final Map<String, List<SystemPackage>> systemInstalledPackagesByName;
    private final PackageType packageType;

    public TestEvaluator(Map<String, List<SystemPackage>> systemInstalledPackagesByName, PackageType packageType) {
        this.systemInstalledPackagesByName = systemInstalledPackagesByName == null ? new HashMap<>() : systemInstalledPackagesByName;
        this.packageType = packageType;
    }

    public boolean evaluate(OVALPackageTest packageTest) {
        LOG.error("Evaluating OVAL test '{}', comment '{}'", packageTest.getId(), packageTest.getComment());

        if (packageTest.getPackageObject() == null) {
            throw new IllegalStateException();
        }

        OVALPackageObject packageObject = packageTest.getPackageObject();
        List<SystemPackage> packageVersionsOnSystem = systemInstalledPackagesByName.getOrDefault(packageObject.getPackageName(), Collections.emptyList());
        long packageVersionsCount = packageVersionsOnSystem.size();

        ExistenceEnum checkExistence = packageTest.getCheckExistence();
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

        Optional<OVALPackageState> packageStateOpt = packageTest.getPackageState();
        if (packageStateOpt.isEmpty()) {
            return true;
        }

        List<Boolean> stateEvaluations = packageStateOpt.stream()
                .map(state -> evaluatePackageState(packageVersionsOnSystem, state))
                .collect(Collectors.toList());

        return combineBooleans(packageTest.getStateOperator(), stateEvaluations);
    }

    /**
     * Returns {@code true} if any of the installed versions of the package match the given OVAL state.
     * If package is not installed (the list is empty) it should return {@code false}
     * */
    private boolean evaluatePackageState(List<SystemPackage> packageInstalledVersions, OVALPackageState expectedState) {
        return packageInstalledVersions.stream().anyMatch(systemPackage -> {
            // This list holds the evaluation results of each of the specified state entities .e.g. arch,
            // evr, version, etc.
            List<Boolean> stateEntitiesEvaluations = new ArrayList<>();

            LOG.error("Hey {}", expectedState.getId());
            LOG.error("System Package EVR {} ", systemPackage.getPackageEVR().toUniversalEvrString());
            LOG.error("System Package Name {} ", systemPackage.getName());
            LOG.error("aa {}", systemPackage.toString());

            Optional<OVALPackageEvrStateEntity> expectedEvrOpt = expectedState.getPackageEvrState();
            if (expectedEvrOpt.isPresent()) {
                OVALPackageEvrStateEntity expectedEvr = expectedEvrOpt.get();


                PackageEvr packageOnSystemEVR = systemPackage.getPackageEVR();

                PackageEvr packageOnOvalEVR = PackageEvr
                        .parsePackageEvr(toPackageType(expectedEvr.getDatatype()), expectedEvr.getEvr());

                int evrComparisonResult = packageOnSystemEVR.compareTo(packageOnOvalEVR);

                LOG.error("EVR Comparison result: {}", evrComparisonResult);
                LOG.error("EVR on System: {}", packageOnSystemEVR.toUniversalEvrString());
                LOG.error("EVR in OVAL: {}", packageOnOvalEVR.toUniversalEvrString());

                stateEntitiesEvaluations.add(checkPackageEVR(evrComparisonResult, expectedEvr.getOperation()));
            }


            Optional<OVALPackageArchStateEntity> expectedArchOpt = expectedState.getPackageArchState();
            if (expectedArchOpt.isPresent()) {
                OVALPackageArchStateEntity expectedArch = expectedArchOpt.get();

                stateEntitiesEvaluations.add(checkPackageArch(systemPackage.getArch(),
                        expectedArch.getValue(), expectedArch.getOperation()));
            }

            Optional<OVALPackageVersionStateEntity> expectedVersionOpt = expectedState.getPackageVersionState();
            if (expectedVersionOpt.isPresent()) {
                OVALPackageVersionStateEntity expectedVersion = expectedVersionOpt.get();

                LOG.error("Expected version '{}'", expectedVersion.getValue());

                PackageEvr packageOnSystemEVR = systemPackage.getPackageEVR();

                LOG.error("Package on System Version '{}'", packageOnSystemEVR.getVersion());

                stateEntitiesEvaluations.add(checkPackageVersion(packageOnSystemEVR.getVersion(),
                        expectedVersion.getValue(), expectedVersion.getOperation()));
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

    private PackageType toPackageType(EVRDataTypeEnum evrDataTypeEnum) {
        Objects.requireNonNull(evrDataTypeEnum);

        return evrDataTypeEnum == EVRDataTypeEnum.DEBIAN_EVR ? PackageType.DEB : PackageType.RPM;
    }

    private boolean combineBooleans(LogicOperatorType operator, List<Boolean> booleans) {
        //TODO: Temporary
        if (booleans.isEmpty()) {
            return true;
        }

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
