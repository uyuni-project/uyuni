package com.suse.manager.webui.utils;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.server.Server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 *
 */
public class RepoFileManager {

    public static final String GENERATED_SLS_ROOT = "/srv/susemanager/salt";
    public static final String SALT_BASE_FILE_ROOT = "/srv/salt";
    public static final String SALT_CHANNEL_FILES = "channels";

    public static void generateRepositoryFile(Server server) {
        String fileName = "channels.repo." + server.getDigitalServerId();
        String fileContents = StreamSupport.stream(server.getChannels().spliterator(), false)
                .map(RepoFileManager::repoFromChannel)
                .map(RepoFile::fileFormat)
                .collect(Collectors.joining("\n"));

        Path baseDir = Paths.get(SALT_BASE_FILE_ROOT, SALT_CHANNEL_FILES);
        try {
            Files.write(baseDir.resolve(fileName), fileContents.getBytes());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static RepoFile repoFromChannel(Channel channel) {
        String alias = "susemanager:" + channel.getLabel();
        return new RepoFile(alias, channel.getName(), true, true, "https://localhost", "rpm-md", false, false, true, "spacewalk");
    }

}
