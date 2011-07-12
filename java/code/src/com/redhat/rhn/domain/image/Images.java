/**
 * Copyright (c) 2011 Novell
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.image;

import org.apache.log4j.Logger;

import com.redhat.rhn.domain.org.Org;

//import com.redhat.rhn.domain.channel.Channel;
//import com.redhat.rhn.domain.org.Org;
//import com.redhat.rhn.domain.rhnpackage.Package;

//import java.util.Date;
//import java.util.List;
//import java.util.Set;

/**
 * Images - Class representation of the table suseImages.
 * @version $Rev$
 */
public class Images {
    private static Logger log = Logger.getLogger(Images.class);
    
    private Long id;
    private Org org;
    private String name;
    private String version;
    private String arch;
    private String imageType;
    private String downloadUrl;
    private String path;
    private String checksum;
    private String status;
    
    public Long getId() {
        return this.id;
    }

    public void setId(Long inId) {
        this.id = inId;
    }
    
    /**
     * Getter for orgId
     * @return Long to get
     */
    public Org getOrg() {
        return this.org;
    }

    /**
     * Setter for org
     * @param orgIn to set
     */
    public void setOrg(Org orgIn) {
        this.org = orgIn;
    }
    
    public String getName() {
        return this.name;
    }

    public void setName(String inName) {
        this.name = inName;
    }
    
    public String getVersion() {
        return this.version;
    }

    public void setVersion(String inVersion) {
        this.version = inVersion;
    }
    
    public String getArch() {
        return this.arch;
    }

    public void setArch(String inArch) {
        this.arch = inArch;
    }
    
    public String getImageType() {
        return this.imageType;
    }

    public void setImageType(String inImageType) {
        this.imageType = inImageType;
    }
    
    public String getDownloadUrl() {
        return this.downloadUrl;
    }

    public void setDownloadUrl(String inDownloadUrl) {
        this.downloadUrl = inDownloadUrl;
    }
    
    public String getPath() {
        return this.path;
    }

    public void setPath(String inPath) {
        this.path = inPath;
    }
    
    public String getChecksum() {
        return this.checksum;
    }

    public void setChecksum(String inChecksum) {
        this.checksum = inChecksum;
    }
    
    public String getStatus() {
        return this.status;
    }

    public void setStatus(String inStatus) {
        this.status = inStatus;
    }
    
}