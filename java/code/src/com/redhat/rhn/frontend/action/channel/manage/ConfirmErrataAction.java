/**
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.channel.manage;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ErrataOverview;
import com.redhat.rhn.frontend.dto.PackageOverview;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.struts.RhnListAction;
import com.redhat.rhn.frontend.taglibs.list.ListTagHelper;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.rhnset.RhnSetManager;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * ConfirmErrataAction
 * @version $Rev$
 */
public class ConfirmErrataAction extends RhnListAction {


    private static final String CID = "cid";

    private static final String CHECKED = "assoc_checked";

    private static final String MULTI_ARCH = "multi_arch";

    private static final String SELECTED_CHANNEL = "selected_channel";
    private static final String ARCH_COUNT = "arch_count";
    private static final String BUG_COUNT = "bug_count";
    private static final String ENHANCE_COUNT = "enhance_count";
    private static final String SECURE_COUNT = "secure_count";

    private static final String[] MULTI_ARCHES = { "x86_64", "ia64", "ppc", "s390x"};

    /**
     * {@inheritDoc}
     */
    public ActionForward execute(ActionMapping mapping,
            ActionForm formIn,
            HttpServletRequest request,
            HttpServletResponse response) {

        RequestContext requestContext = new RequestContext(request);
        User user =  requestContext.getCurrentUser();
        Long cid = Long.parseLong(request.getParameter(CID));
        Channel currentChan = ChannelFactory.lookupByIdAndUser(cid, user);
        boolean packageAssoc = request.getParameter(CHECKED) != null;

        PublishErrataHelper.checkPermissions(user, cid);

        request.setAttribute(ListTagHelper.PARENT_URL, request.getRequestURI());
        request.setAttribute("channel_name", currentChan.getName());

        request.setAttribute(CID, cid);
        if (requestContext.wasDispatched("Clone Errata")) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("cid", cid);
            return getStrutsDelegate().forwardParams(mapping.findForward("clone"), params);
        }

        Long sourceCid = null;
        Channel srcChan = null;
        String selChannel = request.getParameter(SELECTED_CHANNEL);
        if ((selChannel != null) && (selChannel != "")) {
            sourceCid = Long.parseLong(selChannel);
            srcChan = ChannelFactory.lookupByIdAndUser(sourceCid, user);
        }

        //If this is a possible confusing arch, then set the multi_arch flag
        for (String arch : MULTI_ARCHES) {
            if (arch.equals(currentChan.getChannelArch().getLabel())) {
                request.setAttribute(MULTI_ARCH, "True");
            }
        }




        //Get Errata Summary Counts
        DataResult<ErrataOverview> errataResult = ErrataManager.
                    lookupErrataListFromSet(user, getSetDecl(currentChan).getLabel());
        int bugCount = 0;
        int enhanceCount = 0;
        int securityCount = 0;
        for (ErrataOverview errata : errataResult) {
            if (errata.getAdvisoryType().equals(ErrataFactory.ERRATA_TYPE_BUG)) {
                bugCount++;
            }
            else if (errata.getAdvisoryType().equals(ErrataFactory.
                            ERRATA_TYPE_ENHANCEMENT)) {
                enhanceCount++;
            }
            else {
                securityCount++;
            }
        }
        request.setAttribute(BUG_COUNT, bugCount);
        request.setAttribute(ENHANCE_COUNT, enhanceCount);
        request.setAttribute(SECURE_COUNT, securityCount);
        request.setAttribute("errataList", errataResult);




        //Get Package Info and counts
        DataResult<PackageOverview> packageResult =
            ErrataManager.lookupPacksFromErrataSet(srcChan, currentChan, user,
                    getSetDecl(currentChan).getLabel());


        List<PackageOverview> validList = packageResult;


        storePackagesInSet(user, validList, currentChan);

        Map<String, HashMap> archMap = new HashMap();
        for (PackageOverview pack : validList) {
            if (archMap.get(pack.getPackageArch()) == null) {
                archMap.put(pack.getPackageArch(), new HashMap());
                archMap.get(pack.getPackageArch()).put("size", 0);
                archMap.get(pack.getPackageArch()).put("name", pack.getPackageArch());
            }
            Map arch =   archMap.get(pack.getPackageArch());
            arch.put("size",  ((Integer) arch.get("size")).intValue() + 1);
        }

        request.setAttribute("packageList", validList);
        request.setAttribute(ARCH_COUNT, new ArrayList(archMap.values()));
        request.setAttribute("totalSize", validList.size());


        ListTagHelper.bindSetDeclTo("errata", getSetDecl(currentChan), request);



        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }


    protected RhnSetDecl getSetDecl(Channel chan) {
        return RhnSetDecl.setForChannelErrata(chan);
    }





    private void storePackagesInSet(User user,  List<PackageOverview> packList,
            Channel chan) {

        RhnSet set =  RhnSetDecl.setForChannelPackages(chan).get(user);
        set.clear();

        for (PackageOverview pack : packList) {
            set.addElement(pack.getId());
        }
        RhnSetManager.store(set);


    }


}
