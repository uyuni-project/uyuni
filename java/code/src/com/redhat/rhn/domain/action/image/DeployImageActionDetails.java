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
	private String downloadUrl;

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
	public void setVcpus(Long inVcpus) {
	    this.vcpus = inVcpus;
	}

    /**
     * @return Returns the amount of memory in kb.
     */
	public Long getMemKb() {
	    return memKb;
	}

    /**
     * @param inMemKb The amount of memory to set.
     */
	public void setMemKb(Long inMemKb) {
	    this.memKb = inMemKb;
	}

    /**
     * Set the bridge device.
     * @param bridgeDevice
     */
    public void setBridgeDevice(String bridgeDevice) {
        this.bridgeDevice = bridgeDevice;
    }

    /**
     * Return the bridge device.
     * @param bridgeDevice
     */
    public String getBridgeDevice() {
        return bridgeDevice;
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
}
