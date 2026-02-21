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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package org.cobbler.test;

import com.redhat.rhn.domain.kickstart.KickstartVirtualizationType;
import com.redhat.rhn.domain.server.test.NetworkInterfaceTest;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cobbler.CobblerConnection;
import org.cobbler.XmlRpcException;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


class MockItem {

    private final String xmlrpcHandle;
    private Map<String, Object> dataMap;
    private Map<String, Object> dataMapResolved;

    MockItem(String xmlrpcHandleIn) {
        xmlrpcHandle = xmlrpcHandleIn;
        dataMap = new HashMap<>();
        dataMapResolved = new HashMap<>();
    }

    public Map<String, Object> getDataMap() {
        return dataMap;
    }

    public void setDataMap(Map<String, Object> dataMapIn) {
        dataMap = dataMapIn;
    }

    public Map<String, Object> getDataMapResolved() {
        return dataMapResolved;
    }

    public void setDataMapResolved(Map<String, Object> dataMapResolvedIn) {
        dataMapResolved = dataMapResolvedIn;
    }

    public String getXmlrpcHandle() {
        return xmlrpcHandle;
    }
}

/**
 * @author paji
 */
public class MockConnection extends CobblerConnection {
    private String token;
    private final String url;

    private final Logger log = LogManager.getLogger(MockConnection.class);

    private static List<MockItem> profiles = new ArrayList<>();
    private static List<MockItem> distros = new ArrayList<>();
    private static List<MockItem> systems = new ArrayList<>();
    private static List<MockItem> images = new ArrayList<>();

    private static final List<String> POWER_COMMANDS = new ArrayList<>();

    private static final Map<String, String> REMAP_KEYS = new HashMap<>();

    static {
        REMAP_KEYS.put("kopts", "kernel_options");
        REMAP_KEYS.put("kopts_post", "kernel_options_post");
        REMAP_KEYS.put("autoinstall_meta", "autoinstall_meta");
    }

    /**
     * Mock constructors for Cobbler connection
     * Don't care..
     *
     * @param urlIn  whatever url
     * @param userIn user
     * @param passIn password
     */
    public MockConnection(String urlIn, String userIn, String passIn) {
        super();
        url = urlIn;
        token = "token_is_not_empty";
    }

    /**
     * Mock constructors for Cobbler connection
     * Don't care..
     *
     * @param urlIn   whatever url
     * @param tokenIn session token.
     */
    public MockConnection(String urlIn, String tokenIn) {
        super();
        token = tokenIn;
        url = urlIn;
    }

    /**
     * Mock constructors for Cobbler connection
     * Don't care..
     *
     * @param urlIn whatever url
     */
    public MockConnection(String urlIn) {
        super();
        url = urlIn;
    }

