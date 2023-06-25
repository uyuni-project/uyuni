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
import com.redhat.rhn.manager.satellite.CobblerSyncCommand;
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
 *
 * Login to Cobbler's XMLRPC API and get a token
 */
public class CobblerSystemCreateCommand extends CobblerCommand {

    private static Logger log = LogManager.getLogger(CobblerSystemCreateCommand.class);
    private Action scheduledAction;
    private final Server server;
    private String serverName;
    private Long orgId;
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
     * @param dhcp true if the network type is dhcp
     * @param networkInterfaceIn The name of the network interface
     * @param useIpv6GatewayIn whether to use ipv6 gateway
     * @param ksDistroIn distro to be provisioned
     */
    public void setNetworkInfo(boolean dhcp, String networkInterfaceIn,
            boolean useIpv6GatewayIn, String ksDistroIn) {
        isDhcp = dhcp;
        networkInterface = networkInterfaceIn;
        useIpv6Gateway = useIpv6GatewayIn;
        ksDistro = ksDistroIn;
    }

    /**
     * @param doBridge boolean, whether or not to set up a bridge post-install
     * @param name string, name of the bridge
     * @param slaves string array, nics to use as slaves
     * @param options string, bridge options
     * @param isBridgeDhcpIn boolean, if the bridge will use dhcp to obtain an ip address
     * @param address string, ip address for the bridge (if isDhcp is false)
     * @param netmask string, netmask for the bridge (if isDhcp is false)
     * @param gateway string, gateway for the bridge (if isDhcp is false)
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
     * Constructor
     * @param userIn who is requesting the sync
     * @param serverIn profile we want to create in cobbler
     * @param ksDataIn profile to associate with with server.
     * @param mediaPathIn mediaPath to override in the server profile.
     * @param activationKeysIn to add to the system record.  Used when the system
     * re-registers to Spacewalk
     */
    public CobblerSystemCreateCommand(User userIn, Server serverIn,
            KickstartData ksDataIn, String mediaPathIn, String activationKeysIn) {
        super(userIn);
        this.server = serverIn;
        this.serverName = serverIn.getName();
        this.orgId = serverIn.getOrgId();
        this.mediaPath = mediaPathIn;
        if (ksDataIn != null) {
            profileName = ksDataIn.getCobblerObject(user).getName();
        }
        else {
            throw new NullPointerException("ksDataIn cant be null");
        }
        this.activationKeys = activationKeysIn;
        this.ksData = ksDataIn;
    }

    /**
     * Constructor to be used for a system outside tthe context
     * of actually kickstarting it to a specific profile.
     *
     * @param userIn             who is requesting the sync
     * @param serverIn           profile we want to create in cobbler
     * @param cobblerProfileName the name of the cobbler profile
     *                           to associate with system
     * @param ksDataIn           the kickstart data to associate the system with
     */
    public CobblerSystemCreateCommand(User userIn, Server serverIn, String cobblerProfileName,
                                      KickstartData ksDataIn) {
        super(userIn);
        this.server = serverIn;
        this.serverName = serverIn.getName();
        this.orgId = serverIn.getOrgId();
        this.mediaPath = null;
        this.profileName = cobblerProfileName;
        String keys = "";
        String note = "Reactivation key for " + serverName + ".";
        ActivationKey key = ActivationKeyManager.getInstance().
                createNewReActivationKey(UserFactory.findRandomOrgAdmin(
                        server.getOrg()), server, note);
        key.setUsageLimit(1L);
        log.debug("created reactivation key: {}", key.getKey());
        keys = key.getKey();
        this.ksData = ksDataIn;
        if (this.ksData != null) {
            for (Token token : this.ksData.getDefaultRegTokens()) {
                ActivationKey keyTmp = ActivationKeyFactory.lookupByToken(token);
                if (keyTmp != null) {
                    keys += "," + keyTmp.getKey();
                }
            }
        }
        this.activationKeys = keys;
    }

