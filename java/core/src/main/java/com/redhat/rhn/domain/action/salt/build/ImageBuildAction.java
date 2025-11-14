/*
 * Copyright (c) 2016--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.action.salt.build;

import static com.suse.manager.webui.services.SaltConstants.SALT_CP_PUSH_ROOT_PATH;
import static com.suse.manager.webui.services.SaltConstants.SALT_FILE_GENERATION_TEMP_PATH;
import static java.util.stream.Collectors.toMap;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFormatter;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.image.DockerfileProfile;
import com.redhat.rhn.domain.image.ImageFile;
import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.image.ImageInfoFactory;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageProfileFactory;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.KiwiProfile;
import com.redhat.rhn.domain.image.OSImageStoreUtils;
import com.redhat.rhn.domain.image.ProfileCustomDataValue;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.utils.SaltUtils;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.impl.runner.MgrUtilRunner;
import com.suse.manager.webui.utils.salt.custom.ImageChecksum;
import com.suse.manager.webui.utils.salt.custom.OSImageBuildImageInfoResult;
import com.suse.manager.webui.utils.salt.custom.OSImageBuildSlsResult;
import com.suse.manager.webui.utils.token.DownloadTokenBuilder;
import com.suse.manager.webui.utils.token.TokenBuildingException;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.utils.Json;

import com.google.gson.JsonElement;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;


/**
 * ApplyStatesAction - Action class representing the application of Salt states.
 */
@Entity
@DiscriminatorValue("504")
public class ImageBuildAction extends Action {
    private static final Logger LOG = LogManager.getLogger(ImageBuildAction.class);

