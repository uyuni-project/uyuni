package com.redhat.rhn.domain.server.virtualhostmanager;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.org.Org;
import org.apache.log4j.Logger;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.criterion.Restrictions;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Singletor representing Virtual Host Manager hibernate factory.
 */
public class VirtualHostManagerFactory extends HibernateFactory {

    private static VirtualHostManagerFactory instance;
    private static Logger log;

    private final static String CONFIG_USER = "user";
    private final static String CONFIG_PASS = "pass";

    /**
     * Default constructor.
     */
    private VirtualHostManagerFactory() {
        super();
    }

    /**
     * Gets instance of VirtualHostManagerFactory
     * @return
     */
    public static synchronized VirtualHostManagerFactory getInstance() {
        if (instance == null) {
            instance = new VirtualHostManagerFactory();
        }
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Logger getLogger() {
        if (log == null) {
            log = Logger.getLogger(VirtualHostManagerFactory.class);
        }
        return log;
    }

    /**
     * Looks up VirtualHostManager by label
     * @param label
     * @return VirtualHostManager instance with given label
     */
    public VirtualHostManager lookupByLabel(String label) {
        VirtualHostManager result = (VirtualHostManager) getSession()
                .createCriteria(VirtualHostManager.class)
                .add(Restrictions.eq("label", label))
                .uniqueResult();

        if (result == null) {
            throw new ObjectNotFoundException(VirtualHostManager.class,
                    "Virtual Host Manager with label '" + label + "' not found.");
        }

        return result;
    }

    /**
     * Creates a new VirtualHostManager entity from given arguments
     * @param label
     * @param org
     * @param moduleName
     * @param parameters
     * @return new VirtualHostManager instance
     */
    public VirtualHostManager createVirtualHostManager(
            String label,
            Org org,
            String moduleName,
            Map<String, String> parameters) {

        VirtualHostManager virtualHostManager = new VirtualHostManager();
        virtualHostManager.setLabel(label);
        virtualHostManager.setOrg(org);

        // todo validation, need list of valid modules
        virtualHostManager.setGathererModule(moduleName);
        virtualHostManager.setCredentials(createCredentialsFromParams(parameters));
        virtualHostManager.setConfigs(createVirtualHostManagerConfigs(virtualHostManager, parameters));

        saveObject(virtualHostManager);

        return virtualHostManager;
    }

    /**
     * Helper method for creating VirtualHostManager configs from given key-value
     * parameters.
     * Important note: this method filters out parameters corresponding to username and
     * password as these are handled separately.
     *
     * @param virtualHostManager for which the config shall be created
     * @param parameters input parameters
     * @return set of VirtualHostManagerConfig instances corresponding to input parameters
     */
    private Set<VirtualHostManagerConfig> createVirtualHostManagerConfigs(
            VirtualHostManager virtualHostManager,
            Map<String, String> parameters) {
        if (parameters == null) {
            return null;
        }

        Set<VirtualHostManagerConfig> configs = new HashSet<>();

        for (Map.Entry<String, String> configEntry : parameters.entrySet()) {
            if (configEntry.equals(CONFIG_USER) || configEntry.equals(CONFIG_PASS)) {
                continue;
            }
            configs.add(createVirtualHostManagerConfig(virtualHostManager, configEntry.getKey(), configEntry.getValue()));
        }

        return configs;
    }

    /**
     * Helper method for creating a VirtualHostManagerConfig instance from given
     * VirtualHostManager, parameter and its value.
     * @param virtualHostManager
     * @param param
     * @param value
     * @return VirtualHostManagerConfig instance created from arguments
     */
    private VirtualHostManagerConfig createVirtualHostManagerConfig(
            VirtualHostManager virtualHostManager,
            String param, String value) {
        VirtualHostManagerConfig config = new VirtualHostManagerConfig();
        config.setVirtualHostManager(virtualHostManager);
        config.setParameter(param);
        config.setValue(value);

        return config;
    }

    /**
     * Creates and stores db entity for credentials if the input params contain
     * entry for username and password.
     * Important note: This method removes credentials data from input params!
     * @param params
     * @return - new Credentials instance
     *           if input params contain entry for username and password
     *         - null otherwise
     */
    private Credentials createCredentialsFromParams(Map<String, String> params) {
        if (params != null && params.containsKey(CONFIG_USER) && params.containsKey(CONFIG_PASS)) {
            Credentials credentials = CredentialsFactory.createVHMCredentials();
            credentials.setUsername(params.get(CONFIG_USER));
            credentials.setPassword(params.get(CONFIG_PASS));
            CredentialsFactory.storeCredentials(credentials);

            // filter creds from the params, as they are stored separately
            params.remove(CONFIG_USER);
            params.remove(CONFIG_PASS);

            return credentials;
        }

        return null;
    }
}