    /**
     * Constructor to be used to create a new system with a cobbler profile.
     *
     * @param userIn the user creating the system
     * @param cobblerProfileName the name of the cobbler profile
     * to associate with system
     * @param ksDataIn the kickstart data to associate the system with
     * @param serverNameIn the name of the system to create
     * @param orgIdIn the organization ID the system will belong to
     */
    public CobblerSystemCreateCommand(User userIn, String cobblerProfileName, KickstartData ksDataIn,
            String serverNameIn, Long orgIdIn) {
        super(userIn);
        this.server = null;
        this.serverName = serverNameIn;
        this.orgId = orgIdIn;
        this.mediaPath = null;
        this.profileName = cobblerProfileName;
        String keys = "";
        this.ksData = ksDataIn;
        if (this.ksData != null) {
            for (Token token : this.ksData.getDefaultRegTokens()) {
                ActivationKey keyTmp = ActivationKeyFactory.lookupByToken(token);
                if (keyTmp != null) {
                    if (!keys.isBlank()) {
                        keys += ",";
                    }
                    keys += keyTmp.getKey();
                }
            }
        }
        this.activationKeys = keys;
    }


    /**
     * Constructor
     * @param userIn who is requesting the sync
     * @param serverIn profile we want to create in cobbler
     * @param nameIn profile name to associate with with server.
     */
    public CobblerSystemCreateCommand(User userIn, Server serverIn,
            String nameIn) {
        super(userIn);
        this.server = serverIn;
        this.serverName = serverIn.getName();
        this.orgId = serverIn.getOrgId();
        profileName = nameIn;
    }

    /**
     * Store the System to cobbler
     * @return ValidatorError if the store failed.
     */
    @Override
    public ValidatorError store() {
        return store(true);
    }

