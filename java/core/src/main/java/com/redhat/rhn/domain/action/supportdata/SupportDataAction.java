/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.domain.action.supportdata;

import com.redhat.rhn.common.util.http.HttpClientAdapter;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionSummary;

import com.suse.manager.supportconfig.SupportDataStateResult;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.impl.SaltSSHService;
import com.suse.manager.webui.utils.MinionActionUtils;
import com.suse.manager.webui.utils.salt.LocalCallWithExecutors;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.calls.modules.Test;
import com.suse.utils.Json;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;

/**
 * SupportDataAction - Class representing TYPE_SUPPORTDATA_GET
 */
@Entity
@DiscriminatorValue("526")
public class SupportDataAction extends Action {
    private static final Logger LOG = LogManager.getLogger(SupportDataAction.class);

    @OneToOne(mappedBy = "parentAction", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private SupportDataActionDetails details;

    public SupportDataActionDetails getDetails() {
        return details;
    }

    /**
     * Sets the details for this SupportDataAction.
     *
     * @param detailsIn the Set of SupportDataActionDetails to be set
     */
    public void setDetails(SupportDataActionDetails detailsIn) {
        details = detailsIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (this == oIn) {
            return true;
        }
        if (!(oIn instanceof SupportDataAction that)) {
            return false;
        }
        return new EqualsBuilder().appendSuper(super.equals(oIn)).append(details, that.details).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(details)
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<LocalCall<?>, List<MinionSummary>> getSaltCalls(List<MinionSummary> minionSummaries) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();

        var partitioned = minionSummaries.stream().collect(Collectors.partitioningBy(minionSummary -> {
            var actionPath = MinionActionUtils.getFullActionPath(getOrg().getId(), minionSummary.getServerId(),
                    getId());
            var bundle = actionPath.resolve("bundle.tar");
            return Files.exists(bundle);
        }));

        var pillar = Optional.ofNullable(getDetails().getParameter())
                .map(p -> Map.of("arguments", (Object)p));
        var full = partitioned.get(false);
        var onlyUpload = partitioned.get(true);
        if (!full.isEmpty()) {
            // supportdata should be taken always in direct mode - also on transactional systems
            var apply = State.apply(List.of("supportdata"), pillar);
            ret.put(new LocalCallWithExecutors<>(apply, List.of("direct_call"), Collections.emptyMap()), full);
        }
        if (!onlyUpload.isEmpty()) {
            ret.put(Test.echo("supportdata"), onlyUpload);
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleUpdateServerAction(ServerAction serverAction, JsonElement jsonResult, UpdateAuxArgs auxArgs) {
        var action = (SupportDataAction) serverAction.getParentAction();
        var caseNumber = action.getDetails().getCaseNumber();
        serverAction.getServer().asMinionServer().ifPresentOrElse(
                minionServer -> {
                    var actionPath = MinionActionUtils.getFullActionPath(action.getOrg().getId(), minionServer.getId(),
                            action.getId());
                    var bundle = actionPath.resolve("bundle.tar");
                    var dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_hhmm").withZone(ZoneOffset.UTC);

                    var prefix = "MIN";
                    if (minionServer.isProxy()) {
                        prefix = "PXY";
                    }
                    else if (minionServer.isMgrServer()) {
                        prefix = "SRV";
                    }

                    var shortHostname = minionServer.getHostname().split("\\.")[0];
                    var uploadName = "SR%s_%s_%s_%s.tar".formatted(caseNumber, prefix, shortHostname,
                            dateTimeFormatter.format(Instant.now()));

                    if (jsonResult.isJsonPrimitive() && jsonResult.getAsJsonPrimitive().isString() &&
                            jsonResult.getAsJsonPrimitive().getAsString().equals("supportdata")) {
                        uploadSupportData(uploadName, bundle, serverAction);
                    }
                    else {
                        fetchSupportData(jsonResult, serverAction, minionServer, auxArgs.getSaltApi())
                                .ifPresent(path -> uploadSupportData(uploadName, path, serverAction));
                    }
                },
                () -> serverAction.fail("server is not a minion: %d".formatted(serverAction.getServer().getId()))
        );
    }

    private void uploadSupportData(String uploadName, Path bundle, ServerAction serverAction) {
        var httpClient = new HttpClientAdapter();
        var action = (SupportDataAction) serverAction.getParentAction();
        var host = switch (action.getDetails().getGeoType()) {
            case EU -> "https://support-ftp.emea.suse.com";
            case US -> "https://support-ftp.us.suse.com";
        };

        if (Files.exists(bundle)) {
            var url = host + "/incoming/upload.php?file=" + uploadName;
            HttpPut put = new HttpPut(url);
            put.setHeader("User-Agent", "SupportConfig");
            put.setEntity(new FileEntity(bundle.toFile()));
            try {
                var response = httpClient.executeRequest(put);
                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    serverAction.fail("Error uploading supportdata status code: %d"
                            .formatted(response.getStatusLine().getStatusCode()));
                    return;
                }
                serverAction.setResultMsg(uploadName + " uploaded");
                Files.deleteIfExists(bundle);
            }
            catch (IOException e) {
                serverAction.fail("Error uploading supportdata: %s".formatted(e.getMessage()));
            }
        }
        else {
            serverAction.fail("Error uploading supportdata: file '%s' does not exist".formatted(bundle.toString()));
        }
    }

    private Optional<Path> fetchSupportData(JsonElement jsonResult, ServerAction serverAction,
                                            MinionServer minionServer, SaltApi saltApi) {
        try {

            var result = Json.GSON.fromJson(jsonResult, SupportDataStateResult.class);
            var supportDataOpt = result.getSupportData();
            if (supportDataOpt.isPresent()) {
                var supportData = supportDataOpt.get();
                if (!supportData.isResult()) {
                    serverAction.fail("Error fetching supportdata from minion: %s"
                            .formatted(supportData.getComment()));
                    return Optional.empty();
                }
                if (!supportData.getChanges().getRet().isSuccess()) {
                    serverAction.fail("Error fetching supportdata: %s"
                            .formatted(supportData.getChanges().getRet().getError()));
                    return Optional.empty();
                }

                var supportDataDir = Paths.get(supportData.getChanges().getRet().getSupportDataDir());

                return fetchSupportFiles(minionServer, serverAction, supportDataDir, saltApi)
                        .flatMap(actionPath -> {
                            var bundle = actionPath.resolve("bundle.tar");

                            List<Path> supportDataFiles;
                            try (var files = Files.list(actionPath)) {
                                supportDataFiles = files.toList();
                            }
                            catch (IOException e) {
                                serverAction.fail("Error listing files in '%s': %s"
                                        .formatted(actionPath, e.getMessage()));
                                return Optional.empty();
                            }

                            try (var outputStream = new TarArchiveOutputStream(Files.newOutputStream(bundle))) {
                                outputStream.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
                                outputStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
                                for (var file : supportDataFiles) {
                                    var entry = outputStream.createArchiveEntry(file.toFile(), file.getFileName()
                                            .toString());
                                    outputStream.putArchiveEntry(entry);
                                    Files.copy(file, outputStream);
                                    outputStream.closeArchiveEntry();
                                }
                                return Optional.of(bundle);
                            }
                            catch (IOException e) {
                                serverAction.fail("Error packaging supportdata: %s".formatted(e.getMessage()));
                                return Optional.empty();
                            }
                            finally {
                                supportDataFiles.forEach(file -> {
                                    try {
                                        Files.deleteIfExists(file);
                                    }
                                    catch (IOException e) {
                                        LOG.error("Error deleting file '{}': {}", file, e.getMessage(), e);
                                    }
                                });
                            }
                        });

            }
            else {
                serverAction.fail("Error missing supportdata result");
                return Optional.empty();
            }
        }
        catch (JsonSyntaxException e) {
            serverAction.fail("Error parsing supportdata result: %s".formatted(e.getMessage()));
            return Optional.empty();
        }
    }


    private Optional<Path> fetchSupportFiles(MinionServer minionServer, ServerAction serverAction,
                                             Path supportDataDir, SaltApi saltApi) {
        var actionId = serverAction.getParentAction().getId();
        var hostname = minionServer.getMinionId();
        if (minionServer.isSSHPush()) {
            try {
                var actionPath = MinionActionUtils.getActionPath(minionServer, actionId);
                var user = SaltSSHService.getSSHUser();
                var port = Optional.ofNullable(minionServer.getSSHPushPort()).orElse(SaltSSHService.SSH_PUSH_PORT);
                String method = minionServer.getContactMethod().getLabel();
                List<String> proxyPath = SaltSSHService.proxyPathToHostnames(minionServer.getServerPaths(),
                        Optional.empty());
                List<String> proxyCommand = SaltSSHService.sshProxyCommandOption(proxyPath, method, hostname, port)
                        .orElse(List.of());
                String sshOptions = "-o ConnectTimeout=2 " + proxyCommand.stream()
                        .map("-o %s"::formatted)
                        .collect(Collectors.joining(" "));

                var rsync = "rsync -p --chown salt:susemanager --chmod=660 -e \"ssh %s -p %d -i %s\" %s@%s:%s/* %s/."
                        .formatted(sshOptions, port, SaltSSHService.SSH_KEY_PATH, user, hostname,
                                supportDataDir, actionPath);
                LOG.info(rsync);
                var copyResult = saltApi.execOnMaster(rsync);
                String error = copyResult.orElse("Error copying supportdata");
                if (!error.isBlank()) {
                    serverAction.fail(error);
                    return Optional.empty();
                }

                return Optional.of(actionPath);
            }
            catch (IOException e) {
                serverAction.fail("Error copying supportdata: %s".formatted(e.getMessage()));
                return Optional.empty();
            }
            finally {
                var cleanupResult = saltApi.callSync(
                        com.suse.salt.netapi.calls.modules.File.remove(supportDataDir.toString()),
                        minionServer.getMinionId()
                );
                if (cleanupResult.isEmpty()) {
                    LOG.warn("Error cleaning up supportdata files '{}' on minion: {}",
                            supportDataDir, minionServer.getMinionId());
                }
            }
        }
        else {
            var copyResult = saltApi.storeMinionScapFiles(minionServer, supportDataDir.toString(), actionId);
            if (copyResult.containsKey(false)) {
                serverAction.fail("Error copying supportdata: %s".formatted(copyResult.get(false)));
                return Optional.empty();
            }
            return Optional.of(Paths.get(copyResult.get(true)));
        }
    }

}
