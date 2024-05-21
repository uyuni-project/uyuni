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
package com.redhat.rhn.manager.kickstart.cobbler;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.server.NetworkInterface;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import com.redhat.rhn.domain.token.Token;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.kickstart.KickstartFormatter;
import com.redhat.rhn.manager.kickstart.KickstartUrlHelper;
import com.redhat.rhn.manager.token.ActivationKeyManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cobbler.Network;
import org.cobbler.Profile;
import org.cobbler.SystemRecord;
import org.cobbler.XmlRpcException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Create a System inside Cobbler via its XML-RPC API.
 */
public class CobblerSystemCreateCommand extends CobblerCommand {

    private static final Logger LOG = LogManager.getLogger(CobblerSystemCreateCommand.class);
    private Action scheduledAction;
    private final Server server;
    private final String serverName;
    private final Long orgId;
    private String mediaPath;
    private String profileName;
    private String activationKeys;
    private String kickstartHost;
    private String kernelOptions;
    private String postKernelOptions;
    private String comment;
    protected String networkInterface;
    protected boolean isDhcp;
    private boolean useIpv6Gateway;
    private String ksDistro;
    private boolean setupBridge;
    private String bridgeName;
    private List<String> bridgeSlaves;
    private String bridgeOptions;
    private String bridgeAddress;
    private String bridgeNetmask;
    private String bridgeGateway;
    private boolean isBridgeDhcp;
    private KickstartData ksData;

    /**
     * Private base constructor that contains shared logic between the different constructors.
     *
     * @param userIn               The user that requests the installation.
     * @param serverIn             The server that is requested to be created inside Cobbler.
     * @param serverNameIn         The name of the server.
     * @param orgIdIn              The organisation the server belongs to.
     * @param cobblerProfileNameIn The name of the Cobbler Profile that is used for installation.
     */
    private CobblerSystemCreateCommand(
            User userIn,
            Server serverIn,
            String serverNameIn,
            Long orgIdIn,
            String cobblerProfileNameIn
    ) {
        super(userIn);
        this.serverName = serverNameIn;
        this.orgId = orgIdIn;
        this.server = serverIn;
        this.profileName = cobblerProfileNameIn;
    }

    /**
     * Constructor that persists the Cobbler ID in the database after creating it via {@link #store()}.
     *
     * @param userIn   The user that creates the system.
     * @param ksDataIn The Kickstart data that should be used for creating the Cobbler System.
     * @param serverIn The Server that should be installed.
     */
    public CobblerSystemCreateCommand(User userIn, KickstartData ksDataIn, Server serverIn) {
        this(
            userIn,
            serverIn,
            serverIn.getName(),
            userIn.getOrg().getId(),
            ksDataIn.getCobblerObject(userIn).getName()
        );
        this.ksData = ksDataIn;
    }

    /**
     * Constructor
     *
     * @param userIn           who is requesting the sync
     * @param serverIn         profile we want to create in cobbler
     * @param ksDataIn         profile to associate with the server.
     * @param mediaPathIn      mediaPath to override in the server profile.
     * @param activationKeysIn to add to the system record.  Used when the system
     *                         re-registers to Spacewalk
     */
    public CobblerSystemCreateCommand(User userIn, Server serverIn,
                                      KickstartData ksDataIn, String mediaPathIn, String activationKeysIn) {
        this(userIn, serverIn, serverIn.getName(), serverIn.getOrgId(), "");
        this.mediaPath = mediaPathIn;
        if (ksDataIn == null) {
            throw new NullPointerException("ksDataIn cant be null");
        }
        this.profileName = ksDataIn.getCobblerObject(user).getName();
        this.activationKeys = activationKeysIn;
        this.ksData = ksDataIn;
    }

