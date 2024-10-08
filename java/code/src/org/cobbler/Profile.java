/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.util.StringUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


/**
 * Cobbler Profile
 *
 * @author paji
 * @see <a href="https://cobbler.readthedocs.io/en/v3.3.3/code-autodoc/cobbler.items.html#module-cobbler.items.profile">RTFD - Cobbler - 3.3.3 - Profile</a>
 */
public class Profile extends CobblerObject {

    /**
     * Logger for this class
     */
    private static Logger log = LogManager.getLogger(Profile.class);
    /**
     * Cobbler field name for the DHCP Tag
     */
    private static final String DHCP_TAG = "dhcp_tag";
    /**
     * Cobbler field name for the auto-installation property
     */
    private static final String KICKSTART = "autoinstall";
    /**
     * Cobbler field name for the virtual bridge property
     */
    private static final String VIRT_BRIDGE = "virt_bridge";
    /**
     * Cobbler field name for the number of virtual CPU cores to assign to a VM
     */
    private static final String VIRT_CPUS = "virt_cpus";
    /**
     * Cobbler field name for the type of VM that is created
     */
    private static final String VIRT_TYPE = "virt_type";
    /**
     * Cobbler field name for the repository property
     */
    private static final String REPOS = "repos";
    /**
     * Cobbler field name for the path to the virtual machine disk image
     */
    private static final String VIRT_PATH = "virt_path";
    /**
     * Cobbler field name to override the server templating variable
     */
    private static final String SERVER = "server";
    /**
     * Cobbler field name for the name servers
     */
    private static final String NAME_SERVERS = "name_servers";
    /**
     * Cobbler field name whether to enable the visibility in the boot menu or not
     */
    private static final String ENABLE_MENU = "enable_menu";
    /**
     * Cobbler field name for the size of the created disk image(s)
     */
    private static final String VIRT_FILE_SIZE = "virt_file_size";
    /**
     * Cobbler field name for the amount of RAM assigned to the created VM
     */
    private static final String VIRT_RAM = "virt_ram";
    /**
     * Cobbler field name for the name of the distribution that is being used for the profile
     */
    private static final String DISTRO = "distro";

    /**
     * Cobbler profile name for default PXE boot
     */
    public static final String BOOTSTRAP_NAME = "pxe-default-profile";


    /**
     * Constructor that enabled the profile to be created, read, modified or deleted
     *
     * @param clientIn The Cobbler connection used to communicate with the server
     */
    private Profile(CobblerConnection clientIn) {
        client = clientIn;
    }

    /**
     * Create a new kickstart profile in cobbler
     *
     * @param client the xmlrpc client
     * @param name   the profile name
     * @param distro the distro allocated to this profile.
     * @return the newly created profile
     */
    public static Profile create(CobblerConnection client,
                                 String name, Distro distro) {
        Profile profile = new Profile(client);
        profile.handle = (String) client.invokeTokenMethod("new_profile");
        profile.modify(NAME, name, false);
        profile.modify(DISTRO, distro.getName(), false);
        profile.save();
        profile = lookupByName(client, name);
        return profile;
    }

    /**
     * Create a new child profile in cobbler
     *
     * @param client the xmlrpc client
     * @param name   the profile name
     * @param parent the parent profile name.
     * @return the newly created profile
     */
    public static Profile create(CobblerConnection client,
                                 String name, String parent) {
        Profile profile = new Profile(client);
        profile.handle = (String) client.invokeTokenMethod("new_profile");
        profile.modify(NAME, name, false);
        profile.modify(PARENT, parent, false);
        profile.save();
        profile = lookupByName(client, name);
        return profile;
    }

    /**
     * Returns a kickstart profile matching the given name or null
     *
     * @param client the xmlrpc client
     * @param name   the profile name
     * @return the profile that maps to the name or null
     */
    public static Profile lookupByName(CobblerConnection client, String name) {
        return handleLookup(client, lookupDataMapByName(client, name, "get_profile"));
    }

