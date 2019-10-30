/**
 * Copyright (c) 2020 SUSE LLC
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

package com.redhat.rhn.domain.contentmgmt;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.Modules;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Mock class for modulemd API
 */
public class ModulemdApi {

    private static final String MOUNT_POINT_PATH = Config.get().getString(ConfigDefaults.MOUNT_POINT);

    public List<Module> getAllModules(Channel channel) throws RepositoryNotModularException {
        // Mock list
        return Arrays.asList(
                new Module("postgresql", "9.6"),
                new Module("postgresql",  "10"),
                new Module("perl", "5.26"),
                new Module("perl", "5.24")
        );
    }

    private String getMetadataPath(Channel channel) throws RepositoryNotModularException {
        Modules metadata = channel.getModules();
        if (metadata == null) {
            throw new RepositoryNotModularException();
        }
        return new File(MOUNT_POINT_PATH, metadata.getRelativeFilename()).getAbsolutePath();
    }

}
