/**
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
import java.util.Set;


/**
 * @version $Rev$
 */

public class SystemRecord extends CobblerObject {
    private static final String HOSTNAME = "hostname";
    private static final String NAME_SERVERS = "name_servers";
    private static final String GATEWAY = "gateway";
    private static final String PROFILE = "profile";
    private static final String SERVER = "server";
    private static final String VIRT_BRIDGE = "virt_bridge";
    private static final String VIRT_CPUS = "virt_cpus";
    private static final String VIRT_TYPE = "virt_type";
    private static final String VIRT_PATH = "virt_path";
    private static final String VIRT_FILE_SIZE = "virt_file_size";
    private static final String VIRT_RAM = "virt_ram";
    private static final String NETBOOT_ENABLED = "netboot_enabled";
    public static final String REDHAT_MGMT_SERVER = "redhat_management_server";
    private static final String SET_INTERFACES = "modify_interface";
    private static final String GET_INTERFACES = "interface";
    private static final String IPV6_AUTOCONF = "ipv6_autoconfiguration";
    /** Cobbler system name for default PXE boot */
    public static final String BOOTSTRAP_NAME = "default";

    private SystemRecord(CobblerConnection clientIn) {
        client = clientIn;
    }

    /**
     * Create a new system record in cobbler
     * @param client the xmlrpc client
     * @param name the system record name
     * @param profile the profile to be associated to this system
     * @return the newly created system record
     */
    public static SystemRecord create(CobblerConnection client,
                                String name,
                                Profile profile) {
        SystemRecord sys = new SystemRecord(client);
        sys.handle = (String) client.invokeTokenMethod("new_system");
        sys.modify(NAME, name);
        sys.setProfile(profile);
        sys.save();
        sys = lookupByName(client, name);
        return sys;
    }

    /**
     * Returns a system record matching the given name or null
     * @param client the xmlrpc client
     * @param name the system name
     * @return the system that maps to the name or null
     */
    public static SystemRecord lookupByName(CobblerConnection client, String name) {
        return handleLookup(client, lookupDataMapByName(client, name, "get_system"));
    }

    /**
     *  Returns the system matching the given uid or null
     * @param client client the xmlrpc client
     * @param id the uid of the system record
     * @return the system record matching the given uid or null
     */
    public static SystemRecord lookupById(CobblerConnection client, String id) {
        return handleLookup(client, lookupDataMapById(client, id, "find_system"));
    }

    /**
     * List all SystemRecords associated with a particular profile
     * @param client the xmlrpc client
     * @param profileName the profile name (Cobbler profile name)
     * @return the List of SystemRecords
     */
    public static List<SystemRecord> listByAssociatedProfile(CobblerConnection client,
                                                                    String profileName) {
        List<SystemRecord> toReturn = new ArrayList<SystemRecord>();
        List<Map<String, Object>> maps =  lookupDataMapsByCriteria(
                        client, PROFILE, profileName, "find_system");

        for (Map map : maps) {
            toReturn.add(handleLookup(client, map));
        }
        return toReturn;
    }


    private static SystemRecord handleLookup(CobblerConnection client, Map sysMap) {
        if (sysMap != null) {
            SystemRecord sys = new SystemRecord(client);
            sys.dataMap = sysMap;
            return sys;
        }
        return null;
    }

    /**
     * Returns a list of available systems
     * @param connection the cobbler connection
     * @return a list of systems.
     */
    public static List<SystemRecord> list(CobblerConnection connection) {
        List <SystemRecord> systems = new LinkedList<SystemRecord>();
        List <Map<String, Object >> cSystems = (List <Map<String, Object >>)
                                        connection.invokeMethod("get_systems");

        for (Map<String, Object> sysMap : cSystems) {
            SystemRecord sys = new SystemRecord(connection);
            sys.dataMap = sysMap;
            systems.add(sys);
        }
        return systems;
    }


    /**
     * Returns a list of available systems minus the excludes list
     * @param connection the cobbler connection
     * @param excludes a list of cobbler ids to file on
     * @return a list of systems.
     */
    public static List<SystemRecord> list(CobblerConnection connection,
                                Set<String> excludes) {
        List <SystemRecord> systems = new LinkedList<SystemRecord>();
        List <Map<String, Object >> cSystems = (List <Map<String, Object >>)
                                        connection.invokeMethod("get_systems");

        for (Map<String, Object> sysMap : cSystems) {
            SystemRecord sys = new SystemRecord(connection);
            sys.dataMap = sysMap;
            if (!excludes.contains(sys.getId())) {
                systems.add(sys);
            }
        }
        return systems;
    }

    @Override
    protected String invokeGetHandle() {
        return (String)client.invokeTokenMethod("get_system_handle", this.getName());
    }

    @Override
    protected void invokeModify(String key, Object value) {
        client.invokeTokenMethod("modify_system", getHandle(), key, value);
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
    }

    /* (non-Javadoc)
     * @see org.cobbler.CobblerObject#renameTo(java.lang.String)
     */

