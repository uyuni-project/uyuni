/**
 * Copyright (c) 2016 SUSE LLC
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

import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Server;
import com.suse.utils.Opt;
import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.webui.controllers.ECMAScriptDateAdapter;

/**
 * Factory class for working with formulas.
 */
public class FormulaFactory {

    /** This formula is coupled with the monitoring system type */
    public static final String PROMETHEUS_EXPORTERS = "prometheus-exporters";

    // Logger for this class
    private static final Logger LOG = Logger.getLogger(FormulaFactory.class);

    private static String dataDir = "/srv/susemanager/formula_data/";
    private static final String METADATA_DIR_MANAGER = "/usr/share/susemanager/formulas/metadata/";
    private static final String METADATA_DIR_STANDALONE_SALT = "/usr/share/salt-formulas/metadata/";
    private static final String METADATA_DIR_CUSTOM = "/srv/formula_metadata/";
    private static final String PILLAR_DIR = "pillar/";
    private static final String GROUP_PILLAR_DIR = "group_pillar/";
    private static final String GROUP_DATA_FILE  = "group_formulas.json";
    private static final String SERVER_DATA_FILE = "minion_formulas.json";
    private static final String LAYOUT_FILE = "form.yml";
    private static final String METADATA_FILE = "metadata.yml";
    private static final String PILLAR_EXAMPLE_FILE = "pillar.example";
    private static final String PILLAR_FILE_EXTENSION = "json";
    private static String metadataDirOfficial = METADATA_DIR_MANAGER;
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

    private FormulaFactory() { }

    /**
     * Setter for data directory(will be needed for testing)
     * @param dataDirPath base path to where store files
     */
    public static void setDataDir(String dataDirPath) {
        FormulaFactory.dataDir = dataDirPath;
    }

    public static void setMetadataDirOfficial(String metadataDirPath) {
        FormulaFactory.metadataDirOfficial = metadataDirPath;
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
     * Returns the list of the names of all currently installed formulas.
     * @return the names of all currently installed formulas.
     */
    public static List<String> listFormulaNames() {
        File standaloneDir = new File(METADATA_DIR_STANDALONE_SALT);
        File managerDir = new File(METADATA_DIR_MANAGER);
        File customDir = new File(METADATA_DIR_CUSTOM);
        List<File> files = new LinkedList<>(
                Arrays.asList(standaloneDir.listFiles()));
        files.addAll(Arrays.asList(managerDir.listFiles()));
        files.addAll(Arrays.asList(customDir.listFiles()));
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

    /**
     * Returns all currently installed formulas.
     * @return a list of all currently installed formulas.
     */
    public static List<Formula> listFormulas() {
        List<Formula> formulas = new LinkedList<>();
        for (String formulaName : listFormulaNames()) {
            Formula formula = new Formula(formulaName);
            formula.setMetadata(getMetadata(formulaName));
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
                        SystemManager.removeServerEntitlement(minion.getId(), EntitlementManager.MONITORING);
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
        if (!hasEntitlement && SystemManager.canEntitleServer(server, EntitlementManager.MONITORING)) {
            SystemManager.entitleServer(server, EntitlementManager.MONITORING);
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
     * @throws UnsupportedOperationException if the server is not a salt minion
     */
    public static void saveServerFormulaData(Map<String, Object> formData, String minionId,
            String formulaName) throws IOException, UnsupportedOperationException {
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
                        SystemManager.removeServerEntitlement(s.getId(),
                                EntitlementManager.MONITORING);
                    }
                }
                else if (!SystemManager.hasEntitlement(s.getId(), EntitlementManager.MONITORING) &&
                        SystemManager.canEntitleServer(s, EntitlementManager.MONITORING)) {
                    SystemManager.entitleServer(s, EntitlementManager.MONITORING);
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
        File layoutFileManager = new File(METADATA_DIR_MANAGER + layoutFilePath);
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
                return Optional.of((Map<String, Object>) GSON.fromJson(
                        new BufferedReader(new FileReader(dataFile)), Map.class));
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
     * Save the selected formulas for a group.
     * This also deletes all saved values values for the formula for all group members
     * @param groupId the id of the group
     * @param selectedFormulas the new selected formulas to save
     * @param org the org, the group belongs to
     * @throws IOException if an IOException occurs while saving the data
     */
    public static synchronized void saveGroupFormulas(Long groupId,
            List<String> selectedFormulas, Org org) throws IOException {
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
                    SystemManager.removeServerEntitlement(minion.getId(), EntitlementManager.MONITORING);
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
     * Save the selected formulas for a server.
     * This also deletes all saved values of that formula.
     * @param minionId the minion id
     * @param selectedFormulas the new selected formulas to save
     * @throws IOException if an IOException occurs while saving the data
     * @throws UnsupportedOperationException in case serverId does not represent a minion
     */
    public static synchronized void saveServerFormulas(String minionId,
            List<String> selectedFormulas) throws IOException,
            UnsupportedOperationException {
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
                    SystemManager.removeServerEntitlement(s.getId(), EntitlementManager.MONITORING);
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
     * Returns the metadata of a formula.
     * @param name the name of the formula
     * @return the metadata
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getMetadata(String name) {
        String metadataFilePath = name + File.separator + METADATA_FILE;
        File metadataFileStandalone = new File(METADATA_DIR_STANDALONE_SALT + metadataFilePath);
        File metadataFileManager = new File(METADATA_DIR_MANAGER + metadataFilePath);
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
        File pillarExampleFileManager = new File(METADATA_DIR_MANAGER + pillarExamplePath);
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
        ((Map<String, Object>) formData.get("node_exporter")).put("enabled", false);
        ((Map<String, Object>) formData.get("postgres_exporter")).put("enabled", false);
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
        Map<String, Object> nodeExporter = (Map<String, Object>) formData.get("node_exporter");
        Map<String, Object> postgresExporter = (Map<String, Object>) formData.get("postgres_exporter");
        return (boolean) nodeExporter.get("enabled") || (boolean) postgresExporter.get("enabled");
    }
}
