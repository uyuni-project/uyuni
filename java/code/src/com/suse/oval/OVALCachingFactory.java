package com.suse.oval;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.errata.Cve;
import com.redhat.rhn.domain.errata.CveFactory;

import com.suse.oval.db.OVALDefinition;
import com.suse.oval.db.OVALPackageArchStateEntity;
import com.suse.oval.db.OVALPackageEvrStateEntity;
import com.suse.oval.db.OVALPackageObject;
import com.suse.oval.db.OVALPackageState;
import com.suse.oval.db.OVALPackageTest;
import com.suse.oval.db.OVALPackageVersionStateEntity;
import com.suse.oval.db.OVALPlatform;
import com.suse.oval.db.OVALPlatformVulnerablePackage;
import com.suse.oval.db.OVALReference;
import com.suse.oval.db.OVALVulnerablePackage;
import com.suse.oval.manager.OvalObjectManager;
import com.suse.oval.manager.OvalStateManager;
import com.suse.oval.ovaltypes.Advisory;
import com.suse.oval.ovaltypes.DefinitionType;
import com.suse.oval.ovaltypes.ObjectType;
import com.suse.oval.ovaltypes.ReferenceType;
import com.suse.oval.ovaltypes.StateType;
import com.suse.oval.ovaltypes.TestType;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OVALCachingFactory extends HibernateFactory {
    private static final Logger LOG = LogManager.getLogger(OVALCachingFactory.class);
    private static OVALCachingFactory instance = new OVALCachingFactory();

    private OVALCachingFactory() {
        // Left empty on purpose
    }

    /**
     * Insert the passed OVAL definitions into the database.
     * <p>
     * Also inserts the affected platforms and references information (if not already inserted) into the relevant tables
     */
    public static void saveDefinitions(List<DefinitionType> definitionTypes, OVALDefinitionSource source) {
        for (int i = 0; i < definitionTypes.size(); i++) {
            DefinitionType definitionType = definitionTypes.get(i);

            if(definitionType.getDefinitionClass() != DefinitionClassEnum.VULNERABILITY) {
                continue;
            }

            OVALDefinition definition = new OVALDefinition();
            definition.setId(definitionType.getId());
            definition.setTitle(definitionType.getMetadata().getTitle());
            definition.setDefClass(definitionType.getDefinitionClass());
            definition.setDescription(definitionType.getMetadata().getDescription());
            definition.setCriteriaTree(definitionType.getCriteria());

            definition.setSource(source);

            // TODO: affected cpe list is not present in all OVAL files.
            List<String> affectedCpeList = definitionType.getMetadata().getAdvisory()
                    .map(Advisory::getAffectedCpeList)
                    .orElse(Collections.emptyList());

            HibernateFactory.doWithoutAutoFlushing(
                    () -> saveDefinition(definition, affectedCpeList, definitionType.getMetadata().getReference())
            );


            if (i % 60 == 0) {
                LOG.error(definitionType.getId());
                getSession().flush();
            }
        }
    }


    private static void saveDefinition(OVALDefinition definition, List<String> affectedCpeList, List<ReferenceType> references) {
        definition.setAffectedPlatforms(
                affectedCpeList.stream()
                        .map(OVALCachingFactory::lookupOrInsertPlatformByCpe)
                        .collect(Collectors.toList())
        );

        definition.setReferences(
                references.stream().map(ref -> {
                    OVALReference ovalRef = new OVALReference();
                    ovalRef.setDefinition(definition);
                    ovalRef.setRefId(ref.getRefId());
                    ovalRef.setSource(ref.getSource());
                    ovalRef.setRefURL(ref.getRefUrl().orElse(""));

                    return ovalRef;
                }).collect(Collectors.toList())
        );

        String cve = extractCveFromDefinition(definition);
        definition.setCve(CveFactory.lookupOrInsertByName(cve));

        instance.saveObject(definition);
    }


    /**
     * Looks up an {@link OVALReference} by id (reference and definition id)
     */
    public static OVALReference lookupReferenceByRefIdAndDefinition(String refId, String definitionId) {
        return getSession()
                .createNamedQuery("OVALReference.lookupReferenceByRefIdAndDefinition", OVALReference.class)
                .setParameter("refId", refId)
                .setParameter("definitionId", definitionId)
                .uniqueResult();
    }

    /**
     * Looks up an {@link OVALPlatform} or inserts it if it does not exist.
     *
     * @param cpe cpe of the platform
     * @return the platform
     */
    public static OVALPlatform lookupOrInsertPlatformByCpe(String cpe) {
        OVALPlatform platform = lookupPlatformByCpe(cpe);
        if (platform == null) {
            OVALPlatform newPlatform = new OVALPlatform();
            newPlatform.setCpe(cpe);
            instance.saveObject(newPlatform);

            platform = newPlatform;
        }

        return platform;
    }

    /**
     *
     * */
    public static OVALPlatform lookupPlatformByCpe(String cpe) {
        Session session = HibernateFactory.getSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();

        CriteriaQuery<OVALPlatform> criteriaQuery = builder.createQuery(OVALPlatform.class);
        Root<OVALPlatform> root = criteriaQuery.from(OVALPlatform.class);

        criteriaQuery.where(builder.equal(root.get("cpe"), cpe));

        return session.createQuery(criteriaQuery)
                .setCacheable(true).uniqueResult();
    }

    public static void savePackageTests(List<TestType> ovalTests,
                                        OvalObjectManager objectManager, OvalStateManager stateManager) {

        for (int i = 0; i < ovalTests.size(); i++) {
            TestType testType = ovalTests.get(i);

            OVALPackageTest ovalPackageTest = new OVALPackageTest();
            ovalPackageTest.setId(testType.getId());
            ovalPackageTest.setComment(testType.getComment());
            ovalPackageTest.setCheck(testType.getCheck());
            ovalPackageTest.setCheckExistence(testType.getCheckExistence());
            // TODO: fix!
            ovalPackageTest.setRpm(true);

            ObjectType objectType = objectManager.get(testType.getObject().getObjectRef());
            OVALPackageObject ovalPackageObject = new OVALPackageObject();
            ovalPackageObject.setId(objectType.getId());
            ovalPackageObject.setPackageName(objectType.getPackageName());
            ovalPackageObject.setRpm(true);

            LOG.error("Package name: '{}'", objectType.getPackageName());
            // Debian OVAL data sometimes contains null objects
            if (objectType.getPackageName() == null || StringUtils.isEmpty(objectType.getPackageName())) {
                continue;
            }

            StateType stateType = testType.getStateRef().map(stateManager::get).orElse(null);
            // TODO: fix!
            if (stateType == null) {
                throw new IllegalStateException();
            }

            OVALPackageState ovalPackageState = new OVALPackageState();
            ovalPackageState.setId(stateType.getId());
            ovalPackageState.setOperator(stateType.getOperator());
            ovalPackageState.setRpm(true);

            OVALPackageEvrStateEntity ovalPackageEvrStateEntity = stateType.getPackageEVR().map(evrType -> {
                OVALPackageEvrStateEntity result = new OVALPackageEvrStateEntity();
                result.setEvr(evrType.getValue());
                result.setDatatype(evrType.getDatatype());
                result.setOperation(evrType.getOperation());

                return result;
            }).orElse(null);

            OVALPackageArchStateEntity ovalPackageArchStateEntity = stateType.getPackageArch().map(archType -> {
                OVALPackageArchStateEntity result = new OVALPackageArchStateEntity();
                result.setOperation(archType.getOperation());
                result.setValue(archType.getValue());

                return result;
            }).orElse(null);

            OVALPackageVersionStateEntity ovalPackageVersionStateEntity = stateType.getPackageVersion().map(versionType -> {
                OVALPackageVersionStateEntity result = new OVALPackageVersionStateEntity();
                result.setValue(versionType.getValue());
                result.setOperation(versionType.getOperation());

                return result;
            }).orElse(null);

            ovalPackageTest.setPackageState(ovalPackageState);
            ovalPackageTest.setPackageObject(ovalPackageObject);

            HibernateFactory.doWithoutAutoFlushing(() -> {
                if (ovalPackageEvrStateEntity != null) {
                    ovalPackageState.setPackageEvrState(ovalPackageEvrStateEntity);
                    instance.saveObject(ovalPackageEvrStateEntity, false);
                }

                if (ovalPackageArchStateEntity != null) {
                    ovalPackageState.setPackageArchState(ovalPackageArchStateEntity);
                    instance.saveObject(ovalPackageArchStateEntity, false);
                }

                if (ovalPackageVersionStateEntity != null) {
                    ovalPackageState.setPackageVersionState(ovalPackageVersionStateEntity);
                    instance.saveObject(ovalPackageVersionStateEntity, false);
                }

                getSession().merge(ovalPackageState);
                getSession().merge(ovalPackageObject);
                getSession().merge(ovalPackageTest);
            });

            if (i % 60 == 0) {
                LOG.error("Saving '{}'", testType.getId());
                getSession().flush();
            }
        }
    }

    public static OVALDefinition lookupDefinitionById(String id) {
        return getSession().byId(OVALDefinition.class).load(id);
    }

    private OVALPackageObject lookupOrInsetPackageObject(OVALPackageObject ovalPackageObject) {
        OVALPackageObject lookup = lookupPackageObjectById(ovalPackageObject.getId());
        if (lookup == null) {
            instance.saveObject(ovalPackageObject);

            return ovalPackageObject;
        }

        return lookup;
    }

    private OVALPackageTest lookupOrInsetPackageTest(OVALPackageTest ovalPackageTest) {
        OVALPackageTest lookup = lookupPackageTestById(ovalPackageTest.getId());
        if (lookup == null) {
            instance.saveObject(ovalPackageTest);
            return ovalPackageTest;
        }

        return lookup;
    }

    private OVALPackageState lookupOrInsetPackageState(OVALPackageState ovalPackageState) {
        OVALPackageState lookup = lookupPackageStateById(ovalPackageState.getId());
        if (lookup == null) {
            instance.saveObject(ovalPackageState);

            return ovalPackageState;
        }

        return lookup;
    }

    public static void savePackageTest(OVALPackageTest pkgTest) {

    }

    /**
     *
     * */
    public static void savePackageState(OVALPackageState pkgState) {

    }

    public static void savePackageObject(OVALPackageObject pkgObject) {

    }

    public static OVALPackageTest lookupPackageTestById(String id) {
        return getSession().byId(OVALPackageTest.class).load(id);
    }

    /**
     *
     * */
    public static OVALPackageState lookupPackageStateById(String id) {
        return getSession().byId(OVALPackageState.class).load(id);
    }

    public static OVALPackageObject lookupPackageObjectById(String id) {
        return getSession().byId(OVALPackageObject.class).load(id);
    }

    public static List<OVALPlatform> getPlatformsAffectedByCve(String cve) {
        return getSession()
                .createNamedQuery("OVALPlatform.getPlatformsAffectedByCve", OVALPlatform.class)
                .setParameter("cve", cve)
                .getResultList();
    }

    public static List<OVALPlatform> lookupPlatformsAffectedByDefinition(String defId) {
        return getSession()
                .createNamedQuery("OVALPlatform.getPlatformsAffectedByDefinition", OVALPlatform.class)
                .setParameter("defId", defId)
                .getResultList();
    }

    public static Optional<OVALDefinition> lookupVulnerabilityDefinitionByCve(String cve) {
        return getSession()
                .createNamedQuery("OVALDefinition.getVulnerabilityDefinitionByCve", OVALDefinition.class)
                .setParameter("cve", cve)
                .uniqueResultOptional();
    }

    public static List<OVALVulnerablePackage> lookupVulnerablePackagesByPlatformAndCve(long platformId, String cve) {
        return getSession()
                .createNamedQuery("OVALVulnerablePackage.lookupVulnerablePackagesByPlatformAndCve", OVALVulnerablePackage.class)
                .setParameter("platformId", platformId)
                .setParameter("cve", cve)
                .getResultList();
    }

    public static void assignVulnerablePackageToPlatform(String platformName, String cveName, String pkgName, String pkgFixVersion) {
        OVALPlatform platform = lookupPlatformByCpe(platformName);
        Cve cve = CveFactory.lookupByName(cveName);
        OVALVulnerablePackage vulnerablePkg = lookupOrInsertVulnerablePackage(pkgName, pkgFixVersion);

        OVALPlatformVulnerablePackage platformVulnerablePkg = new OVALPlatformVulnerablePackage();
        platformVulnerablePkg.setPlatform(platform);
        platformVulnerablePkg.setCve(cve);
        platformVulnerablePkg.setVulnerablePackage(vulnerablePkg);

        instance.saveObject(platformVulnerablePkg);
    }

    public static OVALVulnerablePackage lookupOrInsertVulnerablePackage(String pkgName, String fixVersion) {
        OVALVulnerablePackage vulnerablePackage = lookupVulnerablePackageByNameAndFixVersion(pkgName, fixVersion);
        if (vulnerablePackage != null) {
            return vulnerablePackage;
        } else {
            OVALVulnerablePackage newVulnerablePackage = new OVALVulnerablePackage();
            newVulnerablePackage.setName(pkgName);
            newVulnerablePackage.setFixVersion(fixVersion);

            instance.saveObject(newVulnerablePackage);

            return newVulnerablePackage;
        }
    }

    public static OVALVulnerablePackage lookupVulnerablePackageByNameAndFixVersion(String pkgName, String fixVersion) {
        Session session = HibernateFactory.getSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();

        CriteriaQuery<OVALVulnerablePackage> criteriaQuery = builder.createQuery(OVALVulnerablePackage.class);
        Root<OVALVulnerablePackage> root = criteriaQuery.from(OVALVulnerablePackage.class);

        criteriaQuery.where(builder.and(
                        builder.equal(root.get("name"), pkgName),
                        builder.equal(root.get("fixVersion"), fixVersion)
                )
        );

        return session.createQuery(criteriaQuery).uniqueResult();
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    private static String extractCveFromDefinition(OVALDefinition definition) {
        OVALDefinitionSource source = definition.getSource();
        if (source == null) {
            throw new IllegalStateException("Definition doesn't have a source property");
        }
        switch (source) {
            case openSUSE_LEAP:
            case SUSE_LINUX_ENTERPRISE_SERVER:
            case SUSE_LINUX_ENTERPRISE_DESKTOP:
            case openSUSE:
                return definition.getTitle();
            case DEBIAN:
                return definition.getTitle().split("\\s+")[0];
            case REDHAT_ENTERPRISE_LINUX:
            case UBUNTU:
                throw new NotImplementedException("Cannot extract cve from '" + source + "' OVAL definitions");
        }
        return "";
    }
}