    @Override
    public Object invokeMethod(String name, Object... args) {
        //no op -> mock version ..
        log.debug(String.format("No-Op Mock called: \"%s\" args: \"%s\"", name, Arrays.toString(args)));
        if (name == null) {
            throw new RuntimeException("name for invokeMethod cannot be null!");
        }
        for (Object object : args) {
            if (object == null) {
                throw new RuntimeException("No Argument for invokeMethod can be null!");
            }
        }

        switch (name) {
            case "token_check":
            case "update":
                return true;
            // login
            case "login":
                return random();
            //profiles:
            case "get_profiles":
                return convertItemListToMapList(profiles, false);
            case "find_profile":
                List<MockItem> findResultsProfile = find((Map<String, Object>) args[0], profiles);
                return convertItemListToMapList(findResultsProfile, false);
            case "modify_profile":
                log.debug("PROFILE: Modify  w/ handle {}, set {} to {}", args[0], args[1], args[2]);
                modifyItem(profiles, (String) args[0], (String) args[1], args[2]);
                break;
            case "get_profile":
                return getItem(profiles, (String) args[0], (args.length >= 3 && (boolean) args[2]));
            case "get_profile_handle":
                return getItemHandle((String) args[0], profiles);
            case "remove_profile":
                profiles.remove(findByName((String) args[0], profiles));
                return true;
            case "new_profile":
                return newProfile();
            //distros
            case "find_distro":
                List<MockItem> findResultsDistro = find((Map<String, Object>) args[0], distros);
                return convertItemListToMapList(findResultsDistro, false);
            case "get_distros":
                return convertItemListToMapList(distros, false);
            case "modify_distro":
                log.debug("DISTRO: Modify  w/ handle {}, set {} to {}", args[0], args[1], args[2]);
                modifyItem(distros, (String) args[0], (String) args[1], args[2]);
                break;
            case "get_distro":
                return getItem(distros, (String) args[0], (args.length >= 3 && (boolean) args[2]));
            case "rename_distro":
                log.debug("DISTRO: Rename w/ handle{}", args[0]);
                renameItem((String) args[0], (String) args[2], distros);
                return "";
            case "get_distro_handle":
                log.debug("DISTRO:  Got handle  w/ name {}", args[0]);
                return getItemHandle((String) args[0], distros);
            case "remove_distro":
                distros.remove(findByName((String) args[0], distros));
                return true;
            case "new_distro":
                return newDistro();
            //System
            case "find_system":
                List<MockItem> findResultsSystem = find((Map<String, Object>) args[0], systems);
                return convertItemListToMapList(findResultsSystem, false);
            case "get_systems":
                return convertItemListToMapList(systems, false);
            case "modify_system":
                modifyItem(systems, (String) args[0], (String) args[1], args[2]);
                break;
            case "get_system":
                return getItem(systems, (String) args[0], (args.length >= 3 && (boolean) args[2]));
            case "get_system_handle":
                return getItemHandle((String) args[0], systems);
            case "remove_system":
                systems.remove(findByName((String) args[0], systems));
                return true;
            case "new_system":
                return newSystem();
            case "power_system":
                boolean firstArgumentValid = false;
                String systemUid = "invalid system uid";
                for (MockItem item : systems) {
                    if (item.getXmlrpcHandle().equals(args[0])) {
                        firstArgumentValid = true;
                        systemUid = (String) item.getDataMap().get("uid");
                        break;
                    }
                }
                boolean secondArgumentValid = args[1].equals("on") || args[1].equals("off") ||
                        args[1].equals("reboot") || args[1].equals("status");
                boolean thirdArgumentValid = args[2].equals(token);
                if (firstArgumentValid && secondArgumentValid && thirdArgumentValid) {
                    String powerCommand = String.format(
                            "%s %s %s",
                            name,
                            args[1],
                            systemUid
                    );
                    POWER_COMMANDS.add(powerCommand);
                    return true;
                }
                return false;

            // images
            case "find_image":
                List<MockItem> findResultsImage = find((Map<String, Object>) args[0], images);
                return convertItemListToMapList(findResultsImage, false);
            case "get_images":
                return convertItemListToMapList(images, false);
            case "modify_image":
                modifyItem(images, (String) args[0], (String) args[1], args[2]);
                break;
            case "rename_image":
                renameItem((String) args[0], (String) args[2], images);
                return "";
            case "get_image":
                return getItem(images, (String) args[0], (args.length >= 3 && (boolean) args[2]));
            case "get_image_handle":
                return getItemHandle((String) args[0], images);
            case "remove_image":
                images.remove(findByName((String) args[0], images));
                return true;
            case "new_image":
                return newImage();
            // other
            case "get_item_resolved_value":
                if (args.length != 2) {
                    throw new RuntimeException("get_item_resolved_value needs exactly two arguments!");
                }
                // First argument is the item uuid and second one is the requested attribute
                MockItem getItemResolveItem = null;
                // Loop over all collection with find_by_criteria
                List<List<MockItem>> collections = Arrays.asList(profiles, distros, systems, images);
                for (List<MockItem> collection : collections) {
                    List<MockItem> result = find(
                            Map.ofEntries(new AbstractMap.SimpleEntry<>("uuid", (String) args[1])),
                            collection
                    );
                    if (result.size() == 1) {
                        getItemResolveItem = result.get(0);
                        break;
                    }
                }
                if (getItemResolveItem == null) {
                    // Log a warning because in production this should not happen, however it is possible that the
                    // Mock doesn't implement everything as desired.
                    this.log.warn(
                            String.format("Requested attribute \"%s\" for uuid \"%s\" not found!", args[0], args[1])
                    );
                    return null;
                }
                // Then get the attribute and return it
                return getItemResolveItem.getDataMapResolved().get("uuid");
            case "sync":
                return true;
            case "version":
                return 2.2;
            default:
                log.debug("Unhandled xmlrpc call in MockConnection: {}", name);
                break;
        }
        return "";
    }

    private Map<String, Object> getItem(List<MockItem> collection, String name, boolean resolved) {
        MockItem item = findByName(name, collection);
        if (item == null) {
            return null;
        }
        if (resolved) {
            return item.getDataMapResolved();
        }
        return item.getDataMap();
    }

    private String getItemHandle(String name, List<MockItem> collection) {
        MockItem item = findByName(name, collection);
        if (item != null) {
            return item.getXmlrpcHandle();
        }
        return null;
    }

