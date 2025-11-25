/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package org.cobbler;

import com.redhat.rhn.common.util.StringUtil;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


/**
 * Base class has attributes common to distros, profiles, system records.
 *
 * @author paji
 * @see <a href="https://cobbler.readthedocs.io/en/v3.3.3/code-autodoc/cobbler.items.html#module-cobbler.items.item">RTFD - Cobbler - 3.3.3 - Item</a>
 */
public abstract class CobblerObject {
    /**
     * Constant to define the field name for the comment
     */
    protected static final String COMMENT = "comment";
    /**
     * Constant to define the field name for the owners
     */
    protected static final String OWNERS = "owners";
    /**
     * Constant to define the field name for the creation time of the object
     */
    protected static final String CTIME = "ctime";
    /**
     * Constant to define the field name for the getter of kernel post options
     */
    protected static final String KERNEL_OPTIONS_POST = "kernel_options_post";
    /**
     * Constant to define the field name for the setter of the kernel post options
     */
    protected static final String SET_KERNEL_OPTIONS_POST = "kernel_options_post";
    /**
     * Constant to define the field name for the logical object depth in the inheritance
     */
    protected static final String DEPTH = "depth";
    /**
     * Constant to define the field name for the getter of the kernel options
     */
    protected static final String KERNEL_OPTIONS = "kernel_options";
    /**
     * Constant to define the field name for the setter of the kernel options
     */
    protected static final String SET_KERNEL_OPTIONS = "kernel_options";
    /**
     * Constant to define the field name for name of an object
     */
    protected static final String NAME = "name";
    /**
     * Constant to define the field name for the getter of the autoinstallation metadata
     */
    protected static final String KS_META = "autoinstall_meta";
    /**
     * Constant to define the field name for the setter of the autoinstallation metadata
     */
    protected static final String SET_KS_META = "autoinstall_meta";
    /**
     * Constant to define the field name for the Cobbler parent property
     */
    protected static final String PARENT = "parent";
    /**
     * Constant to define the field name for the Cobbler modification time property
     */
    protected static final String MTIME = "mtime";
    /**
     * Constant to define the field name for the Cobbler management classes property
     */
    protected static final String MGMT_CLASSES = "mgmt_classes";
    /**
     * Constant to define the field name for the Cobbler template files property
     */
    protected static final String TEMPLATE_FILES = "template_files";
    /**
     * Constant to define the field name for the Cobbler uid property
     */
    protected static final String UID = "uid";
    /**
     * Constant to define the field name for the Cobbler redhat management key property
     */
    private static final String REDHAT_KEY = "redhat_management_key";
    /**
     * Constant to define the value Cobbler uses for inheritance
     */
    public static final String INHERIT_KEY = "<<inherit>>";

    /**
     * Holds the identifier for the XML-RPC API
     */
    protected String handle;
    /**
     * The map with the raw data that an object has assigned to itself
     */
    protected Map<String, Object> dataMap = new HashMap<>();
    /**
     * The map with the resolved data that is combined from all objects down the inheritance chain
     */
    protected Map<String, Object> dataMapResolved = new HashMap<>();
    /**
     * The connection to the Cobbler server
     */
    protected CobblerConnection client;

    /**
     * Helper method used by all cobbler objects to
     * return a version of themselves by UID
     *
     * @param client     the Cobbler Connection
     * @param id         the UID of the distro/profile/system record
     * @param findMethod the find XML-RPC method, eg: find_distro
     * @return true if the cobbler object was found.
     * @see org.cobbler.Distro#lookupById for example usage.
     */
    protected static Map<String, Object> lookupDataMapById(CobblerConnection client,
                                                           String id, String findMethod) {
        if (id == null) {
            return null;
        }
        List<Map<String, Object>> objects = lookupDataMapsByCriteria(client,
                UID, id, findMethod);
        if (!objects.isEmpty()) {
            return objects.get(0);
        }
        return null;

    }

