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
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.transaction.NotSupportedException;

import org.apache.commons.lang3.ArrayUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.suse.manager.webui.controllers.ECMAScriptDateAdapter;

/**
 * Factory class for working with formulas.
 */
public class FormulaFactory {

    private static final String DATA_DIR = "/srv/susemanager/formula_data/";
    private static final String METADATA_DIR_OFFICIAL =
            "/usr/share/susemanager/formulas/metadata/";
    private static final String METADATA_DIR_CUSTOM = "/srv/formula_metadata/";
    private static final String PILLAR_DIR = DATA_DIR + "pillar/";
    private static final String GROUP_PILLAR_DIR = DATA_DIR + "group_pillar/";
    private static final String GROUP_DATA_FILE = DATA_DIR + "group_formulas.json";
    private static final String SERVER_DATA_FILE = DATA_DIR + "minion_formulas.json";
    private static final String PILLAR_FILE_EXTENSION = "json";
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Date.class, new ECMAScriptDateAdapter())
            .serializeNulls()
            .create();
    private static final Yaml YAML = new Yaml(new SafeConstructor());

    private FormulaFactory() { }

    /**
     * Returns the list of the names of all currently installed formulas.
     * @return the names of all currently installed formulas.
     */
    public static List<String> listFormulaNames() {
        File officialDir = new File(METADATA_DIR_OFFICIAL);
        File customDir = new File(METADATA_DIR_CUSTOM);
        File[] files = ArrayUtils.addAll(officialDir.listFiles(), customDir.listFiles());
        List<String> formulasList = new LinkedList<>();

        // TODO: Check if directory is a real formula (contains form.yml and init.sls)?
        for (File f : files) {
            if (f.isDirectory()) {
                formulasList.add(f.getName());
            }
        }
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
        File file = new File(GROUP_PILLAR_DIR +
                groupId + "_" + formulaName + "." + PILLAR_FILE_EXTENSION);
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        catch (FileAlreadyExistsException e) {
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(GSON.toJson(formData));
        writer.close();
    }

    /**
     * Saves the values of a formula for a server.
     * @param formData the values to save
     * @param serverId the id of the server
     * @param formulaName the name of the formula
     * @throws IOException if an IOException occurs while saving the data
     * @throws NotSupportedException if the server is not a salt minion
     */
    public static void saveServerFormulaData(Map<String, Object> formData, Long serverId,
            String formulaName) throws IOException, NotSupportedException {
        File file = new File(PILLAR_DIR + getMinionId(serverId) +
                "_" + formulaName + "." + PILLAR_FILE_EXTENSION);
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        catch (FileAlreadyExistsException e) {
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(GSON.toJson(formData));
        writer.close();
    }

    /**
     * Returns the formulas applied to a given group
     * @param groupId the id of the group
     * @return the list of formulas
     */
    public static List<String> getFormulasByGroupId(Long groupId) {
        File serverFile = new File(GROUP_DATA_FILE);
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
            return new LinkedList<String>();
        }
    }

    /**
     * Returns the formulas applied to a given server
     * @param serverId the id of the server
     * @return the list of formulas
     */
    public static List<String> getFormulasByServerId(Long serverId) {
        Set<String> formulas = new HashSet<>();
        File groupDataFile = new File(GROUP_DATA_FILE);
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

        File serverDataFile = new File(SERVER_DATA_FILE);
        try {
            Map<String, List<String>> serverFormulas = GSON.fromJson(
                    new BufferedReader(new FileReader(serverDataFile)), Map.class);
            formulas.addAll(serverFormulas.getOrDefault(serverId.toString(),
                    Collections.emptyList()));
        }
        catch (FileNotFoundException e) {
        }
        return orderFormulas(new LinkedList<>(formulas));
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
                        new FileInputStream(layoutFileOfficial)));
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
     * @param serverId the id of the server
     * @return the saved values or an empty optional if no values were found
     */
    public static Optional<Map<String, Object>> getFormulaValuesByNameAndServerId(
            String name, Long serverId) {
        try {
            File dataFile = new File(PILLAR_DIR +
                    getMinionId(serverId) + "_" + name + "." + PILLAR_FILE_EXTENSION);
            if (dataFile.exists()) {
                return Optional.of((Map<String, Object>) GSON.fromJson(
                        new BufferedReader(new FileReader(dataFile)), Map.class));
            }
            else {
                return Optional.empty();
            }
        }
        catch (FileNotFoundException | NotSupportedException e) {
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
        File dataFile = new File(GROUP_PILLAR_DIR +
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
        File dataFile = new File(GROUP_DATA_FILE);

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
        for (Server server : ServerGroupFactory.lookupByIdAndOrg(groupId, org)
                .getServers()) {
            for (String deletedFormula : deletedFormulas) {
                deleteServerFormulaData(server.getId(), deletedFormula);
            }
        }
        for (String deletedFormula : deletedFormulas) {
            deleteGroupFormulaData(groupId, deletedFormula);
        }

        // Save selected Formulas
        groupFormulas.put(groupId.toString(), orderFormulas(selectedFormulas));
        BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile));
        writer.write(GSON.toJson(groupFormulas));
        writer.close();
    }

    /**
     * Save the selected formulas for a server.
     * This also deletes all saved values of that formula.
     * @param serverId the id of the server
     * @param selectedFormulas the new selected formulas to save
     * @throws IOException if an IOException occurs while saving the data
     */
    public static synchronized void saveServerFormulas(Long serverId,
            List<String> selectedFormulas) throws IOException {
        File dataFile = new File(SERVER_DATA_FILE);

        Map<String, List<String>> serverFormulas;
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            dataFile.createNewFile();
            serverFormulas = new HashMap<String, List<String>>();
        }
        else {
            serverFormulas =
                    GSON.fromJson(new BufferedReader(new FileReader(dataFile)),
                            Map.class);
        }

        // Remove formula data for unselected formulas
        List<String> deletedFormulas =
                new LinkedList<>(serverFormulas.getOrDefault(serverId.toString(),
                        new LinkedList<>()));
        deletedFormulas.removeAll(selectedFormulas);
        for (String deletedFormula : deletedFormulas) {
            deleteServerFormulaData(serverId, deletedFormula);
        }

        // Save selected Formulas
        serverFormulas.put(serverId.toString(), orderFormulas(selectedFormulas));

        // Write server_formulas file
        BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile));
        writer.write(GSON.toJson(serverFormulas));
        writer.close();
    }

    /**
     * Deletes all saved values of a given server for a given formula
     * @param serverId the id of the server
     * @param formulaName the name of the formula
     * @throws IOException if an IOException occurs while saving the data
     */
    public static void deleteServerFormulaData(Long serverId, String formulaName)
            throws IOException {
        try {
            File file = new File(PILLAR_DIR +
                    getMinionId(serverId) + "_" + formulaName +
                    "." + PILLAR_FILE_EXTENSION);
            if (file.exists()) {
                file.delete();
            }
        }
        catch (NotSupportedException e) {
            //TODO: log error message?
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
        File file = new File(GROUP_PILLAR_DIR +
                groupId + "_" + formulaName + "." + PILLAR_FILE_EXTENSION);
        if (file.exists()) {
            file.delete();
        }
    }

    // TODO: there are much more efficient algorithms for dependency solving
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

    /**
     * Returns the minion id of a given server.
     * @param serverId the id of the server
     * @return the minion id
     * @throws NotSupportedException if the server is not a salt minion
     */
    private static String getMinionId(Long serverId) throws NotSupportedException {
        Optional<MinionServer> minionServer =
                ServerFactory.lookupById(serverId).asMinionServer();
        if (minionServer.isPresent()) {
            return minionServer.get().getMinionId();
        }
        else {
            throw new NotSupportedException("The system is not a salt minion!");
        }
    }
}