    private void renameItem(String name, String newName, List<MockItem> collection) {
        for (MockItem distro : collection) {
            if (distro.getXmlrpcHandle().equals(name)) {
                distro.getDataMap().put("name", newName);
                distro.getDataMapResolved().put("name", newName);
                break;
            }
        }
    }

    private void modifyItem(List<MockItem> collection, String xmlrpcHandle, String arg1, Object arg2) {
        Optional<MockItem> distroFound = Optional.empty();
        for (MockItem item : collection) {
            if (item.getXmlrpcHandle().equals(xmlrpcHandle)) {
                distroFound = Optional.of(item);
                break;
            }
        }
        if (distroFound.isEmpty()) {
            throw new RuntimeException(
                    String.format("Item with handle '%s' not found!", xmlrpcHandle)
            );
        }
        Map<String, Object> distro = distroFound.get().getDataMap();

        if (arg1.startsWith("modify_interface")) {
            Map<String, Object> input = (Map<String, Object>)arg2;
            modifyInterfaces(collection, distro, input);
            return;
        }

        if (arg1.startsWith("kernel_options") && arg2.equals("")) {
            arg2 = new HashMap<>();
        }
        distro.put(arg1, arg2);

        // Some cobbler options have 2 names (the first name is used for writing to cobbler,
        // and the second one for reading from cobbler. Here we make sure the second one
        // is updated too.
        if (REMAP_KEYS.containsKey(arg1)) {
            distro.put(REMAP_KEYS.get(arg1), arg2);
        }
    }

    private void modifyInterfaces(List<MockItem> collection, Map<String, Object> system,
                                  Map<String, Object> input) {
        // Translate to the interface name - key - value
        Map<String, Map<String, Object>> newInterfaces = input.entrySet().stream()
                .collect(Collectors.groupingBy(
                        // 1. Outer Map Key (Grouping Key): 'name'
                        //    This gets the part *after* the last dash.
                        entry -> entry.getKey().substring(entry.getKey().lastIndexOf('-') + 1),

                        // 2. Downstream Collector (Value for Outer Map):
                        //    This creates the inner Map<String, Object> for each group.
                        Collectors.toMap(
                                // 2a. Inner Map Key: 'somekey'
                                //     This gets the part *before* the last dash.
                                entry -> entry.getKey().substring(0, entry.getKey().lastIndexOf('-')),

                                // 2b. Inner Map Value: (the original value)
                                Map.Entry::getValue
                        )
                ));
        List<String> macs = newInterfaces.values().stream().map(
                nic -> (String) nic.get("mac_address")
        ).toList();

        // Check if the MAC is already present for some system. Cobbler throws XmlRpcException in this case.
        collection.stream()
                .filter(
                    item -> !item.getDataMap().get("name").equals(system.get("name")))
                .filter(
                    item -> {
                    Map<String, Map<String, Object>> ifaces =
                            (Map<String, Map<String, Object>>) item.getDataMap().get("interfaces");
                    return ifaces.entrySet().stream().anyMatch(
                            nic -> macs.contains(nic.getValue().get("mac_address").toString())
                    );
                }
                ).findAny().ifPresent(
                item -> {
                    throw new XmlRpcException(
                            "MAC address duplicate found. Object with the conflict has the name \"" +
                                    item.getDataMap().get("name") + "\"");
                }
        );

        Map<String, Map<String, Object>> interfaces = (Map<String, Map<String, Object>>) system.get("interfaces");
        interfaces.putAll(newInterfaces);
    }

    private String newImage() {
        Map<String, Object> newImage = new HashMap<>();
        String xmlrpcHandle = random();
        String uid = random();
        newImage.put("uid", uid);
        MockItem newImageMock = new MockItem(xmlrpcHandle);
        newImageMock.setDataMap(newImage);
        images.add(newImageMock);
        return xmlrpcHandle;
    }

    private String newProfile() {
        Map<String, Object> profile = new HashMap<>();
        String uid = random();
        String xmlrpcHandle = random();
        profile.put("uid", uid);
        profile.put("name", random());
        String distro = newDistro();
        profile.put("distro", distro);

        log.debug("PROFILE: Created w/ uid {} returning handle {}", uid, xmlrpcHandle);

        profile.put("virt_bridge", "xenb0");
        profile.put("virt_cpus", 1);
        profile.put("virt_type", KickstartVirtualizationType.XEN_FULLYVIRT);
        profile.put("virt_path", "/tmp/foo");
        profile.put("virt_file_size", 8.0);
        profile.put("virt_ram", 512);
        profile.put("kernel_options", new HashMap<>());
        profile.put("kernel_options_post", new HashMap<>());
        profile.put("autoinstall_meta", new HashMap<>());
        profile.put("redhat_management_key", "");
        MockItem item = new MockItem(xmlrpcHandle);
        item.setDataMap(profile);
        profiles.add(item);
        return xmlrpcHandle;
    }

