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
 * Utilities for working with repo files
 */
public class RepoFileUtils {

    public static final String GENERATED_SLS_ROOT = "/srv/susemanager/salt";
    public static final String SALT_CHANNEL_FILES = "channels";

    private RepoFileUtils() {
    }

    /**
     * Generates a .repo file containting all its channels for a given server
     *
     * @param server server to generate repo file for
     * @throws IOException if an I/O error occurs writing to or creating the file
     * @throws JoseException if the token creation fails
     */
    public static void generateRepositoryFile(Server server)
            throws IOException, JoseException {
            String fileName = "channels.repo." + server.getDigitalServerId();
            String token = TokenUtils.createTokenWithServerKey(
                    Optional.of(server.getOrg().getId()), Collections.emptySet());

            String fileContents = StreamSupport
                    .stream(server.getChannels().spliterator(), false)
                    .map(ch -> RepoFileUtils
                            .repoFileEntryForChannel(ch, token)
                            .fileFormat())
                    .collect(Collectors.joining("\n"));

            Path baseDir = Paths.get(GENERATED_SLS_ROOT, SALT_CHANNEL_FILES);
            Files.write(baseDir.resolve(fileName), fileContents.getBytes());
    }

    /**
     * Creates a RepoFileEntry for a given channel using some authorisation token
     *
     * @param channel the channel to
     * @param token the token to be used to authorize the channel access
     * @return a RepoFileEntry containing the channel information
     */
    public static RepoFileEntry repoFileEntryForChannel(Channel channel, String token) {
        String alias = "susemanager:" + channel.getLabel();
        return new RepoFileEntry(alias, channel.getName(), true, true,
                String.format("https://%s/rhn/manager/download/%s?%s",
                        ConfigDefaults.get().getCobblerHost(),
                        channel.getLabel(),
                        token),
                "rpm-md", false, false, true, Optional.empty());
    }
}
