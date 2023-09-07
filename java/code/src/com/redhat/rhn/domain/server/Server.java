/*
 * Copyright (c) 2009--2015 Red Hat, Inc.
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
package com.redhat.rhn.domain.server;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.Identifiable;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.common.ProvisionState;
import com.redhat.rhn.domain.common.SatConfigFactory;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.config.ConfigChannelListProcessor;
import com.redhat.rhn.domain.config.ConfigChannelType;
import com.redhat.rhn.domain.config.ConfigurationFactory;
import com.redhat.rhn.domain.entitlement.Entitlement;
import com.redhat.rhn.domain.org.CustomDataKey;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.product.SUSEProductSet;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageType;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.configuration.ConfigurationManager;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerXMLRPCHelper;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.model.maintenance.MaintenanceSchedule;
import com.suse.utils.Opt;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cobbler.CobblerConnection;
import org.cobbler.SystemRecord;

import java.net.IDN;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Server - Class representation of the table rhnServer.
 */
public class Server extends BaseDomainHelper implements Identifiable {

    /**
     * Logger for this class
     */
    private static Logger log = LogManager.getLogger(Server.class);

    private Boolean ignoreEntitlementsForMigration;

    private Long id;
    private Org org;
    private String digitalServerId;
    private String os;
    private String osFamily;
    private String release;
    private String name;
    private String description;
    private String info;
    private String secret;
    private User creator;
    private String autoUpdate;
    private ContactMethod contactMethod;
    private String runningKernel;
    private Long lastBoot;
    private ServerArch serverArch;
    private ProvisionState provisionState;
    private Date channelsChanged;
    private Date created;
    private String cobblerId;
    private Set<Device> devices;
    private ServerInfo serverInfo;
    private Set<ServerPath> serverPaths = new HashSet<>();
    private CPU cpu;
    private ServerLock lock;
    private ServerUuid serverUuid;
    private Set<Note> notes;
    private Set<ServerFQDN> fqdns;
    private Ram ram;
    private Dmi dmi;
    private NetworkInterface primaryInterface;
    private Set<NetworkInterface> networkInterfaces;
    private Set<CustomDataValue> customDataValues;
    private Set<Channel> channels = new HashSet<>();
    private List<ConfigChannel> configChannels = new ArrayList<>();
    private Set<ConfigChannel> localChannels = new HashSet<>();
    private Location serverLocation;
    private Set<VirtualInstance> guests = new HashSet<>();
    private VirtualInstance virtualInstance;
    private PushClient pushClient;
    private final ConfigChannelListProcessor configListProc =
        new ConfigChannelListProcessor();
    private Set<ServerHistoryEvent> history = new HashSet<>();
    private Set<InstalledPackage> packages = new HashSet<>();
    private ProxyInfo proxyInfo;
    private MgrServerInfo mgrServerInfo;
    private Set<ServerGroup> groups = new HashSet<>();
    private Set<ClientCapability> capabilities = new HashSet<>();
    private Set<InstalledProduct> installedProducts = new HashSet<>();
    private String machineId;
    private String hostname;
    private boolean payg;
    private MaintenanceSchedule maintenanceSchedule;
    private Boolean hasConfigFeature;

    private String cpe;

    public static final String VALID_CNAMES = "valid_cnames_";

    /**
     * @return Returns the capabilities.
     */
    public Set<ClientCapability> getCapabilities() {
        return capabilities;
    }



    /**
     * @param capabilitiesIn The capabilities to set.
     */
    public void setCapabilities(Set<ClientCapability> capabilitiesIn) {
        capabilities = capabilitiesIn;
    }


    /**
     * Retrieves an unmodifiable collection containing the server groups.
     * @return Returns the groups.
     */
    public Set<ServerGroup> getUnmodifiableGroups() {
        return Set.copyOf(groups);
    }

    /**
     * @return Returns the groups.
     */
    public Set<ServerGroup> getGroups() {
        return groups;
    }

    /**
     * @param groupsIn The groups to set.
     */
    protected void setGroups(Set<ServerGroup> groupsIn) {
        groups = groupsIn;
    }

    /**
     * @return the proxyInfo
     */
    public ProxyInfo getProxyInfo() {
        return proxyInfo;
    }

    /**
     * the proxy information to set
     * @param proxy the proxyInfo to set
     */
    public void setProxyInfo(ProxyInfo proxy) {
        this.proxyInfo = proxy;
    }

    /**
     * @return the mgrServerInfo
     */
    public MgrServerInfo getMgrServerInfo() {
        return mgrServerInfo;
    }

    /**
     * the mgr server information to set
     * @param mgrServer the mgrServerInfo to set
     */
    public void setMgrServerInfo(MgrServerInfo mgrServer) {
        mgrServerInfo = mgrServer;
    }

    /**
     * Retrieves the local override channel associated with this system.
     * @return the Local Override Channel or create one if none exists
     */
    public ConfigChannel getLocalOverride() {
        return  findLocal(ConfigChannelType.local());
    }

    /**
     * Retrieves the local override channel associated with this system.
     * @return the Local Override Channel or NULL if there's none created yet
     */
    public ConfigChannel getLocalOverrideNoCreate() {
        ensureConfigManageable();
        ConfigChannel channel = null;
        for (ConfigChannel ch : localChannels) {
            if (ch.getConfigChannelType().equals(ConfigChannelType.local())) {
                channel = ch;
                break;
            }
        }
        return channel;
    }

    /**
     *
     * @param ch Override channel to set
     */
    public void setLocalOverride(ConfigChannel ch) {
        setLocalType(ch, ConfigChannelType.local());
    }

    private void setLocalType(ConfigChannel channel,
            ConfigChannelType cct) {

        ConfigChannel ch =  findLocal(cct);
        if (ch != null) {
            localChannels.remove(ch);
        }
        localChannels.add(channel);
    }

    protected void setLocalChannels(Set<ConfigChannel> chls) {
        localChannels = chls;
    }

    protected Set<ConfigChannel> getLocalChannels() {
        return localChannels;
    }

    /**
     * Used for retrieving Local/Sandbox override channels since the process is
     *  exacly the same. Creates the channel if it does not exist.
     * @param cct Config Channel type .. (local or sandbox)
     * @return Config channel associated with the given type
     */
    private ConfigChannel findLocal(ConfigChannelType cct) {

        assert localChannels.size() <= 2 : "More than two local override  channels" +
                "Associated with this server." +
                "There should be NO more than Two" +
                " Override Channels associated";
        ensureConfigManageable();
        for (ConfigChannel ch : localChannels) {
            ConfigChannelType item = ch.getConfigChannelType();
            if (cct.equals(item)) {
                return ch;
            }
        }

        //We automatically create local config channels, so
        //if we didn't find one, we just haven't created it yet.
        ConfigChannel channel = ConfigurationFactory.createNewLocalChannel(this, cct);

        //TODO: Adding the new channel to the set of local channels should
        //happen in the createNewLocalChannel method.  However, the way things
        //are currently set up, I have to work with the member variable, because using
        //accessors and mutators would create an infinite loop.  Fix this setup.
        localChannels.add(channel);
        setLocalChannels(localChannels);
        return channel;
    }

    /**
     * Retrieves the sandbox override channel associated with this system.
     * @return the Sandbox Override Channel or create one if none exists
     */
    public ConfigChannel getSandboxOverride() {
        return findLocal(ConfigChannelType.sandbox());
    }

    /**
     * Retrieves the sandbox override channel associated with this system.
     * @return the Sandbox Override Channel or NULL if there's none created yet
     */
    public ConfigChannel getSandboxOverrideNoCreate() {
        ensureConfigManageable();
        ConfigChannel channel = null;
        for (ConfigChannel ch : localChannels) {
            if (ch.getConfigChannelType().equals(ConfigChannelType.sandbox())) {
                channel = ch;
                break;
            }
        }
        return channel;
    }

    /**
     *
     * @param ch sets the sandbox override channel
     */
    public void setSandboxOverride(ConfigChannel ch) {
        setLocalType(ch, ConfigChannelType.sandbox());
    }

