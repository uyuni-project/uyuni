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

import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageAction;
import com.redhat.rhn.common.util.FileUtils;
import com.redhat.rhn.domain.formula.FormulaFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Pillar;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

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

        LOG.debug("Processing ProxyBackupEvent for minion {}", proxy.getMinionId());

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

        // Copy the backup common files as pillar in the database
        Path configPath = copiedFiles.stream().filter(file -> file.endsWith("config.yaml"))
            .findFirst().map(tmpPath::resolve).orElse(null);
        Path httpdPath = copiedFiles.stream().filter(file -> file.endsWith("httpd.yaml"))
            .findFirst().map(tmpPath::resolve).orElse(null);
        Path sshPath = copiedFiles.stream().filter(file -> file.endsWith("ssh.yaml"))
            .findFirst().map(tmpPath::resolve).orElse(null);
        Path pxeEntries = copiedFiles.stream().filter(file -> file.endsWith("pxe_entries.yaml"))
            .findFirst().map(tmpPath::resolve).orElse(null);

        ProxyConfig config = ProxyConfigUtils.loadFilesToProxyConfig(configPath, httpdPath, sshPath);
        Pillar configPillar = ProxyConfigUtils.proxyConfigToPillar(config).setMinion(proxy);

        proxy.getPillarByCategory(ProxyConfigUtils.PROXY_PILLAR_CATEGORY).ifPresentOrElse(
            pillar -> {
                pillar.setPillar(configPillar.getPillar());
            },
            () -> {
                proxy.addPillar(configPillar);
        });

        SystemManager.addHistoryEvent(proxy, "Proxy configuration created",
                "Proxy configuration migrated. Reinstallation of the proxy will autoconfigure it.");

        // Create cobbler records based on PXE entries
        try {
            String branchid = convertRBSToContainerized(proxy, config.getProxyFqdn());
            convertPxeEntriesToCobbler(pxeEntries, proxy, branchid);
        }
        catch (SaltbootException e) {
            LOG.error("Failed to convert PXE entries for minion {}", proxy.getMinionId());
            // TODO: mark backup action as failed
        }
        catch (ProxyException e) {
            LOG.info(e);
        }

        // TODO create config channel for custom config files if needed
        // TODO Remove the files in the temporary folder
    }

    private String convertRBSToContainerized(MinionServer proxy, String fqdn) throws ProxyException, SaltbootException {
        /* TODO: migrate proxy formulas:
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

        ServerGroup branchGroup = ServerGroupFactory.lookupByNameAndOrg(branchId, proxy.getOrg());
        if (branchGroup == null) {
            throw new SaltbootException("Unable to find branch group with id " + branchId);
        }

        // Check branch is part of the group
        if (!branchGroup.getServers().contains(proxy)) {
            proxy.addGroup(branchGroup);
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
        branchFormulas.remove("tftp");
        FormulaFactory.saveServerFormulas(proxy, branchFormulas);
        return branchId;
    }

    private void convertPxeEntriesToCobbler(Path pxeEntries, MinionServer proxy, String branchId)
            throws SaltbootException {
        if (pxeEntries == null) {
            LOG.info("No branch server and PXE entries found in backup configuration");
            return;
        }

        Map<String, Object> retailData = YamlHelper.loadAs(
                FileUtils.readStringFromFile(pxeEntries.toString()), Map.class);

        String assumedBranchId = (String)retailData.get("branch_id");


        if (retailData.containsKey("pxe_entries")) {
            LOG.debug("Found pxe entries");
            List<Map<String, String>> pixies = (List<Map<String, String>>) retailData.get("pxe_entries");
            try {
                pixies.forEach(pxe -> this.convertSinglePxeEntry(branchId, pxe));
            }
            catch (SaltbootException e) {
                LOG.warn("Ignoring", e);
            }
        }
        else {
            LOG.info("Proxy backup parsing did not find any pxe entries");
        }
    }

    private void convertSinglePxeEntry(String branchid, Map<String, String> entry) throws SaltbootException {
        // Lookup MinionEntry based on MAC address
        LOG.debug("processing entry for MAC {}", entry.get("mac"));
        List<MinionServer> minions = MinionServerFactory.findMinionsByHwAddrs(Set.of(entry.get("mac")));
        if (minions == null || minions.isEmpty()) {
            throw new SaltbootException("Unable to find minion by its MAC address {}");
        }
        else if (minions.size() > 1) {
            throw new SaltbootException("Multiple minions found by one MAC address. Cannot choose one");
        }
        SaltbootUtils.createSaltbootSystem(minions.get(0).getMinionId(),
                entry.get("probable_boot_image"), branchid, List.of(entry.get("mac")), entry.get("args"));
    }

    @Override
    public boolean canRunConcurrently() {
        return true;
    }
}

