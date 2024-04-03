/*
 * Copyright (c) 2009--2011 Red Hat, Inc.
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

package org.cobbler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


/**
 * Cobbler System
 *
 * @see <a href="https://cobbler.readthedocs.io/en/v3.3.3/code-autodoc/cobbler.items.html#module-cobbler.items.system">RTFD - Cobbler - 3.3.3 - System</a>
 */
public class SystemRecord extends CobblerObject {
    /**
     * Constant to define the field name for hostname of a System
     */
    private static final String HOSTNAME = "hostname";
    /**
     * Constant to define the field name for name servers of a System
     */
    private static final String NAME_SERVERS = "name_servers";
    /**
     * Constant to define the field name for the gateway of a System
     */
    private static final String GATEWAY = "gateway";
    /**
     * Constant to define the field name for the profile of a System
     */
    private static final String PROFILE = "profile";
    /**
     * Constant to define the field name for the server of a System
     */
    private static final String SERVER = "server";
    /**
     * Constant to define the field name for the virtual bridge of a System
     */
    private static final String VIRT_BRIDGE = "virt_bridge";
    /**
     * Constant to define the field name for the virtual CPUs of a System
     */
    private static final String VIRT_CPUS = "virt_cpus";
    /**
     * Constant to define the field name for the virtual machine type of a System
     */
    private static final String VIRT_TYPE = "virt_type";
    /**
     * Constant to define the field name for the path to the VM image of a System
     */
    private static final String VIRT_PATH = "virt_path";
    /**
     * Constant to define the field name for the virtual machine image size of a System
     */
    private static final String VIRT_FILE_SIZE = "virt_file_size";
    /**
     * Constant to define the field name for the virtual RAM of a System
     */
    private static final String VIRT_RAM = "virt_ram";
    /**
     * Constant to define the field name for the enabled netboot of a System
     */
    private static final String NETBOOT_ENABLED = "netboot_enabled";
    /**
     * Constant to define the field name for the redhat management server of a System
     */
    public static final String REDHAT_MGMT_SERVER = "redhat_management_server";
    /**
     * Constant to define the field name for the setter of the interfaces of a System
     */
    public static final String SET_INTERFACES = "modify_interface";
    /**
     * Constant to define the field name for the getter of the interfaces of a System
     */
    private static final String GET_INTERFACES = "interface";
    /**
     * Constant to define the field name for the ipv6 autoconfiguration of a System
     */
    private static final String IPV6_AUTOCONF = "ipv6_autoconfiguration";

    /**
     * Image key.
     */
    public static final String IMAGE = "image";

    /**
     * Power management type key.
     */
    public static final String POWER_TYPE = "power_type";

    /**
     * Power management address key.
     */
    public static final String POWER_ADDRESS = "power_address";

    /**
     * Power username key.
     */
    public static final String POWER_USERNAME = "power_user";

    /**
     * Power type key.
     */
    public static final String POWER_PASSWORD = "power_pass";

    /**
     * Power management id key.
     */
    public static final String POWER_ID = "power_id";

    private SystemRecord(CobblerConnection clientIn) {
        client = clientIn;
    }

    /**
     * Create a new system record in cobbler
     *
     * @param client  the xmlrpc client
     * @param name    the system record name
     * @param profile the profile to be associated to this system
     * @return the newly created system record
     */
    public static SystemRecord create(CobblerConnection client,
                                      String name,
                                      Profile profile) {
        SystemRecord sys = new SystemRecord(client);
        sys.handle = (String) client.invokeTokenMethod("new_system");
        sys.modify(NAME, name, false);
        sys.modify(PROFILE, profile.getName(), false);
        sys.save();
        sys = lookupByName(client, name);
        return sys;
    }

    /**
     * Create a new system record in Cobbler, based on an image
     *
     * @param client the xmlrpc client
     * @param name   the system record name
     * @param image  the image to be associated to this system
     * @return the newly created system record
     */
    public static SystemRecord create(CobblerConnection client,
                                      String name,
                                      Image image) {
        SystemRecord sys = new SystemRecord(client);
        sys.handle = (String) client.invokeTokenMethod("new_system");
        sys.modify(NAME, name, false);
        sys.modify(IMAGE, image.getName(), false);
        sys.save();
        sys = lookupByName(client, name);
        return sys;
    }

