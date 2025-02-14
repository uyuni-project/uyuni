import { hot } from "react-hot-loader/root";

import { useState } from "react";

import withPageWrapper from "components/general/with-page-wrapper";
import { HelpLink } from "components/utils";

import { HubDetailData } from "./iss_data_props";

export type HubDetailsProps = {
  hub: HubDetailData;
};

const HubDetails = (prop: HubDetailsProps) => {
  const [hub] = useState(prop.hub);

  const title = (
    <div className="spacewalk-toolbar-h1">
      <h1>
        <i className="fa fa-cogs"></i>
        &nbsp;
        {t("Hub Details")}
        &nbsp;
        <HelpLink url="reference/admin/hub/hub-details.html" />
      </h1>
    </div>
  );
  let pageContent = (
    <>
      <span>{t("Noo Hub is currently configured for this server.")}</span>
      <span>{t("Refresh this page after configuring this server as a Peripheral to see the Hub details")}</span>
    </>
  );
  if (hub != null) {
    pageContent = <div>Hub FQDN: {hub.fqdn}</div>;
  }
  return (
    <div className="responsive-wizard">
      {title}
      <span>
        <h1>Known Hub Instance</h1>
      </span>
      {pageContent}
    </div>
  );
};

export default hot(withPageWrapper(HubDetails));
