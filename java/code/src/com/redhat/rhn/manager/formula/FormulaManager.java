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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
/**
 * Singleton class, validates and save formulas' data.
 */
public class FormulaManager {

    private static FormulaManager instance;
    private SaltService saltService;
    private final String DEFAULT_KEY = "$default";
    private final String TYPE_KEY = "$type";
    private final String EDIT_GROUP = "edit-group";
    private FormulaManager() {
        saltService = SaltService.INSTANCE;
    }

    /**
     * get the singleton instance.
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
     * @param saltServiceIn to set
     */
    public void setSaltService(SaltService saltServiceIn) {
        this.saltService = saltServiceIn;
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
        saltService.refreshPillar(new MinionList(minion.get().getMinionId()));
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
        saltService.refreshPillar(new MinionList(minionIds));
    }

    /**
     * Get the saved formula data for the specific server.
     * @param user user
     * @param formulaName formula name for the formula for which data should be returned
     * @param serverId server Id of the server for which data should be returned
     * @return the saved data in map form
     */
    public Map<String, Object> getSystemFormulaData(User user, String formulaName, Long serverId) {
        Optional<MinionServer> minion = MinionServerFactory.lookupById(serverId);
        checkUserHasPermissionsOnServer(user, minion.get());
        Optional<Map<String, Object>> data = FormulaFactory.getFormulaValuesByNameAndServerId(formulaName, serverId);
        return data.orElse(Collections.emptyMap());
    }

    /**
     * Get the saved formula data for the specific group.
     * @param user user
     * @param formulaName formula name for the formula for which data should be returned
     * @param groupId groupId Id of the Group for which data should be returned
     * @return the saved data in map form
     */
    public Map<String, Object> getGroupFormulaData(User user, String formulaName, Long groupId) {
        ManagedServerGroup group = ServerGroupFactory.lookupByIdAndOrg(groupId, user.getOrg());
        checkUserHasPermissionsOnServerGroup(user, group);
        Optional<Map<String, Object>> data = FormulaFactory.getGroupFormulaValuesByNameAndGroupId(formulaName, groupId);
        return data.orElse(Collections.emptyMap());
    }

    /**
     * Validate the provided data
     * @param formulaName formula name
     * @param contents contents
     */
    public void validInput(String formulaName, Map<String, Object> contents) {
        Map<String, Object> layout = getFormulaLayout(formulaName);
        validateContents(contents, layout);
    }

    /**
     * Validate the formula contents.
     * @param contents contents
     * @param layout corresponding layout with the definition of formula
     */
    public void validateContents(Map<String, Object> contents, Map<String, Object> layout) {
        contents.forEach((String key, Object value) -> {
            checkForUndefinedFields(key, layout);
            boolean isEditGroup = isEditGroup(key, layout);
            //Edit Group should be handled different
            if (value instanceof Map && !isEditGroup) {
                validateContents((Map) value, (Map) layout.get(key));
            }
            else {
                Map<String, Object> def = (Map<String, Object>) layout.get(key);
                Optional.ofNullable(def.get(DEFAULT_KEY)).ifPresent(defaultValue-> {
                    Class<?> expectedClass = def.get(DEFAULT_KEY).getClass();
                    Class<?> actualClass = value.getClass();
                    validateTypes(key, actualClass, expectedClass);
                    validateEditGroups(key, value, isEditGroup, def, expectedClass);
                });
            }
        });
    }

    /**
     * Validate Edit groups based on its type
     * @param key key
     * @param value value
     * @param isEditGroup is item edit-group
     * @param def definition
     * @param expectedClass expected class
     */
    private void validateEditGroups(String key, Object value, boolean isEditGroup,
                                    Map<String, Object> def, Class<?> expectedClass) {
        if (isEditGroup) {
            if (List.class.isAssignableFrom(expectedClass)) {
                validateListContents(key, (List) value, (List<Object>) def.get(DEFAULT_KEY), def);
            }
            else if (Map.class.isAssignableFrom(expectedClass)) {
                validateDictionary((Map<String, Object>) value, def);
            }
        }
    }

    /**
     * Validate dictionary(Map) of given daata against the definition
     * @param editGroupDict dict
     * @param def definition
     * @return true if everything is fine
     */
    private boolean validateDictionary(Map<String, Object> editGroupDict, Map<String, Object> def) {
        Map<String, Object> prototype = (Map<String, Object>) def.get("$prototype");
        editGroupDict.forEach((k, v) -> {
            if (v instanceof Map) {
                Optional.ofNullable(prototype.get(k)).map(item -> validateDictionary((Map) v, (Map) item))
                        .orElseGet(() -> validateDictionary((Map) v, def));
            }
            else {
                Optional<Object> defaultValue = Opt.or(Optional.ofNullable(prototype.get(k))
                        .map(item -> ((Map) item).get(DEFAULT_KEY)), Optional.ofNullable(prototype.get(DEFAULT_KEY)));
                defaultValue.ifPresent(value -> validateTypes(k, v.getClass(), value.getClass()));
            }
        });
        return true;
    }

    /**
     * Validate List contents
     * @param key key
     * @param actualList data to be checked
     * @param expectedListFormat expected format
     * @param def definition
     */
    private void validateListContents(String key, List<Object> actualList, List<Object> expectedListFormat,
                                                                                      Map<String, Object> def) {
        Class<?> expectedClass = expectedListFormat.iterator().next().getClass();
        if (Map.class.isAssignableFrom(expectedClass)) {
            actualList.forEach(item -> validateDictionary((Map) item, def));
        }
        else {
            actualList.forEach(item -> validateTypes(key, expectedClass, item.getClass()));
        }
    }

    /**
     * Validate the actual data by comparing the classes(type)
     * @param key key for which data should be validated
     * @param actualClass actual class
     * @param expectedClass expected class
     */
    private  void validateTypes(String key, Class<?> actualClass, Class<?> expectedClass) {
        boolean isAssignable = expectedClass.isAssignableFrom(actualClass);
        if (!isAssignable) {
            throw new IllegalArgumentException("For " + key + ": wrong Type." +
                    " Expected " + expectedClass.getSimpleName() + " - Found " + actualClass.getSimpleName());
        }
    }

    /**
     * Check if provided data's key exist in definition
     * @param key key
     * @param layout form definition
     */
    private void checkForUndefinedFields(String key, Map<String, Object> layout) {
        if (Objects.isNull(layout.get(key))) {
            throw new IllegalArgumentException(key + " : doesn't exist in definition");
        }
    }

    /**
     * Check if the given item is of type 'edit-group'
     * @return
     */
    private boolean isEditGroup(String key, Map<String, Object> layout) {
        Map<String, Object> def = (Map<String, Object>) layout.get(key);
        return Optional.ofNullable(def.get(TYPE_KEY)).map(d->d.equals(EDIT_GROUP)).orElse(false);
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
