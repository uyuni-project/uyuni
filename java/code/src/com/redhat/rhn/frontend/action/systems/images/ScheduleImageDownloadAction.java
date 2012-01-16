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
package com.redhat.rhn.frontend.action.systems.images;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.redhat.rhn.domain.image.Image;
import com.redhat.rhn.domain.image.ImageFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnListSetHelper;
import com.redhat.rhn.frontend.taglibs.list.ListTagHelper;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.rhnset.RhnSetManager;
import com.suse.studio.client.SUSEStudioClient;
import com.suse.studio.client.data.Appliance;
import com.suse.studio.client.data.Build;

/**
 * This action will present the user with a list of all studio images
 * and allow one to be selected.
 */
public class ScheduleImageDownloadAction extends RhnAction {

    private static final String LIST_NAME = "images";
    private static final String DATA_SET = "pageList";
    private static final String SUCCESS_KEY = "studio.download.scheduled";

    private List<Image> images;
    
    /**
     * Return the set declaration used for this action.
     * @return the set declaration
     */
    protected RhnSetDecl getDecl() {
        return RhnSetDecl.IMAGES;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public ActionForward execute(ActionMapping actionMapping,
                                 ActionForm actionForm,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws Exception {

    	// Init the context and current user
        RequestContext context = new RequestContext(request);
        User user = context.getLoggedInUser();
        RhnSet set = getDecl().get(user);

        // If not submitted, initialize the set
        if (!context.isSubmitted()) {
            set.clear();
            for (Object o : getImages(context)) {
                set.add(o);
            }
            RhnSetManager.store(set);
        }

        RhnListSetHelper helper = new RhnListSetHelper(request);

        request.setAttribute(ListTagHelper.PARENT_URL, request.getRequestURI());
        request.setAttribute(DATA_SET, images);
        ListTagHelper.bindSetDeclTo(LIST_NAME, getDecl(), request);

        ActionForward forward;
        if (context.isSubmitted()) {
            // Store selected images
            helper.updateSet(set, LIST_NAME);
            storeImages(set.getElementValues());
            createSuccessMessage(request, SUCCESS_KEY, String.valueOf(set.size()));
            forward = actionMapping.findForward("success");
        } else {
            forward = actionMapping.findForward("default");
        }

        return forward;
    }

    /**
     * Store images given by buildIDs as {@link Long}.
     * @param selected
     */
    private void storeImages(Set<Long> selected) {
        for (Long s : selected) {
            // Write selected image builds to the DB
            for (Image i : images) {
                if (s.equals(i.getBuildId())) {
                    i.setStatus(Image.STATUS_PICKUP);
                    ImageFactory.saveImage(i);
                }
            }
        }
	}

	/**
	 * Get the {@link Image} DTOs.
	 */
    public List getImages(RequestContext context) {
        List<Appliance> ret = new ArrayList<Appliance>();

        Org org = context.getCurrentUser().getOrg();

        // Take credentials stored with the org
        String user = org.getStudioUser();
        String apikey = org.getStudioKey();

        // Get appliance builds from studio
    	if (user != null && apikey != null) {
        	SUSEStudioClient client = new SUSEStudioClient(user, apikey);
    		try {
    			ret = client.getAppliances();
    		} catch (IOException e) {
    			throw new RuntimeException(e);
    		}	
    	}

    	// Convert to image objects
        images = createImageList(ret, context);
        
        // Check which of these images are already available
        List<Image> available = ImageFactory.getAllImages(org);
        for (Image i : images) {
            if (available.contains(i)) {
                // This one is already in our DB, get the status
                String s = available.get(available.indexOf(i)).getStatus();
                i.setStatus(s);
                // Only allow to select images with status new or error
                if (!(s.equals(Image.STATUS_NEW) || s.equals(Image.STATUS_ERROR))) {
                    i.setSelected(true);
                    i.setSelectable(false);
                }
            }
        }

        return images;
    }

    /**
     * Create an {@link Image} object out of every build of an appliance.
     * @param appliances
     * @return list of images
     */
    private List<Image> createImageList(List<Appliance> appliances, 
    		RequestContext context) {
    	List<Image> ret = new LinkedList<Image>();
    	for (Appliance appliance : appliances) {
    		// Create one image object for every build
    		for (Build build : appliance.getBuilds()) {
        		Image img = ImageFactory.createImage();
        		img.setOrg(context.getCurrentUser().getOrg());
        		// Appliance attributes
        		img.setName(appliance.getName());
        		img.setArch(appliance.getArch());
        		// Build attributes
        		img.setBuildId(new Long(build.getId()));
        		img.setVersion(build.getVersion());
        		img.setImageType(build.getImageType());
        		img.setDownloadUrl(build.getDownloadURL());
        		// Take filename and checksum from the URL
        		img.setFileName(getFilename(img.getDownloadUrl()));
        		img.setChecksum(getChecksum(img.getDownloadUrl()));
        		ret.add(img);
    		}
    	}
    	return ret;
    }

    /**
     * Parse the filename from the URL.
     * @param downloadURL
     * @return checksum
     */
    private String getFilename(String downloadURL) {
        String[] parts = downloadURL.split("/");
        return parts[parts.length-1];
    }

    /**
     * Parse the checksum from the URL.
     * @param downloadURL
     * @return checksum
     */
    private String getChecksum(String downloadURL) {
        String[] parts = downloadURL.split("/");
        return parts[parts.length-2];
    }
}
