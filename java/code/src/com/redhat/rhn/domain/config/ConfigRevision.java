/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.domain.config;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;

import java.util.Date;

/**
 * ConfigRevision - Class representation of the table rhnConfigRevision.
 */
public class ConfigRevision extends BaseDomainHelper {

    protected Long id;
    protected Long revision;
    protected ConfigFile configFile;
    protected ConfigContent configContent;
    protected ConfigInfo configInfo;
    protected ConfigFileType configFileType;

    protected Long changedById;

    private static final String INIT_SLS_PATH = "/init.sls";

    /**
     * Protected constructor
     * Use ConfigurationFactory to create a new revision.
     */
    protected ConfigRevision() {
        // Default to "file"
        configFileType = ConfigFileType.file();
    }

    /**
     * Getter for id
     * @return Long to get
    */
    public Long getId() {
        return this.id;
    }

    /**
     * Setter for id
     * @param idIn to set
    */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * Getter for revision
     * @return Long to get
    */
    public Long getRevision() {
        return this.revision;
    }

    /**
     * Setter for revision
     * @param revisionIn to set
    */
    public void setRevision(Long revisionIn) {
        this.revision = revisionIn;
    }

    /**
     * @return Returns the configContent.
     */
    public ConfigContent getConfigContent() {
        return configContent;
    }
    /**
     * @param configContentIn The configContent to set.
     */
    public void setConfigContent(ConfigContent configContentIn) {
        this.configContent = configContentIn;
    }
    /**
     * @return Returns the configFile.
     */
    public ConfigFile getConfigFile() {
        return configFile;
    }
    /**
     * @param configFileIn The configFile to set.
     */
    public void setConfigFile(ConfigFile configFileIn) {
        this.configFile = configFileIn;
    }
    /**
     * @return Returns the configInfo.
     */
    public ConfigInfo getConfigInfo() {
        return configInfo;
    }
    /**
     * @param configInfoIn The configInfo to set.
     */
    public void setConfigInfo(ConfigInfo configInfoIn) {
        this.configInfo = configInfoIn;
    }

    /**
     * User-id that caused this revision to be made
     * @return usr-id of logged-in-user at change time, or "null" if unknown
     */
    public Long getChangedById() {
        return changedById;
    }

    /**
     * Set the user-id that casued this change
     * @param usrId changing (currently-logged-in) user
     */
    public void setChangedById(Long usrId) {
        changedById = usrId;
    }

    /**
     * User that caused this revision to be made
     * @return User of logged-in-user at change time, or "null" if unknown
     */
    public User getChangedBy() {
        if (getChangedById() != null && getChangedById() > 0) {
            return UserFactory.lookupById(getChangedById());
        }
        return null;
    }

    /**
     * Copies the contents of this object into a new ConfigRevision object.
     * The created and modified dates of the new object are set to now.
     * @return A new ConfigRevision object.
     */
    public ConfigRevision copy() {
        ConfigRevision retval = new ConfigRevision();
        retval.setConfigContent(getConfigContent());
        retval.setConfigFile(getConfigFile());
        retval.setConfigInfo(getConfigInfo());
        retval.setConfigFileType(getConfigFileType());
        retval.setCreated(new Date());
        retval.setModified(new Date());
        retval.setRevision(getRevision());
        return retval;
    }

    /**
     * @return file-type of this revision
     */
    public ConfigFileType getConfigFileType() {
        return configFileType;
    }

    /**
     * @param cft new filetype for this revision
     */
    public void setConfigFileType(ConfigFileType cft) {
        configFileType = cft;
    }

    /**
     * Is this revision a file?
     * @return true if file-type is 'file'
     */
    public boolean isFile() {
        return (configFileType != null && configFileType.getLabel().equals("file"));
    }

    /**
     * Is this revision a sls?
     * @return true if file-type is 'sls'
     */
    public boolean isSls() {
        return (configFileType != null && configFileType.getLabel().equals("sls"));
    }

    /**
     * Is this revision THE init.sls?
     * @return true if file is the init.sls
     */
    public boolean isInitSls() {
        return isSls() && INIT_SLS_PATH.equals(configFile.getConfigFileName().getPath());
    }

    /**
     * Is this revision a directory?
     * @return true if file-type is 'directory'
     */
    public boolean isDirectory() {
        return (configFileType != null && configFileType.getLabel().equals("directory"));
    }

    /**
     * Is this revision a symlink?
     * @return true if file-type is 'symlink'
     */
    public boolean isSymlink() {
        return (configFileType != null && configFileType.getLabel().equals("symlink"));
    }

    /**
     * does the revision match to the other config revision
     * @param other the other revision
     * @return True if matches
     */
    public boolean matches(ConfigRevision other) {
        return configFile.equals(other.getConfigFile()) &&
                configFileType.equals(other.getConfigFileType()) &&
                configInfo.equals(other.getConfigInfo()) &&
                (configContent == null ? other.getConfigContent() == null :
                configContent.equals(other.getConfigContent()));
    }
}