    /**
     * Returns a system record matching the given name or null
     *
     * @param client the xmlrpc client
     * @param name   the system name
     * @return the system that maps to the name or null
     */
    public static SystemRecord lookupByName(CobblerConnection client, String name) {
        return handleLookup(client, lookupDataMapByName(client, name, "get_system"));
    }

    /**
     * Returns the system matching the given uid or null
     *
     * @param client client the xmlrpc client
     * @param id     the uid of the system record
     * @return the system record matching the given uid or null
     */
    public static SystemRecord lookupById(CobblerConnection client, String id) {
        return handleLookup(client, lookupDataMapById(client, id, "find_system"));
    }

    /**
     * List all SystemRecords associated with a particular profile
     *
     * @param client      the xmlrpc client
     * @param profileName the profile name (Cobbler profile name)
     * @return the List of SystemRecords
     */
    public static List<SystemRecord> listByAssociatedProfile(CobblerConnection client,
                                                             String profileName) {
        List<SystemRecord> toReturn = new ArrayList<>();
        List<Map<String, Object>> maps = lookupDataMapsByCriteria(
                client, PROFILE, profileName, "find_system");

        for (Map<String, Object> map : maps) {
            toReturn.add(handleLookup(client, map));
        }
        return toReturn;
    }

    /**
     * Returns a Cobbler system matching the given name or null
     *
     * @param client The XML-RPC connection to the Cobbler server
     * @param sysMap The Java Map with the already retrieved raw values of a system
     * @return The system object or {@code null}
     */
    @SuppressWarnings("unchecked")
    private static SystemRecord handleLookup(CobblerConnection client, Map<String, Object> sysMap) {
        if (sysMap != null) {
            SystemRecord sys = new SystemRecord(client);
            sys.dataMap = sysMap;
            sys.dataMapResolved = (Map<String, Object>) client.invokeMethod(
                    "get_system",
                    sys.getName(), // object name
                    false, // flatten
                    true // resolved
            );
            return sys;
        }
        return null;
    }

    /**
     * Returns a list of available systems
     *
     * @param connection the cobbler connection
     * @return a list of systems.
     */
    @SuppressWarnings("unchecked")
    public static List<SystemRecord> list(CobblerConnection connection) {
        List<SystemRecord> systems = new LinkedList<>();
        List<Map<String, Object>> cSystems = (List<Map<String, Object>>)
                connection.invokeMethod("get_systems");

        for (Map<String, Object> sysMap : cSystems) {
            SystemRecord sys = new SystemRecord(connection);
            sys.dataMap = sysMap;
            sys.dataMapResolved = (Map<String, Object>) connection.invokeMethod(
                    "get_system",
                    sys.getName(), // object name
                    false, // flatten
                    true // resolved
            );
            systems.add(sys);
        }
        return systems;
    }


    /**
     * Returns a list of available systems minus the excludes list
     *
     * @param connection the cobbler connection
     * @param excludes   a list of cobbler ids to file on
     * @return a list of systems.
     */
    @SuppressWarnings("unchecked")
    public static List<SystemRecord> list(CobblerConnection connection,
                                          Set<String> excludes) {
        List<SystemRecord> systems = new LinkedList<>();
        List<Map<String, Object>> cSystems = (List<Map<String, Object>>)
                connection.invokeMethod("get_systems");

        for (Map<String, Object> sysMap : cSystems) {
            SystemRecord sys = new SystemRecord(connection);
            sys.dataMap = sysMap;
            sys.dataMapResolved = (Map<String, Object>) connection.invokeMethod(
                    "get_system",
                    sys.getName(), // object name
                    false, // flatten
                    true // resolved
            );
            if (!excludes.contains(sys.getId())) {
                systems.add(sys);
            }
        }
        return systems;
    }

    /**
     * @inheritDoc
     */
    @Override
    protected String invokeGetHandle() {
        return (String) client.invokeTokenMethod("get_system_handle", this.getName());
    }

    /**
     * @inheritDoc
     */
    @Override
    protected void invokeModify(String key, Object value) {
        client.invokeTokenMethod("modify_system", getHandle(), key, value);
    }

