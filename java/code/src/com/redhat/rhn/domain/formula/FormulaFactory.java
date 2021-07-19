/**
 * Copyright (c) 2016--2019 SUSE LLC
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
package com.redhat.rhn.domain.formula;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.suse.manager.clusters.ClusterFactory;
import com.suse.manager.model.clusters.Cluster;
import com.suse.manager.webui.controllers.ECMAScriptDateAdapter;
import com.suse.utils.Opt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Factory class for working with formulas.
 */
public class FormulaFactory {

    /** This formula is coupled with the monitoring system type */
    public static final String PROMETHEUS_EXPORTERS = "prometheus-exporters";

    // Logger for this class
    private static final Logger LOG = Logger.getLogger(FormulaFactory.class);

    private static String dataDir = "/srv/susemanager/formula_data/";
    private static String metadataDirManager = "/usr/share/susemanager/formulas/metadata/";
    private static final String METADATA_DIR_STANDALONE_SALT = "/usr/share/salt-formulas/metadata/";
    private static final String METADATA_DIR_CUSTOM = "/srv/formula_metadata/";
    private static final String PILLAR_DIR = "pillar/";
    private static final String GROUP_PILLAR_DIR = "group_pillar/";
    private static final String GROUP_DATA_FILE  = "group_formulas.json";
    private static final String SERVER_DATA_FILE = "minion_formulas.json";
    private static final String ORDER_FILE = "formula_order.json";
    private static final String LAYOUT_FILE = "form.yml";
    private static final String METADATA_FILE = "metadata.yml";
    private static final String PILLAR_EXAMPLE_FILE = "pillar.example";
    private static final String PILLAR_FILE_EXTENSION = "json";
    private static final String METADATA_DIR_CLUSTER_PROVIDERS = "/usr/share/susemanager/cluster-providers/metadata/";

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Date.class, new ECMAScriptDateAdapter())
            .registerTypeAdapter(Double.class,  new JsonSerializer<Double>() {
                @Override
                public JsonElement serialize(Double src, Type type,
                            JsonSerializationContext context) {
                        if (src % 1 == 0) {
                            return new JsonPrimitive(src.intValue());
                        }
                        else {
                            return new JsonPrimitive(src);
                        }
                    }
                })
            .serializeNulls()
            .create();
    private static final Yaml YAML = new Yaml(new SafeConstructor());

    private static SystemEntitlementManager systemEntitlementManager = GlobalInstanceHolder.SYSTEM_ENTITLEMENT_MANAGER;

    private FormulaFactory() { }

    /**
     * Setter for data directory, used for testing.
     * @param dataDirPath base path to where store files
     */
    public static void setDataDir(String dataDirPath) {
        FormulaFactory.dataDir = dataDirPath.endsWith(File.separator) ? dataDirPath : dataDirPath + File.separator;
    }

    /**
     * Setter for metadata directory, used for testing.
     * @param metadataDirPath base path where to read metadata files from
     */
    public static void setMetadataDirOfficial(String metadataDirPath) {
        FormulaFactory.metadataDirManager =
                metadataDirPath.endsWith(File.separator) ? metadataDirPath : metadataDirPath + File.separator;
    }

    /**
     * Getter for directory path of group pillar
     * @return group pillar directory
     */
    public static String getGroupPillarDir() {
        return dataDir + GROUP_PILLAR_DIR;
    }

    /**
     * Getter for directory path of system pillar
     * @return system pillar directory
     */
    public static String getPillarDir() {
        return dataDir + PILLAR_DIR;
    }

    /**
     * Getter for path of group data file
     * @return group data file
     */
    public static String getGroupDataFile() {
        return dataDir + GROUP_DATA_FILE;
    }

    /**
     * Getter for path of server data file
     * @return server data file
     */
    public static String getServerDataFile() {
        return dataDir + SERVER_DATA_FILE;
    }

    /**
     * Getter for path of order data file
     * @return order data file
     */
    public static String getOrderDataFile() {
        return dataDir + ORDER_FILE;
    }

    /**
     * Return a warning message in case some folder doesn't exist or have wrong access level.
     * @return a warning message if cannot access one folder. NULL if all folder are ok.
     */
    public static String getWarningMessageAccessFormulaFolders() {
        String message = "";
        boolean error = false;
        if (!new File(METADATA_DIR_STANDALONE_SALT).canRead()) {
            message += (error ? " and '" : " '") + METADATA_DIR_STANDALONE_SALT + "'";
            error = true;
        }
        if (!new File(metadataDirManager).canRead()) {
            message += (error ? " and '" : " '") + metadataDirManager + "'";
            error = true;
        }
        if (!new File(METADATA_DIR_CUSTOM).canRead()) {
            message += (error ? " and '" : " '") + METADATA_DIR_CUSTOM + "'";
            error = true;
        }
        return error ? new ValidatorError("formula.folders.unreachable", message).getLocalizedMessage() : null;
    }

    /**
     * Returns the list of the names of all currently installed formulas.
     * @return the names of all currently installed formulas.
     */
    public static List<String> listFormulaNames() {
        File standaloneDir = new File(METADATA_DIR_STANDALONE_SALT);
        File managerDir = new File(metadataDirManager);
        File customDir = new File(METADATA_DIR_CUSTOM);
        List<File> files = new LinkedList<>();
        files.addAll(getFormulasFiles(standaloneDir));
        files.addAll(getFormulasFiles(managerDir));
        files.addAll(getFormulasFiles(customDir));
        List<String> formulasList = new LinkedList<>();

        for (File f : files) {
            if (f.isDirectory() && new File(f, LAYOUT_FILE).isFile()) {
                if (!formulasList.contains(f.getName())) {
                    formulasList.add(f.getName());
                }
            }
        }
        formulasList.sort(String.CASE_INSENSITIVE_ORDER);
        return FormulaFactory.orderFormulas(formulasList);
    }

    private static List<File> getFormulasFiles(File formulasFolder) {
        return Optional.ofNullable(formulasFolder.listFiles())
                .map(filesList -> Arrays.asList(filesList))
                .orElseGet(() -> {
                    LOG.error("Unable to read formulas from folder '" + formulasFolder.getAbsolutePath() + "'" +
                            ". Check if it exists and have the correct permissions (755).");
                    return Collections.EMPTY_LIST;
                });
    }

    /**
     * Returns all currently installed formulas.
     * @return a list of all currently installed formulas.
     */
    public static List<Formula> listFormulas() {
        List<Formula> formulas = new LinkedList<>();
        for (String formulaName : listFormulaNames()) {
            Map<String, Object> metadata = getMetadata(formulaName);
            Formula formula = new Formula(formulaName);
            formula.setMetadata(metadata);
            formulas.add(formula);
        }
        return formulas;
    }

    /**
     * Saves the values of a formula for a group.
     * @param formData the values to save
     * @param groupId the id of the group
     * @param formulaName the name of the formula
     * @param org the user's org
     * @throws IOException if an IOException occurs while saving the data
     */
    public static void saveGroupFormulaData(Map<String, Object> formData, Long groupId, Org org,
            String formulaName) throws IOException {
        File file = new File(getGroupPillarDir() +
                groupId + "_" + formulaName + "." + PILLAR_FILE_EXTENSION);
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        catch (FileAlreadyExistsException e) {
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(GSON.toJson(formData));
        }

        if (PROMETHEUS_EXPORTERS.equals(formulaName)) {
            Set<MinionServer> minions = getGroupMinions(groupId, org);
            minions.forEach(minion -> {
                if (!hasMonitoringDataEnabled(formData)) {
                    if (!serverHasMonitoringFormulaEnabled(minion)) {
                        systemEntitlementManager.removeServerEntitlement(minion, EntitlementManager.MONITORING);
                    }
                }
                else {
                    grantMonitoringEntitlement(minion);
                }
            });
        }
    }

    /**
     * Entitle server if it doesn't already have the monitoring entitlement and if it's allowed.
     * @param server the server to entitle
     */
    public static void grantMonitoringEntitlement(Server server) {
        boolean hasEntitlement = SystemManager.hasEntitlement(server.getId(), EntitlementManager.MONITORING);
        if (!hasEntitlement && systemEntitlementManager.canEntitleServer(server, EntitlementManager.MONITORING)) {
            systemEntitlementManager.addEntitlementToServer(server, EntitlementManager.MONITORING);
            return;
        }
        if (LOG.isDebugEnabled() && hasEntitlement) {
            LOG.debug("Server " + server.getName() + " already has monitoring entitlement.");
        }

    }

    /**
     * Saves the values of a formula for a server.
     * @param formData the values to save
     * @param minionId the minionId
     * @param formulaName the name of the formula
     * @throws IOException if an IOException occurs while saving the data
     */
    public static void saveServerFormulaData(Map<String, Object> formData, String minionId, String formulaName)
            throws IOException {
        // Add the monitoring entitlement if at least one of the exporters is enabled
        if (PROMETHEUS_EXPORTERS.equals(formulaName)) {
            MinionServerFactory.findByMinionId(minionId).ifPresent(s -> {
                if (!hasMonitoringDataEnabled(formData)) {
                    if (isMemberOfGroupHavingMonitoring(s)) {
                        // nothing to do here, keep monitoring entitlement and disable formula
                        LOG.debug(String.format("Minion %s is member of group having monitoring enabled." +
                                " Not removing monitoring entitlement.", minionId));
                    }
                    else {
                        systemEntitlementManager.removeServerEntitlement(s, EntitlementManager.MONITORING);
                    }
                }
                else if (!SystemManager.hasEntitlement(s.getId(), EntitlementManager.MONITORING) &&
                        systemEntitlementManager.canEntitleServer(s, EntitlementManager.MONITORING)) {
                    systemEntitlementManager.addEntitlementToServer(s, EntitlementManager.MONITORING);
                }
            });
        }

        File file = new File(getPillarDir() + minionId +
                "_" + formulaName + "." + PILLAR_FILE_EXTENSION);
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        catch (FileAlreadyExistsException e) {
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(GSON.toJson(formData));
        }
    }

    /**
     * Returns the formulas applied to a given group
     * @param groupId the id of the group
     * @return the list of formulas
     */
    public static List<String> getFormulasByGroupId(Long groupId) {
        File serverFile = new File(getGroupDataFile());
        if (!serverFile.exists()) {
            return new LinkedList<>();
        }

        try {
            Map<String, List<String>> serverFormulas =
                    GSON.fromJson(new BufferedReader(new FileReader(serverFile)),
                            Map.class);
            return orderFormulas(serverFormulas.getOrDefault(groupId.toString(),
                    Collections.emptyList()));
        }
        catch (FileNotFoundException e) {
            return new LinkedList<>();
        }
    }

    /**
     * Returns the formulas applied to a given server
     * @param minionId the minion id
     * @return the list of formulas
     */
    public static List<String> getFormulasByMinionId(String minionId) {
        List<String> formulas = new LinkedList<>();
        File serverDataFile = new File(getServerDataFile());
        try {
            Map<String, List<String>> serverFormulas = GSON.fromJson(
                    new BufferedReader(new FileReader(serverDataFile)), Map.class);
            formulas.addAll(serverFormulas.getOrDefault(minionId,
                    Collections.emptyList()));
        }
        catch (FileNotFoundException | UnsupportedOperationException e) {
        }
        return orderFormulas(formulas);
    }

    /**
     * Returns a combination of all formulas applied to a server and
     * all formulas inherited from its groups.
     * @param serverId the id of the server
     * @return the combined list of formulas
     */
    public static List<String> getCombinedFormulasByServerId(Long serverId) {
        List<String> formulas = getFormulasByMinionId(MinionServerFactory.getMinionId(serverId));
        List<String> groupFormulas = getGroupFormulasByServerId(serverId);
        formulas.removeAll(groupFormulas); // Remove duplicates
        formulas.addAll(groupFormulas);
        return formulas;
    }

    /**
     * Returns the formulas that a given server inherits from its groups.
     * @param serverId the id of the server
     * @return the list of formulas
     */
    public static List<String> getGroupFormulasByServerId(Long serverId) {
        List<String> formulas = new LinkedList<>();
        File groupDataFile = new File(getGroupDataFile());
        try {
            Map<String, List<String>> groupFormulas = GSON.fromJson(
                    new BufferedReader(new FileReader(groupDataFile)), Map.class);
            for (ServerGroup group : ServerFactory.lookupById(serverId)
                    .getManagedGroups()) {
                formulas.addAll(groupFormulas.getOrDefault(group.getId().toString(),
                        Collections.emptyList()));
            }
        }
        catch (FileNotFoundException e) {
        }
        return orderFormulas(formulas);
    }

    /**
     * Returns the layout of a given formula
     * @param name the name of the formula
     * @return the layout
     */
    @SuppressWarnings("unchecked")
    public static Optional<Map<String, Object>> getFormulaLayoutByName(String name) {
        String layoutFilePath = name + File.separator + LAYOUT_FILE;
        File layoutFileStandalone = new File(METADATA_DIR_STANDALONE_SALT + layoutFilePath);
        File layoutFileManager = new File(metadataDirManager + layoutFilePath);
        File layoutFileCustom = new File(METADATA_DIR_CUSTOM + layoutFilePath);

        try {
            if (layoutFileStandalone.exists()) {
                return Optional.of((Map<String, Object>) YAML.load(new FileInputStream(layoutFileStandalone)));
            }
            else if (layoutFileManager.exists()) {
                return Optional.of((Map<String, Object>) YAML.load(new FileInputStream(layoutFileManager)));
            }
            else if (layoutFileCustom.exists()) {
                return Optional.of((Map<String, Object>) YAML.load(new FileInputStream(layoutFileCustom)));
            }
            else {
                return Optional.empty();
            }
        }
        catch (FileNotFoundException | YAMLException e) {
            LOG.error("Error loading layout for formula '" + name + "'", e);
            return Optional.empty();
        }
    }

    /**
     * Returns the saved values of a given server for a given formula.
     * @param name the name of the formula
     * @param minionId the minion id
     * @return the saved values or an empty optional if no values were found
     */
    public static Optional<Map<String, Object>> getFormulaValuesByNameAndMinionId(
            String name, String minionId) {
        try {
            File dataFile = new File(getPillarDir() +
                    minionId + "_" + name + "." + PILLAR_FILE_EXTENSION);
            if (dataFile.exists()) {
                return Optional.of((Map<String, Object>) GSON.fromJson(
                        new BufferedReader(new FileReader(dataFile)), Map.class));
            }
            else {
                return Optional.empty();
            }
        }
        catch (FileNotFoundException | UnsupportedOperationException e) {
            return Optional.empty();
        }
    }

    /**
     * Returns the saved values of a group for a given formula.
     * The group is found by a given server, that is a member of that group.
     * @param name the name of the formula
     * @param serverId the id of the server
     * @return the saved values or an empty optional if no values were found
     */
    public static Optional<Map<String, Object>> getGroupFormulaValuesByNameAndServerId(
            String name, Long serverId) {
        for (ServerGroup group : ServerFactory.lookupById(serverId).getManagedGroups()) {
            if (getFormulasByGroupId(group.getId()).contains(name)) {
                return getGroupFormulaValuesByNameAndGroupId(name, group.getId());
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the saved values of a given group for a given formula.
     * @param name the name of the formula
     * @param groupId the id of the group
     * @return the saved values or an empty optional if no values were found
     */
    public static Optional<Map<String, Object>> getGroupFormulaValuesByNameAndGroupId(
            String name, Long groupId) {
        File dataFile = new File(getGroupPillarDir() +
                groupId + "_" + name + "." + PILLAR_FILE_EXTENSION);
        try {
            if (dataFile.exists()) {
                Map<String, Object> data = (Map<String, Object>) GSON.fromJson(
                        new BufferedReader(new FileReader(dataFile)), Map.class);

                if (formulaHasType(name, "cluster-formula")) {
                    // find cluster for this group
                    Optional<Cluster> cluster = ClusterFactory.findClusterByGroupId(groupId);
                    if (cluster.isPresent()) {
                        // load cluster provider metadata and look for the key of this formula
                        Map<String, Object> metadata = getClusterProviderMetadata(cluster.get().getProvider());
                        Map<String, Object> formulas = (Map<String, Object>) metadata.get("formulas");
                        Optional<String> formulaKey = formulas.entrySet().stream()
                                .filter(e -> e.getValue() instanceof Map)
                                .filter(e -> ((Map) e.getValue()).get("name").equals(name))
                                .map(e -> e.getKey())
                                .findFirst();
                        if (formulaKey.isPresent()) {
                            // return values under mgr_clusters:<cluster-name>:<formula-key>
                            return getValueByPath(data,
                                    "mgr_clusters:" + cluster.get().getLabel() + ":" + formulaKey.get())
                                    .filter(Map.class::isInstance)
                                    .map(Map.class::cast);
                        }
                        else {
                            return Optional.empty();
                        }
                    }
                    else {
                        return Optional.empty();
                    }
                }
                else {
                    return Optional.of(data);
                }

            }
            else {
                return Optional.empty();
            }
        }
        catch (FileNotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * Get the name of the formula that corresponds to the key used by the cluster provider.
     * @param clusterProvider the name of the cluster provider
     * @param formulaKey the key of the formula
     * @return the name of the formula
     */
    public static Optional<String> getClusterProviderFormulaName(String clusterProvider, String formulaKey) {
        Map<String, Object> metadata = getClusterProviderMetadata(clusterProvider);
        return getValueByPath(metadata, "formulas:" + formulaKey)
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .filter(data -> !"cluster-provider".equals(data.get("source")))
                .filter(data -> data.containsKey("name"))
                .filter(data -> data.get("name") instanceof String)
                .map(data -> (String)data.get("name"));
    }

    /**
     * Save the selected formulas for a group.
     * This also deletes all saved values values for the formula for all group members
     * @param groupId the id of the group
     * @param selectedFormulas the new selected formulas to save
     * @param org the org, the group belongs to
     * @throws IOException if an IOException occurs while saving the data
     * @throws ValidatorException if a formula is not present (unchecked)
     */
    public static synchronized void saveGroupFormulas(Long groupId, List<String> selectedFormulas, Org org)
            throws IOException, ValidatorException {
        validateFormulaPresence(selectedFormulas);
        saveFormulaOrder();
        File dataFile = new File(getGroupDataFile());

        Map<String, List<String>> groupFormulas;
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            dataFile.createNewFile();
            groupFormulas = new HashMap<String, List<String>>();
        }
        else {
            groupFormulas =
                    GSON.fromJson(new BufferedReader(new FileReader(dataFile)),
                            Map.class);
        }

        // Remove formula data for unselected formulas
        List<String> deletedFormulas =
                new LinkedList<>(groupFormulas.getOrDefault(groupId.toString(),
                        new LinkedList<>()));
        deletedFormulas.removeAll(selectedFormulas);

        for (String f : deletedFormulas) {
            deleteGroupFormulaData(groupId, f);
        }

        // Save selected Formulas
        groupFormulas.put(groupId.toString(), orderFormulas(selectedFormulas));
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile))) {
            writer.write(GSON.toJson(groupFormulas));
        }

        // Generate monitoring default data from the 'pillar.example' file, otherwise the API would not return any
        // formula data as long as the default values are unchanged.
        if (selectedFormulas.contains(PROMETHEUS_EXPORTERS) &&
                getGroupFormulaValuesByNameAndGroupId(PROMETHEUS_EXPORTERS, groupId).isEmpty()) {
            FormulaFactory.saveGroupFormulaData(
                    getPillarExample(PROMETHEUS_EXPORTERS), groupId, org, PROMETHEUS_EXPORTERS);
        }

        // Handle entitlement removal in case of monitoring
        if (deletedFormulas.contains(PROMETHEUS_EXPORTERS)) {
            Set<MinionServer> minions = getGroupMinions(groupId, org);
            minions.forEach(minion -> {
                // remove entitlement only if formula not enabled at server level
                if (!serverHasMonitoringFormulaEnabled(minion)) {
                    systemEntitlementManager.removeServerEntitlement(minion, EntitlementManager.MONITORING);
                }
            });
        }
    }

    private static boolean serverHasMonitoringFormulaEnabled(MinionServer minion) {
        List<String> formulas = FormulaFactory.getFormulasByMinionId(minion.getMinionId());
        return formulas.contains(FormulaFactory.PROMETHEUS_EXPORTERS) &&
                getFormulaValuesByNameAndMinionId(PROMETHEUS_EXPORTERS, minion.getMinionId())
                        .map(data -> hasMonitoringDataEnabled(data))
                        .orElse(false);
    }

    private static Set<MinionServer> getGroupMinions(Long groupId, Org org) {
        return ServerGroupFactory
                .lookupByIdAndOrg(groupId, org)
                .getServers().stream()
                .map(server -> server.asMinionServer())
                .flatMap(Opt::stream)
                .collect(Collectors.toSet());
    }

    /**
     * Checks if a formula given is actually present.
     * @param formulasList the new pending formulas to check
     * @throws ValidatorException if a formula is not present
     */
    private static void validateFormulaPresence(List<String> formulasList) throws ValidatorException {
        // check if the passed formulas are actual formulas.
        List<String> incorrectFormulas = ListUtils.subtract(formulasList, listFormulaNames());

        if (!incorrectFormulas.isEmpty()) {
            throw new ValidatorException("\"" + String.join(", ", incorrectFormulas) + "\"" +
                    (incorrectFormulas.size() > 1 ? " are" : " is") +
                    " not found. Please make sure " +
                    (incorrectFormulas.size() > 1 ? "they are" : "it is") +
                    " spelled correctly or installed.");
        }
    }

    /**
     * Save the selected formulas for a server.
     * This also deletes all saved values of that formula.
     * @param minionId the minion id
     * @param selectedFormulas the new selected formulas to save
     * @throws IOException if an IOException occurs while saving the data
     * @throws ValidatorException if a formula is not present (unchecked)
     */
    public static synchronized void saveServerFormulas(String minionId, List<String> selectedFormulas)
            throws IOException, ValidatorException {
        validateFormulaPresence(selectedFormulas);
        saveFormulaOrder();
        File dataFile = new File(getServerDataFile());

        Map<String, List<String>> serverFormulas;
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            dataFile.createNewFile();
            serverFormulas = new HashMap<>();
        }
        else {
            serverFormulas = Optional
                    .ofNullable(GSON.fromJson(new BufferedReader(new FileReader(dataFile)), Map.class))
                    .orElse(new HashMap());
        }

        // Remove formula data for unselected formulas
        List<String> deletedFormulas = new LinkedList<>(serverFormulas.getOrDefault(minionId, new LinkedList<>()));
        deletedFormulas.removeAll(selectedFormulas);
        for (String deletedFormula : deletedFormulas) {
            deleteServerFormulaData(minionId, deletedFormula);
        }

        // Save selected Formulas
        List<String> orderedFormulas = orderFormulas(selectedFormulas);
        if (orderedFormulas.isEmpty()) {
            // when no formulas are assigned, we remove the entry completely for the minion
            serverFormulas.remove(minionId);
        }
        else {
            serverFormulas.put(minionId, orderedFormulas);
        }

        // Write minion_formulas file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile))) {
            writer.write(GSON.toJson(serverFormulas));
        }

        // Generate monitoring default data from the 'pillar.example' file, otherwise the API would not return any
        // formula data as long as the default values are unchanged.
        if (orderedFormulas.contains(PROMETHEUS_EXPORTERS) &&
                getFormulaValuesByNameAndMinionId(PROMETHEUS_EXPORTERS, minionId).isEmpty()) {
            FormulaFactory.saveServerFormulaData(
                    getPillarExample(PROMETHEUS_EXPORTERS), minionId, PROMETHEUS_EXPORTERS);
        }

        // Handle entitlement removal in case of monitoring
        if (deletedFormulas.contains(PROMETHEUS_EXPORTERS)) {
            MinionServerFactory.findByMinionId(minionId).ifPresent(s -> {
                if (!isMemberOfGroupHavingMonitoring(s)) {
                    systemEntitlementManager.removeServerEntitlement(s, EntitlementManager.MONITORING);
                }
            });
        }
    }

    /**
     * Deletes all saved values of a given server for a given formula
     * @param minionId the minion id
     * @param formulaName the name of the formula
     * @throws IOException if an IOException occurs while saving the data
     */
    public static void deleteServerFormulaData(String minionId, String formulaName) {
        try {
            File file = new File(getPillarDir() +
                    minionId + "_" + formulaName +
                    "." + PILLAR_FILE_EXTENSION);
            if (file.exists()) {
                file.delete();
            }
        }
        catch (UnsupportedOperationException e) {
            LOG.error("Error deleting formular data for " + formulaName +
                    ": " + e.getMessage());
        }
    }

    /**
     * Deletes all saved values of a given group for a given formula
     * @param groupId the id of the group
     * @param formulaName the name of the formula
     * @throws IOException if an IOException occurs while saving the data
     */
    public static void deleteGroupFormulaData(Long groupId, String formulaName)
            throws IOException {
        File file = new File(getGroupPillarDir() +
                groupId + "_" + formulaName + "." + PILLAR_FILE_EXTENSION);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * Orders a given list of formulas by the ordering specified in their metadata
     * @param formulasToOrder the list of formulas to order
     * @return a list of formulas in correct order of execution
     */
    public static List<String> orderFormulas(List<String> formulasToOrder) {
        LinkedList<String> formulas = new LinkedList<String>(formulasToOrder);

        Map<String, List<String>> dependencyMap = new HashMap<String, List<String>>();

        for (String formula : formulas) {
            List<String> dependsOnList = (List<String>) getMetadata(formula, "after")
                    .orElse(new ArrayList<>(0));
            dependsOnList.retainAll(formulas);
            dependencyMap.put(formula, dependsOnList);
        }

        int index = 0;
        int minLength = formulas.size();
        LinkedList<String> orderedList = new LinkedList<String>();

        while (!formulas.isEmpty()) {
            String formula = formulas.removeFirst();
            if (orderedList.containsAll(dependencyMap.get(formula))) {
                orderedList.addLast(formula);

                minLength = formulas.size();
                index = 0;
            }
            else if (index == minLength) { // one complete cycle without any change
                orderedList.addAll(formulas);
                return orderedList;
            }
            else {
                formulas.addLast(formula);
                index++;
            }
        }
        return orderedList;
    }

    /**
     * save the order of formulas
     * @throws IOException an IOException occurs while saving the data
     */
    public static void saveFormulaOrder() throws IOException {
        List<String> orderedList = orderFormulas(listFormulaNames());
        File file = new File(getOrderDataFile());
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        catch (FileAlreadyExistsException e) {
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(GSON.toJson(orderedList));
        }
    }

    /**
     * Returns the metadata of a formula.
     * @param name the name of the formula
     * @return the metadata
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getMetadata(String name) {
        String metadataFilePath = name + File.separator + METADATA_FILE;
        File metadataFileStandalone = new File(METADATA_DIR_STANDALONE_SALT + metadataFilePath);
        File metadataFileManager = new File(metadataDirManager + metadataFilePath);
        File metadataFileCustom = new File(METADATA_DIR_CUSTOM + metadataFilePath);
        try {
            if (metadataFileStandalone.isFile()) {
                return (Map<String, Object>) YAML.load(new FileInputStream(metadataFileStandalone));
            }
            else if (metadataFileManager.isFile()) {
                return (Map<String, Object>) YAML.load(new FileInputStream(metadataFileManager));
            }
            else if (metadataFileCustom.isFile()) {
                return (Map<String, Object>) YAML.load(new FileInputStream(metadataFileCustom));
            }
            else {
                return Collections.emptyMap();
            }
        }
        catch (IOException e) {
            return Collections.emptyMap();
        }
    }

    /**
     * Returns a given metadata value of a formula.
     * @param name the name of the formula
     * @param param the name of the metadata value
     * @return the metadata value
     */
    private static Optional<Object> getMetadata(String name, String param) {
        return Optional.ofNullable(getMetadata(name).getOrDefault(param, null));
    }

    /**
     * Read the 'pillar.example' file for a given formula and return the data.
     *
     * @param name the given name of a formula
     * @return data from pillar.example
     * @throws IOException in case there is a problem reading the pillar.example file
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getPillarExample(String name) {
        String pillarExamplePath = name + File.separator + PILLAR_EXAMPLE_FILE;
        File pillarExampleFileStandalone = new File(METADATA_DIR_STANDALONE_SALT + pillarExamplePath);
        File pillarExampleFileManager = new File(metadataDirManager + pillarExamplePath);
        File pillarExampleFileCustom = new File(METADATA_DIR_CUSTOM + pillarExamplePath);

        try {
            if (pillarExampleFileStandalone.isFile()) {
                return (Map<String, Object>) YAML.load(new FileInputStream(pillarExampleFileStandalone));
            }
            else if (pillarExampleFileManager.isFile()) {
                return (Map<String, Object>) YAML.load(new FileInputStream(pillarExampleFileManager));
            }
            else if (pillarExampleFileCustom.isFile()) {
                return (Map<String, Object>) YAML.load(new FileInputStream(pillarExampleFileCustom));
            }
            else {
                return Collections.emptyMap();
            }
        }
        catch (IOException e) {
            return Collections.emptyMap();
        }
    }

    /**
     * Disable all monitoring exporters in a given map of data.
     *
     * @param formData data to be used with the monitoring formula
     * @return data with exporters set to disabled
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> disableMonitoring(Map<String, Object> formData) {
        Map<String, Object> exporters = (Map<String, Object>) formData.get("exporters");
        // If we cannot extract the exporters from the form data the form data file might
        // come from an older version of the prometheus-exporters formula package and we will
        // use the example file instead
        if (exporters == null) {
            formData = FormulaFactory.getPillarExample(PROMETHEUS_EXPORTERS);
            exporters = (Map<String, Object>) formData.get("exporters");
        }
        for (Object exporter : exporters.values()) {
            Map<String, Object> exporterMap = (Map<String, Object>) exporter;
            exporterMap.put("enabled", false);
        }
        return formData;
    }

    /**
     * Check whether a server is member of a group having monitoring formula enabled.
     * @param server the server to check
     * @return true if the server is member of a group having monitoring formula enabled.
     */
    public static boolean isMemberOfGroupHavingMonitoring(Server server) {
        return server.getManagedGroups().stream()
                .map(grp -> FormulaFactory.hasMonitoringDataEnabled(grp))
                .anyMatch(Boolean::booleanValue);
    }

    /**
     * Check whether group has monitoring formula enabled.
     * @param group server group
     * @return true if monitoring formula is enabled, false otherwise
     */
    public static boolean hasMonitoringDataEnabled(ServerGroup group) {
        return getGroupFormulaValuesByNameAndGroupId(PROMETHEUS_EXPORTERS, group.getId())
                .map(FormulaFactory::hasMonitoringDataEnabled)
                .orElse(false);
    }

    @SuppressWarnings("unchecked")
    private static boolean hasMonitoringDataEnabled(Map<String, Object> formData) {
        Map<String, Object> exporters = (Map<String, Object>) formData.get("exporters");
        // If we cannot extract the exporters from the form data the form data file might
        // come from an older version of the prometheus-exporters formula package and we will
        // use the example file instead
        if (exporters == null) {
            exporters = (Map<String, Object>) FormulaFactory.getPillarExample(PROMETHEUS_EXPORTERS).get("exporters");
        }
        return exporters.values().stream()
                .map(exporter -> (Map<String, Object>) exporter)
                .anyMatch(exporter -> (boolean) exporter.get("enabled"));
    }

    /**
     * Returns the metadata of a cluster provider.
     * @param provider the name of the formula
     * @return the metadata
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getClusterProviderMetadata(String provider) {
        // TODO cache metadata ?
        String metadataFilePath = provider + File.separator + METADATA_FILE;
        File metadataFileStandalone = new File(METADATA_DIR_CLUSTER_PROVIDERS + metadataFilePath);
        try {
            if (metadataFileStandalone.isFile()) {
                return (Map<String, Object>) YAML.load(new FileInputStream(metadataFileStandalone));
            }
            else {
                return Collections.emptyMap();
            }
        }
        catch (IOException e) {
            return Collections.emptyMap();
        }
    }

    /**
     * Get a value from the cluster provider metadata.
     * @param provider the name of the cluster provider
     * @param key the key of the value
     * @param keyType the Java type of the value
     * @param <T> the Java type of the value
     * @return the value of the metadata key
     */
    public static <T> Optional<T> getClusterProviderMetadata(String provider, String key, Class<T> keyType) {
        Map<String, Object> metadata = FormulaFactory.getClusterProviderMetadata(provider);
        return FormulaFactory.getValueByPath(metadata, key)
                .filter(keyType::isInstance)
                .map(keyType::cast);
    }

    /**
     * Get a formula layout from a cluster provider. The formula is referenced by its key not by it's actual name.
     * @param provider the name of the cluster provider
     * @param formulaKey the key of the formula used by the provider
     * @return the formula layout as a Map
     */
    public static Optional<Map<String, Object>> getClusterProviderFormulaLayout(String provider, String formulaKey) {
        Map<String, Object> metadata = getClusterProviderMetadata(provider);
        Optional<String> formulaName = getValueByPath(metadata, "formulas:" + formulaKey + ":name")
                .filter(String.class::isInstance)
                .map(String.class::cast);

        String formulaSource = getValueByPath(metadata, "formulas:" + formulaKey + ":source")
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .orElse("system");

        if (formulaName.isEmpty()) {
            return Optional.empty();
        }
        if ("system".equals(formulaSource)) {
            return getFormulaLayoutByName(formulaName.get());
        }
        else if ("cluster-provider".equals(formulaSource)) {
            return getFormulaLayoutByClusterProviderAndName(provider, formulaName.get());
        }
        else {
            throw new RuntimeException("Unknown formula source " + formulaSource);
        }
    }

    private static Optional<Map<String, Object>> getFormulaLayoutByClusterProviderAndName(String provider,
                                                                                          String name) {
        Path layoutFile = Paths.get(METADATA_DIR_CLUSTER_PROVIDERS, provider, name + ".yml");
        try {
            if (Files.exists(layoutFile)) {
                return Optional.of((Map<String, Object>) YAML.load(new FileInputStream(layoutFile.toFile())));
            }
            else {
                return Optional.empty();
            }
        }
        catch (FileNotFoundException | YAMLException e) {
            LOG.error("Error loading layout for formula '" + name +
                    "' from cluster provider '" + provider + "'", e);
            return Optional.empty();
        }
    }

    /**
     * Checks the type of the formula.
     * @param formula the name of the formula
     * @param type the type
     * @return whether the formula has the given type or not
     */
    public static boolean formulaHasType(String formula, String type) {
        Map<String, Object> metadata = getMetadata(formula);
        return Optional.ofNullable(metadata.get("type"))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(t -> t.equals(type))
                .orElse(false);
    }


    /**
     * Get the value from a nested map structure by a colon separated path.
     * E.g. key1:key2:key3 for a map with a depth of 3.
     * @param data the nested map
     * @param path the path
     * @return a value if available
     */
    public static Optional<Object> getValueByPath(Map<String, Object> data, String path) {
        String[] tokens = StringUtils.split(path, ":");
        Map<String, Object> current = data;
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            Object val = current.get(token);
            if (i == tokens.length - 1) {
                return Optional.ofNullable(val);
            }
            if (val == null) {
                return Optional.empty();
            }
            if (val instanceof Map) {
                current = (Map<String, Object>)val;
            }
            else {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    /**
     * Get all installed cluster providers.
     * @return a list containing the metadata of all installed cluster providers
     */
    public static List<Map<String, Object>> getClusterProvidersMetadata() {
        Path dir = Path.of(METADATA_DIR_CLUSTER_PROVIDERS);
        try {
            return Files.list(dir)
                    .filter(Files::isDirectory)
                    .map(p -> {
                        String provider = p.getFileName().toString();
                        Map<String, Object> m = getClusterProviderMetadata(provider);
                        m = new HashMap<>(m);
                        m.put("label", provider);
                        return m;
                    })
                    .collect(Collectors.toList());
        }
        catch (IOException e) {
            LOG.error("Error loading providers metadata", e);
            return Collections.emptyList();
        }
    }

    public static String getClusterPillarDir() {
        return dataDir + PILLAR_DIR;
    }

}
