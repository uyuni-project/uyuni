/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.kickstart;

import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.common.validator.ValidatorResult;
import com.redhat.rhn.domain.kickstart.KickstartScript;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnValidationHelper;
import com.redhat.rhn.manager.kickstart.BaseKickstartCommand;
import com.redhat.rhn.manager.kickstart.BaseKickstartScriptCommand;

import org.apache.struts.action.DynaActionForm;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * KickstartScriptCreateAction action for creating a new kickstart script
 */
public abstract class BaseKickstartScriptAction extends BaseKickstartEditAction {

    public static final String SCRIPTNAME = "script_name";
    public static final String CONTENTS = "contents";
    public static final String LANGUAGE = "language";
    public static final String TYPE = "type";
    public static final String TYPES = "types";
    public static final String NOCHROOT = "nochroot";
    public static final String ERRORONFAIL = "erroronfail";
    public static final String TEMPLATE = "template";

    /**
     * {@inheritDoc}
     */
    @Override
    protected ValidatorError processFormValues(HttpServletRequest request,
                                               DynaActionForm form,
                                               BaseKickstartCommand cmd) {

        BaseKickstartScriptCommand kssc = (BaseKickstartScriptCommand) cmd;

        Boolean template = false;
        if (form.get(TEMPLATE) != null) {
            template = (Boolean) form.get(TEMPLATE);
        }

        Boolean errorOnFail = false;
        if (form.get(ERRORONFAIL) != null) {
            errorOnFail = (Boolean) form.get(ERRORONFAIL);
        }

        String chroot = "Y";
        if (Boolean.TRUE.equals(form.get(NOCHROOT))) {
            chroot = "N";
        }

        String scriptValue = getStrutsDelegate().getTextAreaValue(form, CONTENTS);

        ValidatorResult result = RhnValidationHelper.validate(this.getClass(), form, null,
                "validation/" + form.getDynaClass().getName() + ".xsd");

        if (!result.isEmpty()) {
            request.setAttribute(LANGUAGE, form.getString(LANGUAGE));
            request.setAttribute(SCRIPTNAME, form.getString(SCRIPTNAME));
            request.setAttribute(CONTENTS, scriptValue);
            request.setAttribute(TYPE, form.getString(TYPE));
            request.setAttribute(NOCHROOT, form.get(NOCHROOT));
            request.setAttribute(ERRORONFAIL, form.get(ERRORONFAIL));
            request.setAttribute(TEMPLATE, form.get(TEMPLATE));
            return result.getErrors().get(0);
        }

        kssc.setScript(form.getString(LANGUAGE), scriptValue, form.getString(TYPE), chroot,
                template, form.getString(SCRIPTNAME), errorOnFail);
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getSuccessKey() {
        return "kickstart.script.success";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setupFormValues(RequestContext ctx, DynaActionForm form,
                                   BaseKickstartCommand cmd) {
        List types = new LinkedList<>();
        types.add(lvl10n("kickstart.script.pre", KickstartScript.TYPE_PRE));
        types.add(lvl10n("kickstart.script.post", KickstartScript.TYPE_POST));
        ctx.getRequest().setAttribute(TYPES, types);

        BaseKickstartScriptCommand kssc = (BaseKickstartScriptCommand) cmd;

        if (kssc.getScript().getId() == null) {
            HttpServletRequest req = ctx.getRequest();
            form.set(CONTENTS, req.getAttribute(CONTENTS));
            form.set(SCRIPTNAME, req.getAttribute(SCRIPTNAME));
            form.set(LANGUAGE, req.getAttribute(LANGUAGE));
            form.set(TYPE, req.getAttribute(TYPE));
            form.set(NOCHROOT, req.getAttribute(NOCHROOT));
            form.set(ERRORONFAIL, req.getAttribute(ERRORONFAIL));
            form.set(TEMPLATE, req.getAttribute(TEMPLATE));

        }
        else {
            form.set(CONTENTS, kssc.getContents());
            form.set(SCRIPTNAME, kssc.getScriptName());
            form.set(LANGUAGE, kssc.getLanguage());
            form.set(TYPE, kssc.getType());
            form.set(NOCHROOT, kssc.getNoChrootVal());
            form.set(ERRORONFAIL, kssc.getErrorOnFail());
            form.set(TEMPLATE, !kssc.getScript().getRaw());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getSuccessForward() {
        return "success";
    }
}