    /**
     * @inheritDoc
     */
    @Override
    protected void invokeModifyResolved(String key, Object value) {
        client.invokeTokenMethod("set_item_resolved_value", getUid(), key, value);
    }

    /**
     * calls save_system to complete the commit
     */
    @Override
    protected void invokeSave() {
        client.invokeTokenMethod("save_system", getHandle());
    }

    /**
     * removes the kickstart system from cobbler.
     */
    @Override
    protected boolean invokeRemove() {
        return (Boolean) client.invokeTokenMethod("remove_system", getName());
    }

    /**
     * reloads the kickstart system.
     */
    @Override
    protected void reload() {
        SystemRecord newSystem = lookupById(client, getId());
        dataMap = newSystem.dataMap;
        dataMapResolved = newSystem.dataMapResolved;
    }

    /**
     * @inheritDoc
     */
    @Override
    protected void invokeRename(String newNameIn) {
        client.invokeTokenMethod("rename_system", getHandle(), newNameIn);
    }

    /**
     * Powers on this system, assuming correct power information was set (type,
     * username, password, address, and optionally id).
     *
     * @return true if the command was successful
     */
    public boolean powerOn() {
        return (Boolean) client.invokeTokenMethod("power_system", getHandle(), "on");
    }

    /**
     * Powers off this system, assuming correct power information was set (type,
     * username, password, address, and optionally id).
     *
     * @return true if the command was successful
     */
    public boolean powerOff() {
        return (Boolean) client.invokeTokenMethod("power_system", getHandle(), "off");
    }

    /**
     * Reboots this system, assuming correct power information was set (type,
     * username, password, address, and optionally id).
     *
     * @return true if the command was successful
     */
    public boolean reboot() {
        return (Boolean)
                client.invokeTokenMethod("power_system", getHandle(), "reboot");
    }

    /**
     * Gets the power status of this system, assuming correct power information
     * was set (type, username, password, address, and optionally id).
     *
     * @return true if the system is on, false if it is off, null if it cannot be determined
     */
    public Boolean getPowerStatus() {
        return (Boolean) client.invokeTokenMethod("power_system", getHandle(), "status");
    }

    /**
     * This method retrieves the profile associated with the System at hand. This might be null since a System can also
     * be associated with an Image.
     *
     * @return the Cobbler Profile
     * @see #getImage()
     */
    public Profile getProfile() {
        return Profile.lookupByName(client, (String) dataMap.get(PROFILE));
    }

    /**
     * This method retrieves the Image associated with the System at hand. This might be null since a System can also
     * be associated with a Profile.
     *
     * @return the Cobbler Image
     * @see #getProfile()
     */
    public Image getImage() {
        return Image.lookupByName(client, (String) dataMap.get(IMAGE));
    }

    /**
     * Getter for the virtual bridge property.
     *
     * @return the VirtBridge
     * @cobbler.inheritable This can be inherited from the parent object (profile or image) or the settings.
     */
    public Optional<String> getVirtBridge() {
        return this.<String>retrieveOptionalValue(VIRT_BRIDGE);
    }

    /**
     * Getter for the virtual bridge property in its resolved form
     *
     * @return The virtual bridge name
     */
    public String getResolvedVirtBridge() {
        return (String) dataMapResolved.get(VIRT_BRIDGE);
    }

    /**
     * Getter for the virtual CPU cores property.
     *
     * @return the VirtCpus
     * @cobbler.inheritable This can be inherited from the parent object (profile or image) or the settings.
     */
    public Optional<Integer> getVirtCpus() {
        return this.<Integer>retrieveOptionalValue(VIRT_CPUS);
    }

    /**
     * Getter for the resolved value of the virtual CPU cores
     *
     * @return the VirtCpus
     * @see #getVirtCpus()
     */
    public Integer getResolvedVirtCpus() {
        return (Integer) dataMapResolved.get(VIRT_CPUS);
    }

    /**
     * Getter for the type of VM property.
     *
     * @return the VirtType
     * @cobbler.inheritable This can be inherited from the parent object (profile or image) or the settings.
     */
    public Optional<String> getVirtType() {
        return this.<String>retrieveOptionalValue(VIRT_TYPE);
    }

