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
package com.redhat.rhn.frontend.action.systems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.util.DatePicker;
import com.redhat.rhn.common.util.DynamicComparator;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.dup.DistUpgradeActionDetails;
import com.redhat.rhn.domain.action.dup.DistUpgradeChannelTask;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ClonedChannel;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductSet;
import com.redhat.rhn.domain.product.SUSEProductUpgrade;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ChildChannelDto;
import com.redhat.rhn.frontend.dto.EssentialChannelDto;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.distupgrade.DistUpgradeManager;

/**
 * Action class for scheduling distribution upgrades (Service Pack Migrations).
 */
public class SPMigrationAction extends RhnAction {

    // Request attributes
    private static final String UPGRADE_SUPPORTED = "upgradeSupported";
    private static final String ZYPP_INSTALLED = "zyppPluginInstalled";
    private static final String MIGRATION_SCHEDULED = "migrationScheduled";
    private static final String LATEST_SP = "latestServicePack";
    private static final String TARGET_PRODUCTS = "targetProducts";
    private static final String CHANNEL_MAP = "channelMap";

    // Form parameters
    private static final String ACTION_STEP = "step";
    private static final String SETUP = "setup";
    private static final String CONFIRM = "confirm";
    private static final String SCHEDULE = "schedule";
    private static final String BASE_PRODUCT = "baseProduct";
    private static final String ADDON_PRODUCTS = "addonProducts";
    private static final String BASE_CHANNEL = "baseChannel";
    private static final String CHILD_CHANNELS = "childChannels";

