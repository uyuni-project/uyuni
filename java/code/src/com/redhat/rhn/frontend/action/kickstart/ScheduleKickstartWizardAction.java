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
package com.redhat.rhn.frontend.action.kickstart;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.util.DatePicker;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.kickstart.KickstartCommand;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartableTree;
import com.redhat.rhn.domain.rhnpackage.profile.Profile;
import com.redhat.rhn.domain.server.NetworkInterface;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerNetAddress4;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.systems.sdc.SdcHelper;
import com.redhat.rhn.frontend.dto.OrgProxyServer;
import com.redhat.rhn.frontend.dto.ProfileDto;
import com.redhat.rhn.frontend.dto.kickstart.KickstartDto;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnValidationHelper;
import com.redhat.rhn.frontend.struts.wizard.RhnWizardAction;
import com.redhat.rhn.frontend.struts.wizard.WizardStep;
import com.redhat.rhn.frontend.taglibs.list.ListTagHelper;
import com.redhat.rhn.frontend.taglibs.list.helper.ListHelper;
import com.redhat.rhn.frontend.taglibs.list.helper.Listable;
import com.redhat.rhn.manager.kickstart.KickstartScheduleCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerSystemCreateCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerXMLRPCHelper;
import com.redhat.rhn.manager.system.SystemManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.DynaActionForm;
import org.apache.struts.util.LabelValueBean;
import org.cobbler.CobblerConnection;
import org.cobbler.CobblerObject;
import org.cobbler.Distro;
import org.cobbler.SystemRecord;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * blah blah
 *
 * @version $Rev $
 */
public class ScheduleKickstartWizardAction extends RhnWizardAction {

    /**
     * Logger for this class
     */
    private static Logger log = Logger
            .getLogger(ScheduleKickstartWizardAction.class);

    public static final String SYNCH_PACKAGES = "syncPackages";
    public static final String SYNCH_SYSTEMS = "syncSystems";
    public static final String HAS_PROFILES = "hasProfiles";
    public static final String HAS_PROXIES = "hasProxies";
    public static final String SYNC_PACKAGE_DISABED = "syncPackageDisabled";
    public static final String SYNC_SYSTEM_DISABLED = "syncSystemDisabled";
    public static final String PROXIES = "proxies";
    public static final String CNAMES = "cnames";
    public static final String VALID_CNAMES = "valid_cnames_";
    public static final String KERNEL_PARAMS = "kernelParams";
    public static final String KERNEL_PARAMS_TYPE = "kernelParamsType";
    public static final String KERNEL_PARAMS_DISTRO = "distro";
    public static final String KERNEL_PARAMS_PROFILE = "profile";
    public static final String KERNEL_PARAMS_CUSTOM = "custom";
    private static final String COBBLER_ONLY_PROFILE = "cobblerOnlyProfile";
    public static final String POST_KERNEL_PARAMS = "postKernelParams";
    public static final String POST_KERNEL_PARAMS_TYPE = "postKernelParamsType";
    public static final String PROXY_HOST = "proxyHost";
    public static final String PROXY_HOST_CNAME = "proxyHostCname";
    public static final String IS_VIRTUAL_GUEST = "isVirtualGuest";
    public static final String INVALID_CONTACT_METHOD = "invalidContactMethod";
    public static final String HOST_SID = "hostSid";
    public static final String VIRT_HOST_IS_REGISTERED = "virtHostIsRegistered";
    public static final String TARGET_PROFILE_TYPE = "targetProfileType";
    public static final String NETWORK_TYPE = "networkType";
    public static final String NETWORK_INTERFACE = "networkInterface";
    public static final String NETWORK_INTERFACES = "networkInterfaces";
    public static final String ALL_NETWORK_INTERFACES = "allNetworkInterfaces";
    public static final String USE_IPV6_GATEWAY = "useIpv6Gateway";
    public static final String BOND_TYPE = "bondType";
    public static final String BOND_INTERFACE = "bondInterface";
    public static final String BOND_SLAVE_INTERFACES = "bondSlaveInterfaces";
    public static final String HIDDEN_BOND_SLAVE_INTERFACES = "hiddenBondSlaveInterfaces";
    public static final String BOND_STATIC = "bondStatic";
    public static final String BOND_IP_ADDRESS = "bondAddress";
    public static final String BOND_NETMASK = "bondNetmask";
    public static final String BOND_GATEWAY = "bondGateway";
    public static final String BOND_OPTIONS = "bondOptions";
    public static final String CREATE_BOND_VALUE = "bonding";
    public static final String STATIC_BOND_VALUE = "true";
    public static final String DONT_CREATE_BOND_VALUE = "none";
    public static final String DESTROY_DISKS = "destroyDisks";
    public static final String NEXT_ACTION = "wizardStep";
    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateWizardSteps(Map wizardSteps) {
        List<Method> methods = findMethods("run");
        for (Iterator<Method> iter = methods.iterator(); iter.hasNext();) {
            Method m = iter.next();
            String stepName = m.getName().substring(3).toLowerCase();
            WizardStep wizStep = new WizardStep();
            wizStep.setWizardMethod(m);
            log.debug("Step name: " + stepName);
            if (stepName.equals("first")) {
                wizStep.setNext("second");
                wizardSteps.put(RhnWizardAction.STEP_START, wizStep);
            }
            else if (stepName.equals("second")) {
                wizStep.setPrevious("first");
                wizStep.setNext("third");
            }
            else if (stepName.equals("third")) {
                wizStep.setPrevious("second");
            }
            else if (stepName.equals("fourth")) {
                wizStep.setPrevious("first");
            }
            else if (stepName.equals("fifth")) {
                wizStep.setPrevious("first");
            }
            wizardSteps.put(stepName, wizStep);
        }
    }

