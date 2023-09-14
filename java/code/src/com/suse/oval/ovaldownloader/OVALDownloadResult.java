package com.suse.oval.ovaldownloader;

import java.io.File;
import java.util.Optional;

public class OVALDownloadResult {
    private File vulnerabilityFile;
    private File patchFile;


    public Optional<File> getVulnerabilityFile() {
        return Optional.ofNullable(vulnerabilityFile);
    }

    public Optional<File> getPatchFile() {
        return Optional.ofNullable(patchFile);
    }

    public void setVulnerabilityFile(File vulnerabilityFile) {
        this.vulnerabilityFile = vulnerabilityFile;
    }

    public void setPatchFile(File patchFile) {
        this.patchFile = patchFile;
    }
}