    /**
     * Getter for the resolved type of VM
     *
     * @return the VirtType
     * @see #getVirtType()
     */
    public String getResolvedVirtType() {
        return (String) dataMapResolved.get(VIRT_TYPE);
    }

    /**
     * Getter for the virtual disk location property
     *
     * @return the VirtPath
     * @cobbler.inheritable This can be inherited from the parent object (profile or image) or the settings.
     */
    public Optional<String> getVirtPath() {
        return this.<String>retrieveOptionalValue(VIRT_PATH);
    }

    /**
     * Getter for the resolved path of the virtual disk image
     *
     * @return the VirtPath
     * @see #getVirtPath()
     */
    public String getResolvedVirtPath() {
        return (String) dataMapResolved.get(VIRT_PATH);
    }

    /**
     * Getter for the virtual disk size property
     *
     * @return the VirtFileSize
     * @cobbler.inheritable This can be inherited from the parent object (profile or image) or the settings.
     */
    public Optional<Double> getVirtFileSize() {
        return this.<Double>retrieveOptionalValue(VIRT_FILE_SIZE);
    }

    /**
     * Getter for the resolved value of the filesize of the virtual disk
     *
     * @return The filesize of the disk. Will inherit from the parent objects and settings.
     * @see #getVirtFileSize()
     */
    public Double getResolvedVirtFileSize() {
        return (Double) dataMapResolved.get(VIRT_FILE_SIZE);
    }

    /**
     * Getter for the virtual RAM property
     *
     * @return the VirtRam
     * @cobbler.inheritable This can be inherited from the parent object (profile or image) or the settings.
     */
    public Optional<Integer> getVirtRam() {
        return this.<Integer>retrieveOptionalValue(VIRT_RAM);
    }

    /**
     * Getter for the resolved virtual RAM property
     *
     * @return the VirtRam
     * @see #getVirtRam()
     */
    public Integer getResolvedVirtRam() {
        return (Integer) dataMapResolved.get(VIRT_RAM);
    }

    /**
     * true if netboot enabled is true
     * false otherwise
     *
     * @return netboot enabled value
     */
    public boolean isNetbootEnabled() {
        return Boolean.TRUE.toString().
                equalsIgnoreCase((String.valueOf(dataMap.get(NETBOOT_ENABLED))));
    }

    /**
     * Setter for the virtual bridge name of a virtual system
     *
     * @param virtBridgeIn the VirtBridge
     * @see #getVirtBridge()
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public void setVirtBridge(Optional<String> virtBridgeIn) {
        this.<String>modifyRawHelper(VIRT_BRIDGE, virtBridgeIn);
    }

    /**
     * Setter for the resolved virtual bridge property
     *
     * @param virtBridgeIn the VirtBridge
     * @see #getVirtBridge()
     */
    public void setResolvedVirtBridge(String virtBridgeIn) {
        modifyResolved(VIRT_BRIDGE, virtBridgeIn);
    }

    /**
     * Setter for the virtual CPU cores on a VM
     *
     * @param virtCpusIn the VirtCpus
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public void setVirtCpus(Optional<Integer> virtCpusIn) {
        this.<Integer>modifyRawHelper(VIRT_CPUS, virtCpusIn);
    }

    /**
     * Setter for the resolved virtual CPU cores on a VM
     *
     * @param virtCpusIn the VirtCpus
     * @see #getVirtCpus()
     */
    public void setResolvedVirtCpus(Integer virtCpusIn) {
        modifyResolved(VIRT_CPUS, virtCpusIn);
    }

    /**
     * Setter for the raw virtual type of virtual machine
     *
     * @param virtTypeIn the VirtType
     * @see #getVirtType()
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public void setVirtType(Optional<String> virtTypeIn) {
        this.<String>modifyRawHelper(VIRT_TYPE, virtTypeIn);
    }

    /**
     * Setter for the resolved virtual type of virtual machine
     *
     * @param virtTypeIn the VirtType
     * @see #getVirtType()
     */
    public void setResolvedVirtType(String virtTypeIn) {
        modifyResolved(VIRT_TYPE, virtTypeIn);
    }