    /**
     * ONLY TO BE USED FOR/BY HIBERNATE
     * @param configChannelsIn The configChannels to set.
     */
    protected void setConfigChannelsHibernate(
            List<ConfigChannel> configChannelsIn) {
        configChannels = configChannelsIn;
        configChannels.removeIf(Objects::isNull);
    }

    /**
     * ONLY TO BE USED FOR/BY HIBERNATE
     *
     * @return List of config channels
     */
    protected List<ConfigChannel> getConfigChannelsHibernate() {
        return configChannels;
    }

    protected List<ConfigChannel> getConfigChannels() {
        ensureConfigManageable();
        return configChannels;
    }

    /**
     * Returns a stream of the ServerConfigChannels mappings currently available
     * to the server based on it's entitlements.
     *
     * Any modifications to the config channel subscriptions must be made using the
     * following methods:
     *
     *   {@link Server#subscribeConfigChannel(ConfigChannel, User)}
     *   {@link Server#subscribeConfigChannels(List, User)}
     *   {@link Server#unsubscribeConfigChannel(ConfigChannel, User)}
     *   {@link Server#unsubscribeConfigChannels(List, User)}
     *   {@link Server#setConfigChannels(List, User)}
     *
     * @return A stream of the ServerConfigChannels mappings
     */
    public Stream<ConfigChannel> getConfigChannelStream() {
        return getConfigChannels().stream();
    }

    /**
     * Returns a COPY list of the ServerConfigChannels mappings currently available
     * to the server based on it's entitlements.
     *
     * Any modifications to the config channel subscriptions must be made using the
     * following methods:
     *
     *   {@link Server#subscribeConfigChannel(ConfigChannel, User)}
     *   {@link Server#subscribeConfigChannels(List, User)}
     *   {@link Server#unsubscribeConfigChannel(ConfigChannel, User)}
     *   {@link Server#unsubscribeConfigChannels(List, User)}
     *   {@link Server#setConfigChannels(List, User)}
     *
     * @return A list of the ServerConfigChannels mappings
     */
    public List<ConfigChannel> getConfigChannelList() {
        return new ArrayList<>(getConfigChannels());
    }

    /**
     * @return Returns the number of configuration channels associated with
     * the server.
     */
    public int getConfigChannelCount() {
        return getConfigChannels().size();
    }

    public void setHasConfigFeature(Boolean hasConfig) {
        hasConfigFeature = hasConfig;
    }

    private void ensureConfigManageable() {
        if (!getIgnoreEntitlementsForMigration()) {
            if (hasConfigFeature != null) {
                if (Boolean.FALSE.equals(hasConfigFeature)) {
                    String msg = "Config feature needs to be enabled on the server" +
                            " for handling Config Management. The provided server [%s]" +
                            " does not have have this enabled. Add provisioning" +
                            " capabilities to the system to enable this..";
                    throw new PermissionException(String.format(msg, getId()));
                }
            }
            else {
                ConfigurationManager.getInstance().ensureConfigManageable(this);
            }
        }
    }

    /**
     * Subscribes a channel to a system, appending it to the last position with the
     * least priority.
     * @param configChannel The config channel to subscribe to
     * @param user The user doing the action
     */
    public final void subscribeConfigChannel(ConfigChannel configChannel, User user) {
        this.subscribeConfigChannels(Collections.singletonList(configChannel), user);
    }

    /**
     * Subscribes channels to a system, appending them to the last position with the
     * least priority in the order provided
     * @param configChannelList A {@link List} of the config channels to subscribe to
     * @param user The user doing the action
     */
    public void subscribeConfigChannels(List<ConfigChannel> configChannelList, User user) {
        ensureConfigManageable();
        configChannelList.forEach(cc -> configListProc.add(getConfigChannelsHibernate(), cc));
    }

    /**
     * Unsubscribes the system from the channel
     * @param configChannel The config channel to unsubscribe
     * @param user The user doing the action
     */
    public final void unsubscribeConfigChannel(ConfigChannel configChannel, User user) {
        this.unsubscribeConfigChannels(Collections.singletonList(configChannel), user);
    }

    /**
     * Unsubscribes the system from a list of channels
     * @param configChannelList A {@link List} of config channels to unsubscribe
     * @param user The user doing the action
     */
    public void unsubscribeConfigChannels(List<ConfigChannel> configChannelList, User user) {
        configChannelList.forEach(cc -> configListProc.remove(getConfigChannels(), cc));
    }

    /**
     * subscribes channels to a system, removing all previous channel subscriptions
     * @param configChannelList A {@link List} of the config channels to subscribe to
     * @param user The user doing the action
     */
    public void setConfigChannels(List<ConfigChannel> configChannelList, User user) {
        configListProc.replace(getConfigChannels(), configChannelList);
    }

    /**
     * Save configuration channels to the database. Only needed if the server has been created using the constructor
     */
    public void storeConfigChannels() {
        HibernateFactory.getSession().createNativeQuery("DELETE FROM rhnServerConfigChannel WHERE server_id = :sid ;")
                .setParameter("sid", getId())
                .executeUpdate();

        if (!configChannels.isEmpty()) {
            String values = IntStream.range(0, configChannels.size())
                    .boxed()
                    .map(i -> String.format("(%s, %s, %s)", getId(), configChannels.get(i).getId(), i + 1))
                    .collect(Collectors.joining(","));


            HibernateFactory.getSession().createNativeQuery(
                            "INSERT INTO rhnServerConfigChannel (server_id, config_channel_id, position) " +
                                    "VALUES " + values + ";")
                    .executeUpdate();
        }
    }

    /**
     * Protected constructor
     */
    protected Server() {
        devices = new HashSet<>();
        notes = new HashSet<>();
        networkInterfaces = new HashSet<>();
        customDataValues = new HashSet<>();
        fqdns = new HashSet<>();

        ignoreEntitlementsForMigration = Boolean.FALSE;
    }

    /**
     * Minimal constructor used to avoid loading all properties in SSM config channel subscription
     *
     * @param idIn the server id
     * @param machineIdIn the machine id
     */
    public Server(long idIn, String machineIdIn) {
        id = idIn;
        machineId = machineIdIn;
        hasConfigFeature = Boolean.TRUE;
        ignoreEntitlementsForMigration = Boolean.FALSE;
    }

    /**
     * @return Returns the serverInfo.
     */
    public ServerInfo getServerInfo() {
        return serverInfo;
    }
    /**
     * @param serverInfoIn The serverInfo to set.
     */
    public void setServerInfo(ServerInfo serverInfoIn) {
        this.serverInfo = serverInfoIn;
    }

    /**
     * Gets the server paths.
     *
     * @return the server paths
     */
    public Set<ServerPath> getServerPaths() {
        return serverPaths;
    }

    /**
     * Sets the server paths.
     *
     * @param serverPathsIn the new server paths
     */
    public void setServerPaths(Set<ServerPath> serverPathsIn) {
        serverPaths = serverPathsIn;
    }

    /**
     * Get the {@link ServerPath} on position 0.
     * @return {@link ServerPath} on position 0
     */
    public Optional<ServerPath> getFirstServerPath() {
        return serverPaths.stream()
                .filter(path -> path.getPosition() == 0)
                .findFirst();
    }

    /**
     * Gets the last checkin date for this server
     * @return last checkin date
     */
    public Date getLastCheckin() {
        return serverInfo.getCheckin();
    }
    /**
     * Gets the number of times this server has checked in
     * @return number of times this server has checked in.
     */
    public Long getCheckinCount() {
        return serverInfo.getCheckinCounter();
    }
    /**
     * Getter for id
     *
     * @return Long to get
     */
    @Override
    public Long getId() {
        return this.id;
    }

    /**
     * Setter for id
     *
     * @param idIn to set
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * @return Returns the org.
     */
    public Org getOrg() {
        return org;
    }

    /**
     * @param o The org to set.
     */
    public void setOrg(Org o) {
        this.org = o;
    }

    /**
     * Getter for digitalServerId
     *
     * @return String to get
     */
    public String getDigitalServerId() {
        return this.digitalServerId;
    }