    private class Profiles implements Listable<KickstartDto> {

        /**
         * {@inheritDoc}
         */
        public List<KickstartDto> getResult(RequestContext ctx) {
            Long sid = ctx.getParamAsLong(RequestContext.SID);
            User user = ctx.getCurrentUser();

            KickstartScheduleCommand cmd = getKickstartScheduleCommand(sid,
                    user);
            DataResult<KickstartDto> profiles = cmd.getKickstartProfiles();
            if (profiles.size() == 0) {
                addMessage(ctx.getRequest(), "kickstart.schedule.noprofiles");
                ctx.getRequest().setAttribute(HAS_PROFILES,
                        Boolean.FALSE.toString());
            }
            else {
                ctx.getRequest().setAttribute(HAS_PROFILES,
                        Boolean.TRUE.toString());
            }
            return profiles;
        }
    }


    /**
     * Sets up the proxy information for the wizard.
     * its public in this class because we reuse this in SSM
     * and only this class knows how to format the name nicely.
     * @param ctx the request context needed for user info and
     *                   things to bind to the request
     */
    public static void setupProxyInfo(RequestContext ctx) {
        List<OrgProxyServer> proxies = SystemManager.
                listProxies(ctx.getCurrentUser().getOrg());
        if (proxies != null && proxies.size() > 0) {
            List<LabelValueBean> formatted = new LinkedList<LabelValueBean>();

            formatted.add(lvl10n("kickstart.schedule.default.proxy.jsp", ""));
            Map<String, List<String>> cnames = new HashMap<String, List<String>>();
            for (OrgProxyServer serv : proxies) {
                formatted.add(lv(serv.getName() + " (" + serv.getCheckin() + ")",
                        serv.getId().toString()));
                List<String> proxyCnames =
                        Config.get().getList(VALID_CNAMES +
                        serv.getId().toString());
                if (!proxyCnames.isEmpty()) {
                    cnames.put(serv.getId().toString(), proxyCnames);
                }
            }
            ctx.getRequest().setAttribute(HAS_PROXIES, Boolean.TRUE.toString());
            ctx.getRequest().setAttribute(PROXIES, formatted);
            ctx.getRequest().setAttribute(CNAMES, cnames);
        }
        else {
            ctx.getRequest().setAttribute(HAS_PROXIES, Boolean.FALSE.toString());
        }
    }

    private void setupBondInfo(DynaActionForm form, RequestContext context,
            KickstartScheduleCommand cmd) {
        Server server = cmd.getServer();
        List<NetworkInterface> nics = new LinkedList<NetworkInterface>
        (server.getNetworkInterfaces());

        if (nics.isEmpty()) {
            return;
        }

        for (Iterator<NetworkInterface> itr = nics.iterator(); itr.hasNext();) {
            NetworkInterface nic = itr.next();
            for (ServerNetAddress4 addr : nic.getIPv4Addresses()) {
                if ("127.0.0.1".equals(addr.getAddress())) {
                    itr.remove();
                }
            }
        }

        context.getRequest().setAttribute(ALL_NETWORK_INTERFACES, nics);

        if (StringUtils.isBlank(form.getString(BOND_TYPE))) {
            form.set(BOND_TYPE, DONT_CREATE_BOND_VALUE);
        }

        NetworkInterface oldBond = null;
        for (NetworkInterface nic : nics) {
            if (nic.isBond()) {
                oldBond = nic;
                break;
            }
        }

        if (oldBond != null) {
            if (StringUtils.isBlank(form.getString(BOND_INTERFACE))) {
                form.set(BOND_INTERFACE, oldBond.getName());
            }

            if (StringUtils.isBlank(form.getString(BOND_IP_ADDRESS))) {
                form.set(BOND_IP_ADDRESS, oldBond.getIPv4Addresses().isEmpty() ?
                        null : oldBond.getIPv4Addresses().stream().findFirst()
                        .map(ServerNetAddress4::getAddress).orElse(null));
            }

            if (StringUtils.isBlank(form.getString(BOND_NETMASK))) {
                form.set(BOND_NETMASK, oldBond.getIPv4Addresses().isEmpty() ?
                        null : oldBond.getIPv4Addresses().stream().findFirst()
                        .map(ServerNetAddress4::getNetmask).orElse(null));
            }
        }

        String[] slaves = (String[]) form.get(BOND_SLAVE_INTERFACES);
        if (slaves == null || slaves.length == 0) {
            List<String> slavesList = new ArrayList<String>();
            // if there is a bonded interface on the system
            if (!StringUtils.isBlank(form.getString(BOND_INTERFACE))) {
                for (NetworkInterface nic : nics) {
                    // if the nic does not have an IP address it is probably a
                    // slave to the bond, add it to the default selected list
                    if (nic.getIPv4Addresses().isEmpty()) {
                        slavesList.add(nic.getName());
                    }
                }
            }

            form.set(BOND_SLAVE_INTERFACES,
                    convertToStringArray(slavesList.toArray()));
        }
    }