    private String newSystem() {
        Map<String, Object> profile = new HashMap<>();
        String xmlrpcHandle = random();
        profile.put("uid", random());
        Map<String, Map<String, Object>> interfaces = new HashMap<>();
        Map<String, Object> iface = new HashMap<>();
        iface.put("mac_address", NetworkInterfaceTest.TEST_MAC);
        iface.put("ip_address", "127.0.0.1");
        interfaces.put("eth0", iface);
        profile.put("interfaces", interfaces);
        profile.put("autoinstall_meta", new HashMap<>());
        profile.put("redhat_management_key", "");
        MockItem item = new MockItem(xmlrpcHandle);
        item.setDataMap(profile);
        systems.add(item);
        return xmlrpcHandle;
    }

    private String newDistro() {
        String uid = random();

        Map<String, Object> distro = new HashMap<>();
        String xmlrpcHandle = random();
        distro.put("uid", uid);

        log.debug("DISTRO: Created w/ uid {} returning handle {}", uid, xmlrpcHandle);

        MockItem item = new MockItem(xmlrpcHandle);
        item.setDataMap(distro);
        distros.add(item);

        distro.put("virt_bridge", "xenb0");
        distro.put("virt_cpus", 1);
        distro.put("virt_type", KickstartVirtualizationType.XEN_FULLYVIRT);
        distro.put("virt_path", "/tmp/foo");
        distro.put("virt_file_size", 8.0);
        distro.put("virt_ram", 512);
        distro.put("kernel_options", new HashMap<>());
        distro.put("kernel_options_post", new HashMap<>());
        distro.put("autoinstall_meta", new HashMap<>());
        distro.put("redhat_management_key", "");
        distro.put("name", xmlrpcHandle);
        return xmlrpcHandle;
    }

    private MockItem findByName(String name, List<MockItem> maps) {
        for (MockItem map : maps) {
            if (name.equals(map.getDataMap().get("name"))) {
                return map;
            }
        }
        return null;
    }

    private List<Map<String, Object>> convertItemListToMapList(List<MockItem> collection, boolean resolved) {
        List<Map<String, Object>> returnValue = new LinkedList<>();
        for (MockItem item : collection) {
            if (resolved) {
                returnValue.add(new HashMap<>(item.getDataMapResolved()));
            }
            else {
                returnValue.add(new HashMap<>(item.getDataMap()));
            }
        }
        return returnValue;
    }

    private List<MockItem> find(Map<String, Object> criteria, List<MockItem> maps) {
        List<MockItem> ret = new LinkedList<>();
        for (MockItem map : maps) {
            int matched = 0;
            for (String key : criteria.keySet()) {
                if (!criteria.get(key).equals(map.getDataMap().get(key))) {
                    break;
                }
                matched++;
            }
            if (matched == criteria.size()) {
                ret.add(map);
            }
        }
        return ret;
    }

    private String random() {
        return RandomStringUtils.randomAlphabetic(10);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object invokeTokenMethod(String procedureName,
                                    Object... args) {
        List<Object> params = new LinkedList<>(Arrays.asList(args));
        params.add(token);
        return invokeMethod(procedureName, params.toArray());
    }

    /**
     * updates the token
     *
     * @param tokenIn the cobbler auth token
     */
    @Override
    public void setToken(String tokenIn) {
        token = tokenIn;
    }

    /**
     * @return returns the cobbler url...
     */
    @Override
    public String getUrl() {
        return url + "/cobbler_api";
    }

    @Override
    public Double getVersion() {
        return 2.2;
    }

    /**
     * Returns a list of strings with the latest power commands received by this
     * connection.
     *
     * @return the latest commands
     */
    public static List<String> getPowerCommands() {
        return POWER_COMMANDS;
    }

    /**
     * Returns a string with the latest power command received by this
     * connection or null.
     *
     * @return the latest command
     */
    public static String getLatestPowerCommand() {
        return POWER_COMMANDS.get(POWER_COMMANDS.size() - 1);
    }

    public static void clear() {
        profiles = new ArrayList<>();
        distros = new ArrayList<>();
        systems = new ArrayList<>();
        images = new ArrayList<>();
    }

}