    /**
     * Setter for digitalServerId
     *
     * @param digitalServerIdIn to set
     */
    public void setDigitalServerId(String digitalServerIdIn) {
        this.digitalServerId = digitalServerIdIn;
    }

    /**
     * Getter for os
     *
     * @return String to get
     */
    public String getOs() {
        return this.os;
    }

    /**
     * Setter for os
     *
     * @param osIn to set
     */
    public void setOs(String osIn) {
        this.os = osIn;
    }

    /**
     * Getter for release
     *
     * @return String to get
     */
    public String getRelease() {
        return this.release;
    }

    /**
     * Setter for release
     *
     * @param releaseIn to set
     */
    public void setRelease(String releaseIn) {
        this.release = releaseIn;
    }

    /**
     * Getter for name
     *
     * @return String to get
     */
    public String getName() {
        return this.name;
    }

    /**
     * Setter for name
     *
     * @param nameIn to set
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * Getter for description
     *
     * @return String to get
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Setter for description
     *
     * @param descriptionIn to set
     */
    public void setDescription(String descriptionIn) {
        this.description = descriptionIn;
    }

    /**
     * Getter for info
     *
     * @return String to get
     */
    public String getInfo() {
        return this.info;
    }

    /**
     * Setter for info
     *
     * @param infoIn to set
     */
    public void setInfo(String infoIn) {
        this.info = infoIn;
    }

    /**
     * Getter for secret
     *
     * @return String to get
     */
    public String getSecret() {
        return this.secret;
    }

    /**
     * Setter for secret
     *
     * @param secretIn to set
     */
    public void setSecret(String secretIn) {
        this.secret = secretIn;
    }

    /**
     * @return Returns the creator.
     */
    public User getCreator() {
        return creator;
    }

    /**
     * @param c The creator to set.
     */
    public void setCreator(User c) {
        this.creator = c;
    }

    /**
     * Getter for autoUpdate
     *
     * @return String to get
     */
    public String getAutoUpdate() {
        return this.autoUpdate;
    }

    /**
     * Setter for autoUpdate
     *
     * @param autoUpdateIn to set
     */
    public void setAutoUpdate(String autoUpdateIn) {
        this.autoUpdate = autoUpdateIn;
    }

    /**
     * Get the contact method.
     *
     * @return contact method
     */
    public ContactMethod getContactMethod() {
        return this.contactMethod;
    }

    /**
     * Get the contact method label.
     *
     * @return contact method label
     */
    public Optional<String> getContactMethodLabel() {
        String label = (this.contactMethod == null) ? null : this.contactMethod.getLabel();
        return Optional.ofNullable(label);
    }

    /**
     * Set the contact method.
     *
     * @param contactMethodIn method to set
     */
    public void setContactMethod(ContactMethod contactMethodIn) {
        this.contactMethod = contactMethodIn;
    }

    /**
     * Getter for runningKernel
     *
     * @return String to get
     */
    public String getRunningKernel() {
        return this.runningKernel;
    }

    /**
     * Setter for runningKernel
     *
     * @param runningKernelIn to set
     */
    public void setRunningKernel(String runningKernelIn) {
        this.runningKernel = runningKernelIn;
    }

    /**
     * Getter for lastBoot
     *
     * @return Long to get
     */
    public Long getLastBoot() {
        return this.lastBoot;
    }

    /**
     * Getter for lastBoot as a date
     *
     * @return lastBoot time as a Date object
     */
    public Date getLastBootAsDate() {
        return new Date(this.lastBoot * 1000);
    }

    /**
     * Setter for lastBoot
     *
     * @param lastBootIn to set
     */
    public void setLastBoot(Long lastBootIn) {
        this.lastBoot = lastBootIn;
    }

    /**
     * @return Returns the serverArch.
     */
    public ServerArch getServerArch() {
        return serverArch;
    }

    /**
     * @param s The serverArch to set.
     */
    public void setServerArch(ServerArch s) {
        this.serverArch = s;
    }

    /**
     * @return Returns the provisionState.
     */
    public ProvisionState getProvisionState() {
        return provisionState;
    }

    /**
     * @param p The provisionState to set.
     */
    public void setProvisionState(ProvisionState p) {
        this.provisionState = p;
    }

    /**
     * Getter for channelsChanged
     *
     * @return Date to get
     */
    public Date getChannelsChanged() {
        return this.channelsChanged;
    }

    /**
     * Setter for channelsChanged
     *
     * @param channelsChangedIn to set
     */
    public void setChannelsChanged(Date channelsChangedIn) {
        this.channelsChanged = channelsChangedIn;
    }

    /**
     * The set of group types of the entitled server groups that this Server is a member of
     * @return Returns the serverGroups.
     */
    public List<ServerGroupType> getEntitledGroupTypes() {
        return this.groups.stream().filter(g ->g.getGroupType() != null)
                .map(ServerGroup::getGroupType).collect(Collectors.toList());
    }

    /**
     * The set of entitled ServerGroup(s) that this Server is a member of
     * @return Returns the serverGroups.
     */
    public List<EntitlementServerGroup> getEntitledGroups() {
        return this.groups.stream().filter(g ->g.getGroupType() != null)
                .map(s -> (EntitlementServerGroup) s).collect(Collectors.toList());
    }

    /**
     * The set of managed ServerGroup(s) that this Server is a member of
     * @return Returns the serverGroups.
     */
    public List<ManagedServerGroup> getManagedGroups() {
        return this.groups.stream().filter(g -> g.getGroupType() == null)
                .map(s -> (ManagedServerGroup) s).collect(Collectors.toList());
    }

    /**
     * Returns the set of devices attached to this server.
     * @return Returns the list of devices attached to this server.
     */
    public Set<Device> getDevices() {
        return devices;
    }
    /**
     * Sets the set of devices.
     * @param devicesIn The devices to set.
     */
    protected void setDevices(Set<Device> devicesIn) {
        devices = devicesIn;
    }

    /**
     * Get the Device with the given description (i.e. eth0)
     * @param dev the device name (i.e. sda)
     * @return the Device, otherwise null
     */
    public Device getDevice(String dev) {
        for (Device d : getDevices()) {
            if ((d.getDevice() != null) && (d.getDevice().equals(dev))) {
                return d;
            }
        }
        return null;
    }

    /**
     * Adds a device to the list of devices for this server.
     * @param device Device to add
     */
    public void addDevice(Device device) {
        device.setServer(this);
        devices.add(device);
    }

    /**
     * @return Returns the notes.
     */
    public Set<Note> getNotes() {
        return notes;
    }

    /**
     * @param n The notes to set.
     */
    public void setNotes(Set<Note> n) {
        this.notes = n;
    }

    /**
     * Adds a note to the notes set
     * @param note The note to add
     */
    public void addNote(Note note) {
        note.setServer(this);
        notes.add(note);
    }

    /**
     * Adds a note to the notes set.
     * @param user The user creating the note
     * @param subject The subject for the note
     * @param body The body for the note
     */
    public void addNote(User user, String subject, String body) {
        Note note = new Note();
        note.setCreator(user);
        note.setSubject(subject);
        note.setNote(body);
        note.setCreated(new Date());

        addNote(note);
    }

    /**
     * Get the primary ip address for this server
     * @return Returns the primary ip for this server
     */
    public String getIpAddress() {
        NetworkInterface ni = findPrimaryNetworkInterface();
        if (ni != null) {
            return ni.getIPv4Addresses().stream().findFirst().map(ServerNetAddress4::getAddress).orElse(null);
        }
        return null;
    }

    /**
     * Return the ServerFQDN which is considered the primary.
     *
     * If primary FQDN is not explicitly set, the first one
     * is returned, based on the alphabetic order.
     *
     * @return The primary FQDN for this server
     */
    public ServerFQDN findPrimaryFqdn() {
        return this.fqdns.stream()
                .filter(ServerFQDN::isPrimary)
                .findFirst()
                .orElseGet(() -> {
                    if (!fqdns.isEmpty()) {
                        return Collections.min(fqdns, Comparator.comparing(ServerFQDN::getName));
                    }
                    return null;
                });
    }

