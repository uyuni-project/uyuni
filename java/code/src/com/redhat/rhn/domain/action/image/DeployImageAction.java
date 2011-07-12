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
import com.redhat.rhn.domain.image.Images;

import java.util.HashSet;
import java.util.Set;

/**
 * DeployImageAction - Class representation of the table rhnAction.
 * @version $Rev$
 */
public class DeployImageAction extends Action {

    private Set images;

    /**
     * @return Returns the images.
     */
    public Set getImages() {
        return images;
    }

    /**
     * @param imagesIn The images to set.
     */
    public void setImages(Set imagesIn) {
        this.images = imagesIn;
    }

    /**
     * Add an images to this action.
     * @param i Image to add
     */
    public void addImage(Images i) {
        if (images == null) {
            images = new HashSet();
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
