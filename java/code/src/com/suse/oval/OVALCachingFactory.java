package com.suse.oval;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.suse.oval.db.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

public class OVALCachingFactory extends HibernateFactory {
    private static final Logger LOG = LogManager.getLogger(OVALCachingFactory.class);
    private static OVALCachingFactory instance = new OVALCachingFactory();

    private OVALCachingFactory() {
        // Left empty on purpose
    }

    /**
     * Insert the passed definition object into the database.
     * <p>
     * Also insert the affected platforms information (if not already inserted) into the relevant tables
     */
    public static void saveDefinition(OVALDefinition definition) {
        instance.saveObject(definition);
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

    public static OVALPackageTest getPackageTestById(String id) {
        return null;
    }

    public static OVALPackageState getPackageStateById(String id) {
        return null;
    }

    public static OVALPackageObject getPackageObjectById(String id) {
        return null;
    }

    public static List<OVALPlatform> getPlatformsAffectedByCve(String cve) {
        return null;
    }

    public static Optional<OVALDefinition> getVulnerabilityDefinitionByCve(String cve) {
        return getSession()
                .createNamedQuery("OVALDefinition.getVulnerabilityDefinitionByCve", OVALDefinition.class)
                .setParameter("cve", cve)
                .uniqueResultOptional();
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