    @Override
    protected void invokeRename(String newNameIn) {
        client.invokeTokenMethod("rename_system", getHandle(), newNameIn);
    }

    /**


     /**
     * @return the Cobbler Profile name
     */
     public Profile getProfile() {
         return Profile.lookupByName(client, (String)dataMap.get(PROFILE));
     }

     /**
     * @return the VirtBridge
     */
     public String getVirtBridge() {
         return (String)dataMap.get(VIRT_BRIDGE);
     }

     /**
     * @return the VirtCpus
     */
     public int getVirtCpus() {
         return (Integer)dataMap.get(VIRT_CPUS);
     }

     /**
     * @return the VirtType
     */
     public String getVirtType() {
         return (String)dataMap.get(VIRT_TYPE);
     }

     /**
     * @return the VirtPath
     */
     public String getVirtPath() {
         return (String)dataMap.get(VIRT_PATH);
     }

     /**
     * @return the VirtFileSize
     */
     public int getVirtFileSize() {
         return (Integer)dataMap.get(VIRT_FILE_SIZE);
     }

     /**
     * @return the VirtRam
     */
     public int getVirtRam() {
         return (Integer)dataMap.get(VIRT_RAM);
     }

     /**
      * true if netboot enabled is true
      * false other wise
      * @return netboot enabled value
      */
     public boolean isNetbootEnabled() {
         return Boolean.TRUE.toString().
             equalsIgnoreCase((String.valueOf(dataMap.get(NETBOOT_ENABLED))));
     }

      /**
      * @param virtBridgeIn the VirtBridge
      */
      public void setVirtBridge(String virtBridgeIn) {
          modify(VIRT_BRIDGE, virtBridgeIn);
      }

      /**
      * @param virtCpusIn the VirtCpus
      */
      public void setVirtCpus(int virtCpusIn) {
          modify(VIRT_CPUS, virtCpusIn);
      }

      /**
      * @param virtTypeIn the VirtType
      */
      public void setVirtType(String virtTypeIn) {
          modify(VIRT_TYPE, virtTypeIn);
      }

      /**
      * @param virtPathIn the VirtPath
      */
      public void setVirtPath(String virtPathIn) {
          modify(VIRT_PATH, virtPathIn);
      }

      /**
      * @param virtFileSizeIn the VirtFileSize
      */
      public void  setVirtFileSize(int virtFileSizeIn) {
          modify(VIRT_FILE_SIZE, virtFileSizeIn);
      }

      /**
      * @param virtRamIn the VirtRam
      */
      public void  setVirtRam(int virtRamIn) {
          modify(VIRT_RAM, virtRamIn);
      }

      /**
       * Enable netboot
       * @param enable true to enable net boot.
       */
      public void enableNetboot(boolean enable) {
          modify(NETBOOT_ENABLED, enable);
      }

      /**
       * @param nameServersIn the NameServers
       */
      public void  setNameServers(List<String> nameServersIn) {
          modify(NAME_SERVERS, nameServersIn);
      }

      /**
       * @param gateway the Gateway
       */
      public void  setGateway(String gateway) {
          modify(GATEWAY, gateway);
      }
      /**
       * @param hostname the hostname
       */
      public void  setHostName(String hostname) {
          modify(HOSTNAME, hostname);
      }

      /**
       * Associates a profile to this system record
       * @param profile the profile to associate
       */
      public void  setProfile(Profile profile) {
          setProfile(profile.getName());
      }

      /**
       * Associates a profile to this system record
       * @param profileName the name of the profile
       */
      public void  setProfile(String profileName) {
          modify(PROFILE, profileName);
      }

      /**
       * Sets the cobbler server host information for this system
       * @param server the server host name.
       */
      public void  setServer(String server) {
          modify(SERVER, server);
      }

      /**
       * Sets IPv6 autoconfiguration on
       * @param ipv6Autoconf boolean to indicate autoconf
       */
      public void setIpv6Autoconfiguration(boolean ipv6Autoconf) {
          modify(IPV6_AUTOCONF, ipv6Autoconf);
      }

      /**
       * Sets the network interfaces available to this system
       * @param interfaces a list of network interfaces
       */
      public void setNetworkInterfaces(List<Network> interfaces) {
          Map<String, Object> ifaces = new HashMap<String, Object>();
          for (Network net : interfaces) {
              ifaces.putAll(net.toMap());
          }
          modify(SET_INTERFACES, ifaces);
      }

      /**
       * @return a list of network interfaces associated to this system
       */
      public List<Network>  getNetworkInterfaces() {
          reload();
          List<Network> networks = new LinkedList<Network>();
          Map<String, Map<String, Object>> interfaces = (Map<String, Map<String, Object>>)
                                                      dataMap.get(GET_INTERFACES);
          if (interfaces != null) {
              for (String name : interfaces.keySet()) {
                  networks.add(Network.load(client, name, interfaces.get(name)));
              }
          }
          return networks;
      }
}
