/*
 * Copyright (c) 2020 SUSE LLC
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

package com.suse.manager.webui.services.pillar;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.channel.AccessToken;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Pillar;

import com.suse.manager.model.attestation.AttestationFactory;
import com.suse.manager.model.attestation.CoCoAttestationStatus;
import com.suse.manager.model.attestation.ServerCoCoAttestationConfig;
import com.suse.manager.utils.MachinePasswordUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Class for generating minion pillar data containing general information of minions
 */
public class MinionGeneralPillarGenerator extends MinionPillarGeneratorBase {

    /** Logger */
    private static final Logger LOG = LogManager.getLogger(MinionGeneralPillarGenerator.class);

    public static final MinionGeneralPillarGenerator INSTANCE = new MinionGeneralPillarGenerator();
    public static final String CATEGORY = "general";

    private static final int PKGSET_INTERVAL = 5;
    private static final Integer REBOOT_INFO_INTERVAL = 10;

    private static final Map<String, Object> PKGSET_BEACON_PROPS = new HashMap<>();
    private static final Map<String, Object> REBOOT_INFO_BEACON_PROPS = new HashMap<>();

    static {
        PKGSET_BEACON_PROPS.put("interval", PKGSET_INTERVAL);
        REBOOT_INFO_BEACON_PROPS.put("interval", REBOOT_INFO_INTERVAL);
    }

    /**
     * Generates pillar data containing general information of the passed minion
     * @param minion the minion server
     * @return the SaltPillar containing the pillar data
     */
    @Override
    public Optional<Pillar> generatePillarData(MinionServer minion) {
        Pillar pillar = minion.getPillarByCategory(CATEGORY).orElseGet(() -> {
            Pillar newPillar = new Pillar(CATEGORY, new HashMap<>(), minion);
            minion.getPillars().add(newPillar);
            return newPillar;
        });
        pillar.getPillar().clear();
        pillar.add("org_id", minion.getOrg().getId());

        pillar.add("contact_method", minion.getContactMethod().getLabel());
        pillar.add("mgr_server", minion.getChannelHost());
        if ("ssh-push-tunnel".equals(minion.getContactMethod().getLabel())) {
            pillar.add("mgr_server_https_port", Config.get().getInt("ssh_push_port_https"));
        }

        pillar.add("mgr_origin_server", ConfigDefaults.get().getJavaHostname());
        pillar.add("mgr_server_is_uyuni", ConfigDefaults.get().isUyuni());
        pillar.add("machine_password", MachinePasswordUtils.machinePassword(minion));

        Map<String, Object> chanPillar = new HashMap<>();
        minion.getAccessTokens().stream()
                .filter(AccessToken::getValid)
                .forEach(accessToken -> accessToken.getChannels().forEach(chan -> {
            Map<String, Object> chanProps = getChannelPillarData(minion, accessToken, chan);

            chanPillar.put(chan.getLabel(), chanProps);
        }));
        pillar.add("channels", chanPillar);

        Map<String, Object> beaconConfig = new HashMap<>();
        // this add the configuration for the beacon that tell us when the
        // minion packages are modified locally
        if (minion.getOsFamily().toLowerCase().equals("suse") ||
                minion.getOsFamily().toLowerCase().equals("redhat") ||
                minion.getOsFamily().toLowerCase().equals("debian")) {
            beaconConfig.put("pkgset", PKGSET_BEACON_PROPS);
            beaconConfig.put("reboot_info", REBOOT_INFO_BEACON_PROPS);
        }
        if (!beaconConfig.isEmpty()) {
            pillar.add("beacons", beaconConfig);
        }

        Optional<ServerCoCoAttestationConfig> cocoCnf = minion.getOptCocoAttestationConfig();
        cocoCnf.filter(ServerCoCoAttestationConfig::isEnabled).ifPresent(cnf -> {
            AttestationFactory attfct = new AttestationFactory();
            Map<String, Object> attestationPillar = attfct.lookupLatestReportByServer(minion)
                    .filter(r -> r.getStatus().equals(CoCoAttestationStatus.PENDING))
                    .map(r -> new HashMap<>(r.getInData()))
                    .orElse(new HashMap<>());
            attestationPillar.put("environment_type", cnf.getEnvironmentType().name());
            pillar.add("attestation_data", attestationPillar);
        });
        return Optional.of(pillar);
    }

    /**
     * Create channel pillar data for the given minion, access token and channel
     * @param minion the minion
     * @param accessToken the access token
     * @param chan the channel
     * @return a {@link Map} containing the pillar data
     */
    public Map<String, Object> getChannelPillarData(MinionServer minion, AccessToken accessToken,
            Channel chan) {
        Map<String, Object> chanProps = new HashMap<>();
        chanProps.put("alias", "susemanager:" + chan.getLabel());
        chanProps.put("name", chan.getName());
        chanProps.put("enabled", "1");
        chanProps.put("autorefresh", "1");
        chanProps.put("host", minion.getChannelHost());
        if ("ssh-push-tunnel".equals(minion.getContactMethod().getLabel())) {
            chanProps.put("port", Config.get().getInt("ssh_push_port_https"));
        }
        chanProps.put("token", accessToken.getToken());
        if (chan.isTypeRpm()) {
            chanProps.put("type", "rpm-md");
        }
        else if (chan.isTypeDeb()) {
            chanProps.put("type", "deb");
        }
        else {
            LOG.warn("Unknown repo type for channel {}", chan.getLabel());
        }

        if (ConfigDefaults.get().isMetadataSigningEnabled()) {
            chanProps.put("gpgcheck", chan.isGPGCheck() ? "1" : "0");
            // three state field. yes, no or default
            chanProps.put("repo_gpgcheck", "default");
            chanProps.put("pkg_gpgcheck", "default");
            if (chan.isTypeRpm()) {
                // we need the vendor GPG key for RPM signature check
                // /etc/pki/rpm-gpg/mgr-gpg-pub.key is automatically added via SLS file
                Optional.ofNullable(chan.getGPGKeyUrl()).ifPresent(url -> chanProps.put("gpgkeyurl", url));
            }
            else {
                chanProps.put("gpgkeyurl", "file:///usr/share/keyrings/mgr-archive-keyring.gpg");
            }
        }
        else if (chan.isTypeRpm()) {
            Optional.ofNullable(chan.getGPGKeyUrl()).ifPresent(url -> chanProps.put("gpgkeyurl", url));
            chanProps.put("gpgcheck", "0");
            chanProps.put("repo_gpgcheck", "0");
            chanProps.put("pkg_gpgcheck", chan.isGPGCheck() ? "1" : "0");
        }
        // For Type deb the packages are not signed. No need to set a GPG key here.

        // Flag to override dnf modularity failsafe mechanism (module_hotfixes)
        chanProps.put("cloned_nonmodular", chan.isCloned() && !chan.isModular());
        return chanProps;
    }

    @Override
    public String getCategory() {
        return CATEGORY;
    }
}
