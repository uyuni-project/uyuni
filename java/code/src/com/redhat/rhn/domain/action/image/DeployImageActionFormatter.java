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

import com.redhat.rhn.common.util.StringUtil;
import com.redhat.rhn.domain.action.ActionFormatter;
import com.redhat.rhn.domain.image.Images;

import java.util.Set;

/**
 * DeployImageActionFormatter - Class that overrides getNotes()
 * to display Image specific information.
 *
 * @version $Rev$
 */
public class DeployImageActionFormatter extends ActionFormatter {

    /**
     * Create a new DeployImageActionFormatter
     * @param actionIn the DeployImageAction we want to use to format
     */
    public DeployImageActionFormatter(DeployImageAction actionIn) {
        super(actionIn);
    }

    /**
     * Output the Image info into the body.
     * @return String of the Image HTML
     */
    protected String getNotesBody() {
        StringBuffer retval = new StringBuffer();
        Set images = ((DeployImageAction) this.getAction()).getImages();
        if (images != null && images.size() > 0) {
            Images image = (Images) images.toArray()[0];
            retval.append(StringUtil.htmlifyText(image.getName()));
            retval.append("<br/>");
        }

        return retval.toString();
    }
}