    /**
     * look up data maps by a certain criteria
     *
     * @param client     the XML-RPC client
     * @param critera    (i.e. uid profile, etc..)
     * @param value      the value of the criteria
     * @param findMethod the find method to use (find_system, find_profile)
     * @return List of maps
     */
    @SuppressWarnings("unchecked")
    protected static List<Map<String, Object>> lookupDataMapsByCriteria(
            CobblerConnection client, String critera, String value, String findMethod) {
        if (value == null) {
            return null;
        }

        Map<String, String> criteria = new HashMap<>();
        criteria.put(critera, value);
        return (List<Map<String, Object>>)
                client.invokeTokenMethod(findMethod, criteria, true);


    }


    /**
     * Helper method used by all cobbler objects to return a Map of themselves
     * by name.
     *
     * @param client       the Cobbler Connection
     * @param name         the name of the cobbler object
     * @param lookupMethod the name of the XML-RPC
     *                     method to lookup: eg get_profile for profile
     * @return the Cobbler Object Data Map or null
     * @see org.cobbler.Distro#lookupByName for example usage..
     */
    @SuppressWarnings("unchecked")
    protected static Map<String, Object> lookupDataMapByName(CobblerConnection client,
                                                             String name, String lookupMethod, Object... args) {
        Object obj = client.invokeTokenMethod(lookupMethod, name, args[0], args[1]);
        if ("~".equals(obj)) {
            return null;
        }
        Map<String, Object> map = (Map<String, Object>) obj;
        if (map == null || map.isEmpty()) {
            return null;
        }
        return map;
    }

    /**
     * This method executes the Cobbler server side modification with a raw value
     *
     * @param key   The key to modify. This normally is one of the predefined constants
     * @param value The value to modify.
     */
    protected abstract void invokeModify(String key, Object value);

    /**
     * This method executes the Cobbler server side modification with a resolved value
     *
     * @param key   The key to modify. This normally is one of the predefined constants
     * @param value The value to modify.
     */
    protected abstract void invokeModifyResolved(String key, Object value);

    /**
     * This method saves the entire object that is cached Cobbler server side to the disk.
     */
    protected abstract void invokeSave();

    /**
     * This method removes the object from the Cobbler server
     *
     * @return Whether the removal of the object was successful or not
     */
    protected abstract boolean invokeRemove();

    /**
     * This method retrieves the XML-RPC handle from the Cobbler server via the objects name
     *
     * @return The XML-RPC handle. If the handle has a {@code ___NEW___} prefix it was not saved to disk.
     */
    protected abstract String invokeGetHandle();

    /**
     * This method forgets the local state of the object and loads the current state from the Cobbler server
     */
    protected abstract void reload();

    /**
     * This method renames the current object
     * <p>
     * Modifying the name property directly will not work as expected.
     *
     * @param newName The new name for the object
     */
    protected abstract void invokeRename(String newName);

    /**
     * This method retrieves the resolved value for an object. This is
     * different from the raw value in the sense that some properties in
     * Cobbler have the ability to be resolved to either a parent objects
     * value or the application Settings.
     *
     * @param key The constant for the property of the field name in Cobbler
     * @return The resolved value or in case an attribute doesn't resolve its raw value
     */
    protected final Object getResolvedValue(String key) {
        return client.invokeMethod("get_item_resolved_value", getUid(), key);
    }

