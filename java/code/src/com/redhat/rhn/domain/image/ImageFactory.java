/**
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.domain.image;

import org.apache.log4j.Logger;

import com.redhat.rhn.common.hibernate.HibernateFactory;

/**
 * KickstartFactory
 * @version $Rev$
 */
public class ImageFactory extends HibernateFactory {

    private static ImageFactory singleton = new ImageFactory();
    private static Logger log = Logger.getLogger(ImageFactory.class);

    /**
     * Constructor
     */
    private ImageFactory() {
        super();
    }

    /**
     * Get the Logger for the derived class so log messages
     * show up on the correct class
     */
    protected Logger getLogger() {
        return log;
    }

    /**
     * Create a new {@link Image} from scratch.
     * @return the new image
     */
    public static Image createImage() {
        Image retval = new Image();
        retval.setStatus(Image.STATUS_NEW);
        return retval;
    }
    
    /**
     * @param p KickstartPackage to add to DB
     */
    public static void saveImage(Image image) {
        singleton.saveObject(image);
    }

    /**
     * @param p KickstartPackage to remove from DB
     */
    public static void removeImage(Image image) {
        singleton.removeObject(image);
    }
}
