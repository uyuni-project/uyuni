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
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.transaction.NotSupportedException;

import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.suse.manager.webui.controllers.ECMAScriptDateAdapter;

/**
 * Factory class for working with formulas.
 */
public class FormulaFactory extends HibernateFactory {

    private static Logger log = Logger.getLogger(FormulaFactory.class);
    private static FormulaFactory singleton = new FormulaFactory();
    private static final String FORMULA_DATA_DIRECTORY = "/srv/susemanager/formulas_data/";
    private static final String FORMULA_DIRECTORY = "/usr/share/susemanager/salt/formulas/";
    private static final String FORMULA_PILLAR_DIRECTORY = "/srv/susemanager/formulas_data/pillar/";
    private static final String FORMULA_GROUP_PILLAR_DIRECTORY = "/srv/susemanager/formulas_data/group_pillar/";
    private static final String PILLAR_FILE_EXTENSION = "json";
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Date.class, new ECMAScriptDateAdapter())
            .serializeNulls()
            .create();
    private static final Yaml yaml = new Yaml(new SafeConstructor());

    private FormulaFactory() {
    }
    
    // Code for using hibernate and a database to save formula data
//    /**
//     * Save a {@link Formula}.
//     * @param formula the formula to save
//     */
//    public static void save(Formula formula) {
//        singleton.saveObject(formula);
//    }
//    
//    /**
//     * Get a {@link Formula} object from the db by serverId
//     * @param serverId the serverId
//     * @param orgId the org id
//     * @return an {@link Optional} containing a {@link Formula} object
//     */
//    public static Optional<Formula> getFormulaByServerId(long orgId, Long serverId) {
//        Formula formula = (Formula) getSession().createCriteria(Formula.class)
//                .add(Restrictions.eq("serverId", serverId))
//                .add(Restrictions.eq("org.id", orgId))
//                .uniqueResult();
//        return Optional.ofNullable(formula);
//    }

    public static void saveGroupFormulaData(Map<String, Object> formData, Long groupId, String formulaName) throws IOException {
    	File formula_file = new File(FORMULA_GROUP_PILLAR_DIRECTORY + groupId + "_" + formulaName + "." + PILLAR_FILE_EXTENSION);
    	try {
    		formula_file.getParentFile().mkdirs();
			formula_file.createNewFile();
    	} catch (FileAlreadyExistsException e) {}
    	
    	BufferedWriter writer = new BufferedWriter(new FileWriter(formula_file));
    	writer.write(GSON.toJson(formData));
    	writer.close();
    }
    
    public static void saveServerFormulaData(Map<String, Object> formData, Long serverId, String formulaName) throws IOException, NotSupportedException {
    	File formula_file = new File(FORMULA_PILLAR_DIRECTORY + getMinionId(serverId) + "_" + formulaName + "." + PILLAR_FILE_EXTENSION);
    	try {
    		formula_file.getParentFile().mkdirs();
			formula_file.createNewFile();
    	} catch (FileAlreadyExistsException e) {}
    	
    	BufferedWriter writer = new BufferedWriter(new FileWriter(formula_file));
    	writer.write(GSON.toJson(formData));
    	writer.close();
    }
    
    public static List<String> getFormulasByGroupId(Long groupId) {
    	LinkedList<String> formulas = new LinkedList<>();
    	File server_formulas_file = new File(FORMULA_DATA_DIRECTORY + "group_formulas.json");
    	if (!server_formulas_file.exists())
    		return new LinkedList<>();
    	
    	try {
			Map<String, List<String>> server_formulas = (Map<String, List<String>>) GSON.fromJson(new BufferedReader(new FileReader(server_formulas_file)), Map.class);
			return orderFormulas(server_formulas.getOrDefault(groupId.toString(), new LinkedList<>()));
    	}
    	catch (FileNotFoundException e) {
    		return new LinkedList<String>();
    	}
    }
    
    public static List<String> getFormulasByServerId(Long serverId) {
    	LinkedList<String> formulas = new LinkedList<>();
    	File server_formulas_file = new File(FORMULA_DATA_DIRECTORY + "group_formulas.json");
    	if (!server_formulas_file.exists())
    		return new LinkedList<String>();
    	
    	try {
			Map<String, List<String>> server_formulas = (Map<String, List<String>>) GSON.fromJson(new BufferedReader(new FileReader(server_formulas_file)), Map.class);
			
			for (ManagedServerGroup group : ServerFactory.lookupById(serverId).getManagedGroups())
				formulas.addAll(server_formulas.getOrDefault(group.getId().toString(), new ArrayList<>(0)));
	    	return orderFormulas(formulas);
    	}
    	catch (FileNotFoundException e) {
    		return new LinkedList<String>();
    	}
    }
    
    public static Optional<Map<String, Object>> getFormulaLayoutByName(String name) {
    	File layout_file = new File(FORMULA_DIRECTORY + name + "/form.yml");
    	
    	try {
			if (layout_file.exists())
				return Optional.of((Map<String, Object>) yaml.load(new FileInputStream(layout_file)));
			else
		    	return Optional.empty();
    	} catch (FileNotFoundException e) {
    		return Optional.empty();
    	}
    }
    
    public static Optional<Map<String, Object>> getFormulaValuesByNameAndServerId(String name, Long serverId) {
    	try {
			File data_file = new File(FORMULA_PILLAR_DIRECTORY + getMinionId(serverId) + "_" + name + "." + PILLAR_FILE_EXTENSION);
			if (data_file.exists())
				return Optional.of((Map<String, Object>) GSON.fromJson(new BufferedReader(new FileReader(data_file)), Map.class));
			else
		    	return Optional.empty();
    	} catch (FileNotFoundException | NotSupportedException e) {// TODO: maybe throw error if not supported? (means not a salt minion, @getMinionId)
    		return Optional.empty();
    	}
    }
    
    public static Optional<Map<String, Object>> getGroupFormulaValuesByNameAndServerId(String name, Long serverId) {
    	for (ManagedServerGroup group : ServerFactory.lookupById(serverId).getManagedGroups())
    		if (getFormulasByGroupId(group.getId()).contains(name))
    			return getFormulaValuesByNameAndGroupId(name, group.getId());
    	return Optional.empty();
    }
    
    public static Optional<Map<String, Object>> getFormulaValuesByNameAndGroupId(String name, Long groupId) {
    	File data_file = new File(FORMULA_GROUP_PILLAR_DIRECTORY + groupId + "_" + name + "." + PILLAR_FILE_EXTENSION);
    	try {
			if (data_file.exists())
				return Optional.of((Map<String, Object>) GSON.fromJson(new BufferedReader(new FileReader(data_file)), Map.class));
			else
		    	return Optional.empty();
    	} catch (FileNotFoundException e) {
    		return Optional.empty();
    	}
    }
    
    // TODO: this probably needs synchronization!
    public static void saveServerGroupFormulas(Long groupId, List<String> selectedFormulas, Org org) throws IOException {
    	File server_formulas_file = new File(FORMULA_DATA_DIRECTORY + "group_formulas.json");
    	
    	Map<String, List<String>> server_formulas;
    	if (!server_formulas_file.exists()) {
    		server_formulas_file.getParentFile().mkdirs();
    		server_formulas_file.createNewFile();
    		server_formulas = new HashMap<String, List<String>>();
    	}
    	else server_formulas = (Map<String, List<String>>) GSON.fromJson(new BufferedReader(new FileReader(server_formulas_file)), Map.class);
    	
    	// Remove formula data of servers
    	List<String> deletedFormulas = new LinkedList<>(server_formulas.getOrDefault(groupId.toString(), new LinkedList<>()));
    	deletedFormulas.removeAll(selectedFormulas);
    	for (Server server : ServerGroupFactory.lookupByIdAndOrg(groupId, org).getServers()) {
    		Long serverId = server.getId();
    		for (String deletedFormula : deletedFormulas)
    			deleteServerFormulaData(serverId, deletedFormula);
    	}
		for (String deletedFormula : deletedFormulas)
			deleteGroupFormulaData(groupId, deletedFormula);
    	
		// Save selected Formulas
		server_formulas.put(groupId.toString(), orderFormulas(selectedFormulas));
		
		// Write server_formulas file
    	BufferedWriter writer = new BufferedWriter(new FileWriter(server_formulas_file));
    	writer.write(GSON.toJson(server_formulas));
    	writer.close();
    }

    public static void deleteServerFormulaData(Long serverId, String formulaName) throws IOException {
    	try {
	    	File formula_file = new File(FORMULA_PILLAR_DIRECTORY + getMinionId(serverId) + "_" + formulaName + "." + PILLAR_FILE_EXTENSION);
	    	if (formula_file.exists()) {
	    		formula_file.delete();
	    	}
    	} catch (NotSupportedException e) {
    		//TODO: log error message?
    	}
    }

    public static void deleteGroupFormulaData(Long groupId, String formulaName) throws IOException {
    	File formula_file = new File(FORMULA_GROUP_PILLAR_DIRECTORY + groupId + "_" + formulaName + "." + PILLAR_FILE_EXTENSION);
    	if (formula_file.exists()) {
    		formula_file.delete();
    	}
    }
    
    public static List<String> orderFormulas(List<String> formulasToOrder) {
    	LinkedList<String> formulas = new LinkedList<String>(formulasToOrder);
    	
    	Map<String, List<String>> dependencyMap = new HashMap<String, List<String>>();
    	
    	for (String formula : formulas) {
    		List<String> dependsOnList = (List<String>) getMetadata(formula, "after").orElse(new ArrayList<>(0));
    		dependsOnList.retainAll(formulas);
    		dependencyMap.put(formula, dependsOnList);
    	}

    	// TODO: not very efficient, better with spanning tree algorithm or something like that
    	int index = 0;
    	int minLength = formulas.size();
    	LinkedList<String> orderedList = new LinkedList<String>();
    	
    	while (!formulas.isEmpty()) {
    		String formula = formulas.removeFirst();
    		if (orderedList.containsAll(dependencyMap.get(formula)))
    			orderedList.addLast(formula);
    		else
    			formulas.addLast(formula);
    		
    		// primitive safety check
    		if (formulas.size() < minLength) {
    			minLength = formulas.size();
    			index = 0;
    		} else if (index == minLength)
    			orderedList.addAll(formulas);
    		else
    			index++;
    	}
    	return orderedList;
    }
    
    public static Map<String, Object> getMetadata(String formula_name) {
    	File metadata_file = new File(FORMULA_DIRECTORY + formula_name + "/metadata.yml");
    	try {
    		return (Map<String, Object>) yaml.load(new FileInputStream(metadata_file));
    	}
    	catch (IOException e) {
    		return new HashMap<String, Object>();
    	}
    }
    
    public static Optional<Object> getMetadata(String formula_name, String param) {
    	return Optional.ofNullable(getMetadata(formula_name).getOrDefault(param, null));
    }
    
    private static String getMinionId(Long serverId) throws NotSupportedException {
    	Optional<MinionServer> minionServer = ServerFactory.lookupById(serverId).asMinionServer();
    	if (minionServer.isPresent())
    		return minionServer.get().getMinionId();
    	else
    		throw new NotSupportedException("The system is not a salt minion!");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Logger getLogger() {
        return log;
    }
}