    /*
     * Throwing an error when casting from Object[] to String [], so let's do it
     * manually
     */
    private String[] convertToStringArray(Object[] objects) {
        String[] strings = new String[objects.length];
        int i = 0;
        for (Object object : objects) {
            strings[i] = (String) object;
            i++;
        }
        return strings;
    }

    private List<NetworkInterface> getPublicNetworkInterfaces(
            Server server) {
        List<NetworkInterface> nics = new LinkedList<NetworkInterface>(
                server.getNetworkInterfaces());

        for (Iterator<NetworkInterface> itr = nics.iterator(); itr.hasNext();) {
            NetworkInterface nic = itr.next();
            if (nic.isDisabled() || nic.getIPv4Addresses().isEmpty() ||
                    nic.getIPv4Addresses().get(0).getAddress().equals("127.0.0.1")) {
                itr.remove();
            }
        }

        return nics;
    }

    private void setupNetworkInfo(DynaActionForm form, RequestContext context,
            KickstartScheduleCommand cmd) {
        Server server = cmd.getServer();
        List<NetworkInterface> nics = getPublicNetworkInterfaces(server);

        if (nics.isEmpty()) {
            return;
        }

        context.getRequest().setAttribute(NETWORK_INTERFACES, nics);

        if (StringUtils.isBlank(form.getString(NETWORK_INTERFACE))) {
            String defaultInterface = ConfigDefaults.get().
                    getDefaultKickstartNetworkInterface();
            for (NetworkInterface nic : nics) {
                if (nic.getName().equals(defaultInterface)) {
                    form.set(NETWORK_INTERFACE, ConfigDefaults.get().
                            getDefaultKickstartNetworkInterface());
                }
            }
            if (StringUtils.isBlank(form.getString(NETWORK_INTERFACE))) {
                form.set(NETWORK_INTERFACE, server.
                        findPrimaryNetworkInterface().getName());
            }
        }
    }

