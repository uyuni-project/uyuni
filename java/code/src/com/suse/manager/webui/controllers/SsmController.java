/**
 * Copyright (c) 2018 SUSE LLC
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

package com.suse.manager.webui.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.context.Context;
import com.redhat.rhn.frontend.dto.EssentialChannelDto;
import com.redhat.rhn.frontend.dto.EssentialServerDto;
import com.redhat.rhn.frontend.dto.SystemsPerChannelDto;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.ssm.ScheduleChannelChangesResultDto;
import com.redhat.rhn.manager.ssm.SsmManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.webui.utils.gson.JsonResult;
import com.redhat.rhn.manager.ssm.SsmServerDto;
import com.suse.manager.webui.utils.gson.SsmAllowedBaseChannelsJson;
import com.redhat.rhn.manager.ssm.SsmAllowedChildChannelsDto;
import com.suse.manager.webui.utils.gson.SsmBaseChannelChangesDto;
import com.redhat.rhn.manager.ssm.SsmChannelDto;
import com.suse.manager.webui.utils.gson.SsmScheduleChannelChangesJson;
import com.suse.manager.webui.utils.gson.SsmScheduleChannelChangesResultJson;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import spark.Request;
import spark.Response;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;

/**
 * Controller class providing backend code for the SSM pages.
 */
public class SsmController {

    /** Logger */
    private static final Logger LOG = Logger.getLogger(SsmController.class);

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .serializeNulls()
            .create();

    private SsmController() { }

    /**
     * Get the list of base-channels available to the System Set.
     *
     * @param request the http request
     * @param response the http response
     * @param user the user
     * @return the json response
     */
    public static String getBaseChannels(Request request, Response response, User user) {
        List<SsmAllowedBaseChannelsJson> result = new ArrayList<>();

        for (SystemsPerChannelDto spc : ChannelManager.baseChannelsInSet(user)) {
            // We don't need to do user auth, here because if the user doesn't have
            // subscribe access to the subscribed channel we still want to let them
            //  change the systems base channel
            Channel c = ChannelFactory.lookupById(spc.getId().longValue());
            SsmAllowedBaseChannelsJson allowedBaseJson = new SsmAllowedBaseChannelsJson();
            allowedBaseJson.setBase(new SsmChannelDto(c.getId(), c.getName(), c.isCustom()));

            List<EssentialChannelDto> compatibles = ChannelManager
                    .listCompatibleBaseChannelsForChannel(user, c);

            allowedBaseJson.setAllowedBaseChannels(
                compatibles.stream().map(cc ->
                        new SsmChannelDto(
                                cc.getId(), cc.getName(), cc.isCustom()))
                        .collect(Collectors.toList()));
            List<Server> serversByChannel = SsmManager.findServersInSetByChannel(user, c.getId());
            allowedBaseJson.setServers(serversByChannel.stream()
                    .map(s -> new SsmServerDto(s.getId(), s.getName()))
                    .collect(Collectors.toList()));
            result.add(allowedBaseJson);
        }

        createAllowedBaseChannelsForUnbasedSystems(user)
                .ifPresent(nobase -> result.add(0, nobase));

        return json(GSON, response, JsonResult.success(result));
    }

    /**
     * Create the data-structures needed for systems that aren't currently subscribed to
     * any base channels
     */
    private static Optional<SsmAllowedBaseChannelsJson> createAllowedBaseChannelsForUnbasedSystems(User user) {
        // How many systems don't currently have a base channel?
        DataResult<EssentialServerDto> noBase =
                SystemManager.systemsWithoutBaseChannelsInSet(user);

        // If there are any...
        if (CollectionUtils.isNotEmpty(noBase)) {
            // ...create the "(None)" row
            SsmAllowedBaseChannelsJson rslt = new SsmAllowedBaseChannelsJson();
            rslt.setBase(new SsmChannelDto(-1, "none", false));

            List<SsmChannelDto> allowed =
                Stream.concat(
                        ChannelFactory.listCustomBaseChannelsForSSMNoBase(user).stream(),
                        ChannelFactory.listCompatibleBasesForSSMNoBaseInNullOrg(user).stream())
                        .map(c ->
                            new SsmChannelDto(c.getId(), c.getName(), c.isCustom()))
                        .collect(Collectors.toList());
            rslt.setAllowedBaseChannels(allowed);
            rslt.setServers(noBase.stream()
                    .map(s -> new SsmServerDto(s.getId(), s.getName()))
                    .collect(Collectors.toList()));
            return Optional.of(rslt);

        }
        return Optional.empty();
    }

    /**
     * Get the list of child-channels available to the System Set
     * and create a data-structure mapping them to their respective base-channels.
     *
     * @param request the http request
     * @param response the http response
     * @param user the user
     * @return the json response
     */
    public static String computeAllowedChannelChanges(Request request, Response response, User user) {
        SsmBaseChannelChangesDto changes = GSON.fromJson(request.body(), SsmBaseChannelChangesDto.class);
        List<SsmAllowedChildChannelsDto> result = SsmManager.computeAllowedChannelChanges(changes, user);
        return json(GSON, response, JsonResult.success(result));
    }

    /**
     * Schedule changing the channels of systems in SSM.
     *
     * @param request the http request
     * @param response the http response
     * @param user the user
     * @return the json response
     */
    public static String changeChannels(Request request, Response response, User user) {
        SsmScheduleChannelChangesJson changes = GSON.fromJson(request.body(), SsmScheduleChannelChangesJson.class);
        ZoneId zoneId = Context.getCurrentContext().getTimezone().toZoneId();
        Date earliestDate = Date.from(
                changes.getEarliest().orElseGet(LocalDateTime::now).atZone(zoneId).toInstant()
        );

        ActionChain actionChain = changes.getActionChain()
                .filter(StringUtils::isNotEmpty)
                .map(label -> ActionChainFactory.getOrCreateActionChain(label, user))
                .orElse(null);

        List<ScheduleChannelChangesResultDto> scheduleResult =
                SsmManager.scheduleChannelChanges(changes.getChanges(), earliestDate, actionChain, user);

        SsmScheduleChannelChangesResultJson result = new SsmScheduleChannelChangesResultJson(
                Optional.ofNullable(actionChain).map(ActionChain::getId),
                scheduleResult
        );
        return json(GSON, response, JsonResult.success(result));
    }

}