    /**
     * This helper function checks if the Optional we pass is empty or not. The mechanism in Java works that an Empty
     * means that the object does not have a dedicated value and thus inherits from the parent or the settings.
     * <p>
     * Using this makes only sense in Setters where the corresponding Getter has a {@code cobbler.inheritable} tag set.
     *
     * @param key The property to modify
     * @param valueIn The new value for the property
     * @param <T> The type of the property that can be inherited
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected <T> void modifyRawHelper(String key, Optional<T> valueIn) {
        if (valueIn.isEmpty()) {
            modify(key, INHERIT_KEY);
            return;
        }
        modify(key, valueIn.get());
    }

    /**
     * This helper function will automatically convert the Inherit key to an empty Optional so that this step doesn't
     * have to be repeated.
     * <p>
     * Using this makes only sense in Setters where the corresponding Getter has a {@code cobbler.inheritable} tag set.
     *
     * @param key The property name that should be retrieved
     * @param <T> The type of the property
     */
    @SuppressWarnings("unchecked")
    protected <T> Optional<T> retrieveOptionalValue(String key) {
        if (String.valueOf(dataMap.get(key)).equals(INHERIT_KEY)) {
            return Optional.empty();
        }
        return Optional.of((T) dataMap.get(key));
    }

    /**
     * Gets the XML-RPC handle internal to Cobbler
     *
     * @return The handle for Cobbler. If the Item is not saved to disk it will be prefixed with {@code ___NEW___}.
     */
    protected String getHandle() {
        if (isBlank(handle)) {
            handle = invokeGetHandle();
        }
        return handle;
    }

    /**
     * This method modifies the object on Cobbler server side.
     *
     * @param key   The property name. Normally this is one of the constants defined above.
     * @param value The new value for the property. This must be a "raw" object value and not a resolved one.
     */
    protected void modify(String key, Object value) {
        modify(key, value, !client.isInTransaction());
    }

