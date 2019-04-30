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
    private static final String METADATA_DIR_OFFICIAL = "/usr/share/susemanager/formulas/metadata/";
    private static final String METADATA_DIR_CUSTOM = "/srv/formula_metadata/";
    private static final String PILLAR_DIR = "pillar/";
    private static final String GROUP_PILLAR_DIR = "group_pillar/";
    private static final String GROUP_DATA_FILE  = "group_formulas.json";
    private static final String SERVER_DATA_FILE = "minion_formulas.json";
    private static final String PILLAR_FILE_EXTENSION = "json";
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
        File officialDir = new File(METADATA_DIR_OFFICIAL);
        File customDir = new File(METADATA_DIR_CUSTOM);
        List<File> files = new LinkedList<>(
                Arrays.asList(officialDir.listFiles()));
        files.addAll(Arrays.asList(customDir.listFiles()));
        List<String> formulasList = new LinkedList<>();

        for (File f : files) {
            if (f.isDirectory() && new File(f, "form.yml").isFile()) {
                formulasList.add(f.getName());
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
     * @throws IOException if an IOException occurs while saving the data
     */
    public static void saveGroupFormulaData(Map<String, Object> formData, Long groupId,
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
        File file = new File(getPillarDir() + minionId +
                "_" + formulaName + "." + PILLAR_FILE_EXTENSION);
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        catch (FileAlreadyExistsException e) {
        }

        // Add the monitoring system type if one of the exporters is enabled
        MinionServerFactory.findByMinionId(minionId).ifPresent(s -> {
            if (PROMETHEUS_EXPORTERS.equals(formulaName) && hasMonitoringEnabled(formData)) {
                if (!SystemManager.hasEntitlement(s.getId(), EntitlementManager.MONITORING)) {
                    if (!SystemManager.canEntitleServer(s, EntitlementManager.MONITORING) ||
                            SystemManager.entitleServer(s, EntitlementManager.MONITORING).hasErrors()) {
                        throw new UnsupportedOperationException("Monitoring system type cannot be assigned");
                    }
                }
            }
            else if (PROMETHEUS_EXPORTERS.equals(formulaName)) {
                SystemManager.removeServerEntitlement(s.getId(), EntitlementManager.MONITORING);
            }
        });

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
    public static Optional<Map<String, Object>> getFormulaLayoutByName(String name) {
        File layoutFileOfficial = new File(METADATA_DIR_OFFICIAL + name + "/form.yml");
        File layoutFileCustom = new File(METADATA_DIR_CUSTOM + name + "/form.yml");

        try {
            if (layoutFileOfficial.exists()) {
                return Optional.of((Map<String, Object>) YAML.load(
                        new FileInputStream(layoutFileOfficial)));
            }
            else if (layoutFileCustom.exists()) {
                return Optional.of((Map<String, Object>) YAML.load(
                        new FileInputStream(layoutFileCustom)));
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

        Set<MinionServer> minions = ServerGroupFactory
                .lookupByIdAndOrg(groupId, org)
                .getServers().stream()
                .map(server -> server.asMinionServer())
                .flatMap(Opt::stream)
                .collect(Collectors.toSet());

        for (MinionServer minion : minions) { // foreach loop: we need to throw IOException
            for (String f : deletedFormulas) {
                deleteServerFormulaData(minion.getMinionId(), f);
            }
        }

        // Save selected Formulas
        groupFormulas.put(groupId.toString(), orderFormulas(selectedFormulas));
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile))) {
            writer.write(GSON.toJson(groupFormulas));
        }
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

        // TODO: Save default data for each formula in a generic way (load from yaml, write to json)
        if (orderedFormulas.contains(PROMETHEUS_EXPORTERS)) {
            Map<String, Object> data = new HashMap<>();
            Map<String, Object> nodeExporter = new HashMap<>();
            nodeExporter.put("enabled", true);
            Map<String, Object> postgresExporter = new HashMap<>();
            postgresExporter.put("enabled", false);
            postgresExporter.put("data_source_name", "postgresql://user:passwd@localhost:5432/database?sslmode=disable");
            data.put("node_exporter", nodeExporter);
            data.put("postgres_exporter", postgresExporter);
            FormulaFactory.saveServerFormulaData(data, minionId, FormulaFactory.PROMETHEUS_EXPORTERS);
        }
        else {
            MinionServerFactory.findByMinionId(minionId).ifPresent(s -> {
                if (SystemManager.hasEntitlement(s.getId(), EntitlementManager.MONITORING)) {
                    SystemManager.removeServerEntitlement(s.getId(), EntitlementManager.MONITORING);
                }
            });
        }

        // Write server_formulas file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile))) {
            writer.write(GSON.toJson(serverFormulas));
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
    public static Map<String, Object> getMetadata(String name) {
        File metadataFileOfficial =
                new File(METADATA_DIR_OFFICIAL + name + "/metadata.yml");
        File metadataFileCustom = new File(METADATA_DIR_CUSTOM + name + "/metadata.yml");
        try {
            if (metadataFileOfficial.isFile()) {
                return (Map<String, Object>) YAML.load(
                        new FileInputStream(metadataFileOfficial));
            }
            else if (metadataFileCustom.isFile()) {
                    return (Map<String, Object>) YAML.load(
                            new FileInputStream(metadataFileCustom));
            }
            else {
                return Collections.emptyMap();
            }
        }
        catch (IOException | YAMLException e) {
            return Collections.emptyMap();
        }
    }

    /**
     * Returns a given metadata value of a formula.
     * @param name the name of the formula
     * @param param the name of the metadata value
     * @return the metadata value
     */
    public static Optional<Object> getMetadata(String name, String param) {
        return Optional.ofNullable(getMetadata(name).getOrDefault(param, null));
    }

    @SuppressWarnings("unchecked")
    private static boolean hasMonitoringEnabled(Map<String, Object> formData) {
        Map<String, Object> nodeExporter = (Map<String, Object>) formData.get("node_exporter");
        Map<String, Object> postgresExporter = (Map<String, Object>) formData.get("postgres_exporter");
        return (boolean) nodeExporter.get("enabled") || (boolean) postgresExporter.get("enabled");
    }
}
