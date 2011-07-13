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

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFormatter;
import com.redhat.rhn.domain.image.Image;

import java.util.HashSet;
import java.util.Set;

/**
 * DeployImageAction - Class representation of the table rhnAction.
 * @version $Rev$
 */
public class DeployImageAction extends Action {

	private static final long serialVersionUID = -2655853160693467815L;
	private Set<Image> images;
	private int vcpus;
	private int memKb;

    /**
     * @return Returns the number of virtual cpus
     */
	public int getVCpus() {
	    return vcpus;
	}

    /**
     * @param inVCpus The number of cpus to set.
     */
	public void setVCpus(int inVCpus) {
	    this.vcpus = inVCpus;
	}

    /**
     * @return Returns the amount of memory in kb.
     */
	public int getMemKb() {
	    return memKb;
	}

    /**
     * @param inMemKb The amount of memory to set.
     */
	public void setMemKb(int inMemKb) {
	    this.memKb = inMemKb;
	}

    /**
     * @return Returns the images.
     */
    public Set<Image> getImages() {
        return images;
    }

    /**
     * @param imagesIn The images to set.
     */
    public void setImages(Set<Image> imagesIn) {
        this.images = imagesIn;
    }

    /**
     * Add an images to this action.
     * @param i Image to add
     */
    public void addImage(Image i) {
        if (images == null) {
            images = new HashSet<Image>();
        }
        images.add(i);
    }

    /**
     * Get the Formatter for this class but in this case we use
     * ErrataActionFormatter.
     *
     * {@inheritDoc}
     */
    public ActionFormatter getFormatter() {
        if (formatter == null) {
            formatter = new DeployImageActionFormatter(this);
        }
        return formatter;
    }
}
