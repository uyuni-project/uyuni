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

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.org.Org;

import com.suse.manager.gatherer.GathererRunner;
import com.suse.manager.model.gatherer.GathererModule;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Singleton representing Virtual Host Manager hibernate factory.
 */
public class VirtualHostManagerFactory extends HibernateFactory {

    private static VirtualHostManagerFactory instance;
    private static Logger log;

    /**
     * Name of parameter specifying username in Virtual Host Manager Config
     */
    public static final String CONFIG_USER = "username";
    private static final String CONFIG_PASS = "password";
    private static final List<String> CONFIGS_TO_SKIP = Arrays.asList(
            new String[] {CONFIG_USER, CONFIG_PASS, "id", "module"});

    /**
     * Default constructor.
     * (public for testing reasons so that we can override it in tests)
     */
    public VirtualHostManagerFactory() {
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
     * @return VirtualHostManager object with given label or null if such object doesn't
     * exist
     */
    public VirtualHostManager lookupByLabel(String label) {
        return (VirtualHostManager) getSession()
                .createCriteria(VirtualHostManager.class)
                .add(Restrictions.eq("label", label))
                .uniqueResult();
    }

    /**
     * Looks up VirtualHostManager by id and Org
     * @param id the id
     * @param org the organization
     * @return VirtualHostManager object with given label or null if such object doesn't
     * exist
     */
    public VirtualHostManager lookupByIdAndOrg(Long id, Org org) {
        return (VirtualHostManager) getSession()
                .createCriteria(VirtualHostManager.class)
                .add(Restrictions.eq("org", org))
                .add(Restrictions.eq("id", id))
                .uniqueResult();
    }

    /**
     * Looks up VirtualHostManager by label and Org
     * @param label the label
     * @param org the organization
     * @return VirtualHostManager object with given label or null if such object doesn't
     * exist
     */
    public VirtualHostManager lookupByLabelAndOrg(String label, Org org) {
        return (VirtualHostManager) getSession()
                .createCriteria(VirtualHostManager.class)
                .add(Restrictions.eq("org", org))
                .add(Restrictions.eq("label", label))
                .uniqueResult();
    }

    /**
     * Returns a list of Virtual Host Managers associated with the given organization
     * @param org - organization
     * @return a list of corresponding Virtual Host Managers
     */
    @SuppressWarnings("unchecked")
    public List<VirtualHostManager> listVirtualHostManagers(Org org) {
        return getSession()
                .createCriteria(VirtualHostManager.class)
                .add(Restrictions.eq("org", org))
                .addOrder(Order.asc("label"))
                .list();
    }

    /**
     * Returns a list of all Virtual Host Managers
     * @return list of all Virtual Host Managers
     */
    @SuppressWarnings("unchecked")
    public List<VirtualHostManager> listVirtualHostManagers() {
        return getSession()
                .createCriteria(VirtualHostManager.class)
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
     * @param label - nonempty label
     * @param org - non-null organization
     * @param moduleName - nonempty module name
     * @param parameters - non-null map with additional gatherer parameters
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
        virtualHostManager.setGathererModule(moduleName);
        virtualHostManager.setCredentials(createCredentialsFromParams(parameters));
        virtualHostManager.setConfigs(
                createVirtualHostManagerConfigs(virtualHostManager, parameters));

        return virtualHostManager;
    }

    /**
     * Saves a Virtual Host Manager.
     *
     * @param virtualHostManager the Virtual Host Manager
     */
    public void save(VirtualHostManager virtualHostManager) {
        saveObject(virtualHostManager);
    }

    /**
     * Validate gatherer module configuration. Check for:
     *  - existence of given gatherer module
     *  - existence of required parameters for given gatherer module
     * @param moduleName - gatherer module name
     * @param parameters - non-null map with gatherer parameters
     * @return false if the module name or configuration is not valid
     */
    public boolean isConfigurationValid(String moduleName, Map<String, String> parameters) {
        Map<String, GathererModule> modules = new GathererRunner().listModules();
        if (!modules.containsKey(moduleName)) {
            return false;
        }

        GathererModule details = modules.get(moduleName);
        Set<String> parameterNames = details.getParameters().keySet();

        return parameterNames.stream().allMatch(n -> isNotEmpty(parameters.get(n)));
    }

    /**
     * Helper method for creating VirtualHostManager configs from given key-value
     * parameters.
     * Important note: this method skips the parameters that shouldn't be stored as
     * configs (module, id, username and password).
     *
     * @param virtualHostManager for which the config shall be created
     * @param parameters input parameters
     * @return set of VirtualHostManagerConfig instances corresponding to input parameters
     */
    private Set<VirtualHostManagerConfig> createVirtualHostManagerConfigs(
            VirtualHostManager virtualHostManager,
            Map<String, String> parameters) {
        Set<VirtualHostManagerConfig> configs = new LinkedHashSet<>();

        for (Map.Entry<String, String> configEntry : parameters.entrySet()) {
            String key = configEntry.getKey();
            if (CONFIGS_TO_SKIP.contains(key)) {
                continue;
            }
            configs.add(createVirtualHostManagerConfig(virtualHostManager,
                    key,
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
     * Creates a db entity for credentials if the input params contain
     * entry for username and password.
     * @param params - non-null map of gatherer parameters
     * @return new Credentials instance
     */
    private Credentials createCredentialsFromParams(Map<String, String> params) {
        String username = params.get(CONFIG_USER);
        if (username == null) {
            return null;
        }

        Credentials credentials = CredentialsFactory.createVHMCredentials();
        credentials.setUsername(username);
        credentials.setPassword(params.get(CONFIG_PASS));
        credentials.setModified(new Date());

        return credentials;
    }
}
