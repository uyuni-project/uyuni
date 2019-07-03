/**
 * Copyright (c) 2018 SUSE LLC
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
    private static final String DEFAULT_KEY = "$default";
    private static final String TYPE_KEY = "$type";
    private static final String EDIT_GROUP = "edit-group";
    private static final String PROTOTYPE = "$prototype";
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
     * This method is only for testing purpose.
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
        MinionServer minion = MinionServerFactory.lookupById(systemId)
                .orElseThrow(() -> new IllegalArgumentException("Minion " + systemId + " not found."));
        checkUserHasPermissionsOnServer(user, minion);
        FormulaFactory.saveServerFormulaData(content, minion.getMinionId(), formulaName);
        saltService.refreshPillar(new MinionList(minion.getMinionId()));
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
        FormulaFactory.saveGroupFormulaData(content, groupId, user.getOrg(), formulaName);
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
        MinionServer minion = MinionServerFactory.lookupById(serverId)
                .orElseThrow(() -> new IllegalArgumentException("Minion " + serverId + " not found."));
        checkUserHasPermissionsOnServer(user, minion);
        Optional<Map<String, Object>> data = FormulaFactory
                .getFormulaValuesByNameAndMinionId(formulaName, minion.getMinionId());
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
     * @throws InvalidFormulaException if actual data's type doesn't match with the expected
     */
    public void validateInput(String formulaName, Map<String, Object> contents) throws InvalidFormulaException {
        Map<String, Object> layout = getFormulaLayout(formulaName);
        validateContents(contents, layout);
    }

    /**
     * Validate the formula contents.
     * @param contents contents
     * @param layout corresponding layout with the definition of formula
     * @throws InvalidFormulaException if actual data's type doesn't match with the expected
     */
    public void validateContents(Map<String, Object> contents, Map<String, Object> layout)
            throws InvalidFormulaException {
        for (Map.Entry<String, Object> entry: contents.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            checkForUndefinedFields(key, layout);
            boolean isEditGroup = isEditGroup(key, layout);
            if (value instanceof Map && !isEditGroup) {
                validateContents((Map) value, (Map) layout.get(key));
            }
            else {
                Map<String, Object> def = (Map<String, Object>) layout.get(key);
                if (Objects.nonNull(def.get(DEFAULT_KEY))) {
                    Class<?> expectedClass = def.get(DEFAULT_KEY).getClass();
                    Class<?> actualClass = value.getClass();
                    validateTypes(key, actualClass, expectedClass);
                    validateEditGroups(key, value, isEditGroup, def, expectedClass);
                }
            }
        }
    }

    /**
     * Validate Edit groups based on its type
     * @param key key
     * @param value value
     * @param isEditGroup is item edit-group
     * @param def definition
     * @param expectedClass expected class
     * @throws InvalidFormulaException if actual data's type doesn't match with the expected
     */
    private void validateEditGroups(String key, Object value, boolean isEditGroup,
                                    Map<String, Object> def, Class<?> expectedClass) throws InvalidFormulaException {
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
     * @throws InvalidFormulaException if actual data's type doesn't match with the expected
     * @return true if everything is fine
     */
    private void validateDictionary(Map<String, Object> editGroupDict, Map<String, Object> def)
            throws InvalidFormulaException {
        Map<String, Object> prototype = (Map<String, Object>) def.get(PROTOTYPE);
        for (Map.Entry<String, Object> entry: editGroupDict.entrySet()) {
            String k = entry.getKey();
            Object v = entry.getValue();
            if (v instanceof Map) {
                if (Objects.nonNull(prototype.get(k))) {
                    validateDictionary((Map) v, (Map) prototype.get(k));
                }
                else {
                    validateDictionary((Map) v, def);
                }
            }
            else {
                Optional<Object> defaultValue = Opt.or(Optional.ofNullable(prototype.get(k))
                        .map(item -> ((Map) item).get(DEFAULT_KEY)), Optional.ofNullable(prototype.get(DEFAULT_KEY)));
                if (defaultValue.isPresent()) {
                    validateTypes(k, v.getClass(), defaultValue.get().getClass());
                }
            }
        }
    }

    /**
     * Validate List contents
     * @param key key
     * @param actualList data to be checked
     * @param expectedListFormat expected format
     * @param def definition
     * @throws InvalidFormulaException if actual data's type doesn't match with the expected
     */
    private void validateListContents(String key, List<Object> actualList, List<Object> expectedListFormat,
                                      Map<String, Object> def) throws InvalidFormulaException {
        Class<?> expectedClass = expectedListFormat.iterator().next().getClass();
        if (Map.class.isAssignableFrom(expectedClass)) {
            for (Object item: actualList) {
                validateDictionary((Map) item, def);
            }
        }
        else {
            for (Object item:actualList) {
                validateTypes(key, expectedClass, item.getClass());
            }
        }
    }

    /**
     * Validate the actual data by comparing the classes(type)
     * @param key key for which data should be validated
     * @param actualClass actual class
     * @param expectedClass expected class
     * @throws InvalidFormulaException if actual data's type doesn't match with the expected
     */
    private  void validateTypes(String key, Class<?> actualClass, Class<?> expectedClass)
            throws InvalidFormulaException {
        boolean isAssignable = expectedClass.isAssignableFrom(actualClass);
        if (!isAssignable) {
            throw new InvalidFormulaException("For " + key + ": wrong Type." +
                    " Expected " + expectedClass.getSimpleName() + " - Found " + actualClass.getSimpleName());
        }
    }

    /**
     * Check if provided data's key exist in definition
     * @param key key
     * @param layout form definition
     * @throws InvalidFormulaException if actual data's type doesn't match with the expected
     */
    private void checkForUndefinedFields(String key, Map<String, Object> layout) throws InvalidFormulaException {
        if (Objects.isNull(layout.get(key))) {
            throw new InvalidFormulaException(key + " : doesn't exist in definition");
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
     * Check if the given formula is assigned to the specified system
     * @param formulaName formulaName
     * @param systemId systemId
     * @return True/False based upon if all of the systems has formulas assigned to them
     */
    public boolean hasSystemFormulaAssigned(String formulaName, Integer systemId) {
        return FormulaFactory
                        .getCombinedFormulasByServerId(systemId.longValue())
                        .contains(formulaName);
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
    private Map<String, Object> getFormulaLayout(String formulaName) {
        return FormulaFactory.getFormulaLayoutByName(formulaName).orElseGet(Collections::emptyMap);
    }

    /**
     * Check if user has permission on group
     * @param user user
     * @param group group
     */
    private void checkUserHasPermissionsOnServerGroup(User user, ServerGroup group) {
        ServerGroupManager.getInstance().validateAccessCredentials(user, group, group.getName());
        ServerGroupManager.getInstance().validateAdminCredentials(user);
    }

    /**
     * Check if user has permission on the server.
     * @param user user
     * @param server server
     */
    private void checkUserHasPermissionsOnServer(User user, Server server) {
        SystemManager.ensureAvailableToUser(user, server.getId());
    }
}
