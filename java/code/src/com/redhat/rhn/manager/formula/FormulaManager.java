/**
 * Copyright (c) 2017 SUSE LLC
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
package com.redhat.rhn.manager.formula;

import com.redhat.rhn.domain.formula.FormulaFactory;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.utils.Opt;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
/**
 * Singleton class, validates and save formulas' data.
 */
public class FormulaManager {

    private static FormulaManager instance;

    private FormulaManager() {
    }

    /**
     * Gets the instance.
     *
     * @return instance
     */

    public static synchronized FormulaManager getInstance() {
        if (instance == null) {
            instance = new FormulaManager();
        }
        return instance;
    }

    /**
     * Save the formula data for the given system.
     * @param user user
     * @param systemId systemId
     * @param formulaName formulaName
     * @param content content
     * @throws IOException IOException
     */
    public void saveServerFormulaData(User user, Long systemId, String formulaName, Map<String, Object> content)
            throws IOException {
        Optional<MinionServer> minion = MinionServerFactory.lookupById(systemId);
        checkUserHasPermissionsOnServer(user, minion.get());
        FormulaFactory.saveServerFormulaData(content, systemId, formulaName);
        SaltService.INSTANCE.refreshPillar(new MinionList(minion.get().getMinionId()));
    }

    /**
     * Save the formula data for the group
     * @param user user
     * @param groupId group Id
     * @param formulaName formula name
     * @param content contents
     * @throws IOException IOException
     */
    public void saveGroupFormulaData(User user, Long groupId, String formulaName, Map<String, Object> content)
            throws IOException {

        ManagedServerGroup group = ServerGroupFactory.lookupByIdAndOrg(groupId, user.getOrg());
        checkUserHasPermissionsOnServerGroup(user, group);
        FormulaFactory.saveGroupFormulaData(content, groupId, formulaName);
        List<String> minionIds = group.getServers().stream()
                .flatMap(s -> Opt.stream(s.asMinionServer()))
                .map(MinionServer::getMinionId).collect(Collectors.toList());
        SaltService.INSTANCE.refreshPillar(new MinionList(minionIds));
    }

    /**
     * Validate the provided data
     * @param formulaName formula name
     * @param contents contents
     */
    public void validInput(String formulaName, Map<String, Object> contents) {
        Map<String, Object> layout = getFormulaLayout(formulaName);
        validateFormulaName(formulaName, contents);
        validateContents(contents, layout);
    }

    /**
     * check if formula name matches with the contents
     * @param formulaName formula name
     * @param contents contents
     */
    public void validateFormulaName(String formulaName, Map<String, Object> contents) {
        if (!contents.keySet().stream().findFirst().get().equalsIgnoreCase(formulaName)) {
            throw new IllegalArgumentException("Formula and contents doesn't match");
        }
    }

    /**
     * Validate the formula contents.
     * @param contents contents
     * @param layout corresponding layout with the definition of formula
     */
    public void validateContents(Map<String, Object> contents, Map<String, Object> layout) {
        contents.forEach((String k, Object v) -> {
            if (v instanceof Map) {
                validateContents((HashMap) v, (HashMap) layout.get(k));
            }
            else {
                Map<String, Object> def = (Map<String, Object>) layout.get(k);
                Class<?> expectedClass = def.get("$default").getClass();
                Class<?> actualClass = v.getClass();
                boolean isAssignable = expectedClass.isAssignableFrom(actualClass);
                if (!isAssignable) {
                    throw new IllegalArgumentException("For " + k + ": wrong Type." +
                            " Expected " + expectedClass + "- Found " + actualClass);
                }
            }
        });
    }

    /**
     * Check if given formula is assigned to the specificied list of systems
     * @param formulaName formulaName
     * @param systemIds systemIds
     * @return True/False based upon if all of the systems has formulas assigned to them
     */
    public boolean hasSystemsFormulaAssigned(String formulaName, List<Integer> systemIds) {
        return !systemIds.stream().filter(sid ->
                !FormulaFactory.getCombinedFormulasByServerId(sid.longValue()).contains(formulaName))
                .findFirst().isPresent();
    }

    /**
     * Check if given formula is assigned to the specificied group
     * @param formulaName formulaName
     * @param groupId groupId
     * @return True/False based upon group has formulas assigned to it
     */
    public boolean hasGroupFormulaAssigned(String formulaName, Long groupId) {
        return FormulaFactory.getFormulasByGroupId(groupId).contains(formulaName);

    }

    /**
     * Get formula Layout by name
     * @param formulaName formulaName
     * @return the lay out definition if exist else empty map
     */
    public Map<String, Object> getFormulaLayout(String formulaName) {
        return FormulaFactory.getFormulaLayoutByName(formulaName).orElseGet(Collections::emptyMap);
    }

    /**
     * Check if user has permission on group
     * @param user user
     * @param group group
     */
    public void checkUserHasPermissionsOnServerGroup(User user, ServerGroup group) {
        ServerGroupManager.getInstance().validateAccessCredentials(user, group, group.getName());
        ServerGroupManager.getInstance().validateAdminCredentials(user);
    }

    /**
     * Check if user has permission on the server.
     * @param user user
     * @param server server
     */
    public void checkUserHasPermissionsOnServer(User user, Server server) {
        SystemManager.ensureAvailableToUser(user, server.getId());
    }
}
