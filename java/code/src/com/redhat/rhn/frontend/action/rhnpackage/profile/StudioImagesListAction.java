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
package com.redhat.rhn.frontend.action.rhnpackage.profile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import com.redhat.rhn.domain.image.Image;
import com.redhat.rhn.domain.image.ImageFactory;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnListAction;
import com.redhat.rhn.frontend.taglibs.list.helper.ListHelper;
import com.redhat.rhn.frontend.taglibs.list.helper.Listable;
import com.suse.studio.client.SUSEStudioClient;
import com.suse.studio.client.data.Appliance;
import com.suse.studio.client.data.Build;

/**
 * This action will present the user with a list of all studio images
 * and allow one to be selected.
 *
 * @version $Revision$
 */
public class StudioImagesListAction extends RhnListAction implements Listable {

    private static final String DATA_SET = "pageList";

    // The credentials
    private String studioUser;
    private String studioAPIKey;
    
    /** {@inheritDoc} */
    public ActionForward execute(ActionMapping actionMapping,
                                 ActionForm actionForm,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
        throws Exception {
    	
    	Boolean submitted = false;
    	
    	// Determine credentials from the form
        if (actionForm instanceof DynaActionForm) {
        	DynaActionForm form = (DynaActionForm) actionForm;
        	studioUser = form.getString("studio_user");
        	studioAPIKey = form.getString("studio_api_key");
        	
        	// Get submitted
        	submitted = (Boolean) form.get("submitted");
            if (submitted == null) {
            	submitted = Boolean.FALSE;
            }
        }
    	
        ListHelper helper = new ListHelper(this, request);
        helper.setDataSetName(DATA_SET);
        helper.execute();
                
    	// Get the selection and store the images
//    	String[] selected = ListTagHelper.getSelected(DATA_SET, request);
//    	if (selected != null) {
//        	storeImages(selected);	
//    	}
        
        // Always forward to default
        ActionForward forward = actionMapping.findForward("default");
        return forward;
    }

    /**
     * Store images given by buildIDs as Strings.
     * @param selected
     */
//    private void storeImages(String[] selected) {
//    	for (Image i : images) {
//        	for (String s : selected) {
//        		if (s.equals(i.getBuildId().toString())) {
//        			ImageFactory.saveImage(i);
//        		}
//        	}	
//    	}
//	}

	/** {@inheritDoc} */
    public List getResult(RequestContext context) {
        List<Appliance> ret = new ArrayList<Appliance>();
    	if (weHaveCredentials()) {
        	SUSEStudioClient client = new SUSEStudioClient(studioUser, studioAPIKey);
    		try {
    			ret = client.getAppliances();
    		} catch (IOException e) {
    			throw new RuntimeException(e);
    		}	
    	}
    	// Convert to image objects
        return createImageList(ret, context);
    }

    /**
     * Create an {@link Image} object out of every build of an appliance.
     * @param appliances
     * @return
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
        		ret.add(img);
    		}
    	}
    	return ret;
    }
    
    /**
     * Check if we currently have valid credentials.
     * @return true or false
     */
    private boolean weHaveCredentials() {
    	return studioUser != null && !studioUser.isEmpty() && 
		studioAPIKey != null && !studioAPIKey.isEmpty();
    }
}