    /**
     * Returns the profile matching the given uid or null
     *
     * @param client client the xmlrpc client
     * @param id     the uid of the profile
     * @return the profile matching the given uid or null
     */
    public static Profile lookupById(CobblerConnection client, String id) {
        if (id == null) {
            return null;
        }
        return handleLookup(client, lookupDataMapById(client, id,
                "find_profile"));
    }

    /**
     * Handles lookups.
     *
     * @param client The Client that holds the connection to the Cobbler server
     * @param profileMap The Key-Value Map with the content of the profile
     * @return Either null or the profile that has been build by the Map
     */
    @SuppressWarnings("unchecked")
    private static Profile handleLookup(CobblerConnection client, Map<String, Object> profileMap) {
        if (profileMap != null) {
            Profile profile = new Profile(client);
            profile.dataMap = profileMap;
            profile.dataMapResolved = (Map<String, Object>) client.invokeMethod(
                    "get_profile",
                    profile.getName(), // object name
                    false, // flatten
                    true // resolved
            );
            return profile;
        }
        return null;
    }

    /**
     * Returns a list of available profiles
     *
     * @param connection the cobbler connection
     * @return a list of profiles.
     */
    @SuppressWarnings("unchecked")
    public static List<Profile> list(CobblerConnection connection) {
        List<Profile> profiles = new LinkedList<>();
        List<Map<String, Object>> cProfiles = (List<Map<String, Object>>)
                connection.invokeMethod("get_profiles");

        for (Map<String, Object> profMap : cProfiles) {
            Profile profile = new Profile(connection);
            profile.dataMap = profMap;
            profile.dataMapResolved = (Map<String, Object>) connection.invokeMethod(
                    "get_profile",
                    profile.getName(), // object name
                    false, // flatten
                    true // resolved
            );
            profiles.add(profile);
        }
        return profiles;
    }


    /**
     * Returns a list of available profiles minus the excludes list
     *
     * @param connection the cobbler connection
     * @param excludes   a list of cobbler ids to file on
     * @return a list of profiles.
     */
    @SuppressWarnings("unchecked")
    public static List<Profile> list(CobblerConnection connection,
                                     Set<String> excludes) {
        List<Profile> profiles = new LinkedList<>();
        List<Map<String, Object>> cProfiles = (List<Map<String, Object>>)
                connection.invokeMethod("get_profiles");

        for (Map<String, Object> profMap : cProfiles) {
            Profile profile = new Profile(connection);
            profile.dataMap = profMap;
            profile.dataMapResolved = (Map<String, Object>) connection.invokeMethod(
                    "get_profile",
                    profile.getName(), // object name
                    false, // flatten
                    true // resolved
            );
            if (!excludes.contains(profile.getId())) {
                profiles.add(profile);
            }


        }
        return profiles;
    }

    /**
     * @inheritDoc
     */
    @Override
    protected String invokeGetHandle() {
        return (String) client.invokeTokenMethod("get_profile_handle", this.getName());
    }

    /**
     * @inheritDoc
     */
    @Override
    protected void invokeModify(String key, Object value) {
        client.invokeTokenMethod("modify_profile", getHandle(), key, value);
    }

    /**
     * @inheritDoc
     */
    @Override
    protected void invokeModifyResolved(String key, Object value) {
        client.invokeTokenMethod("set_item_resolved_value", getUid(), key, value);
    }

    /**
     * calls save_profile to complete the commit
     */
    @Override
    protected void invokeSave() {
        client.invokeTokenMethod("save_profile", getHandle());
    }

    /**
     * removes the kickstart profile from cobbler.
     */
    @Override
    protected boolean invokeRemove() {
        return (Boolean) client.invokeTokenMethod("remove_profile", getName());
    }

    /**
     * reloads the kickstart profile.
     */
    @Override
    protected void reload() {
        Profile newProfile = lookupById(client, getId());
        dataMap = newProfile.dataMap;
        dataMapResolved = newProfile.dataMapResolved;
    }

    /**
     * @inheritDoc
     */
    @Override
    protected void invokeRename(String newNameIn) {
        client.invokeTokenMethod("rename_profile", getHandle(), newNameIn);
    }