    /**
     * The first step in the wizard
     * @param mapping ActionMapping for struts
     * @param form DynaActionForm representing the form
     * @param ctx RequestContext request context
     * @param response HttpServletResponse response object
     * @param step WizardStep what step are we on?
     *
     * @return ActionForward struts action forward
     * @throws Exception if something goes amiss
     */
    public ActionForward runFirst(ActionMapping mapping, DynaActionForm form,
            RequestContext ctx, HttpServletResponse response, WizardStep step)
                    throws Exception {
        log.debug("runFirst");
        Long sid = (Long) form.get(RequestContext.SID);
        User user = ctx.getCurrentUser();

        KickstartScheduleCommand cmd = getKickstartScheduleCommand(sid, user);

        Server system = SystemManager.lookupByIdAndUser(sid, user);
        if (system.isVirtualGuest() &&
                VirtualInstanceFactory.getInstance().getParaVirtType().equals(
                        system.getVirtualInstance().getType())) {
            ctx.getRequest().setAttribute(IS_VIRTUAL_GUEST,
                    Boolean.TRUE.toString());

            ctx.getRequest().setAttribute(VIRT_HOST_IS_REGISTERED,
                    Boolean.FALSE.toString());
            if (system.getVirtualInstance().getHostSystem() != null) {
                Long hostSid = system.getVirtualInstance().getHostSystem()
                        .getId();
                ctx.getRequest().setAttribute(VIRT_HOST_IS_REGISTERED,
                        Boolean.TRUE.toString());
                ctx.getRequest().setAttribute(HOST_SID, hostSid);
            }
        }
        else {
            ctx.getRequest().setAttribute(IS_VIRTUAL_GUEST,
                    Boolean.FALSE.toString());
        }

        addRequestAttributes(ctx, cmd, form);

        // Return directly if the contact method is invalid
        if (system.getContactMethod().getLabel().equals("ssh-push-tunnel")) {
            ctx.getRequest().setAttribute(INVALID_CONTACT_METHOD,
                    Boolean.TRUE);
            return mapping.findForward("first");
        }
        else {
            ctx.getRequest().setAttribute(INVALID_CONTACT_METHOD,
                    Boolean.FALSE);
        }

        checkForKickstart(form, cmd, ctx);
        setupProxyInfo(ctx);
        if (StringUtils.isBlank(form.getString(PROXY_HOST))) {
            form.set(PROXY_HOST, "");
        }
        // create and prepopulate the date picker.
        getStrutsDelegate().prepopulateDatePicker(
                ctx.getRequest(), form, "date", DatePicker.YEAR_RANGE_POSITIVE);

        SdcHelper.ssmCheck(ctx.getRequest(), system.getId(), user);
        Map<String, Long> params = new HashMap<String, Long>();
        params.put(RequestContext.SID, sid);
        ListHelper helper = new ListHelper(new Profiles(), ctx.getRequest(),
                params);
        helper.execute();
        if (!StringUtils.isBlank(form.getString(RequestContext.COBBLER_ID))) {
            ListTagHelper.selectRadioValue(ListHelper.LIST,
                    form.getString(RequestContext.COBBLER_ID), ctx.getRequest());
        }
        else if (system.getCobblerId() != null) {
            //if nothing is selected by the user yet, use the cobbler
            //  system record to pre-select something.
            SystemRecord rec = SystemRecord.lookupById(
                    CobblerXMLRPCHelper.getConnection(
                            ConfigDefaults.get().getCobblerAutomatedUser()),
                            system.getCobblerId());
            if (rec != null) {
                org.cobbler.Profile profile = rec.getProfile();
                if (profile != null) {
                    ListTagHelper.selectRadioValue(ListHelper.LIST,
                        profile.getId(), ctx.getRequest());
                }
            }
        }

        // display a warning if displaying ppc64le profiles to a ppc system
        Channel baseChannel = system.getBaseChannel();
        if (baseChannel != null &&
                baseChannel.getChannelArch().getLabel().equals("channel-ppc")) {
            List<KickstartDto> profiles = helper.getDataSet();
            for (KickstartDto profile : profiles) {
                KickstartableTree tree = KickstartFactory.findTreeById(profile
                        .getKstreeId(), user.getOrg().getId());
                if (tree.getChannel().getChannelArch().getLabel()
                        .equals("channel-ppc64le")) {
                    addMessage(ctx.getRequest(), "kickstart.schedule.ppc64lewarning");
                    break;
                }
            }

        }

        return mapping.findForward("first");
    }

    /**
     * The second step in the wizard
     * @param mapping ActionMapping for struts
     * @param form DynaActionForm representing the form
     * @param ctx RequestContext request context
     * @param response HttpServletResponse response object
     * @param step WizardStep what step are we on?
     *
     * @return ActionForward struts action forward
     * @throws Exception if something goes amiss
     */
    public ActionForward runSecond(ActionMapping mapping, DynaActionForm form,
            RequestContext ctx, HttpServletResponse response, WizardStep step)
                    throws Exception {
        log.debug("runSecond");

        if (!StringUtils.isBlank(form.getString(HIDDEN_BOND_SLAVE_INTERFACES))) {
            form.set(BOND_SLAVE_INTERFACES,
                    form.getString(HIDDEN_BOND_SLAVE_INTERFACES).split(","));
        }

        Long sid = (Long) form.get(RequestContext.SID);
        User user = ctx.getCurrentUser();

        if (!validateFirstSelections(form, ctx)) {
            return runFirst(mapping, form, ctx, response, step);
        }
        KickstartScheduleCommand cmd = getScheduleCommand(form, ctx, null, null);

        checkForKickstart(form, cmd, ctx);
        addRequestAttributes(ctx, cmd, form);
        if (!cmd.isCobblerOnly()) {
            List<ProfileDto> packageProfiles = cmd.getProfiles();
            form.set(SYNCH_PACKAGES, packageProfiles);
            List<Map<String, Object>> systemProfiles = cmd.getCompatibleSystems();
            form.set(SYNCH_SYSTEMS, systemProfiles);

            // Disable the package/system sync radio buttons if no profiles are
            // available:
            String syncPackageDisabled = "false";
            if (packageProfiles.size() == 0) {
                syncPackageDisabled = "true";
            }
            String syncSystemDisabled = "false";
            if (systemProfiles.size() == 0) {
                syncSystemDisabled = "true";
            }
            ctx.getRequest()
            .setAttribute(SYNC_PACKAGE_DISABED, syncPackageDisabled);
            ctx.getRequest().setAttribute(SYNC_SYSTEM_DISABLED, syncSystemDisabled);

            if (StringUtils.isEmpty(form.getString(TARGET_PROFILE_TYPE))) {
                form.set(TARGET_PROFILE_TYPE,
                        KickstartScheduleCommand.TARGET_PROFILE_TYPE_NONE);
            }
        }
        else {
            ctx.getRequest().setAttribute(COBBLER_ONLY_PROFILE, Boolean.TRUE);
        }

        if (StringUtils.isEmpty(form.getString(KERNEL_PARAMS_TYPE))) {
            form.set(KERNEL_PARAMS_TYPE, KERNEL_PARAMS_DISTRO);
        }

        if (StringUtils.isEmpty(form.getString(POST_KERNEL_PARAMS_TYPE))) {
            form.set(POST_KERNEL_PARAMS_TYPE, KERNEL_PARAMS_DISTRO);
        }

        SdcHelper.ssmCheck(ctx.getRequest(), sid, user);
        return mapping.findForward("second");
    }

