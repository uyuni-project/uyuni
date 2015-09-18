/**
 * Copyright (c) 2015 SUSE LLC
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

package com.redhat.rhn.domain.server.virtualhostmanager;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.org.Org;
import com.suse.manager.gatherer.GathererCache;
import org.apache.log4j.Logger;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.criterion.Restrictions;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Singleton representing Virtual Host Manager hibernate factory.
 */
public class VirtualHostManagerFactory extends HibernateFactory {

    private static VirtualHostManagerFactory instance;
    private static Logger log;

    private static final String CONFIG_USER = "user";
    private static final String CONFIG_PASS = "pass";

    /**
     * Default constructor.
     * (package local for testing reasons)
     */
    VirtualHostManagerFactory() {
        super();
    }

    /**
     * Gets instance of VirtualHostManagerFactory
     * @return instance of VirtualHostManagerFactory
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
     * @param label the label
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
     * Returns a list of Virtual Host Managers associated with the given organization
     * @param org - organization
     * @return a list of corresponding Virtual Host Managers
     */
    public List<VirtualHostManager> listVirtualHostManagers(Org org) {
        return getSession()
                .createCriteria(VirtualHostManager.class)
                .add(Restrictions.eq("org", org))
                .list();
    }

    /**
     * Deletes given VirtualHostManager and associated objects.
     * @param virtualHostManager to be deleted
     */
    public void delete(VirtualHostManager virtualHostManager) {
        getLogger().debug("Deleting VirtualHostManager " + virtualHostManager);
        removeObject(virtualHostManager);
    }

    /**
     * Creates a new VirtualHostManager entity from given arguments
     * @param label - the label
     * @param org - the organization
     * @param moduleName - the module name
     * @param parameters - the parameters
     * @return new VirtualHostManager instance
     */
    public VirtualHostManager createVirtualHostManager(
            String label,
            Org org,
            String moduleName,
            Map<String, String> parameters) {
        getLogger().debug("Creating VirtualHostManager with label '" + label + "'.");

        VirtualHostManager virtualHostManager = new VirtualHostManager();
        virtualHostManager.setLabel(label);
        virtualHostManager.setOrg(org);

        if (!getAvailableGathererModules().contains(moduleName)) {
            throw new IllegalArgumentException("Module '" + moduleName + "' not available");
        }
        virtualHostManager.setGathererModule(moduleName);
        virtualHostManager.setCredentials(createCredentialsFromParams(parameters));
        virtualHostManager.setConfigs(
                createVirtualHostManagerConfigs(virtualHostManager, parameters));

        saveObject(virtualHostManager);

        return virtualHostManager;
    }

    /**
     * Get list of avaliable gatherer modules
     * (package protected for testing reasons)
     * @return list of available gatherer modules
     */
    Set<String> getAvailableGathererModules() {
        return GathererCache.INSTANCE.listAvailableModules();
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
            configs.add(createVirtualHostManagerConfig(virtualHostManager,
                    configEntry.getKey(),
                    configEntry.getValue()));
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
        if (params != null &&
                params.containsKey(CONFIG_USER) &&
                params.containsKey(CONFIG_PASS)) {
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
