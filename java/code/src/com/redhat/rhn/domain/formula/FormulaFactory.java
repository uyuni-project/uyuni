/*
 * Copyright (c) 2016--2021 SUSE LLC
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
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.util.FileUtils;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.dto.EndpointInfo;
import com.redhat.rhn.domain.dto.FormulaData;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Pillar;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;

import com.suse.manager.saltboot.SaltbootException;
import com.suse.manager.saltboot.SaltbootUtils;
import com.suse.manager.webui.controllers.ECMAScriptDateAdapter;
import com.suse.utils.Maps;
import com.suse.utils.Opt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.collections.ListUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    public static final String PREFIX = "formula-";

    /** Saltboot group deployment formula */
    public static final String SALTBOOT_GROUP = "saltboot-group";

    // Logger for this class
    private static final Logger LOG = LogManager.getLogger(FormulaFactory.class);

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
    private static final String ORDER_PILLAR_CATEGORY = "formula_order";

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Date.class, new ECMAScriptDateAdapter())
            .serializeNulls()
            .create();

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
        return FormulaFactory.orderFormulas(formulasList);
    }

    private static List<File> getFormulasFiles(File formulasFolder) {
        return Optional.ofNullable(formulasFolder.listFiles())
                .map(Arrays::asList)
                .orElseGet(() -> {
                    LOG.error("Unable to read formulas from folder '{}'. Check if it exists and have the " +
                            "correct permissions (755).", formulasFolder.getAbsolutePath());
                    return Collections.emptyList();
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
     * @param group the group
     * @param formulaName the name of the formula
     */
    public static void saveGroupFormulaData(Map<String, Object> formData, ServerGroup group, String formulaName) {
        // Ensure the formulas are converted to pillar in database
        convertGroupFormulasFromFiles(group);

        group.getPillarByCategory(PREFIX + formulaName).orElseGet(() -> {
            Pillar pillar = new Pillar(PREFIX + formulaName, Collections.emptyMap(), group);
            group.getPillars().add(pillar);
            return pillar;
        }).setPillar(formData);

        if (PROMETHEUS_EXPORTERS.equals(formulaName)) {
            Set<MinionServer> minions = getGroupMinions(group);
            minions.forEach(minion -> {
                if (!hasMonitoringDataEnabled(formData)) {
                    if (!serverHasMonitoringFormulaEnabled(minion)) {
                        systemEntitlementManager.removeServerEntitlement(minion, EntitlementManager.MONITORING);
                    }
                }
                else {
                    systemEntitlementManager.grantMonitoringEntitlement(minion);
                }
            });
        }

        // Handle Saltboot group - create Cobbler profile
        if (SALTBOOT_GROUP.equals(formulaName)) {
            Map<String, Object> saltboot = (Map<String, Object>) formData.get("saltboot");
            String kernelOptions = "MINION_ID_PREFIX=" + group.getName();
            kernelOptions += " MASTER=" + saltboot.get("download_server");
            if ((Boolean)saltboot.get("disable_id_prefix")) {
                kernelOptions += " DISABLE_ID_PREFIX=1";
            }
            if ((Boolean)saltboot.get("disable_unique_suffix")) {
                kernelOptions += " DISABLE_UNIQUE_SUFFIX=1";
            }
            if (saltboot.get("minion_id_naming") == "FQDN") {
                kernelOptions += " USE_FQDN_MINION_ID=1";
            }
            else if (saltboot.get("minion_id_naming") == "HWType") {
                kernelOptions += " DISABLE_HOSTNAME_ID=1";
            }
            if (!((String)saltboot.get("default_kernel_parameters")).isEmpty()) {
                kernelOptions += " " + saltboot.get("default_kernel_parameters");
            }
            String bootImage = (String)saltboot.get("default_boot_image");
            String bootImageVersion = (String)saltboot.get("default_boot_image_version");

            try {
                SaltbootUtils.createSaltbootProfile(group.getName(), kernelOptions, group.getOrg(),
                        bootImage, bootImageVersion);
            }
            catch (SaltbootException e) {
                throw new ValidatorException(e.getMessage());
            }

        }
    }

    /**
     * Convert legacy server formulas to DB
     *
     * @param server the server
     */
    public static void convertServerFormulasFromFiles(MinionServer server) {
        Map<String, List<String>> serverFormulas = readFormulaFile(getServerDataFile());
        List<String> legacyFormulas = new LinkedList<>(serverFormulas.getOrDefault(server.getMinionId(),
                new LinkedList<>()));

        if (!legacyFormulas.isEmpty()) {
            legacyFormulas.forEach(formula -> {
                Optional<Map<String, Object>> data = getFormulaValuesByNameAndMinion(formula, server);
                // If data are empty -> load from files. If anything is already in database, ignore files
                if (data.isEmpty()) {
                    File dataFile = new File(getPillarDir() + server.getMinionId() + "_" + formula + "." +
                            PILLAR_FILE_EXTENSION);
                    try {
                        data = Optional.ofNullable(GSON.fromJson(new BufferedReader(new FileReader(dataFile)),
                                                new TypeToken<Map<String, Object>>() { }.getType()));
                    }
                    catch (FileNotFoundException e) {
                        // This happens if the formula has default value
                        data = Optional.of(new HashMap<>());
                    }
                    data.map(FormulaFactory::convertIntegers).ifPresent(d -> {
                        Pillar pillar = new Pillar(PREFIX + formula, d, server);
                        server.getPillars().add(pillar);
                    });
                }
                else {
                    LOG.warn("Minion \"{}\" pillar \"{}\" already in database, not migrating pillar file",
                            server.getMinionId(), formula);
                }
                FileUtils.deleteFile(new File(getPillarDir() +
                        server.getMinionId() + "_" + formula + "." + PILLAR_FILE_EXTENSION).toPath());
            });

            // Remove the entry from the data file
            try {
                removeEntryFromFormulaFile(server.getMinionId(), getServerDataFile());
            }
            catch (IOException ignored) {
            }
        }
    }

    /**
     * Saves the values of a formula for a server.
     * @param formData the values to save
     * @param minion the minion
     * @param formulaName the name of the formula
     */
    public static void saveServerFormulaData(Map<String, Object> formData, MinionServer minion, String formulaName) {
        // Ensure all the minion formulas are converted to pillar in DB
        convertServerFormulasFromFiles(minion);

        minion.getPillarByCategory(PREFIX + formulaName).orElseGet(() -> {
            Pillar pillar = new Pillar(PREFIX + formulaName, Collections.emptyMap(), minion);
            minion.getPillars().add(pillar);
            return pillar;
        }).setPillar(formData);

        // Add the monitoring entitlement if at least one of the exporters is enabled
        if (PROMETHEUS_EXPORTERS.equals(formulaName)) {
            MinionServerFactory.findByMinionId(minion.getMinionId()).ifPresent(s -> {
                if (!hasMonitoringDataEnabled(formData)) {
                    if (isMemberOfGroupHavingMonitoring(s)) {
                        // nothing to do here, keep monitoring entitlement and disable formula
                        LOG.debug(String.format("Minion %s is member of group having monitoring enabled." +
                                " Not removing monitoring entitlement.", minion.getMinionId()));
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
    }

    /**
     * Get the formulas applied to a given group
     *
     * @param group the group
     * @return the list of formulas
     */
    public static List<String> getFormulasByGroup(ServerGroup group) {
        List<String> formulas = group.getPillars().stream()
                .filter(pillar -> pillar.getCategory().startsWith(PREFIX))
                .map(pillar -> pillar.getCategory().substring(PREFIX.length()))
                .collect(Collectors.toList());

        // Still try the legacy way since the formula data may not be converted yet
        File groupDataFile = new File(getGroupDataFile());
        if (formulas.isEmpty() && groupDataFile.exists()) {
            try {
                Map<String, List<String>> serverFormulas =
                        GSON.fromJson(new BufferedReader(new FileReader(groupDataFile)),
                                Map.class);
                return orderFormulas(serverFormulas.getOrDefault(group.getId().toString(),
                        Collections.emptyList()));
            }
            catch (FileNotFoundException e) {
            }
        }
        return orderFormulas(formulas);
    }

    /**
     * Returns the formulas applied to a given server
     * @param minion the minion
     * @return the list of formulas
     */
    public static List<String> getFormulasByMinion(MinionServer minion) {
        List<String> formulas = orderFormulas(minion.getPillars().stream()
                .filter(pillar -> pillar.getCategory().startsWith(PREFIX))
                .map(pillar -> pillar.getCategory().substring(PREFIX.length()))
                .collect(Collectors.toList()));

        // Still try the legacy way since the formula data may not be converted yet
        File serverDataFile = new File(getServerDataFile());
        if (formulas.isEmpty() && serverDataFile.exists()) {
            try {
                Map<String, List<String>> serverFormulas =
                        GSON.fromJson(new BufferedReader(new FileReader(serverDataFile)),
                                Map.class);
                return orderFormulas(serverFormulas.getOrDefault(minion.getMinionId(),
                        Collections.emptyList()));
            }
            catch (FileNotFoundException e) {
            }
        }
        return orderFormulas(formulas);
    }

    /**
     * Returns a combination of all formulas applied to a server and
     * all formulas inherited from its groups.
     * @param server the server
     * @return the combined list of formulas
     */
    public static List<String> getCombinedFormulasByServer(MinionServer server) {
        List<String> formulas = getFormulasByMinion(server);
        List<String> groupFormulas = getGroupFormulasByServer(server);
        formulas.removeAll(groupFormulas); // Remove duplicates
        formulas.addAll(groupFormulas);
        return formulas;
    }

    /**
     * Returns the formulas that a given server inherits from its groups.
     * @param server the server
     * @return the list of formulas
     */
    public static List<String> getGroupFormulasByServer(Server server) {
        List<String> formulas = new LinkedList<>();
        for (ServerGroup group : server.getManagedGroups()) {
            formulas.addAll(getFormulasByGroup(group));
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

        Yaml yaml = new Yaml(new SafeConstructor());
        try {
            if (layoutFileStandalone.exists()) {
                return Optional.of((Map<String, Object>) yaml.load(new FileInputStream(layoutFileStandalone)));
            }
            else if (layoutFileManager.exists()) {
                return Optional.of((Map<String, Object>) yaml.load(new FileInputStream(layoutFileManager)));
            }
            else if (layoutFileCustom.exists()) {
                return Optional.of((Map<String, Object>) yaml.load(new FileInputStream(layoutFileCustom)));
            }
            else {
                return Optional.empty();
            }
        }
        catch (FileNotFoundException | YAMLException e) {
            LOG.error("Error loading layout for formula '{}'", name, e);
            return Optional.empty();
        }
    }

    /**
     * Returns the saved values of a given server for a given formula.
     * @param name the name of the formula
     * @param minion the minion
     * @return the saved values or an empty optional if no values were found
     */
    public static Optional<Map<String, Object>> getFormulaValuesByNameAndMinion(
            String name, MinionServer minion) {
        return minion.getPillarByCategory(PREFIX + name).map(Pillar::getPillar);
    }

    /**
     * Returns the saved values of a group for a given formula.
     * The group is found by a given server, that is a member of that group.
     * @param name the name of the formula
     * @param server the server
     * @return the saved values or an empty optional if no values were found
     */
    public static Optional<Map<String, Object>> getGroupFormulaValuesByNameAndServer(
            String name, Server server) {
        for (ServerGroup group : server.getManagedGroups()) {
            if (getFormulasByGroup(group).contains(name)) {
                return getGroupFormulaValuesByNameAndGroup(name, group);
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the saved values of a given group for a given formula.
     * @param name the name of the formula
     * @param group the group
     * @return the saved values or an empty optional if no values were found
     */
    public static Optional<Map<String, Object>> getGroupFormulaValuesByNameAndGroup(
            String name, ServerGroup group) {
        Optional<Map<String, Object>> data = group.getPillarByCategory(PREFIX + name).map(Pillar::getPillar);

        // Load data from the legacy file if not converted yet
        if (data.isEmpty()) {
            File dataFile = new File(getGroupPillarDir() +
                    group.getId() + "_" + name + "." + PILLAR_FILE_EXTENSION);
            try {
                data = Optional.ofNullable(GSON.fromJson(new BufferedReader(new FileReader(dataFile)),
                                new TypeToken<Map<String, Object>>() { }.getType()));
            }
            catch (FileNotFoundException e) {
                data = Optional.of(new HashMap<>());
            }
        }
        return data.map(FormulaFactory::convertIntegers);
    }

    /**
     * Convert the legacy formulas of a group.
     *
     * @param group the group
     */
    public static void convertGroupFormulasFromFiles(ServerGroup group) {
        Map<String, List<String>> groupFormulas = readFormulaFile(getGroupDataFile());
        List<String> legacyFormulas = new LinkedList<>(groupFormulas.getOrDefault(group.getId().toString(),
                new LinkedList<>()));

        if (!legacyFormulas.isEmpty()) {
            legacyFormulas.forEach(formula -> {
                Optional<Map<String, Object>> data = getGroupFormulaValuesByNameAndGroup(formula, group);
                data.ifPresent(formData -> group.getPillarByCategory(PREFIX + formula).orElseGet(() -> {
                    Pillar pillar = new Pillar(PREFIX + formula, Collections.emptyMap(), group);
                    group.getPillars().add(pillar);
                    return pillar;
                }).setPillar(formData));
                FileUtils.deleteFile(new File(getGroupPillarDir() +
                        group.getId() + "_" + formula + "." + PILLAR_FILE_EXTENSION).toPath());
            });

            // Remove the entry from the data file
            try {
                removeEntryFromFormulaFile(group.getId().toString(), getGroupDataFile());
            }
            catch (IOException ignored) {
            }
        }
    }

    /**
     * Save the selected formulas for a group.
     * This also deletes all saved values values for the formula for all group members
     * @param group the group
     * @param selectedFormulas the new selected formulas to save
     * @throws ValidatorException if a formula is not present (unchecked)
     */
    public static synchronized void saveGroupFormulas(ServerGroup group, List<String> selectedFormulas)
            throws ValidatorException {
        validateFormulaPresence(selectedFormulas);
        saveFormulaOrder();

        // Ensure the formulas are converted to pillar in database
        convertGroupFormulasFromFiles(group);

        // Remove formula data for unselected formulas
        List<String> deletedFormulas = getFormulasByGroup(group);
        deletedFormulas.removeAll(selectedFormulas);

        // Try to remove SaltbootProfile first. It this fails, stop removing formulas
        if (deletedFormulas.contains(SALTBOOT_GROUP)) {
            try {
                SaltbootUtils.deleteSaltbootProfile(group.getName(), group.getOrg());
            }
            catch (SaltbootException e) {
                throw new ValidatorException(e.getMessage());
            }
        }

        for (String f : deletedFormulas) {
            group.getPillarByCategory(PREFIX + f).ifPresent(pillar -> group.getPillars().remove(pillar));
        }

        // Save selected Formulas
        for (String formula : selectedFormulas) {
            if (group.getPillarByCategory(PREFIX + formula).isEmpty()) {
                Pillar pillar = new Pillar(PREFIX + formula, new HashMap<>(), group);
                group.getPillars().add(pillar);
            }
        }

        // Generate monitoring default data from the 'pillar.example' file, otherwise the API would not return any
        // formula data as long as the default values are unchanged.
        if (selectedFormulas.contains(PROMETHEUS_EXPORTERS) &&
                getGroupFormulaValuesByNameAndGroup(PROMETHEUS_EXPORTERS, group).isEmpty()) {
            FormulaFactory.saveGroupFormulaData(
                    getPillarExample(PROMETHEUS_EXPORTERS), group, PROMETHEUS_EXPORTERS);
        }

        // Handle entitlement removal in case of monitoring
        if (deletedFormulas.contains(PROMETHEUS_EXPORTERS)) {
            Set<MinionServer> minions = getGroupMinions(group);
            minions.forEach(minion -> {
                // remove entitlement only if formula not enabled at server level
                if (!serverHasMonitoringFormulaEnabled(minion)) {
                    systemEntitlementManager.removeServerEntitlement(minion, EntitlementManager.MONITORING);
                }
            });
        }
    }

    /**
     * Convert the doubles that could be integers into integers: this is critical for formulas
     * Since we don't serialize the values anymore.
     *
     * @param map the map to iterate on.
     * @return the converted map
     */
    public static Map<String, Object> convertIntegers(Map<String, Object> map) {
        for (String key : map.keySet()) {
            Object value = map.get(key);
            if (value instanceof Double) {
                if (((Double)value) % 1 == 0) {
                    map.put(key, ((Double)value).intValue());
                }
            }
            else if (value instanceof Map) {
                convertIntegers((Map<String, Object>) value);
            }
        }
        return map;
    }

    private static boolean serverHasMonitoringFormulaEnabled(MinionServer minion) {
        List<String> formulas = FormulaFactory.getFormulasByMinion(minion);
        return formulas.contains(FormulaFactory.PROMETHEUS_EXPORTERS) &&
                getFormulaValuesByNameAndMinion(PROMETHEUS_EXPORTERS, minion)
                        .map(FormulaFactory::hasMonitoringDataEnabled)
                        .orElse(false);
    }

    private static Set<MinionServer> getGroupMinions(ServerGroup group) {
        return group.getServers().stream()
                .map(Server::asMinionServer)
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

    private static Map<String, List<String>> readFormulaFile(String path) {
        File dataFile = new File(path);
        Map<String, List<String>> formulas = new HashMap<>();
        try {
            formulas = Optional
                    .ofNullable(GSON.fromJson(new BufferedReader(new FileReader(dataFile)), Map.class))
                    .orElse(new HashMap<>());
        }
        catch (FileNotFoundException e) {
        }
        return formulas;
    }

    /**
     * Save the selected formulas for a server.
     * This also deletes all saved values of that formula.
     * @param minion the minion
     * @param selectedFormulas the new selected formulas to save
     * @throws ValidatorException if a formula is not present (unchecked)
     */
    public static synchronized void saveServerFormulas(MinionServer minion, List<String> selectedFormulas)
            throws ValidatorException {
        validateFormulaPresence(selectedFormulas);
        saveFormulaOrder();

        // Ensure all the minion formulas are converted to pillar in DB
        convertServerFormulasFromFiles(minion);

        // Remove formula data for unselected formulas
        List<String> deletedFormulas = getFormulasByMinion(minion);
        deletedFormulas.removeAll(selectedFormulas);
        for (String deletedFormula : deletedFormulas) {
            minion.getPillarByCategory(PREFIX + deletedFormula).ifPresent(pillar -> {
                minion.getPillars().remove(pillar);
                HibernateFactory.getSession().remove(pillar);
            });
        }

        // Save selected Formulas if we don't have them already
        for (String formula : selectedFormulas) {
            if (minion.getPillarByCategory(PREFIX + formula).isEmpty()) {
                Pillar pillar = new Pillar(PREFIX + formula, new HashMap<>(), minion);
                minion.getPillars().add(pillar);
            }
        }

        // Generate monitoring default data from the 'pillar.example' file, otherwise the API would not return any
        // formula data as long as the default values are unchanged.
        minion.getPillarByCategory(PREFIX + PROMETHEUS_EXPORTERS)
                .ifPresent(pillar -> {
                    if (pillar.getPillar().isEmpty()) {
                        pillar.setPillar(getPillarExample(PROMETHEUS_EXPORTERS));
                    }
                });

        // Handle entitlement removal in case of monitoring
        if (deletedFormulas.contains(PROMETHEUS_EXPORTERS) && !isMemberOfGroupHavingMonitoring(minion)) {
            systemEntitlementManager.removeServerEntitlement(minion, EntitlementManager.MONITORING);
        }
        ServerFactory.save(minion);
    }

    /**
     * Ensure the minion id is removed from the legacy formula file.
     *
     * @param id the minion or group id to look for
     * @param filePath the path of the formula file
     * @throws IOException if the data file failed to be written
     */
    public static void removeEntryFromFormulaFile(String id, String filePath) throws IOException {
        File dataFile = new File(filePath);

        if (dataFile.exists()) {
            Map<String, List<String>> serverFormulas = Optional
                    .ofNullable(GSON.fromJson(new BufferedReader(new FileReader(dataFile)), Map.class))
                    .orElse(new HashMap<>());

            if (serverFormulas.containsKey(id)) {
                serverFormulas.remove(id);
                if (serverFormulas.isEmpty() && dataFile.exists()) {
                    dataFile.delete();
                }
                else {
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile))) {
                        writer.write(GSON.toJson(serverFormulas));
                    }
                }
            }
        }
    }

    /**
     * Orders a given list of formulas by the ordering specified in their metadata
     * @param formulasToOrder the list of formulas to order
     * @return a list of formulas in correct order of execution
     */
    public static List<String> orderFormulas(List<String> formulasToOrder) {
        LinkedList<String> formulas = new LinkedList<>(formulasToOrder);
        formulas.sort(String.CASE_INSENSITIVE_ORDER);

        Map<String, List<String>> dependencyMap = new HashMap<>();

        for (String formula : formulas) {
            List<String> dependsOnList = (List<String>) getMetadata(formula, "after")
                    .orElse(new ArrayList<>(0));
            dependsOnList.retainAll(formulas);
            dependencyMap.put(formula, dependsOnList);
        }

        int index = 0;
        int minLength = formulas.size();
        LinkedList<String> orderedList = new LinkedList<>();

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
     */
    public static void saveFormulaOrder() {
        List<String> orderedList = listFormulaNames();
        Pillar orderPillar = Pillar.getGlobalPillars().stream()
                .filter(pillar -> pillar.getCategory().equals(ORDER_PILLAR_CATEGORY))
                .findFirst()
                .orElseGet(() -> Pillar.createGlobalPillar(ORDER_PILLAR_CATEGORY, Collections.emptyMap()));
        orderPillar.setPillar(Collections.singletonMap("formula_order", orderedList));

        // Ensure the legacy order file is removed
        FileUtils.deleteFile(new File(getOrderDataFile()).toPath());
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

        Yaml yaml = new Yaml(new SafeConstructor());
        try {
            if (metadataFileStandalone.isFile()) {
                return (Map<String, Object>) yaml.load(new FileInputStream(metadataFileStandalone));
            }
            else if (metadataFileManager.isFile()) {
                return (Map<String, Object>) yaml.load(new FileInputStream(metadataFileManager));
            }
            else if (metadataFileCustom.isFile()) {
                return (Map<String, Object>) yaml.load(new FileInputStream(metadataFileCustom));
            }
            else {
                return Collections.emptyMap();
            }
        }
        catch (YAMLException e) {
            LOG.error("Unable to parse metadata file: " + name, e);
            return Collections.emptyMap();
        }
        catch (IOException e) {
            LOG.error("IO Error at metadata file: " + name, e);
            return Collections.emptyMap();
        }
        catch (Exception e) {
            LOG.error("Error in metadata file: " + name, e);
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

        Yaml yaml = new Yaml(new SafeConstructor());
        try {
            if (pillarExampleFileStandalone.isFile()) {
                return (Map<String, Object>) yaml.load(new FileInputStream(pillarExampleFileStandalone));
            }
            else if (pillarExampleFileManager.isFile()) {
                return (Map<String, Object>) yaml.load(new FileInputStream(pillarExampleFileManager));
            }
            else if (pillarExampleFileCustom.isFile()) {
                return (Map<String, Object>) yaml.load(new FileInputStream(pillarExampleFileCustom));
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
                .map(FormulaFactory::hasMonitoringDataEnabled)
                .anyMatch(Boolean::booleanValue);
    }

    /**
     * Check whether group has monitoring formula enabled.
     * @param group server group
     * @return true if monitoring formula is enabled, false otherwise
     */
    public static boolean hasMonitoringDataEnabled(ServerGroup group) {
        return getGroupFormulaValuesByNameAndGroup(PROMETHEUS_EXPORTERS, group)
                .map(FormulaFactory::hasMonitoringDataEnabled)
                .orElse(false);
    }

    @SuppressWarnings("unchecked")
    private static boolean hasMonitoringDataEnabled(Map<String, Object> formData) {
        Map<String, Object> exporters = (Map<String, Object>) formData.get("exporters");
        if (exporters != null && !exporters.isEmpty()) {
            return exporters.values().stream()
                    .filter(Map.class::isInstance)
                    .map(exporter -> (Map<String, Object>) exporter)
                    .anyMatch(exporter -> (boolean) exporter.get("enabled"));
        }
        else {
            return false;
        }
    }

    /**
     * Find endpoint information from given formula data
     * @param formulaName name of the formula to examine
     * @param formulaData formula data to extract information from
     * @return list of endpoint information objects
     */
    public static List<EndpointInfo> getEndpointsFromFormulaData(String formulaName, FormulaData formulaData) {
        return getExportersEndpoints(formulaData);
    }

    private static List<EndpointInfo> getExportersEndpoints(FormulaData formulaData) {
        Map<String, Object> formulaValues = formulaData.getFormulaValues();
        if (formulaValues.containsKey("exporters")) {
            Boolean proxyEnabled = Maps.getValueByPath(formulaValues, "proxy_enabled")
                    .filter(Boolean.class::isInstance)
                    .map(Boolean.class::cast)
                    .orElse(false);
            Optional<Integer> proxyPort = proxyEnabled ? Maps.getValueByPath(formulaValues, "proxy_port")
                    .filter(Number.class::isInstance)
                    .map(Number.class::cast)
                    .map(Number::intValue) : Optional.empty();
            String proxyPath = proxyEnabled ? "/proxy" : null;
            Boolean tlsEnabled = Maps.getValueByPath(formulaValues, "tls:enabled")
                    .filter(Boolean.class::isInstance)
                    .map(Boolean.class::cast)
                    .orElse(false);

            Map<String, Object> exportersMap = Maps.getValueByPath(formulaValues, "exporters")
                    .filter(Map.class::isInstance)
                    .map(Map.class::cast)
                    .orElseGet(Collections::emptyMap);

            return exportersMap.entrySet().stream()
                    .map(exporterEntry -> new ExporterConfig(exporterEntry.getKey(),
                            Optional.ofNullable(exporterEntry.getValue())
                                    .filter(Map.class::isInstance)
                                    .map(Map.class::cast)
                                    .orElseGet(Collections::emptyMap)))
                    .filter(ExporterConfig::isEnabled)
                    .filter(e -> (proxyEnabled && proxyPort.isPresent()) || (!proxyEnabled && e.getPort().isPresent()))
                    .map(exporterConfig -> new EndpointInfo(
                            formulaData.getSystemID(),
                            exporterConfig.getEndpointNameOrFallback(),
                            exporterConfig.getEndpointNameOrFallback(),
                            proxyEnabled ? proxyPort.get() : exporterConfig.getPort().get(),
                            proxyEnabled ? exporterConfig.getProxyModuleOrFallback() : null,
                            proxyPath,
                            tlsEnabled))
                    .collect(Collectors.toList());
        }
        else {
            return new ArrayList<>();
        }
    }
}
