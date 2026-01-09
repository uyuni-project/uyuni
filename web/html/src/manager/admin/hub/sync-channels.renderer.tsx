import { RolesProvider } from "core/auth/roles-context";
import SpaRenderer from "core/spa/spa-renderer";

import { ChannelSyncProps, SyncOrgsToPeripheralChannel } from "components/hub";
import { MessagesContainer } from "components/toastr";

export const renderer = (
  id: string,
  peripheralId: number,
  peripheralFqdn: string,
  channelsSyncData: ChannelSyncProps
) => {
  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <MessagesContainer />
      <SyncOrgsToPeripheralChannel
        peripheralId={peripheralId}
        peripheralFqdn={peripheralFqdn}
        availableOrgs={channelsSyncData.peripheralOrgs}
        channels={channelsSyncData.channels}
        mandatoryMap={new Map(channelsSyncData.mandatoryMap)}
        reversedMandatoryMap={new Map(channelsSyncData.reversedMandatoryMap)}
      />
    </RolesProvider>,
    document.getElementById(id)
  );
};
