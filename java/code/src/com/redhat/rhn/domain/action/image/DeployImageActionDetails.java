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
package com.redhat.rhn.domain.action.image;

import com.redhat.rhn.domain.action.ActionChild;

/**
 * DeployImageActionDetails - Class representation of the table rhnActionImageDeploy.
 * @version $Rev$
 */
public class DeployImageActionDetails extends ActionChild {

	private Long id;
	private Long vcpus;
	private Long memKb;
	private String bridgeDevice;
	private String imageType;
	private String downloadUrl;
	private String proxyServer;
	private String proxyUser;
	private String proxyPass;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
    /**
     * @return Returns the number of virtual cpus
     */
	public Long getVcpus() {
	    return vcpus;
	}

    /**
     * @param inVCpus The number of cpus to set.
     */
	public void setVcpus(Long vcpus) {
	    this.vcpus = vcpus;
	}

    /**
     * @return Returns the amount of memory in kb.
     */
	public Long getMemKb() {
	    return memKb;
	}

    /**
     * @param memkb The amount of memory to set.
     */
	public void setMemKb(Long memkb) {
	    this.memKb = memkb;
	}

    /**
     * Return the bridge device.
     * @param bridgeDevice
     */
    public String getBridgeDevice() {
        return bridgeDevice;
    }

    /**
     * Set the bridge device.
     * @param bridgeDevice
     */
    public void setBridgeDevice(String bridgeDevice) {
        this.bridgeDevice = bridgeDevice;
    }

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    /**
     * Set the download URL.
     * @param downloadUrl
     */
    public String getDownloadUrl() {
        return downloadUrl;
    }

    /**
     * Return the download URL.
     * @param downloadUrl
     */
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getProxyServer() {
        return proxyServer;
    }

    public void setProxyServer(String proxyServer) {
        this.proxyServer = proxyServer;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public void setProxyUser(String proxyUser) {
        this.proxyUser = proxyUser;
    }

    public String getProxyPass() {
        return proxyPass;
    }

    public void setProxyPass(String proxyPass) {
        this.proxyPass = proxyPass;
    }
}