    /**
     * @param fqdnName The FQDN to be obtained
     * @return The fqdn with the specified name
     */
    public Optional<ServerFQDN> lookupFqdn(String fqdnName) {
        return this.fqdns.stream()
                .filter(fqdn -> fqdn.getName().equals(fqdnName))
                .findFirst();
    }

    /**
     * Get the primary ipv6 address for this server
     * @return Returns the primary ip for this server
     */
    public String getIp6Address() {
        NetworkInterface ni = findPrimaryNetworkInterface();
        if (ni != null) {
            for (ServerNetAddress6 ipv6address : ni.getIPv6Addresses()) {
                log.debug("Found a NetworkInterface: {}", ipv6address);
                if (!ipv6address.getAddress().equals("::1")) {
                    return ipv6address.getAddress();
                }
            }
        }
        return null;
    }


    /**
     * Return the NetworkInterface which Spacewalk is guessing is
     * the primary.  Order of preference:
     *
     * eth0, eth0*, eth1, eth1*, after that its first match that is
     * not 127.0.0.1
     *
     * @return NetworkInterface in order of preference: eth0, eth0*,
     * eth1, eth1*, after that its first match that is not 127.0.0.1
     */
    public NetworkInterface findPrimaryNetworkInterface() {
        primaryInterface = lookupForPrimaryInterface();
        if (primaryInterface != null) {
            return primaryInterface;
        }
        if (!networkInterfaces.isEmpty()) {
            // First pass look for names
            NetworkInterface ni = null;

            ni = findActiveIfaceWithName("eth0", false);
            if (ni != null) {
                primaryInterface = ni;
                return ni;
            }
            ni = findActiveIfaceWithName("eth0", true);
            if (ni != null) {
                primaryInterface = ni;
                return ni;
            }
            ni = findActiveIfaceWithName("eth1", false);
            if (ni != null) {
                primaryInterface = ni;
                return ni;
            }
            ni = findActiveIfaceWithName("eth1", true);
            if (ni != null) {
                primaryInterface = ni;
                return ni;
            }
            // Second pass look for localhost
            Iterator<NetworkInterface> i = networkInterfaces.iterator();
            while (i.hasNext()) {
                NetworkInterface n = i.next();
                for (ServerNetAddress4 ad4 : n.getIPv4Addresses()) {
                    if (ad4 != null && !ad4.getAddress().equals("127.0.0.1")) {
                        log.debug("Found NetworkInterface !localhost");
                        primaryInterface = n;
                        return n;
                    }
                }
                for (ServerNetAddress6 ad6 : n.getIPv6Addresses()) {
                    if (ad6 != null && !ad6.getAddress().equals("::1")) {
                        log.debug("Found NetworkInterface !localhost");
                        primaryInterface = n;
                        return n;
                    }
                }
            }
            // If we didnt match any of the above criteria
            // just give up and return the 1st one.
            log.debug("just returning 1st network interface");
            primaryInterface = networkInterfaces.iterator().next();
            return primaryInterface;
        }
        primaryInterface = null;
        return null;
    }

    private NetworkInterface findActiveIfaceWithName(String pattern, boolean startsWith) {
        if (networkInterfaces.isEmpty()) {
            return null;
        }
        for (NetworkInterface ni : networkInterfaces) {
            if (ni.isDisabled()) {
                continue;
            }
            if (startsWith) {
                if (ni.getName().startsWith(pattern)) {
                    log.debug("Found {}*", pattern);
                    return ni;
                }
            }
            else {
                if (ni.getName().equals(pattern)) {
                    log.debug("Found {}", pattern);
                    return ni;
                }
            }
        }
        return null;
    }

    /**
     * Get the primary MAC/hardware address for this server
     * @return Returns the primary MAC/hardware for this server
     */
    public String getHardwareAddress() {
        NetworkInterface network = findPrimaryNetworkInterface();
        if (network != null) {
            return network.getHwaddr();
        }
        return null;
    }

    /**
     * Get the hostname aliases for this server
     * @return Returns the hostname aliases for this server
     */
    public List<String> getCnames() {
        List<String> result = new ArrayList<>();
        List<String> proxyCnames = Config.get().getList(
                VALID_CNAMES +
                serverInfo.getId().toString());
        if (!proxyCnames.isEmpty()) {
            result.addAll(proxyCnames);
        }
        return result;
    }

    /**
     * Get the primary hostname for this server
     * If hostname is IDN, it is decoded from Puny encoding
     * @return Returns the primary hostname for this server
     */
    public String getDecodedHostname() {
        return (hostname == null) ? null : IDN.toUnicode(hostname);
    }

    /**
     * Get the hostname aliases (cname records) for this server
     * If hostname is IDN, it is decoded from Puny encoding
     * @return Returns the primary hostname for this server
     */
    public List<String> getDecodedCnames() {
        List<String> result = new ArrayList<>();
        for (String host : getCnames()) {
            result.add(IDN.toUnicode(host));
        }
        return result;
    }

    /**
     * @return Returns the networkInterfaces.
     */
    public Set<NetworkInterface> getNetworkInterfaces() {
        return networkInterfaces;
    }

    /**
     * @param n The networkInterfaces to set.
     */
    public void setNetworkInterfaces(Set<NetworkInterface> n) {
        this.networkInterfaces = n;
    }

    /**
     * Adds a network interface to the set of network interfaces
     * for this server.
     * @param netIn The NetworkInterface to add
     */
    public void addNetworkInterface(NetworkInterface netIn) {
        netIn.setServer(this);
        networkInterfaces.add(netIn);
    }

    /**
     * Returns the total amount of ram for this server.
     * @return the total amount of ram for this server.
     */
    public long getRam() {
        if (ram == null) {
            return 0;
        }
        return ram.getRam();
    }

    /**
     * Convenience method for formatting the Ram as a String value.
     * @return String of RAM.
     */
    public String getRamString() {
        return Long.toString(getRam());
    }

    /**
     * the total amount of ram for this server.
     * @param ramIn The ram to set.
     */
    public void setRam(long ramIn) {
        initializeRam();
        ram.setRam(ramIn);
    }

    /**
     * Returns the  amount of swap for this server.
     * @return the  amount of swap for this server.
     */
    public long getSwap() {
        if (ram == null) {
            return 0;
        }
        return ram.getSwap();
    }

    /**
     * the amount of swap for this server.
     * @param swapIn the amount of swap for this server.
     */
    public void setSwap(long swapIn) {
        initializeRam();
        ram.setSwap(swapIn);
    }

    /**
     * @return Returns the cpu.
     */
    public CPU getCpu() {
        return cpu;
    }

    /**
     * @param cpuIn The cpu to set.
     */
    public void setCpu(CPU cpuIn) {
        this.cpu = cpuIn;
    }

    /**
     * @return Returns the dmi.
     */
    public Dmi getDmi() {
        return dmi;
    }

    /**
     * @param dmiIn The dmi to set.
     */
    public void setDmi(Dmi dmiIn) {
        dmi = dmiIn;
    }

    /**
     * @return Returns the serverLocation associated with the server.
     */
    public Location getLocation() {
        return serverLocation;
    }

    /**
     * @param locationIn Location to associate with the server.
     */
    public void setLocation(Location locationIn) {
        serverLocation = locationIn;
    }

    private void initializeRam() {
        if (ram == null) {
            ram = new Ram();
            ram.setServer(this);
        }
    }

    /**
     * @return Returns the customDataValues.
     */
    public Set<CustomDataValue> getCustomDataValues() {
        return customDataValues;
    }

    /**
     * @param customDataValuesIn The customDataValues to set.
     */
    public void setCustomDataValues(Set<CustomDataValue> customDataValuesIn) {
        this.customDataValues = customDataValuesIn;
    }

    /**
     * Adds a custom data value to the set of custom data values
     * for this server.
     * @param value The CustomDataValue to add
     */
    public void addCustomDataValue(CustomDataValue value) {
        value.setServer(this);
        customDataValues.add(value);
    }