    /**
     * Getter for the DHCP Tag
     *
     * @return the DhcpTag
     */
    public String getDhcpTag() {
        return (String) dataMap.get(DHCP_TAG);
    }

    /**
     * This returns the full absolute path on the SUMA Server.
     *
     * @return the Kickstart file path
     */
    public String getKickstart() {
        return getFullAutoinstallPath((String) dataMap.get(KICKSTART));
    }

    /**
     * Getter for the virtual bridge property.
     *
     * @return the VirtBridge
     * @cobbler.inheritable This can be inherited from a parent profile or the settings.
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
     * @cobbler.inheritable This can be inherited from a parent profile or the settings.
     */
    public Optional<Integer> getVirtCpus() {
        return this.<Integer>retrieveOptionalValue(VIRT_CPUS);
    }

    /**
     * Getter for the resolved virtual CPU cores for a VM
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
     * @cobbler.inheritable This can be inherited from a parent profile or the settings.
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
     * Getter for the repositories that a profile has assigned
     *
     * @return the Repos
     */
    @SuppressWarnings("unchecked")
    public List<String> getRepos() {
        return (List<String>) dataMap.get(REPOS);
    }

    /**
     * Getter for the virtual disk location property
     *
     * @return the VirtPath
     * @cobbler.inheritable This can be inherited from a parent profile or the settings.
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
     * Getter for the server property
     *
     * @return the Server
     * @cobbler.inheritable This can be inherited from a parent profile or the settings.
     */
    public Optional<String> getServer() {
        return this.<String>retrieveOptionalValue(SERVER);
    }

    /**
     * Getter for the resolved server property
     *
     * @return The server hostname or IP
     * @see #getServer()
     */
    public String getResolvedServer() {
        return (String) dataMapResolved.get(SERVER);
    }

    /**
     * Getter for the name servers property
     *
     * @return the NameServers
     * @cobbler.inheritable This can be inherited from a parent profile or the settings.
     */
    public Optional<List<String>> getNameServers() {
        return this.<List<String>>retrieveOptionalValue(NAME_SERVERS);
    }

    /**
     * Getter for the resolved value of the name server for a profile
     *
     * @return The name servers combined from the Distro and Profile(s)
     * @see #getNameServers()
     */
    @SuppressWarnings("unchecked")
    public List<String> getResolvedNameServer() {
        return (List<String>) dataMapResolved.get(NAME_SERVERS);
    }

    /**
     * Setter for the name servers
     *
     * @param nameServersIn The new value for the name servers
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public void setNameServers(Optional<List<String>> nameServersIn) {
        this.<List<String>>modifyRawHelper(NAME_SERVERS, nameServersIn);
    }

    /**
     * Setter for the resolved name servers of a profile
     *
     * @param nameServersIn The new value for the name server in Cobbler
     * @see #getNameServers()
     */
    public void setResolvedNameServers(List<String> nameServersIn) {
        modifyResolved(NAME_SERVERS, nameServersIn);
    }

    /**
     * If custom boot menus are enabled or the standard Cobbler hierarchy is used
     *
     * @return true if menu enabled
     */
    public boolean menuEnabled() {
        return (boolean) dataMap.get(ENABLE_MENU);
    }

    /**
     * Getter for the virtual disk size property
     *
     * @return the VirtFileSize
     * @cobbler.inheritable This can be inherited from a parent profile or the settings.
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
     * @cobbler.inheritable This can be inherited from a parent profile or the settings.
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
     * Getter for the Distro object
     *
     * @return the Distro
     */
    public Distro getDistro() {
        String distroName = (String) dataMap.get(DISTRO);
        return Distro.lookupByName(client, distroName);
    }

    /**
     * Setter for the DHCP Tag of the profile
     *
     * @param dhcpTagIn the DhcpTag
     */
    public void setDhcpTag(String dhcpTagIn) {
        modify(DHCP_TAG, dhcpTagIn);
    }