    // Message keys
    private static final String DISPATCH_DRYRUN = "spmigration.jsp.confirm.submit.dry-run";
    private static final String MSG_SCHEDULED_MIGRATION = "spmigration.message.scheduled";
    private static final String MSG_SCHEDULED_DRYRUN = "spmigration.message.scheduled.dry-run";

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionForward execute(ActionMapping actionMapping,
            ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        // Bind the server object to the request
        RequestContext ctx = new RequestContext(request);
        Server server = ctx.lookupAndBindServer();

        // Check if this server supports distribution upgrades
        boolean supported = DistUpgradeManager.isUpgradeSupported(
                server, ctx.getCurrentUser());
        request.setAttribute(UPGRADE_SUPPORTED, supported);

        // Check if zypp-plugin-spacewalk is installed
        boolean zyppPluginInstalled = PackageFactory.lookupByNameAndServer(
                "zypp-plugin-spacewalk", server) != null;
        request.setAttribute(ZYPP_INSTALLED, zyppPluginInstalled);

        // Check if there is already a migration in the schedule
        Action migration = null;
        if (supported) {
            migration = ActionFactory.isMigrationScheduledForServer(server.getId());
        }
        request.setAttribute(MIGRATION_SCHEDULED, migration);

        // Init request parameters
        Long targetBaseProduct = null;
        Long[] targetAddonProducts = null;
        Long targetBaseChannel = null;
        Long[] targetChildChannels = null;
        boolean dryRun = false;

        // Read form parameters if dispatching
        DynaActionForm form = (DynaActionForm) actionForm;
        String actionStep = SETUP;
        String dispatch = request.getParameter(RequestContext.DISPATCH);
        if (dispatch != null) {
            actionStep = (String) form.get(ACTION_STEP);

            // Get target product and channel IDs
            targetBaseProduct = (Long) form.get(BASE_PRODUCT);
            targetAddonProducts = (Long[]) form.get(ADDON_PRODUCTS);
            targetBaseChannel = (Long) form.get(BASE_CHANNEL);
            targetChildChannels = (Long[]) form.get(CHILD_CHANNELS);

            // Get additional flags
            if (dispatch.equals(LocalizationService.getInstance().getMessage(
                    DISPATCH_DRYRUN))) {
                dryRun = true;
            }
        }

        // Find the action forward
        ActionForward forward = findForward(actionMapping, actionStep, dispatch);

        // Put data to the request
        if (forward.getName().equals(SETUP) && supported && migration == null) {
            // Find target products
            SUSEProductSet installedProducts = server.getInstalledProducts();
            List<SUSEProductSet> migrationTargets = DistUpgradeManager.
                    getTargetProductSets(installedProducts, ctx.getCurrentUser());

            SUSEProductSet targetProducts = null;
            if (migrationTargets == null) {
                // Installed products are 'unknown'
                return forward;
            } else if (migrationTargets.size() == 0) {
                // Latest SP is apparently installed
                request.setAttribute(LATEST_SP, true);
                return forward;
            } else if (migrationTargets.size() >= 1) {
                // At least one target available
                targetProducts = migrationTargets.get(0);
                request.setAttribute(TARGET_PRODUCTS, targetProducts);
            }

            // Get the base channel
            Channel suseBaseChannel = DistUpgradeManager.getProductBaseChannel(
                    targetProducts.getBaseProduct().getId(), ctx.getCurrentUser());

            // Determine mandatory channels
            List<EssentialChannelDto> requiredChannels =
                    DistUpgradeManager.getRequiredChannels(targetProducts);

            // Get available alternatives
            HashMap<ClonedChannel, List<Long>> alternatives = DistUpgradeManager.
                    getAlternatives(targetProducts, ctx.getCurrentUser());

            // Create new map, put original channels first
            HashMap<Channel, List<ChildChannelDto>> channelMap =
                    new LinkedHashMap<Channel, List<ChildChannelDto>>();
            channelMap.put(suseBaseChannel, getChildChannels(
                    suseBaseChannel, ctx, server, extractIDs(requiredChannels)));

            // Put cloned alternatives
            for (ClonedChannel alternative : alternatives.keySet()) {
                channelMap.put(alternative, getChildChannels(
                        alternative, ctx, server, alternatives.get(alternative)));
            }

            // Put all channel data to the request
            request.setAttribute(CHANNEL_MAP, channelMap);
        }
        else if (forward.getName().equals(CONFIRM)) {
            // Put product data
            SUSEProductSet targetProductSet = createProductSet(
                    targetBaseProduct, targetAddonProducts);
            request.setAttribute(BASE_PRODUCT, targetProductSet.getBaseProduct());
            request.setAttribute(ADDON_PRODUCTS, targetProductSet.getAddonProducts());
            // Put channel data
            Channel baseChannel = ChannelFactory.lookupByIdAndUser(
                    targetBaseChannel, ctx.getCurrentUser());
            request.setAttribute(BASE_CHANNEL, baseChannel);
            // Add those child channels that will be subscribed
            List<EssentialChannelDto> childChannels = getChannelDTOs(ctx, baseChannel,
                    Arrays.asList(targetChildChannels));
            request.setAttribute(CHILD_CHANNELS, childChannels);

            // Pre-populate the date picker
            DatePicker picker = getStrutsDelegate().prepopulateDatePicker(request, form,
                    "date", DatePicker.YEAR_RANGE_POSITIVE);
            request.setAttribute("date", picker);
        }
        else if (forward.getName().equals(SCHEDULE)) {
            // Create target product set from parameters
            SUSEProductSet targetProductSet = createProductSet(
                    targetBaseProduct, targetAddonProducts);

            // Setup list of channels to subscribe to
            List<Long> channelIDs = new ArrayList<Long>();
            channelIDs.addAll(Arrays.asList(targetChildChannels));
            channelIDs.add(targetBaseChannel);

            // Schedule the dist upgrade action
            Date earliest = getStrutsDelegate().readDatePicker(form, "date",
                    DatePicker.YEAR_RANGE_POSITIVE);
            Long actionID = scheduleDistUpgrade(ctx, server, targetProductSet,
                    channelIDs, dryRun, earliest);

            // Display a message to the user
            String product = targetProductSet.getBaseProduct().getFriendlyName();
            String msgKey = dryRun ? MSG_SCHEDULED_DRYRUN : MSG_SCHEDULED_MIGRATION;
            String[] msgParams = new String[] {server.getId().toString(),
                    actionID.toString(), product};
            getStrutsDelegate().saveMessage(msgKey, msgParams, request);
            Map<String, Long> params = new HashMap<String, Long>();
            params.put("sid", server.getId());
            return getStrutsDelegate().forwardParams(forward, params);
        }

        return forward;
    }

    /**
     * Find the destination given the current page and the dispatch string.
     * The order of actions is: SETUP -> CONFIRM -> SCHEDULE.
     * @param mapping
     * @param wizardStep
     * @param dispatch
     * @return
     */
    private ActionForward findForward(ActionMapping mapping, String wizardStep,
            String dispatch) {
        if (dispatch == null) {
            return mapping.findForward(SETUP);
        }

        ActionForward forward;
        if (wizardStep.equals(SETUP)) {
            forward = mapping.findForward(CONFIRM);
        }
        else if (wizardStep.equals(CONFIRM)) {
            forward = mapping.findForward(SCHEDULE);
        }
        else {
            // Unknown wizard step, go to setup
            forward = mapping.findForward(SETUP);
        }
        return forward;
    }

