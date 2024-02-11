/*
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
import com.redhat.rhn.common.util.StringUtil;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.VHMCredentials;
import com.redhat.rhn.domain.org.Org;

import com.suse.manager.gatherer.GathererRunner;
import com.suse.manager.model.gatherer.GathererModule;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 * Singleton representing Virtual Host Manager hibernate factory.
 */
public class VirtualHostManagerFactory extends HibernateFactory {

    public static final String KUBECONFIG_PATH_BASE = "/srv/susemanager/virt_host_mgr";
    public static final String KUBERNETES = "Kubernetes";

    private static VirtualHostManagerFactory instance;
    private static final Logger LOG = LogManager.getLogger(VirtualHostManagerFactory.class);

    /**
     * Name of parameter specifying username in Virtual Host Manager Config
     */
    public static final String CONFIG_USER = "username";
    public static final String CONFIG_PASS = "password";
    public static final String CONFIG_KUBECONFIG = "kubeconfig";

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
        return LOG;
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
     * Looks up VirtualHostManager by id
     * @param id the id
     * @return VirtualHostManager object with given label or null if such object doesn't
     * exist
     */
    public Optional<VirtualHostManager> lookupById(Long id) {
        return Optional.ofNullable(getSession().get(VirtualHostManager.class, id));
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
     * Lookup multiple virtual host managers by an id list and organization
     * @param ids virtual host manager id list
     * @param org the organization
     * @return Returns a list of virtual host managers with the given ids if it exists
     * inside the organization
     */
    public List<VirtualHostManager> lookupByIdsAndOrg(List<Long> ids, Org org) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<VirtualHostManager> criteria =
                builder.createQuery(VirtualHostManager.class);
        Root<VirtualHostManager> root = criteria.from(VirtualHostManager.class);
        criteria.where(builder.and(
                root.get("id").in(ids),
                builder.equal(root.get("org"), org)));
        return getSession().createQuery(criteria).getResultList();
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
        getLogger().debug("Deleting VirtualHostManager {}", virtualHostManager);
        if (KUBERNETES.equalsIgnoreCase(virtualHostManager.getGathererModule())) {
            cleanupOnDeleteKuberentes(virtualHostManager);
        }
        removeObject(virtualHostManager);
    }

    private void cleanupOnDeleteKuberentes(VirtualHostManager virtualHostManager) {
        // remove kubeconfig file
        String kubeconfig = kubeconfigPath(virtualHostManager.getId(),
                virtualHostManager.getOrg());
        try {
            Files.delete(Paths.get(kubeconfig));
        }
        catch (IOException e) {
            LOG.error("Could not remove Kubernetes config file: {}", kubeconfig);
        }
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
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Creating VirtualHostManager with label '{}'.", StringUtil.sanitizeLogInput(label));
        }

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
     * Creates a new VirtualHostManager entity from given arguments
     * @param virtualHostManager - entity to update
     * @param label - nonempty label
     * @param parameters - non-null map with additional gatherer parameters
     */
    public void updateVirtualHostManager(
            VirtualHostManager virtualHostManager,
            String label,
            Map<String, String> parameters) {
        getLogger().debug("Update VirtualHostManager with id '{}'.", virtualHostManager.getId());

        virtualHostManager.setLabel(label);
        if (StringUtils.isNotBlank(parameters.get(CONFIG_PASS))) {
            virtualHostManager.getCredentials().setPassword(parameters.get(CONFIG_PASS));
        }
        if (StringUtils.isNotBlank(parameters.get(CONFIG_USER))) {
            virtualHostManager.getCredentials().setUsername(parameters.get(CONFIG_USER));
        }

        virtualHostManager.getConfigs().clear();
        virtualHostManager.getConfigs().addAll(
                createVirtualHostManagerConfigs(virtualHostManager, parameters));
        save(virtualHostManager);
    }

