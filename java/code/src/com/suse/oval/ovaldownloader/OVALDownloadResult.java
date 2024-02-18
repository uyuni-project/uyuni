/*
 * Copyright (c) 2023 SUSE LLC
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

package com.suse.oval.ovaldownloader;

import java.io.File;
import java.util.Optional;

/**
 * This class encapsulates the OVAL (XML) files produced by the {@link OVALDownloader} class.
 * Usually, OVAL data of Linux distributions is organized in a repository that contains vulnerability and patch
 * definitions. The vulnerability definitions are grouped together in the same OVAL (XML) file, and the same thing
 * for patch definitions.
 * */
public class OVALDownloadResult {
    private File vulnerabilityFile;
    private File patchFile;


    public Optional<File> getVulnerabilityFile() {
        return Optional.ofNullable(vulnerabilityFile);
    }

    public Optional<File> getPatchFile() {
        return Optional.ofNullable(patchFile);
    }

    public void setVulnerabilityFile(File vulnerabilityFileIn) {
        this.vulnerabilityFile = vulnerabilityFileIn;
    }

    public void setPatchFile(File patchFileIn) {
        this.patchFile = patchFileIn;
    }
}