    /**
     * Create a list of all child channels of a given base channel as
     * {@link ChildChannelDto} objects.
     * @param baseChannel
     * @param ctx
     * @param s
     * @param requiredChannels
     * @return
     */
    @SuppressWarnings("unchecked")
    private List<ChildChannelDto> getChildChannels(Channel baseChannel,
            RequestContext ctx, Server s, List<Long> requiredChannels) {
        User user = ctx.getCurrentUser();
        List<Channel> channels = baseChannel.getAccessibleChildrenFor(user);

        // Sort channels by name
        Collections.sort(channels,
                new DynamicComparator("name", RequestContext.SORT_ASC));

        List<ChildChannelDto> childChannels = new ArrayList<ChildChannelDto>();
        for (int i = 0; i < channels.size(); i++) {
            Channel child = (Channel) channels.get(i);
            ChildChannelDto childChannel = new ChildChannelDto(child.getId(), child.getName(),
                    s.isSubscribed(child),
                    ChannelManager.isChannelFreeForSubscription(s, child),
                    child.isSubscribable(user.getOrg(), s));

            childChannel.setAvailableSubscriptions(
                    ChannelManager.getAvailableEntitlements(
                    user.getOrg(), child));

            childChannel.setAvailableFveSubscriptions(
                    ChannelManager.getAvailableFveEntitlements(
                    user.getOrg(), child));

            // Mark required channels as mandatory
            childChannel.setMandatory(requiredChannels.contains(childChannel.getId()));

            childChannels.add(childChannel);
        }
        return childChannels;
    }

    /**
     * Create a list of channels as given by their IDs and their base channel.
     * @param ctx
     * @param baseChannel
     * @param channelIDs
     * @return List of channels
     */
    @SuppressWarnings("unchecked")
    private List<EssentialChannelDto> getChannelDTOs(RequestContext ctx,
            Channel baseChannel, List<Long> channelIDs) {
        List<Channel> childChannels = baseChannel.getAccessibleChildrenFor(
                ctx.getCurrentUser());

        // Sort channels by name
        Collections.sort(childChannels,
                new DynamicComparator("name", RequestContext.SORT_ASC));

        List<EssentialChannelDto> channelDTOs = new ArrayList<EssentialChannelDto>();
        for (Channel child : childChannels) {
            if (channelIDs.contains(child.getId())) {
                EssentialChannelDto dto = new EssentialChannelDto(child);
                channelDTOs.add(dto);
            }
        }
        return channelDTOs;
    }

    /**
     * Create a {@link SUSEProductSet} from IDs given as {@link Long}s.
     * @param baseProduct
     * @param addonProducts
     * @return set of SUSE products
     */
    private SUSEProductSet createProductSet(Long baseProduct, Long[] addonProducts) {
        List<Long> addonProductsList = new ArrayList<Long>();
        addonProductsList.addAll(Arrays.asList(addonProducts));
        return new SUSEProductSet(baseProduct, addonProductsList);
    }

    /**
     * Extract IDs of all entries in a given list of {@link EssentialChannelDto}
     * objects.
     * @param channels
     * @return list of the channel IDs
     */
    private List<Long> extractIDs(List<EssentialChannelDto> channels) {
        List<Long> channelIDs = new ArrayList<Long>();
        for (EssentialChannelDto c : channels) {
            channelIDs.add(c.getId());
        }
        return channelIDs;
    }

    /**
     * Schedule a distribution upgrade action.
     * @param ctx
     * @param server
     * @param targetSet
     * @param channelIDs
     * @param dryRun
     */
    private Long scheduleDistUpgrade(RequestContext ctx, Server server,
            SUSEProductSet targetSet, List<Long> channelIDs,
            boolean dryRun, Date earliest) {
        // Create action details
        DistUpgradeActionDetails details = new DistUpgradeActionDetails();

        // Init product upgrades (base/addons)
        // Note: product upgrades are relevant for SLE 10 only!
        SUSEProductSet installedProducts = server.getInstalledProducts();
        SUSEProductUpgrade upgrade = new SUSEProductUpgrade(
                installedProducts.getBaseProduct(), targetSet.getBaseProduct());
        details.addProductUpgrade(upgrade);
        // Find matching targets for every addon
        for (SUSEProduct addon : installedProducts.getAddonProducts()) {
            upgrade = new SUSEProductUpgrade(addon,
                    DistUpgradeManager.findMatch(addon, targetSet.getAddonProducts()));
            details.addProductUpgrade(upgrade);
        }

        // Add individual channel tasks
        for (Channel c : server.getChannels()) {
            // Remove channels we already subscribed
            if (channelIDs.contains(c.getId())) {
                channelIDs.remove(c.getId());
            }
            else {
                // Unsubscribe from this channel
                DistUpgradeChannelTask task = new DistUpgradeChannelTask();
                task.setChannel(c);
                task.setTask(DistUpgradeChannelTask.UNSUBSCRIBE);
                details.addChannelTask(task);
            }
        }
        // Subscribe to all of the remaining channels
        for (Long cid: channelIDs) {
            DistUpgradeChannelTask task = new DistUpgradeChannelTask();
            task.setChannel(ChannelFactory.lookupById(cid));
            task.setTask(DistUpgradeChannelTask.SUBSCRIBE);
            details.addChannelTask(task);
        }

        // Set additional attributes
        details.setDryRun(dryRun ? 'Y' : 'N');
        details.setFullUpdate('Y');

        // Return the ID of the scheduled action
        return ActionManager.scheduleDistUpgrade(ctx.getCurrentUser(), server, details,
                earliest).getId();
    }
}