    /**
     * Constructor to be used for a system outside the context
     * of actually kickstarting it to a specific profile.
     *
     * @param userIn             the user creating the system
     * @param serverIn           profile we want to create in cobbler
     * @param cobblerProfileName the name of the cobbler profile
     *                           to associate with system
     * @param ksDataIn           the kickstart data to associate the system with
     */
    public CobblerSystemCreateCommand(User userIn, Server serverIn, String cobblerProfileName,
                                      KickstartData ksDataIn) {
        this(userIn, serverIn, serverIn.getName(), serverIn.getOrgId(), cobblerProfileName);
        this.mediaPath = null;
        String note = "Reactivation key for " + serverName + ".";
        ActivationKey key = ActivationKeyManager.getInstance().
                createNewReActivationKey(UserFactory.findRandomOrgAdmin(
                        serverIn.getOrg()), serverIn, note);
        key.setUsageLimit(1L);
        LOG.debug("created reactivation key: {}", key.getKey());
        this.ksData = ksDataIn;
        this.activationKeys = generateActivationKeys(key.getKey());
    }

    /**
     * Constructor to be used to create a new system with a cobbler profile.
     *
     * @param userIn             the user creating the system
     * @param cobblerProfileName the name of the cobbler profile
     *                           to associate with system
     * @param ksDataIn           the kickstart data to associate the system with
     * @param serverNameIn       the name of the system to create
     * @param orgIdIn            the organization ID the system will belong to
     */
    public CobblerSystemCreateCommand(User userIn, String cobblerProfileName, KickstartData ksDataIn,
                                      String serverNameIn, Long orgIdIn) {
        this(userIn, null, serverNameIn, orgIdIn, cobblerProfileName);
        this.mediaPath = null;
        this.ksData = ksDataIn;
        this.activationKeys = generateActivationKeys("");
    }


    /**
     * Constructor
     *
     * @param userIn   who is requesting the sync
     * @param serverIn profile we want to create in cobbler
     * @param nameIn   profile name to associate with server.
     */
    public CobblerSystemCreateCommand(User userIn, Server serverIn,
                                      String nameIn) {
        this(userIn, serverIn, serverIn.getName(), serverIn.getOrgId(), nameIn);
    }

    /**
     * Generate the activate key string that can be passed to Cobbler's "redhat_management_keys" string.
     *
     * @param keyIn In case of reactivation of a system you can pass this as a seed to this method.
     * @return The activation key or keys. In case no kickstart data is set, this method returns an empty string.
     */
    private String generateActivationKeys(String keyIn) {
        StringBuilder keys = new StringBuilder(keyIn);
        if (this.ksData == null) {
            return "";
        }
        for (Token token : this.ksData.getDefaultRegTokens()) {
            ActivationKey keyTmp = ActivationKeyFactory.lookupByToken(token);
            if (keyTmp != null) {
                if (!keys.toString().isBlank()) {
                    keys.append(",");
                }
                keys.append(keyTmp.getKey());
            }
        }
        return keys.toString();
    }

    /**
     * @param dhcp               true if the network type is dhcp
     * @param networkInterfaceIn The name of the network interface
     * @param useIpv6GatewayIn   whether to use ipv6 gateway
     * @param ksDistroIn         distro to be provisioned
     */
    public void setNetworkInfo(boolean dhcp, String networkInterfaceIn,
                               boolean useIpv6GatewayIn, String ksDistroIn) {
        isDhcp = dhcp;
        networkInterface = networkInterfaceIn;
        useIpv6Gateway = useIpv6GatewayIn;
        ksDistro = ksDistroIn;
    }

    /**
     * @param doBridge       boolean, whether or not to set up a bridge post-install
     * @param name           string, name of the bridge
     * @param slaves         string array, nics to use as slaves
     * @param options        string, bridge options
     * @param isBridgeDhcpIn boolean, if the bridge will use dhcp to obtain an ip address
     * @param address        string, ip address for the bridge (if isDhcp is false)
     * @param netmask        string, netmask for the bridge (if isDhcp is false)
     * @param gateway        string, gateway for the bridge (if isDhcp is false)
     */
    public void setBridgeInfo(boolean doBridge, String name,
                              List<String> slaves, String options, boolean isBridgeDhcpIn,
                              String address, String netmask, String gateway) {
        setupBridge = doBridge;
        bridgeName = name;
        bridgeSlaves = slaves;
        bridgeOptions = options;
        isBridgeDhcp = isBridgeDhcpIn;
        bridgeAddress = address;
        bridgeNetmask = netmask;
        bridgeGateway = gateway;
    }

