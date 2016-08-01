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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.server.ServerFactory;
import com.suse.manager.webui.controllers.ECMAScriptDateAdapter;

/**
 * Factory class for working with formulas.
 */
public class FormulaFactory extends HibernateFactory {

    private static Logger log = Logger.getLogger(FormulaFactory.class);
    private static FormulaFactory singleton = new FormulaFactory();
    private static final String FORMULA_DATA_DIRECTORY = "/srv/susemanager/formulas_data/";
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Date.class, new ECMAScriptDateAdapter())
            .serializeNulls()
            .create();

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
    
    public static void saveServerFormula(Formula formula) throws IOException {
    	File formula_file = new File(FORMULA_DATA_DIRECTORY + "pillar/" + ServerFactory.lookupById(formula.getServerId()).getName() + ".json");
    	try {
    		formula_file.getParentFile().mkdirs();
			formula_file.createNewFile();
    	} catch (FileAlreadyExistsException e) {}
    	
    	BufferedWriter writer = new BufferedWriter(new FileWriter(formula_file));
    	Map<String, Object> content = GSON.fromJson(formula.getContent(), Map.class);
    	content.put("form_id", formula.getFormulaName());
    	writer.write("{\"formula\": ");
    	writer.newLine();
    	writer.write(GSON.toJson(content));
    	writer.newLine();
    	writer.write("}");
    	writer.close();
    }
    
    public static void deleteServerFormula(Long serverId) throws IOException {
    	File formula_file = new File(FORMULA_DATA_DIRECTORY + "pillar/" + ServerFactory.lookupById(serverId).getName() + ".json");
    	if (formula_file.exists()) {
    		formula_file.delete();
    	}
    }
    
    public static Optional<Formula> getFormulaByServerId(long orgId, Long serverId) {
    	File formula_file = new File(FORMULA_DATA_DIRECTORY + "pillar/" + ServerFactory.lookupById(serverId).getName() + ".json");
    	if (!formula_file.exists() || !formula_file.isFile())
    		return Optional.empty();
    	
    	try {
	    	FileInputStream fis = new FileInputStream(formula_file);
	        byte[] formula_file_data = new byte[(int) formula_file.length()];
	        fis.read(formula_file_data);
	        fis.close();
	        String file_content = new String(formula_file_data, "UTF-8");
	        Map<String, Object> map = (Map<String, Object>) GSON.fromJson(file_content, Map.class);
	        Map<String, Object> content = (Map<String, Object>) map.get("formula");
	        
	        Formula formula = new Formula();
	    	formula.setOrg(OrgFactory.lookupById(orgId));
	    	formula.setServerId(serverId);
	        formula.setFormulaName((String) content.remove("form_id"));
	    	formula.setContent(GSON.toJson(content));
	    	return Optional.of(formula);
    	}
    	catch (IOException e) {
    		return Optional.empty();
    	}
    }
    
    public static String getFormulasByServerGroup(String serverId) {
    	File server_formulas_file = new File(FORMULA_DATA_DIRECTORY + "server_formulas.json");
    	if (!server_formulas_file.exists())
    		return "none";
    	
    	// Read server_formulas file
    	try {
	    	FileInputStream fis = new FileInputStream(server_formulas_file);
	        byte[] server_formulas_file_content = new byte[(int) server_formulas_file.length()];
	        fis.read(server_formulas_file_content);
	        fis.close();
			Map<String, String> server_formulas = (Map<String, String>) GSON.fromJson(new String(server_formulas_file_content, "UTF-8"), Map.class);
			return server_formulas.getOrDefault(serverId, "none");
    	}
    	catch (IOException e) {
    		return "none";
    	}
    }
    
    public static void saveServerGroupFormulas(String serverGroupId, String selectedFormula) throws IOException {
    	File server_formulas_file = new File(FORMULA_DATA_DIRECTORY + "server_formulas.json");
    	
    	Map<String, String> server_formulas;
    	if (!server_formulas_file.exists()) {
    		server_formulas_file.getParentFile().mkdirs();
    		server_formulas_file.createNewFile();
    		server_formulas = new HashMap<String, String>();
    	}
    	else {
	    	// Read server_formulas file
	    	FileInputStream fis = new FileInputStream(server_formulas_file);
	        byte[] server_formulas_file_data = new byte[(int) server_formulas_file.length()];
	        fis.read(server_formulas_file_data);
	        fis.close();
			server_formulas = (Map<String, String>) GSON.fromJson(new String(server_formulas_file_data, "UTF-8"), Map.class);
    	}
		// Save selected Formula
		server_formulas.put(serverGroupId, selectedFormula);
		
		// Write server_formulas file
    	BufferedWriter writer = new BufferedWriter(new FileWriter(server_formulas_file));
    	writer.write(GSON.toJson(server_formulas));
    	writer.close();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Logger getLogger() {
        return log;
    }
}