    protected void addRequestAttributes(RequestContext ctx,
            KickstartScheduleCommand cmd, DynaActionForm form) {
        ctx.getRequest().setAttribute(RequestContext.SYSTEM, cmd.getServer());
        ctx.getRequest()
        .setAttribute(RequestContext.KICKSTART, cmd.getKsdata());
        if (cmd.getKsdata() != null) {
            ctx.getRequest().setAttribute("profile", cmd.getKsdata());
            ctx.getRequest().setAttribute("distro", cmd.getKsdata().getTree());
            CobblerConnection con = CobblerXMLRPCHelper.
                    getConnection(ctx.getCurrentUser());

            Distro distro = Distro.lookupById(con,
                    cmd.getKsdata().getTree().getCobblerId());

            ctx.getRequest().setAttribute("distro_kernel_params",
                    distro.getKernelOptionsString());
            ctx.getRequest().setAttribute("distro_post_kernel_params",
                    distro.getKernelOptionsPostString());

            org.cobbler.Profile profile = org.cobbler.Profile.
                    lookupById(con, cmd.getKsdata().getCobblerId());
            ctx.getRequest().setAttribute("profile_kernel_params",
                    profile.getKernelOptionsString());
            ctx.getRequest().setAttribute("profile_post_kernel_params",
                    profile.getKernelOptionsPostString());
            if (cmd.getServer().getCobblerId() != null) {
                SystemRecord rec = SystemRecord.
                        lookupById(con, cmd.getServer().getCobblerId());
                if (rec != null && rec.getProfile() != null &&
                    profile.getName().equals(rec.getProfile().getName())) {
                    if (StringUtils.isBlank(form.getString(KERNEL_PARAMS_TYPE))) {
                        form.set(KERNEL_PARAMS_TYPE, KERNEL_PARAMS_CUSTOM);
                        form.set(KERNEL_PARAMS, rec.getKernelOptionsString());
                    }
                    if (StringUtils.isBlank(form.getString(POST_KERNEL_PARAMS_TYPE))) {
                        form.set(POST_KERNEL_PARAMS_TYPE, KERNEL_PARAMS_CUSTOM);
                        form.set(POST_KERNEL_PARAMS, rec.getKernelOptionsPostString());
                    }
                }
            }
        }
        setupNetworkInfo(form, ctx, cmd);
        setupBondInfo(form, ctx, cmd);
    }



