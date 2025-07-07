/*
 * Copyright (c) 2016 SUSE LLC
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
package com.redhat.rhn.domain.action.salt.build;

import static java.util.stream.Collectors.toMap;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFormatter;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.image.DockerfileProfile;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageProfileFactory;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.KiwiProfile;
import com.redhat.rhn.domain.image.ProfileCustomDataValue;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.ActivationKeyFactory;

import com.suse.manager.webui.utils.token.DownloadTokenBuilder;
import com.suse.manager.webui.utils.token.TokenBuildingException;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ApplyStatesAction - Action class representing the application of Salt states.
 */
public class ImageBuildAction extends Action {
    private static final Logger LOG = LogManager.getLogger(ImageBuildAction.class);

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
     * @param minionSummaries a list of minion summaries of the minions involved in the given Action
     * @param action          action which has all the revisions
     * @return minion summaries grouped by local call
     */
    public static Map<LocalCall<?>, List<MinionSummary>> imageBuildAction(
            List<MinionSummary> minionSummaries, ImageBuildAction action) {

        ImageBuildActionDetails details = action.getDetails();
        if (details == null) {
            return Collections.emptyMap();
        }
        return ImageProfileFactory.lookupById(details.getImageProfileId()).map(
                ip -> imageBuildAction(minionSummaries, Optional.ofNullable(details.getVersion()), ip,
                        action.getId())
        ).orElseGet(Collections::emptyMap);
    }

    protected static Map<LocalCall<?>, List<MinionSummary>> imageBuildAction(
            List<MinionSummary> minionSummaries, Optional<String> version,
            ImageProfile profile, Long actionId) {
        List<ImageStore> imageStores = new LinkedList<>();
        imageStores.add(profile.getTargetStore());

        List<MinionServer> minions = MinionServerFactory.findMinionsByServerIds(
                minionSummaries.stream().map(MinionSummary::getServerId).collect(Collectors.toList()));

        //TODO: optimal scheduling would be to group by host and orgid
        return minions.stream().collect(
                Collectors.toMap(minion -> {
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
                                    //TODO: maybe from the database
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

    private static String getChannelUrl(MinionServer minion, String channelLabel) {
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
}
