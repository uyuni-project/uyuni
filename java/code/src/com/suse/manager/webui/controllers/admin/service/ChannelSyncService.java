package com.suse.manager.webui.controllers.admin.service;

import com.redhat.rhn.domain.user.User;

import com.suse.manager.hub.HubManager;
import com.suse.manager.model.hub.OrgInfoJson;
import com.suse.manager.webui.controllers.admin.beans.IssV3ChannelResponse;
import com.suse.manager.webui.utils.token.TokenParsingException;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ChannelSyncService {

    private final HubManager hubManager;

    public ChannelSyncService() {
        this(new HubManager());
    }

    public ChannelSyncService(HubManager hubManagerIn) {
        this.hubManager = hubManagerIn;
    }

    public SyncPeripheralChannelModel getSyncedAndAvailableChannels(User user, Long peripheralId) {
        List<OrgInfoJson> peripheralOrgs;
        Set<IssV3ChannelResponse> peripheralCustomChannels;
        Set<IssV3ChannelResponse> peripheralVendorChannels;
        Set<IssV3ChannelResponse> hubVendorChannels;
        Set<IssV3ChannelResponse> hubCustomChannels;
        try {
            // Can't page this, we need everything.
            peripheralOrgs = hubManager.getPeripheralOrgs(user, peripheralId);
            Set<IssV3ChannelResponse> peripheralChannels = hubManager.getPeripheralChannels(user, peripheralId);
            // Partition here so we don't go inside the list two times
            Map<Boolean, List<IssV3ChannelResponse>> partitioned = peripheralChannels.stream()
                    .collect(Collectors.partitioningBy(ch -> ch.getChannelOrg().getOrgId() != null));
            peripheralCustomChannels = new HashSet<>(partitioned.get(true));
            peripheralVendorChannels = new HashSet<>(partitioned.get(true));
            hubVendorChannels = hubManager.getHubVendorChannels(user);
            hubCustomChannels = hubManager.getHubCustomChannels(user);
        }
        catch (CertificateException | IOException |
               TokenParsingException eIn) {
            throw new RuntimeException(eIn);
        }
        // Utility filter lambdas.
        Function<Set<String>, Predicate<String>> availableFilter = peripheralLabels ->
                hubLabel -> !peripheralLabels.contains(hubLabel);
        Function<Set<String>, Predicate<String>> syncedFilter = peripheralLabels ->
                peripheralLabels::contains;
        // Channels that are not synced
        List<IssV3ChannelResponse> availableCustomChannels = filterHubChannelsByPeripheral(
                hubCustomChannels,
                peripheralCustomChannels,
                IssV3ChannelResponse::getChannelLabel,
                IssV3ChannelResponse::getChannelLabel,
                availableFilter
        );
        List<IssV3ChannelResponse> availableVendorChannels = filterHubChannelsByPeripheral(
                hubVendorChannels,
                peripheralVendorChannels,
                IssV3ChannelResponse::getChannelLabel,
                IssV3ChannelResponse::getChannelLabel,
                availableFilter
        );
        // Channel that are already synced
        List<IssV3ChannelResponse> syncedCustomChannels = filterHubChannelsByPeripheral(
                hubCustomChannels,
                peripheralCustomChannels,
                IssV3ChannelResponse::getChannelLabel,
                IssV3ChannelResponse::getChannelLabel,
                syncedFilter
        );
        List<IssV3ChannelResponse> syncedVendorChannels = filterHubChannelsByPeripheral(
                hubVendorChannels,
                peripheralVendorChannels,
                IssV3ChannelResponse::getChannelLabel,
                IssV3ChannelResponse::getChannelLabel,
                syncedFilter
        );
        return new SyncPeripheralChannelModel(
                peripheralOrgs,
                syncedCustomChannels,
                syncedVendorChannels,
                availableCustomChannels,
                availableVendorChannels
        );
    }

    /**
     * Generic helper function that filters two list with a set for fast access
     *
     * @param hubChannels
     * @param peripheralChannels
     * @param hubLabelExtractor
     * @param peripheralLabelExtractor
     * @param filterFunction
     * @param <H>
     * @param <P>
     * @return
     */
    private static <H, P> List<H> filterHubChannelsByPeripheral(
            Set<H> hubChannels,
            Set<P> peripheralChannels,
            Function<H, String> hubLabelExtractor,
            Function<P, String> peripheralLabelExtractor,
            Function<Set<String>, Predicate<String>> filterFunction
    ) {
        Set<String> peripheralLabels = peripheralChannels.stream()
                .map(peripheralLabelExtractor)
                .collect(Collectors.toSet());
        Predicate<String> hubFilter = filterFunction.apply(peripheralLabels);
        return hubChannels.stream()
                .filter(hub -> hubFilter.test(hubLabelExtractor.apply(hub)))
                .collect(Collectors.toList());
    }

    public static class SyncPeripheralChannelModel {
        private final List<OrgInfoJson> peripheralOrgs;
        private final List<IssV3ChannelResponse> syncedPeripheralCustomChannels;
        private final List<IssV3ChannelResponse> syncedPeripheralVendorChannels;
        private final List<IssV3ChannelResponse> availableCustomChannels;
        private final List<IssV3ChannelResponse> availableVendorChannels;

        /**
         * Helper class
         * @param peripheralOrgsIn
         * @param syncedPeripheralCustomChannelsIn
         * @param syncedPeripheralVendorChannelsIn
         * @param availableCustomChannelsIn
         * @param availableVendorChannelsIn
         */
        public SyncPeripheralChannelModel(
                List<OrgInfoJson> peripheralOrgsIn,
                List<IssV3ChannelResponse> syncedPeripheralCustomChannelsIn,
                List<IssV3ChannelResponse> syncedPeripheralVendorChannelsIn,
                List<IssV3ChannelResponse> availableCustomChannelsIn,
                List<IssV3ChannelResponse> availableVendorChannelsIn) {
            this.peripheralOrgs = peripheralOrgsIn;
            // Group synced custom channels by orgId.
            this.syncedPeripheralCustomChannels = syncedPeripheralCustomChannelsIn;
            this.syncedPeripheralVendorChannels = syncedPeripheralVendorChannelsIn;
            this.availableCustomChannels = availableCustomChannelsIn;
            this.availableVendorChannels = availableVendorChannelsIn;
        }

        public List<OrgInfoJson> getPeripheralOrgs() {
            return peripheralOrgs;
        }

        public List<IssV3ChannelResponse> getSyncedPeripheralCustomChannels() {
            return syncedPeripheralCustomChannels;
        }

        public List<IssV3ChannelResponse> getSyncedPeripheralVendorChannels() {
            return syncedPeripheralVendorChannels;
        }

        public List<IssV3ChannelResponse> getAvailableCustomChannels() {
            return availableCustomChannels;
        }

        public List<IssV3ChannelResponse> getAvailableVendorChannels() {
            return availableVendorChannels;
        }
    }

}
