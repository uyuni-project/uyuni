/*
 * Copyright (c) 2009--2013 Red Hat, Inc.
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
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.manager.kickstart.BaseKickstartCommand;
import com.redhat.rhn.manager.kickstart.KickstartTroubleshootingCommand;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.struts.action.DynaActionForm;

import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Handles display and update of {@literal Kickstart -> System Details -> Troubleshooting}
 *
 */
public class KickstartTroubleshootingEditAction extends BaseKickstartEditAction {

    public static final String BOOTLOADER_OPTIONS = "bootloaders";
    public static final String KERNEL_PARAMS = "kernelParams";
    public static final String BOOTLOADER = "bootloader";
    public static final String UPDATE_METHOD
        = "kickstart.troubleshooting.jsp.updatekickstart";
    public static final String NONCHROOTPOST = "nonChrootPost";
    public static final String VERBOSEUP2DATE = "verboseUp2date";

    /**
     *
     * {@inheritDoc}
     */
    @Override
    protected void setupFormValues(RequestContext ctx, DynaActionForm form,
                                   BaseKickstartCommand cmdIn) {
        KickstartTroubleshootingCommand cmd = (KickstartTroubleshootingCommand) cmdIn;

        ctx.getRequest().setAttribute(BOOTLOADER_OPTIONS, List.of(Map.of("display", "GRUB", "value", "grub")));

        form.set(BOOTLOADER, cmd.getBootloaderType());
        form.set(KERNEL_PARAMS, cmd.getKernelParams());
        form.set(NONCHROOTPOST, cmd.getNonChrootPost());
        form.set(VERBOSEUP2DATE, cmd.getVerboseUp2date());
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    protected ValidatorError processFormValues(HttpServletRequest request,
                                               DynaActionForm form,
                                               BaseKickstartCommand cmd) {

        ValidatorError retval = null;

        KickstartTroubleshootingCommand tscmd = (KickstartTroubleshootingCommand) cmd;
        tscmd.setBootloaderType(form.getString(BOOTLOADER));

        String kernelParams = form.getString(KERNEL_PARAMS);
        if (kernelParams.length() > 2048) {
            retval = new ValidatorError("kickstart.troubleshooting." +
                                        "validation.kernelparams.too_long");
        }

        tscmd.setKernelParams(form.getString(KERNEL_PARAMS));

        tscmd.getKickstartData().setNonChrootPost(
                BooleanUtils.toBoolean((Boolean) form.get(NONCHROOTPOST)));

        tscmd.getKickstartData().setVerboseUp2date(
                BooleanUtils.toBoolean((Boolean) form.get(VERBOSEUP2DATE)));

        return retval;
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    protected String getSuccessKey() {
        return "kickstart.troubleshooting.success";
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    protected BaseKickstartCommand getCommand(RequestContext ctx) {
        return new KickstartTroubleshootingCommand(
                ctx.getRequiredParam(RequestContext.KICKSTART_ID),
                ctx.getCurrentUser());
    }
}
