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
 * DeployImageAction - Class representation of the table rhnAction.
 * @version $Rev$
 */
public class DeployImageActionDetails extends ActionChild {

	private static final long serialVersionUID = -2655853160693467815L;
	private Long id;
	private Long imageId;
	private Long vcpus;
	private Long memKb;

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
     * @return Returns the images.
     */
    public Long getImageId() {
        return this.imageId;
    }

    /**
     * @param imagesIn The images to set.
     */
    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }
}
