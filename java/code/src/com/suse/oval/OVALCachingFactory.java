package com.suse.oval;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.errata.Cve;
import com.redhat.rhn.domain.errata.CveFactory;
import com.suse.oval.db.*;
import com.suse.oval.ovaltypes.AffectedType;
import com.suse.oval.ovaltypes.DefinitionType;
import com.suse.oval.ovaltypes.ReferenceType;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

            OVALDefinition definition = new OVALDefinition();
            definition.setId(definitionType.getId());
            definition.setTitle(definitionType.getMetadata().getTitle());
            definition.setDefClass(definitionType.getDefinitionClass());
            definition.setDescription(definitionType.getMetadata().getDescription());
            definition.setCriteriaTree(definitionType.getCriteria());

            definition.setSource(source);

            List<String> affectedPlatforms = definitionType.getMetadata().getAffected()
                    .stream().map(AffectedType::getPlatforms).flatMap(List::stream)
                    .collect(Collectors.toList());

            saveDefinition(definition, affectedPlatforms, definitionType.getMetadata().getReference());

            if (i % 60 == 0) {
                LOG.error(definitionType.getId());
                getSession().clear();
                getSession().flush();
            }
        }
    }


    private static void saveDefinition(OVALDefinition definition, List<String> affectedPlatforms, List<ReferenceType> references) {
        definition.setAffectedPlatforms(
                affectedPlatforms.stream()
                        .map(OVALCachingFactory::lookupOrInsertPlatformByName)
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

    private static Map<String, OVALPlatform> platformsMap = new HashMap<>(100);

    /**
     * Looks up an {@link OVALPlatform} or inserts it if it does not exist.
     *
     * @param name name of the platform
     * @return the platform
     */
    public static OVALPlatform lookupOrInsertPlatformByName(String name) {
        if (platformsMap.containsKey(name)) {
            return platformsMap.get(name);
        }

        OVALPlatform platform = lookupPlatformByName(name);
        if (platform == null) {
            OVALPlatform newPlatform = new OVALPlatform();
            newPlatform.setName(name);
            instance.saveObject(newPlatform);

            platform = newPlatform;
        }

        platformsMap.put(name, platform);

        return platform;

    }

    public static OVALPlatform lookupPlatformByName(String name) {
        Session session = HibernateFactory.getSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();

        CriteriaQuery<OVALPlatform> criteriaQuery = builder.createQuery(OVALPlatform.class);
        Root<OVALPlatform> root = criteriaQuery.from(OVALPlatform.class);

        criteriaQuery.where(builder.equal(root.get("name"), name));

        return session.createQuery(criteriaQuery).uniqueResult();
    }

    public static void savePackageTest(OVALPackageTest pkgTest) {
        instance.saveObject(pkgTest);
    }

    public static void savePackageState(OVALPackageState pkgState) {
        instance.saveObject(pkgState);
    }

    public static void savePackageObject(OVALPackageObject pkgObject) {
        instance.saveObject(pkgObject);
    }

    public static OVALPackageTest lookupPackageTestById(String id) {
        return getSession().byId(OVALPackageTest.class).load(id);
    }

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
        OVALPlatform platform = lookupPlatformByName(platformName);
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
            case SUSE:
                return definition.getTitle();
            case DEBIAN:
            case REDHAT:
            case UBUNTU:
                throw new NotImplementedException("Cannot extract cve from '" + source + "' OVAL definitions");
        }
        return "";
    }
}