    /**
     * Creates a Kubernetes Virtual Host Manager.
     * @param label the label
     * @param org the organization
     * @param context the selected context from the kubeconfig file
     * @param kubeconfigIn the kubeconfig file
     * @return the new Virtual Host Manager
     * @throws IOException in case of IO errors
     */
    public VirtualHostManager createKuberntesVirtualHostManager(
            String label,
            Org org,
            String context,
            InputStream kubeconfigIn) throws IOException {
        // ensure we have the base directory
        Path kubeconfigDir = Paths.get(KUBECONFIG_PATH_BASE);
        if (!Files.isDirectory(kubeconfigDir)) {
            Files.createDirectory(kubeconfigDir);
        }

        Map<String, String> params = new HashMap<>();
        params.put("context", context);

        VirtualHostManager vhm = createVirtualHostManager(
                label,
                org,
                KUBERNETES,
                params
            );
        save(vhm);
        String kubeconfigPath = kubeconfigPath(vhm.getId(), org);
        try (FileOutputStream kubeconfigOut = new FileOutputStream(kubeconfigPath)) {
            IOUtils.copy(kubeconfigIn, kubeconfigOut);
        }

        vhm.getConfigs().add(createVirtualHostManagerConfig(
                vhm,
                CONFIG_KUBECONFIG,
                kubeconfigPath));

        return vhm;
    }

    /**
     * Updates the given Virtual Host Manager.
     * @param vhm the Virtual Host Manager to update
     * @param label the label
     * @param context the context
     * @param kubeconfigInOpt the kubeconfig file
     * @throws IOException in case of IO errors
     */
    public void updateKuberntesVirtualHostManager(
            VirtualHostManager vhm,
            String label,
            String context,
            Optional<InputStream> kubeconfigInOpt) throws IOException {
        // ensure we have the base directory
        Path kubeconfigDir = Paths.get(KUBECONFIG_PATH_BASE);
        if (!Files.isDirectory(kubeconfigDir)) {
            Files.createDirectory(kubeconfigDir);
        }

        String kubeconfigPath = kubeconfigPath(vhm.getId(), vhm.getOrg());
        if (kubeconfigInOpt.isPresent()) {
            try (FileOutputStream kubeconfigOut = new FileOutputStream(kubeconfigPath)) {
                IOUtils.copy(kubeconfigInOpt.get(), kubeconfigOut);
            }
        }

        Map<String, String> params = new HashMap<>();
        params.put("context", context);
        params.put("kubeconfig", kubeconfigPath);

        updateVirtualHostManager(vhm, label, params);
    }

    private String kubeconfigPath(long vhmId, Org org) {
        return KUBECONFIG_PATH_BASE + "/" + org.getId() + "_" + vhmId + "_kubeconfig";
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
     * @param ignoreParams - params to ignore
     * @return false if the module name or configuration is not valid
     */
    public boolean isConfigurationValid(String moduleName, Map<String, String> parameters,
                                        String... ignoreParams) {
        Map<String, GathererModule> modules = new GathererRunner().listModules();
        return isConfigurationValid(moduleName, parameters, modules, ignoreParams);
    }

    /**
     * Validate gatherer module configuration. Check for:
     *  - existence of given gatherer module
     *  - existence of required parameters for given gatherer module
     * @param moduleName - gatherer module name
     * @param parameters - non-null map with gatherer parameters
     * @param ignoreParams - params to ignore
     * @param modules - map containing {@link GathererModule}
     * @return false if the module name or configuration is not valid
     */
    public boolean isConfigurationValid(String moduleName, Map<String, String> parameters,
                                        Map<String, GathererModule> modules,
                                        String... ignoreParams) {
        Optional<GathererModule> details = modules.entrySet().stream()
                .filter(entry -> moduleName.equalsIgnoreCase(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst();
        if (!details.isPresent()) {
            return false;
        }

        Set<String> parameterNames = details.get().getParameters().keySet();
        List<String> ignoredParams = Arrays.asList(ignoreParams);
        return parameterNames.stream()
                .filter(name -> !ignoredParams.contains(name))
                .allMatch(n -> isNotEmpty(parameters.get(n)));
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
    private VHMCredentials createCredentialsFromParams(Map<String, String> params) {
        String username = params.get(CONFIG_USER);
        if (StringUtils.isBlank(username)) {
            return null;
        }

        return CredentialsFactory.createVHMCredentials(username, params.get(CONFIG_PASS));
    }

    /**
     * @param identifier node identifier
     * @return the node with the given identifier or null
     */
    public Optional<VirtualHostManagerNodeInfo> lookupNodeInfoByIdentifier(
            String identifier) {
        VirtualHostManagerNodeInfo result = (VirtualHostManagerNodeInfo) getSession()
                .createCriteria(VirtualHostManagerNodeInfo.class)
                .add(Restrictions.eq("identifier", identifier))
                .uniqueResult();

        return Optional.ofNullable(result);
    }

}