    /**
     * Store the System to cobbler
     *
     * @return ValidatorError if the store failed.
     */
    @Override
    public ValidatorError store() {
        return store(true);
    }

    /**
     * Store the System to cobbler
     *
     * @param saveCobblerId false if CobblerVirtualSystemCommand is calling, true otherwise
     * @return ValidatorError if the store failed.
     */
    public ValidatorError store(boolean saveCobblerId) {
        Profile profile = Profile.lookupByName(getCobblerConnection(), profileName);
        SystemRecord rec = getCobblerSystem(profile);

        if (server != null) {
            try {
                processNetworkInterfaces(rec, server);
            }
            catch (XmlRpcException e) {
                if (e.getCause() != null && e.getCause().getMessage() != null &&
                        e.getCause().getMessage().contains("IP address duplicated")) {
                    return new ValidatorError(
                            "frontend.actions.systems.virt.duplicateipaddressvalue",
                            serverName);
                }
                throw e;
            }
        }
        rec.enableNetboot(true);
        rec.setProfile(profile);
        rec.setIpv6Autoconfiguration(isDhcp);

        processRedHatManagementKeys(rec, profile);
        rec.setKsMeta(generateKsMeta(rec));
        processKernelOptions(rec.getProfile());
        processOptionalProperties(rec);

        try {
            rec.save();
        }
        catch (XmlRpcException e) {
            if (e.getCause() != null && e.getCause().getMessage() != null &&
                    e.getCause().getMessage().contains("IP address duplicated")) {
                return new ValidatorError(
                        "frontend.actions.systems.virt.duplicateipaddressvalue",
                        serverName);
            }
            throw e;
        }

        /*
         * This is a band-aid for the problem revealed in bug 846221. However
         * the real fix involves creating a new System for the virtual guest
         * instead of re-using the host System object, and I am unsure of what
         * effects that would have. The System object is used when creating
         * reActivation keys and setting up the cobbler SystemRecord network
         * info among other things. No bugs have been reported in those areas
         * yet, so I don't want to change something that has the potential to
         * break a lot of things.
         */
        if (saveCobblerId && server != null) {
            server.setCobblerId(rec.getId());
        }
        return null;
    }

    private void processRedHatManagementKeys(SystemRecord rec, Profile profile) {
        if (server != null) {
            if (this.activationKeys == null || this.activationKeys.isEmpty()) {
                LOG.error("This cobbler profile ({}) does not " +
                        "have a redhat_management_key set ", profile.getId());
            }
            else {
                rec.setRedHatManagementKey(Optional.of(activationKeys));
            }
        }
    }

    private SystemRecord getCobblerSystem(Profile cobblerProfile) {
        SystemRecord rec = null;
        // First lookup by MAC addr
        if (server != null) {
            rec = lookupExisting(server);
            if (rec == null) {
                // Next try by name
                rec = SystemRecord.lookupByName(getCobblerConnection(user),
                        getCobblerSystemRecordName());
            }
        }

        // Else, lets make a new system
        if (rec == null) {
            rec = SystemRecord.create(getCobblerConnection(),
                    getCobblerSystemRecordName(), cobblerProfile);
        }
        return rec;
    }

    private Optional<Map<String, Object>> generateKsMeta(SystemRecord rec) {
        // Setup the kickstart metadata so the URLs and activation key are setup
        Map<String, Object> ksmeta = rec.getKsMeta().orElseGet(HashMap::new);

        if (!StringUtils.isBlank(mediaPath)) {
            ksmeta.put(KickstartUrlHelper.COBBLER_MEDIA_VARIABLE,
                    this.mediaPath);
        }
        if (!StringUtils.isBlank(getKickstartHost())) {
            ksmeta.put(SystemRecord.REDHAT_MGMT_SERVER,
                    getKickstartHost());
        }
        ksmeta.remove(KickstartFormatter.STATIC_NETWORK_VAR);
        ksmeta.put(KickstartFormatter.USE_IPV6_GATEWAY,
                this.useIpv6Gateway ? "true" : "false");
        if (this.ksDistro != null) {
            ksmeta.put(KickstartFormatter.KS_DISTRO, this.ksDistro);
        }
        return Optional.of(ksmeta);
    }