    /**
     * Adds a custom data value to the set of custom data values
     * @param key The CustomDataKey for this value
     * @param value The value to set
     * @param user The user doing the setting
     */
    public void addCustomDataValue(CustomDataKey key, String value, User user) {
        // Check for null key values.
        if (key == null || key.getLabel() == null) {
            throw new
            UndefinedCustomDataKeyException("CustomDataKey can not be null.");
        }

        // Make sure this org has this particular CustomDataKey defined
        if (!org.hasCustomDataKey(key.getLabel())) {
            throw new
            UndefinedCustomDataKeyException("CustomDataKey: " + key.getLabel() +
                    " is not defined for this org.");
        }

        // get the CustomDataValue
        CustomDataValue customValue = getCustomDataValue(key);

        // does the server already have this key defined?
        if (customValue == null) {
            // create a new CustomDataValue object
            customValue = new CustomDataValue();
            customValue.setCreator(user);
            customValue.setKey(key);
        }
        customValue.setValue(value);
        customValue.setLastModifier(user);
        // add customValue to customDataValues set
        addCustomDataValue(customValue);
    }

    /**
     * Adds a custom data value to the set of custom data values
     * @param keyLabel The label for the CustomDataKey for this value
     * @param value The value to set
     * @param user The user doing the setting
     */
    public void addCustomDataValue(String keyLabel, String value, User user) {
        // look up CustomDataKey by keyLabel
        CustomDataKey key = OrgFactory.lookupKeyByLabelAndOrg(keyLabel, user.getOrg());
        addCustomDataValue(key, value, user);
    }

    /**
     * Retrieves a specific CustomDataValue from the customDataValues set
     * @param key The Key for the value you're looking up
     * @return Returns a CustomDataValue if it exists for this server. null otherwise.
     */
    public CustomDataValue getCustomDataValue(CustomDataKey key) {
        return ServerFactory.getCustomDataValue(key, this);
    }

    /**
     * Returns the set of Channels this Server is subscribed to.
     * @return the set of Channels this Server is subscribed to.
     */
    public Set<Channel> getChannels() {
        return channels;
    }

    /**
     * Set channels
     * @param chans the channels
     */
    public void setChannels(Set<Channel> chans) {
        channels = chans;
    }

    /**
     * Adds the given channel to this Server.
     * @param c Channel to be added.
     */
    public void addChannel(Channel c) {
        channels.add(c);
    }

    /**
     * Returns the base channel for this server or null if not set.
     * @return Returns the base channel for this server or null if not set.
     */
    public Channel getBaseChannel() {
        /*
         * The base channel for a given server is designated in the database by
         * parent_channel == null. Since the number of channels for a given server is
         * relatively small, loop through the channels set and look for one without a
         * parentChannel instead of going back to the db.
         */
        for (Channel channel : channels) {
            if (channel.getParentChannel() == null) {
                // This is the base channel
                return channel;
            }
        }
        // Either we have no channels or all channels have a parent_channel. In either
        // case, the base channel cannot be determined for this server.
        return null;
    }

    /**
     * Returns true if this is a mgr server.
     * @return true if this is a mgr server.
     */
    public boolean isMgrServer() {
        return getMgrServerInfo() != null;
    }

    /**
     * Returns true if this is a proxy server.
     * @return true if this is a proxy server.
     */
    public boolean isProxy() {
        return getProxyInfo() != null;
    }

    /**
     * Returns true if the server has the given Entitlement.
     * @param entitlement Entitlement to verify.
     * @return true if the server has the given Entitlement.
     */
    public boolean hasEntitlement(Entitlement entitlement) {
        return this.getEntitledGroupTypes().stream().anyMatch(sgt -> sgt.getLabel().equals(entitlement.getLabel()));
    }

    /**
     * Give a set of the entitlements a server has.
     * This is entirely based on the server groups, but server
     * groups also contain user defined groups.
     * @return a set of Entitlement objects
     */
    public Set<Entitlement> getEntitlements() {
        return this.getEntitledGroupTypes().stream().map(sgt -> EntitlementManager.getByName(sgt.getLabel()))
                .collect(Collectors.toSet());
    }

    /**
     * Base entitlement for the Server.
     * @return Entitlement that is the base entitlement for the server
     */
    public Entitlement getBaseEntitlement() {
        List<ServerGroupType> serverGroupTypes = getEntitledGroupTypes();

       return serverGroupTypes.stream().filter(ServerGroupType::isBase).findFirst()
                .map(sgt -> EntitlementManager.getByName(sgt.getLabel())).orElse(null);
    }

    /**
     * Retrieves the Id of the base entitlement for the Server.
     * @return Entitlement Id of the base entitlement for the server
     */
    public Optional<Long> getBaseEntitlementId() {
        List<ServerGroupType> serverGroupTypes = getEntitledGroupTypes();

       return serverGroupTypes.stream().filter(ServerGroupType::isBase).findFirst()
                .map(ServerGroupType::getId);
    }

    /**
     * Set of add-on entitlements for the Server.
     * @return Set of entitlements that are add-on entitlements for the server
     */
    public Set<Entitlement> getAddOnEntitlements() {
        return this.getEntitledGroupTypes().stream().filter(Predicate.not(ServerGroupType::isBase))
                .map(sgt -> EntitlementManager.getByName(sgt.getLabel())).collect(Collectors.toSet());
    }

    /**
     * Returns a comma-delimted list of add-on entitlements with their human readable
     * labels.
     *
     * @return A comma-delimted list of add-on entitlements with their human readable
     * labels.
     */
    public String getAddOnEntitlementsAsText() {
        Set<?> addOnEntitlements = getAddOnEntitlements();
        Iterator<?> iterator = addOnEntitlements.iterator();
        StringBuilder buffer = new StringBuilder();
        Entitlement entitlement = null;

        while (iterator.hasNext()) {
            entitlement = (Entitlement)iterator.next();
            buffer.append(entitlement.getHumanReadableLabel()).append(", ");
        }

        if (!addOnEntitlements.isEmpty()) {
            buffer.delete(buffer.length() - 2, buffer.length());
        }

        return buffer.toString();
    }

    /**
     * Returns all labels of entitlements associated with this system.
     * @return labels
     */
    public Set<String> getEntitlementLabels() {
        return getGroups().stream()
                .map(ServerGroup::getGroupType)
                .filter(Objects::nonNull)
                .map(ServerGroupType::getLabel)
                .collect(Collectors.toSet());
    }

    /**
     * Return <code>true</code> if this is a virtual host, <code>false</code> otherwise.
     * If this is a host system, {@link #getVirtualInstance()} will always be <code>null
     * </code> since we are not supporting/implementing guests of guest in the RHN 500
     * release.
     *
     * @return true if the system is a virtual host
     */
    public boolean isVirtualHost() {
        return (SystemManager.isVirtualHost(getOrg().getId(), getId())) ||
                hasVirtualizationEntitlement();
    }

    /**
     * Return <code>true</code> if this a guest system, <code>false</code> otherwise. If
     * this system is a guest, {@link #getVirtualInstance()} will be non-<code>null</code>.
     *
     * @return <code>true</code> if this a guest system, <code>false</code> otherwise.
     */
    public boolean isVirtualGuest() {
        return getVirtualInstance() != null;
    }

    /**
     * Return <code>true</code> if this system has virtualization entitlement,
     * <code>false</code> otherwise.
     * @return <code>true</code> if this system has virtualization entitlement,
     *      <code>false</code> otherwise.
     */
    public boolean hasVirtualizationEntitlement() {
        return hasEntitlement(EntitlementManager.VIRTUALIZATION);
    }

    /**
     * Return <code>true</code> if this system has container buildhost entitlement,
     * <code>false</code> otherwise.
     * @return <code>true</code> if this system has container buildhost entitlement,
     *      <code>false</code> otherwise.
     */
    public boolean hasContainerBuildHostEntitlement() {
        return hasEntitlement(EntitlementManager.CONTAINER_BUILD_HOST);
    }

    /**
     * Return <code>true</code> if this system has OS Image buildhost entitlement,
     * <code>false</code> otherwise.
     * @return <code>true</code> if this system has OS Image buildhost entitlement,
     *      <code>false</code> otherwise.
     */
    public boolean hasOSImageBuildHostEntitlement() {
        return hasEntitlement(EntitlementManager.OSIMAGE_BUILD_HOST);
    }

