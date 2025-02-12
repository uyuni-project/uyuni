import { hot } from "react-hot-loader/root";

import * as React from "react";
import { useState } from "react";

import { Button } from "components/buttons";
import withPageWrapper from "components/general/with-page-wrapper";
import { SectionToolbar } from "components/section-toolbar/section-toolbar";
import { HelpLink } from "components/utils";

import { HubsList } from "./iss_data_props";
import IssHubsList from "./list/iss-hubs-list";

const IssPeripheral = (hubsList: HubsList) => {
  const [hubs] = useState(hubsList.hubs);

  const title = (
    <div className="spacewalk-toolbar-h1">
      <h1>
        <i className="fa fa-cogs"></i>
        &nbsp;
        {t("ISS - Hub Configuration")}
        &nbsp;
        <HelpLink url="reference/admin/iss-peripheral.html" />
      </h1>
    </div>
  );

  const addHub = () => {
    window.pageRenderers?.spaengine?.navigate?.(`/rhn/manager/admin/iss/add/hub`);
  };

  let pageContent = <IssHubsList hubs={hubs} />;

  return (
    <div className="responsive-wizard">
      {title}
      <SectionToolbar>
        <div className="action-button-wrapper">
          <div className="btn-group">
            <Button id="addHub" icon="fa-plus" className={"btn-success"} text={t("Add Hub")} handler={addHub} />
          </div>
        </div>
      </SectionToolbar>
      <span>
        <h1>Known Hubs instances</h1>
      </span>
      {pageContent}
    </div>
  );
};

export default hot(withPageWrapper(IssPeripheral));