    private void processKernelOptions(Profile recProfile) {
        if (recProfile != null && "suse".equals(recProfile.getDistro().getBreed()) && kernelOptions != null &&
                kickstartHost != null && mediaPath != null) {
            if (!kernelOptions.contains("install=")) {
                kernelOptions = String.format("%s install=http://%s%s", kernelOptions, kickstartHost, mediaPath);
            }

            Map<String, Object> resKopts = recProfile.getResolvedKernelOptions();
            boolean selfUpdateDisabled = resKopts.getOrDefault("self_update", "Enabled").equals("0");
            if (!(selfUpdateDisabled || kernelOptions.contains("self_update=") || ksData == null)) {
                Optional<Channel> installerUpdated = ksData.getTree().getChannel()
                        .getAccessibleChildrenFor(user)
                        .stream()
                        .filter(Channel::isInstallerUpdates)
                        .findFirst();
                kernelOptions = installerUpdated.map(
                        channel -> String.format(
                                "%s self_update=http://%s/ks/dist/child/%s/%s",
                                kernelOptions,
                                kickstartHost,
                                channel.getLabel(),
                                ksData.getTree().getLabel()
                        )).orElseGet(() -> kernelOptions + " self_update=0");
            }
        }
    }

    private void processOptionalProperties(SystemRecord rec) {
        if (!StringUtils.isBlank(getKickstartHost())) {
            rec.setServer(Optional.of(getKickstartHost()));
        }
        if (server != null && server.getHostname() != null) {
            rec.setHostName(server.getHostname());
        }
        else if (serverName != null) {
            rec.setHostName(serverName);
        }
        if (kernelOptions != null) {
            rec.setKernelOptions(Optional.of(kernelOptions));
        }
        if (postKernelOptions != null) {
            rec.setKernelOptionsPost(Optional.of(postKernelOptions));
        }
        if (comment != null) {
            rec.setComment(comment);
        }
    }

    /**
     * Get the cobbler system record name for a system
     *
     * @return String name of cobbler system record
     */
    public String getCobblerSystemRecordName() {
        return CobblerSystemCreateCommand.getCobblerSystemRecordName(serverName, orgId);
    }

    /**
     * Get the cobbler system record name for a system
     *
     * @param serverNameIn the name of the server
     * @param orgIdIn      the ID of the organization the server is in
     * @return String name of cobbler system record
     */
    public static String getCobblerSystemRecordName(String serverNameIn, Long orgIdIn) {
        String sep = ConfigDefaults.get().getCobblerNameSeparator();
        String name = serverNameIn.replace(' ', '_');
        name = name.replace(' ', '_').replaceAll("[^a-zA-Z0-9_\\-.]", "");
        return name + sep + orgIdIn;
    }

    protected void processNetworkInterfaces(SystemRecord rec,
                                            Server serverIn) {
        List<Network> nics = new LinkedList<>();
        if (serverIn.getNetworkInterfaces() != null) {
            for (NetworkInterface n : serverIn.getNetworkInterfaces()) {
                processSingleNetworkInterface(n).ifPresent(nics::add);
            }
            if (setupBridge) {
                nics.add(setupBridgeInterface(rec));
            }
        }
        rec.setNetworkInterfaces(nics);
    }

    private Optional<Network> processSingleNetworkInterface(NetworkInterface networkInterfaceIn) {
        // don't create a physical network device for a bond
        if (!networkInterfaceIn.isVirtBridge() && !networkInterfaceIn.isBond()) {
            if (networkInterfaceIn.isPublic()) {
                return Optional.of(setupPublicInterface(networkInterfaceIn));
            }
            else if (networkInterfaceIn.isMacValid() && networkInterfaceIn.getIPv4Addresses().isEmpty()) {
                return Optional.of(setupInterface(networkInterfaceIn));
            }
        }
        else if (setupBridge && bridgeSlaves.contains(networkInterfaceIn.getName())) {
            return Optional.of(setupBridgeSlaves(networkInterfaceIn));
        }
        return Optional.empty();
    }