    /**
     * Store the System to cobbler
     * @param saveCobblerId false if CobblerVirtualSystemCommand is calling, true otherwise
     * @return ValidatorError if the store failed.
     */
    public ValidatorError store(boolean saveCobblerId) {
        Profile profile = Profile.lookupByName(getCobblerConnection(), profileName);
        // First lookup by MAC addr
        SystemRecord rec = null;
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
                    getCobblerSystemRecordName(), profile);
        }
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

        if (isDhcp) {
            rec.setIpv6Autoconfiguration(true);
        }
        else {
            rec.setIpv6Autoconfiguration(false);
        }

        if (server != null) {
            if (this.activationKeys == null || this.activationKeys.isEmpty()) {
                log.error("This cobbler profile does not " +
                        "have a redhat_management_key set ");
            }
            else {
                rec.setRedHatManagementKey(Optional.of(activationKeys));
            }
        }
        if (!StringUtils.isBlank(getKickstartHost())) {
            rec.setServer(Optional.of(getKickstartHost()));
        }
        else {
            rec.setServer(Optional.empty());
        }

        // Setup the kickstart metadata so the URLs and activation key are setup
        Map<String, Object> ksmeta;
        if (rec.getKsMeta().isEmpty()) {
            ksmeta = new HashMap<>();
        }
        else {
            ksmeta = rec.getKsMeta().get();
        }

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
        rec.setKsMeta(Optional.of(ksmeta));
        Profile recProfile = rec.getProfile();
        if (recProfile != null && "suse".equals(recProfile.getDistro().getBreed())) {
            if (kernelOptions != null && kickstartHost != null && mediaPath != null) {
                if (!kernelOptions.contains("install=")) {
                    kernelOptions = kernelOptions + " install=http://" + kickstartHost +
                            mediaPath;
                }

                Map<String, Object> resKopts = recProfile.getResolvedKernelOptions();
                boolean selfUpdateDisabled = false;
                if (resKopts.getOrDefault("self_update", "Enabled").equals("0")) {
                    selfUpdateDisabled = true;
                }
                if (!(selfUpdateDisabled || kernelOptions.contains("self_update=") || ksData == null)) {
                    Optional<Channel> installerUpdated = ksData.getTree().getChannel()
                            .getAccessibleChildrenFor(user)
                            .stream()
                            .filter(Channel::isInstallerUpdates)
                            .findFirst();
                    if (installerUpdated.isPresent()) {
                        kernelOptions = kernelOptions + " self_update=http://" + kickstartHost + "/ks/dist/child/" +
                                installerUpdated.get().getLabel() + "/" + ksData.getTree().getLabel();
                    }
                    else {
                        kernelOptions = kernelOptions + " self_update=0";
                    }
                }
            }
        }

        if (server != null && server.getHostname() != null) {
            rec.setHostName(getServer().getHostname());
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
        // The comment is optional
        if (comment != null) {
            rec.setComment(comment);
        }
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
        return new CobblerSyncCommand(user).store();
    }

    /**
     * Get the cobbler system record name for a system
     * @return String name of cobbler system record
     */
    public String getCobblerSystemRecordName() {
        return CobblerSystemCreateCommand.getCobblerSystemRecordName(serverName, orgId);
    }

    /**
     * Get the cobbler system record name for a system
     * @param serverNameIn the name of the server
     * @param orgIdIn the ID of the organization the server is in
     * @return String name of cobbler system record
     */
    public static String getCobblerSystemRecordName(String serverNameIn, Long orgIdIn) {
        String sep = ConfigDefaults.get().getCobblerNameSeparator();
        String name = serverNameIn.replace(' ', '_');
        name = name.replace(' ', '_').replaceAll("[^a-zA-Z0-9_\\-\\.]", "");
        return name + sep + orgIdIn;
    }

    protected void processNetworkInterfaces(SystemRecord rec,
            Server serverIn) {
        List<Network> nics = new LinkedList<>();
        if (serverIn.getNetworkInterfaces() != null) {
            for (NetworkInterface n : serverIn.getNetworkInterfaces()) {
                // don't create a physical network device for a bond
                if (!n.isVirtBridge() && !n.isBond()) {
                    if (n.isPublic()) {
                        Network net = new Network(getCobblerConnection(),
                                n.getName());
                        if (!n.getIPv4Addresses().isEmpty()) {
                            net.setIpAddress(n.getIPv4Addresses().get(0).getAddress());
                            net.setNetmask(n.getIPv4Addresses().get(0).getNetmask());
                        }
                        net.setMacAddress(n.getHwaddr());

                        if (!StringUtils.isBlank(networkInterface) && n.getName().equals(networkInterface)) {
                            net.setStaticNetwork(!isDhcp);
                        }

                        ArrayList<String> ipv6Addresses = n.getGlobalIpv6Addresses();
                        if (!ipv6Addresses.isEmpty()) {
                            net.setIpv6Address(ipv6Addresses.get(0));
                            ipv6Addresses.remove(0);
                        }
                        if (!ipv6Addresses.isEmpty()) {
                            net.setIpv6Secondaries(ipv6Addresses);
                        }
                        if (setupBridge && bridgeSlaves.contains(n.getName())) {
                            net.makeBondingSlave();
                            net.setBondingMaster(bridgeName);
                        }

                        nics.add(net);
                    }
                    else if (n.isMacValid() && n.getIPv4Addresses().isEmpty()) {
                        Network net = new Network(getCobblerConnection(),
                                n.getName());
                        net.setMacAddress(n.getHwaddr());
                        if (setupBridge && bridgeSlaves.contains(n.getName())) {
                            net.makeBondingSlave();
                            net.setBondingMaster(bridgeName);
                        }

                        nics.add(net);
                    }
                }
                else if (setupBridge && bridgeSlaves.contains(n.getName())) {
                    Network net = new Network(getCobblerConnection(),
                            n.getName());
                    net.setMacAddress(n.getHwaddr());
                    net.makeBondingSlave();
                    net.setBondingMaster(bridgeName);
                    nics.add(net);
                }
            }
            if (setupBridge) {
                Network net = new Network(getCobblerConnection(), bridgeName);
                net.makeBondingMaster();
                net.setBondingOptions(bridgeOptions);
                net.setStaticNetwork(!isBridgeDhcp);
                if (!isBridgeDhcp) {
                    net.setNetmask(bridgeNetmask);
                    net.setIpAddress(bridgeAddress);
                    rec.setGateway(bridgeGateway);
                }
                nics.add(net);
            }
        }
        rec.setNetworkInterfaces(nics);
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
     * @param commentIn the comment
     */
    public void setComment(String commentIn) {
        this.comment = commentIn;
    }

    /**
     * Getter for the comment.
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
