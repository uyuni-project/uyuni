/*
 * Copyright (c) 2017 SUSE LLC
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

package com.suse.manager.webui.utils.gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Dto object holding info about SSM base channel changes.
 */
public class SsmBaseChannelChangesDto {

    /**
     * Base channel change.
     */
    public static class Change {

        private long oldBaseId;
        private long newBaseId;

        /**
         * Constructor.
         *
         * @param oldBaseIdIn old base channel id
         * @param newBaseIdIn new base channel id
         */
        public Change(long oldBaseIdIn, long newBaseIdIn) {
            this.oldBaseId = oldBaseIdIn;
            this.newBaseId = newBaseIdIn;
        }

        /**
         * @return old base channel id
         */
        public long getOldBaseId() {
            return oldBaseId;
        }

        /**
         * @param oldBaseIdIn to set
         */
        public void setOldBaseId(long oldBaseIdIn) {
            this.oldBaseId = oldBaseIdIn;
        }

        /**
         * @return new base channel id
         */
        public long getNewBaseId() {
            return newBaseId;
        }
    }

    private List<Change> changes = new ArrayList<>();

    /**
     * @return list of base channel changes
     */
    public List<Change> getChanges() {
        return changes;
    }

}
