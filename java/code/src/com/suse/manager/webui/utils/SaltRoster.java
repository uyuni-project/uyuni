/**
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
package com.suse.manager.webui.utils;

import com.redhat.rhn.common.util.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Abstraction of salt-ssh roster data to be dumped as yaml.
 */
public class SaltRoster {

    private Map<String, Map<String, Object>> data = new HashMap<>();
    private static final String FILE_PREFIX = "susemanager-roster-";

    /**
     * Creates a roster that contains a single host based on the input data.
     * @param hostName host name
     * @param user user
     * @param password optional password
     * @param port port
     * @return roster
     */
    public static SaltRoster createSingleHostRoster(String hostName, String user,
            Optional<String> password, Optional<Integer> port) {
        SaltRoster result = new SaltRoster();
        result.addHost(hostName, user, password, port);
        return result;
    }

    /**
     * Add host data to this roster.
     *
     * @param host The IP address or DNS name of the remote host
     * @param user The user to login as
     * @param passwd The password to login with
     * @param port The target system's ssh port number
     */
    public void addHost(String host, String user, Optional<String> passwd,
            Optional<Integer> port) {
        Map<String, Object> hostData = new LinkedHashMap<>();
        hostData.put("host", host);
        hostData.put("user", user);
        passwd.ifPresent(value -> hostData.put("passwd", value));
        port.ifPresent(value -> hostData.put("port", value));
        data.put(host, hostData);
    }

    /**
     * Persist this roster in a temporary file.
     *
     * @return path to the roster file
     * @throws IOException in case there is an I/O error writing the roster file
     */
    public Path persistInTempFile() throws IOException {
        Path filePath = Files.createTempFile(FILE_PREFIX, ".tmp");
        FileUtils.writeStringToFile(YamlHelper.INSTANCE.dump(data), filePath.toString());
        return filePath;
    }
}
