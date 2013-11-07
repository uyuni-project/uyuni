/**
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

import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author paji
 * @version $Rev$
 */

public class Profile extends CobblerObject {

    /**
     * Logger for this class
     */
    private static Logger log = Logger.getLogger(Profile.class);

    private static final String DHCP_TAG = "dhcp_tag";
    private static final String KICKSTART = "kickstart";
    private static final String VIRT_BRIDGE = "virt_bridge";
    private static final String VIRT_CPUS = "virt_cpus";
    private static final String VIRT_TYPE = "virt_type";
    private static final String REPOS = "repos";
    private static final String VIRT_PATH = "virt_path";
    private static final String SERVER = "server";
    private static final String NAME_SERVERS = "name_servers";
    private static final String ENABLE_MENU = "enable_menu";
    private static final String VIRT_FILE_SIZE = "virt_file_size";
    private static final String VIRT_RAM = "virt_ram";
    private static final String DISTRO = "distro";

    /** Cobbler profile name for default PXE boot */
    public static final String BOOTSTRAP_NAME = "pxe-default-profile";


    private Profile(CobblerConnection clientIn) {
        client = clientIn;
    }

    /**
     * Create a new kickstart profile in cobbler
     * @param client the xmlrpc client
     * @param name the profile name
     * @param distro the distro allocated to this profile.
     * @return the newly created profile
     */
    public static Profile create(CobblerConnection client,
                                String name, Distro distro) {
        Profile profile = new Profile(client);
        profile.handle = (String) client.invokeTokenMethod("new_profile");
        profile.modify(NAME, name);
        profile.setDistro(distro);
        profile.save();
        profile = lookupByName(client, name);
        return profile;
    }

    /**
     * Returns a kickstart profile matching the given name or null
     * @param client the xmlrpc client
     * @param name the profile name
     * @return the profile that maps to the name or null
     */
    public static Profile lookupByName(CobblerConnection client, String name) {
        return handleLookup(client, lookupDataMapByName(client, name, "get_profile"));
    }

    /**
     *  Returns the profile matching the given uid or null
     * @param client client the xmlrpc client
     * @param id the uid of the profile
     * @return the profile matching the given uid or null
     */
    public static Profile lookupById(CobblerConnection client, String id) {
        if (id == null) {
            return null;
        }
        return handleLookup(client, lookupDataMapById(client, id,
                                        "find_profile"));
    }

    private static Profile handleLookup(CobblerConnection client, Map profileMap) {
        if (profileMap != null) {
            Profile profile = new Profile(client);
            profile.dataMap = profileMap;
            return profile;
        }
        return null;
    }
    /**
     * Returns a list of available profiles
     * @param connection the cobbler connection
     * @return a list of profiles.
     */
    public static List<Profile> list(CobblerConnection connection) {
        List <Profile> profiles = new LinkedList<Profile>();
        List <Map<String, Object >> cProfiles = (List <Map<String, Object >>)
                                        connection.invokeMethod("get_profiles");

        for (Map<String, Object> profMap : cProfiles) {
            Profile profile = new Profile(connection);
            profile.dataMap = profMap;
            profiles.add(profile);
        }
        return profiles;
    }


    /**
     * Returns a list of available profiles minus the excludes list
     * @param connection the cobbler connection
     * @param excludes a list of cobbler ids to file on
     * @return a list of profiles.
     */
    public static List<Profile> list(CobblerConnection connection,
                                Set<String> excludes) {
        List <Profile> profiles = new LinkedList<Profile>();
        List <Map<String, Object >> cProfiles = (List <Map<String, Object >>)
                                        connection.invokeMethod("get_profiles");

        for (Map<String, Object> profMap : cProfiles) {
            Profile profile = new Profile(connection);
            profile.dataMap = profMap;
            if (!excludes.contains(profile.getId())) {
                profiles.add(profile);
            }


        }
        return profiles;
    }

    @Override
    protected String invokeGetHandle() {
        return (String)client.invokeTokenMethod("get_profile_handle", this.getName());
    }

    @Override
    protected void invokeModify(String key, Object value) {
        client.invokeTokenMethod("modify_profile", getHandle(), key, value);
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
    }

