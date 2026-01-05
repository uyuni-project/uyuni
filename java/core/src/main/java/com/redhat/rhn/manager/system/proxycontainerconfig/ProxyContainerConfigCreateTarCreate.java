/*
 * Copyright (c) 2024 SUSE LLC
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

package com.redhat.rhn.manager.system.proxycontainerconfig;

import com.redhat.rhn.common.RhnRuntimeException;

import com.suse.manager.webui.utils.YamlHelper;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Handles the process of creating the tarball containing the proxy container configuration files.
 */
public class ProxyContainerConfigCreateTarCreate implements ProxyContainerConfigCreateContextHandler {
    @Override
    public void handle(ProxyContainerConfigCreateContext context) {
        try {
            ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
            BufferedOutputStream bufOut = new BufferedOutputStream(bytesOut);
            GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(bufOut);
            TarArchiveOutputStream tarOut = new TarArchiveOutputStream(gzOut);

            addTarEntry(tarOut, "config.yaml", context.getConfigMap(), 0644);
            addTarEntry(tarOut, "httpd.yaml", context.getHttpConfigMap(), 0600);
            addTarEntry(tarOut, "ssh.yaml", context.getSshConfigMap(), 0600);

            tarOut.finish();
            tarOut.close();

            context.setConfigTar(bytesOut.toByteArray());
        }
        catch (IOException e) {
            throw new RhnRuntimeException(
                    "Exception generating proxy container configuration tarball file: " + e.getMessage()
            );
        }

    }

    /**
     * Creates and adds a yaml file to the tarball
     *
     * @param tarOut the tarball output stream
     * @param name   the name of the yaml file
     * @param map    the contents of the yaml file
     * @param mode   the mode of the file
     * @throws IOException if an error occurs
     */
    private void addTarEntry(
            TarArchiveOutputStream tarOut, String name, Map<String, Object> map, int mode
    ) throws IOException {
        byte[] data = YamlHelper.INSTANCE.dumpPlain(map).getBytes();
        TarArchiveEntry entry = new TarArchiveEntry(name);
        entry.setSize(data.length);
        entry.setMode(mode);
        tarOut.putArchiveEntry(entry);
        tarOut.write(data);
        tarOut.closeArchiveEntry();
    }
}