    @OneToOne(mappedBy = "parentAction", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private ImageBuildActionDetails details;

    /**
     * Return the details.
     * @return details
     */
    public ImageBuildActionDetails getDetails() {
        return details;
    }

    /**
     * Set the details.
     * @param detailsIn details
     */
    public void setDetails(ImageBuildActionDetails detailsIn) {
        if (detailsIn != null) {
            detailsIn.setParentAction(this);
        }
        this.details = detailsIn;
    }

    @Override
    public ActionFormatter getFormatter() {
        if (formatter == null) {
            formatter = new ImageBuildActionFormatter(this);
        }
        return formatter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<LocalCall<?>, List<MinionSummary>> getSaltCalls(List<MinionSummary> minionSummaries) {

        if (details == null) {
            return Collections.emptyMap();
        }
        return ImageProfileFactory.lookupById(details.getImageProfileId()).map(
                ip -> imageBuildAction(minionSummaries, Optional.ofNullable(details.getVersion()), ip,
                        getId())
        ).orElseGet(Collections::emptyMap);
    }

    protected Map<LocalCall<?>, List<MinionSummary>> imageBuildAction(
            List<MinionSummary> minionSummaries, Optional<String> version,
            ImageProfile profile, Long actionId) {
        List<ImageStore> imageStores = new LinkedList<>();
        imageStores.add(profile.getTargetStore());

        List<MinionServer> minions = MinionServerFactory.findMinionsByServerIds(
                minionSummaries.stream().map(MinionSummary::getServerId).collect(Collectors.toList()));

        //INFO: optimal scheduling would be to group by host and orgid
        return minions.stream().collect(
                toMap(minion -> {
                            Map<String, Object> pillar = new HashMap<>();

                            profile.asDockerfileProfile().ifPresent(dockerfileProfile -> {
                                Map<String, Object> dockerRegistries = ImageStore.dockerRegPillar(imageStores);
                                pillar.put("docker-registries", dockerRegistries);

                                String repoPath = Path.of(profile.getTargetStore().getUri(),
                                        profile.getLabel()).toString();
                                String tag = version.orElse("");
                                String certificate = "";
                                // salt 2016.11 dockerng require imagename while salt 2018.3 docker requires it separate
                                pillar.put("imagerepopath", repoPath);
                                pillar.put("imagetag", tag);
                                pillar.put("imagename", repoPath + ":" + tag);
                                pillar.put("builddir", dockerfileProfile.getPath());
                                pillar.put("build_id", "build" + actionId);
                                try {
                                    //Q: maybe from the database
                                    certificate = String.join("\n\n", Files.readAllLines(
                                            Paths.get("/srv/www/htdocs/pub/RHN-ORG-TRUSTED-SSL-CERT"),
                                            Charset.defaultCharset()
                                    ));
                                }
                                catch (IOException e) {
                                    LOG.error("Could not read certificate", e);
                                }
                                pillar.put("cert", certificate);
                                String repocontent = "";
                                if (profile.getToken() != null) {
                                    repocontent = profile.getToken().getChannels().stream()
                                            .map(s -> "[susemanager:" + s.getLabel() + "]\n\n" +
                                                    "name=" + s.getName() + "\n\n" +
                                                    "enabled=1\n\n" +
                                                    "autorefresh=1\n\n" +
                                                    "baseurl=" + getChannelUrl(minion, s.getLabel()) + "\n\n" +
                                                    "type=rpm-md\n\n" +
                                                    "gpgcheck=0\n\n" // we use trusted content and SSL.
                                            ).collect(Collectors.joining("\n\n"));
                                }
                                pillar.put("repo", repocontent);

                                // Add custom info values
                                pillar.put("customvalues", profile.getCustomDataValues().stream()
                                        .collect(toMap(v -> v.getKey().getLabel(), ProfileCustomDataValue::getValue)));
                            });

                            profile.asKiwiProfile().ifPresent(kiwiProfile -> {
                                pillar.put("source", kiwiProfile.getPath());
                                pillar.put("build_id", "build" + actionId);
                                pillar.put("kiwi_options", kiwiProfile.getKiwiOptions());
                                List<String> repos = new ArrayList<>();
                                final ActivationKey activationKey =
                                        ActivationKeyFactory.lookupByToken(profile.getToken());
                                Set<Channel> channels = activationKey.getChannels();
                                for (Channel channel : channels) {
                                    repos.add(getChannelUrl(minion, channel.getLabel()));
                                }
                                pillar.put("kiwi_repositories", repos);
                                pillar.put("activation_key", activationKey.getKey());
                            });

                            String saltCall = "";
                            if (profile instanceof DockerfileProfile) {
                                saltCall = "images.docker";
                            }
                            else if (profile instanceof KiwiProfile) {
                                saltCall = "images.kiwi-image-build";
                            }

                            return State.apply(
                                    Collections.singletonList(saltCall),
                                    Optional.of(pillar)
                            );
                        },
                        m -> Collections.singletonList(new MinionSummary(m))
                ));
    }

    private String getChannelUrl(MinionServer minion, String channelLabel) {
        String token;

        try {
            token = new DownloadTokenBuilder(minion.getOrg().getId())
                    .usingServerSecret()
                    .expiringAfterMinutes(Config.get().getInt(ConfigDefaults.TEMP_TOKEN_LIFETIME))
                    .allowingOnlyChannels(Set.of(channelLabel))
                    .build()
                    .getSerializedForm();
        }
        catch (TokenBuildingException e) {
            LOG.error("Could not generate token for {}", channelLabel, e);
            token = "";
        }

        String host = minion.getChannelHost();

        return "https://" + host + "/rhn/manager/download/" + channelLabel + "?" + token;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleUpdateServerAction(ServerAction serverAction, JsonElement jsonResult, UpdateAuxArgs auxArgs) {

        serverAction.setResultMsg(SaltUtils.getJsonResultWithPrettyPrint(jsonResult));

        Optional<ImageInfo> infoOpt = ImageInfoFactory.lookupByBuildAction(this);
        if (infoOpt.isEmpty()) {
            LOG.error("ImageInfo not found while performing: {} in handleImageBuildData", getName());
            return;
        }
        ImageInfo info = infoOpt.get();

        handleImageBuildLog(info, auxArgs.getSaltApi());

        if (serverAction.isStatusCompleted()) {
            if (details == null) {
                LOG.error("Details not found while performing: {} in handleImageBuildData", getName());
                return;
            }
            Long imageProfileId = details.getImageProfileId();
            if (imageProfileId == null) { // It happens when the image profile is deleted during a build action
                LOG.error("Image Profile ID not found while performing: {} in handleImageBuildData", getName());
                return;
            }

            boolean isKiwiProfile = false;
            Optional<ImageProfile> profileOpt = ImageProfileFactory.lookupById(imageProfileId);
            if (profileOpt.isPresent()) {
                isKiwiProfile = profileOpt.get().asKiwiProfile().isPresent();
            }
            else {
                LOG.warn("Could not find any profile for profile ID {}", imageProfileId);
            }

            if (isKiwiProfile) {
                serverAction.getServer().asMinionServer().ifPresent(minionServer -> {
                    // Update the image info and download the built Kiwi image to SUSE Manager server
                    OSImageBuildImageInfoResult buildInfo =
                            Json.GSON.fromJson(jsonResult, OSImageBuildSlsResult.class)
                                    .getKiwiBuildInfo().getChanges().getRet();

                    info.setChecksum(ImageInfoFactory.convertChecksum(buildInfo.getImage().getChecksum()));
                    info.setName(buildInfo.getImage().getName());
                    info.setVersion(buildInfo.getImage().getVersion());

                    ImageInfoFactory.updateRevision(info);

                    List<List<Object>> files = new ArrayList<>();
                    String imageDir = info.getName() + "-" + info.getVersion() + "-" + info.getRevisionNumber() + "/";
                    if (!buildInfo.getBundles().isEmpty()) {
                        buildInfo.getBundles().forEach(bundle -> files.add(List.of(bundle.getFilepath(),
                                imageDir + bundle.getFilename(), "bundle", bundle.getChecksum())));
                    }
                    else {
                        files.add(List.of(buildInfo.getImage().getFilepath(),
                                imageDir + buildInfo.getImage().getFilename(), "image",
                                buildInfo.getImage().getChecksum()));
                        buildInfo.getBootImage().ifPresent(f -> {
                            files.add(List.of(f.getKernel().getFilepath(),
                                    imageDir + f.getKernel().getFilename(), "kernel",
                                    f.getKernel().getChecksum()));
                            files.add(List.of(f.getInitrd().getFilepath(),
                                    imageDir + f.getInitrd().getFilename(), "initrd",
                                    f.getInitrd().getChecksum()));
                        });
                    }
                    files.stream().forEach(file -> {
                        String targetPath = OSImageStoreUtils.getOSImageStorePathForImage(info);
                        targetPath += info.getName() + "-" + info.getVersion() + "-" + info.getRevisionNumber() + "/";
                        MgrUtilRunner.ExecResult collectResult = auxArgs.getSystemQuery()
                                .collectKiwiImage(minionServer, (String)file.get(0), targetPath)
                                .orElseThrow(() -> new RuntimeException("Failed to download image."));

                        if (collectResult.getReturnCode() != 0) {
                            serverAction.setStatusFailed();
                            serverAction.setResultMsg(StringUtils
                                    .left(SaltUtils.printStdMessages(collectResult.getStderr(),
                                                    collectResult.getStdout()),
                                            1024));
                        }
                        else {
                            ImageFile imagefile = new ImageFile();
                            imagefile.setFile((String)file.get(1));
                            imagefile.setType((String)file.get(2));
                            imagefile.setChecksum(ImageInfoFactory.convertChecksum(
                                    (ImageChecksum.Checksum)file.get(3)));
                            imagefile.setImageInfo(info);
                            info.getImageFiles().add(imagefile);
                        }
                    });
                });
            }
            else {
                ImageInfoFactory.updateRevision(info);
                if (info.getImageType().equals(ImageProfile.TYPE_DOCKERFILE)) {
                    ImageInfoFactory.obsoletePreviousRevisions(info);
                }
            }
        }
        if (serverAction.isStatusCompleted()) {
            // both building and uploading results succeeded
            info.setBuilt(true);

            try {
                ImageInfoFactory.scheduleInspect(info, Date.from(Instant.now()), getSchedulerUser());
            }
            catch (TaskomaticApiException e) {
                LOG.error("Could not schedule image inspection ", e);
            }
        }
        ImageInfoFactory.save(info);
    }

    private void handleImageBuildLog(ImageInfo info, SaltApi saltApi) {
        MinionServer buildHost = info.getBuildServer();
        if (buildHost == null) {
            return;
        }

        Path srcPath = Path.of(SALT_CP_PUSH_ROOT_PATH + buildHost.getMinionId() +
                "/files/image-build" + getId() + ".log");
        Path tmpPath = Path.of(SALT_FILE_GENERATION_TEMP_PATH + "/image-build" + getId() + ".log");

        try {
            // copy the log to a directory readable by tomcat
            saltApi.copyFile(srcPath, tmpPath)
                    .orElseThrow(() -> new RuntimeException("Can't copy the build log file"));

            String log = Files.readString(tmpPath);
            info.setBuildLog(log);
            saltApi.removeFile(srcPath);
            saltApi.removeFile(tmpPath);
        }
        catch (Exception e) {
            LOG.info("No build log for action {} {}", getId(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean clientExecutionReturnsYamlFormat() {
        return true;
    }
}