    /* (non-Javadoc)
     * @see org.cobbler.CobblerObject#renameTo(java.lang.String)
     */

    @Override
    protected void invokeRename(String newNameIn) {
        client.invokeTokenMethod("rename_profile", getHandle(), newNameIn);
    }

    /**

     * @return the DhcpTag
     */
     public String getDhcpTag() {
         return (String)dataMap.get(DHCP_TAG);
     }

     /**
     * @return the Kickstart file path
     */
     public String getKickstart() {
         return (String)dataMap.get(KICKSTART);
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
     * @return the Repos
     */
     public List<String> getRepos() {
         return (List<String>)dataMap.get(REPOS);
     }

     /**
     * @return the VirtPath
     */
     public String getVirtPath() {
         return (String)dataMap.get(VIRT_PATH);
     }

     /**
     * @return the Server
     */
     public String getServer() {
         return (String)dataMap.get(SERVER);
     }

     /**
     * @return the NameServers
     */
     public String getNameServers() {
         return (String)dataMap.get(NAME_SERVERS);
     }

     /**
     * @return true if menu enabled
     */
     public boolean menuEnabled() {
         return 1 == (Integer)dataMap.get(ENABLE_MENU);
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
     * @return the Distro
     */
     public Distro getDistro() {
         String distroName = (String)dataMap.get(DISTRO);
         return Distro.lookupByName(client, distroName);
     }

     /**
      * @param dhcpTagIn the DhcpTag
      */
      public void  setDhcpTag(String dhcpTagIn) {
          modify(DHCP_TAG, dhcpTagIn);
      }

      /**
      * @param kickstartIn the Kickstart
      */
      public void  setKickstart(String kickstartIn) {
          modify(KICKSTART, kickstartIn);
      }

      /**
      * @param virtBridgeIn the VirtBridge
      */
      public void  setVirtBridge(String virtBridgeIn) {
          modify(VIRT_BRIDGE, virtBridgeIn);
      }

      /**
      * @param virtCpusIn the VirtCpus
      */
      public void  setVirtCpus(int virtCpusIn) {
          modify(VIRT_CPUS, virtCpusIn);
      }

      /**
      * @param virtTypeIn the VirtType
      */
      public void  setVirtType(String virtTypeIn) {
          modify(VIRT_TYPE, virtTypeIn);
      }

      /**
      * @param reposIn the Repos
      */
      public void  setRepos(List<String> reposIn) {
          modify(REPOS, reposIn);
      }

      /**
      * @param virtPathIn the VirtPath
      */
      public void  setVirtPath(String virtPathIn) {
          modify(VIRT_PATH, virtPathIn);
      }

      /**
      * @param serverIn the Server
      */
      public void  setServer(String serverIn) {
          modify(SERVER, serverIn);
      }

      /**
      * @param enableMenuIn the EnableMenu
      */
      public void  setEnableMenu(boolean enableMenuIn) {
          modify(ENABLE_MENU, enableMenuIn ? 1 : 0);
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
      * @param distroIn the Distro
      */
      public void  setDistro(Distro distroIn) {
          if (distroIn == null) {
              log.warn("Profile.setDistro was called with null.  This shouldn't happen, " +
                 "so we're ignoring");
              return;
          }
          setDistro(distroIn.getName());
      }

      /**
      * @param name the Distr name
      */
      public void  setDistro(String name) {
          modify(DISTRO, name);
      }

      /**
       * Generates the kickstart text and returns that
       * @return the generated kickstart text
       */
      public String generateKickstart() {
          return (String)client.invokeTokenMethod("generate_kickstart", getName());
      }

      /**
       *
       * {@inheritDoc}
       */
      public void syncRedHatManagementKeys(Collection<String> keysToRemove,
                                                  Collection<String> keysToAdd) {
          super.syncRedHatManagementKeys(keysToRemove, keysToAdd);
          for (SystemRecord record :
                      SystemRecord.listByAssociatedProfile(client, this.getName())) {
              record.syncRedHatManagementKeys(keysToRemove, keysToAdd);
              record.save();
          }
      }

}
