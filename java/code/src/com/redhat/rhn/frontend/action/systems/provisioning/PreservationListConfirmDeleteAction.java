/*
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
package com.redhat.rhn.frontend.action.systems.provisioning;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.common.CommonFactory;
import com.redhat.rhn.domain.common.FileList;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.rhnset.RhnSetElement;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.frontend.struts.BaseSetListAction;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * PreservationListConfirmDeleteAction
 */
public class PreservationListConfirmDeleteAction extends BaseSetListAction {

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataResult<Map<String, Object>> getDataResult(RequestContext rctx, PageControl pc) {

        RhnSet set = getSetDecl().get(rctx.getCurrentUser());
        User user = rctx.getCurrentUser();

        List<Map<String, Object>> selectedFileList = new LinkedList<>();
        for (RhnSetElement elem : set.getElements()) {
            FileList fl = CommonFactory.lookupFileList(elem.getElement(),
                    user.getOrg());
            Map<String, Object> flRow = new HashMap<>();
            flRow.put("label", fl.getLabel());
            flRow.put("id", fl.getId());
            selectedFileList.add(flRow);
        }

        return new DataResult<>(selectedFileList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RhnSetDecl getSetDecl() {
        return RhnSetDecl.FILE_LISTS;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean preClearSet() {
        return false;
    }

}
