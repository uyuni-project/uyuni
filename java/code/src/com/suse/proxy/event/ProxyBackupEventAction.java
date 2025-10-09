/*
 * Copyright (c) 2025 SUSE LLC
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

package com.suse.proxy.event;

import static com.suse.manager.webui.services.SaltConstants.SALT_FILE_GENERATION_TEMP_PATH;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageAction;
import com.redhat.rhn.common.util.FileUtils;
import com.redhat.rhn.domain.formula.FormulaFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Pillar;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.saltboot.SaltbootException;
import com.suse.manager.saltboot.SaltbootUtils;
import com.suse.manager.webui.services.SaltConstants;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.utils.YamlHelper;
import com.suse.proxy.ProxyConfigUtils;
import com.suse.proxy.ProxyException;
import com.suse.proxy.model.ProxyConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ProxyBackupEventAction implements MessageAction {
    private static final Logger LOG = LogManager.getLogger(ProxyBackupEventAction.class);
    private static final String BRANCH_FORMULA = "formula-branch-network";
    private static final String IMAGE_SYNC_FORMULA = "formula-image-synchronize";
    private static final String SALTBOOT_GROUP_FORMULA = "saltboot-group";

    private final SaltApi saltApi;
    /**
     * Constructor taking a {@link SaltApi} instance.
     *
     * @param saltApiIn Salt API instance to use
     */
    public ProxyBackupEventAction(SaltApi saltApiIn) {
        saltApi = saltApiIn;
    }

    @Override
    public void execute(EventMessage msg) {
        ProxyBackupEvent proxyEvent = ((ProxyBackupEventMessage) msg).getProxyBackupEvent();
        MinionServer proxy = proxyEvent.getMinion();

        SystemManager.addHistoryEvent(proxy, "Proxy migration: started",
                "Received event with client side data, starting data migration");
        ServerFactory.save(proxy);

        Path base = Paths.get(SaltConstants.SALT_CP_PUSH_ROOT_PATH, proxy.getMinionId(), "files");
        Path tmpPath = Path.of(SALT_FILE_GENERATION_TEMP_PATH);

        List<String> files = proxyEvent.getFiles();

        // Move files where we can read them
        List<String> copiedFiles = files.stream().map(file -> {
            String newPath = String.format("proxy-%s_%s", proxy.getId(), file.replace("/", "_"));
            saltApi.copyFile(base.resolve(file), tmpPath.resolve(newPath), true, true);
            saltApi.removeFile(base.resolve(file));
            return newPath;
        }).toList();

        if (!validMigrationFiles(copiedFiles)) {
            SystemManager.addHistoryEvent(proxy, "Proxy migration: failed",
                "Received incomplete backup data. Try retrying the migration.");
            ServerFactory.save(proxy);
        }

        // Copy the backup common files as pillar in the database
        Path configPath = copiedFiles.stream().filter(file -> file.endsWith("config.yaml"))
            .findFirst().map(tmpPath::resolve).orElse(null);
        Path httpdPath = copiedFiles.stream().filter(file -> file.endsWith("httpd.yaml"))
            .findFirst().map(tmpPath::resolve).orElse(null);
        Path sshPath = copiedFiles.stream().filter(file -> file.endsWith("ssh.yaml"))
            .findFirst().map(tmpPath::resolve).orElse(null);
        Path pxeEntries = copiedFiles.stream().filter(file -> file.endsWith("pxe_entries.yaml"))
            .findFirst().map(tmpPath::resolve).orElse(null);

        ProxyConfig config;
        Pillar configPillar;
        try {
            config = ProxyConfigUtils.loadFilesToProxyConfig(configPath, httpdPath, sshPath);
            if (config.getEmail() == null || config.getEmail().isEmpty()) {
                config.setEmail(Config.get().getString("web.traceback_mail"));
            }
            config.setProxyPort(8022);
            configPillar = ProxyConfigUtils.proxyConfigToPillar(config).setMinion(proxy);
        }
        catch (Exception e) {
            LOG.error("Failed to parse migration files for minion {}", proxy.getMinionId(), e);
            SystemManager.addHistoryEvent(proxy, "Proxy migration: failed",
                    "Unable to parse uploaded data. See log file for details");
            return;
        }

        proxy.getPillarByCategory(ProxyConfigUtils.PROXY_PILLAR_CATEGORY).ifPresentOrElse(
            pillar -> pillar.setPillar(configPillar.getPillar()),
            () -> proxy.addPillar(configPillar)
        );

        SystemManager.addHistoryEvent(proxy, "Proxy migration: new configuration created",
                "Reinstallation of the proxy will now autoconfigure it on the first boot");
        ServerFactory.save(proxy);

        if (proxy.getPillarByCategory(BRANCH_FORMULA).isPresent()) {
            SystemManager.addHistoryEvent(proxy, "Retail Branch Server migration: started",
                    "Proxy detected as a Retail Branch Server, migration started");
            ServerFactory.save(proxy);
            // Create cobbler records based on PXE entries
            try {
                List<String> results = new ArrayList<>();
                String branchid = convertRBSToContainerized(proxy, config.getProxyFqdn());
                Boolean allPass = convertPxeEntriesToCobbler(pxeEntries, proxy, branchid, results);
                SystemManager.addHistoryEvent(proxy, String.format("Retail Branch Server migration: finished %s",
                                allPass ? "successfully" : "with errors"),
                        "Proxy was detected to be a Retail Branch Server, branch migration was performed.<ul>" +
                                results.stream().collect(Collectors.joining("</li><li>", "<li>", "</li>")) + "</ul>");
                ServerFactory.save(proxy);
            }
            catch (SaltbootException | ProxyException e) {
                LOG.error("Failed to convert PXE entries for minion {}", proxy.getMinionId(), e);
                SystemManager.addHistoryEvent(proxy, "Retail Branch Server migration: failed",
                        "Failed to migrate the branch server. See log files for details.");
                return;
            }
            catch (Exception e) {
                LOG.error("Unexpected exception for minion {}", proxy.getMinionId(), e);
                SystemManager.addHistoryEvent(proxy, "Retail Branch Server migration: failed",
                        "Failed to migrate the branch server. See log files for details.");
                return;
            }
        }
        SystemManager.addHistoryEvent(proxy, "Proxy migration: finished",
                "All data were migrated. Proceed with reinstallation of the proxy host.");

        // TODO create config channel for custom config files if needed
        // TODO Remove the files in the temporary folder
    }

    private boolean validMigrationFiles(List<String> copiedFiles) {
        Long numFiles = copiedFiles.stream().filter(file -> file.endsWith(".yaml")).count();
        return numFiles == 3 || numFiles == 4;
    }

    private String convertRBSToContainerized(MinionServer proxy, String fqdn) throws ProxyException, SaltbootException {
        /*
        1) get proxy formulas and a) check branch network formula for branch id verification
        1) b) check image-sync formula for default image
        2) check if saltboot group formula exists on the branch group
        2) a) if yes, do nothing else
        2) b) if no, assign saltboot group formula on the group and create saltboot profile

        - note this has a prerequisite migration of the image files, if there are any bundled images left
        */

        Pillar branch = proxy.getPillarByCategory(BRANCH_FORMULA).
                orElseThrow(() -> new ProxyException("Not a branch server"));
        String branchId = (String)branch.getPillarValue("pxe:branch_id");
        if (branchId == null || branchId.isEmpty()) {
            throw new SaltbootException("Not a valid branch server configuration");
        }
        // Get default image from the image sync formula
        String defaultImage = proxy.getPillarByCategory(IMAGE_SYNC_FORMULA).map(
                pillar -> {
                    try {
                        return (String)pillar.getPillarValue("image-synchronize:default_boot_image");
                    }
                    catch (LookupException e) {
                        return "";
                    }
                }
        ).orElse("");

        LOG.info("Migrating branch id {} with branch server {}. Default image: '{}'",
                branchId, proxy.getMinionId(), defaultImage);

        ServerGroup branchGroup = ServerGroupFactory.lookupByNameAndOrg(branchId.trim(), proxy.getOrg());
        if (branchGroup == null) {
            throw new SaltbootException("Unable to find branch group with id " + branchId);
        }

        // Assign saltboot-group formula
        List<String> formulas = FormulaFactory.getFormulasByGroup(branchGroup);
        if (!formulas.contains(SALTBOOT_GROUP_FORMULA)) {
            formulas.add(SALTBOOT_GROUP_FORMULA);
            FormulaFactory.saveGroupFormulas(branchGroup, formulas);
        }

        Map<String, Object> data = Map.of(
        "saltboot", Map.ofEntries(
                Map.entry("containerized_proxy", true),
                Map.entry("default_boot_image", defaultImage),
                Map.entry("default_boot_image_version", ""),
                Map.entry("download_server", fqdn),
                Map.entry("minion_id_naming", "Hostname")
        ));
        FormulaFactory.saveGroupFormulaData(data, branchGroup, SALTBOOT_GROUP_FORMULA);

        // Disable tftp and pxe formulas
        List<String> branchFormulas = FormulaFactory.getFormulasByMinion(proxy);
        branchFormulas.remove("pxe");
        branchFormulas.remove("tftpd");
        FormulaFactory.saveServerFormulas(proxy, branchFormulas);
        return branchId;
    }

    private Boolean convertPxeEntriesToCobbler(Path pxeEntries, MinionServer proxy, String branchId,
                                              List<String> messages) throws SaltbootException {
        ServerGroup branchGroup = ServerGroupFactory.lookupByNameAndOrg(branchId.trim(), proxy.getOrg());
        if (branchGroup == null) {
            throw new SaltbootException("Unable to find branch group with id " + branchId);
        }

        if (pxeEntries == null) {
            messages.add("No PXE entries found in a backup configuration for branch " + branchId);
            return true;
        }

        Map<String, Object> retailData = YamlHelper.loadAs(
                FileUtils.readStringFromFile(pxeEntries.toString()), Map.class);

        if (retailData.containsKey("pxe_entries")) {
            LOG.debug("Found pxe entries");
            List<Map<String, String>> pixies = (List<Map<String, String>>) retailData.get("pxe_entries");

            return pixies.stream().map(
                    pxe -> this.convertSinglePxeEntry(proxy, branchGroup, pxe, messages)
            ).reduce(true, (a, b) -> a && b);
        }
        messages.add("No PXE entries found in a backup configuration for branch " + branchId);
        return true;
    }

    private Boolean convertSinglePxeEntry(MinionServer proxy, ServerGroup branchGroup, Map<String, String> entry,
                                         List<String> messages) {
        // Lookup MinionEntry based on MAC address
        String mac = entry.get("mac");
        String branchid = branchGroup.getName();

        LOG.debug("processing entry for MAC {}", mac);
        List<MinionServer> minions = MinionServerFactory.findMinionsByHwAddrs(Set.of(mac));
        if (minions == null || minions.isEmpty()) {
            // Try lookup of empty profiles if this is repeated migration
            minions = MinionServerFactory.findEmptyProfilesByHwAddrs(Set.of(mac));
            if (minions == null || minions.isEmpty()) {
                // Create empty profile for minions that do not exist
                MinionServer minion = GlobalInstanceHolder.SYSTEM_MANAGER.createSystemProfile(proxy.getCreator(), mac,
                        Map.of("hwAddress", mac));
                minion.addGroup(branchGroup);

                // We had to create new empty profile but that might be only forgotten pxe entry. To ease with
                // post-migration process, put all of these to lost and found group.
                ServerGroup branchLostAndFoundGroup = ServerGroupFactory.lookupByNameAndOrg(
                        branchid + "-lostandfound", proxy.getOrg());
                if (branchLostAndFoundGroup == null) {
                    branchLostAndFoundGroup = ServerGroupFactory.create(
                            branchid + "-lostandfound",
                            String.format("Collection of systems not registered, but present in branch %s pxe entries.",
                                    branchid),
                            proxy.getOrg()
                    );
                }
                ServerFactory.addServerToGroup(minion, branchLostAndFoundGroup);
                ServerFactory.addServerToGroup(minion, branchGroup);

                messages.add(String.format(
                "Lost and found: system with HW address %s in branch %s was not found as registered system."
                                , mac, branchid));
                minions = List.of(minion);
            }
        }
        if (minions.size() > 1) {
            messages.add(String.format(
                    "Multiple minions found with the same MAC address %s. Cannot choose one, ignoring", mac));
            return false;
        }

        MinionServer minion = minions.get(0);
        String minionId = minion.getMinionId();

        // Check if minion is part of the branch group. If not, then ignore as this might have been moved minion.
        if (!minion.getGroups().contains(branchGroup)) {
            messages.add(String.format("Not processing minion '%s' as it is not part of a branch group.", minionId));
            return false;
        }

        // Lookup distro to check if exists
        String probableBootImage = entry.get("probable_boot_image");
        String image = SaltbootUtils.findImageSaltbootProfile(probableBootImage, proxy.getOrg())
                .orElseGet(() -> {
                    messages.add(String.format(
                            "Image not found: system '%s' is using unknown image '%s'. Using default boot image.",
                            minionId, probableBootImage));
                    return SaltbootUtils.DEFAULT_BOOT_IMAGE;
                });

        try {
            SaltbootUtils.createSaltbootSystem(minions.get(0), image, branchid,
                    List.of(mac), entry.get("args"));
        }
        catch (SaltbootException e) {
            messages.add("Failed to create Saltboot system: " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean canRunConcurrently() {
        return true;
    }
}
