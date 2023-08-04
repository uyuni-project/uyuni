package com.suse.oval.ovaldownloader;

import java.util.Map;
import java.util.Objects;

public class DebianOVALStreamInfo extends OVALStreamInfo {
    private static final Map<String, String> VERSION_TO_CODENAME_MAPPING =
            Map.of("10", "buster",
                    "11", "bullseye",
                    "12", "bookworm");
    private final String osVersion;
    private final String codename;
    public DebianOVALStreamInfo(String osVersion) {
        this.osVersion = Objects.requireNonNull(osVersion);
        this.codename = VERSION_TO_CODENAME_MAPPING.get(osVersion);
    }

    @Override
    String localFileName() {
        return String.format("debian-%s-%s", osVersion, codename);
    }

    @Override
    String remoteFileUrl() {
        return String.format("https://www.debian.org/security/oval/oval-definitions-%s.xml.bz2", codename);
    }

    @Override
    OVALCompressionMethod getCompressionMethod() {
        return OVALCompressionMethod.BZIP2;
    }
}
