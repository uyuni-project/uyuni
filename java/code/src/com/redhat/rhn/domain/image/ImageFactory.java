/**
 * Copyright (c) 2011 Novell
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

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.org.Org;

/**
 * ImageFactory
 */
public class ImageFactory extends HibernateFactory {

    private static ImageFactory singleton = new ImageFactory();
    private static Logger log = Logger.getLogger(ImageFactory.class);

    // The base path for downloading images
    private static String IMAGE_PATH = "images/";

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
     * Store an image to the database.
     * @param image The image that will be written to the DB
     */
    public static void saveImage(Image image) {
        // Set the image path before saving object
        image.setPath(IMAGE_PATH + image.getOrg().getId() + "/" +
                image.getChecksum() + "/" + image.getFileName());
        singleton.saveObject(image);
    }

    /**
     * @param p KickstartPackage to remove from DB
     */
    public static void removeImage(Image image) {
        singleton.removeObject(image);
    }
    
    @SuppressWarnings("unchecked")
    public static List<Image> getDeployableImages(Org org) {
    	Session session = null;
        List<Image> retval = null;
        
        session = HibernateFactory.getSession();
        Query q = session.getNamedQuery("Image.listImagesByOrg");
        retval = q.setParameter("org", org).list();
        return retval;
    }

    public static Image lookupById(Long id) {
        if (id == null) {
            return null;
        }

        Session session = null;
        try {
            session = HibernateFactory.getSession();
            return (Image) session.getNamedQuery("Image.findById")
                .setParameter("id", id)
                //Retrieve from cache if there
                .setCacheable(true)
                .uniqueResult();
        }
        catch (HibernateException e) {
            log.error("Hibernate exception: " + e.toString());
            throw e;
        }
    }
}