    /**
     * Return <code>true</code> if this system has Ansible control node entitlement,
     * <code>false</code> otherwise.
     * @return <code>true</code> if this system has Ansible control node entitlement,
     *      <code>false</code> otherwise.
     */
    public boolean hasAnsibleControlNodeEntitlement() {
        return hasEntitlement(EntitlementManager.ANSIBLE_CONTROL_NODE);
    }

    /**
     * Return <code>true</code> if this is a bare metal system.
     * @return <code>true</code> if this is bare metal
     */
    public boolean isBootstrap() {
        return hasEntitlement(EntitlementManager.BOOTSTRAP);
    }

    /**
     * Return <code>true</code> if this is a foreign unmanaged system.
     * @return <code>true</code> if this is a foreign unmanaged system.
     */
    public boolean isForeign() {
        return hasEntitlement(EntitlementManager.FOREIGN);
    }

    /**
     *
     * @return the virtual guests
     */
    public Set<VirtualInstance> getVirtualGuests() {
        return guests;
    }

    /**
     * @param virtualGuests the virtual guests to use
     */
    public void setVirtualGuests(Set<VirtualInstance> virtualGuests) {
        // This function is used by hibernate
        this.guests = virtualGuests;
    }

    /**
     * Returns a read-only collection of VirtualInstance objects.
     * @return A read-only collection of VirtualInstance objects.
     */
    public Collection<VirtualInstance> getGuests() {
        Set<VirtualInstance> retval = new HashSet<>();
        for (VirtualInstance vi : getVirtualGuests()) {
            // Filter out the hosts that sometimes show up in this table.
            // Hosts have no UUID defined.
            if (vi.getUuid() != null) {
                retval.add(vi);
            }
        }
        return Collections.unmodifiableCollection(retval);
    }

    /**
     *
     * @param guest the guest to add
     */
    public void addGuest(VirtualInstance guest) {
        guest.setHostSystem(this);
        guests.add(guest);
    }

    /**
     * Remove the association between a guest and this server, but do not delete the
     * guest server.
     *
     * @param guest Guest to remove from this server.
     * @return <code>true</code> if the guest is deleted, <code>false</code> otherwise.
     */
    public boolean removeGuest(VirtualInstance guest) {
        boolean deleted = false;
        for (Iterator<VirtualInstance> it = guests.iterator(); it.hasNext();) {
            VirtualInstance g = it.next();
            if (g.getId().equals(guest.getId())) {
                it.remove();
                deleted = true;
                break;
            }
        }

        return deleted;
    }


    /**
     * Return the virtual instance that owns this server when the server is a virtual guest.
     *
     * @return The virtual instance that owns this server when the server is a virtual
     * guest. If the server is not a guest, the method returns <code>null</code>.
     */
    public VirtualInstance getVirtualInstance() {
        return virtualInstance;
    }