    /**
     * This is the path relative to the Cobbler template directory.
     *
     * @param kickstartIn the Kickstart
     */
    public void setKickstart(String kickstartIn) {
        if (kickstartIn.isEmpty()) {
            modify(KICKSTART, "");
        }
        else {
            modify(KICKSTART, "/" + getRelativeAutoinstallPath(kickstartIn));
        }
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
     * @param reposIn the Repos
     */
    public void setRepos(List<String> reposIn) {
        modify(REPOS, reposIn);
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
     * Sets the cobbler server host information for this system
     *
     * @param serverIn the Server
     * @see #getServer()
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public void setServer(Optional<String> serverIn) {
        modifyRawHelper(SERVER, serverIn);
    }

    /**
     * Sets the cobbler server host information for this system
     *
     * @param serverIn the server host name.
     * @see #getServer()
     */
    public void setResolvedServer(String serverIn) {
        modifyResolved(SERVER, serverIn);
    }

    /**
     * @param enableMenuIn the EnableMenu
     */
    public void setEnableMenu(boolean enableMenuIn) {
        modify(ENABLE_MENU, enableMenuIn);
    }

    /**
     * Setter for the raw virtual disk file size
     *
     * @param virtFileSizeIn the VirtFileSize
     * @see #getVirtFileSize()
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public void setVirtFileSize(Optional<Double> virtFileSizeIn) {
        this.<Double>modifyRawHelper(VIRT_FILE_SIZE, virtFileSizeIn);
    }

    /**
     * Setter for the resolved virtual disk file size
     *
     * @param virtFileSizeIn The new disk size
     * @see #getVirtFileSize()
     */
    public void setResolvedVirtFileSize(Double virtFileSizeIn) {
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
     * Setter for the distro of the profile
     * <p>
     * Wrapper for {@link #setDistro(String)} that does a null check on the Distro
     *
     * @param distroIn the Distro
     */
    public void setDistro(Distro distroIn) {
        if (distroIn == null) {
            log.warn("Profile.setDistro was called with null.  This shouldn't happen, " +
                    "so we're ignoring");
            return;
        }
        setDistro(distroIn.getName());
    }

    /**
     * Setter for the distro of the profile
     *
     * @param name the Distro name
     */
    public void setDistro(String name) {
        modify(DISTRO, name);
    }

    /**
     * Generates the kickstart text and returns that
     *
     * @return the generated kickstart text
     */
    public String generateKickstart() {
        return (String) client.invokeTokenMethod("generate_autoinstall", getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void syncRedHatManagementKeys(Collection<String> keysToRemove,
                                         Collection<String> keysToAdd) {
        // FIXME: The inheritance in Cobbler should not be broken with this method. Thus it should be completely
        //        removed.
        super.syncRedHatManagementKeys(keysToRemove, keysToAdd);
        for (SystemRecord record :
                SystemRecord.listByAssociatedProfile(client, this.getName())) {
            record.syncRedHatManagementKeys(keysToRemove, keysToAdd);
            record.save();
        }
    }

    /**
     * This method converts a relative path required by Cobbler to an absolute filepath on the server
     *
     * @param pathIn The path to convert
     * @return The absolute path on the server
     */
    private String getFullAutoinstallPath(String pathIn) {
        if (pathIn.isEmpty()) {
            return "";
        }
        String cobblerPath = ConfigDefaults.get().getKickstartConfigDir();
        if (pathIn.startsWith(cobblerPath)) {
            return pathIn;
        }
        return StringUtil.addPath(cobblerPath, pathIn);
    }

    /**
     * This method converts an absolute path on the server to a relative one that is required by Cobbler
     *
     * @param pathIn The path to convert
     * @return The relative path
     */
    private String getRelativeAutoinstallPath(String pathIn) {
        String cobblerPath = ConfigDefaults.get().getKickstartConfigDir();
        if (!cobblerPath.endsWith("/")) {
            cobblerPath += "/";
        }
        if (pathIn.startsWith(cobblerPath)) {
            return pathIn.substring(cobblerPath.length());
        }
        return pathIn;
    }
}
