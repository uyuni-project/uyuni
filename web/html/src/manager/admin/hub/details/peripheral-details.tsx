import { hot } from "react-hot-loader/root";

import * as React from "react";

import withPageWrapper from "components/general/with-page-wrapper";
import { SyncOrgsToPeripheralChannel, SyncPeripheralsProps } from "components/hub";

const IssPeripheralDetails = (syncChannelProp: SyncPeripheralsProps) => {
  let componentContent = (
    <div>
      <SyncOrgsToPeripheralChannel
        availableOrgs={syncChannelProp.availableOrgs}
        availableCustomChannels={syncChannelProp.availableCustomChannels}
        availableVendorChannels={syncChannelProp.availableVendorChannels}
        selectedCustomChannels={syncChannelProp.selectedCustomChannels}
      />
    </div>
  );
  return componentContent;
};

export default hot(withPageWrapper(IssPeripheralDetails));
