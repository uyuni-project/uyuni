package com.suse.manager.webui.utils;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.server.Server;
import org.jose4j.lang.JoseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 *
 */
public class RepoFileManager {

    public static final String GENERATED_SLS_ROOT = "/srv/susemanager/salt";
    public static final String SALT_BASE_FILE_ROOT = "/srv/salt";
    public static final String SALT_CHANNEL_FILES = "channels";

    public static void generateRepositoryFile(Server server) throws IOException, JoseException {
            String fileName = "channels.repo." + server.getDigitalServerId();
            String token = TokenUtils.createTokenWithServerKey(
                    Optional.of(server.getOrg().getId()), Collections.emptySet());

            String fileContents = StreamSupport.stream(server.getChannels().spliterator(), false)
                    .map(ch -> RepoFileManager.repoFromChannel(ch, token).fileFormat())
                    .collect(Collectors.joining("\n"));

            Path baseDir = Paths.get(SALT_BASE_FILE_ROOT, SALT_CHANNEL_FILES);
            Files.write(baseDir.resolve(fileName), fileContents.getBytes());
    }

    public static RepoFile repoFromChannel(Channel channel, String token) {
        String alias = "susemanager:" + channel.getLabel();

        return new RepoFile(alias, channel.getName(), true, true,
                String.format("https://%s/rhn/manager/download/%s?%s",
                        ConfigDefaults.get().getCobblerHost(),
                        channel.getLabel(),
                        token),
                "rpm-md", false, false, true, Optional.empty());
    }
}
