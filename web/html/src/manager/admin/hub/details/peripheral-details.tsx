import { hot } from "react-hot-loader/root";

import * as React from "react";

import withPageWrapper from "components/general/with-page-wrapper";
import { SyncOrgsToPeripheralChannel, SyncPeripheralsProps } from "components/hub";

export type PeripheralDetails = {
  fqdn: string;
};

const IssPeripheralDetails = (props: PeripheralDetails) => {
  return <div>"Update Peripheral Configuration"</div>;
};

export default hot(withPageWrapper(IssPeripheralDetails));
