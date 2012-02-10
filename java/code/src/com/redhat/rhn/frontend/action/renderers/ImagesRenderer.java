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

package com.redhat.rhn.frontend.action.renderers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.image.Image;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.frontend.taglibs.list.ListTagHelper;
import com.suse.studio.client.SUSEStudio;
import com.suse.studio.client.data.Appliance;
import com.suse.studio.client.data.Build;

/**
 * Asynchronously render the page content for image selection and deployment.
 */
public class ImagesRenderer extends BaseFragmentRenderer {

    public static final String IMAGES_LIST = "imagesList";

    /**
     * {@inheritDoc}
     */
    @Override
    protected void render(User user, PageControl pc, HttpServletRequest request) {
        // Get the images
        List images = null;
        try {
            images = getImages(user);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Store list of images to the request
        request.setAttribute(IMAGES_LIST, images);
        // Set the "parentUrl" for the form (in rl:listset)
        request.setAttribute(ListTagHelper.PARENT_URL, "");

        // Store the set of images to the session as well
        if (!images.isEmpty()) {
            request.getSession().setAttribute(IMAGES_LIST, images);
        }
    }

    /**
     * Get a list of appliance builds from SUSE Studio.
     * @param user
     * @return list of {@link Image} objects
     */
    private List getImages(User user) throws IOException {
        List<Appliance> ret = new ArrayList<Appliance>();

        // Lookup credentials and studio url
        Credentials creds = CredentialsFactory.lookupByUser(user);
        String studioUser = creds.getUsername();
        String studioKey = creds.getPassword();
        String studioHost = creds.getUrl();

        // Get appliance builds from studio
        if (studioUser != null && studioKey != null) {
            SUSEStudio studio;
            if (studioHost != null) {
                studio = new SUSEStudio(studioUser, studioKey, studioHost);
            } else {
                studio = new SUSEStudio(studioUser, studioKey);
            }
            ret = studio.getAppliances();
        }

        // Convert to a list of images
        return convertAppliances(ret, user);
    }

    /**
     * Convert a list of {@link Appliance}s to a list of {@link Image}s.
     * @param appliances list of appliances
     * @return list of images
     */
    private List<Image> convertAppliances(List<Appliance> appliances,
            User user) {
        List<Image> ret = new LinkedList<Image>();
        for (Appliance appliance : appliances) {
            // Create one image object for every build
            for (Build build : appliance.getBuilds()) {
                Image img = new Image();
                img.setOrg(user.getOrg());
                // Appliance attributes
                img.setName(appliance.getName());
                img.setArch(appliance.getArch());
                // Build attributes
                img.setBuildId(new Long(build.getId()));
                img.setVersion(build.getVersion());
                img.setImageType(build.getImageType());
                img.setDownloadUrl(build.getDownloadUrl());
                ret.add(img);
            }
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageUrl() {
        return "/WEB-INF/pages/common/fragments/systems/images_content.jsp";
    }
}
