import { hot } from "react-hot-loader/root";

import * as React from "react";

import withPageWrapper from "components/general/with-page-wrapper";
import { SyncOrgsToPeripheralChannel, SyncPeripheralsProps } from "components/hub";

const IssPeripheralDetails = (props: SyncPeripheralsProps) => {
  return (
    <div>
      <SyncOrgsToPeripheralChannel
        availableOrgs={props.availableOrgs}
        availableCustomChannels={props.availableCustomChannels}
        availableVendorChannels={props.availableVendorChannels}
        syncedCustomChannels={props.syncedCustomChannels}
        syncedVendorChannels={props.syncedVendorChannels}
      />
    </div>
  );
};

export default hot(withPageWrapper(IssPeripheralDetails));