    private Network setupPublicInterface(NetworkInterface networkInterfaceIn) {
        Network net = new Network(getCobblerConnection(),
                networkInterfaceIn.getName());
        if (!networkInterfaceIn.getIPv4Addresses().isEmpty()) {
            net.setIpAddress(networkInterfaceIn.getIPv4Addresses().get(0).getAddress());
            net.setNetmask(networkInterfaceIn.getIPv4Addresses().get(0).getNetmask());
        }
        net.setMacAddress(networkInterfaceIn.getHwaddr());

        if (!StringUtils.isBlank(networkInterface) &&
                networkInterfaceIn.getName().equals(networkInterface)) {
            net.setStaticNetwork(!isDhcp);
        }

        ArrayList<String> ipv6Addresses = networkInterfaceIn.getGlobalIpv6Addresses();
        if (!ipv6Addresses.isEmpty()) {
            net.setIpv6Address(ipv6Addresses.get(0));
            ipv6Addresses.remove(0);
        }
        if (!ipv6Addresses.isEmpty()) {
            net.setIpv6Secondaries(ipv6Addresses);
        }
        if (setupBridge && bridgeSlaves.contains(networkInterfaceIn.getName())) {
            net.makeBondingSlave();
            net.setBondingMaster(bridgeName);
        }
        return net;
    }

    private Network setupInterface(NetworkInterface networkInterfaceIn) {
        // Method name not optimal, however a precise name is not easy in this scenario.
        Network net = new Network(getCobblerConnection(),
                networkInterfaceIn.getName());
        net.setMacAddress(networkInterfaceIn.getHwaddr());
        if (setupBridge && bridgeSlaves.contains(networkInterfaceIn.getName())) {
            net.makeBondingSlave();
            net.setBondingMaster(bridgeName);
        }
        return net;
    }

    private Network setupBridgeSlaves(NetworkInterface networkInterfaceIn) {
        Network net = new Network(getCobblerConnection(),
                networkInterfaceIn.getName());
        net.setMacAddress(networkInterfaceIn.getHwaddr());
        net.makeBondingSlave();
        net.setBondingMaster(bridgeName);
        return net;
    }

    private Network setupBridgeInterface(SystemRecord systemRecordIn) {
        Network net = new Network(getCobblerConnection(), bridgeName);
        net.makeBondingMaster();
        net.setBondingOptions(bridgeOptions);
        net.setStaticNetwork(!isBridgeDhcp);
        if (!isBridgeDhcp) {
            net.setNetmask(bridgeNetmask);
            net.setIpAddress(bridgeAddress);
            systemRecordIn.setGateway(bridgeGateway);
        }
        return net;
    }

    /**
     * @return the system
     */
    public Server getServer() {
        return server;
    }


    /**
     * @return Returns the kickstartHost.
     */
    public String getKickstartHost() {
        return kickstartHost;
    }


    /**
     * @param kickstartHostIn The kickstartHost to set.
     */
    public void setKickstartHost(String kickstartHostIn) {
        this.kickstartHost = kickstartHostIn;
    }

    /**
     * @param kernelOptionsIn The kernelOptions to set.
     */
    public void setKernelOptions(String kernelOptionsIn) {
        this.kernelOptions = kernelOptionsIn;
    }

    /**
     * @param postKernelOptionsIn The postKernelOptions to set.
     */
    public void setPostKernelOptions(String postKernelOptionsIn) {
        this.postKernelOptions = postKernelOptionsIn;
    }

    /**
     * Set the scheduled action associated to this command.
     *
     * @param kickstartAction ks action associated to this command
     */
    public void setScheduledAction(Action kickstartAction) {
        scheduledAction = kickstartAction;
    }

    protected Action getScheduledAction() {
        return scheduledAction;
    }

    /**
     * Setter for the comment.
     *
     * @param commentIn the comment
     */
    public void setComment(String commentIn) {
        this.comment = commentIn;
    }

    /**
     * Getter for the comment.
     *
     * @return the comment
     */
    public String getComment() {
        return this.comment;
    }

    /**
     * @return the organization Id
     */
    public Long getOrgId() {
        return orgId;
    }

    /**
     * @return the KickstartData
     */
    public KickstartData getKsData() {
        return ksData;
    }
}
