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

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.image.Image;
import com.redhat.rhn.domain.image.ImageFactory;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnListAction;
import com.redhat.rhn.frontend.taglibs.list.ListTagHelper;
import com.redhat.rhn.frontend.taglibs.list.helper.ListHelper;
import com.redhat.rhn.frontend.taglibs.list.helper.Listable;
import com.redhat.rhn.manager.action.ActionManager;

/**
 * This action will present the user with a list of all studio images
 * and allow one to be selected.
 *
 * @version $Revision$
 */
public class ScheduleImageDeploymentAction extends RhnListAction implements Listable {

    private static final String DATA_SET = "pageList";
    
    /** {@inheritDoc} */
    public ActionForward execute(ActionMapping actionMapping,
                                 ActionForm actionForm,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
        throws Exception {
    	
    	Boolean submitted = false;
    	Long vcpus = null;
    	Long memkb = null;
    	Long sid = null;
    	
    	// Read parameters from the form
        if (actionForm instanceof DynaActionForm) {
        	DynaActionForm form = (DynaActionForm) actionForm;
        	submitted = (Boolean) form.get("submitted");
            if (submitted == null) {
            	submitted = Boolean.FALSE;
            }
        	vcpus = (Long) form.get("vcpus");
        	memkb = (Long) form.get("memkb");
        	sid = (Long) form.get("sid");
        }
    	
        String id = ListTagHelper.getRadioSelection(ListHelper.LIST, request);
        
        ListHelper helper = new ListHelper(this, request);
        helper.setDataSetName(DATA_SET);
        helper.execute();

        ActionForward forward;
        if (submitted) {
        	RequestContext ctx = new RequestContext(request);
        	// Get the image from the id
        	Image image = ImageFactory.lookupById(new Long(id));
        	// Create the action and store it
            Action deploy = ActionManager.createDeployImageAction(
            		ctx.getCurrentUser(), image, vcpus, memkb);
            ActionManager.addServerToAction(sid, deploy);
            ActionManager.storeAction(deploy);
        	forward = actionMapping.findForward("success");
        } else {
            forward = actionMapping.findForward("default");        	
        }
        
        return forward;
    }

    /** {@inheritDoc} */
    public List getResult(RequestContext context) {
        return ImageFactory.getDeployableImages(context.getCurrentUser().getOrg());
    }
}