    /**
     * The third step in the wizard
     * @param mapping ActionMapping for struts
     * @param form DynaActionForm representing the form
     * @param ctx RequestContext request context
     * @param response HttpServletResponse response object
     * @param step WizardStep what step are we on?
     *
     * @return ActionForward struts action forward
     * @throws Exception if something goes amiss
     */
    public ActionForward runThird(ActionMapping mapping, DynaActionForm form,
            RequestContext ctx, HttpServletResponse response, WizardStep step)
                    throws Exception {
        log.debug("runThird");
        if (!validateBondSelections(form, ctx)) {
            return runSecond(mapping, form, ctx, response, step);
        }
        if (!validateFirstSelections(form, ctx)) {
            return runFirst(mapping, form, ctx, response, step);
        }
        String scheduleAsap = form.getString("scheduleAsap");
        Date scheduleTime = null;
        if (scheduleAsap != null && scheduleAsap.equals("false")) {
            scheduleTime = getStrutsDelegate().readScheduleDate(form, "date",
                    DatePicker.YEAR_RANGE_POSITIVE);
        }
        else {
            scheduleTime = new Date();
        }
        KickstartHelper helper = new KickstartHelper(ctx.getRequest());
        KickstartScheduleCommand cmd = getScheduleCommand(form, ctx,
                scheduleTime, helper.getKickstartHost());

        if (showDiskWarning(cmd.getKsdata(), form)) {
            form.set(NEXT_ACTION, "third");
            addRequestAttributes(ctx, cmd, form);
            return mapping.findForward("fifth");
        }
        cmd.setNetworkDevice(form.getString(NETWORK_TYPE),
                form.getString(NETWORK_INTERFACE));

        if (CREATE_BOND_VALUE.equals(form.getString(BOND_TYPE))) {
            cmd.setCreateBond(true);
            cmd.setBondInterface(form.getString(BOND_INTERFACE));
            cmd.setBondOptions(form.getString(BOND_OPTIONS));
            String[] slaves = (String[]) form.get(BOND_SLAVE_INTERFACES);
            List<String> tmp = new ArrayList<String>();
            for (String slave : slaves) {
                tmp.add(slave);
            }
            cmd.setBondSlaveInterfaces(tmp);
            if (STATIC_BOND_VALUE.equals(form.getString(BOND_STATIC))) {
                cmd.setBondDhcp(false);
                cmd.setBondAddress(form.getString(BOND_IP_ADDRESS));
                cmd.setBondNetmask(form.getString(BOND_NETMASK));
                cmd.setBondGateway(form.getString(BOND_GATEWAY));
            }
            else {
                cmd.setBondDhcp(true);
            }
        }

        if (form.getString(USE_IPV6_GATEWAY).equals("1")) {
            cmd.setIpv6Gateway();
        }
        cmd.setKernelOptions(parseKernelOptions(form, ctx.getRequest(),
                form.getString(RequestContext.COBBLER_ID), false));
        cmd.setPostKernelOptions(parseKernelOptions(form, ctx.getRequest(),
                form.getString(RequestContext.COBBLER_ID), true));

        if (!cmd.isCobblerOnly()) {
            // now setup system/package profiles for kickstart to sync
            Profile pkgProfile = cmd.getKsdata().getKickstartDefaults()
                    .getProfile();
            Long packageProfileId = pkgProfile != null ? pkgProfile.getId() : null;

            // if user did not override package profile, then grab from ks
            // profile if avail
            if (packageProfileId != null) {
                cmd.setProfileId(packageProfileId);
                cmd.setProfileType(KickstartScheduleCommand.TARGET_PROFILE_TYPE_PACKAGE);
            }
            else {
                /*
                 * NOTE: these values are essentially ignored if user did not go
                 * through advanced config and there is no package profile to
                 * sync in the kickstart profile
                 */
                cmd.setProfileType(form.getString(TARGET_PROFILE_TYPE));
                cmd.setServerProfileId((Long) form.get("targetServerProfile"));
                cmd.setProfileId((Long) form.get("targetProfile"));
            }
        }

        storeProxyInfo(form, ctx, cmd);

        // Store the new KickstartSession to the DB.
        ValidatorError ve = cmd.store();
        if (ve != null) {
            ActionErrors errors = RhnValidationHelper
                    .validatorErrorToActionErrors(ve);
            if (!errors.isEmpty()) {
                getStrutsDelegate().saveMessages(ctx.getRequest(), errors);
                return runFirst(mapping, form, ctx, response, step);
            }
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(RequestContext.SID, form.get(RequestContext.SID));

        if (cmd.isCobblerOnly()) {
            createSuccessMessage(ctx.getRequest(),
                    "kickstart.cobbler.schedule.success", LocalizationService
                    .getInstance().formatDate(scheduleTime));
            return getStrutsDelegate().forwardParams(
                    mapping.findForward("cobbler-success"), params);
        }
        createSuccessMessage(ctx.getRequest(), "kickstart.schedule.success",
                LocalizationService.getInstance().formatDate(scheduleTime));
        return getStrutsDelegate().forwardParams(
                mapping.findForward("success"), params);
    }

    /**
     * Setup the system for provisioning with cobbler.
     *
     * @param mapping ActionMapping for struts
     * @param form DynaActionForm representing the form
     * @param ctx RequestContext request context
     * @param response HttpServletResponse response object
     * @param step WizardStep what step are we on?
     *
     * @return ActionForward struts action forward
     * @throws Exception if something goes amiss
     */
    public ActionForward runFourth(ActionMapping mapping, DynaActionForm form,
            RequestContext ctx, HttpServletResponse response, WizardStep step)
                    throws Exception {

        log.debug("runFourth");
        if (!validateFirstSelections(form, ctx)) {
            return runFirst(mapping, form, ctx, response, step);
        }
        Long sid = (Long) form.get(RequestContext.SID);
        String cobblerId = form.getString(RequestContext.COBBLER_ID);

        log.debug("runFourth.cobblerId: " + cobblerId);

        User user = ctx.getCurrentUser();
        Server server = SystemManager.lookupByIdAndUser(sid, user);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put(RequestContext.SID, sid);

        log.debug("Creating cobbler system record");
        org.cobbler.Profile profile = org.cobbler.Profile.lookupById(
                CobblerXMLRPCHelper.getConnection(user), cobblerId);

        KickstartData data = KickstartFactory.lookupKickstartDataByCobblerIdAndOrg(
                user.getOrg(), profile.getUid());

        if (showDiskWarning(data, form)) {
            form.set(NEXT_ACTION, "fourth");
            ctx.getRequest().setAttribute(RequestContext.SYSTEM, server);
            return mapping.findForward("fifth");
        }

        CobblerSystemCreateCommand cmd = new CobblerSystemCreateCommand(server,
                profile.getName(), data);
        cmd.store();
        log.debug("cobbler system record created.");
        String[] args = new String[2];
        String msgKey = server.isBootstrap() ?
            "kickstart.schedule.cobblercreate.bootstrap" :
            "kickstart.schedule.cobblercreate";
        args[0] = server.getName();
        args[1] = profile.getName();
        createMessage(ctx.getRequest(), msgKey, args);

        return getStrutsDelegate().forwardParams(
                mapping.findForward("cobbler-success"), params);
    }

    /**
     * Returns the kickstart schedule command
     * @param form the dyna aciton form
     * @param ctx the request context
     * @param scheduleTime the schedule time
     * @param host the host url.
     * @return the Ks schedule command
     */
    protected KickstartScheduleCommand getScheduleCommand(DynaActionForm form,
            RequestContext ctx, Date scheduleTime, String host) {
        String cobblerId = form.getString(RequestContext.COBBLER_ID);
        User user = ctx.getCurrentUser();
        KickstartScheduleCommand cmd;
        KickstartData data = KickstartFactory
                .lookupKickstartDataByCobblerIdAndOrg(user.getOrg(), cobblerId);
        if (data != null) {
            cmd = new KickstartScheduleCommand((Long) form
                    .get(RequestContext.SID), data, ctx.getCurrentUser(),
                    scheduleTime, host);
        }
        else {
            org.cobbler.Profile profile = org.cobbler.Profile.lookupById(
                    CobblerXMLRPCHelper.getConnection(user), cobblerId);
            cmd = KickstartScheduleCommand.createCobblerScheduleCommand(
                    (Long) form.get(RequestContext.SID), profile.getName(),
                    user, scheduleTime, host);
        }
        return cmd;
    }

    /**
     * @param form the form containing the proxy info
     * @param ctx the request context associated to this request
     * @param cmd the kicktstart command to which the proxy info will be
     * copied..
     */
    protected void storeProxyInfo(DynaActionForm form, RequestContext ctx,
            KickstartScheduleCommand cmd) {
        // if we need to go through a proxy, do it here.
        String phost = form.getString(PROXY_HOST);
        String phostCname = form.getString(PROXY_HOST_CNAME);

        if (!StringUtils.isEmpty(phostCname)) {
            cmd.setProxyHost(phostCname);
        }
        else if (!StringUtils.isEmpty(phost)) {
            cmd.setProxy(SystemManager.lookupByIdAndOrg(Long.valueOf(phost), ctx
                    .getCurrentUser().getOrg()));
        }
    }

    protected boolean validateBondSelections(DynaActionForm form,
            RequestContext ctx) {
        if (!StringUtils.isBlank(form.getString(HIDDEN_BOND_SLAVE_INTERFACES))) {
            form.set(BOND_SLAVE_INTERFACES,
                    form.getString(HIDDEN_BOND_SLAVE_INTERFACES).split(","));
        }

        String[] slaves = (String[]) form.get(BOND_SLAVE_INTERFACES);
        ActionErrors errors = new ActionErrors();

        // if we are trying to create a bond but have not specified a name or at
        // least one slave interface
        if (form.getString(BOND_TYPE).equals(CREATE_BOND_VALUE) &&
                (StringUtils.isBlank(form.getString(BOND_INTERFACE)) ||
                        slaves.length < 1 || StringUtils.isBlank(slaves[0]))) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
                    "kickstart.bond.not.defined.jsp"));
        }

        final String ipv4addressPattern = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

        /*
         * The IPv6 address regex was created by Stephen Ryan at Dartware
         * and taken from this forum: http://forums.intermapper.com/viewtopic.php?t=452
         * It is licenced under a Creative Commons Attribution-ShareAlike 3.0 Unported
         * License. We can freely use it in (even in commercial products) as long as
         * we attribute its creation to him, so don't remove this message.
         */
        final String ipv6addressPattern = "^(((?=(?>.*?::)(?!.*::)))(::)?([0-9A-" +
                "F]{1,4}::?){0,5}|([0-9A-F]{1,4}:){6})(\\2([0-9A-F]{1,4}(::?|$))" +
                "{0,2}|((25[0-5]|(2[0-4]|1\\d|[1-9])?\\d)(\\.|$)){4}|[0-9A-F]{1," +
                "4}:[0-9A-F]{1,4})(?<![^:]:|\\.)\\z";

        if (form.getString(BOND_STATIC).equals(STATIC_BOND_VALUE)) {
            String address = form.getString(BOND_IP_ADDRESS);
            String netmask = form.getString(BOND_NETMASK);
            String gateway = form.getString(BOND_GATEWAY);

            if (!address.matches(ipv4addressPattern) &&
                    !address.matches(ipv6addressPattern)) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
                        "kickstart.bond.bad.ip.address.jsp"));
            }

            if (!netmask.matches(ipv4addressPattern) &&
                    !netmask.matches(ipv6addressPattern)) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
                        "kickstart.bond.bad.netmask.jsp"));
            }

            if (!gateway.matches(ipv4addressPattern) &&
                    !gateway.matches(ipv6addressPattern)) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
                        "kickstart.bond.bad.ip.address.jsp"));
            }
        }

        if (errors.size() > 0) {
            addErrors(ctx.getRequest(), errors);
            return false;
        }
        return true;
    }

    protected boolean validateFirstSelections(DynaActionForm form,
            RequestContext ctx) {
        String cobblerId = ListTagHelper.getRadioSelection(ListHelper.LIST,
                ctx.getRequest());
        if (StringUtils.isBlank(cobblerId)) {
            cobblerId = ctx.getParam(RequestContext.COBBLER_ID, true);
        }

        boolean retval = false;
        form.set(RequestContext.COBBLER_ID, cobblerId);
        ctx.getRequest().setAttribute(RequestContext.COBBLER_ID, cobblerId);
        if (form.get("scheduleAsap") != null) {
            retval = true;
        }
        else if (form.get(RequestContext.COBBLER_ID) != null) {
            return true;
        }
        return retval;
    }

    /*
     * If the user is going to clear all partitions from all drives we need to ask
     * him if he's sure first.
     */
    protected boolean showDiskWarning(KickstartData data, DynaActionForm form) {
        Set<KickstartCommand> commands = data.getOptions();
        boolean containsClearpartCommand = false;
        for (KickstartCommand command : commands) {
            if (command.getCommandName() != null &&
                command.getCommandName().getName() != null &&
                command.getCommandName().getName().equals("clearpart")) {
                if (command.getArguments() != null &&
                    command.getArguments().contains("--drives")) {
                    return false;
                }
                    containsClearpartCommand = true;
                    break;
                }
        }

        String diskOption = form.getString(DESTROY_DISKS);
        return containsClearpartCommand && (diskOption == null || !diskOption.equals("true"));
    }

    private void checkForKickstart(DynaActionForm form,
            KickstartScheduleCommand cmd, RequestContext ctx) {
        if (ActionFactory.doesServerHaveKickstartScheduled((Long) form
                .get(RequestContext.SID))) {
            String[] params = { cmd.getServer().getName() };
            getStrutsDelegate().saveMessage(
                    "kickstart.schedule.already.scheduled.jsp", params,
                    ctx.getRequest());
        }
    }

    protected KickstartScheduleCommand getKickstartScheduleCommand(Long sid,
            User currentUser) {
        return new KickstartScheduleCommand(sid, currentUser);
    }

    /**
     * Parses the kernel options or Post kernel options
     * from the given form. Called after the advanced options page
     * is typically set..
     *  This is a handy method used in both SSM and SDC KS scheduling.
     * @param form the kickstartScheduleWizardForm that holds the form fields.
     * @param request the servlet request
     * @param profileCobblerId the cobbler profile id
     * @param isPost true if caller is interested in getting the
     *              post kernel options and not the pre.
     * @return the kernel options selected by the user.
     */
    public static String parseKernelOptions(DynaActionForm form,
            HttpServletRequest request,
            String profileCobblerId,
            boolean isPost) {
        RequestContext context = new RequestContext(request);
        String typeKey = !isPost ? KERNEL_PARAMS_TYPE : POST_KERNEL_PARAMS_TYPE;
        String customKey = !isPost ? KERNEL_PARAMS : POST_KERNEL_PARAMS;
        String type = form.getString(typeKey);

        return parseKernelOptions(form.getString(customKey), type, profileCobblerId,
                isPost, context.getCurrentUser());
    }


    /**
     * Parses the kernel options or Post kernel options
     * from the given set of params
     *  This is a handy method used in both SSM and SDC KS scheduling.
     * @param customOptions the kickstartScheduleWizardForm that holds the form fields.
     * @param paramsType  either KERNEL_PARAMS_CUSTOM _DISTRO or _PROFILE
     * @param cobblerId the cobbler profile id
     * @param isPost true if caller is interested in getting the
     *              post kernel options and not the pre.
     * @param user the user doing the request
     * @return the kernel options selected by the user.
     */
    public static String parseKernelOptions(String customOptions,
            String paramsType,
            String cobblerId,
            boolean isPost, User user) {

        CobblerConnection con  = CobblerXMLRPCHelper.
                getConnection(user);
        if (KERNEL_PARAMS_CUSTOM.equals(paramsType)) {
            return customOptions;
        }
        org.cobbler.Profile profile = org.cobbler.Profile.lookupById(con,
                cobblerId);
        CobblerObject ret = profile;

        if (KERNEL_PARAMS_DISTRO.equals(paramsType)) {
            ret = profile.getDistro();
        }
        if (!isPost) {
            return ret.getKernelOptionsString();
        }
        return ret.getKernelOptionsPostString();

    }
}