    /**
     * Sets the owning virtual instance for this server, which effectively makes this a
     * guest system.
     *
     * @param instance The owning virtual instance
     */
    // Note that while the relationship between guest and virtual instance needs to be
    // bi-directional, we want to manage the relationship (add/delete) from the virtual
    // instance since it is the owner/parent. Hence, the reason for package visibility on
    // this method.
    void setVirtualInstance(VirtualInstance instance) {
        virtualInstance = instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Server)) {
            return false;
        }
        Server castOther = (Server) other;

        Optional<PackageEvr> proxyVersion =
                Optional.ofNullable(proxyInfo).map(ProxyInfo::getVersion);
        Optional<PackageEvr> otherProxyVersion =
                Optional.ofNullable(castOther.getProxyInfo()).map(ProxyInfo::getVersion);
        Optional<PackageEvr> mgrVersion =
                Optional.ofNullable(mgrServerInfo).map(MgrServerInfo::getVersion);
        Optional<PackageEvr> otherMgrVersion =
                Optional.ofNullable(castOther.getMgrServerInfo()).map(MgrServerInfo::getVersion);

        return new EqualsBuilder().append(os, castOther.getOs())
                .append(release, castOther.getRelease())
                .append(name, castOther.getName())
                .append(description, castOther.getDescription())
                .append(info, castOther.getInfo())
                .append(secret, castOther.getSecret())
                .append(autoUpdate, castOther.getAutoUpdate())
                .append(runningKernel, castOther.getRunningKernel())
                .append(lastBoot, castOther.getLastBoot())
                .append(channelsChanged, castOther.getChannelsChanged())
                .append(proxyVersion, otherProxyVersion)
                .append(mgrVersion, otherMgrVersion)
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        Optional<PackageEvr> proxyVersion =
                Optional.ofNullable(getProxyInfo()).map(ProxyInfo::getVersion);
        Optional<PackageEvr> mgrVersion =
                Optional.ofNullable(getMgrServerInfo()).map(MgrServerInfo::getVersion);
        return new HashCodeBuilder().append(id).append(digitalServerId).append(os)
                .append(release).append(name).append(description)
                .append(info).append(secret)
                .append(autoUpdate).append(runningKernel)
                .append(lastBoot).append(channelsChanged)
                .append(proxyVersion)
                .append(mgrVersion)
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE).append(
                "id", id).append("org", org).append("name", name).append(
                        "description", description).toString();
    }

    /**
     * @return Returns the created.
     */
    @Override
    public Date getCreated() {
        return created;
    }

    /**
     * @param createdIn The created to set.
     */
    @Override
    public void setCreated(Date createdIn) {
        this.created = createdIn;
    }

    /**
     * @return Returns the lock.
     */
    public ServerLock getLock() {
        return lock;
    }

    /**
     * @param lockIn The lock to set.
     */
    public void setLock(ServerLock lockIn) {
        this.lock = lockIn;
    }

    /**
     * @return Returns the uuid.
     */
    public ServerUuid getServerUuid() {
        return this.serverUuid;
    }

    /**
     * @param serverUuidIn The uuid to set.
     */
    public void setServerUuid(ServerUuid serverUuidIn) {
        this.serverUuid = serverUuidIn;
    }

    /**
     * Business method to check if the system is considered 'inactive'
     * @return boolean if it hasn't checked in recently.
     */
    public boolean isInactive() {
        Date lastCheckin = this.getLastCheckin();
        long millisInDay = (1000 * 60 * 60 * 24);
        long threshold = SatConfigFactory.getSatConfigLongValue(SatConfigFactory.SYSTEM_CHECKIN_THRESHOLD, 1L);
        Date yesterday = new Timestamp(System.currentTimeMillis() -
                (millisInDay * threshold));
        return lastCheckin.before(yesterday);
    }


    /**
     * Get the Set of Child Channel objects associated with this server.  This
     * is just a convenience method.  Basically the channels associated with this
     * server that are not base channels.
     *
     * @return Set of Child Channels.  null of none found.
     */
    public Set<Channel> getChildChannels() {
        // Make sure we return NULL if none are found
        if (this.getChannels() != null) {
            Set<Channel> retval = new HashSet<>();
            for (Channel c : this.getChannels()) {
                // add non base channels (children)
                // to return set.
                if (!c.isBaseChannel()) {
                    retval.add(c);
                }
            }
            if (retval.isEmpty()) {
                return new HashSet<>();
            }
            return retval;
        }
        return new HashSet<>();
    }

    /**
     * @return The push client for this server.
     */
    public PushClient getPushClient() {
        return pushClient;
    }

    /**
     * @param pushClientIn The push client to be used for this server.
     */
    public void setPushClient(PushClient pushClientIn) {
        this.pushClient = pushClientIn;
    }

    /**
     * Simple check to see if the Server is subscribed to the passed in channel already.
     * @param channelIn to check
     * @return boolean true false if subbed or not.
     */
    public boolean isSubscribed(Channel channelIn) {
        Set<Channel> childChannels = this.channels;
        if (childChannels != null) {
            return childChannels.contains(channelIn);
        }
        return false;
    }

    /**
     * Get the Set of valid addon Entitlements for this server.
     *
     * @return Set of valid addon Entitlement instances for this server
     */
    public Set<Entitlement> getValidAddonEntitlementsForServer() {
        Set<Entitlement> retval = new TreeSet<>();
        for (Entitlement ent : this.getOrg().getValidAddOnEntitlementsForOrg()) {
            if (ent.isAllowedOnServer(this)) {
                retval.add(ent);
            }
        }
        return retval;

    }

    /**
     * @return this list of history events for this server
     */
    public Set<ServerHistoryEvent> getHistory() {
        return history;
    }

    /**
     * Set the history events for this server
     * @param historyIn the List of history events
     */
    public void setHistory(Set<ServerHistoryEvent> historyIn) {
        this.history = historyIn;
    }

    /**
     * @return Returns the packages.
     */
    public Set<InstalledPackage> getPackages() {
        return packages;
    }

    /**
     * @deprecated This function does not behave the way you would expect due to
     * hibernate magic. To change server packages manipulate the result of getPackages
     * instead.
     * @param packagesIn The packages to set.
     */
    @Deprecated
    private void setPackages(Set<InstalledPackage> packagesIn) {
        this.packages = packagesIn;
    }

    /**
     * @return Returns the cobblerId.
     */
    public String getCobblerId() {
        return cobblerId;
    }

    /**
     * @param cobblerIdIn The cobblerId to set.
     */
    public void setCobblerId(String cobblerIdIn) {
        this.cobblerId = cobblerIdIn;
    }

    /**
     * @return Returns the ignoreEntitlementsForMigration.
     */
    public boolean getIgnoreEntitlementsForMigration() {
        return ignoreEntitlementsForMigration;
    }

    /**
     * This method should ONLY be used for system migrations, hence the long method name.
     *
     * This method will set a local flag (i.e. not Hibernate-related) that if set will
     * result in skipping entitlement checking on various methods.
     *
     * @param ignoreIn  Set to true to override entitlement sestings.
     */
    public void setIgnoreEntitlementsForMigration(Boolean ignoreIn) {
        this.ignoreEntitlementsForMigration = ignoreIn;
    }

    /**
     * Get the NetworkInteface with the given name (i.e. eth0)
     * @param ifName the interface name (i.e. eth0)
     * @return the NetworkInterface, otherwise null
     */
    public NetworkInterface getNetworkInterface(String ifName) {
        for (NetworkInterface nic : getNetworkInterfaces()) {
            if (nic.getName().equals(ifName)) {
                return nic;
            }
        }
        return null;
    }

    /**
     * Returns the cobbler object associated to
     * to this server.
     * @param user the user object needed for connection,
     *              enter null if you want to use the
     *              automated connection as provided by
     *              taskomatic.
     * @return the SystemRecord associated to this server
     */
    public SystemRecord getCobblerObject(User user) {
        if (StringUtils.isBlank(getCobblerId())) {
            return null;
        }
        CobblerConnection con;
        if (user == null) {
            con = CobblerXMLRPCHelper.getAutomatedConnection();
        }
        else {
            con = CobblerXMLRPCHelper.getConnection(user);
        }
        return SystemRecord.lookupById(con, getCobblerId());
    }

    /**
     * @param installedProductsIn the installedProducts to set
     */
    public void setInstalledProducts(Set<InstalledProduct> installedProductsIn) {
        this.installedProducts = installedProductsIn;
    }

    /**
     * @return the installedProducts
     */
    public Set<InstalledProduct> getInstalledProducts() {
        return installedProducts;
    }

    /**
     * Return the installed products or null in case of no products found.
     * @return installed products
     */
    public Optional<SUSEProductSet> getInstalledProductSet() {
        if (installedProducts.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new SUSEProductSet(installedProducts));
    }

    /**
     * @return primaryInterface Primary network interface
     */
    public NetworkInterface getPrimaryInterface() {
        return primaryInterface;
    }

    /**
     * @param primaryInterfaceIn Primary network interface to be set
     */
    public void setPrimaryInterface(NetworkInterface primaryInterfaceIn) {
        primaryInterface = primaryInterfaceIn;
        for (NetworkInterface n : networkInterfaces) {
            n.setPrimary(null);
        }
        SystemManager.storeServer(this);
        if (primaryInterface != null) {
            primaryInterface.setPrimary("Y");
        }
    }

    /**
     * @param interfaceName name of the interface
     */
    public void setPrimaryInterfaceWithName(String interfaceName) {
        setPrimaryInterface(findActiveIfaceWithName(interfaceName, false));
    }

    private NetworkInterface lookupForPrimaryInterface() {
        for (NetworkInterface n : networkInterfaces) {
            if (n.getPrimary() != null && n.getPrimary().equals("Y")) {
                return n;
            }
        }
        return null;
    }

    /**
     * @param fqdnName name of the primary FQDN
     */
    public void setPrimaryFQDNWithName(String fqdnName) {
        ServerFQDN newPrimaryFQDN = null;
        for (ServerFQDN fqdn: fqdns) {
            fqdn.setPrimary(false);
            if (fqdn.getName().equals(fqdnName)) {
                newPrimaryFQDN = fqdn;
            }
        }
        SystemManager.storeServer(this);
        if (newPrimaryFQDN != null) {
            newPrimaryFQDN.setPrimary(true);
        }
    }

    /**
     * @return active Set of active interaces without lo
     */
    public Set<NetworkInterface> getActiveNetworkInterfaces() {
        Set<NetworkInterface> active = new HashSet<>();
        for (NetworkInterface n : networkInterfaces) {
            if (!n.isDisabled()) {
                active.add(n);
            }
        }
        return active;
    }

    /**
     * @param interfaceName Name of the interface to be checked
     * @return Returns true if yes, otherwise no
     */
    public Boolean existsActiveInterfaceWithName(String interfaceName) {
        return findActiveIfaceWithName(interfaceName, false) != null;
    }

    /**
     * Update the corresponding {@link ServerInfo} with the current
     * time and increment the checkin counter.
     * If {@link ServerInfo} does not exist it will create it.
     */
    public void updateServerInfo() {
        ServerInfo srvInfo = getServerInfo();
        if (srvInfo == null) {
            srvInfo = new ServerInfo();
            srvInfo.setServer(this);
            setServerInfo(srvInfo);
            srvInfo.setCheckinCounter(0L);
        }

        srvInfo.setCheckin(new Date());
        srvInfo.setCheckinCounter(
                Optional.ofNullable(srvInfo.getCheckinCounter()).orElse(0L) + 1);

    }

    /**
     * Converts this server to a MinionServer if it is one.
     *
     * @return optional of MinionServer
     */
    public Optional<MinionServer> asMinionServer() {
        return Optional.empty();
    }

    /**
     * @return the machine id
     */
    public String getMachineId() {
        return machineId;
    }

    /**
     * @param machineIdIn the machine id
     */
    public void setMachineId(String machineIdIn) {
        this.machineId = machineIdIn;
    }

    /**
     * @return the minion id if the server is a salt minion client, else empty string
     */
    public String getMinionId() {
        return Opt.fold(this.asMinionServer(), () -> "", MinionServer::getMinionId);
    }

    /**
     * Gets the hostname.
     *
     * @return the hostname
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Sets the hostname.
     *
     * @param hostnameIn the new hostname
     */
    public void setHostname(String hostnameIn) {
        hostname = hostnameIn;
    }

    /**
     * Sets the FQDNs
     * @return set of FQDNs for the server
     */
    public Set<ServerFQDN> getFqdns() {
        return fqdns;
    }

    /**
     * Add a FQDN to the list of FQDNS of this server
     * @param nameIn FQDN to be added
     */
    public void addFqdn(String nameIn) {
        fqdns.add(new ServerFQDN(this, nameIn));
    }

    /**
     * Setter for the FQDNs variable
     * @param fqdnsIn the fqdns set to be set
     */
    public void setFqdns(Set<ServerFQDN> fqdnsIn) {
        this.fqdns = fqdnsIn;
    }

    /**
     *
     * @return payg
     */
    public boolean isPayg() {
        return payg;
    }

    /**
     *
     * @param paygIn boolean
     */
    public void setPayg(boolean paygIn) {
        payg = paygIn;
    }

    /**
     * Do not use this method, use getMaintenanceScheduleOpt instead.
     * @return the maintenance schedule
     */
    public MaintenanceSchedule getMaintenanceSchedule() {
        return maintenanceSchedule;
    }

    /**
     * @return the maintenance schedule as optional
     */
    public Optional<MaintenanceSchedule> getMaintenanceScheduleOpt() {
        return Optional.ofNullable(maintenanceSchedule);
    }

    /**
     * Set the Maintenance Schedule
     * @param scheduleIn the schedule
     */
    public void setMaintenanceSchedule(MaintenanceSchedule scheduleIn) {
        maintenanceSchedule = scheduleIn;
    }

    /**
     *
     * @return Returns the org Id.
     */
    public Long getOrgId() {
        return org.getId();
    }

    /**
     * Adds a server group to this server
     * @param serverGroup the server group
     * @return true if the server groups of this server changed as a result of the call
     */
    public boolean addGroup(ServerGroup serverGroup) {
        return this.groups.add(serverGroup);
    }

    /**
     * Removes a server group from this server
     * @param serverGroup the serverGroup
     * @return true if the server groups of this server changed as a result of the call
     */
    public boolean removeGroup(ServerGroup serverGroup) {
        return this.groups.remove(serverGroup);
    }

    /**
     * Retrieves the server group that matches the passed entitlement, if this server is a member of.
     * @param ent the entitlement
     * @return the server group
     */
    public Optional<EntitlementServerGroup> findServerGroupByEntitlement(Entitlement ent) {
        String entitlementLabel = ent.getLabel();
        return this.getEntitledGroups().stream().filter(g -> g.getGroupType().getLabel().equals(entitlementLabel))
                .findFirst();
    }

    /**
     * Return the channel hostname for this server
     *
     * If case this server is directly connected to the SUMA Server, this method returns the
     * this server's hostname. If, otherwise, the client is connected to the SUMA Server via a Proxy, this
     * method returns the hostname of the first Proxy the client is connected to
     * @return the channel hostname
     */
    public String getChannelHost() {
        return this.getFirstServerPath().map(ServerPath::getHostname)
                .orElseGet(() -> ConfigDefaults.get().getCobblerHost());
    }

    public PackageType getPackageType() {
        return getServerArch().getArchType().getPackageType();
    }

    /**
     * Return <code>true</code> if OS on this system supports OS Image building,
     * <code>false</code> otherwise.
     *
     * @return <code>true</code> if OS supports OS Image building
     */
    public boolean doesOsSupportsOSImageBuilding() {
        return isSLES11() || isSLES12() || isSLES15() || isLeap15();
    }

    /**
     * Return <code>true</code> if OS on this system supports Containerization,
     * <code>false</code> otherwise.
     * <p>
     * Note: For SLES, we are only checking if it's not 10 nor 11.
     * Older than SLES 10 are not being checked.
     * </p>
     *
     * @return <code>true</code> if OS supports Containerization
     */
    public boolean doesOsSupportsContainerization() {
        return !isSLES10() && !isSLES11();
    }

    /**
     * Return <code>true</code> if OS on this system supports Transactional Update,
     * <code>false</code> otherwise.
     *
     * @return <code>true</code> if OS supports Transactional Update
     */
    public boolean doesOsSupportsTransactionalUpdate() {
        return isSLEMicro() || isLeapMicro() || isopenSUSEMicroOS();
    }

    /**
     * Return <code>true</code> if OS on supports monitoring
     * <code>false</code> otherwise.
     *
     * @return <code>true</code> if OS supports monitoring
     */
    public boolean doesOsSupportsMonitoring() {
        return isSLES12() || isSLES15() || isLeap15() || isUbuntu1804() || isUbuntu2004() || isUbuntu2204() ||
                isRedHat6() || isRedHat7() || isRedHat8() || isAlibaba2() || isAmazon2() || isRocky8() ||
                isRocky9() || isDebian11() || isDebian10();
    }

    /**
     * Return <code>true</code> if OS supports Program Temporary Fixes (PTFs)
     *
     * @return <code>true</code> if OS supports PTF uninstallation
     */
    public boolean doesOsSupportPtf() {
        return ServerConstants.SLES.equals(getOs());
    }

    /**
     * @return true if the installer type is of SLES 10
     */
    boolean isSLES10() {
        return ServerConstants.SLES.equals(getOs()) && getRelease().startsWith("10");
    }

    /**
     * @return true if the installer type is of SLES 11
     */
    boolean isSLES12() {
        return ServerConstants.SLES.equals(getOs()) && getRelease().startsWith("12");
    }

    /**
     * @return true if the installer type is of SLES 11
     */
    boolean isSLES11() {
        return ServerConstants.SLES.equals(getOs()) && getRelease().startsWith("11");
    }

    /**
     * @return true if the installer type is of SLE Micro
     */
    boolean isSLEMicro() {
        return ServerConstants.SLEMICRO.equals(getOs());
    }

    /**
     * @return true if the installer type is of SLES 15
     */
    boolean isSLES15() {
        return ServerConstants.SLES.equals(getOs()) && getRelease().startsWith("15");
    }

    boolean isLeap15() {
        return ServerConstants.LEAP.equalsIgnoreCase(getOs()) && getRelease().startsWith("15");
    }

    /**
     * @return true if the installer type is of openSUSE Leap Micro
     */
    boolean isLeapMicro() {
        return ServerConstants.LEAPMICRO.equals(getOs());
    }

    /**
     * @return true if the installer type is of openSUSE MicroOS
     */
    boolean isopenSUSEMicroOS() {
        return ServerConstants.OPENSUSEMICROOS.equals(getOs());
    }

    boolean isUbuntu1804() {
        return ServerConstants.UBUNTU.equals(getOs()) && getRelease().equals("18.04");
    }

    boolean isUbuntu2004() {
        return ServerConstants.UBUNTU.equals(getOs()) && getRelease().equals("20.04");
    }

    boolean isUbuntu2204() {
        return ServerConstants.UBUNTU.equals(getOs()) && getRelease().equals("22.04");
    }

    boolean isDebian11() {
        return ServerConstants.DEBIAN.equals(getOs()) && getRelease().equals("11");
    }

    boolean isDebian10() {
        return ServerConstants.DEBIAN.equals(getOs()) && getRelease().equals("10");
    }

    /**
     * This is supposed to cover all RedHat flavors (incl. RHEL, RES and CentOS Linux)
     */
    boolean isRedHat6() {
        return ServerConstants.REDHAT.equals(getOsFamily()) && getRelease().equals("6");
    }

    boolean isRedHat7() {
        return ServerConstants.REDHAT.equals(getOsFamily()) && getRelease().equals("7");
    }

    boolean isRedHat8() {
        return ServerConstants.REDHAT.equals(getOsFamily()) && getRelease().equals("8");
    }

    boolean isRedHat9() {
        return ServerConstants.REDHAT.equals(getOsFamily()) && getRelease().equals("9");
    }

    boolean isAlibaba2() {
        return ServerConstants.ALIBABA.equals(getOs());
    }

    boolean isAmazon2() {
        return ServerConstants.AMAZON.equals(getOsFamily()) && getRelease().equals("2");
    }

    boolean isRocky8() {
        return ServerConstants.ROCKY.equals(getOs()) && getRelease().startsWith("8.");
    }

    boolean isRocky9() {
        return ServerConstants.ROCKY.equals(getOs()) && getRelease().startsWith("9.");
    }

    /**
     * Getter for os family
     *
     * @return String to get
     */
    public String getOsFamily() {
        return this.osFamily;
    }

    /**
     * Setter for os family
     *
     * @param osFamilyIn to set
     */
    public void setOsFamily(String osFamilyIn) {
        this.osFamily = osFamilyIn;
    }

    /**
     * Getter for CPE (Common Platform Enumeration)
     *
     * @return cpe
     * */
    public String getCpe() {
        return cpe;
    }

    /**
     * Setter for CPE (Common Platform Enumeration)
     *
     * @param cpeIn to set
     * */
    public void setCpe(String cpeIn) {
        this.cpe = cpeIn;
    }
}