    /**
     * Setter for the raw virtual path to the disk image of a VM
     *
     * @param virtPathIn the VirtPath
     * @see #getVirtPath()
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public void setVirtPath(Optional<String> virtPathIn) {
        this.<String>modifyRawHelper(VIRT_PATH, virtPathIn);
    }

    /**
     * Setter for the resolved virtual path to the disk image of a VM
     *
     * @param virtPathIn the VirtPath
     * @see #getVirtPath()
     */
    public void setResolvedVirtPath(String virtPathIn) {
        modifyResolved(VIRT_PATH, virtPathIn);
    }

    /**
     * Setter for the raw virtual disk file size
     *
     * @param virtFileSizeIn the VirtFileSize
     * @see #getVirtFileSize()
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public void setVirtFileSize(Optional<Integer> virtFileSizeIn) {
        this.<Integer>modifyRawHelper(VIRT_FILE_SIZE, virtFileSizeIn);
    }

    /**
     * Setter for the resolved virtual disk file size
     *
     * @param virtFileSizeIn The new disk size
     * @see #getVirtFileSize()
     */
    public void setResolvedVirtFileSize(Integer virtFileSizeIn) {
        modifyResolved(VIRT_FILE_SIZE, virtFileSizeIn);
    }

    /**
     * Setter for the RAM for a virtual machine
     *
     * @param virtRamIn The new amount of RAM this is Integer is mapped to GB (not GiB).
     * @see #getVirtRam()
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public void setVirtRam(Optional<Integer> virtRamIn) {
        this.<Integer>modifyRawHelper(VIRT_RAM, virtRamIn);
    }

    /**
     * Setter for the resolved RAM for a virtual machines
     *
     * @param virtRamIn The new amount of RAM this is Integer is mapped to GB (not GiB).
     * @see #getVirtRam()
     */
    public void setResolvedVirtRam(Integer virtRamIn) {
        modifyResolved(VIRT_RAM, virtRamIn);
    }

    /**
     * Enable netboot
     *
     * @param enable true to enable net boot.
     */
    public void enableNetboot(boolean enable) {
        modify(NETBOOT_ENABLED, enable);
    }

    /**
     * Setter for the name server property of a system
     *
     * @param nameServersIn the NameServers
     * @cobbler.inheritable This can be inherited from the parent profile or the settings.
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public void setNameServers(Optional<List<String>> nameServersIn) {
        if (nameServersIn.isEmpty()) {
            modify(NAME_SERVERS, INHERIT_KEY);
            return;
        }
        modify(NAME_SERVERS, nameServersIn);
    }

    /**
     * Setter for the resolved value of the name server property for a given system
     *
     * @param nameServersIn the NameServers
     * @see #setNameServers(Optional)
     */
    public void setResolvedNameServers(List<String> nameServersIn) {
        modifyResolved(NAME_SERVERS, nameServersIn);
    }

    /**
     * Setter for the gateway for the system
     *
     * @param gateway the Gateway
     */
    public void setGateway(String gateway) {
        modify(GATEWAY, gateway);
    }

    /**
     * Setter for the hostname of the system
     *
     * @param hostname the hostname
     */
    public void setHostName(String hostname) {
        modify(HOSTNAME, hostname);
    }

    /**
     * Associates a profile to this system record
     *
     * @param profile the profile to associate
     */
    public void setProfile(Profile profile) {
        setProfile(profile.getName());
    }

    /**
     * Associates a profile to this system record
     *
     * @param profileName the name of the profile
     */
    public void setProfile(String profileName) {
        modify(PROFILE, profileName);
    }

    /**
     * Associates an image to this system record
     *
     * @param image the image to associate
     */
    public void setImage(Image image) {
        setImage(image.getName());
    }

    /**
     * Associates an image to this system record
     *
     * @param imageName the name of the image
     */
    public void setImage(String imageName) {
        modify(IMAGE, imageName);
    }

    /**
     * Getter for the server property
     *
     * @return The server hostname or IP. If {@code Optional.emtpy()} is returned use {@link #getResolvedServer()} to
     *         retrieve the final value for the object.
     * @cobbler.inheritable This can be inherited either from a parent object or from the settings.
     */
    public Optional<String> getServer() {
        return this.<String>retrieveOptionalValue(SERVER);
    }

    /**
     * Getter for the server property
     *
     * @return The server hostname or IP
     * @see #getServer()
     */
    public String getResolvedServer() {
        return (String) dataMapResolved.get(SERVER);
    }

