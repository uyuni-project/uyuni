/**
 * Copyright (c) 2012 Novell
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.redhat.rhn.common.hibernate.HibernateFactory;

/**
 * ImageTypeFactory
 */
public class ImageTypeFactory extends HibernateFactory {

    private static ImageTypeFactory singleton = new ImageTypeFactory();
    private static Logger log = Logger.getLogger(ImageTypeFactory.class);

    private ImageTypeFactory() {
        super();
    }

    /**
     * Load all available {@link ImageType}s.
     * @return List of available ImageTypes
     */
    @SuppressWarnings("unchecked")
    public static List<ImageType> getAllImageTypes() {
        Map<String, Object> params = new HashMap<String, Object>();
        return singleton.listObjectsByNamedQuery("ImageType.findAll", params, true);
    }

    /**
     * Return a {@link HashMap} containing all available image types using the
     * labels as keys.
     * @return Map containing available image types.
     */
    public static Map<String, ImageType> getImageTypesMap() {
        Map<String, ImageType> ret = new HashMap<String, ImageType>();
        List<ImageType> list = ImageTypeFactory.getAllImageTypes();
        for (ImageType t : list) {
            ret.put(t.getLabel(), t);
        }
        return ret;
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
