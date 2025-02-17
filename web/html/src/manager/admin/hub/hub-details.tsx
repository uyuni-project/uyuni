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
      <span>{t("No Hub is currently configured for this server.")}</span>
      <span>{t("Refresh this page after configuring this server as a Peripheral to see the Hub details")}</span>
    </>
  );
  if (hub != null) {
    let rootCABlob = new Blob([hub.rootCA], { type: "text/plain" });
    let dlUrl = URL.createObjectURL(rootCABlob);
    pageContent = (
      <>
        <div>Hub FQDN: {hub.fqdn}</div>
        <div>
          <a
            href={dlUrl}
            onClick={() => {
              URL.revokeObjectURL(dlUrl);
            }}
          >
            <i className="bi bi-download">Download</i>
          </a>
        </div>
      </>
    );
  }
  return (
    <div className="responsive-wizard">
      {title}
      <div>
        <h1>Known Hub Instance</h1>
      </div>
      {pageContent}
    </div>
  );
};

export default hot(withPageWrapper(HubDetails));