    /**
     * Sets the cobbler server host information for this system
     *
     * @param server the server host name.
     * @see #getServer()
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public void setServer(Optional<String> server) {
        modifyRawHelper(SERVER, server);
    }

    /**
     * Sets the cobbler server host information for this system
     *
     * @param server the server host name.
     * @see #getServer()
     */
    public void setResolvedServer(String server) {
        modifyResolved(SERVER, server);
    }

    /**
     * Sets IPv6 autoconfiguration on
     *
     * @param ipv6Autoconf boolean to indicate autoconf
     */
    public void setIpv6Autoconfiguration(boolean ipv6Autoconf) {
        modify(IPV6_AUTOCONF, ipv6Autoconf);
    }

    /**
     * Sets the network interfaces available to this system
     *
     * @param interfaces a list of network interfaces
     */
    public void setNetworkInterfaces(List<Network> interfaces) {
        Map<String, Object> ifaces = new HashMap<>();
        for (Network net : interfaces) {
            ifaces.putAll(net.toMap());
        }
        modify(SET_INTERFACES, ifaces);
    }

    /**
     * Retrieves the list of network interfaces and converts them to {@link Network}.
     *
     * @return a list of network interfaces associated to this system
     */
    @SuppressWarnings("unchecked")
    public List<Network> getNetworkInterfaces() {
        reload();
        List<Network> networks = new LinkedList<>();
        Map<String, Map<String, Object>> interfaces = (Map<String, Map<String, Object>>)
                dataMap.get(GET_INTERFACES);
        if (interfaces != null) {
            for (String name : interfaces.keySet()) {
                networks.add(Network.load(client, name, interfaces.get(name)));
            }
        }
        return networks;
    }

    /**
     * Gets the power management scheme/protocol for this system
     *
     * @return the type name
     */
    public String getPowerType() {
        return (String) dataMap.get(POWER_TYPE);
    }

    /**
     * Gets the IP address or hostname for this system's power management
     *
     * @return the address
     */
    public String getPowerAddress() {
        return (String) dataMap.get(POWER_ADDRESS);
    }

    /**
     * Gets the username for this system's power management system
     *
     * @return the username
     */
    public String getPowerUsername() {
        return (String) dataMap.get(POWER_USERNAME);
    }

    /**
     * Gets the password for this system's power management system
     *
     * @return the password
     */
    public String getPowerPassword() {
        return (String) dataMap.get(POWER_PASSWORD);
    }

    /**
     * Gets an additional ID for this system's power management system. The ID
     * is usually a type-specific identifier for the system or port to be
     * managed (eg. plug number on WTI, blade id on DRAC, etc.). See
     * <a href="https://cobbler.readthedocs.io/en/latest/user-guide.html#power-management">RTFD - Power Management</a>
     *
     * @return the ID
     */
    public String getPowerId() {
        return (String) dataMap.get(POWER_ID);
    }

    /**
     * Sets the power management scheme/protocol for this system
     *
     * @param powerType the type name
     */
    public void setPowerType(String powerType) {
        modify(POWER_TYPE, powerType);
    }

    /**
     * Sets the IP address or hostname for this system's power management
     *
     * @param powerAddress the address
     */
    public void setPowerAddress(String powerAddress) {
        modify(POWER_ADDRESS, powerAddress);
    }

    /**
     * Sets the username for this system's power management system
     *
     * @param powerUsername the username
     */
    public void setPowerUsername(String powerUsername) {
        modify(POWER_USERNAME, powerUsername);
    }

    /**
     * Sets the password for this system's power management system
     *
     * @param powerPassword the password
     */
    public void setPowerPassword(String powerPassword) {
        modify(POWER_PASSWORD, powerPassword);
    }

    /**
     * Sets an additional ID for this system's power management system. The ID
     * is usually a type-specific identifier for the system or port to be
     * managed (eg. plug number on WTI, blade id on DRAC, etc.). See
     * <a href="https://cobbler.readthedocs.io/en/latest/user-guide.html#power-management">in the Cobbler Wiki</a>
     *
     * @param powerId the ID
     */
    public void setPowerId(String powerId) {
        modify(POWER_ID, powerId);
    }
}
