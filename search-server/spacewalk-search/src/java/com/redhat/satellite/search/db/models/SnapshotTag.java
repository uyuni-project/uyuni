/**
 * Copyright (c) 2008--2013 Red Hat, Inc.
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
package com.redhat.satellite.search.db.models;


/**
 * SnapshotTag
 * @version $Rev$
 */
public class SnapshotTag extends GenericRecord {
    private long snapshotId;
    private long tagNameId;
    private long serverId;
    private long orgId;
    private String name;
    private String created;
    private String modified;

   private String makeUniqId(long serverIdIn, long snapshotdIn) {
       return serverIdIn + "-" + snapshotdIn;
   }

   /**
    *
    * @return uniqId
    */
   public String getUniqId() {
       return makeUniqId(serverId, snapshotId);
   }

    /**
     * @return the snapshotId
     */
    public long getSnapshotId() {
        return snapshotId;
    }
    /**
     * @param snapshotIdIn the snapshotId to set
     */
    public void setSnapshotId(long snapshotIdIn) {
        this.snapshotId = snapshotIdIn;
    }
    /**
     * @return the tagNameId
     */
    public long getTagNameId() {
        return tagNameId;
    }
    /**
     * @param tagNameIdIn the tagNameId to set
     */
    public void setTagNameId(long tagNameIdIn) {
        this.tagNameId = tagNameIdIn;
    }
    /**
     * @return the serverId
     */
    public long getServerId() {
        return serverId;
    }
    /**
     * @param serverIdIn the serverId to set
     */
    public void setServerId(long serverIdIn) {
        this.serverId = serverIdIn;
    }
    /**
     * @return the orgId
     */
    public long getOrgId() {
        return orgId;
    }
    /**
     * @param orgIdIn the orgId to set
     */
    public void setOrgId(long orgIdIn) {
        this.orgId = orgIdIn;
    }
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @param nameIn the name to set
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }
    /**
     * @return the created
     */
    public String getCreated() {
        return created;
    }
    /**
     * @param createdIn the created to set
     */
    public void setCreated(String createdIn) {
        this.created = createdIn;
    }
    /**
     * @return the modified
     */
    public String getModified() {
        return modified;
    }
    /**
     * @param modifiedIn the modified to set
     */
    public void setModified(String modifiedIn) {
        this.modified = modifiedIn;
    }
}