    /**
     * This method modifies the object on Cobbler server side.
     *
     * @param key   The property name. Normally this is one of the constants defined above.
     * @param value The new value for the property. This must be a "raw" object value and not a resolved one.
     * @param updateResolved Whether to update the resolved value in our internal Map. This should only be set to
     *                       false if you create a new object or in a transaction.
     */
    protected void modify(String key, Object value, boolean updateResolved) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException(
                    "CobblerObject.modify cannot be null or emtpy! Must be a non-emtpy string!"
            );
        }
        invokeModify(key, value);
        dataMap.put(key, value);
        if (updateResolved) {
            refreshResolved(key);
        }
    }

    /**
     * Refreshes the memory internal Map with the values from the server.
     *
     * @param key The key to update.
     */
    protected void refreshResolved(String key) {
        if (getUid() == null) {
            throw new RuntimeException("getUid() was null!");
        }
        if (key.equals(SystemRecord.SET_INTERFACES)) {
            // This exception is needed here because the API Client cannot yet work with the new style property on
            // Cobbler for Network interfaces. Since the network interface handling has not been polished, this is
            // the most reasonable way to add this special case.
            key = "interfaces";
        }
        Object resolvedValue = client.invokeMethod("get_item_resolved_value", getUid(), key);
        dataMapResolved.put(key, resolvedValue);
    }

    /**
     * This method modifies the object on Cobbler server side. It removes
     * duplicated content that is inherited from other objects. As this
     * function causes a lot of overhead on the server, please use it only when
     * needed.
     *
     * @param key   The property name. Normally this is one of the constants
     *              defined above.
     * @param value The new value for the property. This may be a "raw" object
     *              value or a resolved one.
     */
    protected void modifyResolved(String key, Object value) {
        invokeModifyResolved(key, value);
        dataMapResolved.put(key, value);
    }

    /**
     * Calls save object to complete the commit
     */
    public void save() {
        invokeSave();
    }

    /**
     * Removes the kickstart object from cobbler.
     *
     * @return true if successful
     */
    public boolean remove() {
        return invokeRemove();
    }


    /**
     * Getter for the comment
     *
     * @return the comment
     */
    public String getComment() {
        return (String) dataMap.get(COMMENT);
    }


    /**
     * Setter for the comment
     *
     * @param commentIn the comment to set
     */
    public void setComment(String commentIn) {
        modify(COMMENT, commentIn);
    }

    /**
     * Getter for the management classes in their raw form
     *
     * @return the managementClasses
     * @cobbler.inheritable This property can have the value {@link #INHERIT_KEY} and thus has an accompanying
     *                      method {@link #getResolvedManagementClasses()}.
     */
    public Optional<List<String>> getManagementClasses() {
        return this.<List<String>>retrieveOptionalValue(MGMT_CLASSES);
    }

    /**
     * Getter for the management classes in their resolved form
     *
     * @return the managementClasses
     */
    @SuppressWarnings("unchecked")
    public List<String> getResolvedManagementClasses() {
        return (List<String>) dataMapResolved.get(MGMT_CLASSES);
    }

    /**
     * Setter for the management classes in their raw form.
     *
     * @param managementClassesIn the managementClasses to set
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public void setManagementClasses(Optional<List<String>> managementClassesIn) {
        this.<List<String>>modifyRawHelper(MGMT_CLASSES, managementClassesIn);
    }

    /**
     * Setter for the management classes in their resolved form
     *
     * @param managementClassesIn the managementClasses to set
     */
    public void setResolvedManagementClasses(List<String> managementClassesIn) {
        modifyResolved(MGMT_CLASSES, managementClassesIn);
    }


    /**
     * Getter for the template files
     *
     * @return the templateFiles
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getTemplateFiles() {
        return (Map<String, String>) dataMap.get(TEMPLATE_FILES);
    }


    /**
     * Setter for the template files
     *
     * @param templateFilesIn the templateFiles to set
     */
    public void setTemplateFiles(Map<String, String> templateFilesIn) {
        modify(TEMPLATE_FILES, templateFilesIn);
    }


    /**
     * Getter for the uid
     *
     * @return the uid
     */
    public String getUid() {
        return (String) dataMap.get(UID);
    }

    /**
     * Alias for the uid
     *
     * @return the uid
     */
    public String getId() {
        return getUid();
    }

    /**
     * Setter for the uid
     *
     * @param uidIn the uid to set
     */
    public void setUid(String uidIn) {
        modify(UID, uidIn);
    }


    /**
     * Getter for the Parent of the object
     *
     * @return the parent
     */
    public String getParent() {
        return (String) dataMap.get(PARENT);
    }


    /**
     * Setter for the Parent of the object
     *
     * @param parentIn the parent to set
     */
    public void setParent(String parentIn) {
        modify(PARENT, parentIn);
    }

    /**
     * Getter for the owners.
     *
     * @return the owners
     * @cobbler.inheritable This field can have the value {@link #INHERIT_KEY} and thus has an accompanying resolved
     *                      method {@link #getResolvedOwners()}.
     */
    public Optional<List<String>> getOwners() {
        return this.<List<String>>retrieveOptionalValue(OWNERS);
    }

    /**
     * Getter to retrieve the resolved owners
     *
     * @return the owners
     */
    @SuppressWarnings("unchecked")
    public List<String> getResolvedOwners() {
        return (List<String>) dataMapResolved.get(OWNERS);
    }

    /**
     * Setter for the owners
     *
     * @param ownersIn the owners to set
     * @cobbler.inheritable This field can have the value {@link #INHERIT_KEY} and thus has an accompanying resolved
     *                      method {@link #setResolvedOwners(List)} ()}.
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public void setOwners(Optional<List<String>> ownersIn) {
        this.<List<String>>modifyRawHelper(OWNERS, ownersIn);
    }

    /**
     * Setter for the resolved owners
     *
     * @param ownersIn the owners to set
     */
    public void setResolvedOwners(List<String> ownersIn) {
        modifyResolved(OWNERS, ownersIn);
    }

    /**
     * Getter for the object creation time
     *
     * @return the created
     */
    public Date getCreated() {
        Double time = (Double) dataMap.get(CTIME);
        // cobbler deals with seconds since epoch, Date expects milliseconds. Convert.
        return new Date(time.longValue() * 1000);
    }

    /**
     * Setter for the object creation time
     *
     * @param createdIn the created to set
     */
    public void setCreated(Date createdIn) {
        // cobbler deals with seconds since epoch, Date returns milliseconds. Convert.
        modify(CTIME, (double) (createdIn.getTime() / 1000F));
    }

    /**
     * Getter for the last modified date
     *
     * @return the modified
     */
    public Date getModified() {
        Double time = (Double) dataMap.get(MTIME);
        // cobbler deals with seconds since epoch, Date expects milliseconds. Convert.
        return new Date(time.longValue() * 1000);
    }

    /**
     * Setter for the modified date
     *
     * @param modifiedIn the modified to set
     */
    public void setModified(Date modifiedIn) {
        // cobbler deals with seconds since epoch, Date returns milliseconds. Convert.
        modify(MTIME, (double) (modifiedIn.getTime() / 1000F));
    }

    /**
     * Getter for the object depth
     *
     * @return the depth
     */
    public int getDepth() {
        return (Integer) dataMap.get(DEPTH);
    }

    /**
     * Setter for the object depth
     *
     * @param depthIn the depth to set
     */
    public void setDepth(int depthIn) {
        modify(DEPTH, depthIn);
    }


    /**
     * Getter for the kernel options
     *
     * @return the kernelOptions
     * @cobbler.inheritable This can be inherited from the parent object(s) and the settings. Since this is a dict in
     *                      Python this can be resolved through the whole chain.
     */
    public Optional<Map<String, Object>> getKernelOptions() {
        return this.<Map<String, Object>>retrieveOptionalValue(KERNEL_OPTIONS);
    }

    /**
     * Gets resolved kernel options as a dictionary
     * <p>
     * The resolved value includes all the options inherited from above.
     *
     * @return the kernel option map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getResolvedKernelOptions() {
        return (Map<String, Object>) getResolvedValue(KERNEL_OPTIONS);
    }

    /**
     * Getter for the post kernel options
     *
     * @return the kernelOptionsPost
     * @cobbler.inheritable This can be inherited from the parent object(s). Since this is a dict in Python this can be
     *                      resolved through the whole chain.
     */
    public Optional<Map<String, Object>> getKernelOptionsPost() {
        return this.<Map<String, Object>>retrieveOptionalValue(KERNEL_OPTIONS_POST);
    }

    /**
     * Gets resolved kernel post options as a dictionary
     * <p>
     * The resolved value includes all the options inherited from above.
     *
     * @return the kernel post option map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getResolvedKernelOptionsPost() {
        return (Map<String, Object>) getResolvedValue(KERNEL_OPTIONS_POST);
    }

    /**
     * Converts a Java Map to a String that can be understood by Cobbler and then be converted to a Dictionary.
     *
     * @param map The map to convert
     * @return The intended String
     */
    @SuppressWarnings("unchecked")
    public String convertOptionsMap(Map<String, Object> map) {
        StringBuilder string = new StringBuilder();
        for (String key : map.keySet()) {
            List<String> keyList;
            try {
                keyList = (List<String>) map.get(key);
            }
            catch (ClassCastException e) {
                keyList = new ArrayList<>();
                keyList.add((String) map.get(key));
            }
            if (keyList.isEmpty()) {
                string.append(key).append(" ");
            }
            else {
                for (String value : keyList) {
                    string.append(key).append("=");
                    if (value != null && value.contains(" ")) {
                        string.append('"').append(value).append('"');
                    }
                    else {
                        string.append(value);
                    }
                    string.append(" ");
                }
            }
        }
        return string.toString().strip();
    }

    /**
     * Setter for the kernel options
     *
     * @param kernelOptionsIn the kernelOptions to set
     * @param <T> The type you want to use for the kernel options. Must be either a {@code String} or
     *            {@code Map<String, Object>}.
     * @see #getKernelOptions()
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public <T> void setKernelOptions(Optional<T> kernelOptionsIn) {
        if (kernelOptionsIn.isEmpty()) {
            modify(SET_KERNEL_OPTIONS, INHERIT_KEY);
            return;
        }
        T kernelOptionsValue = kernelOptionsIn.get();
        if (kernelOptionsValue instanceof String || kernelOptionsValue instanceof Map) {
            modify(SET_KERNEL_OPTIONS, kernelOptionsValue);
            return;
        }
        throw new IllegalArgumentException("Kernel Options must either be String or Map!");
    }

    /**
     * Setter for the resolved kernel options
     *
     * @param <T> The type you want to use for the kernel options. Must be either a {@code String} or
     *            {@code Map<String, Object>}.
     * @param kernelOptionsIn the kernelOptions to set
     * @see #getKernelOptions()
     */
    public <T> void setResolvedKernelOptions(T kernelOptionsIn) {
        if (kernelOptionsIn instanceof String || kernelOptionsIn instanceof Map) {
            modifyResolved(SET_KERNEL_OPTIONS, kernelOptionsIn);
        }
        throw new IllegalArgumentException("Kernel Options must either be String or Map!");
    }

    /**
     * Setter for the kernel post options via {@link #INHERIT_KEY} or as a
     * string that is splittable by Pythons {@code shelx.split} function.
     *
     * @param kernelOptionsPostIn the kernelOptionsPost to set
     * @param <T> The type you want to use for the post kernel options. Must be either a {@code String} or
     *            {@code Map<String, Object>}.
     * @see <a href="https://docs.python.org/3/library/shlex.html#shlex.split">Python - shlex.split</a>
     * @see #getKernelOptionsPost()
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public <T> void setKernelOptionsPost(Optional<T> kernelOptionsPostIn) {
        if (kernelOptionsPostIn.isEmpty()) {
            if (this instanceof Distro) {
                throw new IllegalArgumentException("Kernel Options Post cannot be set to inherit on a Distro!");
            }
            modify(SET_KERNEL_OPTIONS_POST, INHERIT_KEY);
            return;
        }
        T kernelOptionsPostValue = kernelOptionsPostIn.get();
        if (kernelOptionsPostValue instanceof String || kernelOptionsPostValue instanceof Map) {
            modify(SET_KERNEL_OPTIONS_POST, kernelOptionsPostValue);
            return;
        }
        throw new IllegalArgumentException("Kernel Options Post must either be String or Map!");
    }

    /**
     * Setter for the resolved kernel options
     *
     * @param kernelOptionsPostIn The new kernel options. It is attempted to deduplicate this through the chain upwards.
     * @see #getKernelOptionsPost()
     */
    public void setResolvedKernelOptionsPost(Map<String, Object> kernelOptionsPostIn) {
        modifyResolved(SET_KERNEL_OPTIONS_POST, kernelOptionsPostIn);
    }

    /**
     * Retrieves the raw auto-installation metadata for the object.
     *
     * @return The kernelMeta. It could be that this returns {@link #INHERIT_KEY} instead of a Map.
     * @cobbler.inheritable This property has a matching resolved method. {@link #getResolvedAutoinstallMeta()}
     */
    public Optional<Map<String, Object>> getKsMeta() {
        return this.<Map<String, Object>>retrieveOptionalValue(KS_META);
    }

    /**
     * Retrieves the resolved auto-installation metadata for the object.
     *
     * @return the kernelMeta
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getResolvedAutoinstallMeta() {
        return (Map<String, Object>) dataMapResolved.get(KS_META);
    }

    /**
     * Setter that modifies the autoinstall meta field for the object with a raw value
     *
     * @param kernelMetaIn the kernelMeta to set
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public void setKsMeta(Optional<Map<String, Object>> kernelMetaIn) {
        this.<Map<String, Object>>modifyRawHelper(SET_KS_META, kernelMetaIn);
    }

    /**
     * Setter that modifies the autoinstall meta field for the object with a resolved value
     *
     * @param kernelMetaIn the kernelMeta to set
     */
    public void setResolvedAutoinstallMeta(Map<String, Object> kernelMetaIn) {
        modifyResolved(SET_KS_META, kernelMetaIn);
    }

    /**
     * Getter for the name property of a Cobbler object
     *
     * @return the name
     */
    public String getName() {
        return (String) dataMap.get(NAME);
    }

    /**
     * Setter for the name property of a Cobbler object
     *
     * @param nameIn sets the new name
     */
    public void setName(String nameIn) {
        invokeRename(nameIn);
        dataMap.put(NAME, nameIn);
        dataMapResolved.put(NAME, nameIn);
        handle = null;
        handle = getHandle();
        reload();
    }

    /**
     * Helper method to check if a string is blank or not
     *
     * @param str The String to check.
     * @return True if after trimming the String is of zero length. If instead of a String null was passed this method
     * will also return True. All other cases return False.
     */
    protected boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "DataMap = " + dataMap;
    }

    /**
     * Setter for the Red Hat management key with a String
     *
     * @param key the Red Hat activation key
     * @see #getRedHatManagementKey()
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public void setRedHatManagementKey(Optional<String> key) {
        this.<String>modifyRawHelper(REDHAT_KEY, key);
    }

    /**
     * Setter for the Red Hat management key with a Set of Strings that are comma delimited
     *
     * @param keys the Red Hat activation keys in a set
     * @see #getRedHatManagementKey()
     */
    public void setRedHatManagementKey(Set<String> keys) {
        modify(REDHAT_KEY, StringUtils.defaultString(StringUtil.join(",", keys)));
    }

    /**
     * Setter for the Red Hat management key in its resolved form
     *
     * @param key The key to set.
     * @see #getRedHatManagementKey()
     */
    public void setResolvedRedHatManagementKey(String key) {
        modifyResolved(REDHAT_KEY, key);
    }

    /**
     * Get the Red Hat management key
     * <p>
     * This is used in the context of a
     * {@link com.redhat.rhn.domain.kickstart.KickstartSession} to represent
     * the currently attempted installation. The data is stored as a comma
     * separated string in Cobbler.
     *
     * @return returns the red hat key(s) as a string
     * @cobbler.inheritable The inheritance in this case behaves like a fallback to the most parent object. If the most
     *                      parent object is a distro, the default key from the {@code settings.yaml} is used.
     */
    public Optional<String> getRedHatManagementKey() {
        return this.<String>retrieveOptionalValue(REDHAT_KEY);
    }

    /**
     * Getter for the resolved RedHat Management Key
     *
     * @return The RedHat Management Key resolved down the inheritance chain.
     * @see #getRedHatManagementKey()
     */
    public String getResolvedRedHatManagementKey() {
        return (String) dataMapResolved.get(REDHAT_KEY);
    }

    /**
     * Get the Red Hat management key as a Set of keys
     *
     * @return returns the red hat key as a string
     * @see #getRedHatManagementKey()
     */
    public Set<String> getRedHatManagementKeySet() {
        Optional<String> key = getRedHatManagementKey();
        if (key.isEmpty() || key.get().isBlank()) {
            return new HashSet<>();
        }
        String keys = StringUtils.defaultString(key.get());
        String[] sets = (keys).split(",");
        return new HashSet<>(Arrays.asList(sets));
    }

    /**
     * Remove the specified keys from the key set and add the specified set
     *
     * @param keysToRemove list of tokens to remove
     * @param keysToAdd    list of tokens to add
     * @see #getRedHatManagementKey()
     */
    public void syncRedHatManagementKeys(Collection<String> keysToRemove,
                                         Collection<String> keysToAdd) {
        Set<String> keySet = getRedHatManagementKeySet();
        if (keysToRemove != null) {
            keySet.removeAll(keysToRemove);
        }
        if (keysToAdd != null) {
            keySet.addAll(keysToAdd);
        }
        if (keySet.size() > 1 && keySet.contains(INHERIT_KEY)) {
            keySet.remove(INHERIT_KEY);
        }
        else if (keySet.isEmpty()) {
            keySet.add(INHERIT_KEY);
        }
        setRedHatManagementKey(keySet);
    }

}
