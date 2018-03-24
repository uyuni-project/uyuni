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
package com.suse.manager.webui.services;

/**
 * Various constants related to Salt.
 */
public class SaltConstants {

    private SaltConstants() { }

    public static final String SUMA_STATE_FILES_ROOT_PATH = "/srv/susemanager/salt";

    public static final String SUMA_PILLAR_DATA_PATH = "/srv/susemanager/pillar_data";

    public static final String SALT_FILE_GENERATION_TEMP_PATH = "/srv/susemanager/tmp";

    public static final String PILLAR_DATA_FILE_PREFIX = "pillar";

    public static final String PILLAR_DATA_FILE_EXT = "yml";

    public static final String SLS_FILE_ENCODING = "US-ASCII";

    public static final String SALT_SERVER_STATE_FILE_PREFIX = "custom_";

    public static final String SALT_CONFIG_STATES_DIR = "custom";

    public static final String ORG_STATES_DIRECTORY_PREFIX = "manager_org_";

    public static final String LEGACY_STATES_BACKUP = "/srv/susemanager/legacy_states";

    public static final String SCRIPTS_DIR = "scripts";

}
