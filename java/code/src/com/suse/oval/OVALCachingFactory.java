package com.suse.oval;

import com.redhat.rhn.common.db.datasource.CallableMode;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.Row;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;

import com.suse.oval.manager.OVALLookupHelper;
import com.suse.oval.ovaltypes.DefinitionType;
import com.suse.oval.ovaltypes.OvalRootType;
import com.suse.oval.vulnerablepkgextractor.ProductVulnerablePackages;
import com.suse.oval.vulnerablepkgextractor.VulnerablePackage;
import com.suse.oval.vulnerablepkgextractor.VulnerablePackagesExtractor;
import com.suse.oval.vulnerablepkgextractor.VulnerablePackagesExtractors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class OVALCachingFactory extends HibernateFactory {
    private static final Logger LOG = LogManager.getLogger(OVALCachingFactory.class);
    public static final int BATCH_SIZE = 60;
    private static OVALCachingFactory instance = new OVALCachingFactory();

    private OVALCachingFactory() {
        // Left empty on purpose
    }

    /**
     * Save all OVAL constructs (definitions, tests, objects and states) associated with the given {@code rootType}
     * */
    public static void saveOVAL(OvalRootType rootType, OsFamily osFamily, String osVersion) {
        savePackageObjects(rootType.getObjects().getObjects());
        savePackageStates(rootType.getStates().getStates());
        savePackageTests(rootType.getTests().getTests());
    }

    /**
     * Insert the passed OVAL definitions into the database.
     * <p>
     * Also inserts the affected platforms and references information (if not already inserted) into the relevant tables
     */
    public static void saveDefinitions(List<DefinitionType> definitions, OsFamily osFamily, String osVersion) {
        CallableMode mode = ModeFactory.getCallableMode("oval_queries", "insert_definition");

        int i = 0;
        for (DefinitionType definition : definitions) {
            Map<String, Object> params = new HashMap<>();
            params.put("id", definition.getId());
            params.put("class", definition.getDefinitionClass().toString());
            params.put("title", definition.getMetadata().getTitle());
            params.put("cve_name", definition.getSingleCve());
            params.put("description", definition.getMetadata().getDescription());
            params.put("os_family", osFamily.toString());
            params.put("os_version", osVersion);
            params.put("criteria_tree", ObjectMapperWrapper.INSTANCE.toString(definition.getCriteria()));

            mode.execute(params, new HashMap<>());

            if (i % 60 == 0) {
                LOG.error(definition.getId());
            }

            i++;
        }

        saveAffectedPlatforms(definitions);
        saveReferences(definitions);
    }

    public static void savePackageTests(List<TestType> packageTests) {
        WriteMode mode = ModeFactory.getWriteMode("oval_queries", "insert_package_test");

        DataResult<Map<String, Object>> batch = new DataResult<>(new ArrayList<>(60));
        for (int i = 0; i < packageTests.size(); i++) {
            TestType packageTest = packageTests.get(i);

            Map<String, Object> params = new HashMap<>();
            params.put("id", packageTest.getId());
            params.put("comment", packageTest.getComment());
            params.put("check_exist", packageTest.getCheckExistence().toString());
            params.put("test_check", packageTest.getCheck().toString());
            params.put("state_operator", packageTest.getStateOperator().toString());
            params.put("isrpm", true);
            params.put("pkg_object_id", packageTest.getObjectRef());
            params.put("pkg_state_id", packageTest.getStateRef().orElse(null));

            batch.add(params);

            if (i % 60 == 0) {
                mode.executeBatchUpdates(batch);
                batch.clear();
                LOG.warn("Saved 60 more tests");
            }
        }

        mode.executeBatchUpdates(batch);
    }

    public static void savePackageObjects(List<ObjectType> packageObjects) {
        WriteMode mode = ModeFactory.getWriteMode("oval_queries", "insert_package_object");

        DataResult<Map<String, Object>> batch = new DataResult<>(new ArrayList<>(60));
        for (int i = 0; i < packageObjects.size(); i++) {
            ObjectType packageObject = packageObjects.get(i);

            Map<String, Object> params = new HashMap<>();
            params.put("id", packageObject.getId());
            params.put("name", packageObject.getPackageName());
            params.put("isrpm", packageObject.isRpm());

            batch.add(params);

            if (i % 60 == 0) {
                mode.executeBatchUpdates(batch);
                batch.clear();
                LOG.warn("Saved 60 more objects");
            }
        }

        mode.executeBatchUpdates(batch);
    }

    public static void savePackageStates(List<StateType> packageStates) {
        WriteMode mode = ModeFactory.getWriteMode("oval_queries", "insert_package_state");

        DataResult<Map<String, Object>> batch = new DataResult<>(new ArrayList<>(60));
        for (int i = 0; i < packageStates.size(); i++) {
            StateType packageState = packageStates.get(i);

            Map<String, Object> params = new HashMap<>();
            params.put("id", packageState.getId());
            params.put("operator", packageState.getOperator().toString());
            // TODO: Fix this!
            params.put("isrpm", true);
            // These fields are optional and may not exist for every package state, so we set them to null as a default
            params.put("version_state_id", null);
            params.put("arch_state_id", null);
            params.put("evr_state_id", null);

            packageState.getPackageEVR().ifPresent(evr ->
                    params.put("evr_state_id", savePackageEvrState(evr)));

            packageState.getPackageArch().ifPresent(arch ->
                    params.put("arch_state_id", savePackageArchState(arch)));

            packageState.getPackageVersion().ifPresent(version ->
                    params.put("version_state_id", savePackageVersionState(version)));

            batch.add(params);

            if (i % 60 == 0) {
                mode.executeBatchUpdates(batch);
                batch.clear();
                LOG.warn("Saved 60 more states");
            }
        }

        mode.executeBatchUpdates(batch);
    }

    private static long savePackageEvrState(EVRType evr) {
        CallableMode mode = ModeFactory.getCallableMode("oval_queries", "insert_package_evr_state");

        Map<String, Object> params = new HashMap<>();
        params.put("evr", evr.getValue());
        params.put("operation", evr.getOperation().toString());
        params.put("datatype", evr.getDatatype().toString());

        Map<String, Integer> outParams = new HashMap<>();
        outParams.put("evrStateId", Types.NUMERIC);

        return (long) mode.execute(params, outParams).get("evrStateId");
    }

    private static long savePackageArchState(ArchType arch) {
        CallableMode mode = ModeFactory.getCallableMode("oval_queries", "insert_package_arch_state");

        Map<String, Object> params = new HashMap<>();
        params.put("arch", arch.getValue());
        params.put("operation", arch.getOperation().toString());

        Map<String, Integer> outParams = new HashMap<>();
        outParams.put("archStateId", Types.NUMERIC);

        return (long) mode.execute(params, outParams).get("archStateId");
    }
    private static long savePackageVersionState(VersionType version) {
        CallableMode mode = ModeFactory.getCallableMode("oval_queries", "insert_package_version_state");

        Map<String, Object> params = new HashMap<>();
        params.put("version", version.getValue());
        params.put("operation", version.getOperation().toString());

        Map<String, Integer> outParams = new HashMap<>();
        outParams.put("versionStateId", Types.NUMERIC);

        return (long) mode.execute(params, outParams).get("versionStateId");
    }


    private static void saveAffectedPlatforms(List<DefinitionType> definitions) {
        deleteOldAffectedPlatforms(definitions);

        CallableMode mode = ModeFactory.getCallableMode("oval_queries", "add_affected_platform_to_definition");

        List<Map<String, Object>> collect = definitions.stream().flatMap(definition ->
                definition.getMetadata().getAffected().get(0).getPlatforms().stream().map(affectedPlatform -> {
            Map<String, Object> params = new HashMap<>();
            params.put("definition_id", definition.getId());
            params.put("platform_name", affectedPlatform);

            return params;
        })).collect(Collectors.toList());

        toBatches(collect).forEach(l -> mode.getQuery().executeBatchUpdates(new DataResult<>(l)));
    }

    private static void saveReferences(List<DefinitionType> definitions) {
        deleteOldReferences(definitions);

        WriteMode mode = ModeFactory.getWriteMode("oval_queries", "add_reference_to_definition");

        for (DefinitionType definition : definitions) {
            for (ReferenceType reference : definition.getMetadata().getReference()) {
                Map<String, Object> params = new HashMap<>();
                params.put("ref_id", reference.getRefId());
                params.put("definition_id", definition.getId());
                params.put("source", reference.getSource());
                params.put("url", reference.getRefUrl().orElse(""));

                mode.executeUpdate(params);
            }
        }
    }

    private static void deleteOldReferences(List<DefinitionType> definitions) {
        WriteMode mode = ModeFactory.getWriteMode("oval_queries", "delete_references_from_definition");

        DataResult<Map<String, Object>> batch = new DataResult<>(new ArrayList<>(60));
        for (int i = 0; i < definitions.size(); i++) {
            DefinitionType definition = definitions.get(i);

            Map<String, Object> params = new HashMap<>();
            params.put("definition_id", definition.getId());

            batch.add(params);

            if (i % 60 == 0) {
                mode.executeBatchUpdates(batch);
                batch.clear();
                LOG.warn("Deleted 60 more references");
            }
        }
        mode.executeBatchUpdates(batch);
    }

    private static void deleteOldAffectedPlatforms(List<DefinitionType> definitions) {
        WriteMode mode = ModeFactory.getWriteMode("oval_queries", "delete_affected_platforms_from_definition");

        DataResult<Map<String, Object>> batch = new DataResult<>(new ArrayList<>(60));
        for (int i = 0; i < definitions.size(); i++) {
            DefinitionType definition = definitions.get(i);

            Map<String, Object> params = new HashMap<>();
            params.put("definition_id", definition.getId());

            batch.add(params);

            if (i % 60 == 0) {
                mode.executeBatchUpdates(batch);
                batch.clear();
                LOG.warn("Deleted 60 more affected platforms");
            }
        }
        mode.executeBatchUpdates(batch);
    }

    public static void savePlatformsVulnerablePackages(List<DefinitionType> definitions, OsFamily osFamily, String osVersion) {
        CallableMode mode = ModeFactory.getCallableMode("oval_queries", "add_product_vulnerable_package");

        List<Map<String, Object>> paramsList = new ArrayList<>();

        for (DefinitionType definition : definitions) {
            VulnerablePackagesExtractor vulnerablePackagesExtractor =
                    VulnerablePackagesExtractors.create(definition, osFamily);

            List<ProductVulnerablePackages> extractionResult = vulnerablePackagesExtractor.extract();
            for (ProductVulnerablePackages productVulnerablePackages : extractionResult) {
                for (String cve : productVulnerablePackages.getCves()) {
                    for (VulnerablePackage vulnerablePackage : productVulnerablePackages.getVulnerablePackages()) {
                        Map<String, Object> params = new HashMap<>();
                        params.put("product_name", productVulnerablePackages.getProductCpe());
                        params.put("cve_name", cve);
                        params.put("package_name", vulnerablePackage.getName());
                        params.put("fix_version", vulnerablePackage.getFixVersion().orElse(null));

                        paramsList.add(params);
                    }
                }
            }
        }

        LOG.warn("Starting...");
        toBatches(paramsList).peek(params -> LOG.warn("Saved 1 batch of vulnerable packages"))
                .forEach(l -> mode.getQuery().executeBatchUpdates(new DataResult<>(l)));
        LOG.warn("Ending...");
    }

    public static List<VulnerablePackage> getVulnerablePackagesByProductAndCve(String productCpe, String cve) {
        SelectMode mode = ModeFactory.getMode("oval_queries", "get_vulnerable_packages");

        Map<String, Object> params = new HashMap<>();
        params.put("cve_name", cve);
        params.put("product_cpe", productCpe);

        DataResult<Row> result = mode.execute(params);

        return result.stream().map(row -> {
            VulnerablePackage vulnerablePackage = new VulnerablePackage();
            vulnerablePackage.setName((String) row.get("vulnerablepkgname"));
            vulnerablePackage.setFixVersion((String) row.get("vulnerablepkgfixversion"));
            return vulnerablePackage;
        }).collect(Collectors.toList());
    }

    private static <T> Stream<List<T>> toBatches(List<T> source) {
        int size = source.size();
        if (size == 0)
            return Stream.empty();
        int fullChunks = (size - 1) / BATCH_SIZE;
        return IntStream.range(0, fullChunks + 1).mapToObj(
                n -> source.subList(n * 60, n == fullChunks ? size : (n + 1) * 60));
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
